# RestaurantRush

RestaurantRush is the SC2002 Object-Oriented Design and Programming group assignment. The goal is to build a Java 17 command-line restaurant-management game in three cumulative, independently runnable stages.

> **Current stage:** Stage 2 design preparation has begun; implementation has not
> started. Stage 1 remains published at `stage-1` (`1b9ebf9`). This branch includes
> the folder reorganisation from `main` at `1dadd0b` and the Stage 2 contract draft.

## Required releases

| Stage | Required addition | Git tag | Status |
| --- | --- | --- | --- |
| Stage 1 | Regular customers, menu, Waiter, Chef, Cashier, complete turn flow, victory and defeat | `stage-1` | Published at `1b9ebf9` |
| Stage 2 | VIP and Critic customer behaviour implemented polymorphically | `stage-2` | Contract draft prepared; implementation not started |
| Stage 3 | Host, ComboMeal and Happy Hour | `stage-3` | Not started |

Each tag must point to a commit that compiles and runs independently. Stage 1 source must not contain Stage 2 or Stage 3 implementations, and Stage 2 source must not contain Stage 3 implementations.

Development proceeds one stage at a time. The [Stage 1 contract](docs/class-contracts/stage-1-class-contracts.md) records the accepted gameplay baseline. The [Stage 2 addendum](docs/class-contracts/stage-2-class-contracts.md) sets out the required customer extensions, proposed choices, ownership and acceptance checks for team review.

## Repository structure

```text
.
├── docs/
│   ├── SC2002_Restaurant_Rush_Group_Assignment.docx # Assignment brief
│   ├── class-contracts/
│   │   ├── stage-1-class-contracts.md
│   │   └── stage-2-class-contracts.md
│   ├── ai-prompts/             # Shared template and stage-specific prompt folders
│   ├── testing/
│   │   ├── TEST-TEMPLATE.md
│   │   ├── stage-1-tests.md   # Stage 1 verification record
│   │   ├── stage-1-testscript/ # Replay inputs and saved outputs
│   │   └── stage-2-testscipt/  # Stage 2 replay folder
│   └── uml/
│       ├── stage-1-uml/        # Editable Draw.io source and PNG export
│       └── stage-2-uml/
├── src/restaurantrush/         # Production Java packages
├── tests/restaurantrush/ # Java test packages
├── compile.sh
├── test.sh
└── run.sh
```

The Stage 1 design and shared workflow are in [the Stage 1 contract](docs/class-contracts/stage-1-class-contracts.md). Begin Stage 2 with [the Stage 2 addendum](docs/class-contracts/stage-2-class-contracts.md).

## Prerequisites

- Java Development Kit 17 or newer
- Bash for the supplied helper scripts
- Draw.io/diagrams.net for UML editing

The project is compiled with `javac --release 17`, even when a newer JDK is installed.

## Compile and run

From the repository root:

```bash
# Run the interactive game
./run.sh
```

`run.sh` compiles the production sources and launches
`restaurantrush.app.RestaurantRushApp`. Type `help` at a Waiter or Chef prompt
to see the available commands.

Compile the current production sources:

```bash
./compile.sh
```

### Running the tests

Run all self-contained Java tests:

```bash
./test.sh
```

Run one test class:

```bash
./test.sh restaurantrush.control.GameControllerTest
```

`test.sh` compiles production and test sources, then runs the discovered
`*Test.java` entry points with assertions enabled. Each exits nonzero on failure.
`./compile.sh --tests` remains available for compilation without execution.

| Test class | Owner | Responsibility |
| --- | --- | --- |
| `MenuItemTest` | M1 | Menu values and constructor validation |
| `OrderTest` | M1 | Order states, preparation, service, payment, cancellation and locked billing |
| `CustomerTest` | M1 | Customer states, lifecycle timing, satisfaction and order assignment |
| `RegularCustomerTest` | M1 | Menu selection, price quotes and waiting-loss rules |
| `ActionResultTest` | M3 | Success/failure results and message validation |
| `RestaurantStateTest` | M2 | Queue, seating, order registration, serving, payment order, abandonment and read-only views |
| `StaffTest` | M3 | Waiter, Chef and Cashier actions, including substitution by another `Customer` subtype |
| `TurnManagerTest` | M4 | Turn phases, duplicate/premature call guards and failed-start preservation |
| `GameControllerTest` | M4 | Action limits, failed-action preservation, arrival snapshots, lifecycle timing and victory boundaries |
| `ViewRecordsTest` | M5 | Immutable snapshot and view records |
| `RestaurantCLITest` | M5 | Final-outcome display and rejection of unfinished/null snapshots |

### Reproducible victory and defeat runs

