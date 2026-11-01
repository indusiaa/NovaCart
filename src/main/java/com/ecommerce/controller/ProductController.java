package com.ecommerce.controller;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.service.CatalogService;
import com.ecommerce.util.HttpHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.*;

/**
 * REST controller for catalog products and categories.
 */
public class ProductController implements HttpHandler {
    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                if (path.endsWith("/categories")) {
                    handleGetCategories(exchange);
                } else {
                    handleGetProducts(exchange, path);
                }
            } else {
                HttpHelper.sendErrorResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            HttpHelper.sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetCategories(HttpExchange exchange) throws IOException {
        List<Map<String, String>> list = new ArrayList<>();
        for (Category cat : catalogService.getAllCategories()) {
            Map<String, String> m = new HashMap<>();
            m.put("name", cat.name());
            m.put("displayName", cat.getDisplayName());
            list.add(m);
        }
        HttpHelper.sendJsonResponse(exchange, 200, list);
    }

    private void handleGetProducts(HttpExchange exchange, String path) throws IOException {
        // Check if single product ID requested: e.g. /api/products/PROD-AUDIO-01
        String prefix = "/api/products/";
        if (path.startsWith(prefix) && path.length() > prefix.length()) {
            String productId = path.substring(prefix.length());
            Optional<Product> opt = catalogService.getProductById(productId);
            if (opt.isPresent()) {
                HttpHelper.sendJsonResponse(exchange, 200, opt.get());
            } else {
                HttpHelper.sendErrorResponse(exchange, 404, "Product not found: " + productId);
            }
            return;
        }

        // Query parameters
        Map<String, String> params = HttpHelper.parseQueryParams(exchange.getRequestURI());
        String query = params.get("query");
        String catParam = params.get("category");
        Category category = Category.fromString(catParam);

        Double minPrice = null;
        if (params.containsKey("minPrice")) {
            try { minPrice = Double.parseDouble(params.get("minPrice")); } catch (Exception ignored) {}
        }

        Double maxPrice = null;
        if (params.containsKey("maxPrice")) {
            try { maxPrice = Double.parseDouble(params.get("maxPrice")); } catch (Exception ignored) {}
        }

        List<Product> products = catalogService.searchProducts(query, category, minPrice, maxPrice);
        HttpHelper.sendJsonResponse(exchange, 200, products);
    }
}
