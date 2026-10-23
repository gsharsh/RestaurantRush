# Stage 1 test record

- **Status:** All accumulated M1 domain checks passed; full Stage 1 acceptance is pending.
- **Tested commit:** Uncommitted working tree on `feature/s1-harshith`, based on
  `b17df6781be0f1a86b84c397502312219c61e980`. These results do not describe a
  committed release candidate.
- **Tester:** Codex (automated execution for Harshith's M1 feature).
- **Reviewer:** Human review pending.
- **Date:** 2026-10-24 (Asia/Singapore).
- **Java version:** `javac 26.0.2`, compiled with `--release 17` by `compile.sh`;
  executed with Java 26.0.2. Java 17 runtime execution was not performed.
- **Working directory:** Repository root (`/Users/gsharsh/RestaurantRush`).

| Test | Command or repeatable steps | Expected outcome | Observed outcome | Result |
| --- | --- | --- | --- | --- |
| Compile production and tests | `./compile.sh --tests` | All current sources compile for Java 17 | `Compiled production and test sources to /Users/gsharsh/RestaurantRush/out/classes`; exit 0 | PASS |
| Complete M1 suite (foundation, Order and customers) | `java -ea -cp out/classes restaurantrush.domain.M1DomainTests` | All current M1 checks pass | `M1DomainTests: PASS (2424 checks)`; exit 0 | PASS |
| Launch | `./run.sh` | Full Stage 1 game launches once the application exists | Not run; application and gameplay are outside this feature | NOT RUN |

## M1 foundation coverage

The original foundation suite contains seven focused test methods with 39 checked
assertions. It passed in the prior feature's uncommitted working tree based on
`aaf89d19a63dd2555e3f3ffb0393731d11843ffe`. Those tests are preserved:

- Exact getter values for Salad (900 cents, 1 unit), Burger (1500 cents, 1 unit)
  and Pasta (1800 cents, 2 units).
- `IllegalArgumentException` for null, empty, space-only, mixed-whitespace and
  Unicode-whitespace names.
- Exact preservation of a valid display name containing surrounding whitespace.
- `IllegalArgumentException` for negative prices, including `Integer.MIN_VALUE`;
  valid construction with a zero price.
- `IllegalArgumentException` for zero and negative preparation units, including
  `Integer.MIN_VALUE`.
- Exact `CustomerStatus` constant count, names and declared order.
- Exact `OrderStatus` constant count, names and declared order.

The tests call the public API and use explicit checks that throw `AssertionError`
on failure. An uncaught failure gives a nonzero process exit; checks do not depend
on enabling Java assertions. Later M1 features can add focused test methods to
the same runner.

## M1 Order coverage

The prior Order feature executed 14 focused test methods with 519 checked
assertions in an uncommitted working tree based on
`7754d4883bd9d38dc72a7fb3e2b6cd9653afbb39`: 39 foundation checks and 480 Order
checks. Those tests are retained. Order coverage includes initial values and
constructor validation; one-unit and two-unit preparation; exactly one unit
consumed per preparation request;
`canPrepare()` and `isReady()` in each lifecycle state; service and payment;
cancellation from `PLACED`, `PREPARING` and `READY`; terminal restrictions; and
unchanged observable state after invalid or repeated operations.

Shared-item checks use two orders referring to the same `MenuItem` while retaining
independent preparation counters. Identity and the locked bill stay unchanged
through lifecycle operations.

Quote-storage tests deliberately supply values different from the menu price,
including zero. They test the constructor's explicit quote boundary, not the
normal Stage 1 price rule. Waiter must later supply
`customer.quotePriceCents(item)` in the actual customer flow.

`Order` defensively validates positive item preparation units. A nonpositive-unit
`MenuItem` cannot be created through the agreed public API because its constructor
rejects it and its getters are final. The foundation tests cover that rejection;
no reflection, mutable test hooks or subclasses are used to bypass it.

## M1 customer coverage

The complete runner executed 25 focused test methods with 2424 checked assertions:
39 foundation checks, 480 Order checks and 1905 customer checks. All passed.

Customer checks exercise `RegularCustomer` through `Customer` references for the
three polymorphic rules and shared lifecycle operations. They cover constructor
validation and initial values, deterministic selection and full-price quoting,
required null/empty input validation, selected-object identity and an unchanged
menu.

Lifecycle coverage includes the full route to `PAID`, shared Order identity,
null/mismatched/second-order rejection, order-before-customer service/payment,
invalid and repeated transitions preserving customer and attached-order state,
same-turn eating no-ops, later-turn completion and stable recorded timestamps.
Positive IDs and turns are not limited to the arrival schedule or 18-turn game.

Waiting-loss checks cover both eligible states, the floor at zero without
automatic departure, and no loss in `EATING`, `READY_TO_PAY`, `PAID` or `LEFT`.
Abandonment checks cover waiting/seated customers with positive satisfaction and
cancellation of attached `PLACED`, `PREPARING` and `READY` orders.

The request's "where applicable" is interpreted as skipping cancellation for an
attached `SERVED`, `PAID` or already `CANCELLED` order before the eligible customer
becomes `LEFT`. No order-status precondition is added to `acceptOrder()`.
Invalid supplied arguments use `IllegalArgumentException`; invalid lifecycle
operations use `IllegalStateException`, without promising validation precedence
when multiple preconditions are violated.

## Reproducible scenarios

No victory or defeat scenarios were run. This branch contains the six M1 domain
types; full game scenarios require peer implementations and integration.

## Defects and limitations

- No failures were observed in the executed compilation or complete M1 runner.
- This feature adds only `Customer` and `RegularCustomer` to production, building
  on the unchanged foundation and Order types. Peer production types remain
  outside this feature.
- M2's `Restaurant` will construct the production menu; the three agreed menu
  offerings here are test fixtures only.
- Full Stage 1 integration, launch, timing, scenario and human-review evidence
  remains pending. No tag-gate completion is claimed.

## Tag-gate review

- [ ] Exact committed release-candidate SHA recorded
- [ ] All required Stage 1 tests and scenarios executed
- [x] Observed complete M1 customer-feature results recorded
- [ ] Another member reviewed the evidence
- [ ] UML and class contract match the release-candidate code
- [ ] Working tree is clean before tagging
