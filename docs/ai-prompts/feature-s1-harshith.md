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

