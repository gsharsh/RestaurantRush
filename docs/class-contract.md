# RestaurantRush class contract and development plan

## Document status

This document is the team's working design agreement for RestaurantRush. It records planned responsibilities, collaboration boundaries, stage gates, evidence requirements and proposed Java interfaces. It is not evidence that a class or feature has already been implemented.

The assignment brief remains authoritative. If this document conflicts with the brief, correct this document before writing or merging code. Stage-specific sections marked **planned** must not be represented as completed work.

## 1. Delivery plan

RestaurantRush will be delivered as three cumulative, independently playable command-line releases.

| Release | Behaviour required at that snapshot | Tag |
| --- | --- | --- |
| Stage 1 | Six Regular-customer arrivals, base menu, two tables, Waiter, Chef, Cashier, all 18 turns, satisfaction, abandonment, payment, victory and defeat | `stage-1` |
| Stage 2 | VIP and Critic customers added through polymorphic customer behaviour | `stage-2` |
| Stage 3 | Host, a composed ComboMeal and one-use two-turn Happy Hour | `stage-3` |

The source at `stage-1` must contain no VIP, Critic, Host, ComboMeal or Happy Hour implementation. The source at `stage-2` must contain no Stage 3 implementation. General Stage 1 abstractions may be extensible, but they must have a real Stage 1 responsibility rather than existing only as future placeholders.

### Technical baseline

- Java 17 language target, compiled with plain `javac`.
- Command-line interface only; all mandatory behaviour works offline.
- Production packages live under `src/main/java/restaurantrush`.
- Test packages live under `src/test/java/restaurantrush`.
- Draw.io is the editable UML source format.
- Money is stored in integer cents and displayed with two decimal places.
- Build output belongs under `out/` and is never committed.

## 2. Ownership and collaboration

The five roles are placeholders until member names are assigned.

| Owner | Area | Principal types | Evidence owned |
| --- | --- | --- | --- |
| M1 | Customer and order domain | `Customer`, `RegularCustomer`, `Order`, `MenuItem`, lifecycle enums | Domain tests and inheritance review |
| M2 | Restaurant state | `Restaurant`, `Table`, queues, histories and statistics | State-invariant, payment and abandonment tests |
| M3 | Staff actions | `Waiter`, `Chef`, `Cashier`, `ActionResult` | Staff validation and timing tests |
| M4 | Game control | `GameController`, `TurnManager`, configuration | Turn-flow and result tests |
| M5 | Boundary and integration | `RestaurantRushApp`, `RestaurantCLI`, immutable snapshots | Scripted runs, README and Draw.io coordination |

Ownership identifies the first implementer and reviewer contact; it does not give one member exclusive control over a class. Cross-owner contracts must be agreed before either side is merged:

- M1-M2: seating, service, payment and abandonment transitions.
- M1-M3: order creation, shared order identity and preparation.
- M2-M3: table occupancy, automatic payment and resource release.
- M3-M4: staff limits and Waiter-before-Chef sequencing.
- M4-M5: commands, retries, snapshots and end-of-turn display.

Every feature branch requires review from at least one adjacent subsystem owner. Every member contributes tests and report material for their area.

## 3. Shared design rules

1. Fields are private. State changes occur through methods that enforce lifecycle rules; public setters are not used.
2. `RestaurantCLI` reads and prints but contains no game rules.
3. `GameController` coordinates manager requests but does not reproduce entity or turn rules.
4. `TurnManager` is the sole authority for turn phase and per-turn staff availability.
5. `Restaurant` coordinates mutations spanning multiple entities and validates the entire request before changing state.
6. `Order` owns preparation and order-status transitions.
7. Customer-specific waiting, selection and pricing behaviour is dispatched through customer objects, not `instanceof` chains.
8. Invalid player choices return a failed `ActionResult`, preserve state and do not consume a staff task. Invalid internal lifecycle calls may throw `IllegalStateException`.
9. Historical paid and departed customers remain available for reporting but are excluded from active queues.
10. Snapshots given to the CLI are immutable and contain no mutable entity references.
11. An order's price is calculated and locked when the order is taken.
12. Collections exposed outside their owner are immutable copies or read-only views.

### Base configuration

