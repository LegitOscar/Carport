package app.controllers;

import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderController {

    private final ConnectionPool connectionPool;

    public OrderController(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {

        app.post("/deleteorder", ctx -> deleteOrder(ctx, connectionPool));

        app.post("/updateorder", ctx -> {
            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            double totalPrice = Double.parseDouble(ctx.formParam("totalPrice"));
            String orderStatus = ctx.formParam("orderStatus");

            Order order = OrderMapper.getOrderById(orderId, connectionPool);
            if (order != null) {
                order.setTotalPrice(totalPrice);
                order.setOrderStatus(orderStatus);
                OrderMapper.updateOrder(order, connectionPool);
            }

            ctx.redirect("/sellerdashboard");
        });

        app.get("/getorders", ctx -> getOrdersForUser(ctx, connectionPool));

        app.get("/sellerdashboard", ctx -> {
            Integer currentWorkerId = ctx.sessionAttribute("workerId");

            if (currentWorkerId == null) {
                ctx.redirect("/login");
                return;
            }

            List<Order> orders = OrderMapper.getAllOrdersPerWorker(currentWorkerId, connectionPool);
            List<Order> otherOrders = OrderMapper.getOrdersNotAssignedToWorker(currentWorkerId, connectionPool);

            String editOrderIdStr = ctx.queryParam("editOrderId");
            if (editOrderIdStr != null) {
                ctx.attribute("editOrderId", Integer.parseInt(editOrderIdStr));
            }

            ctx.attribute("orders", orders);
            ctx.attribute("otherOrders", otherOrders);
            ctx.render("sellerdashboard.html");
        });

        app.post("/selectorder", ctx -> {
            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            int currentWorkerId = ctx.sessionAttribute("workerId");

            OrderMapper.assignOrderToWorker(orderId, currentWorkerId, connectionPool);
            ctx.redirect("/sellerdashboard");
        });
    }

    private static void deleteOrder(Context ctx, ConnectionPool connectionPool) {
        try {
            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            OrderMapper.deleteOrder(orderId, connectionPool);
            ctx.status(200).result("Ordre slettet med ID: " + orderId);
        } catch (NumberFormatException e) {
            ctx.status(400).result("Ugyldigt order ID");
        } catch (DatabaseException e) {
            ctx.status(500).result("Fejl ved sletning af ordre: " + e.getMessage());
        }
        ctx.redirect("/sellerdashboard");
    }

    private static void getOrdersForUser(Context ctx, ConnectionPool connectionPool) {
        User user = ctx.sessionAttribute("currentUser");
        if (user == null) {
            ctx.status(401).result("Du er ikke logget ind.");
            return;
        }

        try {
            List<Order> orders = OrderMapper.getAllOrdersPerUser(user.getId(), connectionPool);
            ctx.attribute("orders", orders);
            ctx.render("orders.html");
        } catch (DatabaseException e) {
            ctx.status(500).result("Fejl ved hentning af ordrer: " + e.getMessage());
        }
    }

    public static Order createOrder(User user, ConnectionPool connectionPool) throws DatabaseException {
        Order newOrder = OrderMapper.createOrder(user, connectionPool);
        if (newOrder == null) {
            throw new DatabaseException("Order creation failed. Order is null.");
        }
        return newOrder;
    }

    public static List<Order> getAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                Date orderDate = rs.getDate("order_date");
                double totalPrice = rs.getDouble("total_price");
                String orderStatus = rs.getString("order_status");
                int customerId = rs.getInt("customer_id");
                int workerId = rs.getInt("worker_id");

                LocalDate localOrderDate = orderDate.toLocalDate();
                Order order = new Order(orderId, localOrderDate, totalPrice, orderStatus, customerId, workerId);
                orderList.add(order);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all orders", e.getMessage());
        }

        return orderList;
    }
}
