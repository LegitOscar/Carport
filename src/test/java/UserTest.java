import app.controllers.UserController;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {




    private static ConnectionPool connectionPool;

    @BeforeAll
    static void setUp() {
        // Use test database credentials here
        String user = "postgres";
        String password = "postgres";
        String url = "jdbc:postgresql://localhost:5432/%s?currentSchema=public"; // or whatever schema you use
        String db = "carport";

        connectionPool = ConnectionPool.getInstance(user, password, url, db);
    }

    @BeforeEach
    void setupTestData(){} //not setup needed


    @Test
    void testCreateUserSuccess() {
        String username = "test123"; // ensure uniqueness
        String password = "123";
        String role = "customer";

        assertDoesNotThrow(() -> {
            UserMapper.createuser(username, password, role, connectionPool);
        });
    }


    @Test
    void testDuplicateUsername() {
        String username = "test123";
        String password = "123";
        String role = "customer";

        assertDoesNotThrow(() -> {
            UserMapper.createuser(username, password, role, connectionPool);
            UserMapper.createuser(username, password, role, connectionPool); // Should not throw
        });
    }


    @Test
    void testLogin(){
        String username = "test12345"; // ensure uniqueness
        String password = "123";
        String role = "customer";

        assertDoesNotThrow(() -> {
            UserMapper.login(username, password, connectionPool);
        });
    }

    @Test
    void testPasswordMatch() {
        try {
            assertThrows(IllegalArgumentException.class, () -> {
                UserController.createUser("testuser111111111", "pass1", "pass2", connectionPool); // pass1 != pass2
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
