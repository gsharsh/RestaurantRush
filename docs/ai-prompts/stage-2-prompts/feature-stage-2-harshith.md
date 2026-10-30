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
