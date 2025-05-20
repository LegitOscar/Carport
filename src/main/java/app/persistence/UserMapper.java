package app.persistence;


import app.entities.CustomerProfile;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.*;

public class UserMapper
{
    private final ConnectionPool connectionPool;

    public UserMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }


    public static User login(String email, String password, ConnectionPool connectionPool) throws DatabaseException {
        try (Connection connection = connectionPool.getConnection()) {
            // Check customer
            String sqlCustomer = "SELECT * FROM customer WHERE customer_email=? AND password=?";
            try (PreparedStatement ps = connection.prepareStatement(sqlCustomer)) {
                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int id = rs.getInt("customer_id");
                    String fetchedEmail = rs.getString("customer_email");
                    return new User(id, fetchedEmail, password, null); // roleId is null
                }
            }

            // Check worker
            String sqlWorker = "SELECT * FROM workers WHERE worker_email=? AND password=?";
            try (PreparedStatement ps = connection.prepareStatement(sqlWorker)) {
                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int id = rs.getInt("worker_id");
                    String fetchedEmail = rs.getString("worker_email");
                    Integer roleId = rs.getInt("role_id");
                    return new User(id, fetchedEmail, password, roleId);
                }
            }

            throw new DatabaseException("Login failed: no such user.");

        } catch (SQLException e) {
            throw new DatabaseException("DB error during login", e.getMessage());
        }
    }


    public void createUser(User user) {
        String insertCustomerSql = "INSERT INTO customer (customer_name, customer_email, customer_phone, password) " +
                "VALUES (?, ?, ?, ?) RETURNING customer_id";

        String insertZipSql = "INSERT INTO customer_zip (customer_id, postcode, address, city) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectionPool.getConnection()) {
            conn.setAutoCommit(false);  // start transaction

            int customerId;
            try (PreparedStatement stmt = conn.prepareStatement(insertCustomerSql)) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getEmail());
                stmt.setInt(3, user.getPhone());
                stmt.setString(4, user.getPassword());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    customerId = rs.getInt("customer_id");
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }

            try (PreparedStatement stmt2 = conn.prepareStatement(insertZipSql)) {
                stmt2.setInt(1, customerId);
                stmt2.setInt(2, user.getPostcode());
                stmt2.setString(3, user.getAddress());
                stmt2.setString(4, user.getCity());

                stmt2.executeUpdate();
            }

            conn.commit();  // commit transaction if all good

        } catch (SQLException e) {
            e.printStackTrace();  // print full stack trace for debugging
            try {
                connectionPool.getConnection().rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Error creating user", e);
        }
    }
}


