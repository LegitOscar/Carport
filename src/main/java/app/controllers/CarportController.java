package app.controllers;

import app.persistence.ConnectionPool;
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

        int redskabsrumBredde = Integer.parseInt(ctx.formParam("redskabsrumBredde"));
        int redskabsrumLængde = Integer.parseInt(ctx.formParam("redskabsrumLængde"));

        ctx.sessionAttribute("redskabsrumBredde", redskabsrumBredde);
        ctx.sessionAttribute("redskabsrumLængde", redskabsrumLængde);

        try {
            Carport carport = new Carport(lengthCm, widthCm);
            CarportMapper.createCarport(carport, connectionPool);

            if (user == null) {
                ctx.sessionAttribute("pendingCarport", carport);
                ctx.sessionAttribute("width", widthCm);
                ctx.sessionAttribute("length", lengthCm);
                ctx.render("orderSite3.html"); // en side hvor brugeren udfylder navn, adresse, etc.
                return;
            }

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

            ctx.sessionAttribute("width", widthCm);
            ctx.sessionAttribute("length", lengthCm);
            ctx.sessionAttribute("totalPrice", totalPrice);
            ctx.attribute("order", order);
            ctx.attribute("carport", carport);
            ctx.attribute("orderItems", orderItems);
            ctx.attribute("totalPrice", totalPrice);
            System.out.println("Total price sat i session: " + totalPrice);
            ctx.redirect("/orderConfirmation");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl under ordreoprettelse: " + e.getMessage());
        }
    }


    public static void previewCarport(Context ctx, ConnectionPool connectionPool) {
        try {
            int lengthCm = Integer.parseInt(ctx.formParam("length"));
            int widthCm = Integer.parseInt(ctx.formParam("width"));


            Carport carport = new Carport(lengthCm, widthCm);
            ctx.sessionAttribute("pendingCarport", carport);

            List<WoodVariant> woodVariants = WoodVariantMapper.getAllWoodVariants(connectionPool);

            Calculator calculator = new Calculator(woodVariants);

            List<OrderItem> orderItems = calculator.generateBillOfMaterials(carport);
            double totalPrice = calculator.calculateTotalPrice(orderItems);

            ctx.attribute("carport", carport);
            ctx.attribute("totalPrice", totalPrice);
            ctx.sessionAttribute("carport",carport);
            ctx.sessionAttribute("totalPrice",totalPrice);
            ctx.sessionAttribute("width", widthCm);
            ctx.sessionAttribute("length", lengthCm);
            ctx.redirect("/orderSite2");


        } catch (NumberFormatException e) {
            ctx.status(400).result("Ugyldigt input");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved beregning");
        }
    }
}


