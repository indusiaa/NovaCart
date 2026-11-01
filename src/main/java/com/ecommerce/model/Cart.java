package com.ecommerce.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shopping Cart domain model.
 */
public class Cart {
    private final String cartId;
    private final String sessionId;
    private final Map<String, CartItem> items;
    private Coupon appliedCoupon;

    public static final double DEFAULT_TAX_RATE = 0.08; // 8%
    public static final double FREE_SHIPPING_THRESHOLD = 50.0;
    public static final double STANDARD_SHIPPING_FEE = 5.99;

    public Cart(String cartId, String sessionId) {
        this.cartId = cartId;
        this.sessionId = sessionId;
        this.items = new LinkedHashMap<>();
        this.appliedCoupon = null;
    }

    public String getCartId() {
        return cartId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public synchronized Collection<CartItem> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public synchronized Map<String, CartItem> getItemsMap() {
        return Collections.unmodifiableMap(items);
    }

    public synchronized CartItem getItem(String productId) {
        return items.get(productId);
    }

    public synchronized int getTotalItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public synchronized void addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) return;
        
        CartItem existing = items.get(product.getId());
        if (existing != null) {
            existing.addQuantity(quantity);
        } else {
            items.put(product.getId(), new CartItem(product, quantity));
        }
    }

    public synchronized boolean updateQuantity(String productId, int quantity) {
        if (!items.containsKey(productId)) return false;
        if (quantity <= 0) {
            items.remove(productId);
            return true;
        }
        items.get(productId).setQuantity(quantity);
        return true;
    }

    public synchronized boolean removeItem(String productId) {
        return items.remove(productId) != null;
    }

    public synchronized void clear() {
        items.clear();
        appliedCoupon = null;
    }

    public synchronized Coupon getAppliedCoupon() {
        return appliedCoupon;
    }

    public synchronized void applyCoupon(Coupon coupon) {
        this.appliedCoupon = coupon;
    }

    public synchronized void removeCoupon() {
        this.appliedCoupon = null;
    }

    public synchronized double getSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : items.values()) {
            subtotal += item.getSubtotal();
        }
        return round(subtotal);
    }

    public synchronized double getShippingFee() {
        if (isEmpty()) return 0.0;
        if (appliedCoupon != null && appliedCoupon.getType() == Coupon.DiscountType.FREE_SHIPPING) {
            return 0.0;
        }
        return getSubtotal() >= FREE_SHIPPING_THRESHOLD ? 0.0 : STANDARD_SHIPPING_FEE;
    }

    public synchronized double getDiscountAmount() {
        if (appliedCoupon == null || isEmpty()) return 0.0;
        double subtotal = getSubtotal();
        double shipping = (appliedCoupon.getType() == Coupon.DiscountType.FREE_SHIPPING) ? STANDARD_SHIPPING_FEE : 0.0;
        return round(appliedCoupon.calculateDiscount(subtotal, shipping));
    }

    public synchronized double getTaxableAmount() {
        double discountedSubtotal = Math.max(0.0, getSubtotal() - getDiscountAmount());
        return round(discountedSubtotal);
    }

    public synchronized double getTax() {
        if (isEmpty()) return 0.0;
        return round(getTaxableAmount() * DEFAULT_TAX_RATE);
    }

    public synchronized double getTotal() {
        if (isEmpty()) return 0.0;
        double total = getTaxableAmount() + getTax() + getShippingFee();
        return round(Math.max(0.0, total));
    }

    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
