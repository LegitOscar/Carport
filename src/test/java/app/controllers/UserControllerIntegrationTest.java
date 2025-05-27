package app.controllers;

import app.persistence.ConnectionPool;
import io.javalin.http.Context;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerIntegrationTest {

    private ConnectionPool connectionPool;
    private Context ctx;
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://164.90.223.15:5432/%s?currentSchema=public";
    private static final String DB = "carport";

    @BeforeAll
    public void setupClass() {
        connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    }

    @BeforeEach
    public void setup() {
        ctx = mock(Context.class);
    }

    @Test
    public void testCreateUser_Success() {
        when(ctx.formParam("navn")).thenReturn("TestUser");
        when(ctx.formParam("adresse")).thenReturn("Test Address 123");
        when(ctx.formParam("postnummer")).thenReturn("8000");
        when(ctx.formParam("by")).thenReturn("TestCity");
        when(ctx.formParam("telefon")).thenReturn("12345678");
        when(ctx.formParam("email")).thenReturn("testuser@example.com");
        when(ctx.formParam("password1")).thenReturn("password123");
        when(ctx.formParam("password2")).thenReturn("password123");

        UserController.createUser(ctx, connectionPool);

        verify(ctx).attribute("message", "Bruger oprettet!");
        verify(ctx, never()).render(any());
    }

    @Test
    public void testCreateUser_PasswordMismatch() {
        when(ctx.formParam("password1")).thenReturn("password123");
        when(ctx.formParam("password2")).thenReturn("differentpass");

        UserController.createUser(ctx, connectionPool);

        verify(ctx).attribute("message", "Passwords do not match.");
        verify(ctx).render("createuser.html");
    }

    @Test
    public void testCreateUser_InvalidPhoneNumber() {
        when(ctx.formParam("navn")).thenReturn("TestUser");
        when(ctx.formParam("adresse")).thenReturn("Test Address 123");
        when(ctx.formParam("postnummer")).thenReturn("8000");
        when(ctx.formParam("by")).thenReturn("TestCity");
        when(ctx.formParam("telefon")).thenReturn("abc123");  // Invalid phone number
        when(ctx.formParam("email")).thenReturn("testuser@example.com");
        when(ctx.formParam("password1")).thenReturn("password123");
        when(ctx.formParam("password2")).thenReturn("password123");

        Assertions.assertThrows(NumberFormatException.class, () -> {
            UserController.createUser(ctx, connectionPool);
        });
    }
    @AfterEach
    public void cleanup() {
        try (Connection conn = connectionPool.getConnection();
             var stmt = conn.prepareStatement("DELETE FROM customer WHERE customer_email = ?")) {
            stmt.setString(1, "testuser@example.com");
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
