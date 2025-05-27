package app.persistence;

import app.entities.WoodVariant;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WoodVariantMapper {
    private static ConnectionPool connectionPool;

    public WoodVariantMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public WoodVariant findByMaterialIdAndLength(int materialId, int lengthCm) {
        String sql = "SELECT wood_id, material_id, length, size, price, material_name\n" +
                "FROM wood_variant w\n" +
                "JOIN material ON material_id = material_id\n" +
                "WHERE material_id = ? AND w.length_cm = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materialId);
            ps.setInt(2, lengthCm);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new WoodVariant(
                        rs.getInt("wood_id"),
                        rs.getString("material_name"),
                        rs.getInt("material-id"),
                        rs.getInt("length_cm"),
                        rs.getString("size"),
                        rs.getDouble("price")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<WoodVariant> getAllWoodVariants(ConnectionPool connectionPool) throws DatabaseException {
        List<WoodVariant> woodVariants = new ArrayList<>();
        String sql = "SELECT w.wood_id, m.material_name AS material_name, w.material_id, w.length, w.size, w.price " +
                "FROM wood_variant w " +
                "JOIN material m ON w.material_id = m.material_id";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                WoodVariant variant = new WoodVariant(
                        rs.getInt("wood_id"),
                        rs.getString("material_name"),
                        rs.getInt("material_id"),
                        rs.getInt("length"),
                        rs.getString("size"),
                        rs.getDouble("price")
                );
                woodVariants.add(variant);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af wood_variants", e.getMessage());
        }
        return woodVariants;
    }
}