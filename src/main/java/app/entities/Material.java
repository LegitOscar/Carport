package app.entities;

public class Material {
    private int materialId;
    private String materialName;
    private String unit;
    private String size;
    private int quantityPerPackage;
    private double price;

    // Constructor for general materials
    public Material(int materialId, String materialName, String unit) {
        this.materialId = materialId;
        this.materialName = materialName;
        this.unit = unit;
    }

    // Extended constructor for fittings and screws
    public Material(int materialId, String materialName, String unit, String size, int quantityPerPackage, double price) {
        this.materialId = materialId;
        this.materialName = materialName;
        this.unit = unit;
        this.size = size;
        this.quantityPerPackage = quantityPerPackage;
        this.price = price;
    }

    public int getMaterialId() {
        return materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getUnit() {
        return unit;
    }

    public String getSize() {
        return size;
    }

    public int getQuantityPerPackage() {
        return quantityPerPackage;
    }

    public double getPrice() {
        return price;
    }
}
