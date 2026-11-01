package com.ecommerce.repository;

import com.ecommerce.model.Coupon;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for coupons and promotional vouchers.
 */
public class CouponRepository {
    private final Map<String, Coupon> coupons = new ConcurrentHashMap<>();

    public CouponRepository() {
        seedCoupons();
    }

    private void seedCoupons() {
        save(new Coupon("WELCOME10", "10% off your entire order (Min. $20)", 
                Coupon.DiscountType.PERCENTAGE, 10.0, 20.0));
        save(new Coupon("SAVE20", "$20 off orders over $100", 
                Coupon.DiscountType.FIXED_AMOUNT, 20.0, 100.0));
        save(new Coupon("FREESHIP", "Free standard shipping on all orders", 
                Coupon.DiscountType.FREE_SHIPPING, 5.99, 0.0));
        save(new Coupon("TECH50", "$50 instant discount on orders over $300", 
                Coupon.DiscountType.FIXED_AMOUNT, 50.0, 300.0));
        save(new Coupon("GAMER15", "15% off gaming & accessories (Min. $75)", 
                Coupon.DiscountType.PERCENTAGE, 15.0, 75.0));
    }

    public void save(Coupon coupon) {
        if (coupon != null && coupon.getCode() != null) {
            coupons.put(coupon.getCode().toUpperCase(), coupon);
        }
    }

    public Optional<Coupon> findByCode(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(coupons.get(code.trim().toUpperCase()));
    }

    public Map<String, Coupon> findAll() {
        return coupons;
    }
}
