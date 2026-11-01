package com.ecommerce.ai;

import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.CartService;
import com.ecommerce.service.CatalogService;
import java.util.*;

/**
 * Core AI Assistant orchestration service handling conversational dialogue,
 * semantic intent execution, and live e-commerce actions.
 */
public class AiAssistantService {
    private final NlpProcessor nlpProcessor;
    private final CatalogService catalogService;
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final AiRecommendationEngine recommendationEngine;

    public AiAssistantService(CatalogService catalogService, CartService cartService, 
                              OrderRepository orderRepository) {
        this.nlpProcessor = new NlpProcessor();
        this.catalogService = catalogService;
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.recommendationEngine = new AiRecommendationEngine(catalogService);
    }

    public static class AssistantResponse {
        private final String message;
        private final String spokenText;
        private final String action;
        private final Object data;
        private final List<String> suggestions;

        public AssistantResponse(String message, String spokenText, String action, Object data, List<String> suggestions) {
            this.message = message;
            this.spokenText = spokenText != null ? spokenText : message;
            this.action = action;
            this.data = data;
            this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        }

        public String getMessage() {
            return message;
        }

        public String getSpokenText() {
            return spokenText;
        }

        public String getAction() {
            return action;
        }

        public Object getData() {
            return data;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }
    }

    public AssistantResponse processQuery(String sessionId, String queryText, boolean isVoice) {
        if (queryText == null || queryText.trim().isEmpty()) {
            return new AssistantResponse(
                "How can I help you today? You can search products, manage your cart, or ask for recommendations.",
                "How can I help you today?",
                "NONE",
                null,
                List.of("Show Electronics", "What's in my cart?", "Recommend popular items")
            );
        }

        IntentResult intentResult = nlpProcessor.parse(queryText);
        Cart cart = cartService.getCart(sessionId);

        switch (intentResult.getIntent()) {
            case GREETING:
                return handleGreeting(cart);

            case ADD_TO_CART:
                return handleAddToCart(sessionId, intentResult, cart);

            case VIEW_CART:
                return handleViewCart(cart);

            case REMOVE_FROM_CART:
                return handleRemoveFromCart(sessionId, intentResult, cart);

            case CLEAR_CART:
                return handleClearCart(sessionId);

            case APPLY_COUPON:
                return handleApplyCoupon(sessionId, intentResult);

            case CHECKOUT:
                return handleCheckout(cart);

            case RECOMMEND_PRODUCTS:
                return handleRecommendations(intentResult, cart);

            case TRACK_ORDER:
                return handleTrackOrder(intentResult);

            case FAQ_SUPPORT:
                return handleFaq(intentResult);

            case SEARCH_PRODUCTS:
            default:
                return handleSearchProducts(intentResult);
        }
    }

    private AssistantResponse handleGreeting(Cart cart) {
        String msg = "Hello! I'm your AI Shopping Assistant. I can help you search our catalog, manage your shopping cart, find deals, or answer any questions.";
        String spoken = "Hello! I am your AI Shopping Assistant. How can I help you today?";
        List<String> chips = new ArrayList<>(List.of("Show best sellers", "Recommend headphones", "Apply coupon WELCOME10"));
        if (!cart.isEmpty()) {
            chips.add(0, "View my cart (" + cart.getTotalItemCount() + " items)");
        }
        return new AssistantResponse(msg, spoken, "NONE", null, chips);
    }

