# AI prompt record

- **Branch:** `feature/stage-2-harshith`
- **Member:** Harshith (M1)
- **Stage:** Stage 2
- **Related pull request:** None created.

## Prompt 1 — Shared customer-type label API

**Prompt — material excerpts**

> Implement the shared customer-type label API for my Stage 2 M1 contribution to RestaurantRush. I am Harshith (M1).
>
> Add public abstract String typeLabel() to Customer. Document it as a stable, non-null presentation label.
> Override it in RegularCustomer to return exactly "Regular".
> Preserve RegularCustomer's loss of 8, full-price quotes and existing ID-cycle selection/validation.
> Keep Customer's lifecycle fields private and its lifecycle methods final. Labels must not select gameplay behaviour.
>
> At the current baseline, GameControllerTest.QuotedCustomer and StaffTest.DiscountedCustomer need minimal typeLabel() overrides.
> These narrow test-only compatibility edits are permitted. Preserve their existing behaviour and assertions, disclose the edits and flag M4/M3 owner review respectively.
>
> Add a focused assertion that a RegularCustomer referenced as Customer returns "Regular".
> This should demonstrate an abstract contract implemented by a concrete subtype, without casts, instanceof, class-name checks or label-based branching.
>
> Compile production and tests using the existing scripts, then run the relevant existing customer test.
> Do not implement VIPCustomer in this prompt.

**What was used or rejected**

Added the agreed abstract presentation method, the constant Regular label and one focused assertion in the existing Regular test. Added only constant `"Quoted"` and `"Discounted"` labels to the existing controller/staff test fixtures. Their rules and assertions are unchanged; M4 (Viti) and M3 (Hunar) review respectively remains pending.

Scanned all Customer subclasses, including anonymous implementations; only Regular and these two fixtures exist at this baseline. No Critic implementation was present to preserve. The future M2 Critic must return `"Critic"`; M1 VIP/`"VIP"` is later work. M5 still owns `CustomerView.type`, CLI display and the README transition record. Supply the new `+ typeLabel(): String` operation on abstract Customer and Regular's override for M5's shared UML coordination.

Labels are a confirmed team requirement in the Stage 2 contract; the brief requires customer-object dispatch and a common lifecycle. No material conflict was found for this change. Proposed VIP rounding and arrival mapping were not implemented. No lifecycle, selection, quotation, MenuItem, Order, enum, arrival, view or CLI changes were needed.

**Files affected**

- `src/restaurantrush/domain/Customer.java`
- `src/restaurantrush/domain/RegularCustomer.java`
- `tests/restaurantrush/domain/RegularCustomerTest.java`
- `tests/restaurantrush/control/GameControllerTest.java` — test-only compatibility; M4 review pending
- `tests/restaurantrush/staff/StaffTest.java` — test-only compatibility; M3 review pending
- This branch prompt record and the shared `docs/testing/stage-2-tests.md` record.

**Human verification**

Pending. Automated working-tree results and runtime limits are recorded in the [shared Stage 2 test record](../../testing/stage-2-tests.md). Automated checks do not establish human approval or Stage 2 release readiness.

**Related commits**

- None created. Working-tree base: `550ae6f8b5dffc2c4b614df65153783f22b3a513`.

## Prompt 2 — VIPCustomer and focused rule tests

**Prompt**