The saved inputs in `docs/testing/stage-1-testscript/` replay a full 18-turn game. Compare
the output with the saved `.out` file (no output from `diff` means identical):

```bash
./compile.sh
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/stage-1-testscript/stage-1-victory.txt | diff docs/testing/stage-1-testscript/stage-1-victory.out -
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/stage-1-testscript/stage-1-defeat.txt | diff docs/testing/stage-1-testscript/stage-1-defeat.out -
```

The [Stage 1 test record](docs/testing/stage-1-tests.md) records the tested commit,
Java version, commands and observed results. The shared
[class contract](docs/class-contracts/stage-1-class-contracts.md) describes the exact APIs and
responsibilities, and `docs/ai-prompts/stage-1-prompts/` holds the Stage 1 per-branch AI-prompt records.

## Team ownership

The table below records Stage 1 ownership. The Stage 2 addendum proposes carrying these roles forward, with focused implementation for M1/M4 and integration, testing and evidence responsibilities for the other members. Stage 2 assignments remain subject to team agreement.

| Role | Stage 1 ownership | Principal types |
| --- | --- | --- |
| M1 | Customers and orders | `Customer`, `RegularCustomer`, `Order`, `MenuItem`, `CustomerStatus`, `OrderStatus` |
| M2 | Restaurant state | `Restaurant`, `Table` |
| M3 | Staff operations | `Waiter`, `Chef`, `Cashier`, `ActionResult` |
| M4 | Game control | `GameController`, `TurnManager`, `ArrivalSchedule` |
| M5 | Boundary and integration | `RestaurantRushApp`, `RestaurantCLI`, immutable views and scripted scenarios |

The contract defines the shared handoffs between these areas. Ownership identifies the first implementer and required reviewer; it does not prohibit reviewed cross-owner changes.

Stage 1 remains extensible through behaviour it genuinely uses: game code depends on the abstract `Customer`, `RegularCustomer` supplies the current rules, `ArrivalSchedule` isolates arrival creation, and `Order` receives a locked customer-quoted price. No future-stage classes or empty placeholders are included.

## Git contribution and tagging policy

- Configure `user.name` and a GitHub-linked `user.email` before committing.
- Create short-lived feature branches from the latest `main` and open pull requests back into `main`; do not use permanent personal branches.
- Make meaningful, reviewable commits. There is no commit-count or commit-spacing quota.
- Request the relevant owner as reviewer when a branch changes another member's classes.
- Preserve individual commits with a normal merge when that history is useful for assessment; the merger or tagger does not become the author of other members' work.
- Create the current stage tag only after its implementation, tests, UML and evidence have passed review—not after every member reaches an arbitrary number of commits.

## Development evidence

- Create one prompt record from [docs/ai-prompts/TEMPLATE.md](docs/ai-prompts/TEMPLATE.md) for each AI-assisted branch, in the corresponding stage's prompt folder.
- Record observed tests using [docs/testing/TEST-TEMPLATE.md](docs/testing/TEST-TEMPLATE.md).
- Keep the Stage 1 diagram and export in `docs/uml/stage-1-uml/`; add the Stage 2 design under `docs/uml/stage-2-uml/` when implemented.
- Before creating each stage tag, compile the candidate commit, run its tests and victory/defeat scenarios, and have another member review the evidence.
- Keep tests under the same feature branch as the behaviour they verify, and record the observed result against the exact commit SHA.

## Decisions still open

- Review the Stage 2 VIP rounding policy, arrival mapping and ownership proposed in the addendum. The Critic preference is confirmed: most preparation units, then higher price, then menu order.
- Assign the Stage 2 release coordinator and evidence reviewer.
- Confirm the submission deadline and lab-group archive name.

## Stage 1 → Stage 2 transition

The published `stage-1` tag identifies
[`1b9ebf9`](https://github.com/gsharsh/RestaurantRush/commit/1b9ebf92330316628010880245f98a67416aaef9).
The folder reorganisation and script fixes are recorded on `main` at
[`1dadd0b`](https://github.com/gsharsh/RestaurantRush/commit/1dadd0bbda58403d33afb2b7f7dd795dabdd8792)
and are merged into this documentation branch. The existing release tag is unchanged.

Stage 2 currently changes documentation only. The contract draft and AI-use
record are in their stage-specific folders. No Java behaviour has been changed
for Stage 2. As implementation lands, record the actual new, changed and deleted
files, reasons and relevant commit or compare links here, as required by brief §5.

## Repository

- GitHub: <https://github.com/gsharsh/RestaurantRush>
- Assignment brief: [docs/SC2002_Restaurant_Rush_Group_Assignment.docx](docs/SC2002_Restaurant_Rush_Group_Assignment.docx)
