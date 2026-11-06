package com.uet.legacy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PaymentAndValidationTest extends BillingTestBase {

    // ---------- CREDIT CARD ----------
    @Test
    void validCreditCardIsPaid() throws IOException {
        assertTrue(engine.processOrder(order(1000, 1)));
        assertTrue(Files.readString(logFile).contains("Status: PAID"));
    }

    @Test
    void cardWith15DigitsIsDeclined() {
        Map<String, Object> o = order(1000, 1);
        o.put("cardNumber", "123456789012345");
        assertFalse(engine.processOrder(o));
        assertTrue(output().contains("Card declined"));
        assertFalse(Files.exists(logFile));                 // nothing saved on failure
    }

    @Test
    void cardWithLettersIsDeclined() {
        Map<String, Object> o = order(1000, 1);
        o.put("cardNumber", "abcdabcdabcdabcd");
        assertFalse(engine.processOrder(o));
    }

    @Test
    void missingCardNumberIsDeclined() {
        Map<String, Object> o = order(1000, 1);
        o.remove("cardNumber");
        assertFalse(engine.processOrder(o));
    }

    // ---------- EASYPAISA ----------
    @Test
    void validEasyPaisaIsPaid() throws IOException {
        Map<String, Object> o = order(1000, 1);
        o.put("paymentMethod", "EASYPAISA");
        o.put("mobileNumber", "03001234567");
        assertTrue(engine.processOrder(o));
        assertTrue(Files.readString(logFile).contains("Status: PAID"));
        assertTrue(output().contains("EasyPaisa OTP"));
    }

    @Test
    void easyPaisaNotStartingWith03IsRejected() {
        Map<String, Object> o = order(1000, 1);
        o.put("paymentMethod", "EASYPAISA");
        o.put("mobileNumber", "04001234567");
        assertFalse(engine.processOrder(o));
    }

    @Test
    void easyPaisaWrongLengthIsRejected() {
        Map<String, Object> o = order(1000, 1);
        o.put("paymentMethod", "EASYPAISA");
        o.put("mobileNumber", "0300123");
        assertFalse(engine.processOrder(o));
    }

    // ---------- CASH ON DELIVERY ----------
    @Test
    void codUnderLimitIsPendingCash() throws IOException {
        Map<String, Object> o = order(1000, 1);
        o.put("paymentMethod", "CASH_ON_DELIVERY");
        assertTrue(engine.processOrder(o));
        assertTrue(Files.readString(logFile).contains("Status: PENDING_CASH"));
    }

    @Test
    void codOver25000IsRejected() {
        Map<String, Object> o = order(25000, 1);           // payable = 28665
        o.put("paymentMethod", "CASH_ON_DELIVERY");
        assertFalse(engine.processOrder(o));
        assertTrue(output().contains("Cash on delivery not allowed"));
    }

    // ---------- UNSUPPORTED METHOD ----------
    @Test
    void unsupportedPaymentMethodIsRejected() {
        Map<String, Object> o = order(1000, 1);
        o.put("paymentMethod", "BITCOIN");
        assertFalse(engine.processOrder(o));
        assertTrue(output().contains("Unsupported payment method"));
    }

    // ---------- INVALID INPUT ----------
    @Test
    void nullOrderIsRejected() {
        assertFalse(engine.processOrder(null));
    }

    @Test
    void emptyOrderIsRejected() {
        assertFalse(engine.processOrder(new HashMap<>()));
        assertTrue(output().contains("Order data is empty"));
    }

    @Test
    void missingEmailIsRejected() {
        Map<String, Object> o = order(1000, 1);
        o.remove("customerEmail");
        assertFalse(engine.processOrder(o));
    }

    @Test
    void emailWithoutAtSignIsRejected() {
        Map<String, Object> o = order(1000, 1);
        o.put("customerEmail", "aliexample.com");
        assertFalse(engine.processOrder(o));
        assertTrue(output().contains("Invalid customer email"));
    }

    @Test
    void emptyCartIsRejected() {
        Map<String, Object> o = order(1000, 1);
        o.put("items", new ArrayList<>());
        assertFalse(engine.processOrder(o));
        assertTrue(output().contains("Cart has no items"));
    }

    @Test
    void missingItemsKeyIsRejected() {
        Map<String, Object> o = order(1000, 1);
        o.remove("items");
        assertFalse(engine.processOrder(o));
    }

    @Test
    void itemMissingQuantityIsRejected() {
        Map<String, Object> o = order(1000, 1);
        Map<String, Object> badItem = new HashMap<>();
        badItem.put("price", 100.0);
        ((java.util.List<Map<String, Object>>) o.get("items")).add(badItem);
        assertFalse(engine.processOrder(o));
        assertTrue(output().contains("Missing item price or quantity"));
    }

    @Test
    void negativePriceIsRejected() {
        assertFalse(engine.processOrder(order(-100, 1)));
        assertTrue(output().contains("Invalid price or quantity"));
    }

    @Test
    void zeroPriceIsRejected() {
        assertFalse(engine.processOrder(order(0, 1)));
    }

    @Test
    void zeroQuantityIsRejected() {
        assertFalse(engine.processOrder(order(100, 0)));
    }

    @Test
    void negativeQuantityIsRejected() {
        assertFalse(engine.processOrder(order(100, -2)));
    }

    // ---------- NOTIFICATIONS & LOGGING ----------
    @Test
    void defaultNotificationIsEmail() {
        assertTrue(engine.processOrder(order(1000, 1)));
        assertTrue(output().contains("[EMAIL DISPATCHED] To: ali@example.com"));
    }

    @Test
    void smsNotificationUsesGivenPhone() {
        Map<String, Object> o = order(1000, 1);
        o.put("notifyChannel", "SMS");
        o.put("phone", "0311-1111111");
        assertTrue(engine.processOrder(o));
        assertTrue(output().contains("[SMS DISPATCHED] To: 0311-1111111"));
    }

    @Test
    void smsNotificationUsesDefaultPhoneWhenMissing() {
        Map<String, Object> o = order(1000, 1);
        o.put("notifyChannel", "SMS");
        assertTrue(engine.processOrder(o));
        assertTrue(output().contains("[SMS DISPATCHED] To: 0300-0000000"));
    }

    @Test
    void unknownNotifyChannelStillSucceedsButSendsNothing() {
        Map<String, Object> o = order(1000, 1);
        o.put("notifyChannel", "PIGEON");
        assertTrue(engine.processOrder(o));
        assertFalse(output().contains("DISPATCHED"));
    }

    @Test
    void successfulOrderIsWrittenToLogWithEmail() throws IOException {
        assertTrue(engine.processOrder(order(1000, 1)));
        assertTrue(Files.readString(logFile).contains("Order for: ali@example.com"));
    }
}