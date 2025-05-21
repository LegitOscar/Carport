package app.entities;

import java.time.LocalDate;

public class Order {
    private int orderId;
    private LocalDate orderDate;
    private double totalPrice;
    private String orderStatus;
    private int customerId;
    private int workerId;
    private int carportId;

    public Order (int orderId, LocalDate orderDate, double totalPrice, String orderStatus, int customerId, int workerId, int carportId){
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.customerId = customerId;
        this.workerId = workerId;
        this.carportId = carportId;
    }

    public int getOrderId(){
        return orderId;
    }

    public void setOrderId(int orderId){
        this.orderId = orderId;
    }

    public LocalDate getOrderDate(){
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate){
        this.orderDate = orderDate;
    }

    public double getTotalPrice(){
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice){
        this.totalPrice = totalPrice;
    }

    public String getOrderStatus(){
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus){
        this.orderStatus = orderStatus;
    }

    public int getCustomerId(){
        return customerId;
    }

    public void setCustomerId(int customerId){
        this.customerId = customerId;
    }

    public int getWorkerId(){
        return workerId;
    }

    public void setWorkerId(int workerId){
        this.workerId = workerId;
    }

    public int getCarportId(){
        return carportId;
    }

    public void setCarportId(int carportId){
        this.carportId = carportId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderID=" + orderId +
                ", orderDate=" + orderDate +
                ", totalPrice=" + totalPrice +
                ", orderStatus='" + orderStatus + '\'' +
                ", customerId=" + customerId +
                ", workerId=" + workerId +
                ". carportId=" + carportId +
                '}';
    }
}