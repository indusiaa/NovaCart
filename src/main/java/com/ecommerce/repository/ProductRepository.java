package com.ecommerce.repository;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory thread-safe repository for products and inventory management.
 */
public class ProductRepository {
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    public ProductRepository() {
        seedProducts();
    }

    private void seedProducts() {
        save(new Product(
            "PROD-AUDIO-01",
            "Sony WH-1000XM5 Wireless Headphones",
            "Industry-leading noise cancellation with dual processors, 30-hour battery life, and ultra-comfortable lightweight design.",
            349.99,
            Category.AUDIO,
            24,
            4.8,
            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80",
            List.of("audio", "headphones", "bluetooth", "noise-cancelling", "sony")
        ));

        save(new Product(
            "PROD-AUDIO-02",
            "Bose QuietComfort 45 Headphones",
            "Iconic quiet, comfort, and sound. Deep, clear audio with active acoustic noise cancellation.",
            279.00,
            Category.AUDIO,
            18,
            4.7,
            "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500&q=80",
            List.of("audio", "headphones", "bose", "wireless", "travel")
        ));

        save(new Product(
            "PROD-AUDIO-03",
            "Apple AirPods Pro (2nd Gen)",
            "Next-level active noise cancellation, adaptive transparency, and personalized spatial audio.",
            239.00,
            Category.AUDIO,
            45,
            4.9,
            "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=500&q=80",
            List.of("audio", "earbuds", "apple", "wireless", "airpods")
        ));

        save(new Product(
            "PROD-ELEC-01",
            "Apple MacBook Air 15-inch M3",
            "Strikingly thin design with the lightning-fast Apple M3 chip, up to 18 hours of battery, and stunning Liquid Retina display.",
            1249.00,
            Category.ELECTRONICS,
            12,
            4.9,
            "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&q=80",
            List.of("laptop", "macbook", "apple", "computer", "ultrabook", "m3")
        ));

        save(new Product(
            "PROD-ELEC-02",
            "Dell UltraSharp 27\" 4K USB-C Hub Monitor",
            "Brilliant IPS Black technology with 4K resolution, 98% DCI-P3 color gamut, and 90W USB-C power delivery.",
            499.50,
            Category.ELECTRONICS,
            15,
            4.6,
            "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&q=80",
            List.of("monitor", "display", "4k", "dell", "screen", "office")
        ));

        save(new Product(
            "PROD-ELEC-03",
            "Kindle Paperwhite 16GB",
            "6.8-inch glare-free display, adjustable warm light, waterproof design, and weeks of battery life.",
            139.99,
            Category.ELECTRONICS,
            30,
            4.7,
            "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&q=80",
            List.of("reader", "kindle", "books", "amazon", "e-reader")
        ));

        save(new Product(
            "PROD-WEAR-01",
            "Samsung Galaxy Watch 6 Classic",
            "Rotating bezel, sapphire crystal glass, advanced sleep coaching, and comprehensive fitness tracking.",
            329.00,
            Category.WEARABLES,
            20,
            4.6,
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80",
            List.of("watch", "smartwatch", "samsung", "fitness", "wearable")
        ));

        save(new Product(
            "PROD-WEAR-02",
            "Apple Watch Series 9 GPS 45mm",
            "Powerful S9 SiP chip, magical double tap gesture, brighter Always-On display, and advanced health sensors.",
            399.00,
            Category.WEARABLES,
            25,
            4.8,
            "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=500&q=80",
            List.of("watch", "smartwatch", "apple", "fitness", "wearable")
        ));

        save(new Product(
            "PROD-OFF-01",
            "Logitech MX Master 3S Wireless Mouse",
            "Quiet clicks, 8K DPI any-surface sensor, MagSpeed electromagnetic scrolling, and ergonomic thumb rest.",
            99.99,
            Category.HOME_OFFICE,
            50,
            4.9,
            "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&q=80",
            List.of("mouse", "logitech", "bluetooth", "office", "ergonomic", "mx")
        ));

        save(new Product(
            "PROD-OFF-02",
            "Keychron K2 Pro Mechanical Keyboard",
            "QMK/VIA wireless custom mechanical keyboard with hot-swappable switches and RGB backlighting.",
            119.00,
            Category.HOME_OFFICE,
            22,
            4.7,
            "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&q=80",
            List.of("keyboard", "mechanical", "keychron", "wireless", "gaming", "office")
        ));

        save(new Product(
            "PROD-OFF-03",
            "Rode NT-USB Mini Studio Microphone",
            "Compact studio-quality USB microphone with directional cardioid pickup, built-in pop filter, and zero-latency monitoring.",
            99.00,
            Category.HOME_OFFICE,
            16,
            4.8,
            "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&q=80",
            List.of("microphone", "rode", "usb", "streaming", "podcast", "office")
        ));

        save(new Product(
            "PROD-ACC-01",
            "Anker 737 Power Bank 24,000mAh (GaNPrime)",
            "Ultra-powerful two-way charging with 140W max output and smart digital display showing power metrics.",
            109.99,
            Category.ACCESSORIES,
            35,
            4.8,
            "https://images.unsplash.com/photo-1609592424369-07440b8a4f3a?w=500&q=80",
            List.of("powerbank", "anker", "charger", "battery", "usb-c")
        ));

        save(new Product(
            "PROD-GAME-01",
            "Sony PlayStation DualSense Wireless Controller",
            "Immersive haptic feedback, dynamic adaptive triggers, and built-in microphone in an iconic ergonomic design.",
            69.99,
            Category.GAMING,
            40,
            4.8,
            "https://images.unsplash.com/photo-1600080972464-8e5f35f63d08?w=500&q=80",
            List.of("gaming", "playstation", "controller", "sony", "dualsense")
        ));

        save(new Product(
            "PROD-GAME-02",
            "Razer BlackShark V2 Pro Gaming Headset",
            "Wireless esports headset with Razer TriForce Titanium 50mm drivers and HyperClear supercardioid mic.",
            179.99,
            Category.GAMING,
            14,
            4.6,
            "https://images.unsplash.com/photo-1599669454699-248893623440?w=500&q=80",
            List.of("gaming", "headset", "razer", "audio", "esports")
        ));
    }

