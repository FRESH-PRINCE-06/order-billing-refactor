package com.uet.legacy.notification;

import java.util.Map;

public class EmailNotification implements NotificationService {
    @Override
    public void send(Map<String, Object> orderData, double amount) {
        String email = (String) orderData.get("customerEmail");
        System.out.println("[EMAIL DISPATCHED] To: " + email + " | Your order of PKR "
                + String.format("%.2f", amount) + " is confirmed!");
    }
}