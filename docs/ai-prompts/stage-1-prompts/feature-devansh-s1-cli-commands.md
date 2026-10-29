# AI prompt record

- **Branch:** `feature/devansh-s1-cli-commands`
- **Member:** Devansh Poddar (M5)
- **Stage:** Stage 1
- **Related pull request:** #3

Paths and verification notes below describe the original implementation layout.
See the [current repository structure](../../../README.md#repository-structure) for the moved files.

## Prompt 1

**Prompt**

> Go through this repository and tell me how and what to do to get my work started. (Followed by agreeing to have the assistant draft the Stage 1 CLI command wording into the class contract.)

**What was used or rejected**

The assistant (Claude) suggested settling the then-open CLI wording decision first, because every other member's integration depends on it. It drafted a command table for the Waiter and Chef phases (`seat`, `order`, `serve`, `cook`, `wait`, `help`), phase-labelled prompts so `wait` is unambiguous, retry rules that separate malformed input from domain rejections, and an end-of-turn summary line covering the fields required by Section 3.5 of the brief. I reviewed the wording against the brief and the existing `GameController` contract before committing.

**Files affected**

- `docs/class-contracts.md`

**Human verification**

Checked that each command maps to exactly one existing `GameController` operation and that the end-of-turn line lists every field required by brief Section 3.5. Pending M4 review on the pull request.

**Related commits**

- `fb3d383` - Propose Stage 1 CLI command wording
