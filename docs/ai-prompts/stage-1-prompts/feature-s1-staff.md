# AI prompt record

- **Branch:** `feature/s1-staff`
- **Member:** Hunar (M3)
- **Stage:** Stage 1
- **Related pull request:** to be added when opened

Paths and verification notes below describe the original implementation layout.
See the [current repository structure](../../../README.md#repository-structure) for the moved files.

## Prompt 1

**Prompt**

> Implement the M3 staff classes (Waiter, Chef, Cashier, ActionResult) from docs/class-contracts.md, with tests.

**What was used or rejected**

Used the AI draft of the four classes and their tests. Kept the contract's design of no common `Staff` superclass and no availability flags in the staff classes. The LSP/OCP test uses a test-only `Customer` subtype instead of adding a Stage 2 class to production code.

**Files affected**

- `src/main/java/restaurantrush/domain/ActionResult.java`
- `src/main/java/restaurantrush/staff/Waiter.java`
- `src/main/java/restaurantrush/staff/Chef.java`
- `src/main/java/restaurantrush/staff/Cashier.java`
- `src/test/java/restaurantrush/staff/StaffTest.java`
- `src/test/java/restaurantrush/domain/ActionResultTest.java`

**Human verification**

Compiled the staff, domain and test sources and ran `StaffTest` (92 checks) and `ActionResultTest` (7 checks) with `java -ea`; both exited normally. Full-game launch is not verified because the control classes are on a peer branch.

**Related commits**

- See `git log feature/s1-staff`
