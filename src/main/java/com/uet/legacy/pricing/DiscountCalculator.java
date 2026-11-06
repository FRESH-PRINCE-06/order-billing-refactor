package com.uet.legacy.pricing;

public class DiscountCalculator {

    private static final double VIP_THRESHOLD = 5000;
    private static final double VIP_HIGH_RATE = 0.15;
    private static final double VIP_LOW_RATE = 0.10;
    private static final double STUDENT_RATE = 0.08;
    private static final int CORPORATE_BULK_ITEM_COUNT = 10;
    private static final double CORPORATE_BULK_RATE = 0.20;
    private static final double CORPORATE_STANDARD_RATE = 0.05;
    private static final double REGULAR_THRESHOLD = 10000;
    private static final double REGULAR_FLAT_DISCOUNT = 500.0;

    public double calculate(String customerType, double rawTotal, int itemCount) {
        switch (customerType) {
            case "VIP":
                return rawTotal * (rawTotal > VIP_THRESHOLD ? VIP_HIGH_RATE : VIP_LOW_RATE);
            case "STUDENT":
                return rawTotal * STUDENT_RATE;
            case "CORPORATE":
                return rawTotal * (itemCount >= CORPORATE_BULK_ITEM_COUNT
                        ? CORPORATE_BULK_RATE : CORPORATE_STANDARD_RATE);
            default:
                return rawTotal > REGULAR_THRESHOLD ? REGULAR_FLAT_DISCOUNT : 0.0;
        }
    }
}