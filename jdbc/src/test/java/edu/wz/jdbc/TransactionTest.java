package edu.wz.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;

import static org.assertj.core.api.Assertions.*;

public class TransactionTest {
    private static DataSource connPool;

    @BeforeAll
    public static void setUp() throws SQLException {
        connPool = getDatasource();
        try (Connection connection = connPool.getConnection()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS route ("
                    + "route_id bigserial PRIMARY KEY, "
                    + "number int NOT NULL,"
                    + "origin varchar(64) NOT NULL,"
                    + "destination varchar(64) NOT NULL"
                    + ")";
            try (Statement statement = connection.createStatement()) {
                statement.execute(createTableQuery);
            }
            String insertionQuery = "INSERT INTO route (number, origin, destination) VALUES "
                    + "(487, 'Beach', 'Downtown'),"
                    + "(888, 'University', 'Bowling'),"
                    + "(3, 'Police Department', 'Stadium')";
            try (Statement statement = connection.createStatement()) {
                statement.execute(insertionQuery);
            }
        }
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        try (Connection connection = connPool.getConnection()) {
            String truncateTable = "DROP TABLE route";
            connection.createStatement().execute(truncateTable);
        }
    }

    @Test
    public void changesMadeWithoutAutoCommitModeShouldNotBePersisted() throws SQLException {
        try(Connection connection = connPool.getConnection()){
            String insertionQuery = "INSERT INTO route (number, origin, destination) VALUES (?, ?, ?)";
            String deleteQuery = "DELETE FROM route WHERE number=?";

            connection.setAutoCommit(false);
            try(PreparedStatement prStmnt = connection.prepareStatement(insertionQuery)) {
                prStmnt.setInt(1, 65);
                prStmnt.setString(2, "Forest");
                prStmnt.setString(3, "Swimming pool");
                assertThat(prStmnt.executeUpdate()).isEqualTo(1);
            }
            try(PreparedStatement prStmnt = connection.prepareStatement(deleteQuery)) {
                prStmnt.setInt(1, 888);
                assertThat(prStmnt.executeUpdate()).isEqualTo(1);
            }
            //connection.commit();
        }
        try(Connection connection = connPool.getConnection()){
            String selectQuery = "SELECT * FROM route WHERE number = ?";
            try(PreparedStatement prStmnt = connection.prepareStatement(selectQuery)){
                prStmnt.setInt(1, 65);
                ResultSet resultSet = prStmnt.executeQuery();
                assertThat(resultSet.next()).isFalse();
            }
            try(PreparedStatement prStmnt = connection.prepareStatement(selectQuery)){
                prStmnt.setInt(1, 888);
                ResultSet resultSet = prStmnt.executeQuery();
                assertThat(resultSet.next()).isTrue();
            }
        }
    }

    @Test
    public void transactionCommitPersistsChanges() throws SQLException {
        try(Connection connection = connPool.getConnection()){
            String insertionQuery = "INSERT INTO route (number, origin, destination) VALUES (?, ?, ?)";
            String deleteQuery = "DELETE FROM route WHERE number=?";

            connection.setAutoCommit(false);
            try(PreparedStatement prStmnt = connection.prepareStatement(insertionQuery)) {
                prStmnt.setInt(1, 909);
                prStmnt.setString(2, "Forest");
                prStmnt.setString(3, "Swimming pool");
                assertThat(prStmnt.executeUpdate()).isEqualTo(1);
            }
            try(PreparedStatement prStmnt = connection.prepareStatement(deleteQuery)) {
                prStmnt.setInt(1, 888);
                assertThat(prStmnt.executeUpdate()).isEqualTo(1);
            }
            connection.commit();
        }
        try(Connection connection = connPool.getConnection()){
            String selectQuery = "SELECT * FROM route WHERE number = ?";
            try(PreparedStatement prStmnt = connection.prepareStatement(selectQuery)){
                prStmnt.setInt(1, 909);
                ResultSet resultSet = prStmnt.executeQuery();
                assertThat(resultSet.next()).isTrue();
            }
            try(PreparedStatement prStmnt = connection.prepareStatement(selectQuery)){
                prStmnt.setInt(1, 888);
                ResultSet resultSet = prStmnt.executeQuery();
                assertThat(resultSet.next()).isFalse();
            }
        }
    }

    @Test
    public void changesMadeWhenTransactionWasRolledBackShouldNotBePersisted() throws SQLException {
        try(Connection connection = connPool.getConnection()){
            String insertionQuery = "INSERT INTO route (number, origin, destination) VALUES (?, ?, ?)";
            try{
                connection.setAutoCommit(false);
                try(PreparedStatement prStmnt = connection.prepareStatement(insertionQuery)) {
                    prStmnt.setInt(1, 65);
                    prStmnt.setString(2, "Forest");
                    prStmnt.setString(3, "Swimming pool");
                    prStmnt.executeUpdate();
                }
                try(PreparedStatement prStmnt = connection.prepareStatement(insertionQuery)) {
                    prStmnt.setString(1, null);
                    prStmnt.setString(2, "Blah");
                    prStmnt.setString(3, "Booo");
                    prStmnt.executeUpdate();
                    //throw new RuntimeException("Oops an error");
                }
                connection.commit();
                fail("Commit happened");
            }
            catch (SQLException | RuntimeException exc){
                connection.rollback();
            }
        }

        try(Connection connection = connPool.getConnection()){
            String selectQuery = "SELECT * FROM route WHERE number = ?";
            try(PreparedStatement prStmnt = connection.prepareStatement(selectQuery)){
                prStmnt.setInt(1, 65);
                ResultSet resultSet = prStmnt.executeQuery();
                assertThat(resultSet.next()).isFalse();
            }
            try(PreparedStatement prStmnt = connection.prepareStatement(selectQuery)){
                prStmnt.setInt(1, 888);
                ResultSet resultSet = prStmnt.executeQuery();
                assertThat(resultSet.next()).isTrue();
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
