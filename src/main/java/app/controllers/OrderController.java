
package app.controllers;

import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OrderController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.post("addorder", ctx -> addOrder(ctx, connectionPool));
        app.post("deleteorder", ctx -> deleteOrder(ctx, connectionPool));
        app.post("editorder", ctx -> editOrder(ctx, connectionPool));
        app.post("updateorder", ctx -> updateOrder(ctx, connectionPool));
        app.get("getorders", ctx -> getOrdersForUser(ctx, connectionPool));
        app.get("allorders", ctx -> getAllOrders(ctx, connectionPool));
    }

    private static void addOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException, SQLException {
        User user = ctx.sessionAttribute("currentUser");
        if (user == null) {
            ctx.status(401).result("Du er ikke logget ind.");
            return;
        }

        Order order = OrderMapper.createOrder(user, connectionPool);
        ctx.status(201).result("Ordre oprettet med ID: " + order.getOrderId());
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
    }

    private static void editOrder(Context ctx, ConnectionPool connectionPool) {
        try {
            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            Order order = OrderMapper.getOrderById(orderId, connectionPool);
            if (order != null) {
                ctx.attribute("order", order);
                ctx.render("editorder.html"); // todo skal ændres til rigtigt html
            } else {
                ctx.status(404).result("Ordre ikke fundet");
            }
        } catch (Exception e) {
            ctx.status(500).result("Fejl ved hentning af ordre: " + e.getMessage());
        }
    }

    public static Order createOrder(User user, ConnectionPool connectionPool) throws DatabaseException, SQLException {
        Order newOrder = OrderMapper.createOrder(user, connectionPool);

        if (newOrder != null) {
            System.out.println("Order created successfully with ID: " + newOrder.getOrderId());
        } else {
            throw new RuntimeException("Order creation failed.");
        }

        return newOrder;
    }

    private static void updateOrder(Context ctx, ConnectionPool connectionPool) {
        try {

            User currentUser = ctx.sessionAttribute("currentUser");

            if (currentUser == null) {
                ctx.status(401).result("Ingen bruger er logget ind.");
                return;
            }

            int customerId = currentUser.getUserId();


            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            LocalDate orderDate = LocalDate.parse(ctx.formParam("orderDate"));
            double totalPrice = Double.parseDouble(ctx.formParam("totalPrice"));
            String orderStatus = ctx.formParam("orderStatus");


            int workerId = 0;
            int carportId = Integer.parseInt(ctx.formParam("carportId")); // Adjust based on your form


            Order order = new Order(orderId, orderDate, totalPrice, orderStatus, customerId, workerId, carportId);
            OrderMapper.updateOrder(order, connectionPool);

            ctx.status(200).result("Ordre opdateret");

        } catch (Exception e) {
            ctx.status(400).result("Fejl ved opdatering af ordre: " + e.getMessage());
        }
    }



    private static void getOrdersForUser(Context ctx, ConnectionPool connectionPool) {
        User user = ctx.sessionAttribute("currentUser");
        if (user == null) {
            ctx.status(401).result("Du er ikke logget ind.");
            return;
        }

        try {
            List<Order> orders = OrderMapper.getAllOrdersPerUser(user.getUserId(), connectionPool);
            ctx.attribute("orders", orders);
            ctx.render("orders.html"); //todo ændre muligvis HTML
        } catch (DatabaseException e) {
            ctx.status(500).result("Fejl ved hentning af ordrer: " + e.getMessage());
        }
    }

    private static void getAllOrders(Context ctx, ConnectionPool connectionPool) {
        try {
            List<Order> orders = OrderMapper.getAllOrders(connectionPool);
            ctx.attribute("orders", orders);
            ctx.render("allorders.html"); // todo: opret/tilpas HTML-side
        } catch (DatabaseException e) {
            ctx.status(500).result("Fejl ved hentning af alle ordrer: " + e.getMessage());
        }
    }
}

