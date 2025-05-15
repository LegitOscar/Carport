package app;

import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.entities.Order;
import app.entities.User;

import java.time.LocalDate;


public class Main {
    public static void main(String[] args) {
        ConnectionPool connectionPool = ConnectionPool.getInstance(
                "postgres", "datdat2025!", "jdbc:postgresql://164.90.223.15:5432/%s", "carport");

        try {
            // Simulate a logged-in user
            User currentUser = new User(1, "joe", "123", "customer");
            int customerId = currentUser.getUserId();

            // Provide test values for the order
            int orderId = 6; // existing order ID
            LocalDate orderDate = LocalDate.now();
            double totalPrice = 4999.95;
            String orderStatus = "bekræftet";
            int workerId = 0; // or actual worker ID
            int carportId = 1; // make sure this exists

            // Create the Order
            Order order = new Order(orderId, orderDate, totalPrice, orderStatus, customerId, workerId, carportId);

            // Call the update method
            OrderMapper.updateOrder(order, connectionPool);

            System.out.println("Ordre opdateret!");

        } catch (Exception e) {
            System.err.println("Fejl ved opdatering af ordre: " + e.getMessage());
            e.printStackTrace();
        } finally {
            connectionPool.close();
        }
    }
}
