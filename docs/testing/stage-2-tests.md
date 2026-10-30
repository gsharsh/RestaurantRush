# Stage 2 test record

- **Status:** Partial — shared customer-type label API only; Stage 2 release checks remain pending.
- **Tested commit:** Uncommitted working tree based on `550ae6f8b5dffc2c4b614df65153783f22b3a513`; no exact implementation-commit verification claimed.
- **Branch:** `feature/stage-2-harshith`
- **Tester:** Codex automated execution for Harshith (M1)
- **Reviewer:** Pending; M4 (Viti) for QuotedCustomer and M3 (Hunar) for DiscountedCustomer compatibility edits.
- **Date:** 2026-10-30
- **Java version:** `java` and `javac` 26.0.2; scripts compile with `--release 17`. Execution on a Java 17 runtime was not tested.

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
