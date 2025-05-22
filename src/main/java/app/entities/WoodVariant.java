package app.entities;

public class WoodVariant {
    private int woodVariantId;
    private String MaterialName;
    private int materialId;
    private int lengthCm;
    private String size;
    private double price;

    public WoodVariant(int woodVariantId, String materialName, int materialId, int lengthCm, String size, double price) {
        this.woodVariantId = woodVariantId;
        this.MaterialName = materialName;
        this.materialId = materialId;
        this.lengthCm = lengthCm;
        this.size = size;
        this.price = price;
    }

    public int getWoodVariantId() {
        return woodVariantId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public String getMaterialName() {
        return MaterialName;
    }

    public int getLengthCm() {
        return lengthCm;
    }

    public String size(){
        return size;
    }

    public double getPrice() {
        return price;
    }
    @Override
    public String toString() {
        return "WoodVariant{" +
                "id=" + woodVariantId +
                ", materialId=" + materialId +
                ", length=" + lengthCm +
                ", size='" + size + '\'' +
                ", price=" + price +
                '}';
    }
}
