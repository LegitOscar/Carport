package app.entities;

public class OrderDetails {
    private Order order;
    private User customer;  // or Customer entity if you have that

    public OrderDetails(Order order, User customer) {
        this.order = order;
        this.customer = customer;
    }

    public Order getOrder() { return order; }
    public User getCustomer() { return customer; }
}
