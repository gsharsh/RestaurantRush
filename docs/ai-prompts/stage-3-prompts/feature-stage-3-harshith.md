# AI prompt record

- **Branch:** `feature/stage-3-harshith`
- **Member:** Harshith (M1)
- **Stage:** Stage 3
- **Related pull request:** None created for this work.

## Prompt 1 — ComboMeal and focused domain tests

**Prompt**

> Implement `ComboMeal` exactly to the Stage 3 contract, with focused Javadoc and `ComboMealTest`. Preserve component identity, inherit `MenuItem` validation, and use overflow-safe integer arithmetic with half-up rounding.
>
> Test the 2430-cent/3-unit combo, inherited getters, component identity, invalid inputs, zero prices, rounding and integer boundaries—including a combined price above `Integer.MAX_VALUE` whose discounted result fits.
>
> Run `ComboMealTest`, `MenuItemTest` and `OrderTest` individually using `./test.sh <fully-qualified-class>`. Report results and the M2 registration handoff. Stop here.

**What was used or rejected**

Implemented the `ComboMeal` portion of the [Stage 3 contract](../../class-contracts/stage-3-class-contracts.md): a final subclass of `MenuItem` with two private final component references and the contracted constructor, `getFirst()` and `getSecond()`. Components retain their original identity, including when shared or repeated. Name validation and the name/price/preparation getters remain inherited from `MenuItem`.

The constructor rejects null components with `IllegalArgumentException`, sums preparation units using `long`, and computes the combo price as `(9L * ((long) first.getPriceCents() + second.getPriceCents()) + 5L) / 10L`. Each computed result is checked before conversion to `int`. The price range check applies after discounting, so a combined component price above `Integer.MAX_VALUE` remains valid when its discounted result fits. Focused Javadoc documents these rules and exceptions.

Added tests in the existing offline `main`/`check` style with literal expected prices. Coverage includes Salad + Pasta at 2430 cents and 3 preparation units, inherited getters, component identity and unchanged values, null components, invalid names, exact display-name preservation, zero prices, half-up rounding of the combined total, and price/preparation boundaries. Boundary examples include 2,200,000,000 combined cents yielding 1,980,000,000 cents, an exact maximum discounted result, and the next combined-cent value being rejected. An `Order` scenario verifies the combo remains one shared `MenuItem`, needs three preparation actions and retains its locked bill through payment.

**M2 registration handoff**

M2 (Vedavit) registers `new ComboMeal("Salad + Pasta Combo", salad, pasta)` fourth in `Restaurant`, using its existing Salad and Pasta instances. Preserve immutable menu exposure and update menu-size/customer-selection expectations. Registration and M2 arithmetic review remain pending; this implementation changed only the new combo class and its test.

**Files affected**

- `src/restaurantrush/domain/ComboMeal.java` — added
- `tests/restaurantrush/domain/ComboMealTest.java` — added
- This branch prompt record — added on request after implementation.

**Human verification**

Pending; automated results do not establish human approval. The following automated executions were observed on 2026-11-07 in the uncommitted working tree based on `450e58df7d235d474cc667dd1c240119ec5772cd`; each command exited 0:

| Command | Observed result |
| --- | --- |
| `./test.sh restaurantrush.domain.ComboMealTest` | PASS — 114 checks |
| `./test.sh restaurantrush.domain.MenuItemTest` | PASS — 25 checks |
| `./test.sh restaurantrush.domain.OrderTest` | PASS — 487 checks |

Each command compiled production and test sources before executing the named test individually. Compiler and runtime were Java 26.0.2; compilation used `--release 17`. Execution on a Java 17 runtime and full Stage 3 integration remain unverified. Final source/test diff review and whitespace checks found no issues. These executions occurred during implementation; this subsequent prompt-record edit did not rerun them.

**Related commits**

- None created for Prompt 1. Working-tree base: `450e58df7d235d474cc667dd1c240119ec5772cd`.
- No commit, push, merge, pull request or tag mutation was performed by the assistant.

