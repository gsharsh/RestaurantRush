# AI prompt record

- **Branch:** `feature/s1-harshith`
- **Member:** Harshith (M1)
- **Stage:** Stage 1
- **Related pull request:** None; work is uncommitted.

## Prompt 1 — M1 foundation

**Prompt**

> On branch feature/s1-harshith, implement the M1 foundation feature: MenuItem, CustomerStatus and OrderStatus.
>
> Apply the shared instructions. Do not implement Order or customer classes yet.
>
> 1. MenuItem
>
> Implement the exact contract declaration:
>
> public class MenuItem
>
> Do not make it final, abstract, a record or an enum.
>
> Use the agreed private final fields:
> - String name
> - int priceCents
> - int preparationUnits
>
> Implement the exact constructor and final getters from Section 4.3.
>
> Validate:
> - Name must be non-null and nonblank.
> - Price must be nonnegative; zero is valid.
> - Preparation units must be positive.
>
> There must be no setters. Preserve the supplied valid display name; do not introduce unagreed trimming or normalization rules.
>
> Explain how private final state, validated construction and final accessors provide an immutable offering through the agreed API.
>
> Do not introduce MenuItem subclasses. M2’s Restaurant will construct the production menu.
>
> 2. Lifecycle enums
>
> Implement exactly these constants:
>
> CustomerStatus:
> WAITING, SEATED, EATING, READY_TO_PAY, PAID, LEFT
>
> OrderStatus:
> PLACED, PREPARING, READY, SERVED, PAID, CANCELLED
>
> Do not merge these state models or add extra constants.
>
> Explain why preparation readiness belongs to OrderStatus while eating and payment readiness belong to CustomerStatus.
>
> 3. Focused tests
>
> Create a self-contained runner under:
> /Users/gsharsh/RestaurantRush/src/test/java/restaurantrush/domain/
>
> Use the entry point:
> restaurantrush.domain.M1DomainTests
>
> Structure it so later prompts can extend the suite.
>
> Cover:
> - Valid construction and exact getter values.
> - Null, empty and whitespace-only names.
> - Negative prices.
> - Valid zero price.
> - Zero and negative preparation units.
> - Exact enum constants and their declared order.
>
> Use the agreed menu as test fixtures:
> - Salad: 900 cents, 1 unit.
> - Burger: 1500 cents, 1 unit.
> - Pasta: 1800 cents, 2 units.
>
> Use checked assertions and a nonzero exit on failure.
>
> Run:
> ./compile.sh --tests
> java -ea -cp out/classes restaurantrush.domain.M1DomainTests
>
> Report actual results. Stop after this feature. Log this prompt under ai-prompts too.

**What was used or rejected**

Applied the requested API and validation to `MenuItem` and kept the two lifecycle
enums separate with the exact constants and order. Private final fields hold only
an immutable `String` and primitive integers; constructor validation establishes
the invariants, and final getters expose them without setters or overrideable
accessors. Valid display names are stored unchanged.

Order readiness describes preparation of the dish. Eating and readiness to pay
describe the customer's lifecycle after service, including the separate timing
delays in Section 5 of the contract.

Only these three production types are implemented. The agreed menu is test data;
M2's `Restaurant` remains responsible for constructing the production menu.
No contract changes, subclasses, dependencies or production placeholders were
introduced. Human acceptance is pending.

**Files affected**

- `src/main/java/restaurantrush/domain/MenuItem.java`
- `src/main/java/restaurantrush/domain/CustomerStatus.java`
- `src/main/java/restaurantrush/domain/OrderStatus.java`
- `src/test/java/restaurantrush/domain/M1DomainTests.java`
- `docs/ai-prompts/feature-s1-harshith.md`
- `docs/testing/stage-1-tests.md`

**Human verification**

Pending Harshith's review. Codex executed `./compile.sh --tests` successfully
(exit 0), then `java -ea -cp out/classes restaurantrush.domain.M1DomainTests`
returned `M1DomainTests: PASS (39 checks)` (exit 0). These automated results are
recorded in [the Stage 1 test record](../testing/stage-1-tests.md); they are not
human verification. Compilation targeted Java 17 using JDK 26.0.2, and execution
used Java 26.0.2.

**Related commits**

None created. This feature is an uncommitted working-tree change based on
`aaf89d19a63dd2555e3f3ffb0393731d11843ffe`.

## Prompt 2 — M1 Order

**Prompt**

