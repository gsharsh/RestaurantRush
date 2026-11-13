# RestaurantRush

RestaurantRush is the SC2002 Object-Oriented Design and Programming group assignment. The goal is to build a Java 17 command-line restaurant-management game in three cumulative, independently runnable stages.

> **Current stage:** Stage 3 (Host, ComboMeal and Happy Hour) is implemented, with
> its final class diagram, sequence diagram and reproducible victory/defeat runs.
> All 19 test runners and both saved scenarios passed at `5c583fb`; see the
> [Stage 3 test record](docs/testing/stage-3-tests.md). Team sign-off and the
> `stage-3` tag are pending. Stage 1 is preserved at `stage-1` (`1b9ebf9`) and
> Stage 2 at `stage-2` (`27b7c9f`).

## Required releases

| Stage | Required addition | Git tag | Status |
| --- | --- | --- | --- |
| Stage 1 | Regular customers, menu, Waiter, Chef, Cashier, complete turn flow, victory and defeat | `stage-1` | Published at `1b9ebf9` |
| Stage 2 | VIP and Critic customer behaviour implemented polymorphically | `stage-2` | Published at `27b7c9f` |
| Stage 3 | Host, ComboMeal and Happy Hour | `stage-3` | Implementation, tests, UML and replay evidence complete; sign-off/tag pending |

Each tag must point to a commit that compiles and runs independently. Stage 1 source must not contain Stage 2 or Stage 3 implementations, and Stage 2 source must not contain Stage 3 implementations.

Development proceeds one stage at a time. The [Stage 1 contract](docs/class-contracts/stage-1-class-contracts.md) records the gameplay baseline. The [Stage 2 addendum](docs/class-contracts/stage-2-class-contracts.md) records the customer extensions, and the [Stage 3 contract](docs/class-contracts/stage-3-class-contracts.md) records the Host, ComboMeal and Happy Hour design, gameplay choices and ownership.

## Repository structure

```text
.
├── docs/
│   ├── SC2002_Restaurant_Rush_Group_Assignment.docx # Assignment brief
│   ├── class-contracts/
│   │   ├── stage-1-class-contracts.md
│   │   ├── stage-2-class-contracts.md
│   │   └── stage-3-class-contracts.md
│   ├── ai-prompts/             # Shared template and stage-specific prompt folders
│   ├── testing/
│   │   ├── TEST-TEMPLATE.md
│   │   ├── stage-N-tests.md    # Verification record for each stage
│   │   └── stage-N-testscript/ # Replay inputs and saved outputs (stages 1-3)
│   └── uml/
│       ├── stage-1-uml/        # Editable Draw.io source and PNG export
│       ├── stage-2-uml/
│       └── stage-3-uml/        # Final class diagram and sequence diagram
├── src/restaurantrush/         # Production Java packages
├── tests/restaurantrush/ # Java test packages
├── compile.sh
├── test.sh
└── run.sh
```

The Stage 1 design and shared workflow are in [the Stage 1 contract](docs/class-contracts/stage-1-class-contracts.md). Customer rules are in [the Stage 2 addendum](docs/class-contracts/stage-2-class-contracts.md), and the Stage 3 additions are in [the Stage 3 contract](docs/class-contracts/stage-3-class-contracts.md).

## Prerequisites

