# AI prompt record

- **Branch:** `feature/devansh-s3-cli`
- **Member:** Devansh Poddar (M5)
- **Stage:** Stage 3
- **Related pull request:** To be added

## Prompt 1

**Prompt**

> Implement the M5 side of the Stage 3 contract (PR #25): App wiring, the new `GameSnapshot` components, and the CLI Happy Hour prompt, Host commands and display, against the contract signatures.

**What was used or rejected**

The assistant (Claude) made these changes:
- `GameSnapshot` gains `hostAvailable` (immediately before `waiterAvailable`), `happyHourTurnsRemaining` (validated 0 to 2) and `happyHourUsed`, and rejects an active Happy Hour that was never used.
- `RestaurantRushApp` creates one shared `HappyHour` for `TurnManager` and `Waiter`, plus one `Host`.
- `RestaurantCLI` asks `Happy Hour> on/skip` before each turn only while Happy Hour is unused, and retries rejected or malformed answers without starting the turn twice. It adds `Host> seat` and `Host> wait` before the Waiter, and shows a Happy Hour line (ACTIVE with turns left, used, or not used yet). The staff line distinguishes "available after the Host/Waiter" from "used".
- `GameController.getSnapshot()` (M4's file) changes only so that it copies the three new values from `TurnManager`. M4 is asked to review this.
- Existing test snapshots gained the three new values, and new checks cover the components, their validation and the new display lines.

**Files affected**

- `src/restaurantrush/view/GameSnapshot.java`
- `src/restaurantrush/app/RestaurantRushApp.java`
- `src/restaurantrush/boundary/RestaurantCLI.java`
- `src/restaurantrush/control/GameController.java` (snapshot copy only)
- `tests/restaurantrush/view/ViewRecordsTest.java`
- `tests/restaurantrush/boundary/RestaurantCLITest.java`

**Human verification**

This branch depends on M1–M4's Stage 3 classes and will not compile until they are merged. It was compiled and played against temporary local stand-ins written to the contract (not committed): `ViewRecordsTest` passed 55 checks and `RestaurantCLITest` passed 30. A scripted session showed the Happy Hour prompt, 2 → 1 → used countdown, Host → Waiter → Chef order and same-turn seat/order/cook. The full test suite must be re-run after the real classes land.

**Related commits**

- To be added after commit
