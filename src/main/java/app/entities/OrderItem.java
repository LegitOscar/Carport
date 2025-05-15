package app.entities;

public class OrderItem {
    private int orderItemId;
    private Order order;
    private ProductVariant productVariant;
    private int quantity;
    private String description;
    private double unitPrice;
    private double totalPrice;

    public OrderItem(int orderItemId, Order order, ProductVariant productVariant, int quantity, String description) {
        this.orderItemId = orderItemId;
        this.order = order;
        this.productVariant = productVariant;
        this.quantity = quantity;
        this.description = description;
        this.unitPrice = productVariant.getPrice();
        this.totalPrice = unitPrice * quantity;
    }

    public OrderItem(Order order, ProductVariant productVariant, int quantity, String description) {
        this(-1, order, productVariant, quantity, description);
    }

    // Getters
    public int getOrderItemId() {
        return orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDescription() {
        return description;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    // Setters (hvis nødvendigt)
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = unitPrice * quantity;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
