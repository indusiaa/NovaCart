package com.ecommerce.service;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import java.util.List;
import java.util.Optional;

/**
 * Service managing product catalog browsing, search, and inventory checks.
 */
public class CatalogService {
    private final ProductRepository productRepository;

    public CatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String query, Category category, Double minPrice, Double maxPrice) {
        return productRepository.search(query, category, minPrice, maxPrice);
    }

    public boolean isStockAvailable(String productId, int quantity) {
        return productRepository.findById(productId)
                .map(p -> p.isInStock(quantity))
                .orElse(false);
    }

    public Category[] getAllCategories() {
        return Category.values();
    }
}
