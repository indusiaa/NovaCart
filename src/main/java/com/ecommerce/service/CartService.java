package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import java.util.Optional;

/**
 * Service managing customer cart operations, stock validation, and totals.
 */
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final DiscountService discountService;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, DiscountService discountService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.discountService = discountService;
    }

    public static class CartOperationResult {
        private final boolean success;
        private final String message;
        private final Cart cart;

        public CartOperationResult(boolean success, String message, Cart cart) {
            this.success = success;
            this.message = message;
            this.cart = cart;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Cart getCart() {
            return cart;
        }
    }

    public Cart getCart(String sessionId) {
        return cartRepository.getOrCreateCart(sessionId);
    }

    public CartOperationResult addItem(String sessionId, String productId, int quantity) {
        if (quantity <= 0) {
            return new CartOperationResult(false, "Quantity must be greater than zero.", getCart(sessionId));
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new CartOperationResult(false, "Product not found: " + productId, getCart(sessionId));
        }

        Product product = productOpt.get();
        Cart cart = getCart(sessionId);
        CartItem existingItem = cart.getItem(productId);
        int currentInCart = existingItem != null ? existingItem.getQuantity() : 0;
        int requestedTotal = currentInCart + quantity;

        if (requestedTotal > product.getStockQuantity()) {
            return new CartOperationResult(
                false, 
                String.format("Cannot add %d. Only %d units of '%s' in stock (%d already in your cart).", 
                    quantity, product.getStockQuantity(), product.getName(), currentInCart),
                cart
            );
        }

        cart.addItem(product, quantity);
        cartRepository.save(cart);
        return new CartOperationResult(
            true, 
            String.format("Added %d x '%s' to cart.", quantity, product.getName()), 
            cart
        );
    }

    public CartOperationResult updateQuantity(String sessionId, String productId, int quantity) {
        Cart cart = getCart(sessionId);
        if (quantity <= 0) {
            cart.removeItem(productId);
            cartRepository.save(cart);
            return new CartOperationResult(true, "Item removed from cart.", cart);
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new CartOperationResult(false, "Product not found.", cart);
        }

        Product product = productOpt.get();
        if (quantity > product.getStockQuantity()) {
            return new CartOperationResult(
                false, 
                String.format("Cannot set quantity to %d. Only %d available in stock.", quantity, product.getStockQuantity()), 
                cart
            );
        }

        cart.updateQuantity(productId, quantity);
        cartRepository.save(cart);
        return new CartOperationResult(true, "Cart updated.", cart);
    }

    public CartOperationResult removeItem(String sessionId, String productId) {
        Cart cart = getCart(sessionId);
        boolean removed = cart.removeItem(productId);
        cartRepository.save(cart);
        return new CartOperationResult(removed, removed ? "Item removed from cart." : "Item not in cart.", cart);
    }

    public CartOperationResult clearCart(String sessionId) {
        Cart cart = getCart(sessionId);
        cart.clear();
        cartRepository.save(cart);
        return new CartOperationResult(true, "Shopping cart cleared.", cart);
    }

    public CartOperationResult applyCoupon(String sessionId, String couponCode) {
        Cart cart = getCart(sessionId);
        if (cart.isEmpty()) {
            return new CartOperationResult(false, "Cart is empty. Add items before applying coupons.", cart);
        }

        DiscountService.CouponValidationResult validation = discountService.validateCoupon(couponCode, cart.getSubtotal());
        if (!validation.isValid()) {
            return new CartOperationResult(false, validation.getMessage(), cart);
        }

        cart.applyCoupon(validation.getCoupon());
        cartRepository.save(cart);
        return new CartOperationResult(
            true, 
            String.format("Coupon '%s' applied! Discount: $%.2f", validation.getCoupon().getCode(), cart.getDiscountAmount()), 
            cart
        );
    }

    public CartOperationResult removeCoupon(String sessionId) {
        Cart cart = getCart(sessionId);
        cart.removeCoupon();
        cartRepository.save(cart);
        return new CartOperationResult(true, "Coupon removed from cart.", cart);
    }
}
