# RestaurantRush

RestaurantRush is the SC2002 Object-Oriented Design and Programming group assignment. The goal is to build a Java 17 command-line restaurant-management game in three cumulative, independently runnable stages.

> **Current stage:** Stage 2 gameplay, customer-type display, class diagram and
> reproducible victory/defeat evidence are implemented. All 15 test runners and
> both saved scenarios passed on candidate `97463b1`; see the
> [Stage 2 test record](docs/testing/stage-2-tests.md). Team release sign-off and
> publication of `stage-2` remain pending. Stage 1 is preserved at `stage-1` (`1b9ebf9`).

## Required releases

| Stage | Required addition | Git tag | Status |
| --- | --- | --- | --- |
| Stage 1 | Regular customers, menu, Waiter, Chef, Cashier, complete turn flow, victory and defeat | `stage-1` | Published at `1b9ebf9` |
| Stage 2 | VIP and Critic customer behaviour implemented polymorphically | `stage-2` | Implementation, tests, UML and replay evidence complete; release sign-off/tag pending |
| Stage 3 | Host, ComboMeal and Happy Hour | `stage-3` | Not started |

Each tag must point to a commit that compiles and runs independently. Stage 1 source must not contain Stage 2 or Stage 3 implementations, and Stage 2 source must not contain Stage 3 implementations.

