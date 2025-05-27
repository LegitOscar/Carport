
package app.entities;

public class FittingsAndScrews {

    private int FittingsAndScrewsId;
    private int MaterialId;
    private String sizeFS;
    private int QuantityPerPackage;
    private double priceFS;

    public FittingsAndScrews(int fittingsAndScrewsId, int materialId, String sizeFS, int quantityPerPackage, double priceFS) {
        FittingsAndScrewsId = fittingsAndScrewsId;
        MaterialId = materialId;
        this.sizeFS = sizeFS;
        QuantityPerPackage = quantityPerPackage;
        this.priceFS = priceFS;
    }

    public int getFittingsAndScrewsId() {
        return FittingsAndScrewsId;
    }

    public void setFittingsAndScrewsId(int fittingsAndScrewsId) {
        FittingsAndScrewsId = fittingsAndScrewsId;
    }

    public int getMaterialId() {
        return MaterialId;
    }

    public void setMaterialId(int materialId) {
        MaterialId = materialId;
    }

    public String getSizeFS() {
        return sizeFS;
    }

    public void setSizeFS(String sizeFS) {
        this.sizeFS = sizeFS;
    }

    public int getQuantityPerPackage() {
        return QuantityPerPackage;
    }

    public void setQuantityPerPackage(int quantityPerPackage) {
        QuantityPerPackage = quantityPerPackage;
    }

    public double getPriceFS() {
        return priceFS;
    }

    public void setPriceFS(double priceFS) {
        this.priceFS = priceFS;
    }
}