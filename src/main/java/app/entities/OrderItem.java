package app.entities;

public class OrderItem {
    private Order order;
    private WoodVariant woodVariant;
    private int quantity;
    private double unitPrice;

    // (uden ID endnu)
    public OrderItem(Order order, WoodVariant woodVariant, int quantity, double unitPrice) {
        this.order = order;
        this.woodVariant = woodVariant;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
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



