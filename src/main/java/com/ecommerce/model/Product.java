package com.ecommerce.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Product domain entity.
 */
public class Product {
    private final String id;
    private final String name;
    private final String description;
    private final double price;
    private final Category category;
    private int stockQuantity;
    private final double rating;
    private final String imageUrl;
    private final List<String> tags;

    public Product(String id, String name, String description, double price, 
                   Category category, int stockQuantity, double rating, 
                   String imageUrl, List<String> tags) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public synchronized int getStockQuantity() {
        return stockQuantity;
    }

    public double getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public synchronized boolean isInStock(int quantity) {
        return this.stockQuantity >= quantity;
    }

    public synchronized boolean deductStock(int quantity) {
        if (quantity <= 0) return false;
        if (isInStock(quantity)) {
            this.stockQuantity -= quantity;
            return true;
        }
        return false;
    }

    public synchronized void replenishStock(int quantity) {
        if (quantity > 0) {
            this.stockQuantity += quantity;
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - $%.2f [Stock: %d]", name, id, price, stockQuantity);
    }
}
