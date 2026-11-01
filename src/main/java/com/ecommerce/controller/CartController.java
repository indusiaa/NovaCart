package com.ecommerce.controller;

import com.ecommerce.model.Cart;
import com.ecommerce.service.CartService;
import com.ecommerce.util.HttpHelper;
import com.ecommerce.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

/**
 * REST controller for shopping cart operations, item quantities, and coupons.
 */
public class CartController implements HttpHandler {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();
        String sessionId = HttpHelper.getSessionId(exchange);

        try {
            if ("/api/cart".equals(path) && "GET".equals(method)) {
                Cart cart = cartService.getCart(sessionId);
                HttpHelper.sendJsonResponse(exchange, 200, cart);
            } 
            else if (path.startsWith("/api/cart/items")) {
                handleCartItems(exchange, method, sessionId);
            } 
            else if (path.startsWith("/api/cart/coupon")) {
                handleCoupon(exchange, method, sessionId);
            } 
            else if (path.startsWith("/api/cart/clear") && "POST".equals(method)) {
                CartService.CartOperationResult res = cartService.clearCart(sessionId);
                HttpHelper.sendJsonResponse(exchange, 200, res.getCart());
            } 
            else {
                HttpHelper.sendErrorResponse(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (Exception e) {
            HttpHelper.sendErrorResponse(exchange, 500, "Cart Error: " + e.getMessage());
        }
    }

    private void handleCartItems(HttpExchange exchange, String method, String sessionId) throws IOException {
        if ("POST".equals(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseObject(body);
            String productId = (String) req.get("productId");
            int quantity = req.containsKey("quantity") ? ((Number) req.get("quantity")).intValue() : 1;

            if (productId == null || productId.trim().isEmpty()) {
                HttpHelper.sendErrorResponse(exchange, 400, "productId is required");
                return;
            }

            CartService.CartOperationResult res = cartService.addItem(sessionId, productId, quantity);
            if (res.isSuccess()) {
                HttpHelper.sendJsonResponse(exchange, 200, res.getCart());
            } else {
                HttpHelper.sendErrorResponse(exchange, 400, res.getMessage());
            }
        } 
        else if ("PUT".equals(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseObject(body);
            String productId = (String) req.get("productId");
            int quantity = req.containsKey("quantity") ? ((Number) req.get("quantity")).intValue() : 1;

            if (productId == null) {
                HttpHelper.sendErrorResponse(exchange, 400, "productId is required");
                return;
            }

            CartService.CartOperationResult res = cartService.updateQuantity(sessionId, productId, quantity);
            if (res.isSuccess()) {
                HttpHelper.sendJsonResponse(exchange, 200, res.getCart());
            } else {
                HttpHelper.sendErrorResponse(exchange, 400, res.getMessage());
            }
        } 
        else if ("DELETE".equals(method)) {
            Map<String, String> params = HttpHelper.parseQueryParams(exchange.getRequestURI());
            String productId = params.get("productId");
            if (productId == null || productId.trim().isEmpty()) {
                String body = HttpHelper.readRequestBody(exchange);
                if (!body.isEmpty()) {
                    Map<String, Object> req = JsonUtil.parseObject(body);
                    productId = (String) req.get("productId");
                }
            }

            if (productId == null) {
                HttpHelper.sendErrorResponse(exchange, 400, "productId is required");
                return;
            }

            CartService.CartOperationResult res = cartService.removeItem(sessionId, productId);
            HttpHelper.sendJsonResponse(exchange, 200, res.getCart());
        } 
        else {
            HttpHelper.sendErrorResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleCoupon(HttpExchange exchange, String method, String sessionId) throws IOException {
        if ("POST".equals(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            Map<String, Object> req = JsonUtil.parseObject(body);
            String code = (String) req.get("code");
            if (code == null || code.trim().isEmpty()) {
                HttpHelper.sendErrorResponse(exchange, 400, "Coupon code is required");
                return;
            }

            CartService.CartOperationResult res = cartService.applyCoupon(sessionId, code);
            if (res.isSuccess()) {
                HttpHelper.sendJsonResponse(exchange, 200, res.getCart());
            } else {
                HttpHelper.sendErrorResponse(exchange, 400, res.getMessage());
            }
        } 
        else if ("DELETE".equals(method)) {
            CartService.CartOperationResult res = cartService.removeCoupon(sessionId);
            HttpHelper.sendJsonResponse(exchange, 200, res.getCart());
        } 
        else {
            HttpHelper.sendErrorResponse(exchange, 405, "Method Not Allowed");
        }
    }
}
