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
