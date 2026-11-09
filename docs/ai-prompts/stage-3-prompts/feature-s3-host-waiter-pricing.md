# AI prompt record

- **Branch:** `feature/s3-host-waiter-pricing`
- **Member:** Hunar (M3)
- **Stage:** Stage 3
- **Related pull request:** to be added when opened

## Prompt 1

**Prompt**

> Do the M3 Stage 3 work from docs/class-contracts/stage-3-class-contracts.md: the Host, the Waiter pricing handoff with Happy Hour, and mixed staff/payment regressions.

**What was used or rejected**

Used the AI draft of `Host`, the `Waiter(HappyHour)` constructor and the turn-aware `takeOrder`, and three test classes. Rejected replacing the old `Waiter()` constructor and two-argument `takeOrder` outright: `GameController`, `RestaurantRushApp` and their tests (M4/M5) still call them, so removing them would break the build before those owners migrate. They remain as thin overloads that use a private, never-activated policy and can be deleted once the callers move to the Stage 3 signatures.

**Files affected**

- `src/restaurantrush/staff/Host.java`
- `src/restaurantrush/staff/Waiter.java`
- `tests/restaurantrush/staff/HostTest.java`
- `tests/restaurantrush/staff/StaffHappyHourTest.java`

**Human verification**

Ran `./test.sh` on this branch (based on `origin/main` at `ecc82c0` plus M2's `feature/s3-happy-hour-menu` for `HappyHour` and the four-item menu). Every test class printed its PASS line, including `HostTest` (39 checks) and `StaffHappyHourTest` (76 checks). A reviewer has not yet checked the assertions.

**Related commits**

- See `git log feature/s3-host-waiter-pricing`