> Continue my Stage 2 M1 work under the constraints established in Prompt 1. Implement VIPCustomer and its focused rule tests only.
>
> Recheck the current Stage 2 contract and existing Customer/RegularCustomer implementation before editing. Preserve changes already made by teammates.
>
> Add:
>
> - /Users/gsharsh/RestaurantRush/src/restaurantrush/domain/VIPCustomer.java
> - /Users/gsharsh/RestaurantRush/tests/restaurantrush/domain/VIPCustomerTest.java
>
> Declare:
>
> ```java
> public final class VIPCustomer extends Customer
> ```
>
> Add no instance fields or lifecycle state. Extend Customer directly; do not change RegularCustomer's final declaration or make VIP inherit from it.
>
> The public surface must be:
>
> ```java
> public VIPCustomer(int id)
> public String typeLabel()
> public int waitingLossPerTurn()
> public MenuItem chooseMenuItem(List<MenuItem> menu)
> public int quotePriceCents(MenuItem item)
> ```
>
> Use @Override on the four display/rule methods.
>
> Constructor:
>
> - Call super(id).
> - Inherit positive-ID validation and initial state: satisfaction 100, WAITING, no order and both turn timestamps -1.
> - Do not duplicate base-class initialization or validation.
>
> Rules:
>
> - typeLabel() returns exactly "VIP".
> - waitingLossPerTurn() returns 5.
> - chooseMenuItem(menu) returns menu.get((getId() - 1) % menu.size()).
> - Reject a null/empty menu or a null selected item with IllegalArgumentException.
> - An unused null entry does not invalidate a valid selection.
> - Return the existing selected MenuItem instance without copying, sorting or mutating the menu.
> - quotePriceCents(item) rejects null with IllegalArgumentException and applies a 10% discount using integer cents.
> - Required quotes: Salad 900 -> 810, Burger 1500 -> 1350, Pasta 1800 -> 1620.
>
> General-price rounding remains proposed in the current contract. Unless the current agreement has since settled it differently, use its documented formula as an explicit working assumption pending team review:
>
> ```java
> (int) ((9L * item.getPriceCents() + 5L) / 10L)
> ```
>
> This rounds the discounted total to the nearest cent, with half cents rounded up. Preserve long arithmetic, allow zero, and avoid floating point or separately rounding the discount. Do not describe the proposal as an approved decision.
>
> Use the existing self-contained *Test.java/main/check style. Exercise VIPCustomer through Customer references.
>
> Cover:
>
> - Invalid zero/negative IDs and valid positive IDs, including Integer.MAX_VALUE.
> - All inherited initial-state values.
> - Exact VIP label and waiting-loss rule.
> - Cyclic choice for IDs 1 through 6 and larger valid IDs.
> - Custom, reordered and immutable menus.
> - Exact selected-item identity and unchanged menu contents/order.
> - Null/empty menus, selected null entries, unused null entries and null quoted items.
> - Preservation of customer/menu state after rule calls and rejected inputs.
> - All base-menu quotes and zero.
> - Rounding below, at and above a half-cent boundary.
> - Overflow-safe pricing for Integer.MAX_VALUE.
>
> Use independently specified expected values, not the production formula as the only test oracle. Under the working policy, examples are:
>
> ```text
> 1 -> 1
> 5 -> 5
> 6 -> 5
> 15 -> 14
> Integer.MAX_VALUE -> 1932735282 cents
> ```
>
> Do not construct nullable fixtures with List.of(null).

**What was used or rejected**

Added a final, field-free VIPCustomer extending Customer directly, with only the requested constructor and four overrides. The constructor delegates to `super(id)`; all lifecycle state and operations remain inherited. VIP supplies the exact `"VIP"` label, loss of 5, ID-cycle selection and discounted integer-cent quotes. Existing production classes and teammate test fixtures were unchanged.

The current Stage 2 contract still describes general-price rounding as proposed. Used its documented long-arithmetic formula as an explicit working assumption pending team review, as requested; source and test comments preserve that status. No approval of the rounding proposal is claimed.

Added focused tests in the existing self-contained main/check style. Calls use Customer references, literal expected selections/quotes and mutable nullable fixtures. Coverage includes all requested initial-state, validation, menu identity/order, rounding and maximum-value cases, plus preservation of a customer with reduced satisfaction and an associated order. No new harness, dependencies, staff/payment regressions, arrival integration, view/CLI changes or Stage 3 features were added.

**Files affected**

- `src/restaurantrush/domain/VIPCustomer.java` — added
- `tests/restaurantrush/domain/VIPCustomerTest.java` — added
- This branch prompt record — Prompt 2 appended on request after implementation.

**Human verification**

Pending; automated results do not establish human approval. The following automated executions were observed on 2026-10-30 in the uncommitted working tree based on `5e86818543e698f06cbab45ed2474ace974f78fc`; each command exited 0:

| Command | Observed result |
| --- | --- |
| `./compile.sh` | Production sources compiled successfully |
| `./compile.sh --tests` | Production and test sources compiled successfully |
| `java -ea -cp out/classes restaurantrush.domain.VIPCustomerTest` | PASS — 468 checks |
| `java -ea -cp out/classes restaurantrush.domain.RegularCustomerTest` | PASS — 111 checks |
| `java -ea -cp out/classes restaurantrush.domain.CustomerTest` | PASS — 2292 checks |

