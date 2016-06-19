package me.dibro.salesbot;

import java.util.Arrays;

public class Result {
    private Product[] products;
    private int idx;

    public Result(Product[] products) {
        this.products = products;
        this.idx = -1;
    }

    public boolean hasNext() {
        return idx + 1 < products.length;
    }

    public boolean hasBack() {
        return idx - 1 >= 0;
    }

    public Product next() {
        return products[++idx];
    }

    public Product back() {
        return products[--idx];
    }

    public int getTotal() {
        return products.length;
    }

    public int getCurrent() {
        return idx;
    }

    @Override
    public String toString() {
        return "Result{" +
                "products=" + Arrays.toString(products) +
                ", idx=" + idx +
                '}';
    }
}