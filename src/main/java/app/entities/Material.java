package app.entities;

public class Material {
    private int materialId;
    private String name;
    private String unit;

    public Material(int materialId, String name, String unit) {
        this.materialId = materialId;
        this.name = name;
        this.unit = unit;
    }

    public int getMaterialId() {
        return materialId;
    }

    public String getName() {
        return name;
    }

    public String getUnit(){
        return unit;
    }
}
