package app.persistence;

import app.entities.FittingsAndScrews;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FittingsAndScrewsMapper {

    public List<FittingsAndScrews> getAllFittingsAndScrews(ConnectionPool connectionPool) throws SQLException {
        List<FittingsAndScrews> list = new ArrayList<>();
        String sql = "SELECT * FROM fittings_and_screws";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("fs_id");
                int materialId = rs.getInt("material_id");
                String size = rs.getString("size");
                int quantity = rs.getInt("quantity_per_package");
                double price = rs.getDouble("price");

                FittingsAndScrews f = new FittingsAndScrews(id, materialId, size, quantity, price);
                list.add(f);
            }
        }

        return list;
    }
}