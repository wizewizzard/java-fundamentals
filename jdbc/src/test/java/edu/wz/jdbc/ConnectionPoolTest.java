package edu.wz.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

public class ConnectionPoolTest {

    private static DataSource connPool;

    @BeforeAll
    public static void setUp(){
        connPool = getDatasource();
    }

    @Test
    public void gettingConnectionFromConnectionPool() throws SQLException {
        try(Connection connection = connPool.getConnection()){
            assertThat(connection.isValid(150)).isTrue();
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
