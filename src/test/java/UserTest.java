import app.controllers.UserController;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {


    private static ConnectionPool connectionPool;

    @BeforeAll
    static void setUp() {
        // Use test database credentials here
        String user = "postgres";
        String password = "datdat2025!";
        String url = "jdbc:postgresql://164.90.223.15:5432/%s?currentSchema=public"; // or whatever schema you use
        String db = "carport";

        connectionPool = ConnectionPool.getInstance(user, password, url, db);
    }

    @BeforeEach
    void setupTestData(){} //not setup needed


    @Test
    void testCreateUser() throws SQLException, DatabaseException {
        String username = "456";
        String password = "abc";
        String role = "customer";

        // Act: try to create user
        UserMapper.createUser(username, password, role, connectionPool);

        // Assert: fetch the user and check data
        try (Connection conn = connectionPool.getConnection()) {
            String sql = "SELECT * FROM customer WHERE customer_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(username, rs.getString("customer_name"));
                    assertEquals(password, rs.getString("password"));
                    assertEquals(role, rs.getString("role"));
                }
            }
        }
    }



    @Test
    void testDuplicateUsername() {
        String username = "test123";
        String password = "123";
        String role = "customer";

        assertDoesNotThrow(() -> {
            UserMapper.createUser(username, password, role, connectionPool);
            UserMapper.createUser(username, password, role, connectionPool); // Should not throw
        });
    }


    @Test
    void testLogin(){
        String username = "usertest"; // ensure uniqueness
        String password = "test";
        String role = "customer";

        assertDoesNotThrow(() -> {
            UserMapper.login(username, password, connectionPool);
        });
    }

    @Test
    void testPasswordMatch() {
        try {
            assertThrows(IllegalArgumentException.class, () -> {
                UserController.createUser("testuser", "pass1", "pass2", connectionPool); // pass1 != pass2
            });
            System.out.println("Test successful: There was a mismatch in the passwords.");
        } catch (AssertionError e) {
            System.err.println("Test failed: Exception not thrown.");
            throw e;
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE user_name LIKE 'test%'")) {
            stmt.executeUpdate();
        }
    }
}
