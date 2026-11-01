package com.ecommerce.service;

import com.ecommerce.model.Coupon;
import com.ecommerce.repository.CouponRepository;
import java.util.Optional;

/**
 * Service managing coupon validation and discount computation.
 */
public class DiscountService {
    private final CouponRepository couponRepository;

    public DiscountService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public static class CouponValidationResult {
        private final boolean valid;
        private final String message;
        private final Coupon coupon;

        public CouponValidationResult(boolean valid, String message, Coupon coupon) {
            this.valid = valid;
            this.message = message;
            this.coupon = coupon;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public Coupon getCoupon() {
            return coupon;
        }
    }

    public CouponValidationResult validateCoupon(String code, double currentSubtotal) {
        if (code == null || code.trim().isEmpty()) {
            return new CouponValidationResult(false, "Coupon code cannot be empty.", null);
        }

        Optional<Coupon> opt = couponRepository.findByCode(code);
        if (opt.isEmpty()) {
            return new CouponValidationResult(false, "Invalid coupon code: '" + code + "'.", null);
        }

        Coupon coupon = opt.get();
        if (!coupon.isEligible(currentSubtotal)) {
            return new CouponValidationResult(
                false, 
                String.format("Coupon '%s' requires a minimum order of $%.2f (Current: $%.2f).", 
                    coupon.getCode(), coupon.getMinimumOrderAmount(), currentSubtotal),
                null
            );
        }

        return new CouponValidationResult(true, "Coupon '" + coupon.getCode() + "' applied successfully!", coupon);
    }

    public Optional<Coupon> getCoupon(String code) {
        return couponRepository.findByCode(code);
    }
}
