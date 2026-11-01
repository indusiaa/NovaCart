package com.ecommerce.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Placed customer order entity.
 */
public class Order {
    private final String orderId;
    private final String customerName;
    private final String customerEmail;
    private final String customerPhone;
    private final String shippingAddress;
    private final List<OrderItem> items;
    private final double subtotal;
    private final double discountAmount;
    private final double tax;
    private final double shippingFee;
    private final double totalAmount;
    private final PaymentMethod paymentMethod;
    private final String transactionId;
    private OrderStatus status;
    private final String trackingNumber;
    private final Instant createdAt;

    public Order(String orderId, String customerName, String customerEmail, 
                 String customerPhone, String shippingAddress, List<OrderItem> items, 
                 double subtotal, double discountAmount, double tax, 
                 double shippingFee, double totalAmount, PaymentMethod paymentMethod, 
                 String transactionId, OrderStatus status, String trackingNumber) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.shippingAddress = shippingAddress;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.tax = tax;
        this.shippingFee = shippingFee;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.status = status;
        this.trackingNumber = trackingNumber;
        this.createdAt = Instant.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTax() {
        return tax;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public synchronized OrderStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
