# Stage 1 test record — M1 domain

- **Status:** All four self-contained M1 class tests passed. Full Stage 1 acceptance is pending.
- **Tested implementation:** Uncommitted working tree on `feature/s1-harshith`.
- **Base SHA:** `b6db48f378e73852a7e039efb8e877c38b634757`; this is not the exact tested implementation commit.
- **Tester:** Codex, automated execution for Harshith (M1).
- **Reviewer:** Human review pending.
- **Date:** 2026-10-24 (Asia/Singapore).
- **Java/compiler:** Java 26.0.2 and `javac 26.0.2`; compilation targets Java 17 with `--release 17`. Execution on Java 17 has not been verified.
- **Working directory:** `/Users/gsharsh/RestaurantRush`.

| Test | Exact command | Expected outcome | Observed outcome | Result |
| --- | --- | --- | --- | --- |
| Production build | `./compile.sh` | All current production sources compile | Compiled production sources; exit 0 | PASS |
| Test build | `./compile.sh --tests` | Production and test sources compile | Compiled production and test sources; exit 0 | PASS |
| MenuItem suite alone | `java -ea -cp out/classes restaurantrush.domain.MenuItemTest` | Menu values and validation pass | `MenuItemTest: PASS (25 checks)`; exit 0 | PASS |
| Order suite alone | `java -ea -cp out/classes restaurantrush.domain.OrderTest` | Order enum, preparation, billing and transitions pass | `OrderTest: PASS (487 checks)`; exit 0 | PASS |
| Customer suite alone | `java -ea -cp out/classes restaurantrush.domain.CustomerTest` | Customer enum, lifecycle, timing, state preservation and cross-class handoffs pass | `CustomerTest: PASS (2292 checks)`; exit 0 | PASS |
| RegularCustomer suite alone | `java -ea -cp out/classes restaurantrush.domain.RegularCustomerTest` | Selection, quotes and rule validation pass | `RegularCustomerTest: PASS (110 checks)`; exit 0 | PASS |
| Whole-game launch | `./run.sh` | Complete application launches after integration | Not run: RestaurantRushApp and peer implementations are absent | NOT RUN |

To rerun from the repository root:

```bash
./compile.sh
./compile.sh --tests
java -ea -cp out/classes restaurantrush.domain.MenuItemTest
java -ea -cp out/classes restaurantrush.domain.OrderTest
java -ea -cp out/classes restaurantrush.domain.CustomerTest
java -ea -cp out/classes restaurantrush.domain.RegularCustomerTest
```

The test build compiles but does not execute tests. Both builds clean
`out/classes`, so compile tests before running the classes. Checked failures throw
`AssertionError` and produce a nonzero exit even without `-ea`.

## M1 coverage

| Area | Checked behavior |
| --- | --- |
| Menu and enums | Agreed Salad/Burger/Pasta values; name preservation; invalid construction; zero price; exact enum constants and order |
| Order lifecycle | One/two-unit preparation; readiness; service/payment; cancellation from PLACED/PREPARING/READY; terminal restrictions; nonnegative counter |
| Billing and identity | Explicit locked quotes, including zero; normal Customer-derived quotes; shared MenuItem and Order identities; independent preparation counters |
| Customer rules | Selection, quote and waiting loss through Customer references; IDs 1–6 select the required cycle; invalid menu/item arguments |
| Customer lifecycle | Exact initialization; single matching-order assignment; Order-before-Customer service/payment; positive IDs/turns without game-specific upper limits |
| Timing and satisfaction | Same/earlier-turn eating no-ops; later completion and stable timestamps; loss only while WAITING/SEATED; floor zero without automatic departure |
| Abandonment | WAITING/SEATED departure, including positive satisfaction; cancellation of unfinished and READY food |
| Collaboration failures | Rejected operations preserve customer, associated/candidate Order and menu state, values and object identities |

The four class tests retain all 28 original test groups and their unchanged test
bodies, totaling 2914 checks. Enum tests live with the corresponding class;
customer/order collaboration scenarios live in CustomerTest. Each class runs
independently and keeps its required assertion helpers private. The check count
includes repeated field and identity assertions, not that many independent
gameplay scenarios. Source review
also confirmed the exact contracted APIs/modifiers and the actual call from
`applyWaitingLoss()` to the overridden `waitingLossPerTurn()` rule.

## Reproducible scenarios

No whole-game victory/defeat scenario was executed. These are direct M1 domain
tests; they do not establish arrivals, staff limits, Cashier selection/eligibility,
Restaurant registration, queue/table cleanup, statistics or CLI behavior.

## Defects and limitations

- No failures in the current production build, test build or four class tests. Consolidation changed no production behavior or test expectation; the separate enum/collaboration files, aggregate runner and shared support file were removed.
- Invalid supplied method arguments use IllegalArgumentException; lifecycle misuse uses IllegalStateException. Multiple-failure precedence is unspecified.
- Acceptance adds no Order-status guard. Abandonment cancels attached PLACED/PREPARING/READY orders; other attached states remain unchanged before LEFT.
- Order's defensive invalid-item-unit branch cannot be reached through valid MenuItem construction/final getters; tests do not bypass those invariants.
- Contract Section 4.3's prose says `item.priceCents()` while its exact API specifies `getPriceCents()`. The agreed getter is preserved; shared editorial correction is pending.
- A previous documentation check had 15 missing-summary warnings, which were repaired. Generated HTML, the standalone demo and the separate M1 handoff were removed; none is required to rerun these tests.
- Peer integration, human review, shared Draw.io source/export and full Stage 1 acceptance remain pending. No shared contract or peer code was rewritten.

## Tag-gate review

- [ ] Exact committed release-candidate SHA recorded
- [ ] Required full Stage 1 tests and victory/defeat scenarios executed
- [x] Current M1 test results recorded
- [ ] Another member reviewed the evidence
- [ ] Shared UML matches the release-candidate code
- [ ] Working tree is clean before tagging
