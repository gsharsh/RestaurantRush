# AI prompt record

- **Branch:** `feature/s2-staff-regression`
- **Member:** Hunar (M3)
- **Stage:** Stage 2
- **Related pull request:** to be added when opened

## Prompt 1

**Prompt**

> Do the M3 Stage 2 work from docs/class-contracts/stage-2-class-contracts.md: staff and payment regression tests with Regular, VIP and Critic customers.

**What was used or rejected**

Used the AI draft of one new test class. No production code was changed: the contract allows staff changes only for a demonstrated gap, and none was found. Tests use the real `VIPCustomer` and `CriticCustomer` directly, so they do not depend on M4's pending `ArrivalSchedule` change.

**Files affected**

- `tests/restaurantrush/staff/StaffMixedCustomerTest.java`

**Human verification**

Ran `./test.sh` on this branch (based on `origin/main` at `1ba2954`, Java 25.0.4.1, compiled with `--release 17`). Every test class printed its PASS line, including `StaffMixedCustomerTest` (141 checks). A reviewer has not yet checked the assertions.

**Related commits**

- See `git log feature/s2-staff-regression`
