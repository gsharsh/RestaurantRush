# Stage 1 test record

## Current baseline and verification

- **Release status:** The published `stage-1` tag was removed on 2026-10-29. Stage 1 completion and cleanup await the user's review; do not recreate the tag without explicit user approval.
- **Tested source:** Commit `698a85a210ea5bc549ee9b49f328985eb758b5cc`; subsequent changes on `refactor/stage-1` are documentation only.
- **Date and tester:** 2026-10-29 (Asia/Singapore), Codex automated execution for Harshith.
- **Java/compiler:** Java 26.0.2 and `javac 26.0.2`; compilation targets Java 17 with `--release 17`. Execution on a Java 17 runtime was not verified.
- **Status:** Production/test compilation, all ten test runners and both full-game scenario comparisons passed with exit 0. Another member's review is not recorded here; automated results do not establish that approval.

## Commands and observed results

Run the build and the ten separate test commands in the [README](../../README.md#compile-and-run). `./compile.sh --tests` compiles tests without executing them; each runner must be run separately. The test build cleans `out/classes`, so compile tests before running them.

| Check | Observed result |
| --- | --- |
| `./compile.sh` | PASS; production sources compile |
| `./compile.sh --tests` | PASS; production and test sources compile |
| `MenuItemTest` | PASS; 25 checks |
| `OrderTest` | PASS; 487 checks |
| `CustomerTest` | PASS; 2292 checks |
| `RegularCustomerTest` | PASS; 110 checks |
| `ActionResultTest` | PASS; 7 checks |
| `RestaurantStateTest` | PASS; 15 scenarios, 0 failures |
| `StaffTest` | PASS; 124 checks |
| `TurnManagerTest` | PASS; 650 checks |
| `ViewRecordsTest` | PASS; 46 checks |
| `RestaurantCLITest` | PASS; 16 checks |

Counts include repeated field and identity assertions; they are not counts of independent gameplay scenarios. `RestaurantCLITest` checks outcome rendering and rejection of unfinished/null snapshots; it does not automate input parsing or retry coverage.

## Reproducible victory and defeat

From the repository root:

```bash
./compile.sh
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/scenarios/stage-1-victory.txt | diff docs/testing/scenarios/stage-1-victory.out -
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/scenarios/stage-1-defeat.txt | diff docs/testing/scenarios/stage-1-defeat.out -
```

Both comparisons passed on 2026-10-29: output matched the saved files exactly. Each input contains 36 commands, one Waiter and one Chef choice per turn. Direct Java execution keeps machine-specific compilation paths out of the transcripts.

| Metric | Victory expected and observed | Defeat expected and observed |
| --- | --- | --- |
| Turns completed | 18 | 18 |
| Result | VICTORY | DEFEAT |
| Customers served / paid | 5 / 5 | 0 / 0 |
| Unhappy departures | 0 | 2 |
| Revenue | $66.00 | $0.00 |
| Paid-customer average satisfaction | 79.2 | N/A |

**Victory plan:** Seat customers as tables become available, take their orders and cook immediately, then serve when READY. Customers 1–5 pay on turns 5, 8, 12, 15 and 18 with satisfaction 84, 84, 76, 76 and 76. The turn-16 arrival cannot pay before the game ends. Customer 3's Pasta follows the brief's example: 92 after seating, 84 after ordering/unit 1, 76 after unit 2, service on a later turn, eating completion one turn later and payment one further turn later.

**Defeat plan:** Both staff wait throughout. Customers 1 and 2 reach zero satisfaction and leave at the end of turns 13 and 16. Nobody is served or pays.

## Historical regression notes

These observations describe earlier working trees; the current verification above supersedes their build/integration status.

- **2026-10-28 — Release-candidate checks (Vedavit, M2):** OpenJDK 21.0.12, targeting Java 17. All ten runners and both scenarios passed against `54dbebd` plus `docs/s1-release-evidence`, later merged as `284b259d`. Changes removed an unused arrival import, clarified two controller failure messages and refreshed the transcripts after PR #14 (`60cab62`) changed snapshots to active customers only. The former published tag pointed to `698a85a`, not the earlier candidate; it has since been removed for user review.
- **2026-10-28 — Manual CLI and UML checks:** The recorded checks covered unknown/malformed commands and invalid seat/order/serve/cook requests retrying without consuming a task; an unserved Salad customer abandoning on turn 13 and freeing its table; and EOF stopping without a fabricated outcome. No Stage 2/3 implementation was present. The class diagram was checked against Stage 1 fields and public methods, excluding private helpers and compiler-generated members. Human review remained unrecorded.
- **2026-10-26 — First full-game scenarios (Devansh Poddar, M5):** Commit `f21512f`, OpenJDK 21.0.12 targeting Java 17. Both saved playthroughs produced the same final metrics shown above. The later transcript refresh changed the customer display and removed the compilation-path line, without changing the game results.
- **2026-10-26 — Cashier regressions (Codex for Harshith):** Uncommitted `fix/harshith` work based on `3f940d72fa85aae0947016b3a6d30cd2b2bbeecd`, with pending merge `83c0e9388b4f8d1fcddd2603651be81f0db821a4`; neither SHA alone identifies that tested tree. Added assertions for two eligible payers with different ready turns and a repeated collection attempt in the same turn. The strengthened tests rejected both deliberately incorrect variants: newest-first selection and removal of the same-turn payment guard. All ten runners passed; human/M3 review was pending.
- **2026-10-26 — TurnManager guards (Codex for Harshith):** Uncommitted work based on `f2600c45a6dd4c1383a435700f214d2609310848`, using real Restaurant/staff dependencies from `820720c4382bdbfb340e0706f17304ccb26e1eba`. Before the fix, regressions exposed staff availability after a failed start and repeated final advancement producing turn 19. After the fix, 650 TurnManager checks and the existing focused runners passed. Dependencies were not yet merged in that earlier branch; the integrated build now passes. Human/M4 review was pending.
- **2026-10-24 — M1 foundations (Codex for Harshith):** Uncommitted `feature/s1-harshith` work based on `b6db48f378e73852a7e039efb8e877c38b634757`. The four domain runners passed 2914 checks covering menu/enums, locked billing, lifecycle/timing, polymorphic rules, abandonment and rejection without mutation. This was domain verification before the whole-game application existed, using Java 26.0.2 targeting Java 17; it did not establish full-game acceptance or human review.
