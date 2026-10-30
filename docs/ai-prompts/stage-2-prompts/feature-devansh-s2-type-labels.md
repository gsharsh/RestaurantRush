# AI prompt record

- **Branch:** `feature/devansh-s2-type-labels`
- **Member:** Devansh Poddar (M5)
- **Stage:** Stage 2
- **Related pull request:** To be added

## Prompt 1

**Prompt**

> Implement the M5 side of the agreed Stage 2 type-label handoff: add `CustomerView.type`, copy it from `Customer.typeLabel()`, show it in the CLI, and update affected tests.

**What was used or rejected**

The assistant (Claude) added a required `String type` component immediately after `id` in `CustomerView`, rejecting null or blank labels, with `from()` copying `customer.typeLabel()`. The CLI shows the label after each customer ID in both the waiting queue and the customer list (for example `C2 (VIP)`), looking it up from the snapshot's own views so no concrete-type checks are added. Existing `CustomerView` constructions in `ViewRecordsTest` and `GameControllerTest` gained the `"Regular"` label, and new checks cover label copying, null/blank rejection and labelled CLI output for Regular, VIP and Critic customers.

**Files affected**

- `src/restaurantrush/view/CustomerView.java`
- `src/restaurantrush/boundary/RestaurantCLI.java`
- `tests/restaurantrush/view/ViewRecordsTest.java`
- `tests/restaurantrush/boundary/RestaurantCLITest.java`
- `tests/restaurantrush/control/GameControllerTest.java`

**Human verification**

Depends on M1's `Customer.typeLabel()`. Compiled and ran `./test.sh` against a temporary local copy of that method (not committed): all test classes passed, including `ViewRecordsTest` (49 checks) and `RestaurantCLITest` (20 checks), and the game displays `C1 (Regular)`. Must be re-run once M1's branch is merged.

## Prompt 2

**Prompt**

> Apply M1's review on the PR: occupied tables still show only `Table 1: C1`; include the type there too.

**What was used or rejected**

The assistant added the same snapshot-based label lookup to the Tables section, so an occupied table now reads `Table 1: C1 (Regular)` while free tables are unchanged, and added CLI test checks for both.

**Files affected**

- `src/restaurantrush/boundary/RestaurantCLI.java`
- `tests/restaurantrush/boundary/RestaurantCLITest.java`

**Human verification**

Re-run `./test.sh` after merging M1's `Customer.typeLabel()`.

**Related commits**

- To be added after commit
