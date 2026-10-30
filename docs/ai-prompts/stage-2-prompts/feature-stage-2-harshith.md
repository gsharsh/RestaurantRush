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
