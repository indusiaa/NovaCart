package com.ecommerce.model;

/**
 * Coupon / Discount Voucher entity.
 */
public class Coupon {
    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT,
        FREE_SHIPPING
    }

    private final String code;
    private final String description;
    private final DiscountType type;
    private final double value; // e.g. 10 for 10%, 20.0 for $20
    private final double minimumOrderAmount;

    public Coupon(String code, String description, DiscountType type, double value, double minimumOrderAmount) {
        this.code = code != null ? code.trim().toUpperCase() : "";
        this.description = description;
        this.type = type;
        this.value = value;
        this.minimumOrderAmount = minimumOrderAmount;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiscountType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public double getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public boolean isEligible(double subtotal) {
        return subtotal >= minimumOrderAmount;
    }

    public double calculateDiscount(double subtotal, double shippingFee) {
        if (!isEligible(subtotal)) {
            return 0.0;
        }

        switch (type) {
            case PERCENTAGE:
                return Math.round((subtotal * (value / 100.0)) * 100.0) / 100.0;
            case FIXED_AMOUNT:
                return Math.min(value, subtotal);
            case FREE_SHIPPING:
                return shippingFee;
            default:
                return 0.0;
        }
    }
}
