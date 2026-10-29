# Stage 1 tests

Verified on 2026-10-29 by Codex for Harshith on `refactor/stage-1` (working tree based on `a4dc510`; production source `698a85a`). Java/compiler 26.0.2, targeting Java 17 with `--release 17`.

## Expected and observed results

Expected values follow the Stage 1 rules and original victory/defeat fixtures. Actual values below were read from fresh production CLI runs and matched the saved transcripts.

| Metric | Victory expected | Victory actual | Defeat expected | Defeat actual |
| --- | --- | --- | --- | --- |
| Outcome | Victory | Victory | Defeat | Defeat |
| Completed turns | 18 | 18 | 18 | 18 |
| Paid customers | 5 | 5 | 0 | 0 |
| Revenue | $66.00 (6600 cents) | $66.00 (6600 cents) | $0.00 | $0.00 |
| Average paid satisfaction | 79.20 | 79.20 | N/A | N/A |
| Served customers | 5 | 5 | 0 | 0 |
| Unhappy departures | 0 | 0 | 2 | 2 |

Both processes exited successfully. Each transcript contained sequential summaries `End of turn 1` through `End of turn 18`, one final outcome and `Turns completed: 18 of 18`, with no turn 19 or premature end-of-input message. Replay commands are in the [README](../../README.md#reproducible-victory-and-defeat-runs).

Victory requires **paid count ≥ 4 AND revenue ≥ 4500 cents AND average paid satisfaction ≥ 60**, after turn 18. The victory run satisfies all three. The defeat run misses the count and revenue targets; its `N/A` satisfaction is not a qualifying average. The victory average is `79.2` before display formatting.

## Existing automated checks

```bash
./test.sh
```

All **11 test classes passed**. They cover domain/staff/view behaviour, controller action limits and victory boundaries, and turn-manager guards. `GameControllerTest` constructs precise boundary statistics with a test-only customer fixture; the original CLI replays above are checked separately. Test responsibilities and individual commands are in the [README](../../README.md#running-the-tests).

Java 17 runtime execution remains unverified. Stage 1 awaits user review; do not recreate `stage-1` without explicit approval.
