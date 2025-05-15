package app.services;

import app.entities.Order;
import app.persistence.ConnectionPool;

import java.util.ArrayList;
import java.util.List;

public class Calculator {
    private static final int Pole = //sit ID
    private static final int Beams = //sit ID
    private static final int Rafters = //sit ID

    private List<OrderItem> orderItems = new ArrayList<>();
    private int width;
    private int length;
    private ConnectionPool connectionPool;

    public Calculator( int width, int length, ConnectionPool connectionPool) {
        this.width = width;
        this.length = length;
        this.connectionPool = connectionPool;
    }

    public void calculateCarport(Order order){
        calculatePoles(order);
        calculateBeams(order);
        calculateRafters(order);

    }
    //Stolper
    private void calculatePoles(Order order){
        // Beregn antal Stolper
        int quantity = 6;
        // Finde Længde på stolper - dvs variant (300cm)
        List<ProductVariant> productVariant = ProductMapper.getVariantsByProductIdAndMinLength(0, Pole, connectionPool);
        OrderItem orderItem = OrderItem(0);
    }
    // Remme
    private void calculateBeams(Order order){}

    // Spær
    private void calculateRafters(Order order){}

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
}
