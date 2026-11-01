package com.ecommerce.util;

import com.ecommerce.ai.AiAssistantService;
import com.ecommerce.ai.VoiceAssistantService;
import com.ecommerce.model.*;
import java.util.*;

/**
 * Lightweight, zero-dependency JSON serializer and parser for Java 27.
 */
public class JsonUtil {

    // ==========================================
    // SERIALIZER
    // ==========================================

    public static String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        serialize(obj, sb);
        return sb.toString();
    }

    private static void serialize(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String) {
            escapeString((String) obj, sb);
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj);
        } else if (obj instanceof Enum<?>) {
            escapeString(((Enum<?>) obj).name(), sb);
        } else if (obj instanceof Product) {
            serializeProduct((Product) obj, sb);
        } else if (obj instanceof Cart) {
            serializeCart((Cart) obj, sb);
        } else if (obj instanceof CartItem) {
            serializeCartItem((CartItem) obj, sb);
        } else if (obj instanceof Coupon) {
            serializeCoupon((Coupon) obj, sb);
        } else if (obj instanceof Order) {
            serializeOrder((Order) obj, sb);
        } else if (obj instanceof OrderItem) {
            serializeOrderItem((OrderItem) obj, sb);
        } else if (obj instanceof AiAssistantService.AssistantResponse) {
            serializeAssistantResponse((AiAssistantService.AssistantResponse) obj, sb);
        } else if (obj instanceof VoiceAssistantService.VoiceResponse) {
            serializeVoiceResponse((VoiceAssistantService.VoiceResponse) obj, sb);
        } else if (obj instanceof Map<?, ?>) {
            serializeMap((Map<?, ?>) obj, sb);
        } else if (obj instanceof Collection<?>) {
            serializeCollection((Collection<?>) obj, sb);
        } else if (obj.getClass().isArray()) {
            serializeCollection(Arrays.asList((Object[]) obj), sb);
        } else {
            escapeString(obj.toString(), sb);
        }
    }

    private static void escapeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    private static void serializeCollection(Collection<?> col, StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (Object item : col) {
            if (!first) sb.append(',');
            serialize(item, sb);
            first = false;
        }
        sb.append(']');
    }

    private static void serializeMap(Map<?, ?> map, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(',');
            escapeString(String.valueOf(entry.getKey()), sb);
            sb.append(':');
            serialize(entry.getValue(), sb);
            first = false;
        }
        sb.append('}');
    }

    private static void serializeProduct(Product p, StringBuilder sb) {
        sb.append('{');
        sb.append("\"id\":"); escapeString(p.getId(), sb); sb.append(',');
        sb.append("\"name\":"); escapeString(p.getName(), sb); sb.append(',');
        sb.append("\"description\":"); escapeString(p.getDescription(), sb); sb.append(',');
        sb.append("\"price\":").append(p.getPrice()).append(',');
        sb.append("\"category\":"); escapeString(p.getCategory().name(), sb); sb.append(',');
        sb.append("\"categoryDisplay\":"); escapeString(p.getCategory().getDisplayName(), sb); sb.append(',');
        sb.append("\"stockQuantity\":").append(p.getStockQuantity()).append(',');
        sb.append("\"rating\":").append(p.getRating()).append(',');
        sb.append("\"imageUrl\":"); escapeString(p.getImageUrl(), sb); sb.append(',');
        sb.append("\"tags\":"); serialize(p.getTags(), sb);
        sb.append('}');
    }

    private static void serializeCartItem(CartItem item, StringBuilder sb) {
        sb.append('{');
        sb.append("\"productId\":"); escapeString(item.getProduct().getId(), sb); sb.append(',');
        sb.append("\"productName\":"); escapeString(item.getProduct().getName(), sb); sb.append(',');
        sb.append("\"price\":").append(item.getProduct().getPrice()).append(',');
        sb.append("\"imageUrl\":"); escapeString(item.getProduct().getImageUrl(), sb); sb.append(',');
        sb.append("\"quantity\":").append(item.getQuantity()).append(',');
        sb.append("\"subtotal\":").append(item.getSubtotal()).append(',');
        sb.append("\"stockQuantity\":").append(item.getProduct().getStockQuantity());
        sb.append('}');
    }

    private static void serializeCart(Cart cart, StringBuilder sb) {
        sb.append('{');
        sb.append("\"cartId\":"); escapeString(cart.getCartId(), sb); sb.append(',');
        sb.append("\"sessionId\":"); escapeString(cart.getSessionId(), sb); sb.append(',');
        sb.append("\"totalItemCount\":").append(cart.getTotalItemCount()).append(',');
        sb.append("\"subtotal\":").append(cart.getSubtotal()).append(',');
        sb.append("\"discountAmount\":").append(cart.getDiscountAmount()).append(',');
        sb.append("\"tax\":").append(cart.getTax()).append(',');
        sb.append("\"shippingFee\":").append(cart.getShippingFee()).append(',');
        sb.append("\"total\":").append(cart.getTotal()).append(',');
        sb.append("\"appliedCoupon\":"); serialize(cart.getAppliedCoupon(), sb); sb.append(',');
        sb.append("\"items\":"); serializeCollection(cart.getItems(), sb);
        sb.append('}');
    }

    private static void serializeCoupon(Coupon c, StringBuilder sb) {
        if (c == null) {
            sb.append("null");
            return;
        }
        sb.append('{');
        sb.append("\"code\":"); escapeString(c.getCode(), sb); sb.append(',');
        sb.append("\"description\":"); escapeString(c.getDescription(), sb); sb.append(',');
        sb.append("\"type\":"); escapeString(c.getType().name(), sb); sb.append(',');
        sb.append("\"value\":").append(c.getValue()).append(',');
        sb.append("\"minimumOrderAmount\":").append(c.getMinimumOrderAmount());
        sb.append('}');
    }

    private static void serializeOrderItem(OrderItem item, StringBuilder sb) {
        sb.append('{');
        sb.append("\"productId\":"); escapeString(item.getProductId(), sb); sb.append(',');
        sb.append("\"productName\":"); escapeString(item.getProductName(), sb); sb.append(',');
        sb.append("\"unitPrice\":").append(item.getUnitPrice()).append(',');
        sb.append("\"quantity\":").append(item.getQuantity()).append(',');
        sb.append("\"totalPrice\":").append(item.getTotalPrice());
        sb.append('}');
    }

    private static void serializeOrder(Order o, StringBuilder sb) {
        sb.append('{');
        sb.append("\"orderId\":"); escapeString(o.getOrderId(), sb); sb.append(',');
        sb.append("\"customerName\":"); escapeString(o.getCustomerName(), sb); sb.append(',');
        sb.append("\"customerEmail\":"); escapeString(o.getCustomerEmail(), sb); sb.append(',');
        sb.append("\"customerPhone\":"); escapeString(o.getCustomerPhone(), sb); sb.append(',');
        sb.append("\"shippingAddress\":"); escapeString(o.getShippingAddress(), sb); sb.append(',');
        sb.append("\"subtotal\":").append(o.getSubtotal()).append(',');
        sb.append("\"discountAmount\":").append(o.getDiscountAmount()).append(',');
        sb.append("\"tax\":").append(o.getTax()).append(',');
        sb.append("\"shippingFee\":").append(o.getShippingFee()).append(',');
        sb.append("\"totalAmount\":").append(o.getTotalAmount()).append(',');
        sb.append("\"paymentMethod\":"); escapeString(o.getPaymentMethod().getLabel(), sb); sb.append(',');
        sb.append("\"transactionId\":"); escapeString(o.getTransactionId(), sb); sb.append(',');
        sb.append("\"status\":"); escapeString(o.getStatus().getDisplayStatus(), sb); sb.append(',');
        sb.append("\"trackingNumber\":"); escapeString(o.getTrackingNumber(), sb); sb.append(',');
        sb.append("\"createdAt\":"); escapeString(o.getCreatedAt().toString(), sb); sb.append(',');
        sb.append("\"items\":"); serializeCollection(o.getItems(), sb);
        sb.append('}');
    }

    private static void serializeAssistantResponse(AiAssistantService.AssistantResponse resp, StringBuilder sb) {
        sb.append('{');
        sb.append("\"message\":"); escapeString(resp.getMessage(), sb); sb.append(',');
        sb.append("\"spokenText\":"); escapeString(resp.getSpokenText(), sb); sb.append(',');
        sb.append("\"action\":"); escapeString(resp.getAction(), sb); sb.append(',');
        sb.append("\"suggestions\":"); serialize(resp.getSuggestions(), sb); sb.append(',');
        sb.append("\"data\":"); serialize(resp.getData(), sb);
        sb.append('}');
    }

    private static void serializeVoiceResponse(VoiceAssistantService.VoiceResponse resp, StringBuilder sb) {
        sb.append('{');
        sb.append("\"recognizedText\":"); escapeString(resp.getRecognizedText(), sb); sb.append(',');
        sb.append("\"spokenResponse\":"); escapeString(resp.getSpokenResponse(), sb); sb.append(',');
        sb.append("\"displayText\":"); escapeString(resp.getDisplayText(), sb); sb.append(',');
        sb.append("\"action\":"); escapeString(resp.getAction(), sb); sb.append(',');
        sb.append("\"speechRate\":").append(resp.getSpeechRate()).append(',');
        sb.append("\"speechPitch\":").append(resp.getSpeechPitch()).append(',');
        sb.append("\"data\":"); serialize(resp.getData(), sb);
        sb.append('}');
    }

    // ==========================================
    // PARSER (Recursive Descent)
    // ==========================================

    public static Object parse(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        return new Parser(json.trim()).parseValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object res = parse(json);
        if (res instanceof Map<?, ?>) {
            return (Map<String, Object>) res;
        }
        return Collections.emptyMap();
    }

    private static class Parser {
        private final String src;
        private int pos = 0;

        public Parser(String src) {
            this.src = src;
        }

        public Object parseValue() {
            skipWhitespace();
            if (pos >= src.length()) return null;

            char c = src.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();

            throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + pos);
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // skip '{'
            skipWhitespace();

            if (pos < src.length() && src.charAt(pos) == '}') {
                pos++;
                return map;
            }

            while (pos < src.length()) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ':') {
                    pos++;
                } else {
                    throw new IllegalArgumentException("Expected ':' at position " + pos);
                }
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ',') {
                    pos++;
                } else if (pos < src.length() && src.charAt(pos) == '}') {
                    pos++;
                    break;
                } else {
                    break;
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // skip '['
            skipWhitespace();

            if (pos < src.length() && src.charAt(pos) == ']') {
                pos++;
                return list;
            }

            while (pos < src.length()) {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ',') {
                    pos++;
                } else if (pos < src.length() && src.charAt(pos) == ']') {
                    pos++;
                    break;
                } else {
                    break;
                }
            }
            return list;
        }

        private String parseString() {
            if (src.charAt(pos) != '"') {
                throw new IllegalArgumentException("Expected '\"' at position " + pos);
            }
            pos++; // skip opening quote
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                } else if (c == '\\') {
                    if (pos >= src.length()) break;
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (pos + 4 <= src.length()) {
                                String hex = src.substring(pos, pos + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                pos += 4;
                            }
                            break;
                        default: sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (src.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Expected boolean at position " + pos);
        }

        private Object parseNull() {
            if (src.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Expected null at position " + pos);
        }

        private Number parseNumber() {
            int start = pos;
            if (src.charAt(pos) == '-') pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                pos++;
            }
            boolean isFloat = false;
            if (pos < src.length() && src.charAt(pos) == '.') {
                isFloat = true;
                pos++;
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                    pos++;
                }
            }
            if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
                isFloat = true;
                pos++;
                if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                    pos++;
                }
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                    pos++;
                }
            }

            String numStr = src.substring(start, pos);
            if (isFloat) {
                return Double.parseDouble(numStr);
            } else {
                long val = Long.parseLong(numStr);
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    return (int) val;
                }
                return val;
            }
        }

        private void skipWhitespace() {
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    pos++;
                } else {
                    break;
                }
            }
        }
    }
}