Compiler and runtime were Java 26.0.2; compilation used `--release 17`. No Java 17 runtime execution, mixed-customer full-game verification or exact implementation-commit verification is claimed. Compiled API inspection confirmed the direct final Customer inheritance, no declared fields and the exact requested constructor/method surface. The published `stage-1` tag was unchanged.

**Related commits**

- None created for Prompt 2. Working-tree base: `5e86818543e698f06cbab45ed2474ace974f78fc` (Prompt 1's customer-type label API).
- No commit, push, merge, pull request or tag mutation was performed by the assistant.

## Prompt 3 — Customer substitution, lifecycle and collaboration

**Prompt**

> Continue my Stage 2 M1 work under the constraints established in Prompt 1.
>
> Demonstrate that RegularCustomer and VIPCustomer are substitutable Customer implementations through focused lifecycle and collaboration tests. Use actual objects and observable behaviour.
>
> Inspect existing coverage first. Extend VIPCustomerTest, CustomerTest, RegularCustomerTest or OrderTest only where the responsibility naturally belongs. Reuse useful coverage instead of duplicating every Stage 1 test. Do not introduce a demonstration application or another test harness.
>
> Use a mixed collection of Customer references or a shared helper accepting Customer. Supply independent expected outcomes for each instance.
>
> Demonstrate:
> - Abstraction: tests and collaborators depend on Customer's public contract.
> - Inheritance: both concrete types reuse Customer's final lifecycle methods.
> - Encapsulation: private state changes only through guarded operations.
> - Runtime polymorphism: the same base-typed calls dispatch labels, selection, quotation and waiting loss to the concrete object.
> - Object collaboration: Customer retains the actual shared Order; Order retains the selected shared immutable MenuItem.
>
> Do not use casts, instanceof, getClass(), class-name switches or labels to select behaviour. The documented ID modulo inside menu selection remains intentional.
>
> Cover these lifecycle behaviours:
>
> 1. VIP waiting loss
> - applyWaitingLoss() deducts 5 while WAITING and SEATED.
> - Satisfaction clamps at zero and remains within 0–100.
> - No loss occurs in EATING, READY_TO_PAY, PAID or LEFT.
> - A VIP starting at 100 reaches zero after 20 applicable loss calls.
> - Test this boundary at unit level. Do not extend the fixed 18-turn game or present it as an ordinary base-run VIP abandonment scenario.
>
> 2. Shared order and locked billing
> - Customer chooses the existing MenuItem and supplies the quote.
> - Construct Order with that quote and associate the same Order instance with the customer.
> - Verify item/order identity and the locked discounted bill through preparation and lifecycle transitions.
> - Exercise legal preparation, READY -> SERVED, later-turn eating completion and payment prerequisites.
> - Confirm no post-service waiting loss.
> - Verify representative rejected transitions preserve state.
> - Preserve the agreed lifecycle guards; do not add a new order-status restriction to acceptOrder().
>
> 3. Abandonment
> - An inherited abandon() call cancels a READY-but-unserved VIP order and retains the documented references.
> - Reaching zero in applyWaitingLoss() does not itself abandon the customer.
> - Restaurant remains responsible for automatic abandonment, queue/table cleanup and accounting.
> - Do not claim that an entity-only test verifies table release or revenue totals.
>
> Inspect the existing pricing flow:
>
> ```text
> Customer selection -> Customer quote -> locked Order bill
> -> Restaurant payment accounting
> ```
>
> Confirm that Waiter, Cashier and the controller do not calculate or reapply the VIP discount. Demonstrate the M1 portion with focused tests. Leave the complete staff/payment, table-release and totals regression matrix to M3.
>
> Keep production behaviour unchanged unless a focused test exposes a demonstrated M1 defect. Explain any necessary fix and its contract implications. Report defects outside my scope for owner review.
>
> Do not require CriticCustomer for my focused tests to compile. If M2's actual implementation is available, review its preference against the contract:
> - Most preparation units first.
> - Higher base price breaks preparation ties.
> - Earliest supplied item wins a complete tie.
> - Existing item identity and menu contents are preserved.
> - Required validation, full-price quote, loss of 12 and "Critic" label are present.
>
> Report this as a review of the actual implementation. If unavailable, mark the review pending; do not implement a substitute.
>
> Run the affected focused tests. Finish with concrete source/test examples of OOP, actual results and remaining handoffs. Describe Customer–Order and Order–MenuItem accurately as associations; a stored reference alone does not prove strong UML composition.

**What was used or rejected**

Inspected existing coverage and reused the CustomerTest collaboration scenarios through shared helpers accepting Customer. The same calls exercise labels, selection, quotation, waiting loss and inherited final lifecycle methods for actual Regular and VIP objects, with independent expected bills (1800/1620 cents), loss rules (8/5) and satisfaction sequences (92, 84, 76 / 95, 90, 85). Existing Regular coverage was preserved.

The shared scenarios verify exact Order/MenuItem identity and locked billing through preparation, service, later-turn eating completion and entity payment. Representative rejected service, payment, mismatched-order and replacement-order operations preserve customer/order state. VIP abandonment cancels a READY-but-unserved order while retaining the documented references. Customer–Order and Order–MenuItem are described as associations; no strong UML composition claim is made from stored references.

Added focused VIP unit checks for actual five-point losses in WAITING and SEATED, the twentieth applicable call reaching zero, bounds and repeated zero clamping, no automatic departure at zero, and no loss after explicit departure. Shared collaboration checks cover no loss in EATING, READY_TO_PAY and PAID. The 20-call boundary is explicitly distinguished from the fixed 18-turn game; no base-run VIP abandonment claim is made.

Source inspection confirmed Waiter passes the customer's quote into Order, Restaurant records the locked bill, and Cashier/controller do not calculate or reapply the VIP discount. These entity tests do not verify Restaurant's payer eligibility, automatic abandonment, table release or revenue totals. The complete mixed-customer staff/payment regression matrix remains M3's responsibility.

No defect requiring a production or contract change was found. Production code, acceptOrder() guards, RegularCustomerTest and OrderTest were unchanged. No demonstration application, new harness, dependencies or teammate implementation was added. CriticCustomer was unavailable, so review of the actual M2 implementation remains pending; no substitute was introduced. M4 arrival integration, M5 view/CLI and shared UML/evidence coordination, M1 evidence/UML completion and owner reviews remain outstanding. General-price rounding remains a working assumption pending team review.

**Files affected**

- `tests/restaurantrush/domain/CustomerTest.java` — reused collaboration scenarios for Regular/VIP substitution and entity lifecycle checks
- `tests/restaurantrush/domain/VIPCustomerTest.java` — added focused waiting-loss boundary and departure checks
- This branch prompt record — Prompt 3 appended on request after implementation.

**Human verification**

Pending; automated results do not establish human approval. The following automated executions were observed on 2026-10-30 in the uncommitted working tree based on `afef6b6ded306759034407ecee458a5595d074de`; each command exited 0:

| Command | Observed result |
| --- | --- |
| `./compile.sh --tests` | Production and test sources compiled successfully |
| `java -ea -cp out/classes restaurantrush.domain.CustomerTest` | PASS — 2868 checks |
| `java -ea -cp out/classes restaurantrush.domain.VIPCustomerTest` | PASS — 826 checks |
| `java -ea -cp out/classes restaurantrush.domain.RegularCustomerTest` | PASS — 111 checks |
| `java -ea -cp out/classes restaurantrush.domain.OrderTest` | PASS — 487 checks |

Compiler and runtime were Java 26.0.2; compilation used `--release 17`. No Java 17 runtime execution, full-game verification, complete staff/payment regression matrix or exact implementation-commit verification is claimed. Final diff review found no production changes; `git diff --check` passed. The published `stage-1` tag was unchanged. The executions above occurred during implementation; this subsequent prompt-record edit did not rerun them.

**Related commits**

- None created for Prompt 3. Working-tree base: `afef6b6ded306759034407ecee458a5595d074de` (VIPCustomer rules and focused tests from Prompt 2).
- No commit, push, merge, pull request or tag mutation was performed by the assistant.
