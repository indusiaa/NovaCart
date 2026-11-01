package com.ecommerce.repository;

import com.ecommerce.model.Order;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe repository for placed orders and fulfillment status.
 */
public class OrderRepository {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public void save(Order order) {
        if (order != null && order.getOrderId() != null) {
            orders.put(order.getOrderId(), order);
        }
    }

    public Optional<Order> findById(String orderId) {
        if (orderId == null) return Optional.empty();
        return Optional.ofNullable(orders.get(orderId.trim().toUpperCase()));
    }

    public List<Order> findAll() {
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Order> findByEmail(String email) {
        if (email == null) return Collections.emptyList();
        String cleanEmail = email.trim().toLowerCase();
        return orders.values().stream()
                .filter(o -> o.getCustomerEmail().toLowerCase().equals(cleanEmail))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}