    private AssistantResponse handleAddToCart(String sessionId, IntentResult intent, Cart cart) {
        String prodKeyword = intent.getStringEntity("productKeyword");
        int quantity = intent.getIntEntity("quantity", 1);

        if (prodKeyword == null || prodKeyword.trim().isEmpty()) {
            return new AssistantResponse(
                "What product would you like to add? For example, say 'Add Sony headphones' or 'Add Apple MacBook'.",
                "Which product would you like to add to your cart?",
                "NONE",
                null,
                List.of("Add Sony headphones", "Add MacBook Air", "Add Logitech Mouse")
            );
        }

        // Search catalog for closest matching product
        List<Product> matches = catalogService.searchProducts(prodKeyword, null, null, null);
        if (matches.isEmpty()) {
            return new AssistantResponse(
                "I couldn't find any products matching '" + prodKeyword + "'. Would you like to browse our catalog?",
                "I couldn't find any products matching " + prodKeyword + ".",
                "SHOW_CATALOG",
                null,
                List.of("Show Electronics", "Show Audio", "Show Gaming Gear")
            );
        }

        Product bestMatch = matches.get(0);
        CartService.CartOperationResult result = cartService.addItem(sessionId, bestMatch.getId(), quantity);

        if (result.isSuccess()) {
            Cart updatedCart = result.getCart();
            String msg = String.format("Added %d x **%s** to your cart ($%.2f each). Your cart subtotal is now **$%.2f** (%d items).", 
                    quantity, bestMatch.getName(), bestMatch.getPrice(), updatedCart.getSubtotal(), updatedCart.getTotalItemCount());
            String spoken = String.format("I've added %d %s to your cart. Your subtotal is now $%.2f. Would you like to checkout or keep shopping?", 
                    quantity, bestMatch.getName(), updatedCart.getSubtotal());
            return new AssistantResponse(msg, spoken, "CART_UPDATED", updatedCart, List.of("View Cart", "Proceed to Checkout", "Recommend accessories"));
        } else {
            return new AssistantResponse(result.getMessage(), result.getMessage(), "ERROR", null, List.of("View Cart", "Browse Products"));
        }
    }

    private AssistantResponse handleViewCart(Cart cart) {
        if (cart.isEmpty()) {
            return new AssistantResponse(
                "Your shopping cart is currently empty. Would you like to check out some trending products?",
                "Your shopping cart is currently empty. Would you like some recommendations?",
                "VIEW_CART",
                cart,
                List.of("Show best sellers", "Show Audio deals", "Browse Laptops")
            );
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("You have **%d items** in your cart:\n", cart.getTotalItemCount()));
        for (CartItem item : cart.getItems()) {
            sb.append(String.format("- %d x %s ($%.2f)\n", item.getQuantity(), item.getProduct().getName(), item.getSubtotal()));
        }
        sb.append(String.format("\n**Subtotal:** $%.2f", cart.getSubtotal()));
        if (cart.getDiscountAmount() > 0) {
            sb.append(String.format("\n**Discount:** -$%.2f (%s)", cart.getDiscountAmount(), cart.getAppliedCoupon().getCode()));
        }
        sb.append(String.format("\n**Estimated Tax (8%%):** $%.2f", cart.getTax()));
        sb.append(String.format("\n**Shipping:** %s", cart.getShippingFee() == 0 ? "FREE" : String.format("$%.2f", cart.getShippingFee())));
        sb.append(String.format("\n**Total:** **$%.2f**", cart.getTotal()));

        String spoken = String.format("You have %d items in your cart with a total of $%.2f. Ready to checkout?", 
                cart.getTotalItemCount(), cart.getTotal());

        return new AssistantResponse(sb.toString(), spoken, "VIEW_CART", cart, List.of("Proceed to Checkout", "Apply Coupon SAVE20", "Clear Cart"));
    }

    private AssistantResponse handleRemoveFromCart(String sessionId, IntentResult intent, Cart cart) {
        String keyword = intent.getStringEntity("productKeyword");
        if (keyword == null || cart.isEmpty()) {
            return new AssistantResponse(
                cart.isEmpty() ? "Your cart is already empty." : "Which item would you like to remove from your cart?",
                cart.isEmpty() ? "Your cart is already empty." : "Which item would you like to remove?",
                "NONE",
                null,
                List.of("View Cart", "Clear Cart")
            );
        }

        // Find item in cart matching keyword
        Optional<CartItem> match = cart.getItems().stream()
                .filter(item -> item.getProduct().getName().toLowerCase().contains(keyword.toLowerCase()) || 
                                item.getProduct().getId().equalsIgnoreCase(keyword))
                .findFirst();

        if (match.isEmpty()) {
            return new AssistantResponse(
                "I couldn't find '" + keyword + "' in your shopping cart.",
                "I couldn't find that item in your cart.",
                "NONE",
                null,
                List.of("View Cart")
            );
        }

        Product prod = match.get().getProduct();
        cartService.removeItem(sessionId, prod.getId());
        Cart updatedCart = cartService.getCart(sessionId);

        String msg = String.format("Removed **%s** from your cart. New total: **$%.2f**.", prod.getName(), updatedCart.getTotal());
        String spoken = String.format("I've removed %s from your cart. Your new total is $%.2f.", prod.getName(), updatedCart.getTotal());

        return new AssistantResponse(msg, spoken, "CART_UPDATED", updatedCart, List.of("View Cart", "Continue Shopping"));
    }

