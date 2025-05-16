import app.controllers.OrderController;
import app.entities.Order;
import app.entities.User;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

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
    void testCreateOrder() {
        User user = new User(1, "123", "123", null);

        assertDoesNotThrow(() -> {
            Order order = OrderController.createOrder(user, connectionPool);
            assertNotNull(order);
            assertTrue(order.getOrderId() > 0);
        });
    }

    @Test
    void testDeleteOrder() {
        User user = new User(2, "123", "123", null);

        assertDoesNotThrow(() -> {
            Order order = OrderController.createOrder(user, connectionPool);
            int orderId = order.getOrderId();

            OrderMapper.deleteOrder(orderId, connectionPool);

            Order deleteOrder = OrderMapper.getOrderById(orderId, connectionPool);
            assertNull(deleteOrder, "Order should be null after deletion");
        });
    }


}

