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
import com.uet.legacy.repository.FileOrderRepository;
import com.uet.legacy.repository.OrderRepository;
import com.uet.legacy.validation.InvalidOrderException;
import com.uet.legacy.validation.OrderValidator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Coordinates the order flow. Each step is delegated to a focused class. */
public class OrderBillingEngine {

    private static final String DEFAULT_LOG_FILE = "order_audit.log";

    private final OrderValidator validator = new OrderValidator();
    private final DiscountCalculator discountCalculator = new DiscountCalculator();
    private final TaxCalculator taxCalculator = new TaxCalculator();
    private final Map<String, PaymentStrategy> paymentStrategies = new HashMap<>();
    private final Map<String, NotificationService> notificationServices = new HashMap<>();
    private String logFilePath = DEFAULT_LOG_FILE;
    private OrderRepository repository = new FileOrderRepository(DEFAULT_LOG_FILE);

    public OrderBillingEngine() {
        paymentStrategies.put("CREDIT_CARD", new CreditCardPayment());
        paymentStrategies.put("EASYPAISA", new EasyPaisaPayment());
        paymentStrategies.put("CASH_ON_DELIVERY", new CashOnDeliveryPayment());
        notificationServices.put("EMAIL", new EmailNotification());
        notificationServices.put("SMS", new SmsNotification());
    }

    public boolean processOrder(Map<String, Object> orderData) {
        try {
            validator.validate(orderData);
        } catch (InvalidOrderException e) {
            System.out.println(e.getMessage());
            return false;
        }

        double payableAmount = calculatePayable(orderData);

        String paymentStatus;
        try {
            paymentStatus = findPaymentStrategy(orderData).pay(orderData, payableAmount);
        } catch (PaymentException e) {
            System.out.println(e.getMessage());
            return false;
        }

        repository.save((String) orderData.get("customerEmail"), payableAmount, paymentStatus);
        notifyCustomer(orderData, payableAmount);
        return true;
    }

    private double calculatePayable(Map<String, Object> orderData) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");

        double rawTotal = 0.0;
        for (Map<String, Object> item : items) {
            rawTotal += (double) item.get("price") * (int) item.get("quantity");
        }

        String customerType = (String) orderData.getOrDefault("customerType", "REGULAR");
        String country = (String) orderData.getOrDefault("country", "PK");

        double netAmount = rawTotal - discountCalculator.calculate(customerType, rawTotal, items.size());
        return netAmount + taxCalculator.calculate(netAmount, country);
    }

    private PaymentStrategy findPaymentStrategy(Map<String, Object> orderData) throws PaymentException {
        String method = (String) orderData.getOrDefault("paymentMethod", "CASH");
        PaymentStrategy strategy = paymentStrategies.get(method);
        if (strategy == null) {
            throw new PaymentException("Error: Unsupported payment method: " + method);
        }
        return strategy;
    }

    private void notifyCustomer(Map<String, Object> orderData, double payableAmount) {
        String channel = (String) orderData.getOrDefault("notifyChannel", "EMAIL");
        NotificationService service = notificationServices.get(channel);
        if (service != null) {
            service.send(orderData, payableAmount);
        }
    }

    // ----- extension points (Open/Closed: add behaviour without editing this class) -----
    public void registerPaymentStrategy(String name, PaymentStrategy strategy) {
        paymentStrategies.put(name, strategy);
    }

    public void registerNotificationService(String channel, NotificationService service) {
        notificationServices.put(channel, service);
    }

    // ----- kept so the original tests still work -----
    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
        this.repository = new FileOrderRepository(logFilePath);
    }

    public String getLogFilePath() {
        return logFilePath;
    }
}