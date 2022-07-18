package edu.wz.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.*;

public class StatementsTest {
    private static DataSource connPool;

    @BeforeAll
    public static void setUp() throws SQLException {
        connPool = getDatasource();
        try (Connection connection = connPool.getConnection()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS customer ("
                    + "customer_id bigserial PRIMARY KEY, "
                    + "name varchar(64) NOT NULL,"
                    + "phone varchar(64) NOT NULL"
                    + ")";
            connection.createStatement().execute(createTableQuery);
            String insertionQuery = "INSERT INTO customer (name, phone) VALUES "
                    + "('Ben', '81239543'),"
                    + "('Hannah', '734193452'),"
                    + "('John', '99324123')"
                    ;
            connection.createStatement().execute(insertionQuery);
        }
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        try (Connection connection = connPool.getConnection()) {
            String truncateTable = "TRUNCATE customer";
            connection.createStatement().execute(truncateTable);
            String dropTablesSql = "DROP TABLE IF EXISTS item, vehicle";
            Statement statement = connection.createStatement();
            statement.execute(dropTablesSql);
        }
    }

    /**
     * execute returns false because it has no ResultSet and getUpdateCount equals to 0 as it is db structure
     * modification query
     */
    @Test
    public void statementExecute(){
        try(Connection connection = connPool.getConnection()){
            try (Statement statement = connection.createStatement()){
                String createTableQuery = "CREATE TABLE IF NOT EXISTS item ("
                        + "item_id bigserial PRIMARY KEY, "
                        + "name varchar(64) NOT NULL"
                        + ")";
                assertThat(statement.execute(createTableQuery)).isFalse();
                assertThat(statement.getUpdateCount()).isEqualTo(0);
            }
        } catch (SQLException e) {
            fail("Exception: ", e);
        }
    }

    @Test
    public void statementExecuteReturnsResultSet(){
        try(Connection connection = connPool.getConnection()){
            String query = "SELECT * FROM customer";
            try(Statement statement = connection.createStatement()){
                assertThat(statement.execute(query)).isTrue();
                assertThat(statement.getUpdateCount()).isEqualTo(-1);
                ResultSet resultSet = statement.getResultSet();
                while(resultSet.next()){
                    assertThat(resultSet.getString("name")).isIn("Ben", "Hannah", "John");
                }
            }

        } catch (SQLException e) {
            fail("Exception: ", e);
        }
    }

    @Test
    public void statementExecuteInsertionAndUpdate(){
        try(Connection connection = connPool.getConnection()){
            String createTableQuery = "CREATE TABLE IF NOT EXISTS vehicle ("
                    + "vehicle_id bigserial PRIMARY KEY, "
                    + "year varchar(64) NOT NULL, "
                    + "model varchar(64) "
                    + ")";
            try(Statement createTableStatement = connection.createStatement()){
                createTableStatement.execute(createTableQuery);
            }
            String insertionQuery = "INSERT INTO vehicle (year, model) VALUES "
                    + "('1995', 'Jeep'), "
                    + "('2016', 'Toyota')";
            try(Statement insertionStatement = connection.createStatement()){
                assertThat(insertionStatement.execute(insertionQuery)).isFalse();
                assertThat(insertionStatement.getUpdateCount()).isEqualTo(2);
            }

            String updateQuery = "UPDATE vehicle SET year='1994' WHERE model='Jeep'";
            try(Statement updateStatement = connection.createStatement()){
                assertThat(updateStatement.execute(updateQuery)).isFalse();
                assertThat(updateStatement.getUpdateCount()).isEqualTo(1);
            }
        } catch (SQLException e) {
            fail("Exception: ", e);
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
