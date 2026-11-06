package com.uet.legacy.payment;

import java.util.Map;

public class CashOnDeliveryPayment implements PaymentStrategy {
    private static final double MAX_AMOUNT = 25000;

    @Override
    public String pay(Map<String, Object> orderData, double amount) throws PaymentException {
        if (amount > MAX_AMOUNT) {
            throw new PaymentException("Cash on delivery not allowed for orders above PKR 25,000.");
        }
        return "PENDING_CASH";
    }
}