## Prompt 2 — Customer waiting-satisfaction recovery

**Prompt**

> Add `public final void recoverWaitingSatisfaction(int amount)` to `Customer`: reject negatives; recover only WAITING/SEATED customers; cap at 100 safely; leave other state unchanged.
>
> Extend `CustomerTest` using existing helpers. Cover all customer types, eligible order states including READY, excluded lifecycle states, zero, negatives, overflow and unchanged order/timestamp fields. Preserve normal waiting losses.
>
> Run `CustomerTest`, `RegularCustomerTest`, `VIPCustomerTest` and `CriticCustomerTest`. Report results. Leave Happy Hour policy and recovery timing to M2/M4. Stop here.

**What was used or rejected**

Added the final shared recovery method and focused Javadoc to `Customer`. Negative amounts throw `IllegalArgumentException` before checking lifecycle eligibility, including for excluded states. Only `WAITING` and `SEATED` customers recover, using `(int) Math.min(100L, (long) satisfaction + amount)` to cap safely at 100. Zero is valid; repeated recovery calls are allowed. Eligibility depends on customer status, so a seated customer with a READY order still qualifies. The method changes only satisfaction and leaves orders, timestamps and lifecycle state unchanged.

Extended `CustomerTest` through helpers accepting actual Regular, VIP and Critic objects as `Customer` references. Reused `checkCustomer`, `snapshotOrder`, `expectCustomerNoOp`, `expectUnchangedIllegalArgument` and `customerOrderInState`. Coverage includes waiting and seated customers without orders, seated customers with every accepted order state, recovery from zero, repeated recovery, exact and excess capping, and `Integer.MAX_VALUE` recovery from positive satisfaction. Tests reject `-1` and `Integer.MIN_VALUE` and accept zero across all lifecycle states. EATING, READY_TO_PAY, PAID and LEFT remain unchanged, with attached orders and recorded timestamps checked through existing snapshots.

Existing waiting-loss code and tests were preserved. New checks also apply normal waiting losses after recovery and verify the unchanged Regular/VIP/Critic losses of 8/5/12, including while food is READY. The implementation added no Happy Hour policy or turn-timing behavior.

**M2/M4 handoff**

The generic recovery API is available for integration. Happy Hour policy remains with M2 (Vedavit); recovery scheduling and once-per-active-turn enforcement remain with M4 (Viti) under the Stage 3 turn contract. Those integrations and their timing tests remain pending.

**Files affected**

- `src/restaurantrush/domain/Customer.java` — added recovery method and Javadoc
- `tests/restaurantrush/domain/CustomerTest.java` — extended focused recovery coverage
- This branch prompt record — Prompt 2 appended on request after implementation.

**Human verification**

Pending; automated results do not establish human approval. The following automated executions were observed on 2026-11-07 during recovery implementation; each command exited 0:

| Command | Observed result |
| --- | --- |
| `./test.sh restaurantrush.domain.CustomerTest` | PASS — 7986 checks |
| `./test.sh restaurantrush.domain.RegularCustomerTest` | PASS — 111 checks |
| `./test.sh restaurantrush.domain.VIPCustomerTest` | PASS — 826 checks |
| `./test.sh restaurantrush.domain.CriticCustomerTest` | PASS — 55 checks |

Each command compiled production and test sources before executing the named test individually. Compiler and runtime were Java 26.0.2; compilation used `--release 17`. Execution on a Java 17 runtime and Happy Hour integration remain unverified. Final source/test diff review found no issues and `git diff --check` passed. These executions checked the uncommitted recovery changes; this subsequent prompt-record edit did not rerun them.

**Related commits**

- None created for Prompt 2. Recovery changes remain uncommitted against the current branch base `ebe0062e37ee85d0b677a65803b0a2de369a3dc7`.
- No commit, push, merge, pull request or tag mutation was performed by the assistant.
