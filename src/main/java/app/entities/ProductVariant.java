package app.entities;

public class ProductVariant {
    private int productVariantId;
    private Product product;
    private int lenght;

    public ProductVariant(int productVariantId, Product product, int lenght){
        this.productVariantId = productVariantId;
        this.product= product;
        this.lenght = lenght;
    }

    public int getProductVariantId(){
        return productVariantId;
    }

    public Product getProduct(){ //muligvis forkert
        return product;
    }

    public int getLenght(){
        return lenght;
    }
}
