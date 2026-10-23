# Stage 1 test record

- **Status:** M1 foundation and Order checks passed; full Stage 1 acceptance is pending.
- **Tested commit:** Uncommitted working tree on `feature/s1-harshith`, based on
  `7754d4883bd9d38dc72a7fb3e2b6cd9653afbb39`. These results do not describe a
  committed release candidate.
- **Tester:** Codex (automated execution for Harshith's M1 feature).
- **Reviewer:** Human review pending.
- **Date:** 2026-10-23 (Asia/Singapore).
- **Java version:** `javac 26.0.2`, compiled with `--release 17` by `compile.sh`;
  executed with Java 26.0.2. Java 17 runtime execution was not performed.
- **Working directory:** Repository root (`/Users/gsharsh/RestaurantRush`).

| Test | Command or repeatable steps | Expected outcome | Observed outcome | Result |
| --- | --- | --- | --- | --- |
| Compile production and tests | `./compile.sh --tests` | All current sources compile for Java 17 | `Compiled production and test sources to /Users/gsharsh/RestaurantRush/out/classes`; exit 0 | PASS |
| Complete M1 suite (foundation and Order) | `java -ea -cp out/classes restaurantrush.domain.M1DomainTests` | All current M1 checks pass | `M1DomainTests: PASS (519 checks)`; exit 0 | PASS |
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

The complete runner executed 14 focused test methods with 519 checked assertions:
39 retained foundation checks and 480 Order checks. The extension covers initial
values and constructor validation; one-unit
and two-unit preparation; exactly one unit consumed per preparation request;
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

## Reproducible scenarios

No victory or defeat scenarios were run. This branch contains the M1 foundation
and Order; full game scenarios require the remaining M1 and peer implementations.

## Defects and limitations

- No failures were observed in the executed compilation or complete M1 runner.
- This feature adds only `Order` to production, building on the unchanged
  `MenuItem`, `CustomerStatus` and `OrderStatus`. Customer classes and peer
  production types remain outside this feature.
- M2's `Restaurant` will construct the production menu; the three agreed menu
  offerings here are test fixtures only.
- Full Stage 1 integration, launch, timing, scenario and human-review evidence
  remains pending. No tag-gate completion is claimed.

## Tag-gate review

- [ ] Exact committed release-candidate SHA recorded
- [ ] All required Stage 1 tests and scenarios executed
- [x] Observed current M1 foundation and Order results recorded
- [ ] Another member reviewed the evidence
- [ ] UML and class contract match the release-candidate code
- [ ] Working tree is clean before tagging
