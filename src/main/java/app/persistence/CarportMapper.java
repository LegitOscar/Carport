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

        public static Carport createCarport(Carport carport, ConnectionPool connectionPool) {
            String sql = "INSERT INTO carport (length_cm, width_cm) VALUES (?, ?) RETURNING carport_id";

            try (Connection connection = connectionPool.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setInt(1, carport.getLengthCm());
                ps.setInt(2, carport.getWidthCm());

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int id = rs.getInt("carport_id");
                    carport.setCarportId(id);
                    return carport;
                } else {
                    throw new RuntimeException("Fejl ved oprettelse af carport – intet ID returneret");
                }

            } catch (SQLException e) {
                throw new RuntimeException("Fejl ved oprettelse af carport", e);
            }
        }
    }

