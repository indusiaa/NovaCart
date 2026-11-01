package com.ecommerce.controller;

import com.ecommerce.model.Order;
import com.ecommerce.model.PaymentMethod;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.CheckoutService;
import com.ecommerce.util.HttpHelper;
import com.ecommerce.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for checkout transactions, order placement, and tracking.
 */
public class CheckoutController implements HttpHandler {
    private final CheckoutService checkoutService;
    private final OrderRepository orderRepository;

    public CheckoutController(CheckoutService checkoutService, OrderRepository orderRepository) {
        this.checkoutService = checkoutService;
        this.orderRepository = orderRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();
        String sessionId = HttpHelper.getSessionId(exchange);

        try {
            if ("/api/checkout".equals(path) && "POST".equals(method)) {
                handleProcessCheckout(exchange, sessionId);
            } 
            else if (path.startsWith("/api/orders")) {
                handleOrders(exchange, method, path);
            } 
            else {
                HttpHelper.sendErrorResponse(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (Exception e) {
            HttpHelper.sendErrorResponse(exchange, 500, "Checkout Error: " + e.getMessage());
        }
    }

    private void handleProcessCheckout(HttpExchange exchange, String sessionId) throws IOException {
        String body = HttpHelper.readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);

        String name = (String) req.get("customerName");
        String email = (String) req.get("customerEmail");
        String phone = (String) req.get("customerPhone");
        String address = (String) req.get("shippingAddress");
        String paymentMethodStr = (String) req.get("paymentMethod");
        String paymentDetails = (String) req.get("paymentDetails");

        PaymentMethod pm = PaymentMethod.fromString(paymentMethodStr);
        CheckoutService.CheckoutRequest chkReq = new CheckoutService.CheckoutRequest(name, email, phone, address, pm, paymentDetails);

        CheckoutService.CheckoutResult result = checkoutService.processCheckout(sessionId, chkReq);
        if (result.isSuccess()) {
            HttpHelper.sendJsonResponse(exchange, 201, result.getOrder());
        } else {
            HttpHelper.sendErrorResponse(exchange, 400, result.getMessage());
        }
    }

    private void handleOrders(HttpExchange exchange, String method, String path) throws IOException {
        if (!"GET".equals(method)) {
            HttpHelper.sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String prefix = "/api/orders/";
        if (path.startsWith(prefix) && path.length() > prefix.length()) {
            String orderId = path.substring(prefix.length());
            Optional<Order> opt = orderRepository.findById(orderId);
            if (opt.isPresent()) {
                HttpHelper.sendJsonResponse(exchange, 200, opt.get());
            } else {
                HttpHelper.sendErrorResponse(exchange, 404, "Order not found: " + orderId);
            }
            return;
        }

        Map<String, String> params = HttpHelper.parseQueryParams(exchange.getRequestURI());
        String email = params.get("email");
        if (email != null && !email.trim().isEmpty()) {
            HttpHelper.sendJsonResponse(exchange, 200, orderRepository.findByEmail(email));
        } else {
            HttpHelper.sendJsonResponse(exchange, 200, orderRepository.findAll());
        }
    }
}
