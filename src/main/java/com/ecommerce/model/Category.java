package com.ecommerce.model;

/**
 * Product category enumeration.
 */
public enum Category {
    ELECTRONICS("Electronics"),
    AUDIO("Audio & Headphones"),
    WEARABLES("Wearables & Smartwatches"),
    ACCESSORIES("Accessories"),
    HOME_OFFICE("Home Office"),
    GAMING("Gaming");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Category fromString(String text) {
        if (text == null) return null;
        for (Category c : Category.values()) {
            if (c.name().equalsIgnoreCase(text.trim()) || 
                c.displayName.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        return null;
    }
}
