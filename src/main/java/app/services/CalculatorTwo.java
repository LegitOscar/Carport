package app.services;

import app.entities.Order;
import app.entities.OrderItem;
import app.entities.ProductVariant;
import app.persistence.ConnectionPool;
import app.persistence.ProductMapper;

import java.util.ArrayList;
import java.util.List;

public class Calculator {
    private static final int POLE_ID = 1;   // fx. stolper
    private static final int BEAM_ID = 2;   // fx. remme
    private static final int RAFTER_ID = 3; // fx. spær

    private List<OrderItem> orderItems = new ArrayList<>();
    private int width;
    private int length;
    private ConnectionPool connectionPool;
    private double totalPrice;

    public Calculator(int width, int length, ConnectionPool connectionPool) {
        this.width = width;
        this.length = length;
        this.connectionPool = connectionPool;
    }

    public void calculateCarport(Order order) {
        calculatePoles(order);
        calculateBeams(order);
        calculateRafters(order);
        order.setTotalPrice(totalPrice); // Opdater ordren med samlet pris
    }

    private void calculatePoles(Order order) {
        int quantity = (length > 600) ? 8 : 6; // Eksempel: flere stolper hvis carport er lang
        ProductVariant variant = getCheapestVariantByMinLength(POLE_ID, 300);
        double itemTotal = quantity * variant.getPrice();

        orderItems.add(new OrderItem(order.getOrderId(), POLE_ID, variant.getWoodId(), quantity));
        totalPrice += itemTotal;
    }

    private void calculateBeams(Order order) {
        int quantity = 2;
        ProductVariant variant = getCheapestVariantByMinLength(BEAM_ID, length);
        double itemTotal = quantity * variant.getPrice();

        orderItems.add(new OrderItem(order.getOrderId(), BEAM_ID, variant.getWoodId(), quantity));
        totalPrice += itemTotal;
    }

    private void calculateRafters(Order order) {
        int spacing = 55;
        int quantity = (int) Math.ceil((double) length / spacing);
        ProductVariant variant = getCheapestVariantByMinLength(RAFTER_ID, width);
        double itemTotal = quantity * variant.getPrice();

        orderItems.add(new OrderItem(order.getOrderId(), RAFTER_ID, variant.getWoodId(), quantity));
        totalPrice += itemTotal;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    private ProductVariant getCheapestVariantByMinLength(int productId, int minLength) {
        List<ProductVariant> variants = ProductMapper.getVariantsByProductIdAndMinLength(productId, minLength, connectionPool);
        return variants.stream()
                .min((v1, v2) -> Double.compare(v1.getPrice(), v2.getPrice()))
                .orElseThrow(() -> new RuntimeException("Ingen variant fundet for produkt ID: " + productId));
    }
}