Development proceeds one stage at a time. The [Stage 1 contract](docs/class-contracts/stage-1-class-contracts.md) records the gameplay baseline. The [Stage 2 addendum](docs/class-contracts/stage-2-class-contracts.md) records the implemented customer extensions, ownership, acceptance checks and remaining release tasks.

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
│   │   └── stage-2-testscript/  # Stage 2 replay folder
│   └── uml/
│       ├── stage-1-uml/        # Editable Draw.io source and PNG export
│       └── stage-2-uml/
├── src/restaurantrush/         # Production Java packages
├── tests/restaurantrush/ # Java test packages
├── compile.sh
├── test.sh
└── run.sh
```

The Stage 1 design and shared workflow are in [the Stage 1 contract](docs/class-contracts/stage-1-class-contracts.md). Current customer rules and handoffs are in [the Stage 2 addendum](docs/class-contracts/stage-2-class-contracts.md).

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
| `VIPCustomerTest` | M1 | VIP labels, five-point waiting loss, discounted quotes, rounding and lifecycle substitution |
| `CriticCustomerTest` | M2 | Critic labels, twelve-point waiting loss, preference/tie-breaking and full-price quotes |
| `ActionResultTest` | M3 | Success/failure results and message validation |
| `RestaurantStateTest` | M2 | Queue, seating, order registration, serving, payment order, abandonment and read-only views |
| `StaffTest` | M3 | Waiter, Chef and Cashier actions, including substitution by another `Customer` subtype |
| `StaffMixedCustomerTest` | M3 | Actual Regular/VIP/Critic staff flow, locked bills, paid-only totals and abandonment |
| `ArrivalScheduleTest` | M4 | All 18 turns, six mixed arrivals, labels, invalid turns and fresh instances |
| `TurnManagerTest` | M4 | Turn phases, duplicate/premature call guards and failed-start preservation |
| `GameControllerTest` | M4 | Action limits, failed-action preservation, arrival snapshots, lifecycle timing and victory boundaries |
| `ViewRecordsTest` | M5 | Immutable snapshot and view records |
| `RestaurantCLITest` | M5 | Final-outcome display and rejection of unfinished/null snapshots |

### Reproducible victory and defeat runs

The current Stage 2 inputs in `docs/testing/stage-2-testscript/` replay separate
18-turn games containing Regular, VIP and Critic customers. Compare the game
output with the saved `.out` file (no output from `diff` means identical):

```bash
./compile.sh
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/stage-2-testscript/stage-2-victory.txt | diff docs/testing/stage-2-testscript/stage-2-victory.out -
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/stage-2-testscript/stage-2-defeat.txt | diff docs/testing/stage-2-testscript/stage-2-defeat.out -
```

The victory replay ends with 4 paid customers, $58.50 revenue and average paid
satisfaction 75.5. The defeat replay ends with 0 paid customers, $0.00 revenue,
average N/A and 2 unhappy departures. The [Stage 2 test record](docs/testing/stage-2-tests.md)
records the candidate, Java versions, all 15 test runners and observed results.
Verification used Java 26.0.2 with `--release 17`; execution on Java 17 itself has
not been verified.

The [Stage 1 test record](docs/testing/stage-1-tests.md), Stage 1 scripts/outputs and
Stage 1 prompt records are historical evidence. Replay Stage 1 from its `stage-1`
tag using that version's paths; current Stage 2 output intentionally differs.

## Team ownership

The table below records Stage 1 ownership. The [Stage 2 addendum](docs/class-contracts/stage-2-class-contracts.md#stage-2-handoffs-and-ownership) assigns VIP implementation to Harshith, Critic implementation to Vedavit, staff/payment regressions to Hunar, arrival/controller work to Viti, and CLI presentation/evidence to Devansh. Each member supplies their own UML details, with Devansh coordinating the diagram.

Type labels are implemented in Stage 2: Harshith added `public abstract String typeLabel()` to `Customer` and the Regular/VIP implementations, Vedavit added the Critic implementation, and Devansh added `String type` to `CustomerView` and its CLI display, coordinating affected snapshots with Viti. The labels are `Regular`, `VIP` and `Critic`; the view copies `customer.typeLabel()` and the CLI uses `type()` without concrete-type checks.

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
- Keep the Stage 1 diagram and export in `docs/uml/stage-1-uml/`; the implemented Stage 2 class diagram and PNG export are in `docs/uml/stage-2-uml/`.
- After Stage 3 is complete, annotate the final class diagram with encapsulation, polymorphism and composition examples required by brief §5. Also choose a scenario, assign ownership and complete the sequence diagram then; it must show a Stage 2 or Stage 3 behaviour, with VIP pricing as a possible scenario. These final diagram tasks are deferred until after Stage 3 and do not gate the Stage 2 release.
- Before creating each stage tag, compile the candidate commit, run its tests and victory/defeat scenarios, and have another member review the evidence.
- Keep tests under the same feature branch as the behaviour they verify, and record the observed result against the exact commit SHA.

## Stage 1 → Stage 2 transition

The published `stage-1` tag identifies
[`1b9ebf9`](https://github.com/gsharsh/RestaurantRush/commit/1b9ebf92330316628010880245f98a67416aaef9).
The integrated Stage 2 implementation and evidence are at
[`f9e7408`](https://github.com/gsharsh/RestaurantRush/commit/f9e7408f550d10625f683c53c879b7ce239d564c);
the [Stage 1 → integrated Stage 2 comparison](https://github.com/gsharsh/RestaurantRush/compare/stage-1...f9e7408f550d10625f683c53c879b7ce239d564c)
shows the complete implementation transition. Candidate `97463b1ce1481806c5861047f7a16bb71b0674e2`
adds removal of three obsolete `.gitkeep` files, with no Java or build-script changes.
The final `stage-2` release commit must include this documentation update as well
as the verified implementation. The `stage-1` tag remains unchanged.

Paths below use the current layout unless an old path is shown explicitly.

| Change | Files | Reason and implementation evidence |
| --- | --- | --- |
| New customer types | `src/restaurantrush/domain/VIPCustomer.java`, `CriticCustomer.java` | Add five-point waiting loss and 10% off for VIP; twelve-point loss, full price and preparation/price/menu-order preference for Critic. [VIP implementation](https://github.com/gsharsh/RestaurantRush/commit/afef6b6ded306759034407ecee458a5595d074de), [Critic implementation](https://github.com/gsharsh/RestaurantRush/commit/863cef92efb986eff225e5fc752693e584333b5d). |
| Changed customer contract | `src/restaurantrush/domain/Customer.java`, `RegularCustomer.java` | Add polymorphic `typeLabel()`; preserve the shared lifecycle and Regular rules. [Label API](https://github.com/gsharsh/RestaurantRush/commit/5e86818543e698f06cbab45ed2474ace974f78fc). |
| Changed arrival setup | `src/restaurantrush/control/ArrivalSchedule.java` | Construct VIP on turn 4 and Critic on turn 10; retain six scheduled arrivals and fresh instances. [Arrival mapping](https://github.com/gsharsh/RestaurantRush/commit/4c44fcccd683c69ab492c1c8a80bdb98dc0771fb). |
| Changed display | `src/restaurantrush/view/CustomerView.java`, `src/restaurantrush/boundary/RestaurantCLI.java` | Copy and display customer labels in queues, tables and customer rows. Gameplay remains in domain/control objects. [View/CLI change](https://github.com/gsharsh/RestaurantRush/commit/d3bd7d1c23b266c6ec4c6a3b9bd074ff65e434a4), [table labels](https://github.com/gsharsh/RestaurantRush/commit/2df93134b3929265ad3ddde143faeff60690ccb2). |
| New tests | `tests/restaurantrush/domain/VIPCustomerTest.java`, `CriticCustomerTest.java`; `tests/restaurantrush/control/ArrivalScheduleTest.java`; `tests/restaurantrush/staff/StaffMixedCustomerTest.java` | Verify each new rule, all arrival turns and real mixed-customer staff/payment behaviour. [Mixed-customer regressions](https://github.com/gsharsh/RestaurantRush/commit/a7afd1417eb4ff3daa2078f75e4854da18a97479); other tests accompany the customer/arrival commits above. |
| Extended tests | `tests/restaurantrush/domain/CustomerTest.java`, `RegularCustomerTest.java`; `tests/restaurantrush/control/GameControllerTest.java`; `tests/restaurantrush/staff/StaffTest.java`; `tests/restaurantrush/view/ViewRecordsTest.java`; `tests/restaurantrush/boundary/RestaurantCLITest.java` | Add Regular/VIP lifecycle substitution, label assertions and compatibility updates for test subclasses/views. [Lifecycle tests](https://github.com/gsharsh/RestaurantRush/commit/28047699139c75cdce5d0e52b19d99de13e10e17), label/view commits above. |
| New Stage 2 evidence | `docs/class-contracts/stage-2-class-contracts.md`; `docs/testing/stage-2-tests.md`; `docs/testing/stage-2-testscript/stage-2-{victory,defeat}.{txt,out}`; `docs/uml/stage-2-uml/class-diagram.{drawio,png}`; `docs/ai-prompts/stage-2-prompts/*.md` | Document rules, tests, replay inputs/outputs, editable UML/export and AI assistance. [Scenarios](https://github.com/gsharsh/RestaurantRush/commit/d4d130451710573a6adcb2e62f586df64a687a45), [portable outputs/history clarification](https://github.com/gsharsh/RestaurantRush/commit/e5a381e033586a39b5ffd2503ea2aa71a9f1abcf), [UML](https://github.com/gsharsh/RestaurantRush/commit/e1cf6c6612e3eb327e688872e310b4de9fa89bee). |
| Moved source and tests | `src/main/java/restaurantrush/` → `src/restaurantrush/`; `src/test/java/restaurantrush/` → `tests/restaurantrush/` | Simplify the folder layout. Classes other than the explicitly changed customer/arrival/display files retain their Stage 1 implementation. [Folder reorganisation](https://github.com/gsharsh/RestaurantRush/commit/1dadd0bbda58403d33afb2b7f7dd795dabdd8792). |
| Changed tooling and existing documentation | `compile.sh`, `test.sh`, `README.md`, `docs/testing/stage-1-tests.md` | Use the new source/test roots, validate test discovery and repair documentation paths; README now records the actual transition and Stage 2 checks. Same reorganisation commit and this documentation update. |
| Moved historical evidence | `docs/class-contracts.md` → `docs/class-contracts/stage-1-class-contracts.md`; `docs/testing/scenarios/` → `docs/testing/stage-1-testscript/`; Stage 1 prompt `.md` files → `docs/ai-prompts/stage-1-prompts/`; `docs/uml/class-diagram.drawio` and `class-diagram-high-resolution.png` → `docs/uml/stage-1-uml/` | Separate stage evidence while preserving the runnable Stage 1 tag and historical scenarios. Same reorganisation commit. |
| Removed placeholders | `.gitkeep` under `docs/ai-prompts/stage-2-prompts/`, `docs/testing/stage-2-testscript/` and `docs/uml/stage-2-uml/` | Remove placeholders after real evidence files were added (`97463b1`). No gameplay class was deleted; old source/test/documentation paths were replaced by the moves above. |

The controller, turn manager, Restaurant, Order and staff implementations did not
need Stage 2 rule rewrites: they already use the abstract `Customer`, a shared
order and a locked customer-quoted bill. The new subclasses supply their own
selection, quotation, waiting-loss and display rules. Host, ComboMeal and Happy
Hour are deferred to Stage 3.

## Repository

- GitHub: <https://github.com/gsharsh/RestaurantRush>
- Assignment brief: [docs/SC2002_Restaurant_Rush_Group_Assignment.docx](docs/SC2002_Restaurant_Rush_Group_Assignment.docx)
