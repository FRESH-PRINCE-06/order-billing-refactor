package com.uet.legacy.pricing;

import java.util.Map;

public class TaxCalculator {

    private static final double DEFAULT_RATE = 0.15;
    private static final Map<String, Double> RATES_BY_COUNTRY =
            Map.of("PK", 0.17, "US", 0.08, "UK", 0.20);

    public double calculate(double netAmount, String country) {
        return netAmount * RATES_BY_COUNTRY.getOrDefault(country, DEFAULT_RATE);
    }
}