package app.entities;

public class Carport {
    private int carportId;
    private int widthCm;
    private int lengthCm;
    private int BOMId;
    private int orderId;

    public Carport(int carportId, int widthCm, int lengthCm) {
        this.carportId = carportId;
        this.widthCm = widthCm;
        this.lengthCm = lengthCm;
    }

    public Carport (int widthCm, int lengthCm){
        this.widthCm = widthCm;
        this.lengthCm = lengthCm;
    }

    public int getCarportId() {
        return carportId;
    }

    public void setCarportId(int carportId) {
        this.carportId = carportId;
    }

    public int getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(int widthCm) {
        this.widthCm = widthCm;
    }

    public int getLengthCm() {
        return lengthCm;
    }

    public void setLengthCm(int lengthCm) {
        this.lengthCm = lengthCm;
    }

    public int getBOMId() {
        return BOMId;
    }

    public void setBOMId(int BOMId) {
        this.BOMId = BOMId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
