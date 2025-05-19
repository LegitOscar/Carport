package app.entities;

public class Material {
    private int materialId;
    private String name;

    public Material(int materialId, String name) {
        this.materialId = materialId;
        this.name = name;
    }

    public int getMaterialId() {
        return materialId;
    }

    public String getName() {
        return name;
    }
}
