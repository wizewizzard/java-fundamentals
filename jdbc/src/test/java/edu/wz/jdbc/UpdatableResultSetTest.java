package edu.wz.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class UpdatableResultSetTest {
    private static DataSource connPool;

    @BeforeAll
    public static void setUp() throws SQLException {
        connPool = getDatasource();
        try (Connection connection = connPool.getConnection()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS employee ("
                    + "employee_id bigserial PRIMARY KEY, "
                    + "name varchar(64) NOT NULL,"
                    + "phone varchar(64) NOT NULL,"
                    + "salary int NOT NULL,"
                    + "age int NOT NULL"
                    + ")";
            try (Statement statement = connection.createStatement()) {
                statement.execute(createTableQuery);
            }
            String insertionQuery = "INSERT INTO employee (name, phone, salary, age) VALUES "
                    + "('Ben', '81239543', 24000, 23),"
                    + "('Hannah', '734193452', 39000, 40),"
                    + "('John', '99324123', 49000, 38)";
            try (Statement statement = connection.createStatement()) {
                statement.execute(insertionQuery);
            }
        }
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        try (Connection connection = connPool.getConnection()) {
            String truncateTable = "DROP TABLE employee";
            connection.createStatement().execute(truncateTable);
        }
    }

    @Test
    public void shouldUpdateTheRows() throws SQLException {
        try (Connection connection = connPool.getConnection()) {
            String selectionQuery = "SELECT * FROM employee WHERE age > ?";
            Map<Integer, Integer> map = new HashMap<>();
            try (PreparedStatement prStmnt = connection.prepareStatement(selectionQuery,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                prStmnt.setInt(1, 35);
                ResultSet updatableResultSet = prStmnt.executeQuery();
                while (updatableResultSet.next()) {
                    map.put(updatableResultSet.getInt("employee_id"), updatableResultSet.getInt("salary"));
                    updatableResultSet.updateInt("salary", updatableResultSet.getInt("salary") + 1000);
                    updatableResultSet.updateRow();
                }
                updatableResultSet.moveToInsertRow();
                updatableResultSet.updateString("name", "Aaron");
                updatableResultSet.updateString("phone", "8124344");
                updatableResultSet.updateInt("salary", 36000);
                updatableResultSet.updateInt("age", 67);
                updatableResultSet.insertRow();
            }

            try (PreparedStatement prStmnt = connection.prepareStatement(selectionQuery)) {
                prStmnt.setInt(1, 35);
                ResultSet resultSet = prStmnt.executeQuery();
                int count = 0;
                while (resultSet.next()) {
                    count++;
                    if(!resultSet.getString("name").equals("Aaron"))
                        assertThat(resultSet.getInt("salary"))
                                .isEqualTo(map.get(resultSet.getInt("employee_id")) + 1000);
                    else
                        assertThat(resultSet.getInt("salary")).isEqualTo(36000);

                }
                assertThat(count).isEqualTo(3);
            }
        }
    }

    private static DataSource getDatasource() {
        HikariConfig config = new HikariConfig();
        config.setUsername("wuz1moo");
        config.setPassword("wuz1mooPassword");
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/wuz1moo");
        DataSource ds = new HikariDataSource(config);
        return ds;
    }

}
