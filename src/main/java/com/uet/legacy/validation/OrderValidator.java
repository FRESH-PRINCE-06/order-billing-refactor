package com.uet.legacy.validation;

import java.util.List;
import java.util.Map;

public class OrderValidator {

    public void validate(Map<String, Object> orderData) throws InvalidOrderException {
        if (orderData == null || orderData.isEmpty()) {
            throw new InvalidOrderException("Error: Order data is empty!");
        }
        String email = (String) orderData.get("customerEmail");
        if (email == null || !email.contains("@")) {
            throw new InvalidOrderException("Error: Invalid customer email!");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Error: Cart has no items!");
        }
        for (Map<String, Object> item : items) {
            validateItem(item);
        }
    }

    private void validateItem(Map<String, Object> item) throws InvalidOrderException {
        if (!item.containsKey("price") || !item.containsKey("quantity")) {
            throw new InvalidOrderException("Error: Missing item price or quantity!");
        }
        double price = (double) item.get("price");
        int quantity = (int) item.get("quantity");
        if (price <= 0 || quantity <= 0) {
            throw new InvalidOrderException("Error: Invalid price or quantity for item!");
        }
    }
}