    public void save(Product product) {
        if (product != null && product.getId() != null) {
            products.put(product.getId(), product);
        }
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public Optional<Product> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(products.get(id));
    }

    public List<Product> findByCategory(Category category) {
        if (category == null) return findAll();
        return products.values().stream()
                .filter(p -> p.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<Product> search(String query, Category category, Double minPrice, Double maxPrice) {
        String cleanQuery = query != null ? query.trim().toLowerCase() : "";
        String[] queryTokens = cleanQuery.isEmpty() ? new String[0] : cleanQuery.split("\\s+");

        class ScoredProduct {
            final Product product;
            final int score;

            ScoredProduct(Product product, int score) {
                this.product = product;
                this.score = score;
            }
        }

        List<ScoredProduct> scoredList = new ArrayList<>();

        for (Product p : products.values()) {
            if (category != null && p.getCategory() != category) {
                continue;
            }
            if (minPrice != null && p.getPrice() < minPrice) {
                continue;
            }
            if (maxPrice != null && p.getPrice() > maxPrice) {
                continue;
            }

            if (queryTokens.length == 0) {
                scoredList.add(new ScoredProduct(p, 1));
                continue;
            }

            String pName = p.getName().toLowerCase();
            String pDesc = p.getDescription().toLowerCase();
            String pCat = p.getCategory().getDisplayName().toLowerCase();
            List<String> pTags = p.getTags();

            int score = 0;
            if (pName.contains(cleanQuery)) {
                score += 100;
            }

            int matchedTokens = 0;
            for (String token : queryTokens) {
                boolean tokenMatched = false;
                if (pName.contains(token)) {
                    score += 25;
                    tokenMatched = true;
                }
                for (String tag : pTags) {
                    if (tag.toLowerCase().contains(token)) {
                        score += 15;
                        tokenMatched = true;
                        break;
                    }
                }
                if (pCat.contains(token)) {
                    score += 10;
                    tokenMatched = true;
                }
                if (pDesc.contains(token)) {
                    score += 5;
                    tokenMatched = true;
                }
                if (tokenMatched) {
                    matchedTokens++;
                }
            }

            if (matchedTokens == queryTokens.length) {
                score += 50; // Bonus for matching all search words
            }

            if (score > 0) {
                scoredList.add(new ScoredProduct(p, score));
            }
        }

        return scoredList.stream()
                .sorted((a, b) -> {
                    if (b.score != a.score) {
                        return Integer.compare(b.score, a.score);
                    }
                    return Double.compare(b.product.getRating(), a.product.getRating());
                })
                .map(sp -> sp.product)
                .collect(Collectors.toList());
    }

    public synchronized boolean deductStock(String productId, int quantity) {
        Product p = products.get(productId);
        if (p == null) return false;
        return p.deductStock(quantity);
    }

    public synchronized void replenishStock(String productId, int quantity) {
        Product p = products.get(productId);
        if (p != null) {
            p.replenishStock(quantity);
        }
    }
}
