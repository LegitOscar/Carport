package app.entities;

import java.sql.Date;

public class Order {
    int orderID;
    Date orderDate;
    double totalPrice;
    String orderStatus;

    public Order (int orderID, Date orderDate, double totalPrice, String orderStatus){
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
    }

    public int getOrderId() {
        return orderID;
    }

    public void setOrderId(int orderID) {
        this.orderID = orderID;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderID=" + orderID +
                ", orderDate=" + orderDate +
                ", totalPrice=" + totalPrice +
                ", orderStatus='" + orderStatus + '\'' +
                '}';
    }
}
