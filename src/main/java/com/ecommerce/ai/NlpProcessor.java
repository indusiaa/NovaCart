package com.ecommerce.ai;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Natural Language Processing (NLP) engine for Intent Classification and Entity Slot Extraction.
 * Handles both typed queries and voice transcripts with high specificity.
 */
public class NlpProcessor {

    private static final Map<String, Integer> WORD_TO_NUMBER = new HashMap<>();
    static {
        WORD_TO_NUMBER.put("a", 1);
        WORD_TO_NUMBER.put("an", 1);
        WORD_TO_NUMBER.put("one", 1);
        WORD_TO_NUMBER.put("two", 2);
        WORD_TO_NUMBER.put("three", 3);
        WORD_TO_NUMBER.put("four", 4);
        WORD_TO_NUMBER.put("five", 5);
        WORD_TO_NUMBER.put("six", 6);
        WORD_TO_NUMBER.put("seven", 7);
        WORD_TO_NUMBER.put("eight", 8);
        WORD_TO_NUMBER.put("nine", 9);
        WORD_TO_NUMBER.put("ten", 10);
        WORD_TO_NUMBER.put("couple", 2);
        WORD_TO_NUMBER.put("pair", 2);
    }

    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:under|below|less\\s+than|up\\s+to|within)\\s*\\$?\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile(
        "\\b(ORD-[A-Za-z0-9-]+)\\b", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern COUPON_PATTERN = Pattern.compile(
        "(?:coupon|code|promo|voucher)\\s+([A-Za-z0-9_]{4,15})\\b", Pattern.CASE_INSENSITIVE
    );

    public IntentResult parse(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return new IntentResult(IntentType.UNKNOWN, 0.0, "");
        }

        String text = rawText.trim().toLowerCase(Locale.ROOT);
        IntentResult result;

        // 1. GREETING (Only if brief conversational greeting)
        if (matchesRegex(text, "^(hi|hello|hey|greetings|good\\s+(morning|afternoon|evening))([\\s!.,]|$)|how\\s+are\\s+you")) {
            return new IntentResult(IntentType.GREETING, 0.95, rawText);
        }

        // 2. ORDER TRACKING
        Matcher orderMatcher = ORDER_ID_PATTERN.matcher(rawText);
        if (orderMatcher.find()) {
            result = new IntentResult(IntentType.TRACK_ORDER, 0.95, rawText);
            result.putEntity("orderId", orderMatcher.group(1).toUpperCase());
            return result;
        }
        if (matchesAny(text, "track order", "order status", "where is my order", "track package")) {
            return new IntentResult(IntentType.TRACK_ORDER, 0.85, rawText);
        }

        // 3. APPLY COUPON / PROMO CODE
        Matcher couponMatcher = COUPON_PATTERN.matcher(rawText);
        if (couponMatcher.find()) {
            result = new IntentResult(IntentType.APPLY_COUPON, 0.95, rawText);
            result.putEntity("couponCode", couponMatcher.group(1).toUpperCase());
            return result;
        }
        if (matchesRegex(text, "^(apply|use)\\s+(coupon|promo|code|voucher)\\s+([A-Za-z0-9_]+)")) {
            Pattern p = Pattern.compile("^(?:apply|use)\\s+(?:coupon|promo|code|voucher)\\s+([A-Za-z0-9_]+)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                result = new IntentResult(IntentType.APPLY_COUPON, 0.95, rawText);
                result.putEntity("couponCode", m.group(1).toUpperCase());
                return result;
            }
        }

        // 4. CLEAR / EMPTY CART (Explicit clear command)
        if (matchesRegex(text, "\\b(clear|empty|delete\\s+all)\\s+(?:my\\s+|the\\s+)?cart\\b")) {
            return new IntentResult(IntentType.CLEAR_CART, 0.95, rawText);
        }

        // 5. REMOVE FROM CART (Before Add and View)
        if (matchesRegex(text, "^(remove|delete|drop)\\s+") || matchesRegex(text, "\\b(remove|delete|drop)\\s+.+\\s+(from|out\\s+of)\\s+(?:my\\s+|the\\s+)?cart\\b")) {
            result = new IntentResult(IntentType.REMOVE_FROM_CART, 0.95, rawText);
            String item = text.replaceAll("(?i)^(remove|delete|drop)\\s+", "")
                              .replaceAll("(?i)\\s+(from|out\\s+of)\\s+(?:my\\s+|the\\s+)?cart.*$", "")
                              .replaceAll("(?i)^(the|a|an)\\s+", "")
                              .trim();
            result.putEntity("productKeyword", item);
            return result;
        }

        // 6. ADD TO CART (Specific add patterns)
        if (matchesRegex(text, "^(add|put|buy)\\s+") || matchesRegex(text, "\\b(add|put)\\s+.+\\s+(to|into|in)\\s+(?:my\\s+|the\\s+)?cart\\b")) {
            result = new IntentResult(IntentType.ADD_TO_CART, 0.95, rawText);
            extractAddProductAndQuantity(text, result);
            return result;
        }

        // 7. CHECKOUT / PLACE ORDER
        if (matchesRegex(text, "\\b(checkout|proceed\\s+to\\s+checkout|place\\s+order|pay\\s+now|ready\\s+to\\s+pay|i\\s+want\\s+to\\s+pay)\\b")) {
            return new IntentResult(IntentType.CHECKOUT, 0.95, rawText);
        }

        // 8. VIEW CART (Strictly inquiry about cart state)
        if (matchesRegex(text, "^(?:my\\s+)?cart$") || 
            matchesRegex(text, "\\b(view|show|check|open|see|what\\s+is\\s+in|what's\\s+in|whats\\s+in|read|display)\\s+(?:my\\s+|the\\s+)?cart\\b") ||
            matchesRegex(text, "\\bcart\\s+total\\b")) {
            return new IntentResult(IntentType.VIEW_CART, 0.95, rawText);
        }

        // 9. RECOMMENDATION
        if (matchesAny(text, "recommend", "suggestion", "what should i buy", "what do you suggest", "what's popular", "what is trending", "best ", "top rated")) {
            result = new IntentResult(IntentType.RECOMMEND_PRODUCTS, 0.90, rawText);
            extractCategoryAndPrice(text, result);
            return result;
        }

        // 10. FAQ / SUPPORT
        if (matchesAny(text, "return policy", "refund", "shipping policy", "how long does shipping take", "customer service", "support", "warranty")) {
            result = new IntentResult(IntentType.FAQ_SUPPORT, 0.90, rawText);
            result.putEntity("topic", extractFaqTopic(text));
            return result;
        }

        // 11. SEARCH PRODUCTS
        if (matchesRegex(text, "^(search|find|show|look\\s+for|browse|do\\s+you\\s+have)\\b")) {
            result = new IntentResult(IntentType.SEARCH_PRODUCTS, 0.88, rawText);
            String query = text.replaceAll("(?i)^(search for|search|find me|find|show me|show|look for|looking for|browse|do you have)\\s+", "")
                               .replaceAll("(?i)^(a|an|the|any)\\s+", "")
                               .trim();
            extractCategoryAndPrice(text, result);
            query = query.replaceAll("(?i)(?:under|below|less than|up to)\\s*\\$?\\s*\\d+(?:\\.\\d+)?", "").trim();
            result.putEntity("productKeyword", query);
            return result;
        }

        // DEFAULT: Fallback to product search
        result = new IntentResult(IntentType.SEARCH_PRODUCTS, 0.50, rawText);
        extractCategoryAndPrice(text, result);
        result.putEntity("productKeyword", text);
        return result;
    }

    private void extractAddProductAndQuantity(String text, IntentResult result) {
        int qty = 1;

        // Try extracting numeric digit first: "add 2 bose headphones to cart"
        Pattern digitPattern = Pattern.compile("(?:add|put|buy)\\s+(\\d+)\\s+(?:units?\\s+of\\s+|pieces?\\s+of\\s+)?(.+)", Pattern.CASE_INSENSITIVE);
        Matcher m = digitPattern.matcher(text);
        if (m.find()) {
            try {
                qty = Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
            String prod = cleanProductString(m.group(2));
            result.putEntity("quantity", qty);
            result.putEntity("productKeyword", prod);
            return;
        }

        // Try extracting word number (e.g. "two headphones", "a laptop")
        for (Map.Entry<String, Integer> entry : WORD_TO_NUMBER.entrySet()) {
            Pattern wordPattern = Pattern.compile("(?:add|put|buy)\\s+" + entry.getKey() + "\\s+(?:units?\\s+of\\s+)?(.+)", Pattern.CASE_INSENSITIVE);
            Matcher wm = wordPattern.matcher(text);
            if (wm.find()) {
                qty = entry.getValue();
                String prod = cleanProductString(wm.group(1));
                result.putEntity("quantity", qty);
                result.putEntity("productKeyword", prod);
                return;
            }
        }

        // Default single item extraction
        String prod = text.replaceAll("(?i)^(add|put|buy)\\s+", "");
        prod = cleanProductString(prod);
        result.putEntity("quantity", 1);
        result.putEntity("productKeyword", prod);
    }

    private String cleanProductString(String text) {
        return text.replaceAll("(?i)\\s+(in|into|to)\\s+(?:my\\s+|the\\s+)?cart.*$", "")
                   .replaceAll("(?i)^(the|a|an)\\s+", "")
                   .replaceAll("(?i)\\s+please\\s*$", "")
                   .trim();
    }

    private void extractCategoryAndPrice(String text, IntentResult result) {
        Matcher priceMatcher = PRICE_PATTERN.matcher(text);
        if (priceMatcher.find()) {
            try {
                double maxPrice = Double.parseDouble(priceMatcher.group(1));
                result.putEntity("maxPrice", maxPrice);
            } catch (NumberFormatException ignored) {}
        }

        if (text.contains("audio") || text.contains("headphone") || text.contains("earbud") || text.contains("music")) {
            result.putEntity("category", "AUDIO");
        } else if (text.contains("electronics") || text.contains("laptop") || text.contains("macbook") || text.contains("monitor")) {
            result.putEntity("category", "ELECTRONICS");
        } else if (text.contains("wearable") || text.contains("watch") || text.contains("smartwatch")) {
            result.putEntity("category", "WEARABLES");
        } else if (text.contains("office") || text.contains("mouse") || text.contains("keyboard") || text.contains("mic")) {
            result.putEntity("category", "HOME_OFFICE");
        } else if (text.contains("gaming") || text.contains("game") || text.contains("controller") || text.contains("playstation")) {
            result.putEntity("category", "GAMING");
        } else if (text.contains("accessory") || text.contains("accessories") || text.contains("charger") || text.contains("power bank")) {
            result.putEntity("category", "ACCESSORIES");
        }
    }

    private String extractFaqTopic(String text) {
        if (text.contains("return") || text.contains("refund")) return "RETURNS";
        if (text.contains("shipping") || text.contains("delivery")) return "SHIPPING";
        if (text.contains("warranty")) return "WARRANTY";
        return "GENERAL";
    }

    private boolean matchesRegex(String text, String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    private boolean matchesAny(String text, String... patterns) {
        for (String p : patterns) {
            if (text.contains(p.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
