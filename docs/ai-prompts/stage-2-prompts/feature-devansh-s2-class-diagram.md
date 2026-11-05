# AI prompt record

- **Branch:** `feature/devansh-s2-evidence`
- **Member:** Devansh Poddar (M5)
- **Stage:** Stage 2
- **Related pull request:** To be added

## Prompt 1

**Prompt**

> Draft the Stage 2 class diagram from the merged Stage 2 code, starting from the team's Stage 1 diagram so its layout and style are kept.

**What was used or rejected**

The assistant (Claude) copied the Stage 1 class-diagram page from `docs/uml/stage-1-uml/class-diagram.drawio` and applied only the Stage 2 changes found in the code on `main` (`1ba2954`): `+ typeLabel(): String {abstract}` on `Customer`; `typeLabel()` on `RegularCustomer`; new `VIPCustomer` and `CriticCustomer` entities (including Critic's private static `isPreferredOver(...)`), each generalising `Customer`; and `- type: String {readOnly}` on `CustomerView`. To fit the three subclasses in one row under `Customer`, the two enumerations moved to the support/view column and the Customer–Order and Restaurant–MenuItem lines were re-routed. The Stage 1 sequence-diagram pages were left out because sequence diagrams are scheduled after Stage 3. Vedavit's CriticCustomer UML details were used as a cross-check.

**Files affected**

- `docs/uml/stage-2-uml/class-diagram.drawio`

**Human verification**

Opened the diagram in draw.io and checked the new and changed classes against the source on `main` (`d050b2d`). M4's merged arrival schedule changes only `ArrivalSchedule`'s internals (its fields and operations are unchanged), so no further diagram edit was needed. Exported `class-diagram.png` at 200% (light theme). Owners still to confirm their classes on the pull request.

**Related commits**

- To be added after commit
