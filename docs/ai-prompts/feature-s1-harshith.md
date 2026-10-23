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


## Prompt 3 — M1 customers

**Prompt**

> Implement the complete M1 customer feature: Customer and RegularCustomer.
>
> Apply the shared instructions. Build on the existing MenuItem, Order and enums. Preserve earlier behavior and tests.
>
> Implement Customer and RegularCustomer together so the shared lifecycle and active polymorphic rules are exercised in the same feature.
>
> 1. Customer design
>
> Implement exactly:
>
> public abstract class Customer
>
> Use the agreed private fields:
> - final positive int id.
> - int satisfaction, initially 100.
> - CustomerStatus status, initially WAITING.
> - int servedTurn, initially -1.
> - int readyToPayTurn, initially -1.
> - Order order, initially null and assigned at most once.
>
> Implement the protected constructor, three abstract rule methods, all final lifecycle methods and all final getters exactly as documented.
>
> The abstract rules are:
> - waitingLossPerTurn()
> - chooseMenuItem(List<MenuItem> menu)
> - quotePriceCents(MenuItem item)
>
> Keep shared lifecycle mutation in Customer. Do not duplicate it in RegularCustomer.
>
> 2. Customer transitions
>
> Implement:
>
> - seat(): require WAITING; become SEATED.
> - acceptOrder(Order): require SEATED, no existing order, non-null order and matching IDs; store the supplied instance.
> - markServed(int): require positive turn, SEATED and associated order already SERVED; become EATING and record the turn.
> - finishEating(int): require positive turn. If EATING and servedTurn < turn, become READY_TO_PAY and record the turn; otherwise make no change.
> - applyWaitingLoss(): only WAITING and SEATED lose satisfaction. Call waitingLossPerTurn() polymorphically and floor the result at zero.
> - markPaid(): require READY_TO_PAY and associated order already PAID; become PAID.
> - abandon(): require WAITING or SEATED, cancel an attached unserved order where applicable and become LEFT.
>
> Preserve these boundaries:
>
> - Ordering/preparation leave the customer SEATED.
> - applyWaitingLoss() never performs abandonment automatically.
> - Customer.abandon() does not require zero satisfaction; Restaurant.removeAbandoned() owns that check.
> - acceptOrder() has no additional agreed PLACED-order precondition.
> - Domain IDs/turns must be positive, without imposing schedule/game maximums here.
> - finishEating() must preserve its documented no-op behavior and must not rewrite recorded readiness on repeated calls.
> - getOrder() returns the actual associated object, or null before ordering.
> - Queue cleanup, table release, revenue and statistics belong to Restaurant.
> - Staff availability and payment eligibility timing belong to collaborating systems.
>
> Validate preconditions before mutation. Preserve state after failures.
>
> 3. RegularCustomer
>
> Implement exactly:
>
> public final class RegularCustomer extends Customer
>
> Add no fields.
>
> Delegate construction and ID validation to Customer.
>
> Override:
> - waitingLossPerTurn(): return 8.
> - chooseMenuItem(menu): validate the documented requirements, then return menu.get((getId() - 1) % menu.size()).
> - quotePriceCents(item): validate the item, then return item.getPriceCents().
>
> Return the existing selected menu object without changing the menu.
>
> IDs 1–6 must select:
> Salad, Burger, Pasta, Salad, Burger, Pasta.
>
> Use @Override. Do not add future customer types.
>
> 4. Demonstrate genuine OOP
>
> Exercise a RegularCustomer through a variable declared as Customer.
>
> Through that reference:
> - Select an item.
> - Obtain the quote.
> - Invoke waiting-loss behavior.
> - Exercise inherited lifecycle operations.
>
> Customer.applyWaitingLoss() must call the overridable rule rather than hardcode 8.
>
> Do not use instanceof, downcasts, class-name checks or concrete-type switches to choose behavior.
>
> Explain:
> - Abstraction through the Customer contract.
> - Inheritance and reuse of shared lifecycle.
> - Runtime dispatch of customer rules.
> - Encapsulation through private state and final lifecycle methods.
> - The template-method-style relationship between protected lifecycle invariants and overridable rules.
>
> Do not introduce extra interfaces or synthetic production subclasses merely to demonstrate these concepts.
>
> 5. Focused tests
>
> Extend M1DomainTests with:
>
> - Constructor validation and exact initial values.
> - Selection and quoting through Customer references.
> - Required null/empty menu and null item validation.
> - Selected-object identity and no menu mutation.
> - Valid customer lifecycle through PAID.
> - Order-before-customer service/payment requirements.
> - Null/mismatched order rejection.
> - Rejection of second-order assignment.
> - Preservation of the accepted Order identity.
> - Invalid and repeated transitions without partial mutation.
> - Same-turn finishEating() no-op.
> - Later-turn eating completion and stable timestamps.
> - Waiting loss only in WAITING/SEATED.
> - Repeated losses reaching zero without automatically departing.
> - No waiting loss in EATING, READY_TO_PAY, PAID or LEFT.
> - Abandonment while WAITING and SEATED.
> - Cancellation of attached PLACED, PREPARING and READY orders during abandonment.
> - Customer.abandon() working without an invented zero-satisfaction requirement.
>
> Use valid public operations to establish test states. Do not bypass invariants with reflection or setters.
>
> Run all accumulated M1 tests, report actual results and stop.

