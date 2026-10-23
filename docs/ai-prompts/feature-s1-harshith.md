# AI prompt record

- **Branch/member:** `feature/s1-harshith`; Harshith (M1), Stage 1.
- **Related PR:** Pending; no PR details supplied or verified.
- **Human verification:** Pending throughout; automated results below are not approval.
- **Current scope:** Six contracted M1 production types and four self-contained class tests: MenuItemTest, OrderTest, CustomerTest and RegularCustomerTest. Standalone demo, generated HTML, separate M1 handoff and the four extra testing files were removed. Add no optional extras.

Material excerpts below are verbatim; repeated specification text is omitted.
The full shared APIs/invariants remain in [class-contracts.md](../class-contracts.md).
All automated executions used Java/compiler 26.0.2 with Java 17 compilation target;
no Java 17 runtime execution is claimed. “Test build” means `./compile.sh --tests`.
Earlier references to “runner” mean the now-removed
`java -ea -cp out/classes restaurantrush.domain.M1DomainTests` entry point.
Current results and limits are in [stage-1-tests.md](../testing/stage-1-tests.md).
Source names below are under `src/main/java/restaurantrush/domain/`; test names
are under `src/test/java/restaurantrush/domain/`. Both evidence records were updated
as needed. No commit, push, merge or tag was performed by these assistant tasks.

## Prompt 1 — Foundation

**Prompt — material excerpt**

> On branch feature/s1-harshith, implement the M1 foundation feature: MenuItem, CustomerStatus and OrderStatus.

**What was used or rejected:** Exact MenuItem declaration, validated private final state, final getters, unchanged display name and separate enums. No subclasses or extra APIs.

**Files affected:** MenuItem.java, CustomerStatus.java, OrderStatus.java, M1DomainTests.java and evidence records.

**Human verification:** Pending. Automated test build exited 0; runner reported PASS (39 checks), exit 0, in a working tree based on `aaf89d19a63dd2555e3f3ffb0393731d11843ffe`.

**Related commit, subsequently observed:** `7754d4883bd9d38dc72a7fb3e2b6cd9653afbb39` — feat(menu item & lifecycle).

## Prompt 2 — Order

**Prompt — material excerpt**

> Implement the M1 Order feature, building on the existing MenuItem and lifecycle enums.

**What was used or rejected:** Exact guarded preparation/service/payment/cancellation lifecycle and explicit locked cents. Stored supplied quotes without recomputation; normal-flow quoting remains the caller's responsibility. No customer/peer behavior added.

**Files affected:** Order.java, M1DomainTests.java and evidence records.

**Human verification:** Pending. Automated test build exited 0; runner reported PASS (519 checks), exit 0, in a working tree based on `7754d4883bd9d38dc72a7fb3e2b6cd9653afbb39`.

**Related commit, subsequently observed:** `b17df6781be0f1a86b84c397502312219c61e980` — feat(order): implement order lifecycle and locked billing, with tests too.

## Prompt 3 — Customers

**Prompt — material excerpt**

> Implement the complete M1 customer feature: Customer and RegularCustomer.

**What was used or rejected:** Abstract Customer, field-free final RegularCustomer, final shared lifecycle, three polymorphic rules, documented no-ops and shared Order identity. No extra order-state acceptance guard, zero-satisfaction departure condition, maximum ID/turn or peer responsibilities.

**Files affected:** Customer.java, RegularCustomer.java, M1DomainTests.java and evidence records.

**Human verification:** Pending. Automated test build exited 0; runner reported PASS (2424 checks), exit 0, in a working tree based on `b17df6781be0f1a86b84c397502312219c61e980`.

**Related commit, subsequently observed:** `9a32cc426421b5391f27199b25eec60ac26aeb64` — feat(customer): implement customer lifecycle and RegularCustomer rules.

## Prompt 4 — Collaboration

**Prompt — material excerpt**

> Complete the M1 collaboration feature: cross-class lifecycle tests and an executable OOP demonstration.

**What was used or rejected:** Added normal quote/selection/shared-Order collaboration, abandonment and rejected-handoff checks. The requested standalone demo was created then, but removed by the later scope correction. No peer implementations or stronger guards.

**Files affected:** M1DomainTests.java, the since-deleted M1DomainDemo.java and evidence records.

**Human verification:** Pending. Automated test build exited 0; runner PASS (2914 checks), exit 0; historical demo PASS (257 checks), exit 0. Original working-tree base: `9a32cc426421b5391f27199b25eec60ac26aeb64`.

**Related commit, subsequently observed:** `b6db48f378e73852a7e039efb8e877c38b634757` — Add M1 collaboration tests.

## Prompt 5 — Verification and contribution evidence

**Prompt — material excerpts**

> Complete the verification and contribution evidence for my Stage 1 M1 work.
>
> Add useful class/method documentation explaining responsibilities, meaningful preconditions, exceptions, transitions and nullable getters.

**What was used or rejected:** Audit found no behavioral defect. Added requested source comments and updated README/evidence. Exact APIs, bodies and tests were preserved. A separate handoff and generated Javadoc HTML were later removed. Flagged the contract's `priceCents()` prose typo instead of adding an alias or editing the shared agreement.

