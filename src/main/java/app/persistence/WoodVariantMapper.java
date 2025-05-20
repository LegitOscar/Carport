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
        String sql = "SELECT * FROM wood_variant WHERE material_id = ? AND length_cm = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materialId);
            ps.setInt(2, lengthCm);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new WoodVariant(
                        rs.getInt("wood_variant_id"),
                        rs.getInt("material_id"),
                        rs.getInt("length_cm"),
                        rs.getString("size"),
                        rs.getDouble("price")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace(); // todo ændre til exception
        }
        return null;
    }

    public static List<WoodVariant> getAllWoodVariants(ConnectionPool connectionPool) throws DatabaseException {
        List<WoodVariant> woodVariants = new ArrayList<>();
        String sql = "SELECT wv.wood_variant_id, m.name AS material_name, wv.material_id, wv.length_cm, wv.size, wv.price " +
                "FROM wood_variant wv JOIN material m ON wv.material_id = m.material_id";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
                woodVariants.add(variant);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af wood_variants", e.getMessage());
        }

        return woodVariants;
    }

}
