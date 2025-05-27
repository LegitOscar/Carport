package app.persistence;

import app.entities.Order;
import app.entities.OrderDetails;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

    public static Order createOrder(User user, int carportId, ConnectionPool connectionPool) throws DatabaseException, SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        String sql = "INSERT INTO orders (order_date, total_price, customer_id, order_status, worker_id, carport_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            Date currentDate = new Date(System.currentTimeMillis());

            int workerId = getAvailableWorkerId(connectionPool);
            ps.setDate(1, currentDate);
            ps.setDouble(2, 0);
            ps.setInt(3, user.getId());
            ps.setString(4, "Pending");
            ps.setInt(5, workerId);
            ps.setInt(6, carportId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int orderId = keys.getInt(1);
                    return new Order(orderId, currentDate.toLocalDate(), 0, "Pending", user.getId(), workerId);
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
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("order_id");
                    Date date = rs.getDate("order_date");
                    double price = rs.getDouble("total_price");
                    String status = rs.getString("order_status");
                    int workerId = rs.getInt("worker_id");
                    String notes = rs.getString("internal_notes");

                    orderList.add(new Order(id, date.toLocalDate(), price, status, customerId, workerId, notes));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving orders for customer " + customerId, e.getMessage());
        }
        return orderList;
    }

    public static int getAvailableWorkerId(ConnectionPool connectionPool) throws SQLException {
        String sql = "SELECT worker_id FROM workers LIMIT 1";
        try (Connection conn = connectionPool.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("worker_id");
            } else {
                throw new SQLException("No workers available");
            }
        }
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
                    double totalPrice = rs.getDouble("total_price");
                    String orderStatus = rs.getString("order_status");
                    int customerId = rs.getInt("customer_id");
                    String notes = rs.getString("internal_notes");

                    orders.add(new Order(orderId, orderDate.toLocalDate(), totalPrice, orderStatus, customerId, workerId, notes));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching orders for worker", e.getMessage());
        }

        return orders;
    }

    public static List<Order> getOrdersNotAssignedToWorker(int workerId, ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE worker_id != ? OR worker_id IS NULL";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, workerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    Date orderDate = rs.getDate("order_date");
                    double totalPrice = rs.getDouble("total_price");
                    String orderStatus = rs.getString("order_status");
                    int customerId = rs.getInt("customer_id");
                    int worker = rs.getInt("worker_id");

                    orderList.add(new Order(orderId, orderDate.toLocalDate(), totalPrice, orderStatus, customerId, worker));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching unassigned orders", e.getMessage());
        }

        return orderList;
    }

    public static Order getOrderById(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date date = rs.getDate("order_date");
                    double totalPrice = rs.getDouble("total_price");
                    String orderStatus = rs.getString("order_status");
                    int customerId = rs.getInt("customer_id");
                    int workerId = rs.getInt("worker_id");
                    String notes = rs.getString("internal_notes");

                    return new Order(orderId, date.toLocalDate(), totalPrice, orderStatus, customerId, workerId, notes);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting order by ID", e.getMessage());
        }
        return null;
    }

    public static void updateOrder(Order order, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE orders SET total_price = ?, order_status = ?, internal_notes = ? WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setDouble(1, order.getTotalPrice());
            ps.setString(2, order.getOrderStatus());
            ps.setString(3, order.getInternalNotes());
            ps.setInt(4, order.getOrderId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating order", e.getMessage());
        }
    }

    public static void updateTotalPrice(int orderId, double totalPrice, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE orders SET total_price = ? WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setDouble(1, totalPrice);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved opdatering af totalpris", e.getMessage());
        }
    }

    public static void deleteOrder(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting order", e.getMessage());
        }
    }

    public static void assignOrderToWorker(int orderId, int workerId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE orders SET worker_id = ? WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, workerId);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error assigning worker to order", e.getMessage());
        }
    }

    public static OrderDetails getOrderDetailsById(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = """
        SELECT o.order_id, o.order_date, o.total_price, o.order_status, o.customer_id, o.worker_id, o.internal_notes,
               c.customer_name, c.customer_email, c.customer_phone, c.customer_address, c.postcode
        FROM orders o
        JOIN customer c ON o.customer_id = c.customer_id
        WHERE o.order_id = ?
    """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order(
                            rs.getInt("order_id"),
                            rs.getDate("order_date").toLocalDate(),
                            rs.getDouble("total_price"),
                            rs.getString("order_status"),
                            rs.getInt("customer_id"),
                            rs.getInt("worker_id"),
                            rs.getString("internal_notes")
                    );

                    User customer = new User(
                            rs.getInt("customer_id"),
                            rs.getString("customer_name"),
                            rs.getString("customer_email"),
                            rs.getInt("customer_phone"),
                            rs.getString("customer_address"),
                            rs.getInt("postcode")
                    );

                    return new OrderDetails(order, customer);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting order details", e.getMessage());
        }
        return null;
    }

    public static int getCarportIdByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        int carportId = -1;
        String sql = "SELECT carport_id FROM orders WHERE order_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    carportId = rs.getInt("carport_id");
                } else {
                    throw new DatabaseException("Order with id " + orderId + " not found.");
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error fetching carportId for orderId " + orderId);
        }

        return carportId;
    }
}
