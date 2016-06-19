package me.dibro.salesbot;

public class Product {
    private String name;
    private String descFull;
    private String descShort;
    private String sourceUrl;
    private String sourceName;
    private boolean inStock;
    private int discount;
    private int price;
    private String started;
    private int duration;

    public Product(String name, String descFull, String descShort, String sourceUrl, String sourceName,
                   boolean inStock, int discount, int price, String started, int duration) {
        this.name = name;
        this.descFull = descFull;
        this.descShort = descShort;
        this.sourceUrl = sourceUrl;
        this.sourceName = sourceName;
        this.inStock = inStock;
        this.discount = discount;
        this.price = price;
        this.started = started;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getDescFull() {
        return descFull;
    }

    public String getDescShort() {
        return descShort;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getSourceName() {
        return sourceName;
    }

    public boolean isInStock() {
        return inStock;
    }

    public int getDiscount() {
        return discount;
    }

    public int getPrice() {
        return price;
    }

    public String getStarted() {
        return started;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", descFull='" + descFull + '\'' +
                ", descShort='" + descShort + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", inStock=" + inStock +
                ", discount=" + discount +
                ", price=" + price +
                ", started=" + started +
                ", duration=" + duration +
                '}';
    }
}