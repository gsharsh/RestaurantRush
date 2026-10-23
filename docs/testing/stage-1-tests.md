# Stage 1 test record

- **Status:** M1 foundation checks passed; full Stage 1 acceptance is pending.
- **Tested commit:** Uncommitted working tree on `feature/s1-harshith`, based on
  `aaf89d19a63dd2555e3f3ffb0393731d11843ffe`. These results do not describe a
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
| M1 foundation suite | `java -ea -cp out/classes restaurantrush.domain.M1DomainTests` | All foundation checks pass | `M1DomainTests: PASS (39 checks)`; exit 0 | PASS |
| Launch | `./run.sh` | Full Stage 1 game launches once the application exists | Not run; application and gameplay are outside this feature | NOT RUN |

## M1 foundation coverage

The runner executes seven focused test methods containing 39 checked assertions:

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

## Reproducible scenarios

No victory or defeat scenarios were run. Only the M1 foundation exists on this
branch; full game scenarios require the remaining owners' implementations.

## Defects and limitations

- No failures were observed in the executed checks.
- This feature adds only `MenuItem`, `CustomerStatus` and `OrderStatus` to
  production. `Order`, customer classes and peer production types remain outside
  this feature.
- M2's `Restaurant` will construct the production menu; the three agreed menu
  offerings here are test fixtures only.
- Full Stage 1 integration, launch, timing, scenario and human-review evidence
  remains pending. No tag-gate completion is claimed.

## Tag-gate review

- [ ] Exact committed release-candidate SHA recorded
- [ ] All required Stage 1 tests and scenarios executed
- [x] Observed M1 foundation results recorded
- [ ] Another member reviewed the evidence
- [ ] UML and class contract match the release-candidate code
- [ ] Working tree is clean before tagging
