package app.entities;

public class WoodVariant {
    private int woodVariantId;
    private int materialId;
    private int lengthCm;
    private double price;

    public WoodVariant(int woodVariantId, int materialId, int lengthCm, double price) {
        this.woodVariantId = woodVariantId;
        this.materialId = materialId;
        this.lengthCm = lengthCm;
        this.price = price;
    }

    public int getWoodVariantId() {
        return woodVariantId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public int getLengthCm() {
        return lengthCm;
    }

    public double getPrice() {
        return price;
    }
}
