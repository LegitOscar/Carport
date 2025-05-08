
package app.persistence;

import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.*;
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
            ps.setInt(3, user.getUserId()); // customer_id (ensure this user exists in the users table)
            ps.setString(4, "Pending"); // order_status

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int orderId = keys.getInt(1);
                    return new Order(orderId, currentDate, 0, "Pending");
                } else {
                    throw new DatabaseException("No ID returned when creating order");
                }
            } else {
                throw new DatabaseException("Order creation failed, no rows affected");
            }
        }
    }



    public static List<Order> getAllOrdersPerUser(int userId, ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY orderdate DESC";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("order_id");
                Date date = rs.getDate("orderdate");
                double price = rs.getDouble("totalprice");
                String status = rs.getString("orderstatus");
                orderList.add(new Order(id, date, price, status));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af ordrer for bruger " + userId, e.getMessage());
        }

        return orderList;
    }

    public static List<Order> getAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY orderdate DESC";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("order_id"); //todo ændre navn
                Date date = rs.getDate("orderdate");
                double price = rs.getDouble("totalprice");
                String status = rs.getString("orderstatus");
                orderList.add(new Order(id, date, price, status));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af alle ordrer", e.getMessage());
        }

        return orderList;
    }



    public static void deleteOrder(int orderId, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "delete from orders where order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl i opdatering af en task");
            }
        }
        catch (SQLException e)
        {
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
                Date orderDate = rs.getDate("orderdate");
                double totalPrice = rs.getDouble("totalprice");
                String orderStatus = rs.getString("orderstatus");

                order = new Order(id, orderDate, totalPrice, orderStatus);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af ordre med id = " + orderId, e.getMessage());
        }

        return order;
    }

    public static void updateOrder(Order order, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE orders SET orderstatus = ?, totalprice = ? WHERE order_id = ?";

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

