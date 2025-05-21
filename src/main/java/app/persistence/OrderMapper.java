
package app.persistence;
import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {


    public static Order createOrder(User user, ConnectionPool connectionPool) throws DatabaseException, SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        String sql = "INSERT INTO orders (order_date, total_price, customer_id, order_status) VALUES (?, ?, ?, ?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            Date currentDate = new Date(System.currentTimeMillis());

            ps.setDate(1, currentDate); // order_date
            ps.setDouble(2, 0); // default price
            ps.setInt(3, user.getId()); // customer_id (ensure this user exists in the users table)
            ps.setString(4, "Pending"); // order_status

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int orderId = keys.getInt(1);
                    return new Order(orderId, currentDate.toLocalDate(), 0, "Pending", user.getId(), 0, 0);
                } else {
                    throw new DatabaseException("No ID returned when creating order");
                }
            } else {
                throw new DatabaseException("Order creation failed, no rows affected");
            }
        }
    }


    public static List<Order> getAllOrdersPerUser(int customerId, ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_date DESC";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("order_id");
                Date date = rs.getDate("order_date");
                double price = rs.getDouble("total_price");
                String status = rs.getString("order_status");
                int workerId = rs.getInt("worker_id");
                int carportId = rs.getInt("carport_id");

                orderList.add(new Order(id, date.toLocalDate(), price, status, customerId, workerId, carportId));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving orders from customer " + customerId, e.getMessage());
        }

        return orderList;
    }

    public static List<Order> getAllOrdersPerWorker(int workerId, ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE worker_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, workerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");

                    Date orderDate = rs.getDate("order_date");
                    LocalDate localOrderDate = null;
                    if (orderDate != null) {
                        localOrderDate = orderDate.toLocalDate();
                    }

                    double totalPrice = rs.getDouble("total_price");
                    String orderStatus = rs.getString("order_status");
                    int customerId = rs.getInt("customer_id");
                    int worker = rs.getInt("worker_id");
                    int carportId = rs.getInt("carport_id");

                    Order order = new Order(orderId, localOrderDate, totalPrice, orderStatus, customerId, worker, carportId);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching orders for worker", e.getMessage());
        }

        return orders;
    }


    public static List<Order> getAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("order_id");
                Date date = rs.getDate("order_date");
                double price = rs.getDouble("total_price");
                String status = rs.getString("order_status");
                int customerId = rs.getInt("customer_id");
                int workerId = rs.getInt("worker_id");
                int carportId = rs.getInt("carport_id");

                orderList.add(new Order(id, date.toLocalDate(), price, status, customerId, workerId, carportId));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting all orders", e.getMessage());
        }

        return orderList;
    }


    public static void deleteOrder(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "delete from orders where order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl i opdatering af en task");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved sletning af en task", e.getMessage());
        }
    }

    public static Order getOrderById(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        Order order = null;

        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("order_id");
                Date date = rs.getDate("order_date");
                double totalPrice = rs.getDouble("total_price");
                String orderStatus = rs.getString("order_status");
                int customerId = rs.getInt("customer_id");
                int workerId = rs.getInt("worker_id");
                int carportId = rs.getInt("carport_id");

                order = new Order(id, date.toLocalDate(), totalPrice, orderStatus, customerId, workerId, carportId);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting order by id = " + orderId, e.getMessage());
        }

        return order;
    }

    public static void updateOrder(Order order, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE orders SET order_status = ?, total_price = ? WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, order.getOrderStatus());
            ps.setDouble(2, order.getTotalPrice());
            ps.setInt(3, order.getOrderId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl under opdatering af ordre med id = " + order.getOrderId());
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl i DB connection", e.getMessage());
        }
    }
}

