package app.persistence;

import app.entities.OrderItem;
import app.entities.Order;
import app.entities.Product;
import app.entities.WoodVariant;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class OrderItemMapper {

    public static void insertOrderItem(OrderItem item, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO billofmaterials (order_id, wood_variant_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, item.getOrder().getOrderId());
            ps.setInt(2, item.getWoodVariant().getWoodVariantId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke indsætte orderItem", e.getMessage());
        }
    }

    public static List<OrderItem> getOrderItemsByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        List<OrderItem> items = new ArrayList<>();

        String sql = "SELECT b.quantity, b.unit_price,\n" +
                "w.wood_variant_id, w.material_id, w.length_cm, w.size, w.price,\n" +
                "o.order_id, o.order_date, o.total_price, o.order_status, o.customer_id, o.worker_id\n" +
                "FROM billofmaterials b\n" +
                "JOIN wood_variant w ON b.wood_variant_id = w.wood_variant_id\n" +
                "JOIN material m ON w.material_id = m.material_id" +
                "JOIN orders o ON b.order_id = o.order_id\n" +
                "WHERE b.order_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                WoodVariant variant = new WoodVariant(
                        rs.getInt("wood_variant_id"),
                        rs.getString("material_name"),
                        rs.getInt("material_id"),
                        rs.getInt("length_cm"),
                        rs.getString("size"),
                        rs.getDouble("price")
                );
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getDate("order_date").toLocalDate(),
                        rs.getDouble("total_price"),
                        rs.getString("order_status"),
                        rs.getInt("customer_id"),
                        rs.getInt("worker_id")
                );

                OrderItem item = new OrderItem(
                        order,
                        variant,
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price")
                );

                items.add(item);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente order items", e.getMessage());
        }

        return items;
    }
}

