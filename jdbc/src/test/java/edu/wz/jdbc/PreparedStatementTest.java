package edu.wz.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.*;

import static org.assertj.core.api.Assertions.*;

public class PreparedStatementTest {
    private static DataSource connPool;

    @BeforeAll
    public static void setUp() throws SQLException {
        connPool = getDatasource();
        try (Connection connection = connPool.getConnection()) {
            String createStudentTableQuery = "CREATE TABLE IF NOT EXISTS student ("
                    + "student_id bigserial PRIMARY KEY, "
                    + "name varchar(64) NOT NULL,"
                    + "phone varchar(64) NOT NULL,"
                    + "age int NOT NULL"
                    + ")";
            try(Statement statement = connection.createStatement()){
                statement.execute(createStudentTableQuery);
            }
            String createCourseTableQuery = "CREATE TABLE IF NOT EXISTS course ("
                    + "course_id bigserial PRIMARY KEY, "
                    + "name varchar(64) NOT NULL"
                    + ")";
            try(Statement statement = connection.createStatement()){
                statement.execute(createCourseTableQuery);
            }

            String studentInsertionQuery = "INSERT INTO student (name, phone, age) VALUES "
                    + "('Ben', '81239543', 38),"
                    + "('Hannah', '734193452', 25),"
                    + "('John', '99324123', 45)"
                    ;
            try(Statement statement = connection.createStatement()){
                statement.execute(studentInsertionQuery);
            }
            String courseInsertionQuery = "INSERT INTO course (name) VALUES "
                    + "('Math'),"
                    + "('English'),"
                    + "('History'),"
                    + "('Biology'),"
                    + "('Geography')"
                    ;
            try(Statement statement = connection.createStatement()){
                statement.execute(courseInsertionQuery);
            }
            String createJoinTable = "CREATE TABLE IF NOT EXISTS students_courses ("
                    + "student_id int, "
                    + "course_id int, "
                    + "FOREIGN KEY (student_id) REFERENCES student(student_id), "
                    + "FOREIGN KEY (course_id) REFERENCES course(course_id) "
                    + ")";
            try(Statement statement = connection.createStatement()){
                statement.execute(createJoinTable);
            }
            String studentCoursesInsertionQuery = "INSERT INTO students_courses (student_id, course_id) VALUES "
                    + "(1, 1),"
                    + "(1, 2),"
                    + "(1, 4),"
                    + "(2, 1),"
                    + "(2, 3),"
                    + "(2, 5),"
                    + "(3, 5),"
                    + "(3, 1)"
                    ;
            try(Statement statement = connection.createStatement()){
                statement.execute(studentCoursesInsertionQuery);
            }
        }
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        try (Connection connection = connPool.getConnection()) {
            String truncateTable = "DROP TABLE students_courses, student, course";
            connection.createStatement().execute(truncateTable);
        }
    }

    @Test
    public void preparedStatementTest() throws SQLException {
        String preparedInsertionQuery = "INSERT INTO student (name, phone, age) VALUES (?, ?, ?)";
        String preparedDeleteQuery = "DELETE FROM student where name=?";
        String preparedUpdate = "UPDATE student SET name=?, phone=?, age=? WHERE name=?";
        try(Connection connection = connPool.getConnection()){
            try(PreparedStatement prInsStmnt = connection.prepareStatement(preparedInsertionQuery)){
                prInsStmnt.setString(1, "Polly");
                prInsStmnt.setString(2, "6512351");
                prInsStmnt.setInt(3, 13);
                assertThat(prInsStmnt.execute()).isFalse();
                assertThat(prInsStmnt.getUpdateCount()).isEqualTo(1);
            }
            try(PreparedStatement prUpdStmnt = connection.prepareStatement(preparedUpdate)){
                prUpdStmnt.setString(1, "Polly Dolly");
                prUpdStmnt.setString(2, "6512351");
                prUpdStmnt.setInt(3, 14);
                prUpdStmnt.setString(4, "Polly");
                assertThat(prUpdStmnt.execute()).isFalse();
                assertThat(prUpdStmnt.getUpdateCount()).isEqualTo(1);
            }
            try(PreparedStatement prDelStmnt = connection.prepareStatement(preparedDeleteQuery)){
                prDelStmnt.setString(1, "Polly Dolly");
                assertThat(prDelStmnt.execute()).isFalse();
                assertThat(prDelStmnt.getUpdateCount()).isEqualTo(1);
            }
        }
    }

    @Test
    public void joinQueryTest() throws SQLException {
        String preparedQuery = "SELECT s.name as student_name, c.name as course_name FROM student s " +
                "LEFT JOIN students_courses sc using(student_id) " +
                "LEFT JOIN course c using(course_id)" +
                "WHERE s.name=?";
        try(Connection connection = connPool.getConnection()){
            try(PreparedStatement prStmnt = connection.prepareStatement(preparedQuery)){
                prStmnt.setString(1, "Ben");
                ResultSet resultSet = prStmnt.executeQuery();

                int count = 0;
                while(resultSet.next()){
                    count++;
                    assertThat(resultSet.getString("course_name")).isIn("Math", "English", "Biology");
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
