# Stage 2 tests

Verified on 2026-11-06 by Codex for Harshith at `97463b1`. Java/compiler 26.0.2, targeting Java 17 with `--release 17`.

## Expected and observed results

Expected values follow the Stage 2 rules and saved mixed-customer victory/defeat fixtures. Actual values below were read from production CLI runs and matched the saved transcripts.

| Metric | Victory expected | Victory actual | Defeat expected | Defeat actual |
| --- | --- | --- | --- | --- |
| Outcome | Victory | Victory | Defeat | Defeat |
| Completed turns | 18 | 18 | 18 | 18 |
| Paid customers | 4 | 4 | 0 | 0 |
| Revenue | $58.50 (5850 cents) | $58.50 (5850 cents) | $0.00 | $0.00 |
| Average paid satisfaction | 75.5 | 75.5 | N/A | N/A |
| Served customers | 5 | 5 | 0 | 0 |
| Unhappy departures | 0 | 0 | 2 | 2 |

Both processes exited successfully. Each transcript contained sequential summaries `End of turn 1` through `End of turn 18`, one final outcome and `Turns completed: 18 of 18`, with no turn 19 or premature end-of-input message. Replay commands are in the [README](../../README.md#reproducible-victory-and-defeat-runs).

Victory requires **paid count ≥ 4 AND revenue ≥ 4500 cents AND average paid satisfaction ≥ 60**, after turn 18. The victory run satisfies all three. The defeat run misses the count and revenue targets; its `N/A` satisfaction is not a qualifying average.

The six arrivals include VIP customer 2 and Critic customer 4. The victory run shows the VIP's discounted Burger bill of $13.50 and five-point waiting loss, and the Critic's Pasta preference, full $18.00 bill and twelve-point waiting loss.

## Existing automated checks

```bash
./test.sh
```

All **15 test classes passed**. They cover Regular/VIP/Critic rules, shared lifecycle and locked billing, mixed-customer staff/payment behaviour, all 18 arrival turns, immutable views/type labels, controller action limits and victory boundaries, and turn-manager guards. The CLI replays above are checked separately. Test responsibilities and individual commands are in the [README](../../README.md#running-the-tests).

Java 17 runtime execution remains unverified.
