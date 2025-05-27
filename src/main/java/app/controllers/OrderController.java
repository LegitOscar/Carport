package app.controllers;

import app.services.CarportSvg;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

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
        app.post("/updateitem", ctx -> updateItem(ctx, connectionPool));

        app.get("/profile", ctx -> CustomerProfileController.showProfile(ctx, connectionPool));
        app.get("/profile/edit", ctx -> CustomerProfileController.editProfile(ctx, connectionPool));
        app.post("/profile/update", ctx -> CustomerProfileController.updateProfile(ctx, connectionPool));
        app.post("/deletefitting", ctx -> deleteItem(ctx, connectionPool));


        app.get("/addfitting", ctx -> {
            Map<String, Object> model = new HashMap<>();

            // Show the "Add Fitting" form
            model.put("showAddForm", true);

            // Also include the orders like in /sellerdashboard
            int workerId = ctx.sessionAttribute("workerId"); // Or however you store it
            List<Order> orders = OrderMapper.getAllOrdersPerWorker(workerId, connectionPool);
            List<Order> otherOrders = OrderMapper.getOrdersNotAssignedToWorker(workerId, connectionPool);

            model.put("orders", orders);
            model.put("otherOrders", otherOrders);

            ctx.render("/sellerdashboard.html", model);
        });


        app.get("/sellerdashboard", ctx -> {
            Integer currentWorkerId = ctx.sessionAttribute("workerId");
            if (currentWorkerId == null) {
                ctx.redirect("/login");
                return;
            }

            List<Order> orders = OrderMapper.getAllOrdersPerWorker(currentWorkerId, connectionPool);
            List<Order> otherOrders = OrderMapper.getOrdersNotAssignedToWorker(currentWorkerId, connectionPool);

            // Query materials (fittings)
            List<Map<String, Object>> fittings = new ArrayList<>();
            String sql = "SELECT m.material_id, m.material_name, m.unit, f.size, f.quantity_per_package, f.price " +
                    "FROM material m JOIN fittings_and_screws f ON m.material_id = f.material_id";

            try (Connection conn = connectionPool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("materialId", rs.getInt("material_id"));          // <--- Add this line
                    row.put("materialName", rs.getString("material_name"));
                    row.put("unit", rs.getString("unit"));
                    row.put("size", rs.getString("size"));
                    row.put("quantityPerPackage", rs.getInt("quantity_per_package"));
                    row.put("price", rs.getDouble("price"));
                    fittings.add(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            Map<String, Object> model = new HashMap<>();
            model.put("orders", orders);
            model.put("otherOrders", otherOrders);
            model.put("fittings", fittings);  // add materials to model

            // Support edit mode if requested
            String editOrderId = ctx.queryParam("editOrderId");
            if (editOrderId != null) {
                model.put("editOrderId", Integer.parseInt(editOrderId));
            }

            ctx.render("sellerdashboard.html", model);
        });



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


        app.post("/generateCarport", ctx -> {
            Locale.setDefault(Locale.US);

            String breddeStr = ctx.formParam("width");
            String længdeStr = ctx.formParam("length");
            String shedBreddeStr = ctx.formParam("redskabsrumBredde");
            String shedLængdeStr = ctx.formParam("redskabsrumLængde");

            //DEBUGING
            System.out.println("width = " + breddeStr);
            System.out.println("length = " + længdeStr);
            System.out.println("shed width = " + shedBreddeStr);
            System.out.println("shed length = " + shedLængdeStr);


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
                int carportId = OrderMapper.getCarportIdByOrderId(orderId, connectionPool); // skal laves i mapperen
                Carport carport = CarportMapper.getCarportById(carportId, connectionPool);

                String oldstatus = order.getOrderStatus();
                order.setTotalPrice(totalPrice);
                order.setOrderStatus(orderStatus);
                String notes = ctx.formParam("internalNotes");
                order.setInternalNotes(notes);
                OrderMapper.updateOrder(order, connectionPool);

                if("Pending".equalsIgnoreCase(oldstatus) && "Confirmed".equalsIgnoreCase(orderStatus)){
                    sendOrderStatusUpdateEmail(order);
                }
                if("payment".equalsIgnoreCase(oldstatus) && "Completed".equalsIgnoreCase(orderStatus)){
                    sendBillOfMaterialsEmail(order,carport);}
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

            String editOrderIdParam = ctx.queryParam("editOrderId");
            Integer editOrderId = null;
            if (editOrderIdParam != null) {
                try {
                    editOrderId = Integer.parseInt(editOrderIdParam);
                } catch (NumberFormatException e) {
                    editOrderId = null;
                }
            }

            // New: Read the detailsOrderId query param
            String detailsOrderIdParam = ctx.queryParam("detailsOrderId");
            Integer detailsOrderId = null;
            if (detailsOrderIdParam != null) {
                try {
                    detailsOrderId = Integer.parseInt(detailsOrderIdParam);
                } catch (NumberFormatException e) {
                    detailsOrderId = null;
                }
            }

            ctx.attribute("orders", orders);
            ctx.attribute("otherOrders", otherOrders);
            ctx.attribute("editOrderId", editOrderId);

            // If detailsOrderId is present, fetch the full order details and pass to template
            if (detailsOrderId != null) {
                OrderDetails orderDetails = OrderMapper.getOrderDetailsById(detailsOrderId, connectionPool);
                ctx.attribute("orderDetails", orderDetails);
                ctx.attribute("detailsOrderId", detailsOrderId);
            }

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

    private static void sendBillOfMaterialsEmail(Order order, Carport carport) throws DatabaseException {
        GmailEmailSenderHTML sender = new GmailEmailSenderHTML();
        ConnectionPool connectionPool = new ConnectionPool();

        String customerEmail = UserMapper.getEmailByUserId(order.getCustomerId(), connectionPool);
        String customerName = UserMapper.getNameByUserId(order.getCustomerId(), connectionPool);

        List<WoodVariant> woodVariants = WoodVariantMapper.getAllWoodVariants(connectionPool);
        Calculator calculator = new Calculator(woodVariants);
        List<OrderItem> items = calculator.generateBillOfMaterials(carport);
        Map<String, Object> variables = Map.of(
                "title", "Din ordre er betalt!",
                "name", customerName != null ? customerName : "kunde",
                "message", "Ordre #" + order.getOrderId() + " er blevet betalt - derfor modtager du nu din stykliste",
                "billOfMaterials", items // Vigtigt: hele listen sendes ind
        );

        String html = sender.renderTemplate("BillOfMaterials.html", variables);

        try {
            sender.sendHtmlEmail(customerEmail, "Carport fra FOG - Stykliste", html);
        } catch (MessagingException e) {
            System.err.println("Kunne ikke sende e-mail: " + e.getMessage());
        }
    }


    public static void updateItem(Context ctx, ConnectionPool connectionPool) {
        try {
            int materialId = Integer.parseInt(ctx.formParam("materialId"));
            String size = ctx.formParam("size");
            int quantityPerPackage = Integer.parseInt(ctx.formParam("quantityPerPackage"));
            double price = Double.parseDouble(ctx.formParam("price"));

            String sql = "UPDATE fittings_and_screws SET quantity_per_package = ?, price = ? WHERE material_id = ? AND size = ?";

            try (Connection conn = connectionPool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, quantityPerPackage);
                ps.setDouble(2, price);
                ps.setInt(3, materialId);
                ps.setString(4, size);

                int updated = ps.executeUpdate();
                if (updated == 0) {
                    ctx.status(404).result("No item found to update with given material_id and size");
                    return;
                }
                ctx.status(200).result("Item updated successfully");
            }
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid number format in parameters");
        } catch (SQLException e) {
            ctx.status(500).result("Failed to update item: " + e.getMessage());
        }
    }

    public static void deleteItem(Context ctx, ConnectionPool connectionPool) {
        try {

            int materialId = Integer.parseInt(ctx.formParam("materialId"));

            String sql = "DELETE FROM material WHERE material_id = ?";

            try (Connection conn = connectionPool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, materialId);

                int deleted = ps.executeUpdate();
                if (deleted == 0) {
                    ctx.status(404).result("No item found to delete with materialId: " + materialId);
                    return;
                }
                ctx.status(200).result("Item '" + "' deleted successfully.");
            }
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid number format in parameters");
        } catch (SQLException e) {
            ctx.status(500).result("Failed to delete item: " + e.getMessage());
        }
    }



}

