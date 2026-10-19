# RestaurantRush

RestaurantRush is the SC2002 Object-Oriented Design and Programming group assignment. The goal is to build a Java 17 command-line restaurant-management game in three cumulative, independently runnable stages.

> **Current status:** planning and repository scaffolding. No gameplay classes have been implemented and no stage tags have been created yet.

## Required releases

| Stage | Required addition | Git tag | Status |
| --- | --- | --- | --- |
| Stage 1 | Regular customers, menu, Waiter, Chef, Cashier, complete turn flow, victory and defeat | `stage-1` | Not started |
| Stage 2 | VIP and Critic customer behaviour implemented polymorphically | `stage-2` | Not started |
| Stage 3 | Host, ComboMeal and Happy Hour | `stage-3` | Not started |

Each tag must point to a commit that compiles and runs independently. Stage 1 source must not contain Stage 2 or Stage 3 implementations, and Stage 2 source must not contain Stage 3 implementations.

## Repository structure

```text
.
├── docs/
│   ├── class-contract.md       # Development plan, proposed contracts and team workflow
│   ├── ai-prompts/             # One concise AI-use record per feature branch
│   ├── testing/                # Stage test records and reproducible-run evidence
│   └── uml/                    # Editable Draw.io sources and reviewed exports
├── src/
│   ├── main/java/restaurantrush/
│   └── test/java/restaurantrush/
├── compile.sh
└── run.sh
```

The detailed design, ownership plan, Git workflow, testing process and evidence requirements are in [docs/class-contract.md](docs/class-contract.md).

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

## Development evidence

- Create one prompt record from [docs/ai-prompts/TEMPLATE.md](docs/ai-prompts/TEMPLATE.md) for each AI-assisted branch.
- Record observed tests using [docs/testing/TEST-TEMPLATE.md](docs/testing/TEST-TEMPLATE.md).
- Store the editable UML sources as `docs/uml/class-diagram.drawio` and `docs/uml/sequence-diagram.drawio`, together with reviewed PDF or PNG exports.
- Before creating a stage tag, compile the tagged commit, run its tests and scenarios, and have another member review the evidence.

## Decisions still open

- Map the five team members to the M1–M5 ownership roles in the class contract.
- Define the Critic's distinct Stage 2 order preference or service reaction. If no alternative is agreed at the Stage 2 gate, the documented fallback is to choose the highest-priced menu item.
- Finalise the exact CLI command wording and input presentation before Stage 1 implementation.
- Confirm the submission deadline, lab-group archive name, and final video segment ownership.
- Confirm whether the assignment brief itself should be committed or kept only as a local reference.

## Repository

- GitHub: <https://github.com/gsharsh/RestaurantRush>
- Assignment brief: `SC2002_Restaurant_Rush_Group_Assignment.docx` (currently a local, untracked reference)
