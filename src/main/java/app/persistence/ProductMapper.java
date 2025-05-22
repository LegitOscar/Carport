package app.persistence;

import app.entities.Product;
import app.entities.ProductVariant;
import app.persistence.ConnectionPool;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductMapper {

    public static List<Product> getAllProducts(ConnectionPool connectionPool) throws DatabaseException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM product";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("product_id");
                String name = rs.getString("name");
                String unit = rs.getString("unit");
                int price = rs.getInt("price");

                products.add(new Product(id, name, unit, price));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af produkter", e.getMessage());
        }

        return products;
    }

    public static List<ProductVariant> getVariantsByProductId(int productId, ConnectionPool connectionPool) throws DatabaseException {
        List<ProductVariant> variants = new ArrayList<>();
        String sql = "SELECT * FROM product_variant pv " +
                "JOIN product p ON pv.product_id = p.product_id " +
                "WHERE pv.product_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        rs.getInt("price")
                );

                ProductVariant variant = new ProductVariant(
                        rs.getInt("product_variant_id"),
                        product,
                        rs.getInt("length"),
                        rs.getInt("wood_type_id")
                );

                variants.add(variant);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af varianter", e.getMessage());
        }

        return variants;
    }

    public static ProductVariant getCheapestVariantByMinLength(int productId, int minLength, ConnectionPool connectionPool) throws DatabaseException {
        ProductVariant cheapest = null;
        String sql = "SELECT * FROM product_variant pv " +
                "JOIN product p ON pv.product_id = p.product_id " +
                "WHERE pv.product_id = ? AND pv.length >= ? " +
                "ORDER BY p.price ASC LIMIT 1";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setInt(2, minLength);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        rs.getInt("price")
                );

                cheapest = new ProductVariant(
                        rs.getInt("product_variant_id"),
                        product,
                        rs.getInt("length"),
                        rs.getInt("wood_type_id")
                );
            } else {
                throw new DatabaseException("Ingen variant fundet for produkt id " + productId + " med længde ≥ " + minLength);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af billigste variant", e.getMessage());
        }

        return cheapest;
    }
}
