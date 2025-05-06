
package app.persistence;

import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

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

    public static Order createOrder(User user, ConnectionPool connectionPool) throws DatabaseException {
        Order newOrder = null;

        String sql = "INSERT INTO orders (orderdate, totalprice, orderstatus, user_id) VALUES (?, ?, ?, ?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            Date orderDate = Date.valueOf(LocalDate.now());
            double totalPrice = 0.0;
            String orderStatus = "Ikke behandlet";

            ps.setDate(1, orderDate);
            ps.setDouble(2, totalPrice);
            ps.setString(3, orderStatus);
            ps.setInt(4, user.getUserId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 1) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int generatedOrderId = rs.getInt(1);
                    newOrder = new Order(generatedOrderId, orderDate, totalPrice, orderStatus);
                }
            } else {
                throw new DatabaseException("Fejl under indsættelse af ordre.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl i DB connection", e.getMessage());
        }

        return newOrder;
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

