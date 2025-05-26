package app.entities;

public class OrderItem {
    private Order order;
    private WoodVariant woodVariant;
    private int quantity;
    private double unitPrice;
    private String description;

    public OrderItem(Order order, WoodVariant woodVariant, int quantity, double unitPrice, String description) {
        this.order = order;
        this.woodVariant = woodVariant;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.description = description;
    }

    public Order getOrder() {
        return order;
    }

    public WoodVariant getWoodVariant() {
        return woodVariant;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalPrice() {
        return quantity * unitPrice;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                ", orderId=" + (order != null ? order.getOrderId() : "null") +
                ", woodVariantId=" + (woodVariant != null ? woodVariant.getWoodVariantId() : "null") +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}



