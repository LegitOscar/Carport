package app.persistence;

import app.entities.Carport;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CarportMapper {
    public static Carport getCarportById(int id, ConnectionPool pool) throws DatabaseException {
        String sql = "SELECT * FROM carport WHERE carport_id = ?";
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int width = rs.getInt("width");
                int length = rs.getInt("length");
                boolean hasShed = rs.getBoolean("shed");
                return new Carport(id, width, length);
            } else {
                throw new DatabaseException("Carport ikke fundet med ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }
}
