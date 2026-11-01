package com.ecommerce.ai;

import java.util.HashMap;
import java.util.Map;

/**
 * Result of intent classification and entity slot extraction.
 */
public class IntentResult {
    private final IntentType intent;
    private final double confidence;
    private final String rawQuery;
    private final Map<String, Object> entities;

    public IntentResult(IntentType intent, double confidence, String rawQuery) {
        this.intent = intent;
        this.confidence = confidence;
        this.rawQuery = rawQuery;
        this.entities = new HashMap<>();
    }

    public IntentType getIntent() {
        return intent;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public Map<String, Object> getEntities() {
        return entities;
    }

    public IntentResult putEntity(String key, Object value) {
        if (value != null) {
            this.entities.put(key, value);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getEntity(String key) {
        return (T) entities.get(key);
    }

    public String getStringEntity(String key) {
        Object val = entities.get(key);
        return val != null ? val.toString() : null;
    }

    public int getIntEntity(String key, int defaultValue) {
        Object val = entities.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public Double getDoubleEntity(String key) {
        Object val = entities.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            try {
                return Double.parseDouble((String) val);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("IntentResult{intent=%s, conf=%.2f, entities=%s}", intent, confidence, entities);
    }
}
