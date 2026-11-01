package com.ecommerce.controller;

import com.ecommerce.util.HttpHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller serving static front-end assets (HTML, CSS, JavaScript).
 */
public class StaticContentController implements HttpHandler {
    private final Path staticDir;

    public StaticContentController(String staticPath) {
        this.staticDir = Paths.get(staticPath).toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path == null || path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        // Prevent directory traversal attacks
        Path targetFile = staticDir.resolve(path.substring(1)).normalize();
        if (!targetFile.startsWith(staticDir)) {
            HttpHelper.sendErrorResponse(exchange, 403, "Forbidden");
            return;
        }

        File file = targetFile.toFile();
        if (!file.exists() || !file.isFile()) {
            HttpHelper.sendErrorResponse(exchange, 404, "File Not Found: " + path);
            return;
        }

        String mimeType = getMimeType(file.getName());
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        HttpHelper.enableCors(exchange);
        exchange.sendResponseHeaders(200, file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buf = new byte[4096];
            int read;
            while ((read = fis.read(buf)) != -1) {
                os.write(buf, 0, read);
            }
        }
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}
