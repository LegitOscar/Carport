package app.entities;

public class OrderItem {
    private int orderItemId;     // Optional - hvis du vil bruge den
    private int woodVariantId;
    private int quantity;
    private double unitPrice;

    public OrderItem(int woodVariantId, int quantity, double unitPrice) {
        this.woodVariantId = woodVariantId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getWoodVariantId() {
        return woodVariantId;
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
}
