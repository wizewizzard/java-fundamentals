package edu.wz.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class JDBCTest {
    private static Connection dbConnection;

    @BeforeAll
    public static void setUp() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/wuz1moo";
        String username = "wuz1moo";
        String password = "wuz1mooPassword";
        dbConnection = DriverManager.getConnection(url, username, password);
    }

    @Test
    public void testConnection(){
        assertThat(dbConnection).isNotNull();
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        if(dbConnection != null){
            dbConnection.close();
        }
    }
}
