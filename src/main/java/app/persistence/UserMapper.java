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
            String sqlCustomer = "SELECT * FROM customer WHERE customer_email=? AND customer_password=?";
            try (PreparedStatement ps = connection.prepareStatement(sqlCustomer)) {
                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int id = rs.getInt("customer_id");
                    String fetchedEmail = rs.getString("customer_email");
                    return new User(id, fetchedEmail, password, null);
                }
            }

            // Check worker
            String sqlWorker = "SELECT * FROM workers WHERE worker_email=? AND worker_password=?";
            try (PreparedStatement ps = connection.prepareStatement(sqlWorker)) {
                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int id = rs.getInt("worker_id");
                    String fetchedEmail = rs.getString("worker_email");

                    int roleIdValue = rs.getInt("role_id");
                    Integer roleId = rs.wasNull() ? null : roleIdValue;

                    return new User(id, fetchedEmail, password, roleId);
                }
            }

            throw new DatabaseException("Login failed: no such user.");
        } catch (SQLException e) {
            throw new DatabaseException("DB error during login", e.getMessage());
        }
    }


    public void createUser(User user) {
        String checkPostcodeSql = "SELECT 1 FROM postcode WHERE postcode = ?";
        String insertCustomerSql = """
        INSERT INTO customer 
            (customer_name, customer_email, customer_password, customer_phone, customer_address, postcode)
        VALUES (?, ?, ?, ?, ?, ?) RETURNING customer_id
    """;

        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            conn.setAutoCommit(false);

            // Check if postcode exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkPostcodeSql)) {
                checkStmt.setInt(1, user.getPostcode());
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Postcode " + user.getPostcode() + " does not exist.");
                }
            }

            // Insert new customer
            try (PreparedStatement custStmt = conn.prepareStatement(insertCustomerSql)) {
                custStmt.setString(1, user.getName());
                custStmt.setString(2, user.getEmail());
                custStmt.setString(3, user.getPassword());
                custStmt.setInt(4, user.getPhone());
                custStmt.setString(5, user.getAddress());
                custStmt.setInt(6, user.getPostcode());

                ResultSet rs = custStmt.executeQuery();
                if (rs.next()) {
                    int customerId = rs.getInt("customer_id");
                    user.setId(customerId);
                } else {
                    throw new SQLException("Creating customer failed, no ID returned.");
                }
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error creating user", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    public static User getCustomerProfileById(int customerId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT c.customer_id, c.customer_name, c.customer_email, c.customer_phone, c.customer_password, " +
                "c.customer_address AS address, cz.postcode, cz.city " +
                "FROM customer c " +
                "JOIN postcode cz ON c.postcode = cz.postcode " +
                "WHERE c.customer_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("customer_name");
                String address = rs.getString("address");
                int postcode = rs.getInt("postcode");
                String city = rs.getString("city");
                int phone = rs.getInt("customer_phone");
                String email = rs.getString("customer_email");
                String password = rs.getString("customer_password");

                User user = new User(name, address, postcode, city, phone, email, password);
                user.setId(customerId);
                return user;

            } else {
                throw new DatabaseException("Bruger ikke fundet");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl under hentning af profil", e.getMessage());
        }
    }


    public static void updateUser(User user, ConnectionPool connectionPool) throws DatabaseException {
        String updateCustomerSql = "UPDATE customer SET customer_name = ?, customer_email = ?, customer_phone = ?, customer_address = ?, postcode = ? WHERE customer_id = ?";

        try (Connection connection = connectionPool.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement customerStmt = connection.prepareStatement(updateCustomerSql)) {

                // Step 1: Check if the new postcode exists in the postcode table
                String cityInDb = getCityByPostcode(user.getPostcode(), connectionPool);
                if (cityInDb == null || cityInDb.isEmpty()) {
                    throw new DatabaseException("Den indtastede postkode findes ikke i systemet.");
                }

                // Step 2: Update customer with new data (including the new valid postcode)
                customerStmt.setString(1, user.getName());
                customerStmt.setString(2, user.getEmail());
                customerStmt.setInt(3, user.getPhone());
                customerStmt.setString(4, user.getAddress());
                customerStmt.setInt(5, user.getPostcode());   // <- new postcode
                customerStmt.setInt(6, user.getId());         // <- where customer_id = ?

                customerStmt.executeUpdate();
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw new DatabaseException("Kunne ikke opdatere brugerprofil", e.getMessage());
            }
        } catch (SQLException e) {
            throw new DatabaseException("Databaseforbindelse fejlede", e.getMessage());
        }
    }


    public static String getCityByPostcode(int postcode, ConnectionPool connectionPool) throws DatabaseException {
        try (Connection conn = connectionPool.getConnection()) {
            String sql = "SELECT city FROM postcode WHERE postcode = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, postcode);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getString("city");
                } else {
                    return "";
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting city from postcode", e.getMessage());
        }
    }


}