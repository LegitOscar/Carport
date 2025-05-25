package app.controllers;

import app.services.CarportSvg;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.*;
import app.services.Calculator;


import static app.controllers.CustomerProfileController.showUserOrders;

import jakarta.mail.MessagingException;
import util.GmailEmailSenderHTML;



public class OrderController {

    private final ConnectionPool connectionPool;

    public OrderController(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {

        app.post("/deleteorder", ctx -> deleteOrder(ctx, connectionPool));

        app.post("/updateorder", ctx -> updateOrder(ctx, connectionPool));

        app.get("/getorders", ctx -> getOrdersForUser(ctx, connectionPool));

        app.get("/profile", ctx -> CustomerProfileController.showProfile(ctx, connectionPool));
        app.get("/profile/edit", ctx -> CustomerProfileController.editProfile(ctx, connectionPool));
        app.post("/profile/update", ctx -> CustomerProfileController.updateProfile(ctx, connectionPool));


        app.get("/sellerdashboard", ctx -> showSellerDashboard(ctx, connectionPool));

        app.post("/selectorder", ctx -> assignOrderToWorker(ctx, connectionPool));

        app.get("/orderSite2", ctx -> ctx.render("orderSite2.html"));

        app.get("/user/orders", ctx -> showUserOrders(ctx, connectionPool));

        app.post("/orderSite2", ctx -> {
            int bredde = Integer.parseInt(ctx.formParam("bredde"));
            int længde = Integer.parseInt(ctx.formParam("længde"));
            String tag = ctx.formParam("tag");
            String bemærkning = ctx.formParam("bemærkning");

            ctx.sessionAttribute("bredde", bredde);
            ctx.sessionAttribute("længde", længde);
            ctx.sessionAttribute("tag", tag);
            ctx.sessionAttribute("bemærkning", bemærkning);

            ctx.redirect("/orderSite2");
        });

        app.get("/orderSite3", ctx -> ctx.render("orderSite3.html"));


        app.post("/skipStep3", ctx -> {
            User user = ctx.sessionAttribute("currentUser");

            int redskabsrumBredde = Integer.parseInt(ctx.formParam("redskabsrumBredde"));
            int redskabsrumLængde = Integer.parseInt(ctx.formParam("redskabsrumLængde"));

            ctx.sessionAttribute("redskabsrumBredde", redskabsrumBredde);
            ctx.sessionAttribute("redskabsrumLængde", redskabsrumLængde);

            if (user != null) {
                ctx.render("orderConfirmation.html");
            } else {
                ctx.redirect("/orderSite3");
            }
        });

        app.post("/generateCarport", ctx -> {
            Locale.setDefault(Locale.US);

            String breddeStr = ctx.formParam("bredde");
            String længdeStr = ctx.formParam("længde");
            String shedBreddeStr = ctx.formParam("redskabsrumBredde");
            String shedLængdeStr = ctx.formParam("redskabsrumLængde");

            if (breddeStr == null || længdeStr == null || shedBreddeStr == null || shedLængdeStr == null) {
                ctx.status(400).result("En eller flere parametre mangler!");
                return;
            }

            int width = Integer.parseInt(breddeStr);
            int length = Integer.parseInt(længdeStr);
            int shedWidth = Integer.parseInt(shedBreddeStr);
            int shedLength = Integer.parseInt(shedLængdeStr);

            CarportSvg carportSvg = new CarportSvg(width, length, shedWidth, shedLength);
            String svg = carportSvg.toString();

            ctx.attribute("svg", svg);
            ctx.render("showOrder.html");
        });


        app.get("/orders", ctx -> {
            // Your snippet goes here
            User user = ctx.sessionAttribute("currentUser"); // get user from session

            if (user == null) {
                ctx.status(401).result("You are not logged in");
                return;
            }

            List<Order> orders = OrderMapper.getAllOrdersPerUser(user.getId(), connectionPool);

            ctx.attribute("user", user);
            ctx.attribute("orders", orders);
            ctx.render("orders.html");
        });


    }



    private static void updateOrder(Context ctx, ConnectionPool connectionPool) {
        try {
            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            double totalPrice = Double.parseDouble(ctx.formParam("totalPrice"));
            String orderStatus = ctx.formParam("orderStatus");

            Order order = OrderMapper.getOrderById(orderId, connectionPool);

            if (order != null) {
                String oldstatus = order.getOrderStatus();
                order.setTotalPrice(totalPrice);
                order.setOrderStatus(orderStatus);
                OrderMapper.updateOrder(order, connectionPool);

                if("Pending".equalsIgnoreCase(oldstatus) && "Confirmed".equalsIgnoreCase(orderStatus)){
                    sendOrderStatusUpdateEmail(order);
                }
            }

            ctx.redirect("/sellerdashboard");

        } catch (DatabaseException | NumberFormatException e) {
            ctx.status(400).result("Fejl ved opdatering af ordre: " + e.getMessage());
        }
    }

    private static void showSellerDashboard(Context ctx, ConnectionPool connectionPool) {
        Integer currentWorkerId = ctx.sessionAttribute("workerId");

        if (currentWorkerId == null) {
            ctx.redirect("/login");
            return;
        }

        try {
            List<Order> orders = OrderMapper.getAllOrdersPerWorker(currentWorkerId, connectionPool);
            List<Order> otherOrders = OrderMapper.getOrdersNotAssignedToWorker(currentWorkerId, connectionPool);

            // Read the editOrderId query parameter from URL (e.g., /sellerdashboard?editOrderId=6)
            String editOrderIdParam = ctx.queryParam("editOrderId");
            Integer editOrderId = null;
            if (editOrderIdParam != null) {
                try {
                    editOrderId = Integer.parseInt(editOrderIdParam);
                } catch (NumberFormatException e) {
                    // Optional: handle invalid editOrderId param gracefully
                    editOrderId = null;
                }
            }

            ctx.attribute("orders", orders);
            ctx.attribute("otherOrders", otherOrders);

            // Pass editOrderId to the template so you can show the edit form for that order
            ctx.attribute("editOrderId", editOrderId);

            ctx.render("sellerdashboard.html");

        } catch (DatabaseException e) {
            ctx.status(500).result("Databasefejl: " + e.getMessage());
        }
    }


    private static void assignOrderToWorker(Context ctx, ConnectionPool connectionPool) {
        try {
            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            Integer currentWorkerId = ctx.sessionAttribute("workerId");

            if (currentWorkerId == null) {
                ctx.status(401).result("Ikke logget ind.");
                return;
            }

            OrderMapper.assignOrderToWorker(orderId, currentWorkerId, connectionPool);
            ctx.redirect("/sellerdashboard");

        } catch (DatabaseException | NumberFormatException e) {
            ctx.status(400).result("Fejl ved tildeling af ordre: " + e.getMessage());
        }
    }

    private static void deleteOrder(Context ctx, ConnectionPool connectionPool) {
        try {
            int orderId = Integer.parseInt(ctx.formParam("orderId"));
            OrderMapper.deleteOrder(orderId, connectionPool);
            ctx.redirect("/sellerdashboard");
        } catch (NumberFormatException e) {
            ctx.status(400).result("Ugyldigt order ID");
        } catch (DatabaseException e) {
            ctx.status(500).result("Fejl ved sletning af ordre: " + e.getMessage());
        }
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

    private static void sendOrderStatusUpdateEmail(Order order) throws DatabaseException {
        GmailEmailSenderHTML sender = new GmailEmailSenderHTML();

        ConnectionPool connectionPool = new ConnectionPool();

        // You might want to fetch actual customer info from DB; this is just for illustration
        String customerEmail = UserMapper.getEmailByUserId(order.getCustomerId(), connectionPool); // You need to implement this method
        String customerName = UserMapper.getNameByUserId(order.getCustomerId(), connectionPool);   // Optional, for personalization

        Map<String, Object> variables = Map.of(
                "title", "Din ordre er blevet bekræftet!",
                "name", customerName != null ? customerName : "kunde",
                "message", "Ordre #" + order.getOrderId() + " er nu blevet bekræftet og er under behandling."
        );

        String html = sender.renderTemplate("email.html", variables);

        try {
            sender.sendHtmlEmail(customerEmail, "Ordrebekræftelse – Din ordre er bekræftet", html);
        } catch (MessagingException e) {
            System.err.println("Kunne ikke sende e-mail: " + e.getMessage());
        }
    }


}

