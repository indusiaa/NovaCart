package com.ecommerce.model;

/**
 * Supported payment methods.
 */
public enum PaymentMethod {
    CREDIT_CARD("Credit / Debit Card"),
    UPI("Instant UPI"),
    PAYPAL("PayPal Wallet"),
    CASH_ON_DELIVERY("Cash on Delivery");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PaymentMethod fromString(String str) {
        if (str == null) return CREDIT_CARD;
        for (PaymentMethod pm : values()) {
            if (pm.name().equalsIgnoreCase(str.trim()) || pm.label.equalsIgnoreCase(str.trim())) {
                return pm;
            }
        }
        return CREDIT_CARD;
    }
}
