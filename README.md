# Order Billing Engine: Legacy Code Refactoring

Assignment 02 for **Software Construction & Development**
University of Engineering and Technology, Abbottabad Campus
Department of Computer Science & Software Engineering
Instructor: Engr. Rizwan Shah

**Student:** [M.Mubashir Hassan] | **Roll No:** [24ABSWE0009]

## About

This project takes a messy legacy Java class (`OrderBillingEngine`) and refactors it safely:

1. JUnit 5 characterization tests were written **first**, against the untouched legacy code.
2. The code smells and SOLID violations were audited and documented.
3. The code was then refactored step by step, running the same tests after every change.

The original tests pass unchanged on the refactored code. No assertion was modified.

## Project Structure

```
src/main/java/com/uet/legacy/
  OrderBillingEngine.java        coordinates the order flow only
  pricing/
    DiscountCalculator.java      customer-type discount rules
    TaxCalculator.java           tax rate by country
  payment/
    PaymentStrategy.java         interface
    CreditCardPayment.java
    EasyPaisaPayment.java
    CashOnDeliveryPayment.java
    PaymentException.java
  notification/
    NotificationService.java     interface
    EmailNotification.java
    SmsNotification.java
  repository/
    OrderRepository.java         interface
    FileOrderRepository.java     file-based order log
  validation/
    OrderValidator.java
    InvalidOrderException.java

src/test/java/com/uet/legacy/
  BillingTestBase.java           shared test helpers
  DiscountAndTaxTest.java
  PaymentAndValidationTest.java

docs/
  code-smells-audit.md           smells and SOLID audit of the baseline code
  results.md                     before and after comparison
```

## Design

| Concern | Before | After |
|---------|--------|-------|
| Discounts and tax | Nested if-else inside one method | `DiscountCalculator`, `TaxCalculator` |
| Payments | if-else chain | `PaymentStrategy` (Strategy pattern) |
| Notifications | if-else chain | `NotificationService` interface |
| Order saving | `FileWriter` inside business logic | `OrderRepository` interface |
| Validation | Mixed into `processOrder` | `OrderValidator` |

## Adding New Behaviour (Open/Closed Principle)

A new payment method or notification channel is a **new class plus one register call**. No existing class is edited.

```java
OrderBillingEngine engine = new OrderBillingEngine();
engine.registerPaymentStrategy("JAZZCASH", new JazzCashPayment());
engine.registerNotificationService("WHATSAPP", new WhatsAppNotification());
```

## Requirements

- JDK 17 (or the version set in `pom.xml`)
- Apache Maven
- NetBeans, IntelliJ IDEA, or any Maven-compatible IDE

## Running the Tests

```
mvn test
```

In NetBeans: right-click the project and choose **Test** (Alt+F6).

Result: **[41] tests, 0 failures.**

## Refactoring Workflow

The commit history shows the refactoring in small steps:

1. Add baseline legacy file
2. Add discount characterization tests
3. Add payment and invalid-input tests
4. Document code smells and SOLID violations
5. Extract tax and discount calculators
6. Implement strategy pattern for payments
7. Decouple email and SMS notifications
8. Move file persistence behind a repository interface
9. Clean up the main service (single responsibility)
10. Verify all tests pass with the clean architecture

See `git log --oneline` for the full trail.
