package com.ecommerce.repository;

import com.ecommerce.model.Cart;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe repository managing active customer shopping carts keyed by session ID.
 */
public class CartRepository {
    private final Map<String, Cart> carts = new ConcurrentHashMap<>();

    public Cart getOrCreateCart(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "guest-session-" + UUID.randomUUID().toString().substring(0, 8);
        }
        final String finalSessionId = sessionId;
        return carts.computeIfAbsent(finalSessionId, id -> {
            String cartId = "CART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return new Cart(cartId, id);
        });
    }

    public Optional<Cart> findCart(String sessionId) {
        if (sessionId == null) return Optional.empty();
        return Optional.ofNullable(carts.get(sessionId));
    }

    public void save(Cart cart) {
        if (cart != null && cart.getSessionId() != null) {
            carts.put(cart.getSessionId(), cart);
        }
    }

    public void deleteCart(String sessionId) {
        if (sessionId != null) {
            carts.remove(sessionId);
        }
    }
}