**Files affected:** All six production files (comments only), README.md, evidence records and the since-deleted docs/uml/m1-handoff.md.

**Human verification:** Pending. Production/test builds, runner (2914 checks), historical demo (257 checks) and compiled API inspection exited 0. Strict Javadoc first exited 1 for 15 missing summaries; after correction it exited 0. These were working-tree results based on `b6db48f378e73852a7e039efb8e877c38b634757`, not exact-commit verification.

**Related commits:** No new commit; PR details pending.

## Prompt 6 — Remove optional artifacts

**Prompt — material excerpt**

> okay please remove this. i do not want anything "optional" added yet

The selected context was the standalone demo. A follow-up questioned generated `out/javadoc` output.

**What was used or rejected:** Removed the demo source/compiled class, generated Javadoc HTML and active regeneration/demo instructions. Required automated tests and production code were retained. Deleted handoff files were not restored.

**Files affected:** Deleted M1DomainDemo.java and ignored out/javadoc; updated README.md and evidence records.

**Human verification:** Pending. Production/test builds and runner PASS (2914 checks), all exit 0; removed artifacts confirmed absent.

**Related commits:** Uncommitted working tree based on `b6db48f378e73852a7e039efb8e877c38b634757`.

## Prompt 7 — Keep the repo focused

**Prompt**

> is there anything else optional or unnecessary you've added? check the repo and remove it, just keep what's required for the cli game and tests.

**What was used or rejected:** Audited all code, tests, build output and docs. No further optional code/dependencies found. Removed repeated review/OOP/handoff narratives from the test record and full-prompt duplication from this record. Kept concise material excerpts and actual evidence required by the shared agreement. Cleared only the stale staged addition of the already-deleted standalone handoff; preserved other staging. Kept the original brief, contract/templates, source comments, six M1 types, automated runner and normal compiler output.

**Files affected:** README.md, both evidence records and the staged addition of docs/uml/m1-handoff.md (cleared; file remains absent).

**Human verification:** Pending. Current production/test builds exited 0; runner reported `M1DomainTests: PASS (2914 checks)`, exit 0. Removed artifacts remain absent and generated output contains only the current classes and compiler source list.

**Related commits:** Uncommitted working tree based on `b6db48f378e73852a7e039efb8e877c38b634757`; PR details pending.

## Prompt 8 — Split tests by responsibility

**Prompt — material excerpt**

> for this: src/test/java/restaurantrush/domain/M1DomainTests.java
>
> can you see how they broke down the tests to the diff parts of the game? i want you to follow something similar to that

Reference supplied: [tests/restaurantrush](https://github.com/YangXu624/Restaurant-Rush-Group-Assignment/tree/main/tests/restaurantrush). Its test tree separates entity, staff, control, boundary, support and integration responsibilities.

**What was used or rejected:** Applied the per-class/responsibility organization to existing M1 tests. Moved all 28 test groups unchanged into six independently runnable suites; kept M1DomainTests as the aggregate entry point and shared checked assertions in package-private M1TestSupport. Retained all 2914 checks. Added no game behavior, peer tests, framework or demonstration.

**Files affected:** M1DomainTests.java; new MenuItemTest.java, LifecycleStatusTest.java, OrderTest.java, CustomerTest.java, RegularCustomerTest.java, M1CollaborationTest.java and M1TestSupport.java; README.md and both evidence records.

**Human verification:** Pending. Production build, test build and aggregate runner exited 0; runner reported `M1DomainTests: PASS (2914 checks)`. Each standalone command `java -ea -cp out/classes restaurantrush.domain.<suite>` also exited 0: MenuItemTest 25, LifecycleStatusTest 14, OrderTest 480, CustomerTest 1795, RegularCustomerTest 110 and M1CollaborationTest 490 checks. Source comparison confirmed every original test body and helper was retained. This layout was superseded by Prompt 9.

**Related commits:** Uncommitted working tree based on `b6db48f378e73852a7e039efb8e877c38b634757`; no new commit or PR.

## Prompt 9 — Remove extra testing files

**Prompt**

> remove these if they are jsoptional

The selected context named LifecycleStatusTest, M1CollaborationTest, M1DomainTests and M1TestSupport.

**What was used or rejected:** Removed all four files. Moved enum checks into CustomerTest/OrderTest and collaboration checks into CustomerTest. Kept only the private assertion helpers needed by each of the four self-contained class tests. Preserved all 28 test groups and 2914 checks; added no replacement runner, helper file or game feature.

**Files affected:** Deleted the four named testing files; updated MenuItemTest.java, OrderTest.java, CustomerTest.java, RegularCustomerTest.java, README.md and both evidence records.

**Human verification:** Pending. `./compile.sh` and `./compile.sh --tests` exited 0. The four direct test commands recorded in the test record exited 0: MenuItemTest 25, OrderTest 487, CustomerTest 2292 and RegularCustomerTest 110 checks. Production source checksums are unchanged.

**Related commits:** Uncommitted working tree based on `b6db48f378e73852a7e039efb8e877c38b634757`; no new commit or PR.