| Setting | Required value |
| --- | --- |
| Turns | Exactly 18; no early ending |
| Tables | Two |
| Arrivals | Turns 1, 4, 7, 10, 13 and 16 |
| Staff capacity | One Waiter task, one Chef unit and at most one automatic Cashier payment per turn |
| Menu | Salad: 900 cents/1 unit; Burger: 1500 cents/1 unit; Pasta: 1800 cents/2 units |
| Stage 1 selection | `(customerId - 1) % menu.size()` |
| Victory | At least 4 paid customers, at least 4500 cents revenue and paid-customer average satisfaction at least 60 after turn 18 |

## 4. Stage 1 proposed class contracts

These signatures are the baseline to review before implementation. Small constructor or naming refinements are allowed only when all affected owners update this contract, UML and tests together.

### Boundary and view

| Type | Responsibility | Key public contract |
| --- | --- | --- |
| `RestaurantRushApp` | Construct one shared game object graph and launch the CLI | `public static void main(String[] args)` |
| `RestaurantCLI` | Parse commands, retry invalid input and render state/outcome | `run()`, `displayState(GameSnapshot)`, `displayOutcome(GameSnapshot, boolean)` |
| `GameSnapshot` | Immutable state required by the CLI | Java record containing turn, totals, queue, customer/table views and staff availability |
| `CustomerView` | Immutable customer display data | Java record containing identity, satisfaction, status, table and order summary |
| `TableView` | Immutable table display data | Java record containing table ID and optional occupant ID |
| `ActionResult` | Report accepted or rejected player actions without printing | `ok(String)`, `fail(String)`, `success()`, `message()` |

Malformed commands and non-integer IDs are boundary errors. They must be explained and retried without invoking domain actions. End-of-input stops safely rather than inventing completed turns.

### Control

| Type | Responsibility | Key public contract |
| --- | --- | --- |
| `GameController` | Accept manager actions, delegate to staff/domain objects and expose snapshots | `startTurn()`, `seatCustomer(int,int)`, `takeOrder(int)`, `serveOrder(int)`, `prepareOrder(int)`, `waitWaiter()`, `waitChef()`, `finishTurn()`, `snapshot()`, `isFinished()`, `isVictory()` |
| `TurnManager` | Own phase, arrivals, task limits, automatic events and advancement | `beginTurn(Restaurant)`, `canAssignWaiter()`, `canAssignChef()`, `recordWaiterTask()`, `recordChefTask()`, `resolveTurn(Restaurant)`, `advanceTurn()`, `currentTurn()`, `isFinished()` |

The controller records a task only after a successful staff/domain action. A deliberate `wait` is successful and consumes that staff choice.

### Customer and order domain

| Type | Responsibility | Key public contract |
| --- | --- | --- |
| `Customer` | Own identity, satisfaction, lifecycle, service/payment timing and optional order | `chooseMenuItem(List<MenuItem>)`, `waitingLossPerTurn()`, `priceAfterCustomerDiscount(int)`, `seat()`, `acceptOrder(Order)`, `markServed(int)`, `finishEating(int)`, `applyWaitingLoss()`, `markPaid()`, `abandon()` |
| `RegularCustomer` | Provide Stage 1 customer behaviour | Loss of 8, deterministic base selection and no customer discount |
| `Order` | Own selected item, preparation progress, locked price and lifecycle | `canPrepare()`, `prepareOneUnit()`, `markServed()`, `markPaid()`, `cancel()` |
| `MenuItem` | Immutable menu offering | `name()`, `priceCents()`, `preparationUnits()` |
| `CustomerStatus` | Define customer lifecycle | `WAITING`, `SEATED`, `SERVED`, `READY_TO_PAY`, `PAID`, `LEFT` |
| `OrderStatus` | Define food/payment lifecycle | `PLACED`, `PREPARING`, `READY`, `SERVED`, `PAID`, `CANCELLED` |

Customer normal flow is `WAITING -> SEATED -> SERVED -> READY_TO_PAY -> PAID`. Abandonment is allowed only from `WAITING` or `SEATED`. Satisfaction remains between 0 and 100.

Order normal flow is `PLACED -> PREPARING -> READY -> SERVED -> PAID`. Cancellation is allowed only before service. Zero remaining units alone does not imply that an order is currently serviceable; the status must be `READY`.

### Restaurant resources

