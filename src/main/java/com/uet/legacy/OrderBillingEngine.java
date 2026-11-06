package com.uet.legacy;
import com.uet.legacy.notification.EmailNotification;
import com.uet.legacy.notification.NotificationService;
import com.uet.legacy.notification.SmsNotification;
import com.uet.legacy.payment.CashOnDeliveryPayment;
import com.uet.legacy.payment.CreditCardPayment;
import com.uet.legacy.payment.EasyPaisaPayment;
import com.uet.legacy.payment.PaymentException;
import com.uet.legacy.payment.PaymentStrategy;
import com.uet.legacy.pricing.DiscountCalculator;
import com.uet.legacy.pricing.TaxCalculator;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LEGACY BILLING ENGINE
 * 
 * NOTICE TO STUDENTS:
 * This class works and compiles, but its design is messy, hard to test, and tightly coupled.
 * Do NOT start modifying this file directly. First, write your JUnit 5 safety tests.
 */
public class OrderBillingEngine {

    // Bad Practice 1: Hardcoded file paths and direct file manipulation
    private String logFilePath = "order_audit.log";
        private final Map<String, PaymentStrategy> paymentStrategies = new HashMap<>();
            private final Map<String, NotificationService> notificationServices = new HashMap<>();

    public OrderBillingEngine() {
        paymentStrategies.put("CREDIT_CARD", new CreditCardPayment());
        paymentStrategies.put("EASYPAISA", new EasyPaisaPayment());
        paymentStrategies.put("CASH_ON_DELIVERY", new CashOnDeliveryPayment());
                notificationServices.put("EMAIL", new EmailNotification());
        notificationServices.put("SMS", new SmsNotification());
    }

    public void registerPaymentStrategy(String name, PaymentStrategy strategy) {
        paymentStrategies.put(name, strategy);
    }
        public void registerNotificationService(String channel, NotificationService service) {
        notificationServices.put(channel, service);
    }

    public boolean processOrder(Map<String, Object> orderData) {
        // 1. Input Validation (Messy nested validation and console error printing)
        if (orderData == null || orderData.isEmpty()) {
            System.out.println("Error: Order data is empty!");
            return false;
        }

        String email = (String) orderData.get("customerEmail");
        if (email == null || !email.contains("@")) {
            System.out.println("Error: Invalid customer email!");
            return false;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
        if (items == null || items.isEmpty()) {
            System.out.println("Error: Cart has no items!");
            return false;
        }

        // 2. Total & Discount Calculation (Long method, primitive types, magic numbers)
        double rawTotal = 0.0;
        for (Map<String, Object> item : items) {
            if (!item.containsKey("price") || !item.containsKey("quantity")) {
                System.out.println("Error: Missing item price or quantity!");
                return false;
            }
            double price = (double) item.get("price");
            int quantity = (int) item.get("quantity");
            if (price <= 0 || quantity <= 0) {
                System.out.println("Error: Invalid price or quantity for item!");
                return false;
            }
            rawTotal += price * quantity;
        }
        String customerType = (String) orderData.getOrDefault("customerType", "REGULAR");
        double discount = new DiscountCalculator().calculate(customerType, rawTotal, items.size());
        double netAmount = rawTotal - discount;

        String country = (String) orderData.getOrDefault("country", "PK");
        double tax = new TaxCalculator().calculate(netAmount, country);
        double payableAmount = netAmount + tax;
        

        String paymentMethod = (String) orderData.getOrDefault("paymentMethod", "CASH");
        PaymentStrategy strategy = paymentStrategies.get(paymentMethod);
        if (strategy == null) {
            System.out.println("Error: Unsupported payment method: " + paymentMethod);
            return false;
        }
        String paymentStatus;
        try {
            paymentStatus = strategy.pay(orderData, payableAmount);
        } catch (PaymentException e) {
            System.out.println(e.getMessage());
            return false;
        }

        // 4. Direct File Saving (Side-effect inside business logic)
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(LocalDateTime.now() + " | Order for: " + email + " | Payable: PKR " 
                         + payableAmount + " | Status: " + paymentStatus + "\n");
        } catch (IOException e) {
            System.out.println("Failed to write order log: " + e.getMessage());
        }

           String notifyChannel = (String) orderData.getOrDefault("notifyChannel", "EMAIL");
        NotificationService notifier = notificationServices.get(notifyChannel);
        if (notifier != null) {
            notifier.send(orderData, payableAmount);
        }
        return true;
    }

    // Helper getter/setter for testing file path redirection
    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getLogFilePath() {
        return logFilePath;
    }
}
