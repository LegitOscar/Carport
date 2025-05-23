//package app.persistence;
//
//import app.entities.User;
//import app.exceptions.DatabaseException;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.sql.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class UserMapperTest {
//
//    private static ConnectionPool connectionPool;
//
//    @BeforeAll
//    static void setupClass() {
//        String user = "postgres";
//        String password = "postgres";
//        // Use your IP address and specify the database name (e.g., "postgres" if default)
//        // The %s in the URL will be replaced by the schema name
//        String url = "jdbc:postgresql://164.90.223.15:5432/%s?currentSchema=test";
//        String DB = "carport";
//
//        connectionPool = ConnectionPool.getInstance(user, password, url, DB);
//
//        try (Connection connection = connectionPool.getConnection();
//             Statement stmt = connection.createStatement()) {
//
//            // Drop and recreate sequence in the test schema
//            stmt.execute("DROP SEQUENCE IF EXISTS test.customer_customer_id_seq CASCADE");
//            stmt.execute("CREATE SEQUENCE test.customer_customer_id_seq START 3");
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            fail("Database connection failed in setupClass");
//        }
//    }
//
//
//    @BeforeEach
//    void setUp() {
//        try (Connection connection = connectionPool.getConnection();
//             Statement stmt = connection.createStatement()) {
//
//            // Clean tables before inserting test data
//            stmt.execute("DELETE FROM test.customer");
//            stmt.execute("DELETE FROM test.postcode");
//
//            // Insert your fixed postcodes and cities
//            stmt.execute("""
//            INSERT INTO test.postcode (postcode, city) VALUES
//            (1000, 'Copenhagen'),
//            (3000, 'Helsingør'),
//            (4000, 'Roskilde'),
//            (5000, 'Odense'),
//            (6000, 'Kolding'),
//            (6700, 'Esbjerg'),
//            (7100, 'Vejle'),
//            (8000, 'Aarhus'),
//            (8800, 'Viborg'),
//            (9000, 'Aalborg')
//        """);
//
//            // Insert some existing customers if needed
//            stmt.execute("""
//            INSERT INTO test.customer (customer_id, customer_name, customer_email, customer_password, customer_phone, customer_address, postcode)
//            VALUES
//              (1, 'Hugo Jensen', 'hugo@test.dk', 'hugo123', '12345678', 'Testvej 1', 1000),
//              (2, 'Signe Andersen', 'signe@test.dk', 'signe123', '12345678', 'Testvej 2', 3000)
//        """);
//
//            // Reset sequence for customer IDs
//            stmt.execute("SELECT setval('test.customer_customer_id_seq', COALESCE((SELECT MAX(customer_id) + 1 FROM test.customer), 1), false)");
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            fail("Database setup failed");
//        }
//    }
//
//
//
//
//    @Test
//    void testCreateUserWithZipCode() {
//        UserMapper userMapper = new UserMapper(connectionPool);
//        User newUser = new User("Kasper Hansen", "Solvej 13", 2670, "Hundige", 12345678, "kasper@kode.dk", "1234");
//
//        userMapper.createUser(newUser);
//
//        try (Connection conn = connectionPool.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(
//                     "SELECT * FROM customer WHERE customer_email = ?")) {
//            stmt.setString(1, "kasper@kode.dk");
//
//            ResultSet rs = stmt.executeQuery();
//            assertTrue(rs.next(), "User was not created in customer table");
//            assertEquals("Kasper Hansen", rs.getString("customer_name"));
//            assertEquals(2670, rs.getInt("postcode"));
//
//            // No need to check postcode table for insertion
//            // Just check postcode exists (optional)
//            try (PreparedStatement zipStmt = conn.prepareStatement(
//                    "SELECT * FROM postcode WHERE postcode = ?")) {
//                zipStmt.setInt(1, 2670);
//                ResultSet zipRs = zipStmt.executeQuery();
//                assertTrue(zipRs.next(), "Zip code does not exist in postcode table");
//                assertEquals("Hundige", zipRs.getString("city"));
//            }
//        } catch (SQLException e) {
//            fail("Database query failed: " + e.getMessage());
//        }
//    }
//
//
//    @Test
//    void createUser() {
//    }
//}