| Type | Responsibility | Key public contract |
| --- | --- | --- |
| `Restaurant` | Own tables, queues, customers, orders, menu and aggregate totals | `addArrival(Customer)`, `seat(int,int)`, `registerOrder(Order)`, `serve(int,int)`, `oldestEligiblePayer(int)`, `recordPayment(Customer,int)`, `removeAbandoned(Customer)`, query and statistic methods |
| `Table` | Own one fixed table ID and at most one occupant | `isAvailable()`, `assign(Customer)`, `release()`, `occupant()` |

A customer is in the waiting queue or at one table, never both. A table remains occupied until payment or abandonment. Cross-object operations validate first so failures cannot leave partially changed state.

### Staff

| Type | Responsibility | Key public contract |
| --- | --- | --- |
| `Waiter` | Delegate seating, create/register orders and delegate service | `seat(Restaurant,int,int)`, `takeOrder(Restaurant,int)`, `serve(Restaurant,int,int)` |
| `Chef` | Advance one valid order by exactly one preparation unit | `prepareOneUnit(Order)` |
| `Cashier` | Automatically collect at most one eligible payment, oldest first | `collectOne(Restaurant,int)` |

Staff objects do not read input, print output or own duplicate turn-availability flags.

## 5. Turn and timing contract

One turn proceeds in this order:

1. `TurnManager` begins the turn and adds the scheduled arrival.
2. The CLI renders the start-of-turn snapshot.
3. The manager completes one valid Waiter action or waits.
4. The manager completes one valid Chef action or waits. The Chef may prepare an order taken during step 3.
5. Customers served on an earlier turn finish eating and become ready to pay.
6. The Cashier pays at most one customer who became ready to pay on an earlier turn.
7. Waiting and seated-but-unserved customers lose satisfaction.
8. Customers at zero satisfaction abandon; unfinished orders are cancelled and occupied tables released.
9. The turn advances. After turn 18, all three victory conditions are evaluated together.

An order completed by the Chef cannot be served until a later turn because the Waiter has already acted. A customer served this turn cannot finish eating this turn, and a customer who finishes eating this turn cannot pay this turn.

## 6. Planned extension contracts

These sections guide future design. They do not authorise merging later-stage source before the preceding tag is complete.

### Stage 2

- Add `VIPCustomer` and `CriticCustomer` under `Customer`.
- Use the arrival sequence Regular, VIP, Critic, Regular, VIP, Regular.
- `VIPCustomer` loses 5 satisfaction and applies a 10% customer discount.
- `CriticCustomer` loses 12 satisfaction and overrides one documented selection or service-reaction behaviour.
- The Critic rule is selected at the Stage 2 design gate. The fallback is choosing the highest-priced menu item.
- Existing controller and turn code calls the customer contract polymorphically and must not inspect concrete types.

### Stage 3

- Add `Host` with one independent seat-or-wait choice. Manager choice order becomes Host, Waiter, Chef so the Waiter may take an order from a Host-seated customer in the same turn.
- Add `ComboMeal extends MenuItem`, composed of exactly two existing `MenuItem` objects.
- The required planned combo is Salad plus Pasta: 2430 cents and 3 preparation units.
- Add the combo as the fourth menu entry so the deterministic cycle selects it for customer 4.
- Add `HappyHourPolicy` to own one-time activation, the active two-turn window, remaining duration and recovery eligibility.
- Add a focused price calculator that applies customer pricing first, then the 20% Happy Hour discount, rounding to the nearest cent after each step.
- Orders retain their calculated price even after Happy Hour ends.
- The Stage 3 start-turn interface may be extended to accept the activation decision; the exact signature must be recorded before implementation.

## 7. Draw.io UML process

Maintain two editable files:

- `docs/uml/class-diagram.drawio`
- `docs/uml/sequence-diagram.drawio`

Export reviewed PDF or high-resolution PNG copies for the report. M5 is the primary Draw.io editor because concurrent XML edits are difficult to merge; other members review exported diagrams and request corrections through the related pull request.

The class diagram must match the corresponding tagged code and show visibility, principal attributes/operations, inheritance, associations, multiplicities and meaningful composition. Mark boundary, control and entity responsibilities clearly. Do not add decorative interfaces that are absent from the code.

The final sequence diagram must include arrival, order selection, constrained staff choices, preparation, `READY -> SERVED`, eating, delayed payment, result checking and one Stage 2 or Stage 3 behaviour. Show relevant retry guards and turn loops.

## 8. Testing and evidence process

