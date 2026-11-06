package com.uet.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DiscountAndTaxTest extends BillingTestBase {

    // ---------- REGULAR ----------
    @Test
    void regularSmallOrderHasNoDiscount() throws IOException {
        assertTrue(engine.processOrder(order(1000, 1)));
        assertEquals(1170.0, payableFromLog(), 0.01);      // 1000 + 17% tax
    }

    @Test
    void quantityMultipliesPrice() throws IOException {
        assertTrue(engine.processOrder(order(500, 3)));
        assertEquals(1755.0, payableFromLog(), 0.01);      // 1500 * 1.17
    }

    @Test
    void regularExactly10000GetsNoDiscount() throws IOException {
        assertTrue(engine.processOrder(order(10000, 1)));
        assertEquals(11700.0, payableFromLog(), 0.01);
    }

    @Test
    void regularOver10000GetsFlat500Off() throws IOException {
        assertTrue(engine.processOrder(order(12000, 1)));
        assertEquals(13455.0, payableFromLog(), 0.01);     // (12000-500)*1.17
    }

    @Test
    void unknownCustomerTypeIsTreatedAsRegular() throws IOException {
        Map<String, Object> o = order(1000, 1);
        o.put("customerType", "GOLD");
        assertTrue(engine.processOrder(o));
        assertEquals(1170.0, payableFromLog(), 0.01);
    }

    // ---------- VIP ----------
    @Test
    void vipUnder5000Gets10Percent() throws IOException {
        Map<String, Object> o = order(1000, 1);
        o.put("customerType", "VIP");
        assertTrue(engine.processOrder(o));
        assertEquals(1053.0, payableFromLog(), 0.01);      // 900 * 1.17
    }

    @Test
    void vipExactly5000StillGets10Percent() throws IOException {
        Map<String, Object> o = order(5000, 1);
        o.put("customerType", "VIP");
        assertTrue(engine.processOrder(o));
        assertEquals(5265.0, payableFromLog(), 0.01);      // 4500 * 1.17
    }

    @Test
    void vipOver5000Gets15Percent() throws IOException {
        Map<String, Object> o = order(6000, 1);
        o.put("customerType", "VIP");
        assertTrue(engine.processOrder(o));
        assertEquals(5967.0, payableFromLog(), 0.01);      // 5100 * 1.17
    }

    // ---------- STUDENT ----------
    @Test
    void studentGets8Percent() throws IOException {
        Map<String, Object> o = order(1000, 1);
        o.put("customerType", "STUDENT");
        assertTrue(engine.processOrder(o));
        assertEquals(1076.4, payableFromLog(), 0.01);      // 920 * 1.17
    }

    // ---------- CORPORATE ----------
    @Test
    void corporateWithFewItemsGets5Percent() throws IOException {
        Map<String, Object> o = order(1000, 1);
        o.put("customerType", "CORPORATE");
        assertTrue(engine.processOrder(o));
        assertEquals(1111.5, payableFromLog(), 0.01);      // 950 * 1.17
    }

    @Test
    void corporateWith9ItemsStillGets5Percent() throws IOException {
        Map<String, Object> o = order(100, 1);
        o.put("customerType", "CORPORATE");
        addItems(o, 8, 100);                                // 9 items total
        assertTrue(engine.processOrder(o));
        assertEquals(1000.35, payableFromLog(), 0.01);     // 855 * 1.17
    }

    @Test
    void corporateWith10ItemsGets20Percent() throws IOException {
        Map<String, Object> o = order(100, 1);
        o.put("customerType", "CORPORATE");
        addItems(o, 9, 100);                                // 10 items total
        assertTrue(engine.processOrder(o));
        assertEquals(936.0, payableFromLog(), 0.01);       // 800 * 1.17
    }

    // ---------- TAX BY COUNTRY ----------
    @Test
    void usTaxIs8Percent() throws IOException {
        Map<String, Object> o = order(1000, 1);
        o.put("country", "US");
        assertTrue(engine.processOrder(o));
        assertEquals(1080.0, payableFromLog(), 0.01);
    }

    @Test
    void ukTaxIs20Percent() throws IOException {
        Map<String, Object> o = order(1000, 1);
        o.put("country", "UK");
        assertTrue(engine.processOrder(o));
        assertEquals(1200.0, payableFromLog(), 0.01);
    }

    @Test
    void otherCountriesUseDefault15Percent() throws IOException {
        Map<String, Object> o = order(1000, 1);
        o.put("country", "FR");
        assertTrue(engine.processOrder(o));
        assertEquals(1150.0, payableFromLog(), 0.01);
    }

    // helper: add 'count' extra items to the order
    @SuppressWarnings("unchecked")
    private void addItems(Map<String, Object> o, int count, double price) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) o.get("items");
        for (int i = 0; i < count; i++) {
            items.add(item(price, 1));
        }
    }
}