package com.ecommerce.model;

/**
 * Order fulfillment statuses.
 */
public enum OrderStatus {
    PENDING("Pending Payment"),
    CONFIRMED("Order Confirmed"),
    PROCESSING("Processing & Packing"),
    SHIPPED("Shipped & In Transit"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayStatus;

    OrderStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public String getDisplayStatus() {
        return displayStatus;
    }
}
