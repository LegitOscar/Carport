package app.persistence;

import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderMapperTest {

    private ConnectionPool connectionPool;
    private User testUser;

    // Put your test DB config here - change DB name if needed
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://164.90.223.15:5432/%s?currentSchema=test";
    private static final String TEST_DB = "carport"; // Use a dedicated test DB if possible

    @BeforeAll
    void setup() {
        // Initialize the connection pool with test DB credentials
        connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, TEST_DB);

        // Create or load a User object for testing (ensure this user exists in your test DB)
        testUser = new User(1, "Test User", "test@example.com", 123456789, "123 Test St", 9999);
    }

    @AfterAll
    void tearDown() {
        if (connectionPool != null) {
            connectionPool.close();
        }
    }

    @Test
    void testCreateOrder() throws SQLException, DatabaseException {
        int testCarportId = 2;

        Order order = OrderMapper.createOrder(testUser, testCarportId, connectionPool);

        assertNotNull(order);
        assertEquals(testUser.getId(), order.getCustomerId());
        assertEquals("Pending", order.getOrderStatus());
        assertEquals(0, order.getTotalPrice());
        assertEquals(LocalDate.now(), order.getOrderDate());
        assertTrue(order.getOrderId() > 0);
    }

    @Test
    void testGetAllOrdersPerUser() throws DatabaseException {
        List<Order> orders = OrderMapper.getAllOrdersPerUser(testUser.getId(), connectionPool);

        assertNotNull(orders);
        // Optionally assert size or contents if you know expected data
    }

    @Test
    void testUpdateOrderAndDeleteOrder() throws SQLException, DatabaseException {
        // First create an order to update/delete
        Order order = OrderMapper.createOrder(testUser, 5, connectionPool);

        order.setTotalPrice(500);
        order.setOrderStatus("Completed");
        order.setInternalNotes("Test update");

        OrderMapper.updateOrder(order, connectionPool);

        Order updatedOrder = OrderMapper.getOrderById(order.getOrderId(), connectionPool);
        assertNotNull(updatedOrder);
        assertEquals(500, updatedOrder.getTotalPrice());
        assertEquals("Completed", updatedOrder.getOrderStatus());
        assertEquals("Test update", updatedOrder.getInternalNotes());

        // Now delete the order
        OrderMapper.deleteOrder(order.getOrderId(), connectionPool);

        Order deletedOrder = OrderMapper.getOrderById(order.getOrderId(), connectionPool);
        assertNull(deletedOrder);
    }

    @Test
    void testAssignOrderToWorker() throws SQLException, DatabaseException {
        Order order = OrderMapper.createOrder(testUser, 1, connectionPool);

        int newWorkerId = 2; // Make sure this worker exists in your test DB

        OrderMapper.assignOrderToWorker(order.getOrderId(), newWorkerId, connectionPool);

        Order updatedOrder = OrderMapper.getOrderById(order.getOrderId(), connectionPool);
        assertEquals(newWorkerId, updatedOrder.getWorkerId());

        // Cleanup
        OrderMapper.deleteOrder(order.getOrderId(), connectionPool);
    }
}
