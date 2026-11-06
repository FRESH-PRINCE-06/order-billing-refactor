package com.uet.legacy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

abstract class BillingTestBase {

    @TempDir
    Path tempDir;

    OrderBillingEngine engine;
    Path logFile;
    ByteArrayOutputStream console;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        engine = new OrderBillingEngine();
        logFile = tempDir.resolve("test_audit.log");
        engine.setLogFilePath(logFile.toString());
        originalOut = System.out;
        console = new ByteArrayOutputStream();
        System.setOut(new PrintStream(console));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    Map<String, Object> item(double price, int qty) {
        Map<String, Object> m = new HashMap<>();
        m.put("price", price);
        m.put("quantity", qty);
        return m;
    }

    /** A valid order: one item, paid by credit card, Pakistan, regular customer. */
    Map<String, Object> order(double price, int qty) {
        Map<String, Object> o = new HashMap<>();
        o.put("customerEmail", "ali@example.com");
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(item(price, qty));
        o.put("items", items);
        o.put("paymentMethod", "CREDIT_CARD");
        o.put("cardNumber", "1234567890123456");
        return o;
    }

    /** Reads the payable amount that the engine wrote to the log file. */
    double payableFromLog() throws IOException {
        String text = Files.readString(logFile);
        Matcher m = Pattern.compile("Payable: PKR (\\S+)").matcher(text);
        assertTrue(m.find(), "No Payable amount found in log: " + text);
        return Double.parseDouble(m.group(1));
    }

    String output() {
        return console.toString();
    }
}