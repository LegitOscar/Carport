package app.persistence;

import app.entities.Carport;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;

import java.sql.*;

public class CarportMapper {

    public static Carport getCarportById(int id, ConnectionPool pool) throws DatabaseException {
        String sql = "SELECT * FROM carport WHERE carport_id = ?";
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int width = rs.getInt("carport_width");
                int length = rs.getInt("carport_length");

                return new Carport(id, width, length);
            } else {
                throw new DatabaseException("Carport ikke fundet med ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

    public static Carport createCarport(Carport carport, ConnectionPool connectionPool) {
        String sql = "INSERT INTO carport (carport_length, carport_width) VALUES (?, ?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, carport.getLengthCm());
            ps.setInt(2, carport.getWidthCm());
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        carport.setCarportId(id);
                        return carport;
                    } else {
                        throw new RuntimeException("Fejl ved oprettelse af carport – intet ID returneret");
                    }
                }
            } else {
                throw new RuntimeException("Fejl ved oprettelse af carport – ingen rækker oprettet");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Fejl ved oprettelse af carport", e);
        }
    }
}