> Implement the M1 Order feature, building on the existing MenuItem and lifecycle enums.
>
> Apply the shared instructions. Preserve and extend the foundation tests. Do not implement customer or peer-owned classes yet.
>
> 1. Exact design
>
> Implement:
>
> public final class Order
>
> Use the exact contract fields and API:
> - final positive int id.
> - final non-null MenuItem item.
> - OrderStatus status, initially PLACED.
> - int remainingUnits, initially item.getPreparationUnits().
> - final int billCents, copied from the explicit constructor quote.
>
> The constructor is:
> Order(int id, MenuItem item, int lockedBillCents)
>
> Validate positive ID, non-null item, positive item units and a nonnegative quote before construction succeeds.
>
> Implement every documented getter with its exact signature.
>
> 2. Preparation and transitions
>
> Implement the documented operations precisely:
>
> - canPrepare(): true only for PLACED or PREPARING with positive remaining units.
> - prepareOneUnit(): require canPrepare(), subtract exactly one unit, then become PREPARING or READY.
> - markServed(): require READY, then become SERVED.
> - markPaid(): require SERVED, then become PAID.
> - cancel(): require PLACED, PREPARING or READY, then become CANCELLED.
> - isReady(): check status == READY.
>
> A one-unit order moves directly from PLACED to READY.
>
> A two-unit order progresses:
> PLACED -> PREPARING -> READY
>
> Zero remaining units alone must not make a SERVED, PAID or CANCELLED order ready.
>
> Reject invalid and repeated transitions without changing state. Do not make cancellation or payment silently idempotent.
>
> 3. Locked billing
>
> Store the supplied lockedBillCents value. Do not recompute it from MenuItem.
>
> The constructor accepts a nonnegative explicit quote. In actual Stage 1 customer flow, Waiter must supply customer.quotePriceCents(item).
>
> Explain this responsibility boundary without implementing Waiter or inventing pricing policies.
>
> 4. OOP requirements
>
> Order must own its preparation counter and lifecycle. Callers request behavior through methods rather than set status or remaining units.
>
> Explain:
> - Encapsulation through guarded operations.
> - Immutable identity and locked billing.
> - The has-a relationship with immutable MenuItem.
> - Why referencing a shared MenuItem does not imply exclusive ownership of it.
>
> Keep the implementation cohesive; add no controller, staff or customer responsibilities.
>
> 5. Focused tests
>
> Extend M1DomainTests to cover:
> - Initial values and constructor validation.
> - One-unit and two-unit preparation.
> - Exactly one unit consumed per preparation call.
> - Correct canPrepare() and isReady() results.
> - Valid service and payment progression.
> - Cancellation from PLACED, PREPARING and READY.
> - Invalid and repeated transitions.
> - State preservation after rejected operations.
> - No negative remaining units.
> - Terminal-state restrictions.
> - Storage of the explicit quote, including zero, without recomputation.
>
> Distinguish constructor quote-storage tests from normal Stage 1 pricing behavior.
>
> Run the complete existing M1 test runner, report actual results and stop.

**What was used or rejected**

Implemented only the contracted `Order` type and extended the existing runner
with checked assertions without removing foundation checks. `Order` owns its private
preparation counter and status. Every mutation checks its required state before
changing anything; repeated payment and cancellation are rejected.

The ID, item reference and supplied integer-cent bill are private final fields.
`Order` has a `MenuItem` reference and returns that same immutable offering; the
menu item can be shared by independent orders without exclusive ownership.

Constructor quote-storage tests deliberately use nonnegative quotes that differ
from the menu price, including zero. They verify storage only. In actual Stage 1
flow, Waiter must obtain and pass `customer.quotePriceCents(item)`; the Order
constructor does not select or recompute prices. Customer, staff, control and
peer-owned classes are not part of this feature. Human acceptance is pending.

**Files affected**

- `src/main/java/restaurantrush/domain/Order.java`
- `src/test/java/restaurantrush/domain/M1DomainTests.java`
- `docs/ai-prompts/feature-s1-harshith.md`
- `docs/testing/stage-1-tests.md`

**Human verification**

Pending Harshith's review. Codex executed `./compile.sh --tests` successfully
(exit 0), then `java -ea -cp out/classes restaurantrush.domain.M1DomainTests`
returned `M1DomainTests: PASS (519 checks)` (exit 0), including the original
39 foundation checks. Compilation targeted Java 17 using JDK 26.0.2, and execution
used Java 26.0.2. The current results are recorded in
[the Stage 1 test record](../testing/stage-1-tests.md); they are not human
verification.

**Related commits**

No Order-feature commit created. This feature is an uncommitted working-tree
change based on `7754d4883bd9d38dc72a7fb3e2b6cd9653afbb39`, which contains the
foundation implementation.
