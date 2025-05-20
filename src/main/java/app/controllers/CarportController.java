package app.controllers;

import app.entities.Carport;
import app.entities.OrderItem;
import app.entities.WoodVariant;
import app.persistence.ConnectionPool;
import app.persistence.WoodVariantMapper;
import app.services.Calculator;
import io.javalin.http.Context;

import java.util.List;

public class CarportController {

    public static void beregnCarport(Context ctx, ConnectionPool connectionPool) {
        int lengthCm = Integer.parseInt(ctx.formParam("lengthCm"));
        int widthCm = Integer.parseInt(ctx.formParam("widthCm"));

        Carport carport = new Carport(0,lengthCm, widthCm); // evt. udvidet constructor

        WoodVariantMapper woodVariantMapper = new WoodVariantMapper(connectionPool);
        List<app.entities.WoodVariant> woodVariants = woodVariantMapper.getAllWood();

        Calculator calculator = new Calculator(woodVariants);
        List<OrderItem> orderItems = calculator.calculateMaterials(carport);
        double totalPrice = calculator.calculateTotalPrice(orderItems);

        // Send data til visning
        ctx.attribute("orderItems", orderItems);
        ctx.attribute("totalPrice", totalPrice);
        ctx.render("stykliste.html");
    }

    public void handleNewCarportOrder(Context ctx) {
        int length = Integer.parseInt(ctx.formParam("length"));
        int width = Integer.parseInt(ctx.formParam("width"));

        Carport carport = new Carport(0, width, length); // ID = 0 før den er gemt

        // Kald Calculator med woodVariants
        List<WoodVariant> variants = WoodVariantMapper.getAllWood();
        Calculator calculator = new Calculator(variants);
        List<OrderItem> orderItems = calculator.calculateMaterials(carport);
        double totalPrice = calculator.calculateTotalPrice(orderItems);

        // Gem i database:
        int carportId = carportMapper.createCarport(carport);
        int orderId = orderMapper.createOrder(carportId, userId, totalPrice);
        billOfMaterialsMapper.create(orderItems, orderId);

        // evt. redirect eller vis kvittering
    }

}