**What was used or rejected**

Added only the contracted abstract `Customer` and field-free final
`RegularCustomer`. Private state and final lifecycle methods protect the shared
invariants; three abstract rule methods supply customer behavior. The inherited
waiting-loss operation calls `waitingLossPerTurn()` dynamically rather than
hardcoding RegularCustomer's value. Tests exercise selection, quoting, waiting
loss and lifecycle operations through `Customer` references.

No type checks, downcasts, extra interfaces, future customer types, production
stubs or peer-owned responsibilities were added. Customer IDs and turns have a
positive lower bound with no schedule or game maximum. Waiting loss floors at
zero without performing abandonment; customer abandonment does not require zero
satisfaction. Order assignment preserves the supplied object's identity and has
no extra order-status precondition. Valid eating-completion no-ops preserve the
original timestamps.

For invalid supplied method arguments, the implementation uses
`IllegalArgumentException`; lifecycle misuse uses `IllegalStateException`.
The contract explicitly prescribes the latter and constructor validation; method
argument exception categories use ordinary implementation judgment. No precedence
is promised when a call violates multiple preconditions.

Edge interpretation: abandonment requires only `WAITING` or `SEATED`, cancels
attached `PLACED`, `PREPARING` or `READY` orders, and leaves an attached `SERVED`,
`PAID` or already `CANCELLED` order unchanged before becoming `LEFT`. This follows
"where applicable" without adding a precondition that would contradict the
explicitly unrestricted order-state acceptance boundary. Repeated abandonment
still fails. The selected menu entry is validated as non-null under the shared
non-null-return convention; unused entries do not add a menu-wide restriction.

**Files affected**

- `src/main/java/restaurantrush/domain/Customer.java`
- `src/main/java/restaurantrush/domain/RegularCustomer.java`
- `src/test/java/restaurantrush/domain/M1DomainTests.java`
- `docs/ai-prompts/feature-s1-harshith.md`
- `docs/testing/stage-1-tests.md`

**Human verification**

Pending Harshith's review. On 2026-10-24 (Asia/Singapore), Codex executed
`./compile.sh --tests` successfully (exit 0), then
`java -ea -cp out/classes restaurantrush.domain.M1DomainTests` returned
`M1DomainTests: PASS (2424 checks)` (exit 0). All previous 519 checks were retained.
Compilation targeted Java 17 using JDK 26.0.2; execution used Java 26.0.2.
The results are recorded in [the Stage 1 test record](../testing/stage-1-tests.md);
they are not human verification.

**Related commits**

No customer-feature commit created. This feature is an uncommitted working-tree
change based on `b17df6781be0f1a86b84c397502312219c61e980`, which contains the
foundation and Order implementation.

## Prompt 4 — M1 collaboration and executable OOP demo

**Prompt**

