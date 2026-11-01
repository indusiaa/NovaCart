package com.ecommerce;

import com.ecommerce.ai.*;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import com.ecommerce.service.*;
import java.util.List;
import java.util.Optional;

/**
 * End-to-end integration and assertion test suite verifying:
 * 1. Product Catalog & Inventory
 * 2. Shopping Cart lifecycle (add, update, coupon, totals, shipping threshold)
 * 3. Checkout workflow (stock validation, payment, order receipt, stock deduction)
 * 4. AI Chatbot NLP intent matching & domain execution
 * 5. Voice Assistant command handling & spoken text synthesis
 */
public class SystemIntegrationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("     RUNNING SYSTEM INTEGRATION & VERIFICATION TESTS           ");
        System.out.println("===============================================================\n");

        testProductCatalog();
        testCartManagementAndCoupons();
        testCheckoutAndInventoryDeduction();
        testAiChatbotNluAndActions();
        testVoiceAssistantPipeline();

        System.out.println("\n===============================================================");
        System.out.println(String.format("TEST RESULTS: %d PASSED, %d FAILED", testsPassed, testsFailed));
        System.out.println("===============================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void testProductCatalog() {
        System.out.println("--- 1. Testing Product Catalog & Inventory ---");
        ProductRepository repo = new ProductRepository();
        CatalogService catalogService = new CatalogService(repo);

        List<Product> all = catalogService.getAllProducts();
        assertTrue(all.size() >= 10, "Catalog contains pre-seeded products (Found: " + all.size() + ")");

        Optional<Product> sony = catalogService.getProductById("PROD-AUDIO-01");
        assertTrue(sony.isPresent() && sony.get().getName().contains("Sony"), "Lookup by ID works for Sony headphones");

        List<Product> audioList = catalogService.getProductsByCategory(Category.AUDIO);
        assertTrue(!audioList.isEmpty(), "Category filter returns audio products");

        List<Product> searchResults = catalogService.searchProducts("macbook", null, null, null);
        assertTrue(!searchResults.isEmpty() && searchResults.get(0).getName().contains("MacBook"), "Keyword search finds MacBook");

        List<Product> priceFiltered = catalogService.searchProducts(null, null, 0.0, 150.0);
        boolean allUnder150 = priceFiltered.stream().allMatch(p -> p.getPrice() <= 150.0);
        assertTrue(allUnder150 && !priceFiltered.isEmpty(), "Price range filtering works correctly");
    }

    private static void testCartManagementAndCoupons() {
        System.out.println("\n--- 2. Testing Cart Management & Coupon Discounts ---");
        ProductRepository prodRepo = new ProductRepository();
        CouponRepository couponRepo = new CouponRepository();
        CartRepository cartRepo = new CartRepository();
        DiscountService discountService = new DiscountService(couponRepo);
        CartService cartService = new CartService(cartRepo, prodRepo, discountService);

        String sessionId = "test-session-001";
        Cart cart = cartService.getCart(sessionId);
        assertTrue(cart.isEmpty(), "Initial cart is empty");

        // Add 2 Sony Headphones ($349.99 each)
        CartService.CartOperationResult addRes = cartService.addItem(sessionId, "PROD-AUDIO-01", 2);
        assertTrue(addRes.isSuccess() && cart.getTotalItemCount() == 2, "Added 2 Sony headphones to cart");
        assertTrue(Math.abs(cart.getSubtotal() - 699.98) < 0.01, "Subtotal calculated correctly: $" + cart.getSubtotal());

        // Free shipping eligibility (over $50)
        assertTrue(cart.getShippingFee() == 0.0, "Free shipping unlocked for subtotal > $50");

        // Apply SAVE20 coupon ($20 off orders > $100)
        CartService.CartOperationResult coupRes = cartService.applyCoupon(sessionId, "SAVE20");
        assertTrue(coupRes.isSuccess(), "Coupon SAVE20 applied successfully");
        assertTrue(Math.abs(cart.getDiscountAmount() - 20.00) < 0.01, "Discount amount is $20.00");

        // Check tax (8% on $679.98)
        double expectedTax = Math.round((679.98 * 0.08) * 100.0) / 100.0;
        assertTrue(Math.abs(cart.getTax() - expectedTax) < 0.01, "Tax computed accurately: $" + cart.getTax());

        // Update quantity
        cartService.updateQuantity(sessionId, "PROD-AUDIO-01", 1);
        assertTrue(cart.getTotalItemCount() == 1, "Quantity updated to 1");

        // Remove item
        cartService.removeItem(sessionId, "PROD-AUDIO-01");
        assertTrue(cart.isEmpty(), "Item removed and cart is empty");
    }

    private static void testCheckoutAndInventoryDeduction() {
        System.out.println("\n--- 3. Testing Checkout & Inventory Deduction ---");
        ProductRepository prodRepo = new ProductRepository();
        CouponRepository couponRepo = new CouponRepository();
        CartRepository cartRepo = new CartRepository();
        OrderRepository orderRepo = new OrderRepository();
        DiscountService discountService = new DiscountService(couponRepo);
        CartService cartService = new CartService(cartRepo, prodRepo, discountService);
        PaymentGateway paymentGateway = new PaymentGateway();
        CheckoutService checkoutService = new CheckoutService(cartService, prodRepo, orderRepo, paymentGateway);

        String sessionId = "checkout-test-session";
        Product mouse = prodRepo.findById("PROD-OFF-01").get();
        int initialStock = mouse.getStockQuantity();

        cartService.addItem(sessionId, "PROD-OFF-01", 2);

        CheckoutService.CheckoutRequest req = new CheckoutService.CheckoutRequest(
            "Jane Doe",
            "jane@example.com",
            "+1-555-0123",
            "123 Innovation Way, Tech City, CA",
            PaymentMethod.CREDIT_CARD,
            "4532-0000-0000-1111"
        );

        CheckoutService.CheckoutResult chkResult = checkoutService.processCheckout(sessionId, req);
        assertTrue(chkResult.isSuccess(), "Checkout succeeded: " + chkResult.getMessage());

        Order order = chkResult.getOrder();
        assertTrue(order != null && order.getOrderId().startsWith("ORD-"), "Order generated with ID: " + order.getOrderId());
        assertTrue(order.getStatus() == OrderStatus.CONFIRMED, "Order status is CONFIRMED");
        assertTrue(order.getItems().size() == 1 && order.getItems().get(0).getQuantity() == 2, "Order items match purchased items");

        // Verify stock deducted
        Product updatedMouse = prodRepo.findById("PROD-OFF-01").get();
        assertTrue(updatedMouse.getStockQuantity() == initialStock - 2, 
                "Inventory deducted from " + initialStock + " to " + updatedMouse.getStockQuantity());

        // Verify cart cleared
        Cart cart = cartService.getCart(sessionId);
        assertTrue(cart.isEmpty(), "Cart cleared after checkout");
    }

    private static void testAiChatbotNluAndActions() {
        System.out.println("\n--- 4. Testing AI Chatbot Natural Language Processor & Actions ---");
        ProductRepository prodRepo = new ProductRepository();
        CartRepository cartRepo = new CartRepository();
        OrderRepository orderRepo = new OrderRepository();
        CouponRepository couponRepo = new CouponRepository();
        CatalogService catalogService = new CatalogService(prodRepo);
        DiscountService discountService = new DiscountService(couponRepo);
        CartService cartService = new CartService(cartRepo, prodRepo, discountService);
        AiAssistantService aiService = new AiAssistantService(catalogService, cartService, orderRepo);

        String sessionId = "ai-test-session";

        // Query: "Add 2 Bose headphones to my cart"
        AiAssistantService.AssistantResponse resp1 = aiService.processQuery(sessionId, "Add 2 Bose headphones to my cart", false);
        assertTrue("CART_UPDATED".equals(resp1.getAction()), "AI correctly classified ADD_TO_CART intent and updated cart");
        assertTrue(cartService.getCart(sessionId).getTotalItemCount() == 2, "Cart contains 2 items added via AI assistant");

        // Query: "What is in my cart?"
        AiAssistantService.AssistantResponse resp2 = aiService.processQuery(sessionId, "What is in my cart?", false);
        assertTrue("VIEW_CART".equals(resp2.getAction()) && resp2.getMessage().contains("Bose"), "AI correctly handles VIEW_CART inquiry");

        // Query: "Apply coupon WELCOME10"
        AiAssistantService.AssistantResponse resp3 = aiService.processQuery(sessionId, "Apply coupon WELCOME10", false);
        assertTrue("CART_UPDATED".equals(resp3.getAction()) && cartService.getCart(sessionId).getAppliedCoupon() != null, 
                "AI applied coupon WELCOME10");

        // Query: "Recommend top items for gaming"
        AiAssistantService.AssistantResponse resp4 = aiService.processQuery(sessionId, "Recommend top items for gaming", false);
        assertTrue("SHOW_RECOMMENDATIONS".equals(resp4.getAction()) || resp4.getMessage().contains("recommendation"), 
                "AI returns contextual recommendations");

        // Query: "What is your return policy?"
        AiAssistantService.AssistantResponse resp5 = aiService.processQuery(sessionId, "What is your return policy?", false);
        assertTrue(resp5.getMessage().toLowerCase().contains("return") && resp5.getMessage().toLowerCase().contains("30-day"), 
                "AI answers customer FAQ correctly");
    }

    private static void testVoiceAssistantPipeline() {
        System.out.println("\n--- 5. Testing Voice Assistant Pipeline ---");
        ProductRepository prodRepo = new ProductRepository();
        CartRepository cartRepo = new CartRepository();
        OrderRepository orderRepo = new OrderRepository();
        CouponRepository couponRepo = new CouponRepository();
        CatalogService catalogService = new CatalogService(prodRepo);
        DiscountService discountService = new DiscountService(couponRepo);
        CartService cartService = new CartService(cartRepo, prodRepo, discountService);
        AiAssistantService aiService = new AiAssistantService(catalogService, cartService, orderRepo);
        VoiceAssistantService voiceService = new VoiceAssistantService(aiService);

        String sessionId = "voice-test-session";

        // Vocal command: "Hey assistant, add a wireless mouse to my cart please"
        VoiceAssistantService.VoiceResponse voiceResp = voiceService.processVoiceCommand(
            sessionId, "Hey assistant, please add a Logitech mouse to my cart"
        );

        assertTrue("CART_UPDATED".equals(voiceResp.getAction()), "Voice pipeline executed cart addition");
        assertTrue(voiceResp.getSpokenResponse() != null && !voiceResp.getSpokenResponse().isEmpty(), 
                "Generated spoken response: \"" + voiceResp.getSpokenResponse() + "\"");
        assertTrue(!voiceResp.getSpokenResponse().contains("**"), "Spoken response is free of markdown formatting for natural TTS audio");
        assertTrue(cartService.getCart(sessionId).getTotalItemCount() == 1, "Cart contains Logitech mouse from voice command");
    }
}
