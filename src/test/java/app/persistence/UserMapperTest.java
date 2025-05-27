package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;
import io.javalin.http.Context;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {
    private static ConnectionPool connectionPool;
    private Context ctx;
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://164.90.223.15:5432/%s?currentSchema=public";
    private static final String DB = "carport";

    @BeforeAll
    public static void setupClass() {
        connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    }

    @Test
    public void testCustomerLoginSuccess() {
        String email = "test@test.dk";
        String password = "test";

        try {
            User user = UserMapper.login(email, password, connectionPool);
            assertNotNull(user, "User should not be null");
            assertEquals(1, user.getRoleId(), "RoleId should be 1 for customers");
            assertEquals(email, user.getEmail(), "Email should match");
        } catch (DatabaseException e) {
            fail("Login threw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testCustomerLoginFailure() {
        String email = "fakemail@fake.dk";
        String password = "fakepassword";

        assertThrows(DatabaseException.class, () -> {
            UserMapper.login(email, password, connectionPool);
        });
    }
}
