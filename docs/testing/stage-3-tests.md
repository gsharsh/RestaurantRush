# Stage 3 tests

Verified on 2026-11-12 by Devansh Poddar (M5) at `5c583fb` (`main` after PRs #26–#29). OpenJDK 21.0.12, compiled with `javac --release 17` via `compile.sh`.

## Expected and observed results

Expected values follow the Stage 3 rules and the saved victory/defeat inputs in `docs/testing/stage-3-testscript/`. Actual values were read from production CLI runs and matched the saved transcripts.

| Metric | Victory expected | Victory actual | Defeat expected | Defeat actual |
| --- | --- | --- | --- | --- |
| Outcome | Victory | Victory | Defeat | Defeat |
| Completed turns | 18 | 18 | 18 | 18 |
| Paid customers | 5 | 5 | 0 | 0 |
| Revenue | $71.10 (7110 cents) | $71.10 (7110 cents) | $0.00 | $0.00 |
| Average paid satisfaction | 84.8 | 84.8 | N/A | N/A |
| Served customers | 6 | 6 | 0 | 0 |
| Unhappy departures | 0 | 0 | 2 | 2 |

Neither run had a rejected command or a premature end-of-input message. Each transcript runs from `End of turn 1` to `End of turn 18` and ends with one outcome and `Turns completed: 18 of 18`.

Victory requires **paid count ≥ 4 AND revenue ≥ 4500 cents AND average paid satisfaction ≥ 60** after turn 18. The victory run meets all three. The defeat run misses the count and revenue targets.

**Stage 3 behaviour shown in the victory run:**

- **Happy Hour:** skipped on turns 1–3, activated on turn 4. The display shows `ACTIVE, 2 turns left`, then `1 turn left` on turn 5, then `used` for the rest of the game, and the `Happy Hour>` prompt stops appearing once it has been used. VIP customer 2's Burger is priced 1500 → 1350 (VIP) → 1080 cents (Happy Hour), and that locked bill is what gets paid on turn 7.
- **Host:** seats a customer on turns 1, 4, 7, 10 and 13, before the Waiter acts, so the same customer can be seated, ordered for and cooked for in one turn.
- **ComboMeal:** Critic customer 4 orders the Salad + Pasta Combo (2430 cents, 3 preparation units). It's cooked over three Chef turns (2 → 1 → READY), served, and paid at full price.

**Defeat run:** the player skips Happy Hour on every turn (the prompt appears all 18 turns because it's never used), and Host, Waiter and Chef all wait. Customer 1 (Regular) leaves at the end of turn 13 and customer 4 (Critic) at the end of turn 18.

To check a replay on any machine:

```bash
./compile.sh
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/stage-3-testscript/stage-3-victory.txt | diff docs/testing/stage-3-testscript/stage-3-victory.out -
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/stage-3-testscript/stage-3-defeat.txt | diff docs/testing/stage-3-testscript/stage-3-defeat.out -
```

## Existing automated checks

```bash
./test.sh
```

All **19 test classes passed**. Stage 3 adds `ComboMealTest`, `HappyHourTest`, `HostTest` and `StaffHappyHourTest`, and extends the customer, controller, turn-manager, CLI and view tests for recovery, Host task limits, Happy Hour activation and the new snapshot fields.

Java 17 runtime execution remains unverified.
