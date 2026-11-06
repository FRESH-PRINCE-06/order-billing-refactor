package com.uet.legacy.payment;

import java.util.Map;

public class CreditCardPayment implements PaymentStrategy {
    private static final int CARD_LENGTH = 16;

    @Override
    public String pay(Map<String, Object> orderData, double amount) throws PaymentException {
        String cardNumber = (String) orderData.getOrDefault("cardNumber", "");
        if (cardNumber.length() != CARD_LENGTH || !cardNumber.matches("\\d+")) {
            throw new PaymentException("Card declined: Must be 16 digits.");
        }
        System.out.println("Processing credit card payment of PKR " + amount);
        return "PAID";
    }
}