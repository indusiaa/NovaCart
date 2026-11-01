package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service orchestrating the transactional multi-step checkout workflow.
 */
public class CheckoutService {
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public CheckoutService(CartService cartService, ProductRepository productRepository, 
                           OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    public static class CheckoutRequest {
        public String customerName;
        public String customerEmail;
        public String customerPhone;
        public String shippingAddress;
        public PaymentMethod paymentMethod;
        public String paymentDetails;

        public CheckoutRequest(String customerName, String customerEmail, String customerPhone, 
                               String shippingAddress, PaymentMethod paymentMethod, String paymentDetails) {
            this.customerName = customerName;
            this.customerEmail = customerEmail;
            this.customerPhone = customerPhone;
            this.shippingAddress = shippingAddress;
            this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.CREDIT_CARD;
            this.paymentDetails = paymentDetails;
        }
    }

    public static class CheckoutResult {
        private final boolean success;
        private final String message;
        private final Order order;

        public CheckoutResult(boolean success, String message, Order order) {
            this.success = success;
            this.message = message;
            this.order = order;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Order getOrder() {
            return order;
        }
    }

    public synchronized CheckoutResult processCheckout(String sessionId, CheckoutRequest request) {
        // 1. Validate customer input
        if (request == null) {
            return new CheckoutResult(false, "Checkout request is missing.", null);
        }
        if (request.customerName == null || request.customerName.trim().isEmpty()) {
            return new CheckoutResult(false, "Customer name is required.", null);
        }
        if (request.customerEmail == null || !request.customerEmail.contains("@")) {
            return new CheckoutResult(false, "A valid email address is required.", null);
        }
        if (request.shippingAddress == null || request.shippingAddress.trim().isEmpty()) {
            return new CheckoutResult(false, "Shipping address is required.", null);
        }

        // 2. Fetch cart
        Cart cart = cartService.getCart(sessionId);
        if (cart.isEmpty()) {
            return new CheckoutResult(false, "Cannot checkout an empty shopping cart.", null);
        }

        // 3. Verify stock availability for all items before any deductions
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.isInStock(item.getQuantity())) {
                return new CheckoutResult(
                    false, 
                    String.format("Insufficient stock for '%s'. Available: %d, Requested: %d.", 
                        product.getName(), product.getStockQuantity(), item.getQuantity()),
                    null
                );
            }
        }

        double totalAmount = cart.getTotal();

        // 4. Process Payment
        PaymentGateway.PaymentResult paymentResult = paymentGateway.processPayment(
            request.paymentMethod, 
            totalAmount, 
            request.paymentDetails
        );

        if (!paymentResult.isSuccess()) {
            return new CheckoutResult(false, "Payment Failed: " + paymentResult.getMessage(), null);
        }

        // 5. Deduct inventory
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            productRepository.deductStock(item.getProduct().getId(), item.getQuantity());
            orderItems.add(new OrderItem(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity()
            ));
        }

        // 6. Create Order Entity
        String orderId = "ORD-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Order order = new Order(
            orderId,
            request.customerName.trim(),
            request.customerEmail.trim(),
            request.customerPhone != null ? request.customerPhone.trim() : "",
            request.shippingAddress.trim(),
            orderItems,
            cart.getSubtotal(),
            cart.getDiscountAmount(),
            cart.getTax(),
            cart.getShippingFee(),
            totalAmount,
            request.paymentMethod,
            paymentResult.getTransactionId(),
            OrderStatus.CONFIRMED,
            trackingNumber
        );

        // 7. Persist Order
        orderRepository.save(order);

        // 8. Clear Cart
        cartService.clearCart(sessionId);

        return new CheckoutResult(true, "Order placed successfully! Order ID: " + orderId, order);
    }
}
