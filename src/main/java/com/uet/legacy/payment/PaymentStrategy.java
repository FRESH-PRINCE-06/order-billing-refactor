package com.uet.legacy.payment;

import java.util.Map;

public interface PaymentStrategy {
    /**
     * Charges the customer.
     * @return the payment status, e.g. "PAID" or "PENDING_CASH"
     * @throws PaymentException if the payment is rejected
     */
    String pay(Map<String, Object> orderData, double amount) throws PaymentException;
}