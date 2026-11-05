# AI prompt record

- **Branch:** `feature/devansh-s2-evidence`
- **Member:** Devansh Poddar (M5)
- **Stage:** Stage 2
- **Related pull request:** To be added

## Prompt 1

**Prompt**

> Prepare reproducible Stage 2 victory and defeat runs that show the VIP and Critic behaviour, and record them in the Stage 2 test record.

**What was used or rejected**

The assistant (Claude) planned an 18-turn victory sequence using the agreed arrival mapping (customer 2 = VIP, customer 4 = Critic) so that the VIP's discounted bill and slower satisfaction loss, and the Critic's Pasta preference and faster loss, all appear in the transcript while still meeting the victory thresholds. The defeat run waits every turn, which shows the three waiting-loss rates through who leaves and when. The sequence was first checked against a local copy of the agreed mapping, then re-run unchanged on `main` once M4's arrival schedule was merged. It also drafted the M5 section of `docs/testing/stage-2-tests.md` from the observed outputs.

**Files affected**

- `docs/testing/stage-2-testscript/stage-2-victory.txt` / `.out`
- `docs/testing/stage-2-testscript/stage-2-defeat.txt` / `.out`
- `docs/testing/stage-2-tests.md`

**Human verification**

Ran `./test.sh` and both scenarios on my Mac at `d050b2d`: all 14 test classes passed, the victory run ended VICTORY (4 paid, $58.50, average 75.5) and the defeat run ended DEFEAT (0 paid, 2 departures). Checked the recorded values against the saved outputs.

## Prompt 2

**Prompt**

> Apply M1's review: label the older test-record sections as historical, and remove the machine-specific compilation banner from the saved outputs.

**What was used or rejected**

The assistant moved M1's 30 October label-API notes under a "Historical label-API verification — 30 October 2026" heading with a one-line explanation, kept the release checklist as a current top-level section, and changed the recorded commands to compile once and run the app directly, with a `diff` replay check. The outputs were re-recorded that way.

**Human verification**

Re-recorded both outputs on my Mac and confirmed they start with the game's welcome line and still end in VICTORY and DEFEAT.

**Related commits**

- To be added after commit
