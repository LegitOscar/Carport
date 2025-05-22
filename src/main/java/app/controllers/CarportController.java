package app.controllers;

import app.entities.Carport;
import app.entities.OrderItem;
import app.entities.Order;
import app.entities.User;
import app.entities.WoodVariant;
import app.persistence.*;
import app.services.Calculator;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class CarportController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.post("ordercarport", ctx -> handleOrder(ctx, connectionPool));
        app.post("previewcarport", ctx -> previewCarport(ctx, connectionPool));
        app.get("/order-form", ctx -> ctx.render("orderform.html"));
    }

    public static void handleOrder(Context ctx, ConnectionPool connectionPool) {
        int lengthCm = Integer.parseInt(ctx.formParam("length"));
        int widthCm = Integer.parseInt(ctx.formParam("width"));
        User user = ctx.sessionAttribute("currentUser");

        if (user == null) {
            ctx.status(401).result("Bruger ikke logget ind");
            return;
        }

        try {

            Carport carport = new Carport(lengthCm, widthCm);
            CarportMapper.createCarport(carport, connectionPool);


            Order order = OrderMapper.createOrder(user, carport.getCarportId(), connectionPool);

            List<WoodVariant> woodVariants = WoodVariantMapper.getAllWoodVariants(connectionPool);

            Calculator calculator = new Calculator(woodVariants);
            List<OrderItem> orderItems = calculator.calculateMaterials(carport, order);

            for (OrderItem item : orderItems) {
                OrderItemMapper.insertOrderItem(item, connectionPool);
            }

            double totalPrice = calculator.calculateTotalPrice(orderItems);
            order.setTotalPrice(totalPrice);
            OrderMapper.updateTotalPrice(order.getOrderId(), totalPrice, connectionPool);

            ctx.attribute("order", order);
            ctx.attribute("carport", carport);
            ctx.attribute("orderItems", orderItems);
            ctx.render("receipt.html");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl under ordreoprettelse: " + e.getMessage());
        }
    }

    public static void previewCarport(Context ctx, ConnectionPool connectionPool) {
        try {
            int length = Integer.parseInt(ctx.formParam("length"));
            int width = Integer.parseInt(ctx.formParam("width"));


            Carport carport = new Carport(length, width);

            List<WoodVariant> woodVariants = WoodVariantMapper.getAllWoodVariants(connectionPool);

            Calculator calculator = new Calculator(woodVariants);
            List<OrderItem> orderItems = calculator.generateBillOfMaterials(carport);
            double totalPrice = calculator.calculateTotalPrice(orderItems);

            ctx.attribute("carport", carport);
            ctx.attribute("totalPrice", totalPrice);
            ctx.render("preview.html");

        } catch (NumberFormatException e) {
            ctx.status(400).result("Ugyldigt input");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved beregning");
        }
    }
}


