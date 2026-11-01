package com.ecommerce.model;

/**
 * Historical snapshot of an item in a placed order.
 */
public class OrderItem {
    private final String productId;
    private final String productName;
    private final double unitPrice;
    private final int quantity;
    private final double totalPrice;

    public OrderItem(String productId, String productName, double unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = Math.round((unitPrice * quantity) * 100.0) / 100.0;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}
