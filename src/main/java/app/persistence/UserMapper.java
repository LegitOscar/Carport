package app.persistence;


import app.entities.CustomerProfile;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper
{

    public static User login(String email, String password, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT * FROM customer WHERE customer_email=? AND password=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("customer_id");
                String fetchedEmail = rs.getString("customer_email");

                return new User(id, fetchedEmail, password, "customer");
            } else {
                throw new DatabaseException("Fejl i login. Forkert brugernavn eller adgangskode.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }




    public static void createUser(String userName, String password, String role, ConnectionPool connectionPool) throws DatabaseException {
        // This version uses ON CONFLICT DO NOTHING to silently skip duplicate inserts
        String sql = "INSERT INTO users (user_name, password, role) VALUES (?,?,?) ON CONFLICT (user_name) DO NOTHING";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, userName);
            ps.setString(2, password);
            ps.setString(3, role);

            ps.executeUpdate(); // No need to check rows affected, since duplicates are allowed
        } catch (SQLException e) {
            throw new DatabaseException("Der er sket en fejl. Prøv igen", e.getMessage());
        }
    }

    public static CustomerProfile getCustomerProfileById(int customerId, ConnectionPool connectionPool) throws DatabaseException{
        String sql = """
            SELECT c.customer_id, c.customer_name, c.customer_email, c.customer_phone, 
                   c.password,
                cz.address, cz.postcode, cz.city
                FROM customer c
                JOIN customer_zip cz ON c.customer_id = cz.customer_id
                WHERE c.customer_id = ?
        """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CustomerProfile profile = new CustomerProfile();
                    profile.setCustomerId(rs.getInt("customer_id"));
                    profile.setName(rs.getString("customer_name"));
                    profile.setEmail(rs.getString("customer_email"));
                    profile.setPhone(rs.getInt("customer_phone"));
                    profile.setAddress(rs.getString("address"));
                    profile.setPostcode(rs.getInt("postcode"));
                    profile.setCity(rs.getString("city"));
                    profile.setPassword(rs.getString("password"));
                    return profile;
                } else {
                    throw new DatabaseException("No customer found with ID: " + customerId);
                }
            }
        }catch (SQLException e){
            throw new DatabaseException("Error fetching customer profile", e.getMessage());
        }

    }

}


