# AI prompt record

- **Branch:** `feature/devansh-s3-evidence`
- **Member:** Devansh Poddar (M5)
- **Stage:** Stage 3
- **Related pull request:** To be added

## Prompt 1

**Prompt**

> Plan reproducible Stage 3 victory and defeat runs that show Host seating, Happy Hour pricing on a VIP order and the Critic's combo, then record them in a Stage 3 test record. Draft the Stage 3 sequence diagram for the VIP-during-Happy-Hour scenario.

**What was used or rejected**

The assistant (Claude) planned the 18-turn victory input so Happy Hour is switched on at turn 4, when VIP customer 2 arrives, and the Critic's combo is cooked over three Chef turns. It first checked the inputs against temporary stand-ins written to the Stage 3 contract, then reran them unchanged on the merged code. It drafted the sequence diagram from the contract and then corrected it against the merged code: `beginTurn` returns nothing and `startTurn` wraps it in an `ActionResult`, recovery goes through `getActiveCustomers()`, and the controller fetches the order before calling the Chef. Page 2 reuses the team's Stage 1 end-of-turn reference, retitled for Stage 3.

**Files affected**

- `docs/testing/stage-3-testscript/stage-3-victory.txt` / `.out`
- `docs/testing/stage-3-testscript/stage-3-defeat.txt` / `.out`
- `docs/testing/stage-3-tests.md`
- `docs/uml/stage-3-uml/sequence-diagram.drawio`

**Human verification**

Ran `./test.sh` and both scenarios on my Mac at `5c583fb`. All 19 test classes passed, the victory run ended VICTORY (5 paid, $71.10, average 84.8) and the defeat run ended DEFEAT (0 paid, 2 departures). Checked the sequence diagram's calls against `GameController`, `TurnManager`, `Waiter`, `Host` and `Chef`.

**Related commits**

- To be added after commit
