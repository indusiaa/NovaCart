package com.ecommerce.ai;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.service.CatalogService;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Intelligent recommendation engine utilizing content-based filtering,
 * cross-category affinities, and rating metrics.
 */
public class AiRecommendationEngine {
    private final CatalogService catalogService;

    public AiRecommendationEngine(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public List<Product> getRecommendations(Cart cart, Category preferredCategory, Double maxPrice) {
        List<Product> all = catalogService.getAllProducts();

        // 1. If preferred category specified, prioritize top rated items in that category
        if (preferredCategory != null) {
            return all.stream()
                    .filter(p -> p.getCategory() == preferredCategory)
                    .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                    .sorted(Comparator.comparingDouble(Product::getRating).reversed())
                    .limit(4)
                    .collect(Collectors.toList());
        }

        // 2. Cross-category affinity based on cart contents
        if (cart != null && !cart.isEmpty()) {
            Set<String> cartProductIds = cart.getItems().stream()
                    .map(item -> item.getProduct().getId())
                    .collect(Collectors.toSet());

            Set<Category> cartCategories = cart.getItems().stream()
                    .map(item -> item.getProduct().getCategory())
                    .collect(Collectors.toSet());

            // Determine complementary categories
            Set<Category> complementary = new HashSet<>();
            if (cartCategories.contains(Category.ELECTRONICS)) {
                complementary.add(Category.HOME_OFFICE);
                complementary.add(Category.ACCESSORIES);
            }
            if (cartCategories.contains(Category.AUDIO)) {
                complementary.add(Category.WEARABLES);
                complementary.add(Category.ACCESSORIES);
            }
            if (cartCategories.contains(Category.GAMING)) {
                complementary.add(Category.AUDIO);
                complementary.add(Category.HOME_OFFICE);
            }

            List<Product> recommended = all.stream()
                    .filter(p -> !cartProductIds.contains(p.getId()))
                    .filter(p -> complementary.contains(p.getCategory()))
                    .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                    .sorted(Comparator.comparingDouble(Product::getRating).reversed())
                    .limit(4)
                    .collect(Collectors.toList());

            if (!recommended.isEmpty()) {
                return recommended;
            }
        }

        // 3. Fallback: Top-rated trending items across catalog
        return all.stream()
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .sorted(Comparator.comparingDouble(Product::getRating).reversed())
                .limit(4)
                .collect(Collectors.toList());
    }
}