- Java Development Kit 17 or newer
- Bash for the supplied helper scripts
- Draw.io/diagrams.net for class-diagram editing; [PlantUML](https://plantuml.com/sequence-diagram) for the main sequence diagram

The project is compiled with `javac --release 17`, even when a newer JDK is installed.

## Compile and run

From the repository root:

```bash
# Run the interactive game
./run.sh
```

`run.sh` compiles the production sources and launches
`restaurantrush.app.RestaurantRushApp`. Each turn may start with a `Happy Hour>`
prompt (`on` or `skip`, shown until Happy Hour is used), followed by the `Host>`,
`Waiter>` and `Chef>` prompts. Type `help` at a Waiter or Chef prompt to see the
available commands.

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
| `ComboMealTest` | M1 | Combo price (90% of the parts, rounded), summed preparation units and validation |
| `OrderTest` | M1 | Order states, preparation, service, payment, cancellation and locked billing |
| `CustomerTest` | M1 | Customer states, lifecycle timing, satisfaction, order assignment and Happy Hour recovery |
| `RegularCustomerTest` | M1 | Menu selection, price quotes and waiting-loss rules |
| `VIPCustomerTest` | M1 | VIP labels, five-point waiting loss, discounted quotes, rounding and lifecycle substitution |
| `CriticCustomerTest` | M2 | Critic labels, twelve-point waiting loss, preference/tie-breaking and full-price quotes |
| `ActionResultTest` | M3 | Success/failure results and message validation |
| `HappyHourTest` | M2 | One activation per game, two-turn duration, 20% off and turn validation |
| `RestaurantStateTest` | M2 | Queue, seating, order registration, serving, payment order, abandonment, read-only views and the four-item menu |
| `StaffTest` | M3 | Waiter, Chef and Cashier actions, including substitution by another `Customer` subtype |
| `StaffMixedCustomerTest` | M3 | Actual Regular/VIP/Critic staff flow, locked bills, paid-only totals and abandonment |
| `HostTest` | M3 | Host seating from the queue, full tables and invalid requests |
| `StaffHappyHourTest` | M3 | Happy Hour discount after the VIP discount, and bills that stay locked after it ends |
| `ArrivalScheduleTest` | M4 | All 18 turns, six mixed arrivals, labels, invalid turns and fresh instances |
| `TurnManagerTest` | M4 | Turn phases, Host → Waiter → Chef order, Happy Hour start and recovery, call guards and failed-start preservation |
| `GameControllerTest` | M4 | Host and staff action limits, failed-action preservation, arrival snapshots, lifecycle timing and victory boundaries |
| `ViewRecordsTest` | M5 | Immutable snapshot and view records, including Host and Happy Hour fields |
| `RestaurantCLITest` | M5 | Final-outcome display, Happy Hour and Host display, and rejection of unfinished/null snapshots |

### Reproducible victory and defeat runs

The current Stage 3 inputs in `docs/testing/stage-3-testscript/` replay two
18-turn games with Regular, VIP and Critic customers, the Host, the combo and
Happy Hour. Compare the game output with the saved `.out` file (no output from
`diff` means identical):

```bash
./compile.sh
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/stage-3-testscript/stage-3-victory.txt | diff docs/testing/stage-3-testscript/stage-3-victory.out -
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/stage-3-testscript/stage-3-defeat.txt | diff docs/testing/stage-3-testscript/stage-3-defeat.out -
```

In the victory replay, Happy Hour is turned on at turn 4, so VIP customer 2's
Burger is billed at $10.80 (1500, then 1350 after the VIP discount, then 1080 after
Happy Hour), and Critic customer 4 orders the Salad + Pasta Combo at $24.30. It
ends with 5 paid customers, $71.10 revenue and average paid satisfaction 84.8.
The defeat replay never uses Happy Hour and only waits; it ends with 0 paid
customers, $0.00 revenue, average N/A and 2 unhappy departures. The
[Stage 3 test record](docs/testing/stage-3-tests.md) records the commit, Java
version, all 19 test runners and observed results. Verification used OpenJDK
21.0.12 with `--release 17`; execution on Java 17 itself has not been verified.

The Stage 1 and Stage 2 test records, scripts/outputs and prompt records are
historical evidence. Replay each from its `stage-1` or `stage-2` tag; current
Stage 3 output intentionally differs (for example, the Host and Happy Hour
prompts).

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
- Keep each stage's diagrams in `docs/uml/stage-N-uml/`. The final class diagram in `docs/uml/stage-3-uml/` carries the encapsulation, polymorphism and composition notes required by brief §5.
- The sequence diagram in `docs/uml/stage-3-uml/` shows a VIP customer ordering during Happy Hour (turn 4 of the victory replay), from the CLI through `GameController`, `TurnManager`, `HappyHour`, `Host`, `Waiter` and `Chef`.
- Sequence exports, generated with PlantUML: [three-page PDF](docs/uml/stage-3-uml/sequence-diagram.pdf), [continuous PNG](docs/uml/stage-3-uml/sequence-diagram.png), [continuous SVG](docs/uml/stage-3-uml/sequence-diagram.svg) and [editable source](docs/uml/stage-3-uml/sequence-diagram.puml). The PDF contains one continuation per page, with selectable text and page bookmarks.
- The [class diagram PNG](docs/uml/stage-3-uml/class-diagram.png) and [editable Draw.io source](docs/uml/stage-3-uml/class-diagram.drawio) are in the same folder.
- Before creating each stage tag, compile the candidate commit, run its tests and victory/defeat scenarios, and have another member review the evidence.
- Keep tests under the same feature branch as the behaviour they verify, and record the observed result against the exact commit SHA.

The sequence follows the existing victory replay: VIP customer 2 arrives on turn 4, orders during Happy Hour, is served on turn 5 and pays on turn 7, followed by the result check after turn 18. The manager's Happy Hour choice changes the bill, as shown in the pricing note. The three PDF pages retain the same lifeline columns and continuous message numbering, with activation bars, relevant guards, `opt` branches, a remaining-turn `loop` and a final-result `alt`.

| Sequence page | Assignment evidence |
| --- | --- |
| 1 — Arrival, order and cooking | Manager activates Happy Hour; scheduled VIP arrival; Host seating; Waiter assignment; VIP and Happy Hour pricing; locked Order creation; Chef assignment and READY |
| 2 — Service and eating | Turn 5 recovery; READY → SERVED with C2 EATING; READY_TO_PAY on turn 6; payment delayed by the eligibility guard |
| 3 — Payment and result | Automatic payment on turn 7; table release; remaining-turn loop; all three victory targets and the observed final result |

To regenerate with PlantUML 1.2026.10, use the source's `newpage` breaks for the three continuations, or define `CONTINUOUS=1` for the complete timeline. Replace the JAR path below with your local installation; use `--svg` instead of `--png` for vector output. The PDF is assembled from the three SVG continuations with one diagram per page.

```bash
java -jar /path/to/plantuml.jar --png --output-dir /tmp/rr-sequence-pages docs/uml/stage-3-uml/sequence-diagram.puml
java -jar /path/to/plantuml.jar --define CONTINUOUS=1 --png --output-dir /tmp/rr-sequence-continuous docs/uml/stage-3-uml/sequence-diagram.puml
```

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

## Stage 2 → Stage 3 transition

The published `stage-2` tag identifies
[`27b7c9f`](https://github.com/gsharsh/RestaurantRush/commit/27b7c9f475b77ab3efa533e5e88317ea6c1b0e55).
The integrated Stage 3 implementation is at
[`5c583fb`](https://github.com/gsharsh/RestaurantRush/commit/5c583fb484eafb985eb432540a816a59407b75ee) (`main` after PRs #26 to #29);
the [Stage 2 → integrated Stage 3 comparison](https://github.com/gsharsh/RestaurantRush/compare/stage-2...5c583fb484eafb985eb432540a816a59407b75ee)
shows the complete implementation change. The evidence branch then adds the
replays, test record, diagrams and this README update, with no Java or build-script
changes. The `stage-1` and `stage-2` tags remain unchanged.

| Change | Files | Reason and implementation evidence |
| --- | --- | --- |
| New design contract | `docs/class-contracts/stage-3-class-contracts.md` | Agree the Stage 3 APIs, gameplay choices (combo on the menu, Host → Waiter → Chef order, rounding) and ownership before coding. [Contract](https://github.com/gsharsh/RestaurantRush/commit/c9025049e3586b35a2b2e62827aeb9fa2070eef7). |
| New combo meal | `src/restaurantrush/domain/ComboMeal.java` | A `MenuItem` subclass made of two items: preparation units are their sum and the price is 90% of their total, rounded. [ComboMeal](https://github.com/gsharsh/RestaurantRush/commit/ebe0062e37ee85d0b677a65803b0a2de369a3dc7). |
| Changed customer | `src/restaurantrush/domain/Customer.java` | Add `recoverWaitingSatisfaction(int)` so Happy Hour can restore 15 satisfaction to waiting customers, capped at 100. [Recovery](https://github.com/gsharsh/RestaurantRush/commit/4379ca3d961c6cfd29a044754d5e1d429766da66). |
| New Happy Hour policy and changed menu | `src/restaurantrush/domain/HappyHour.java`, `Restaurant.java` | One activation per game, lasting that turn and the next, 20% off new orders. The Salad + Pasta Combo is added as the fourth menu item. [Policy and menu](https://github.com/gsharsh/RestaurantRush/commit/0d3c20664caa7a9a4095a1a22208130890635a3f). |
| New Host | `src/restaurantrush/staff/Host.java` | Seat one queued customer per turn in addition to the Waiter's task. [Host](https://github.com/gsharsh/RestaurantRush/commit/0e3e43a0b2e1a8b3e1d2ab0dd2a38eeeb6e4eb47). |
| Changed Waiter | `src/restaurantrush/staff/Waiter.java` | Apply the shared Happy Hour policy after the customer's own quote when taking an order; the bill stays locked afterwards. [Waiter pricing](https://github.com/gsharsh/RestaurantRush/commit/37a951408d21505583d22434b122508995929084). |
| Changed turn flow | `src/restaurantrush/control/TurnManager.java`, `GameController.java` | Track the Host's task, enforce Host → Waiter → Chef, activate Happy Hour at the start of a turn and apply recovery while it is active. [Turn flow](https://github.com/gsharsh/RestaurantRush/commit/2bbee0add19ddac957e37fea49a522135e27e4c7). |
| Changed boundary and views | `src/restaurantrush/boundary/RestaurantCLI.java`, `src/restaurantrush/view/GameSnapshot.java`, `src/restaurantrush/app/RestaurantRushApp.java` | Ask about Happy Hour only while it is unused, add the `Host>` prompt, show turns left and staff status, and wire one shared `HappyHour` and `Host`. [CLI and views](https://github.com/gsharsh/RestaurantRush/commit/28850f9096ee8eac626e87760245885096e65170), [merge with the turn flow](https://github.com/gsharsh/RestaurantRush/commit/3646b36d45e8dbde1dfc339cca91d2062351f55d). |
| New tests | `tests/restaurantrush/domain/ComboMealTest.java`, `HappyHourTest.java`; `tests/restaurantrush/staff/HostTest.java`, `StaffHappyHourTest.java` | Verify each new class and the Happy Hour pricing and locked bills. [Staff Happy Hour tests](https://github.com/gsharsh/RestaurantRush/commit/498b72e2b13c0d1214bb61bf82ff4c63d5297980); other tests come with the commits above. |
| Extended tests | `tests/restaurantrush/domain/CustomerTest.java`, `RegularCustomerTest.java`, `VIPCustomerTest.java`, `CriticCustomerTest.java`, `RestaurantStateTest.java`; `tests/restaurantrush/staff/StaffTest.java`, `StaffMixedCustomerTest.java`; `tests/restaurantrush/control/TurnManagerTest.java`, `GameControllerTest.java`; `tests/restaurantrush/view/ViewRecordsTest.java`; `tests/restaurantrush/boundary/RestaurantCLITest.java` | Cover recovery, combo selection over the four-item menu, the Host's task and the new snapshot fields. [Combo selection tests](https://github.com/gsharsh/RestaurantRush/commit/d649608e70da87e91834eaddad8989150542d7d1); the rest come with the commits above. |
| New Stage 3 evidence | `docs/testing/stage-3-tests.md`; `docs/testing/stage-3-testscript/stage-3-{victory,defeat}.{txt,out}`; `docs/uml/stage-3-uml/class-diagram.{drawio,png}`; `docs/uml/stage-3-uml/sequence-diagram.{puml,png,pdf}`; `docs/ai-prompts/stage-3-prompts/*.md` | Record tests, replay inputs and outputs, the final annotated class diagram, the sequence diagram and AI assistance. [Replays and test record](https://github.com/gsharsh/RestaurantRush/commit/af3b0db36a07d6a3e1a88e98b9353c6e1e96b0b9), [diagrams](https://github.com/gsharsh/RestaurantRush/commit/7d8375844010ee403bbb1356711d02e1fceb7541). |
| Changed documentation | `README.md` | Record the Stage 3 status, tests, replays and this transition. This documentation update. |
| Removed placeholders | `.gitkeep` under `docs/testing/stage-3-testscript/` and `docs/uml/stage-3-uml/` | Removed once real evidence files were added. No class or test was deleted or moved in Stage 3. |

Most Stage 2 code carried over unchanged. Customers kept their own selection and
quote rules, so the combo needed no customer subclass changes beyond the shared
recovery method, and Happy Hour is applied in one place (the Waiter) after the
customer's quote. `Waiter` still has its older no-argument constructor and
two-argument `takeOrder`, which are kept for existing tests.

## Repository

- GitHub: <https://github.com/gsharsh/RestaurantRush>
- Assignment brief: [docs/SC2002_Restaurant_Rush_Group_Assignment.docx](docs/SC2002_Restaurant_Rush_Group_Assignment.docx)
