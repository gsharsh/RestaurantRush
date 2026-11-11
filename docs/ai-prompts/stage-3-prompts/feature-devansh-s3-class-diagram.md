# AI prompt record

- **Branch:** `feature/devansh-s3-evidence`
- **Member:** Devansh Poddar (M5)
- **Stage:** Stage 3 (final class diagram)
- **Related pull request:** To be added

## Prompt 1

**Prompt**

> Build the final Stage 3 class diagram from the merged code, starting from the Stage 2 diagram, and add the encapsulation, polymorphism and composition annotations the brief asks for.

**What was used or rejected**

The assistant (Claude) copied the Stage 2 diagram and applied the Stage 3 changes found in the code at `5c583fb`:
- new `Host`, `HappyHour` and `ComboMeal` classes
- `ComboMeal` generalises `MenuItem` and references two components
- `Restaurant` now owns 4 menu items
- `Customer.recoverWaitingSatisfaction`
- the Host and Happy Hour members of `GameController`, `TurnManager` and `Waiter`
- the three new `GameSnapshot` components
- the new CLI methods

`Waiter`'s older `Waiter()` constructor and two-argument `takeOrder` are still in the code, so they're shown too. The staff row was widened to four classes, and `HappyHour` sits under the control column so its two shared associations (TurnManager, Waiter) stay short. Three notes explain where encapsulation, polymorphism and composition actually show up in the code.

**Files affected**

- `docs/uml/stage-3-uml/class-diagram.drawio` (and its PNG export)

**Human verification**

To do: open in draw.io, tidy any crowded lines, check each class against the source, export the PNG and ask owners to confirm their classes on the PR.

**Related commits**

- To be added after commit
