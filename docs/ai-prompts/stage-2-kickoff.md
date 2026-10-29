# AI prompt record — Stage 2 kickoff

- **Branch:** `docs/stage-2`; documentation was initially prepared on `main` and carried onto this branch without a commit
- **Member:** Harshith
- **Stage:** Stage 2 planning
- **Related pull request:** None yet

## Prompt 1

**Prompt**

> okay we have added the git tag alr for stage 1. we are now ready to move to stage 2 of the project

**What was proposed**

Checked the published Stage 1 tag, read the assignment brief and reviewed the existing customer extension points. Prepared a Stage 2 addendum covering VIP/Critic rules, proposed Critic preference and arrival mapping, continued team ownership, integration order and acceptance evidence. Updated the README and linked the draft from the Stage 1 contract. The repo's existing documentation workflow was retained; no separate planning framework was added.

The initial proposed Critic rule was highest base-price preference with first-in-menu tie-breaking; Prompt 2 below supersedes it. Other design choices and ownership remain drafts for team review. Stage 3 features remain deferred.

**Files affected**

- `README.md`
- `docs/class-contracts.md`
- `docs/stage-2-contracts.md`
- This prompt record

**Verification and review status**

- Remote read confirmed annotated `stage-1` resolves to `698a85a210ea5bc549ee9b49f328985eb758b5cc`, also the remote `main` commit on 2026-10-29.
- Compared proposed requirements against brief §§2–3, 4.2, 4.4 and 5 and inspected existing Customer/Order/arrival/staff integration surfaces.
- No Java source was changed and no gameplay tests were run for this documentation kickoff.
- Preserved the pre-existing local edit to `docs/testing/stage-1-tests.md`.
- The Critic preference was subsequently confirmed by the user in Prompt 2. Review of other design choices and ownership remains pending.

**Related commits**

- None created for this kickoff.

## Prompt 2 — Confirm Critic preference

**Prompt**

> okay let's use that behaviour for our Critic too — most preparation units, then higher price. update the stage 2 contract accordingly

**What was used**

After comparing the [reference repository's Critic implementation](https://github.com/yeoshuyi/RestaurantRush/blob/803b33148a18659c87102a7f5dc71e544c930841/src/rush/model/CriticCustomer.java#L5-L28), the user selected preparation units as the primary preference and higher price as the tie-break. Updated the Stage 2 contract and acceptance checks accordingly. Retained menu order as the deterministic final tie-break when both properties match. The README now distinguishes this confirmed choice from the remaining proposals.

**Verification and review status**

- Documentation-only change; no Java implementation or gameplay test result is claimed.
- Acceptance checks now distinguish preparation-first selection from the superseded price-first proposal and cover both tie-break levels.

## Prompt 3 — Documentation branch

**Prompt**

> i want all of this to be worked on the branch docs/stage-2

**What was used**

Created and switched to `docs/stage-2`, carrying the existing documentation edits forward. The Critic revision was made on that branch. No commit or push was performed, and the pre-existing Stage 1 test-record edit was preserved.

## Prompt 4 — Separate Stage 1 cleanup from Stage 2

**Prompt**

> stage 2 docs keep it on the docs/stage-2 branch
> delete the stage-1 tag on github
> edit these accordingly on branch called refactor/stage-1 and then let me review
> bring back the changes for stage 2

**What was used**

Preserved the Stage 2 draft, confirmed Critic preference and branch-specific README/contract changes on `docs/stage-2`. Kept the Stage 1 cleanup on a separate branch for review. Updated current release wording after deleting the GitHub `stage-1` tag on 2026-10-29; the earlier tag verification above describes its historical state. The source baseline remains `698a85a`. These branch-organisation changes do not implement Stage 2 or alter Java source.

**Verification and review status**

- GitHub confirmed deletion of `refs/tags/stage-1`; a subsequent remote read returned no matching tag.
- Stage 1 cleanup remains separate from this draft and awaits the user's review.
- The Stage 2 draft is saved in a local documentation commit; no Stage 2 implementation or tag is claimed.

## Prompt 5 — Hold the Stage 1 tag for user review

**Prompt**

> dont retag stage 1 yet, let me review that stage 1 is done too

**What was used**

Left `stage-1` absent. Both Stage 1 completion and cleanup require explicit user approval before any replacement tag is created. Updated this branch's release wording while retaining the separate Stage 2 design draft; no cleanup commit was merged into `docs/stage-2`.
