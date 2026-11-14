# RestaurantRush

RestaurantRush is the SC2002 Object-Oriented Design and Programming assignment: a Java command-line restaurant game developed in three independently runnable stages.

- **Repository:** [gsharsh/RestaurantRush](https://github.com/gsharsh/RestaurantRush)
- **Assignment brief:** [SC2002 Restaurant Rush](docs/SC2002_Restaurant_Rush_Group_Assignment.docx)
- **Current status:** Stage 3 implementation, tests and final UML are complete. The `stage-3` tag and final video details remain to be added.

## Build and run

Requires JDK 17 or newer and Bash. The scripts compile with `javac --release 17`; see the [test record](docs/testing/stage-3-tests.md) for the runtime used during verification.

```bash
./run.sh       # Compile and launch the game
./compile.sh   # Compile without launching
```

Each turn offers `Happy Hour> on/skip` until Happy Hour is used, then one `Host>`, `Waiter>` and `Chef>` choice in that order. The Host can seat a customer, the Waiter can seat/order/serve, and the Chef can cook one preparation unit. Each can also `wait`; type `help` for the available commands.

### Running the tests

```bash
./test.sh                                          # Run all 19 test classes
./test.sh restaurantrush.control.GameControllerTest # Run one test class
./compile.sh --tests                               # Compile tests without running
```

Tests cover customer rules and lifecycle, pricing and locked bills, staff limits, turn timing, restaurant state, immutable views and CLI behaviour. Detailed results are in the [Stage 3 test record](docs/testing/stage-3-tests.md).

### Reproducible victory and defeat runs

```bash
./compile.sh
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/stage-3-testscript/stage-3-victory.txt | diff docs/testing/stage-3-testscript/stage-3-victory.out -
java -cp out/classes restaurantrush.app.RestaurantRushApp < docs/testing/stage-3-testscript/stage-3-defeat.txt | diff docs/testing/stage-3-testscript/stage-3-defeat.out -
```

No output from `diff` means the replay matches its saved transcript.

| Run | Paid customers | Revenue | Average paid satisfaction | Outcome |
| --- | --- | --- | --- | --- |
| Victory | 5 | $71.10 | 84.8 | Victory |
| Defeat | 0 | $0.00 | N/A | Defeat |

Both runs complete 18 turns. The victory replay activates Happy Hour on turn 4: VIP C2's Burger costs $10.80 after both discounts, and Critic C4 orders the $24.30 Salad + Pasta Combo. The defeat replay skips Happy Hour and all staff actions, producing two unhappy departures.

Replay earlier stages from their tags; their historical inputs and outputs are stored under `docs/testing/stage-1-testscript/` and `stage-2-testscript/`.

## Final UML

| Diagram | Editable source | Exports |
| --- | --- | --- |
| Class diagram | [Draw.io](docs/uml/stage-3-uml/class-diagram.drawio) | [PNG](docs/uml/stage-3-uml/class-diagram.png) |
| Sequence diagram | [PlantUML](docs/uml/stage-3-uml/sequence-diagram.puml) | [Three-page PDF](docs/uml/stage-3-uml/sequence-diagram.pdf), [continuous PNG](docs/uml/stage-3-uml/sequence-diagram.png), [SVG](docs/uml/stage-3-uml/sequence-diagram.svg) |

The class diagram includes the final types, relationships and examples of encapsulation, polymorphism and composition. The sequence follows VIP C2 through the victory replay, showing how the manager's Happy Hour choice changes the bill.

| PDF page | Scenario |
| --- | --- |
| 1 | Turn 4 arrival, Host seating, Waiter ordering, VIP/Happy Hour pricing and Chef preparation to READY |
| 2 | Turn 5 service to SERVED/EATING, then READY_TO_PAY on turn 6 with payment delayed by its guard |
| 3 | Turn 7 payment and table release, remaining-turn loop and the final result check after turn 18 |

<details>
<summary>Regenerate the sequence diagram</summary>

Use PlantUML 1.2026.10, replacing the JAR path below with your installation. The source's `newpage` breaks generate three continuations; `CONTINUOUS=1` generates the complete timeline. Use `--svg` instead of `--png` for vector output. The PDF combines the three SVG continuations, one per page.

```bash
java -jar /path/to/plantuml.jar --png --output-dir /tmp/rr-sequence-pages docs/uml/stage-3-uml/sequence-diagram.puml
java -jar /path/to/plantuml.jar --define CONTINUOUS=1 --png --output-dir /tmp/rr-sequence-continuous docs/uml/stage-3-uml/sequence-diagram.puml
```

</details>

## Repository structure

| Location | Contents |
| --- | --- |
| `src/restaurantrush/` | Production Java packages: app, boundary, control, domain, staff and view |
| `tests/restaurantrush/` | Self-contained Java test runners |
| [Class contracts](docs/class-contracts/) | [Stage 1 baseline](docs/class-contracts/stage-1-class-contracts.md), [Stage 2](docs/class-contracts/stage-2-class-contracts.md) and [Stage 3](docs/class-contracts/stage-3-class-contracts.md) |
| [Testing evidence](docs/testing/) | Stage-specific test records and replay inputs/outputs |
| [UML](docs/uml/) | Stage-specific diagrams and editable sources |
| [AI-assistance records](docs/ai-prompts/) | Member/branch prompt records and the shared template |

### Team ownership

| Member | Responsibility | Stage 3 addition |
| --- | --- | --- |
| M1 - Harshith | Customers, orders and menu items | ComboMeal and customer recovery |
| M2 - Vedavit | Restaurant state and Critic behaviour | HappyHour policy and menu integration |
| M3 - Hunar | Staff and action results | Host and Waiter pricing |
| M4 - Viti | Game controller, turns and arrivals | Host task limits and Happy Hour timing |
| M5 - Devansh | CLI, views, app wiring and evidence | Display, replays and UML coordination |

## Stage releases

| Tag | Runnable scope | Status |
| --- | --- | --- |
| `stage-1` | Regular customers and the complete core game | `1b9ebf9` |
| `stage-2` | VIP and Critic behaviour | `27b7c9f` |
| `stage-3` | Host, ComboMeal and Happy Hour | Tag pending |

The tables below record the new, changed, moved and deleted files required by brief Section 5. Java paths are relative to `src/restaurantrush/`; test paths are relative to `tests/restaurantrush/` unless stated otherwise.

## Stage 1 → Stage 2 transition

[Compare the runnable Stage 1 and Stage 2 tags](https://github.com/gsharsh/RestaurantRush/compare/stage-1...stage-2).

| Change | Files | Reason |
| --- | --- | --- |
| New customer types | `domain/VIPCustomer.java`, `domain/CriticCustomer.java` | Add VIP discount/five-point loss and Critic preference/twelve-point loss through polymorphism. |
| Changed customer API | `domain/Customer.java`, `domain/RegularCustomer.java` | Add `typeLabel()` while preserving the shared lifecycle. |
| Changed arrivals and display | `control/ArrivalSchedule.java`, `view/CustomerView.java`, `boundary/RestaurantCLI.java` | Schedule VIP/Critic arrivals and display customer-owned type labels. |
| New tests | `control/ArrivalScheduleTest.java`, `domain/VIPCustomerTest.java`, `domain/CriticCustomerTest.java`, `staff/StaffMixedCustomerTest.java` | Verify the new rules, arrival mapping and mixed-customer staff/payment flow. |
| Changed tests | `domain/CustomerTest.java`, `domain/RegularCustomerTest.java`, `control/GameControllerTest.java`, `staff/StaffTest.java`, `view/ViewRecordsTest.java`, `boundary/RestaurantCLITest.java` | Cover subtype substitution, labels and updated view constructors. |
| Moved source and tests | `src/main/java/restaurantrush/` → `src/restaurantrush/`; `src/test/java/restaurantrush/` → `tests/restaurantrush/` | Simplify the source layout; unchanged classes retain their Stage 1 implementation. |
| Changed tooling and documentation | `compile.sh`, `test.sh`, `README.md`, `docs/testing/stage-1-tests.md` | Use the new roots and update instructions/evidence links. |
| New evidence | `docs/class-contracts/stage-2-class-contracts.md`, `docs/testing/stage-2-tests.md`, `docs/testing/stage-2-testscript/*`, `docs/uml/stage-2-uml/*`, `docs/ai-prompts/stage-2-prompts/*` | Record contracts, tests, replays, UML and AI assistance. |
| Moved historical evidence | `docs/class-contracts.md` → `docs/class-contracts/stage-1-class-contracts.md`; `docs/testing/scenarios/` → `docs/testing/stage-1-testscript/`; Stage 1 prompt files → `docs/ai-prompts/stage-1-prompts/`; original UML → `docs/uml/stage-1-uml/` | Preserve Stage 1 evidence in stage-specific folders. |

No gameplay class was deleted. The old paths were replaced by the moves above.

## Stage 2 → Stage 3 transition

[Compare Stage 2 with the integrated Stage 3 implementation](https://github.com/gsharsh/RestaurantRush/compare/stage-2...5c583fb484eafb985eb432540a816a59407b75ee). [Evidence and final UML](https://github.com/gsharsh/RestaurantRush/compare/5c583fb484eafb985eb432540a816a59407b75ee...26d7d38) were merged afterwards. The `stage-3` tag is pending.

| Change | Files | Reason |
| --- | --- | --- |
| New domain classes | `domain/ComboMeal.java`, `domain/HappyHour.java` | Add combined preparation/90% combo pricing and the two-turn Happy Hour policy. |
| New staff class | `staff/Host.java` | Allow one additional seating action per turn. |
| Changed domain | `domain/Customer.java`, `domain/Restaurant.java` | Add capped waiting-satisfaction recovery and register the shared-component combo. |
| Changed staff | `staff/Waiter.java` | Apply Happy Hour after the customer's quote and lock the final bill. |
| Changed control | `control/TurnManager.java`, `control/GameController.java` | Enforce Host → Waiter → Chef and integrate activation, recovery and duration. |
| Changed boundary and views | `app/RestaurantRushApp.java`, `boundary/RestaurantCLI.java`, `view/GameSnapshot.java` | Share one HappyHour, wire Host, collect choices and display the new state. |
| New tests | `domain/ComboMealTest.java`, `domain/HappyHourTest.java`, `staff/HostTest.java`, `staff/StaffHappyHourTest.java` | Verify the new classes, pricing and locked bills. |
| Changed tests | `domain/{Customer,RegularCustomer,VIPCustomer,CriticCustomer,RestaurantState}Test.java`, `staff/{Staff,StaffMixedCustomer}Test.java`, `control/{TurnManager,GameController}Test.java`, `view/ViewRecordsTest.java`, `boundary/RestaurantCLITest.java` | Cover expanded-menu choices, recovery, task limits, timing and new view fields. |
| New evidence | `docs/class-contracts/stage-3-class-contracts.md`, `docs/testing/stage-3-tests.md`, `docs/testing/stage-3-testscript/*`, `docs/uml/stage-3-uml/*`, `docs/ai-prompts/stage-3-prompts/*` | Record the final contracts, replays, diagrams, verification and AI assistance. |
| Changed documentation | `README.md` | Document Stage 3 usage, evidence and both stage transitions. |

No gameplay class or test was deleted or moved. Within Stage 3, the initial Draw.io sequence source was replaced by PlantUML and updated PNG/SVG/PDF exports. Compatibility Waiter overloads remain in use by the regression tests.

## Recorded video

Video filename/link and timestamps have not yet been provided. Brief Section 6.2 requires these seven segments:

| Segment | Timestamp |
| --- | --- |
| Launch from the submitted project | Pending |
| Manager choices with constrained staff | Pending |
| READY → SERVED → payment | Pending |
| VIP or Critic behaviour | Pending |
| A Stage 3 feature | Pending |
| Victory and defeat | Pending |
| Stage tag comparisons and UML diagrams | Pending |