Use a self-contained Java test runner unless the team explicitly agrees to add an external test dependency. Test code belongs under `src/test/java` and must never claim a pass unless the command was actually run.

For each stage, create `docs/testing/stage-N-tests.md` from `TEST-TEMPLATE.md`. Record:

- tester, date, Java version and exact commit SHA;
- command or repeatable steps;
- expected and observed outcomes;
- `PASS` or `FAIL`;
- unresolved defects or limitations;
- reviewer name before tagging.

Stage 1 additionally requires saved, reproducible victory and defeat command inputs. Later stages rerun Stage 1 regression scenarios as well as their new tests.

Minimum coverage:

- compilation and launch;
- arrival schedule and base configuration;
- invalid actions preserve state and staff availability;
- READY, service, eating and payment delays;
- oldest-first single Cashier payment;
- satisfaction loss, abandonment and table release;
- paid-only revenue and satisfaction average;
- victory and defeat after exactly 18 turns;
- Stage 2 polymorphic losses, selection and VIP pricing;
- Stage 3 Host capacity, combo calculation, Happy Hour duration/recovery/discount order and locked prices.

## 9. Lightweight AI-prompt evidence

For each AI-assisted feature branch, copy `docs/ai-prompts/TEMPLATE.md` to a file named after the branch, such as `s1-customer-orders.md`.

Record only prompts that materially influenced design, code or tests. For each entry include the exact prompt, what advice was used or rejected, affected files, human verification and related commits. Do not include credentials, private data, entire chat transcripts or routine follow-ups.

Link the record in the pull request and merge it with the code so it is preserved in the relevant stage tag. This is evidence of responsible assistance and human verification, not proof that an AI response was correct.

## 10. Git workflow and stage gates

Start each task from the latest `main`:

```bash
git switch main
git pull --ff-only
git switch -c feature/s1-customer-orders
```

Before opening a pull request:

```bash
git fetch origin
git merge origin/main
./compile.sh --tests
git push -u origin feature/s1-customer-orders
```

Use short-lived branches and normal reviewed pull requests. Do not merge Stage 2 code before `stage-1`, or Stage 3 code before `stage-2`.

At a stage gate, update the contract, README, UML and test record; confirm a clean working tree; then create the annotated tag:

```bash
git switch main
git pull --ff-only
./compile.sh --tests
git tag -a stage-1 -m "Stage 1: Basic restaurant operation"
git push origin stage-1
```

Verify a grader's view without committing from it:

```bash
git switch --detach stage-1
./compile.sh --tests
git switch main
```

README transition evidence is generated from `git diff --name-status` and `git log` between `stage-1..stage-2` and `stage-2..stage-3`, with reasons for meaningful additions, changes and deletions.

## 11. Suggested schedule and completion standard

1. Days 1-2: agree Stage 1 contracts, CLI vocabulary, package layout and draft Draw.io diagrams.
2. Days 3-7: implement and integrate Stage 1; record victory/defeat evidence; tag `stage-1`.
3. Days 8-11: decide Critic behaviour; implement and test Stage 2; tag `stage-2`.
4. Days 12-15: implement Host, ComboMeal and Happy Hour; update UML and tests; tag `stage-3`.
5. Final 3-5 days: finish report, exports, transition evidence, reflections, declarations, video and detached-tag checks.

The project is complete only when all three tags compile and run offline, required scenarios are reproducible, diagrams match Stage 3 code, evidence names the tested commits, the README documents both transitions and video timestamps, and every member can explain their area and one cross-system interaction.

## 12. Open decisions

| Decision | Owner/deadline | Current default |
| --- | --- | --- |
| Map names to M1-M5 | Team, before Stage 1 coding | Unassigned placeholders |
| Exact CLI command wording | M4 and M5, before Stage 1 coding | Action names in this contract are conceptual |
| Critic's distinct behaviour | Team, immediately after `stage-1` | Highest-priced menu item if no alternative is agreed |
| Exact Stage 3 Happy Hour start-turn API | M3, M4 and M5, before Stage 3 coding | Document the activation decision before any staff task |
| Final archive name and deadline | Team, before submission rehearsal | Follow NTULearn and lab-group details |
| Video segment ownership | Team, before recording | Each member explains their subsystem |
| Whether to commit the supplied assignment DOCX | Team, before the first documentation PR | Keep it local until confirmed |
