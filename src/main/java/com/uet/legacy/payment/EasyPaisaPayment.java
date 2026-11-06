package com.uet.legacy.payment;

import java.util.Map;

public class EasyPaisaPayment implements PaymentStrategy {
    private static final int MOBILE_LENGTH = 11;

    @Override
    public String pay(Map<String, Object> orderData, double amount) throws PaymentException {
        String mobileNo = (String) orderData.getOrDefault("mobileNumber", "");
        if (mobileNo.length() != MOBILE_LENGTH || !mobileNo.startsWith("03")) {
            throw new PaymentException("EasyPaisa failed: Invalid mobile number.");
        }
        System.out.println("Sending EasyPaisa OTP push to " + mobileNo + " for PKR " + amount);
        return "PAID";
    }
}