> Complete the M1 collaboration feature: cross-class lifecycle tests and an executable OOP demonstration.
>
> Apply the shared instructions. Review the six implemented M1 types first.
>
> This feature verifies interaction among M1 objects. It must not implement missing Restaurant, staff, control, arrival or CLI classes.
>
> 1. Cross-class verification
>
> Extend M1DomainTests with scenarios checking:
>
> - A Customer chooses an existing MenuItem through polymorphic dispatch.
> - The chosen item and customer quote are supplied to Order construction.
> - Customer accepts the exact Order instance.
> - Preparation changes Order state without incorrectly changing customer state.
> - The customer remains SEATED and loses satisfaction while food is unfinished or READY but unserved.
> - Service marks Order SERVED before Customer becomes EATING.
> - Eating cannot complete on the serving turn.
> - Payment marks Order PAID before Customer becomes PAID.
> - Abandonment cancels an unserved order, including READY.
> - Rejected operations preserve all relevant M1 state and object identity.
>
> Use the documented contract, rather than inventing stronger guards.
>
> If peer classes exist, inspect compatibility with the agreed handoffs and report discrepancies. Do not silently rewrite another owner’s implementation.
>
> 2. Executable demonstration
>
> Create a test-only entry point:
>
> restaurantrush.domain.M1DomainDemo
>
> Place it under:
> /Users/gsharsh/RestaurantRush/src/test/java/restaurantrush/domain/
>
> Use a Customer reference containing RegularCustomer(3), the agreed menu and one shared Pasta order.
>
> Obtain selection and quote through Customer, not by hardcoding the selected item into the customer flow.
>
> Produce and assert this conceptual timeline:
>
> Turn 7:
> - Seat the customer.
> - Apply waiting loss.
> - Customer: SEATED, satisfaction 92.
>
> Turn 8:
> - Select and quote Pasta.
> - Construct and accept the same Order with a bill of 1800 cents.
> - Prepare one unit.
> - Order: PREPARING, 1 unit remaining.
> - Apply waiting loss: satisfaction 84.
>
> Turn 9:
> - Prepare the second unit.
> - Order: READY, 0 units remaining.
> - Apply waiting loss: satisfaction 76.
>
> Turn 10:
> - Mark the order SERVED, then mark the customer served.
> - Customer: EATING.
> - finishEating(10) makes no change.
>
> Turn 11:
> - finishEating(11) changes the customer to READY_TO_PAY.
> - readyToPayTurn is 11.
> - Satisfaction remains 76.
>
> Turn 12:
> - Simulate an already-validated payment.
> - Mark the order PAID before marking the customer PAID.
> - Satisfaction remains 76.
>
> Keep demonstration printing outside the production domain classes.
>
> Include representative rejected-operation examples with checked state preservation.
>
> 3. Explain the demonstration
>
> Identify the actual calls showing:
> - Encapsulation.
> - Abstraction.
> - Inheritance.
> - Runtime polymorphism.
> - Shared object identity.
> - Customer–Order and Order–MenuItem relationships.
>
> Explain why the direct domain demonstration does not establish arrival scheduling, staff limits, cashier selection, queue/table cleanup or complete-game correctness.
>
> 4. Execute
>
> Run:
> ./compile.sh --tests
> java -ea -cp out/classes restaurantrush.domain.M1DomainTests
> java -ea -cp out/classes restaurantrush.domain.M1DomainDemo
>
> Report actual outcomes, a concise checked trace and any integration dependencies. Stop after this feature.

**What was used or rejected**

Reviewed all six M1 types and kept their production implementations unchanged.
Extended the existing runner with three cross-class scenarios covering the
normal Customer-selection/quote/Order flow, abandonment of unserved orders, and
rejected handoffs with snapshots of customer, accepted/candidate orders, menu
contents, offering values and shared identity. Earlier tests remain intact.

Added `M1DomainDemo` exclusively under test sources. It uses a Customer reference
holding RegularCustomer(3), obtains the existing selected Pasta and quote through
the Customer API, and passes both to one shared Order. It asserts and prints the
requested turns 7–12 and six rejected-operation examples. It explains the actual
OOP calls and the limited domain-only scope.

No peer implementations exist in this branch to assess their handoff
compatibility. No missing peers, production stubs or stronger domain guards were
added. The demo manually supplies turns and assumes validated payment; it does
not establish scheduling, staff limits, Cashier selection, eligibility, revenue,
queue/table cleanup or complete-game correctness. Human acceptance is pending.

**Files affected**

- `src/test/java/restaurantrush/domain/M1DomainTests.java`
- `src/test/java/restaurantrush/domain/M1DomainDemo.java`
- `docs/ai-prompts/feature-s1-harshith.md`
- `docs/testing/stage-1-tests.md`

**Human verification**

Pending Harshith's review. On 2026-10-24 (Asia/Singapore), Codex executed:

- `./compile.sh --tests`: compiled production and test sources; exit 0.
- `java -ea -cp out/classes restaurantrush.domain.M1DomainTests`:
  `M1DomainTests: PASS (2914 checks)`; exit 0, retaining the prior 2424 checks.
- `java -ea -cp out/classes restaurantrush.domain.M1DomainDemo`:
  `M1DomainDemo: PASS (257 checks)`; exit 0, with the exact checked timeline and
  all six rejected-operation examples preserving state and identities.

Compilation targeted Java 17 using JDK 26.0.2; execution used Java 26.0.2. The
observed trace and OOP-call mapping are recorded in
[the Stage 1 test record](../testing/stage-1-tests.md). These automated results
are not human verification or full Stage 1 acceptance.

**Related commits**

No collaboration-feature commit created. This feature is an uncommitted working-
tree change based on `9a32cc426421b5391f27199b25eec60ac26aeb64`, which contains
all six M1 production types and the previous tests.
