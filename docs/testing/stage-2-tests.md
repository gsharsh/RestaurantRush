# Stage 2 test record

- **Status:** Partial — shared customer-type label API only; Stage 2 release checks remain pending.
- **Tested commit:** Uncommitted working tree based on `550ae6f8b5dffc2c4b614df65153783f22b3a513`; no exact implementation-commit verification claimed.
- **Branch:** `feature/stage-2-harshith`
- **Tester:** Codex automated execution for Harshith (M1)
- **Reviewer:** Pending; M4 (Viti) for QuotedCustomer and M3 (Hunar) for DiscountedCustomer compatibility edits.
- **Date:** 2026-10-30
- **Java version:** `java` and `javac` 26.0.2; scripts compile with `--release 17`. Execution on a Java 17 runtime was not tested.

## Mixed-customer victory and defeat scenarios (M5) — 2026-11-05

- **Status:** Separate reproducible Stage 2 victory and defeat runs passed through the full CLI with Regular, VIP and Critic customers.
- **Tested commit:** `d050b2d` (`main` after M4's arrival schedule), with only the new scenario files added.
- **Tester:** Devansh Poddar (M5). **Reviewer:** pending.
- **Java/compiler:** OpenJDK 21.0.12 (Homebrew), compiled with `javac --release 17` via `compile.sh`.
- **Machine:** macOS, `~/Developer/RestaurantRush`.

| Test | Exact command | Expected outcome | Observed outcome | Result |
| --- | --- | --- | --- | --- |
| All test runners | `./test.sh` | Every existing and new Stage 2 test class passes | All 14 test classes passed, including `VIPCustomerTest`, `CriticCustomerTest` and `ArrivalScheduleTest` | PASS |
| Victory run | `./run.sh < docs/testing/stage-2-testscript/stage-2-victory.txt > docs/testing/stage-2-testscript/stage-2-victory.out` | 18 turns; at least 4 paid, at least $45.00, paid average at least 60; VICTORY | 18 of 18 turns; 5 served; 4 paid; 0 unhappy departures; revenue $58.50; average 75.5; `VICTORY` | PASS |
| Defeat run | `./run.sh < docs/testing/stage-2-testscript/stage-2-defeat.txt > docs/testing/stage-2-testscript/stage-2-defeat.out` | 18 turns with targets missed; DEFEAT | 18 of 18 turns; 0 served; 0 paid; 2 unhappy departures; revenue $0.00; average N/A; `DEFEAT` | PASS |

Each input file holds exactly 36 commands (one Waiter command, then one Chef command, for each of the 18 turns). No command in either run was rejected. Arrivals follow the agreed mapping: customer 2 (turn 4) is VIP and customer 4 (turn 10) is Critic; the rest are Regular. Every customer is listed with its type label, e.g. `C2 (VIP)`, `C4 (Critic)`.

**Victory plan and Stage 2 behaviour shown.**

| Customer | Type | Order | Bill | Paid on turn | Satisfaction recorded |
| --- | --- | --- | --- | --- | --- |
| 1 | Regular | Salad | $9.00 | 5 | 84 |
| 2 | VIP | Burger | **$13.50** (10% off $15.00) | 8 | **90** (loses 5 per waiting turn) |
| 3 | Regular | Pasta | $18.00 | 12 | 76 |
| 4 | Critic | **Pasta** (a Regular customer 4 would choose Salad) | $18.00 | 16 | **52** (loses 12 per waiting turn: 88, 76, 64, 52) |
| 5 | Regular | Burger | — | served turn 17, cannot pay by turn 18 | — |

Customer 6 (turn-16 arrival) is seated on turn 18 and cannot pay, as the brief expects. Revenue is 900 + 1350 + 1800 + 1800 = 5850 cents; the paid average is (84 + 90 + 76 + 52) / 4 = 75.5.

**Defeat plan.** The Waiter and Chef `wait` on every turn. Customer 1 (Regular, 8 per turn) leaves at the end of turn 13 and customer 4 (Critic, 12 per turn) leaves at the end of turn 18, nine turns after arriving. Customer 2 (VIP, 5 per turn) is still waiting at satisfaction 30 at the start of turn 18, showing the slower VIP loss.

## Shared label API — Prompt 1

All executed commands below exited 0. Test classes are the existing self-contained runners; no new harness was added.

| Test | Command or repeatable steps | Expected outcome | Observed outcome | Result |
| --- | --- | --- | --- | --- |
| Production compile | `./compile.sh` | Production sources compile targeting Java 17 | Compiled production sources to `out/classes` | PASS |
| Production and test compile | `./compile.sh --tests` | All sources, including concrete test Customer subclasses, compile | Compiled production and test sources to `out/classes` | PASS |
| Regular rules and label | `java -ea -cp out/classes restaurantrush.domain.RegularCustomerTest` | Exact `Regular` label through Customer; existing selection, validation, full prices and loss of 8 retained | `RegularCustomerTest: PASS (111 checks)` | PASS |
| Shared lifecycle | `java -ea -cp out/classes restaurantrush.domain.CustomerTest` | Existing lifecycle and customer/order handoffs retained | `CustomerTest: PASS (2292 checks)` | PASS |
| Controller compatibility | `java -ea -cp out/classes restaurantrush.control.GameControllerTest` | Existing QuotedCustomer behaviour and assertions retained | `GameControllerTest: PASS (753 checks)` | PASS |
| Staff compatibility | `java -ea -cp out/classes restaurantrush.staff.StaffTest` | Existing DiscountedCustomer behaviour and assertions retained | `StaffTest: PASS (124 checks)` | PASS |

## Reproducible scenarios

Mixed-customer victory and defeat runs are recorded in the M5 section above. The note below refers to the earlier label-API verification.

No new gameplay scenarios were run for this API-only change. Mixed-customer victory/defeat evidence remains with M5 after VIP/Critic and M4 arrival integration. The Stage 1 scenario files remain historical evidence.

## Defects and limitations

No failure was observed in the checks above. The remaining existing test runners and full-game scenarios were not executed in this prompt.

The only concrete Customer subclasses found were RegularCustomer, GameControllerTest.QuotedCustomer and StaffTest.DiscountedCustomer; no anonymous implementations or actual CriticCustomer were present. The two fixtures gained constant presentation labels only, preserving their existing gameplay methods and assertions. Owner review remains pending as listed above.

This partial API compiles independently. VIP (`VIP`, M1), Critic (`Critic`, M2), M4 arrival/controller integration and M5 `CustomerView.type`/CLI consumption remain outstanding. UML coordination and the shared README transition record remain pending with M5; the source API is documented and the branch prompt record supplies M1's UML operation handoff. No Stage 2 release or mixed-customer validation is claimed.

## Tag-gate review

- [ ] Exact implementation commit SHA verified
- [ ] All required Stage 2 tests and victory/defeat scenarios executed
- [ ] Another member reviewed the evidence and compatibility edits
- [ ] Shared UML matches the implemented Stage 2 code
- [ ] Working tree is clean before tagging

The published `stage-1` tag remains at `1b9ebf92330316628010880245f98a67416aaef9`. No commit, push, merge, pull request or tag operation was performed.