    private AssistantResponse handleClearCart(String sessionId) {
        cartService.clearCart(sessionId);
        return new AssistantResponse(
            "Your shopping cart has been cleared.",
            "Your shopping cart has been cleared.",
            "CART_UPDATED",
            cartService.getCart(sessionId),
            List.of("Browse catalog", "Show recommendations")
        );
    }

    private AssistantResponse handleApplyCoupon(String sessionId, IntentResult intent) {
        String code = intent.getStringEntity("couponCode");
        if (code == null || code.trim().isEmpty()) {
            return new AssistantResponse(
                "Please specify a coupon code. Try **WELCOME10** (10% off) or **SAVE20** ($20 off orders over $100).",
                "Please specify a coupon code. For example, WELCOME10.",
                "NONE",
                null,
                List.of("Apply WELCOME10", "Apply SAVE20", "Apply FREESHIP")
            );
        }

        CartService.CartOperationResult res = cartService.applyCoupon(sessionId, code);
        if (res.isSuccess()) {
            String msg = String.format("%s Your new total is **$%.2f**.", res.getMessage(), res.getCart().getTotal());
            String spoken = String.format("Coupon %s applied! Your new total is $%.2f.", code.toUpperCase(), res.getCart().getTotal());
            return new AssistantResponse(msg, spoken, "CART_UPDATED", res.getCart(), List.of("Proceed to Checkout", "View Cart"));
        } else {
            return new AssistantResponse(res.getMessage(), res.getMessage(), "ERROR", null, List.of("Try WELCOME10", "Try FREESHIP", "View Cart"));
        }
    }

    private AssistantResponse handleCheckout(Cart cart) {
        if (cart.isEmpty()) {
            return new AssistantResponse(
                "Your cart is empty. Add products to your cart before proceeding to checkout.",
                "Your cart is empty. Please add items before checking out.",
                "NONE",
                null,
                List.of("Show Electronics", "Show Audio Deals")
            );
        }

        String msg = String.format("You are ready to checkout **%d items** for a total of **$%.2f**. I've opened the checkout window for you!", 
                cart.getTotalItemCount(), cart.getTotal());
        String spoken = String.format("You have %d items ready for checkout for $%.2f. I've opened the checkout form for you.", 
                cart.getTotalItemCount(), cart.getTotal());

        return new AssistantResponse(msg, spoken, "OPEN_CHECKOUT", cart, List.of("Place Order", "Apply Coupon", "Edit Cart"));
    }

    private AssistantResponse handleRecommendations(IntentResult intent, Cart cart) {
        String catStr = intent.getStringEntity("category");
        Category category = Category.fromString(catStr);
        Double maxPrice = intent.getDoubleEntity("maxPrice");

        List<Product> recs = recommendationEngine.getRecommendations(cart, category, maxPrice);

        StringBuilder sb = new StringBuilder();
        sb.append("Here are our top recommendations for you:\n\n");
        for (Product p : recs) {
            sb.append(String.format("- **%s** - $%.2f (★ %.1f) - %s\n", p.getName(), p.getPrice(), p.getRating(), p.getDescription()));
        }

        String spoken = String.format("I found %d great recommendations for you, including %s. Would you like to add any of these to your cart?", 
                recs.size(), recs.isEmpty() ? "our top items" : recs.get(0).getName());

        return new AssistantResponse(sb.toString(), spoken, "SHOW_RECOMMENDATIONS", recs, 
                recs.stream().map(p -> "Add " + p.getName()).limit(3).toList());
    }

    private AssistantResponse handleTrackOrder(IntentResult intent) {
        String orderId = intent.getStringEntity("orderId");
        if (orderId != null) {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order o = orderOpt.get();
                String msg = String.format("Order **%s** is currently **%s**.\nTracking Number: `%s`\nTotal: $%.2f (%d items)\nShipping To: %s", 
                        o.getOrderId(), o.getStatus().getDisplayStatus(), o.getTrackingNumber(), o.getTotalAmount(), o.getItems().size(), o.getShippingAddress());
                String spoken = String.format("Order %s is %s with tracking number %s.", o.getOrderId(), o.getStatus().getDisplayStatus(), o.getTrackingNumber());
                return new AssistantResponse(msg, spoken, "ORDER_FOUND", o, List.of("Track another order", "Shop products"));
            } else {
                return new AssistantResponse(
                    "I couldn't find an order with ID `" + orderId + "`. Please check the order number.",
                    "I couldn't find that order number.",
                    "NONE",
                    null,
                    List.of("View my orders", "Shop products")
                );
            }
        }

