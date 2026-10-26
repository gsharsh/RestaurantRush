# Stage 1 test record — Harshith's contributions

## Whole-game victory and defeat scenarios (M5) — 2026-10-26

- **Status:** Separate reproducible victory and defeat runs passed through the full CLI.
- **Tested commit:** `f21512f` (`main` after PRs #3–#9), clean working tree apart from the new scenario files.
- **Tester:** Devansh Poddar (M5). **Reviewer:** pending.
- **Java/compiler:** OpenJDK 21.0.12 (Homebrew), compiled with `javac --release 17` via `compile.sh`.
- **Machine:** macOS, `~/Developer/RestaurantRush`.

| Test | Exact command | Expected outcome | Observed outcome | Result |
| --- | --- | --- | --- | --- |
| Test build | `./compile.sh --tests` | Production and test sources compile | `Compiled production and test sources`; exit 0 | PASS |
| Victory run | `./run.sh < docs/testing/scenarios/stage-1-victory.txt > docs/testing/scenarios/stage-1-victory.out` | All 18 turns complete; at least 4 paid, at least $45.00, paid average at least 60; VICTORY | 18 of 18 turns; 5 served; 5 paid; 0 unhappy departures; revenue $66.00; average 79.2; `VICTORY` | PASS |
| Defeat run | `./run.sh < docs/testing/scenarios/stage-1-defeat.txt > docs/testing/scenarios/stage-1-defeat.out` | All 18 turns complete with targets missed; DEFEAT | 18 of 18 turns; 0 served; 0 paid; 2 unhappy departures; revenue $0.00; average N/A; `DEFEAT` | PASS |

Saved inputs and full outputs are in `docs/testing/scenarios/`. Each input file holds exactly 36 commands: one Waiter command then one Chef command for each of the 18 turns.

**Victory plan.** Every customer is seated on their arrival turn or as soon as a table frees, ordered the next turn with the Chef cooking immediately, and served on the first turn the dish is READY. Customers 1–5 pay on turns 5, 8, 12, 15 and 18 with recorded satisfaction 84, 84, 76, 76 and 76. Customer 6 (turn-16 arrival) orders on turn 18 and cannot pay, as the brief expects. No command in the victory input was rejected.

**Defeat plan.** The Waiter and Chef `wait` on every turn. Customer 1 reaches 0 satisfaction and leaves at the end of turn 13 (13 × 8 points); customer 2 leaves at the end of turn 16. Nobody is served or pays.

**Brief Section 3.3 check.** In the victory run, customer 3 (Pasta, 2 units) follows the brief's worked example: end-of-turn satisfaction 92 (seated), 84 (ordered, unit 1), 76 (unit 2, READY but not servable that turn), then served with no further loss, READY_TO_PAY the next turn and paid the turn after with 76 recorded.

**Observation (not a rule failure).** `GameController.getSnapshot()` builds customer views from `restaurant.getCustomers()`, so PAID and LEFT customers remain listed in every later start-of-turn display. Using `getActiveCustomers()` would show only customers still in the restaurant. Raised with M4.

## Cashier coverage — 2026-10-26

- **Status:** Production build, test build and all ten current test runners passed.
- **Tested implementation:** Uncommitted working tree on `fix/harshith`, HEAD `3f940d72fa85aae0947016b3a6d30cd2b2bbeecd`, with the user's pending merge of `83c0e9388b4f8d1fcddd2603651be81f0db821a4`. Neither SHA alone identifies the exact tested implementation.
- **Tester/reviewer:** Codex automated execution for Harshith; human/M3 review pending.
- **Java/compiler:** Java 26.0.2 and `javac 26.0.2`; compilation targets Java 17. Java 17 runtime execution is unverified.

| Check | Exact command | Expected and observed outcome | Result |
| --- | --- | --- | --- |
| Production build | `./compile.sh` | Sources compile; exit 0 | PASS |
| Test build | `./compile.sh --tests` | Production and tests compile; exit 0 | PASS |
| Staff regression | `java -ea -cp out/classes restaurantrush.staff.StaffTest` | Existing 92 checks plus 32 new checks pass: `StaffTest: PASS (124 checks)`; exit 0 | PASS |
| Mutation analysis | `python3 /private/tmp/restaurantrush-cashier-test-p9lejr8p/mutation-check.py` | Analysis exits 0: original tests pass both deliberately incorrect implementations, strengthened tests reject both with AssertionError/exit 1 | PASS |

The added case makes customer 1 ready on turn 5 and customer 2 ready on turn 4, then collects on turn 6. Both are explicitly checked as eligible. The older customer must pay first despite having the higher ID and later insertion position. A second turn-6 call must preserve the remaining Customer/Order/Table identity and state, revenue, counts and satisfaction totals/average. The remaining customer pays on turn 7. All earlier timing and subtype tests are unchanged; production sources are unchanged.

The nine other runners were also executed separately, all with exit 0:

```bash
java -ea -cp out/classes restaurantrush.domain.MenuItemTest
java -ea -cp out/classes restaurantrush.domain.OrderTest
java -ea -cp out/classes restaurantrush.domain.CustomerTest
java -ea -cp out/classes restaurantrush.domain.RegularCustomerTest
java -ea -cp out/classes restaurantrush.domain.ActionResultTest
java -ea -cp out/classes restaurantrush.domain.RestaurantStateTest
java -ea -cp out/classes restaurantrush.view.ViewRecordsTest
java -ea -cp out/classes restaurantrush.boundary.RestaurantCLITest
java -ea -cp out/classes restaurantrush.control.TurnManagerTest
```

Observed results respectively: 25, 487, 2292, 110 and 7 checks; 15 Restaurant scenarios; 46, 16 and 650 checks.

Mutation copies were isolated under `/private/tmp/restaurantrush-cashier-test-p9lejr8p/`. Removing only Cashier's same-turn guard and selecting the newest eligible payer each passed the original 92 checks. Each mutation now fails the new case at the corresponding assertion. No substitute collaborators were introduced.

The user's staged entries and pending merge state were preserved exactly. No commit, push or merge was performed by this task. This verification does not establish full Stage 1 acceptance.

## TurnManager guard fixes — 2026-10-26

- **Status at that earlier verification:** Focused verification passed using real staff dependencies; the branch's normal build was blocked by unmerged dependencies. The later Cashier verification above supersedes that build status.
- **Tested implementation:** Uncommitted working tree on `fix/harshith`, base SHA `f2600c45a6dd4c1383a435700f214d2609310848`. This SHA is not the exact tested implementation commit.
- **Additional dependency sources:** `Restaurant`, `Table`, `ActionResult`, `Waiter`, `Chef` and `Cashier` from `feature/s1-staff` at `820720c4382bdbfb340e0706f17304ccb26e1eba`. They were read from a temporary checkout and verified against that Git revision; nothing was copied or merged into this branch.
- **Tester/reviewer:** Codex automated execution; human/M4 review pending.
- **Java/compiler:** Java 26.0.2 and `javac 26.0.2`; compilation targets Java 17. No Java 17 runtime execution is claimed.

| Check | Expected outcome | Observed outcome | Result |
| --- | --- | --- | --- |
| `./compile.sh` | Branch production sources compile | Exit 1: missing Restaurant and staff/result dependencies | FAIL |
| `./compile.sh --tests` | Branch production and tests compile | Exit 1: same missing dependencies | FAIL |
| New regression before the fix | Detect both reviewed bugs | Exit 1: rejected start leaves Waiter available; repeated final advancement produces completed turn 19 | EXPECTED FAILURE |
| Focused compilation after the fix | Current sources/tests compile with the real dependency sources above | Exit 0 | PASS |
| `TurnManagerTest` | Failed-start preservation/retry, repeated final rejection and normal 18-turn progression pass | `TurnManagerTest: PASS (650 checks)`; exit 0 | PASS |
| Existing tests in the same focused build | All six existing runners pass | MenuItemTest 25; OrderTest 487; CustomerTest 2292; RegularCustomerTest 110; ViewRecordsTest 46; RestaurantCLITest 16 checks; every runner exit 0 | PASS |

The exact focused commands executed were:

```bash
javac --release 17 -d /private/tmp/restaurantrush-turn-guards-nq0sb0z_/classes-before @/private/tmp/restaurantrush-turn-guards-nq0sb0z_/sources-before.txt
java -ea -cp /private/tmp/restaurantrush-turn-guards-nq0sb0z_/classes-before restaurantrush.control.TurnManagerTest
javac --release 17 -d /private/tmp/restaurantrush-turn-guards-nq0sb0z_/classes-after @/private/tmp/restaurantrush-turn-guards-nq0sb0z_/sources-after.txt
java -ea -cp /private/tmp/restaurantrush-turn-guards-nq0sb0z_/classes-after restaurantrush.control.TurnManagerTest
```

The temporary argument files list all current production/test sources plus the six dependency sources above. Each existing runner was executed separately with `java -ea -cp /private/tmp/restaurantrush-turn-guards-nq0sb0z_/classes-after` and its fully qualified class name listed in the earlier M1 record below (`restaurantrush.domain.*`), plus `restaurantrush.view.ViewRecordsTest` and `restaurantrush.boundary.RestaurantCLITest`.

Once those dependencies are integrated, rerun normally with `./compile.sh --tests` followed by `java -ea -cp out/classes restaurantrush.control.TurnManagerTest`. The test uses public APIs and checked assertions; failures produce a nonzero exit without reflection, test hooks or substitute production classes.

This change does not establish full Stage 1 acceptance. No new whole-game victory/defeat run, commit, push or merge was performed for this fix. The existing deletion of `control/.gitkeep` was preserved.

## Historical M1 verification — 2026-10-24

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

Whole-game victory and defeat runs are recorded in the M5 section at the top of this file. The note below refers to the earlier M1-only verification.

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
