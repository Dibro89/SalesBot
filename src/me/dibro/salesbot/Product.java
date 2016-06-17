package me.dibro.salesbot;

public class Product {
    private String name;
    private String description;
    private String imageUrl;
    private String sourceUrl;
    private double discount;

    public Product(String name, String description, String imageUrl, String sourceUrl, double discount) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.sourceUrl = sourceUrl;
        this.discount = discount;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public double getDiscount() {
        return discount;
    }
}