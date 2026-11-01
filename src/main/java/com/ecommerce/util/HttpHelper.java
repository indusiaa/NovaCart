package com.ecommerce.util;

import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP exchange helper for JSON REST APIs and CORS headers.
 */
public class HttpHelper {

    public static void enableCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Session-ID");
    }

    public static boolean handlePreflight(HttpExchange exchange) throws IOException {
        enableCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    public static Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, val);
            } else if (!pair.isEmpty()) {
                String key = URLDecoder.decode(pair, StandardCharsets.UTF_8);
                params.put(key, "");
            }
        }
        return params;
    }

    public static String getSessionId(HttpExchange exchange) {
        // 1. Try X-Session-ID header
        String sessionHeader = exchange.getRequestHeaders().getFirst("X-Session-ID");
        if (sessionHeader != null && !sessionHeader.trim().isEmpty()) {
            return sessionHeader.trim();
        }

        // 2. Try query parameter ?sessionId=
        Map<String, String> params = parseQueryParams(exchange.getRequestURI());
        if (params.containsKey("sessionId") && !params.get("sessionId").trim().isEmpty()) {
            return params.get("sessionId").trim();
        }

        // 3. Fallback default guest session
        return "guest-user-session";
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        enableCors(exchange);
        byte[] bytes = JsonUtil.toJson(data).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> err = new HashMap<>();
        err.put("error", true);
        err.put("statusCode", statusCode);
        err.put("message", message);
        sendJsonResponse(exchange, statusCode, err);
    }
}
