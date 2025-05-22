package app.entities;

public class ProductVariant {
    private int productVariantId;
    private Product product;
    private int length;
    private int woodTypeId; // evt. senere tilføje en rigtig WoodType-klasse

    public ProductVariant(int productVariantId, Product product, int length, int woodTypeId) {
        this.productVariantId = productVariantId;
        this.product = product;
        this.length = length;
        this.woodTypeId = woodTypeId;
    }

    public int getProductVariantId() { return productVariantId; }
    public Product getProduct() { return product; }
    public int getLength() { return length; }
    public int getWoodTypeId() { return woodTypeId; }

    public double getPrice() {
        return product.getPrice(); // Pris sidder på selve produktet
    }
}
