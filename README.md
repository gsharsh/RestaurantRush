# RestaurantRush

RestaurantRush is the SC2002 Object-Oriented Design and Programming group assignment. The goal is to build a Java 17 command-line restaurant-management game in three cumulative, independently runnable stages.

> **Current stage:** Stage 2 design preparation has begun; implementation has not
> started. The Stage 1 source baseline is `698a85a`; its release tag was removed
> for cleanup review. See the [Stage 2 contract draft](docs/stage-2-contracts.md).

## Required releases

| Stage | Required addition | Git tag | Status |
| --- | --- | --- | --- |
| Stage 1 | Regular customers, menu, Waiter, Chef, Cashier, complete turn flow, victory and defeat | `stage-1` | Cleanup under review; release tag pending |
| Stage 2 | VIP and Critic customer behaviour implemented polymorphically | `stage-2` | Contract draft prepared; implementation not started |
| Stage 3 | Host, ComboMeal and Happy Hour | `stage-3` | Not started |

Each tag must point to a commit that compiles and runs independently. Stage 1 source must not contain Stage 2 or Stage 3 implementations, and Stage 2 source must not contain Stage 3 implementations.

Development proceeds one stage at a time. The [Stage 1 class contract](docs/class-contracts.md) records the Stage 1 baseline. The [Stage 2 addendum](docs/stage-2-contracts.md) sets out the required customer extensions, proposed design choices, ownership and acceptance checks for team review.

## Repository structure

```text
.
├── docs/
│   ├── SC2002_Restaurant_Rush_Group_Assignment.docx # Assignment brief
│   ├── class-contracts.md      # Stage 1 responsibilities, interfaces and workflow
│   ├── stage-2-contracts.md    # Stage 2 contract draft and team handoffs
│   ├── ai-prompts/             # One concise AI-use record per feature branch
│   ├── testing/                # Stage test records and reproducible-run evidence
│   └── uml/                    # Editable Draw.io sources and reviewed exports
├── src/
│   ├── main/java/restaurantrush/
│   └── test/java/restaurantrush/
├── compile.sh
└── run.sh
```

The Stage 1 design and shared workflow are in [docs/class-contracts.md](docs/class-contracts.md). Begin Stage 2 with [docs/stage-2-contracts.md](docs/stage-2-contracts.md).

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

`compile.sh --tests` compiles the tests but does not run them. Each test class is
self-contained and exits nonzero on failure:

```bash
./compile.sh --tests
java -ea -cp out/classes restaurantrush.domain.MenuItemTest
java -ea -cp out/classes restaurantrush.domain.OrderTest
java -ea -cp out/classes restaurantrush.domain.CustomerTest
java -ea -cp out/classes restaurantrush.domain.RegularCustomerTest
java -ea -cp out/classes restaurantrush.domain.ActionResultTest
java -ea -cp out/classes restaurantrush.domain.RestaurantStateTest
java -ea -cp out/classes restaurantrush.staff.StaffTest
java -ea -cp out/classes restaurantrush.control.TurnManagerTest
java -ea -cp out/classes restaurantrush.view.ViewRecordsTest
java -ea -cp out/classes restaurantrush.boundary.RestaurantCLITest
```

| Test class | Owner | Responsibility |
| --- | --- | --- |
| `MenuItemTest` | M1 | Menu values and constructor validation |
| `OrderTest` | M1 | Order states, preparation, service, payment, cancellation and locked billing |
| `CustomerTest` | M1 | Customer states, lifecycle timing, satisfaction and order assignment |
| `RegularCustomerTest` | M1 | Menu selection, price quotes and waiting-loss rules |
| `ActionResultTest` | M3 | Success/failure results and message validation |
| `RestaurantStateTest` | M2 | Queue, seating, order registration, serving, payment order, abandonment and read-only views |
| `StaffTest` | M3 | Waiter, Chef and Cashier actions, including substitution by another `Customer` subtype |
| `TurnManagerTest` | M4 | Turn order, staff limits, arrivals, automatic events and turn guards |
| `ViewRecordsTest` | M5 | Immutable snapshot and view records |
| `RestaurantCLITest` | M5 | Command parsing, retries and displays |

### Reproducible victory and defeat runs

The saved inputs in `docs/testing/scenarios/` replay a full 18-turn game. Compare
the output with the saved `.out` file (no output from `diff` means identical):

```bash
./compile.sh
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/scenarios/stage-1-victory.txt | diff docs/testing/scenarios/stage-1-victory.out -
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/scenarios/stage-1-defeat.txt | diff docs/testing/scenarios/stage-1-defeat.out -
```

The [Stage 1 test record](docs/testing/stage-1-tests.md) records the tested commit,
Java version, commands and observed results. The shared
[class contract](docs/class-contracts.md) describes the exact APIs and
responsibilities, and `docs/ai-prompts/` holds the per-branch AI-prompt records.

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

- Create one prompt record from [docs/ai-prompts/TEMPLATE.md](docs/ai-prompts/TEMPLATE.md) for each AI-assisted branch.
- Record observed tests using [docs/testing/TEST-TEMPLATE.md](docs/testing/TEST-TEMPLATE.md).
- Evolve `docs/uml/class-diagram.drawio` and its reviewed export with each stage; the Stage 1 baseline commit preserves the earlier diagram.
- Before creating each stage tag, compile the candidate commit, run its tests and victory/defeat scenarios, and have another member review the evidence.
- Keep tests under the same feature branch as the behaviour they verify, and record the observed result against the exact commit SHA.

## Decisions still open

- Review the Stage 2 VIP rounding policy, arrival mapping and ownership proposed in the addendum. The Critic preference is confirmed: most preparation units, then higher price, then menu order.
- Assign the Stage 2 release coordinator and evidence reviewer.
- Confirm the submission deadline and lab-group archive name.

## Stage 1 → Stage 2 transition

The Stage 1 source baseline is
[`698a85a`](https://github.com/gsharsh/RestaurantRush/commit/698a85a210ea5bc549ee9b49f328985eb758b5cc).
The published `stage-1` tag was removed on 2026-10-29 while the documentation
cleanup is reviewed separately on `refactor/stage-1`. This Stage 2 draft remains
based on the source baseline; the cleanup has not been merged into this branch.

This kickoff changes documentation only: it adds the Stage 2 contract draft and
AI-use record, updates this README, and links the addendum from the Stage 1
contract. No Java files have been changed or deleted for Stage 2. As implementation
lands, record the actual new, changed and deleted files with reasons here and add
the relevant implementation commits or comparison link, as required by brief §5.

## Repository

- GitHub: <https://github.com/gsharsh/RestaurantRush>
- Assignment brief: [docs/SC2002_Restaurant_Rush_Group_Assignment.docx](docs/SC2002_Restaurant_Rush_Group_Assignment.docx)
