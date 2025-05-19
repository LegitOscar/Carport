package app.entities;

public class Carport {
    private int carportId;
    private int widthCm;
    private int lengthCm;

    public Carport(int carportId, int widthCm, int lengthCm) {
        this.carportId = carportId;
        this.widthCm = widthCm;
        this.lengthCm = lengthCm;
    }

    public int getCarportId() {
        return carportId;
    }

    public int getWidthCm() {
        return widthCm;
    }

    public int getLengthCm() {
        return lengthCm;
    }
}
