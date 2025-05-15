package app.entities;

public class Product {
    private int productId;
    private String name;
    private String unit;
    private int price;

    public Product(int productId, String name, String unit, int price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
    }
    public int getProductId(){
        return 0; // muligvis forkert
    }
}
