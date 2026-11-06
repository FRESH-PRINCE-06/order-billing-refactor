package com.uet.legacy.notification;

import java.util.Map;

public interface NotificationService {
    void send(Map<String, Object> orderData, double amount);
}