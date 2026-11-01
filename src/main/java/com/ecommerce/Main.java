package com.ecommerce;

import com.ecommerce.ai.AiAssistantService;
import com.ecommerce.ai.VoiceAssistantService;
import com.ecommerce.controller.*;
import com.ecommerce.repository.*;
import com.ecommerce.service.*;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main application bootstrap for Java E-Commerce Shopping Cart System
 * with AI Chatbot and Voice Assistant.
 */
public class Main {
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        System.out.println("===============================================================");
        System.out.println("   Starting E-Commerce Shopping Cart System (Java 27 SE)       ");
        System.out.println("===============================================================");

        // 1. Initialize Repositories
        ProductRepository productRepo = new ProductRepository();
        CouponRepository couponRepo = new CouponRepository();
        CartRepository cartRepo = new CartRepository();
        OrderRepository orderRepo = new OrderRepository();

        // 2. Initialize Domain Services
        CatalogService catalogService = new CatalogService(productRepo);
        DiscountService discountService = new DiscountService(couponRepo);
        CartService cartService = new CartService(cartRepo, productRepo, discountService);
        PaymentGateway paymentGateway = new PaymentGateway();
        CheckoutService checkoutService = new CheckoutService(cartService, productRepo, orderRepo, paymentGateway);

        // 3. Initialize AI & Voice Subsystems
        AiAssistantService aiAssistantService = new AiAssistantService(catalogService, cartService, orderRepo);
        VoiceAssistantService voiceAssistantService = new VoiceAssistantService(aiAssistantService);

        // 4. Resolve static files directory
        String staticDir = resolveStaticDirectory();
        System.out.println("[INFO] Static asset directory: " + staticDir);

        // 5. Initialize Controllers
        ProductController productController = new ProductController(catalogService);
        CartController cartController = new CartController(cartService);
        CheckoutController checkoutController = new CheckoutController(checkoutService, orderRepo);
        AiAssistantController aiController = new AiAssistantController(aiAssistantService);
        VoiceAssistantController voiceController = new VoiceAssistantController(voiceAssistantService);
        StaticContentController staticController = new StaticContentController(staticDir);

        // 6. Bootstrap HttpServer
        HttpServer server = startServerWithFallback(port);
        if (server == null) {
            System.err.println("[FATAL] Could not bind server to port " + port + " or fallback ports.");
            System.exit(1);
            return;
        }

        // Register Contexts
        server.createContext("/api/products", productController);
        server.createContext("/api/categories", productController);
        server.createContext("/api/cart", cartController);
        server.createContext("/api/checkout", checkoutController);
        server.createContext("/api/orders", checkoutController);
        server.createContext("/api/ai/chat", aiController);
        server.createContext("/api/ai/voice", voiceController);
        server.createContext("/", staticController);

        // Set virtual thread executor or pooled executor for Java 27
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        int actualPort = server.getAddress().getPort();
        System.out.println("---------------------------------------------------------------");
        System.out.println("   STOREFRONT & AI / VOICE ASSISTANT ONLINE                    ");
        System.out.println("   Local Web UI:       http://localhost:" + actualPort + "/            ");
        System.out.println("   Products API:       http://localhost:" + actualPort + "/api/products");
        System.out.println("   Cart API:           http://localhost:" + actualPort + "/api/cart    ");
        System.out.println("   AI Chat API:        http://localhost:" + actualPort + "/api/ai/chat ");
        System.out.println("   Voice Assistant API:http://localhost:" + actualPort + "/api/ai/voice");
        System.out.println("---------------------------------------------------------------");
        System.out.println("   Press Ctrl+C to stop server.                                ");
        System.out.println("===============================================================");
    }

    private static HttpServer startServerWithFallback(int startPort) {
        for (int p = startPort; p < startPort + 10; p++) {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(p), 0);
                return server;
            } catch (IOException e) {
                System.out.println("[WARN] Port " + p + " is occupied, trying next port...");
            }
        }
        return null;
    }

    private static String resolveStaticDirectory() {
        // Look in standard project locations
        String[] possiblePaths = {
            "src/main/resources/static",
            "../src/main/resources/static",
            "resources/static",
            "static"
        };
        for (String p : possiblePaths) {
            File f = new File(p);
            if (f.exists() && f.isDirectory()) {
                return f.getAbsolutePath();
            }
        }
        return new File("src/main/resources/static").getAbsolutePath();
    }
}
