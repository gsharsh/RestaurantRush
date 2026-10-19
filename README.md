# RestaurantRush

RestaurantRush is the SC2002 Object-Oriented Design and Programming group assignment. The goal is to build a Java 17 command-line restaurant-management game in three cumulative, independently runnable stages.

> **Current status:** Stage 1 planning and repository scaffolding. No gameplay classes have been implemented and no stage tags have been created yet.

## Required releases

| Stage | Required addition | Git tag | Status |
| --- | --- | --- | --- |
| Stage 1 | Regular customers, menu, Waiter, Chef, Cashier, complete turn flow, victory and defeat | `stage-1` | Not started |
| Stage 2 | VIP and Critic customer behaviour implemented polymorphically | `stage-2` | Not started |
| Stage 3 | Host, ComboMeal and Happy Hour | `stage-3` | Not started |

Each tag must point to a commit that compiles and runs independently. Stage 1 source must not contain Stage 2 or Stage 3 implementations, and Stage 2 source must not contain Stage 3 implementations.

Development proceeds one stage at a time. The current class contract defines only Stage 1. After `stage-1` is accepted and tagged, the team will review the working design and extend the contract for Stage 2 rather than predicting all later APIs now.

## Repository structure

```text
.
├── docs/
│   ├── SC2002_Restaurant_Rush_Group_Assignment.docx # Assignment brief
│   ├── class-contracts.md      # Stage 1 responsibilities, interfaces and workflow
│   ├── ai-prompts/             # One concise AI-use record per feature branch
│   ├── testing/                # Stage test records and reproducible-run evidence
│   └── uml/                    # Editable Draw.io sources and reviewed exports
├── src/
│   ├── main/java/restaurantrush/
│   └── test/java/restaurantrush/
├── compile.sh
└── run.sh
```

The Stage 1 design, ownership plan, Git workflow, testing process and evidence requirements are in [docs/class-contracts.md](docs/class-contracts.md).

## Prerequisites

- Java Development Kit 17 or newer
- Bash for the supplied helper scripts
- Draw.io/diagrams.net for UML editing

The project is compiled with `javac --release 17`, even when a newer JDK is installed.

## Compile and run

Once production Java sources exist:

```bash
./compile.sh
./run.sh
```

To compile production and test sources together:

```bash
./compile.sh --tests
```

The scripts currently stop with a clear message because the Java implementation has not started. `run.sh` will launch `restaurantrush.app.RestaurantRushApp` once that entry point is created.

## Team ownership

The current ownership table applies to Stage 1 only. Later-stage ownership will be agreed after the team has completed and reviewed the Stage 1 design.

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
- Maintain the Stage 1 class diagram as `docs/uml/class-diagram.drawio`, together with a reviewed PDF or PNG export. Later diagram work will be planned at the relevant stage.
- Before creating `stage-1`, compile the candidate commit, run its tests and victory/defeat scenarios, and have another member review the evidence.
- Keep tests under the same feature branch as the behaviour they verify, and record the observed result against the exact commit SHA.

## Decisions still open

- Map the five team members to the M1–M5 ownership roles in the class contract.
- Finalise the exact CLI command wording and input presentation before Stage 1 implementation.
- Assign the Stage 1 release coordinator and evidence reviewer.
- Confirm the submission deadline and lab-group archive name.

## Repository

- GitHub: <https://github.com/gsharsh/RestaurantRush>
- Assignment brief: [docs/SC2002_Restaurant_Rush_Group_Assignment.docx](docs/SC2002_Restaurant_Rush_Group_Assignment.docx)
