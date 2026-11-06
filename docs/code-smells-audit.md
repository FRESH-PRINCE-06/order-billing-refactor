# Code Smells & SOLID Audit — OrderBillingEngine.java

## Code Smells
| # | Smell | Lines (approx.) | Why it is bad | Planned fix |
|---|-------|-----------------|---------------|-------------|
| 1 | God Class / Long Method | 21–150 | processOrder does validation, pricing, payment, saving and notifying in ~130 lines | Split into validator, calculators, strategies, repository, notifier; engine only coordinates |
| 2 | Magic Numbers | 63,65,68,71,73,77,85–91,120 | 0.15, 0.17, 5000, 25000 have no names, so rules are hard to read or change | Named constants inside the calculator classes |
| 3 | Primitive Obsession | 21, 28, 35, 58, 98 | Order is a Map<String,Object>; customer type and payment method are plain Strings; money is double | Typed values / constants; keep Map input for tests but isolate casts |
| 4 | Direct File I/O + hardcoded path | 19, 131–136 | Business logic writes files directly, so it is hard to test | OrderRepository interface + FileOrderRepository |
| 5 | Long if-else chains (switch smell) | 61–79, 86–92, 101–128, 140–147 | Every new rule means editing the same method | Switch/Map lookup and Strategy pattern |
| 6 | Console printing as error handling | 24,30,37,45,51,104… | println + return false hides the reason and is untestable | Exceptions carrying messages, printed in one place |
| 7 | Unsafe casts | 28, 35, 48, 49 | (double)/(int) casts crash with ClassCastException on wrong types | Isolate casts in the validator |

## SOLID Violations
| Principle | Where | Why it is violated | Fix |
|-----------|-------|--------------------|-----|
| S — Single Responsibility | whole class | One class changes for 5 different reasons | One class per responsibility |
| O — Open/Closed | 101–128, 140–147 | Adding JazzCash or WhatsApp means editing existing code | PaymentStrategy and NotificationService interfaces + registration |
| D — Dependency Inversion | 131 (FileWriter), 141/145 (System.out) | High-level logic depends on concrete low-level details | Depend on OrderRepository and NotificationService interfaces |