        List<Order> recent = orderRepository.findAll();
        if (recent.isEmpty()) {
            return new AssistantResponse(
                "You haven't placed any orders yet. Once you complete a checkout, you can track it here!",
                "You have no active orders yet.",
                "NONE",
                null,
                List.of("Shop now")
            );
        }

        Order latest = recent.get(0);
        String msg = String.format("Your most recent order **%s** is **%s**.\nTracking: `%s` | Total: $%.2f", 
                latest.getOrderId(), latest.getStatus().getDisplayStatus(), latest.getTrackingNumber(), latest.getTotalAmount());
        String spoken = String.format("Your latest order %s is %s.", latest.getOrderId(), latest.getStatus().getDisplayStatus());
        return new AssistantResponse(msg, spoken, "ORDER_FOUND", latest, List.of("View Order Details", "Shop More"));
    }

    private AssistantResponse handleFaq(IntentResult intent) {
        String topic = intent.getStringEntity("topic");
        if ("RETURNS".equals(topic)) {
            return new AssistantResponse(
                "**Return & Refund Policy:** We offer a 30-day hassle-free return policy on all electronics and accessories in original packaging. Refunds are processed to the original payment method within 3-5 business days.",
                "We offer a 30-day hassle-free return policy on all items in original packaging.",
                "NONE",
                null,
                List.of("Shipping Policy", "Track Order", "Browse Products")
            );
        } else if ("SHIPPING".equals(topic)) {
            return new AssistantResponse(
                "**Shipping Policy:** Free standard shipping on all orders over $50! Standard delivery takes 2-4 business days. Express next-day shipping is available at checkout.",
                "Standard shipping takes 2 to 4 business days, and orders over $50 ship free.",
                "NONE",
                null,
                List.of("Return Policy", "View Cart", "Shop Deals")
            );
        } else if ("WARRANTY".equals(topic)) {
            return new AssistantResponse(
                "**Warranty Coverage:** All products include a standard 1-year manufacturer warranty covering hardware defects. Extended AppleCare and protection plans are supported.",
                "All products include a full 1-year manufacturer warranty.",
                "NONE",
                null,
                List.of("Browse Products", "Contact Support")
            );
        } else {
            return new AssistantResponse(
                "Our store offers 24/7 automated support, free returns within 30 days, free shipping on orders over $50, and secure checkout with Credit Cards, UPI, PayPal, or Cash on Delivery.",
                "We offer 30-day returns, free shipping over $50, and secure multi-payment checkout.",
                "NONE",
                null,
                List.of("Return Policy", "Shipping Policy", "Browse Products")
            );
        }
    }

    private AssistantResponse handleSearchProducts(IntentResult intent) {
        String keyword = intent.getStringEntity("productKeyword");
        String catStr = intent.getStringEntity("category");
        Category cat = Category.fromString(catStr);
        Double maxPrice = intent.getDoubleEntity("maxPrice");

        List<Product> matches = catalogService.searchProducts(keyword, cat, null, maxPrice);

        if (matches.isEmpty()) {
            return new AssistantResponse(
                "I couldn't find any products matching your criteria. Try searching for 'Sony headphones', 'MacBook', or 'wireless mouse'.",
                "I couldn't find any products matching that search. Here are some popular categories.",
                "SHOW_CATALOG",
                null,
                List.of("Show Electronics", "Show Audio", "Show Gaming Gear")
            );
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Found **%d products** matching your search:\n\n", matches.size()));
        for (Product p : matches.stream().limit(5).toList()) {
            sb.append(String.format("- **%s** - $%.2f (Stock: %d) - %s\n", p.getName(), p.getPrice(), p.getStockQuantity(), p.getDescription()));
        }

        String spoken = String.format("I found %d products for you, including %s for $%.2f. Would you like me to add it to your cart?", 
                matches.size(), matches.get(0).getName(), matches.get(0).getPrice());

        return new AssistantResponse(sb.toString(), spoken, "SHOW_PRODUCTS", matches, 
                matches.stream().map(p -> "Add " + p.getName()).limit(3).toList());
    }
}
