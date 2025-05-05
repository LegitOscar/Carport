package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper
{

    public static User login(String userName, String password, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE user_name=? AND password=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, userName);
            ps.setString(2, password); // Now we check password in SQL

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("user_id");
                String fetchedUsername = rs.getString("user_name");
                String fetchedPassword = rs.getString("password");  // Get the actual password from DB
                String role = rs.getString("role");

                // 🔹 Print the fetched values to debug
                System.out.println("🔹 Fetched from DB: ID=" + id + ", Username=" + fetchedUsername + ", Role=" + role);

                return new User(id, fetchedUsername, fetchedPassword, role);
            } else {
                throw new DatabaseException("Fejl i login. Forkert brugernavn eller adgangskode.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }




    public static void createuser(String userName, String password, String role, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "INSERT INTO users (user_name, password, role) VALUES (?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setString(1, userName);
            ps.setString(2, password);
            ps.setString(3, role);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl ved oprettelse af ny bruger");
            }
        }
        catch (SQLException e)
        {
            String msg = "Der er sket en fejl. Prøv igen";
            if (e.getMessage().startsWith("ERROR: duplicate key value "))
            {
                msg = "Brugernavnet findes allerede. Vælg et andet";
            }
            throw new DatabaseException(msg, e.getMessage());
        }
    }
}