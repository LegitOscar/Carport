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
        String sql = "select * from orders where user_id=? order by name";
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int id = rs.getInt("order_id");
                Date date = rs.getDate("date");
                double price = rs.getDouble("totalprice");
                String status = rs.getString("status");
                orderList.add(new Order(id,date,price,status));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl!!!!", e.getMessage());
        }
        return orderList;
    }

    public static Order addOrder(User user, int orderId, ConnectionPool connectionPool) throws DatabaseException {
        Order newOrder = null;

        String sql = "insert into orders (orderId, orderDate, totalPrice, orderStatus, user_id) values (?,?,?,?,?)";

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
                    newOrder = new Order(generatedOrderId, orderDate, totalPrice, orderStatus); //todo tjek om generatedOrderID er korrekt.
                }
            } else {
                throw new DatabaseException("Fejl under indsættelse af ordre.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl i DB connection", e.getMessage());
        }

        return newOrder;
    }

    public static void delete(int orderId, ConnectionPool connectionPool) throws DatabaseException
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

}
