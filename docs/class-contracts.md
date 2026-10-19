# RestaurantRush Stage 1 class contracts

## Document status

This document is the team's working agreement for **Stage 1 only**. It defines the responsibilities, public interfaces, invariants, integration rules and evidence that the five members can implement against without waiting for one another.

It is a design contract, not a claim that the classes or tests already exist. The assignment brief remains authoritative; if this document and the brief disagree, the team must correct this document before merging the affected implementation.

Stage 2 and Stage 3 class designs are intentionally deferred. Their required features are acknowledged in the assignment brief, but their classes, methods and empty placeholders must not be added to the Stage 1 runnable version. After `stage-1` is accepted and tagged, the team will review what actually changed and revise this contract for Stage 2.

## 1. Stage 1 scope and completion standard

Stage 1 delivers a complete command-line game with Regular customers, the three-item menu, two tables, one Waiter, one Chef, one automatic Cashier and the full 18-turn flow.

| Setting | Required Stage 1 value |
| --- | --- |
| Turns | Exactly 18; no early victory or defeat |
| Tables | Two, with IDs 1 and 2 |
| Arrivals | One customer at the start of turns 1, 4, 7, 10, 13 and 16 |
| Staff capacity | One Waiter choice, then one Chef choice, and at most one automatic Cashier payment per turn |
| Menu | Salad: 900 cents/1 unit; Burger: 1500 cents/1 unit; Pasta: 1800 cents/2 units |
| Food selection | `menu.get((customerId - 1) % menu.size())`; the manager never selects the food |
| Victory | After turn 18: at least 4 paid customers, at least 4500 cents revenue and paid-customer average satisfaction at least 60 |

Stage 1 is complete only when:

- the project compiles and launches offline from the documented commands;
- all 18 turns and the required timing rules work through the CLI;
- invalid input and invalid actions can be retried without consuming a staff choice;
- separate, reproducible victory and defeat runs have been recorded;
- focused and integration tests have been run against the exact candidate commit;
- the Stage 1 Draw.io diagram, README and test record match the code;
- another member has reviewed the evidence; and
- the accepted runnable commit is tagged `stage-1`.

### Technical baseline

- Java 17 language target, compiled with plain `javac`.
- Command-line interface only; all mandatory behaviour works offline.
- Production packages live under `src/main/java/restaurantrush`.
- Tests live under `src/test/java/restaurantrush` and use a self-contained Java runner unless the team deliberately changes the build agreement.
- Draw.io is the editable UML source format.
- Money is stored as integer cents and displayed with two decimal places.
- Build output belongs under `out/` and is never committed.

## 2. Stage 1 ownership and collaboration

The five roles are placeholders until the team maps them to names. Ownership identifies the first implementer, maintainer and required reviewer for a type; it does not give exclusive control over a file.

| Owner | Stage 1 area | Principal types | Evidence owned |
| --- | --- | --- | --- |
| M1 | Customers and orders | `Customer`, `RegularCustomer`, `Order`, `MenuItem`, `CustomerStatus`, `OrderStatus` | Lifecycle, satisfaction, polymorphic rule, food-selection and preparation tests |
| M2 | Restaurant state | `Restaurant`, `Table` | Queue, occupancy, payment, abandonment and statistics tests |
| M3 | Staff operations | `Waiter`, `Chef`, `Cashier`, `ActionResult` | Staff validation, action-result and timing tests |
| M4 | Game control | `GameController`, `TurnManager`, `ArrivalSchedule` | Turn ordering, arrival, task-limit and result tests |
| M5 | Boundary and integration | `RestaurantRushApp`, `RestaurantCLI`, `GameSnapshot`, `CustomerView`, `TableView` | CLI retries, displays, scripted runs and integration evidence |

### Shared handoffs

The affected owners agree these contracts before either side is merged:

- M1-M2: customer transitions during seating, service, payment and abandonment.
- M1-M3: one shared `Order` instance, deterministic selection and preparation through `Order` methods.
- M1-M4: `ArrivalSchedule` constructs `RegularCustomer` instances but exposes them to the engine as `Customer`.
- M2-M3: seating delegation, atomic order registration, automatic payment and table release.
- M3-M4: staff availability, Waiter-before-Chef ordering and Cashier timing.
- M4-M5: commands, retry behaviour, snapshots, turn lifecycle and outcome display.

### Contribution policy

- Each member implements and tests their owned Stage 1 behaviour and reviews any change to a shared handoff.
- Cross-owner edits are allowed through a focused feature pull request with the owner requested as reviewer.
- Contributions may include code, tests, Draw.io verification, documentation, scenario evidence and review. There is no target number or artificial spacing of commits.
- Commits must represent meaningful, reviewable work; do not create empty classes or trivial edits to improve contribution statistics.
- Feature branches target `main`; the team does not use permanent personal integration branches.
- Before contributing, each member configures a Git name and an email associated with their GitHub account.

## 3. Shared conventions and invariants

1. Fields are private. State changes occur through methods that enforce lifecycle rules; public setters are not used.
2. `RestaurantCLI` reads and prints but contains no game rules.
3. `GameController` coordinates requests but does not duplicate entity or turn rules.
4. `TurnManager` is the sole authority for turn phase and per-turn staff availability.
5. `Restaurant` validates and coordinates changes spanning customers, tables, orders and totals before mutating them.
6. `Order` owns preparation and order-status transitions.
7. Player-requested actions return `ActionResult`. A failure explains the problem, leaves state unchanged and does not consume a staff choice. An explicit `wait` succeeds and consumes the relevant choice.
8. Malformed CLI input is a boundary error. Invalid internal lifecycle use may throw `IllegalStateException`; invalid constructor values may throw `IllegalArgumentException`.
9. A customer is queued or seated, never both. A table has at most one occupant, and the customer remains at that table until payment or abandonment.
10. Each customer has at most one order. For Stage 1, the order ID equals the customer ID, and the customer's order reference and Restaurant's registry contain the same object.
11. Paid and departed customers and their orders remain in history but are excluded from active queues and actions.
12. Satisfaction stays between 0 and 100. Only `WAITING` and `SEATED` customers lose 8 points at turn end, with a floor of zero.
13. Revenue contains successful payments only. Served, paid and abandoned totals are updated once per event.
14. An order's bill is locked to the customer's nonnegative integer-cent quote when the order is created. In Stage 1, `RegularCustomer` quotes the selected item's full price.
15. Collections exposed outside their owner are immutable copies or read-only views. Snapshots contain no mutable entity references.
16. Do not add `Menu`, `Money`, `Staff`, `WaitingQueue`, `InputValidator` or a design-pattern interface merely to increase the class count. Add an abstraction only when Stage 1 gives it a real responsibility or it removes meaningful duplication.

Customer and order states are deliberately separate. `OrderStatus.SERVED` means the dish has been handed over; `CustomerStatus.EATING` means the customer is completing the required eating delay.

### Stage 1 extensibility rule

Stage 1 must expose stable seams for behaviour that already exists in Stage 1, without implementing later-stage features:

- `Customer` is an abstract lifecycle base used by Restaurant, staff, control and view code. `RegularCustomer` supplies the Stage 1 waiting-loss, food-choice and full-price rules.
- Customer-specific rules are invoked polymorphically. No controller, Restaurant, staff or CLI code may branch on a concrete customer type.
- `ArrivalSchedule` owns the Stage 1 arrival rule and returns the base `Customer` type, so `TurnManager` does not construct a concrete customer directly.
- Waiter asks the customer for the Stage 1 quoted price, then creates an `Order` with that locked integer-cent bill. Order does not need to know the concrete customer type.
- Public code depends on `Customer`, immutable views and documented operations rather than concrete collections or implementation details.

These are active Stage 1 relationships and must be tested in Stage 1. No future-named subclasses, pricing policies, staff roles or commands are added until their stage is being designed.

## 4. Stage 1 class contracts

These signatures are the baseline for implementation. A change is allowed when the affected owners agree and update this document, the Draw.io diagram and relevant tests in the same pull request.

### 4.1 Boundary and immutable views

#### `RestaurantRushApp` — owner M5

Constructs one shared object graph and starts the application.

| Public operation | Contract |
| --- | --- |
| `public static void main(String[] args)` | Create the Restaurant, ArrivalSchedule, Cashier, TurnManager, staff, GameController and RestaurantCLI, then call `run()`. Do not create duplicate domain instances. |

#### `RestaurantCLI` — owner M5

| Public operation | Contract |
| --- | --- |
| `void run()` | Start a turn, display its snapshot, retry Waiter input until one choice succeeds, retry Chef input until one choice succeeds, then finish the turn. Repeat through turn 18. Stop safely at end-of-input. |
| `void displayState(GameSnapshot snapshot)` | Show turn, revenue, paid-customer average or `N/A`, queue, tables, customer satisfaction/status, order progress and current staff availability. |
| `void displayOutcome(GameSnapshot snapshot, boolean victory)` | Show Victory or Defeat plus turns used, served, paid, abandoned, revenue and paid-customer average. |

The exact command words are agreed before CLI implementation. Malformed commands and non-integer IDs are explained and retried without calling a domain action. The CLI prints `ActionResult.message()` but does not recreate the validation rule.

#### Immutable view records — owner M5

| Type | Required content |
| --- | --- |
| `GameSnapshot` | turn, revenue, nullable paid average, waiting IDs, immutable customer/table views, Waiter/Chef availability, paid/served/abandoned counts |
| `CustomerView` | customer ID, satisfaction, customer status, nullable order status/item/remaining units and nullable table ID |
| `TableView` | table ID and nullable occupant ID |

`GameSnapshot.from(Restaurant, TurnManager)` creates immutable list copies. An existing snapshot must not change when the game later changes.

### 4.2 Control

#### `GameController` — owner M4

| Public operation | Contract |
| --- | --- |
| `void startTurn()` | Delegate once to `TurnManager.beginTurn(restaurant)` before the CLI displays the turn. |
| `ActionResult seatCustomer(int customerId, int tableId)` | Require an available Waiter choice, delegate to Waiter and record the choice only on success. |
| `ActionResult takeOrder(int customerId)` | Require an available Waiter choice, delegate to Waiter and record the choice only on success. |
| `ActionResult serveOrder(int customerId)` | Require an available Waiter choice, delegate with the current turn and record the choice only on success. |
| `ActionResult prepareOrder(int customerId)` | Require an available Chef choice, find the order, delegate to Chef and record the choice only on success. |
| `ActionResult waitWaiter()` | Consume the available Waiter choice without another action; otherwise fail. |
| `ActionResult waitChef()` | Consume the available Chef choice without another action; otherwise fail. |
| `void finishTurn()` | Resolve automatic events once, then advance or finish the game. |
| `GameSnapshot snapshot()` | Return a new immutable snapshot of the current state. |
| `boolean isFinished()` | Return whether all 18 turns have completed. |
| `boolean isVictory()` | Return true only after completion and only when all three objectives pass. |

Every manager action passes through the controller. An unavailable staff choice fails before invoking staff.

#### `TurnManager` — owner M4

`TurnManager` owns `currentTurn`, whether the turn has started/resolved, whether each staff choice has been used, completion state, the automatic `Cashier` collaborator and an injected `ArrivalSchedule`. It does not construct a concrete customer.

| Public operation | Contract |
| --- | --- |
| `TurnManager(Cashier cashier, ArrivalSchedule arrivals)` | Store the collaborators used for all turns. Reject null collaborators. |
| `void beginTurn(Restaurant restaurant)` | Start an unfinished, not-yet-started turn; reset per-turn flags and add the scheduled arrival once. Reject duplicate starts. |
| `boolean canAssignWaiter()` | True only during an unresolved active turn whose Waiter choice is unused. |
| `boolean canAssignChef()` | True only after the Waiter choice and before the Chef choice in an unresolved active turn. |
| `void recordWaiterTask()` | Require Waiter availability and mark the choice used. |
| `void recordChefTask()` | Require Chef availability and mark the choice used. |
| `void resolveTurn(Restaurant restaurant)` | Require both choices and an unresolved active turn. Execute the automatic events once in the order defined in Section 5. |
| `void advanceTurn()` | Require a resolved turn. Advance up to turn 18 or mark the game finished after turn 18. |
| `int currentTurn()` | Return the current turn number. |
| `boolean isFinished()` | Return true only after turn 18 has been resolved and closed. |

#### `ArrivalSchedule` — owner M4

Owns the Stage 1 mapping of turns 1, 4, 7, 10, 13 and 16 to customer IDs 1 through 6. `Customer arrivalFor(int turn)` returns a new `RegularCustomer` for a scheduled turn and `null` otherwise. `TurnManager` calls it only from a guarded `beginTurn()`, so a turn cannot receive duplicate arrivals.

### 4.3 Customer and order domain

#### `Customer` — owner M1

`Customer` is an abstract base for the lifecycle shared by every Stage 1 customer. Stage 1 uses only `RegularCustomer`, but Restaurant, staff and control code accept the base type.

| State owned | Initial value or invariant |
| --- | --- |
| ID | positive and immutable; base arrivals use 1 through 6 |
| satisfaction | 100, always clamped to 0 through 100 |
| status | initially `WAITING` |
| served turn | `-1` until service |
| ready-to-pay turn | `-1` until eating finishes |
| order | `null` until ordering; at most one |

| Public operation | Contract |
| --- | --- |
| `protected Customer(int id)` | Initialise the shared lifecycle for a positive immutable ID. Only a concrete customer subclass is instantiated. |
| `abstract int waitingLossPerTurn()` | Return this customer's Stage 1 waiting loss. Called by the base lifecycle method rather than by a type check. |
| `abstract MenuItem chooseMenuItem(List<MenuItem> menu)` | Choose from a nonempty menu. The manager never supplies the selected item. |
| `abstract int quotePriceCents(MenuItem item)` | Return the nonnegative bill to lock when this customer orders the item. |
| `void seat()` | Require `WAITING`; become `SEATED`. |
| `void acceptOrder(Order order)` | Require `SEATED`, no existing order and matching customer/order IDs. |
| `void markServed(int turn)` | Require `SEATED` and an associated order already `SERVED`; become `EATING` and record the service turn. |
| `void finishEating(int turn)` | If `EATING` and served on an earlier turn, become `READY_TO_PAY` and record this turn; otherwise do nothing. |
| `void applyWaitingLoss()` | If `WAITING` or `SEATED`, subtract `waitingLossPerTurn()` with a floor of zero; otherwise do nothing. |
| `void markPaid()` | Require `READY_TO_PAY` and an associated order already `PAID`; become `PAID`. |
| `void abandon()` | Require `WAITING` or `SEATED`; become `LEFT` and cancel any unserved order. Restaurant coordinates queue/table cleanup. |

Standard read-only accessors expose identity, satisfaction, status, timing and the optional order.

Lifecycle mutation stays in the base class. Subclasses supply only the documented behavioural rules unless a later contract deliberately changes that boundary.

#### `RegularCustomer` — owner M1

The only Stage 1 customer implementation. `RegularCustomer(int id)` delegates shared setup to the base constructor. It returns a waiting loss of 8, chooses `menu.get((id - 1) % menu.size())`, and quotes the selected item's full integer-cent price.

#### `Order` — owner M1

| State owned | Initial value or invariant |
| --- | --- |
| ID | positive, immutable and equal to its customer's ID |
| menu item | non-null and immutable |
| status | initially `PLACED` |
| remaining units | item's preparation units; never negative |
| bill cents | explicit nonnegative locked price supplied at construction; never recalculated |

| Public operation | Contract |
| --- | --- |
| `Order(int id, MenuItem item, int lockedBillCents)` | Create a `PLACED` order with the item's preparation units and the supplied Stage 1 quote. Validate all arguments. |
| `boolean canPrepare()` | True only for `PLACED` or `PREPARING` with positive remaining units. |
| `void prepareOneUnit()` | Require `canPrepare()`, subtract exactly one unit, then become `READY` at zero or `PREPARING` otherwise. |
| `void markServed()` | Require `READY`; become `SERVED`. |
| `void markPaid()` | Require `SERVED`; become `PAID`. |
| `void cancel()` | Permit only from `PLACED`, `PREPARING` or `READY`; become `CANCELLED`. |
| `boolean isReady()` | Return whether status is exactly `READY`; zero units alone is insufficient. |

Standard read-only accessors expose ID, item, status, remaining units and locked bill.

In Stage 1, Waiter must pass exactly `customer.quotePriceCents(item)`, which for `RegularCustomer` equals `item.priceCents()`. The separate constructor value is an exercised pricing boundary, not permission for arbitrary prices.

#### `MenuItem` — owner M1

An immutable value containing a nonblank name, nonnegative integer-cent price and positive preparation-unit count. It exposes read-only accessors and no setters.

#### Lifecycle enumerations — owner M1

| Enumeration | Values and normal flow |
| --- | --- |
| `CustomerStatus` | `WAITING -> SEATED -> EATING -> READY_TO_PAY -> PAID`; `WAITING` or `SEATED` may instead become `LEFT` |
| `OrderStatus` | `PLACED -> PREPARING -> READY -> SERVED -> PAID`; `PLACED`, `PREPARING` or `READY` may instead become `CANCELLED` |

A one-unit order moves directly from `PLACED` to `READY`. `PAID`, `LEFT` and `CANCELLED` are terminal in their respective lifecycles.

### 4.4 Restaurant state

#### `Table` — owner M2

Owns a positive immutable ID and a nullable occupant. `assign(Customer)` requires an available table; `release()` requires an occupant. Restaurant coordinates these operations with customer and queue state.

#### `Restaurant` — owner M2

Restaurant owns the waiting queue, all-customer history, two tables, the ordered Stage 1 menu, all-order history, paid revenue, paid satisfaction total and paid/served/abandoned counts.

| Public operation | Contract |
| --- | --- |
| `void addArrival(Customer customer)` | Add a unique customer to history and the back of the waiting queue. |
| `ActionResult seat(int customerId, int tableId)` | Require an existing queued customer and free table. Validate first, then update the queue, table and customer atomically. |
| `void registerOrder(Order order)` | Register a unique order. The customer and registry must share the same instance. |
| `ActionResult serve(int customerId, int turn)` | Require a seated customer, occupied table and their registered `READY` order. Mark order served, mark customer eating and increment served count once. |
| `Customer oldestEligiblePayer(int turn)` | Return the customer with the earliest ready-to-pay turn strictly before this turn, breaking ties by lowest customer ID, or `null`. |
| `boolean recordPayment(Customer customer, int turn)` | Revalidate eligibility, mark the order/customer paid, release the table and update revenue and paid statistics exactly once. |
| `void removeAbandoned(Customer customer)` | For an active unserved customer at zero satisfaction, remove from queue or release table, abandon/cancel and increment the count exactly once. |
| `double averagePaidSatisfaction()` | Return the floating-point average; snapshot uses `null`/`N/A` when paid count is zero. |

Read-only queries provide customers, active customers, queue, tables, menu, orders, lookups and totals. Invalid cross-object actions must not partially change state.

### 4.5 Staff and results

No common `Staff` superclass is required in Stage 1: Waiter, Chef and Cashier do not expose one genuinely substitutable action. They do not read input, print output or own duplicate staff-availability flags.

#### `Waiter` — owner M3

| Public operation | Contract |
| --- | --- |
| `ActionResult seat(Restaurant restaurant, int customerId, int tableId)` | Delegate seating to Restaurant. |
| `ActionResult takeOrder(Restaurant restaurant, int customerId)` | Require a seated customer without an order, ask the customer to choose from the menu and quote its price, create one shared order with that locked bill, link/register it atomically and report the dish. |
| `ActionResult serve(Restaurant restaurant, int customerId, int turn)` | Delegate service to Restaurant. |

#### `Chef` — owner M3

`ActionResult prepareOneUnit(Order order)` fails without mutation for a missing or ineligible order. Otherwise it calls `Order.prepareOneUnit()` exactly once and reports whether the order became `READY`.

#### `Cashier` — owner M3

`boolean collectOne(Restaurant restaurant, int turn)` automatically selects `restaurant.oldestEligiblePayer(turn)` and records at most one successful payment per turn. A customer becoming `READY_TO_PAY` this turn is not eligible until a later turn.

#### `ActionResult` — owner M3

An immutable result with `ok(String)`, `fail(String)`, `success()` and `message()`. It carries feedback but contains no printing or domain mutation.

## 5. Turn and integration contract

One turn proceeds in this order:

1. `TurnManager.beginTurn()` adds the scheduled arrival once.
2. The CLI renders the start-of-turn snapshot.
3. The manager completes one valid Waiter action or waits.
4. The manager completes one valid Chef action or waits. The Chef may prepare an order taken during step 3.
5. Customers served on an earlier turn finish eating and become `READY_TO_PAY`.
6. The Cashier pays at most one customer who became ready to pay on an earlier turn, oldest first.
7. Waiting and seated-but-unserved customers lose satisfaction.
8. Customers at zero satisfaction abandon; unfinished orders are cancelled and occupied tables released.
9. The turn advances. After turn 18, all victory conditions are evaluated together.

The Waiter acts before the Chef, so an order completed by the Chef cannot be served until a later turn. A newly served customer cannot finish eating that turn, and a newly payment-ready customer cannot pay that turn.

### Acceptance examples

- Invalid seat, order, serve and cook requests leave entities, statistics and staff availability unchanged.
- A successful action or explicit wait consumes exactly one corresponding choice.
- Duplicate turn start, resolution or advancement cannot add another arrival, penalty or payment.
- A Pasta order receiving full attention becomes ready after two Chef choices, is served on a later turn, finishes eating on the following turn and pays no earlier than one further turn.
- If several customers can pay, the earliest ready-to-pay turn wins, then the lowest customer ID.
- Abandonment cancels even a `READY` but unserved order, frees any table and adds no revenue.
- Satisfaction averages use values recorded at payment and floating-point division; before any payment the CLI displays `N/A`.
- The turn-16 arrival cannot pay by the end of turn 18. This is expected, not a defect.
- Existing snapshots and their lists cannot be mutated or changed by subsequent turns.

## 6. Stage 1 Draw.io process

Maintain:

- `docs/uml/class-diagram.drawio`
- a reviewed PDF or high-resolution PNG export for evidence

M5 coordinates the single editable file to avoid XML merge conflicts. M1-M4 supply and verify the attributes, operations and relationships for their own types. Only one person edits the Draw.io source at a time.

The Stage 1 diagram must match the `stage-1` code and show visibility, important attributes/operations, enums, the `Customer`/`RegularCustomer` inheritance, associations, multiplicities and meaningful composition. Annotate genuine examples of encapsulation, polymorphism and composition; do not add interfaces or relationships that are absent from the code.

The final submission will eventually require a Stage 3 class diagram and a sequence diagram, but their later-stage content is not designed in this Stage 1 contract. The team may draft a Stage 1 sequence for integration discussion without presenting it as the final design.

## 7. Stage 1 testing and functional evidence

Use a self-contained Java test runner unless the team explicitly agrees to a different offline-capable build. JUnit is optional; the brief assesses testing evidence and reproducibility rather than requiring a particular framework. Tests must never be marked passed unless their commands were actually run.

Each owner writes focused tests for their area. Cross-subsystem scenarios are reviewed jointly; M5 coordinates the scripted end-to-end runs but is not solely responsible for integration quality.

Create `docs/testing/stage-1-tests.md` from `TEST-TEMPLATE.md` when testing begins. Record:

- tester, reviewer, date, Java version and exact commit SHA;
- the command or repeatable steps;
- expected and observed outcomes;
- `PASS` or `FAIL`;
- unresolved defects or limitations; and
- the saved inputs and final output for separate victory and defeat runs.

Minimum Stage 1 coverage:

- compilation and launch;
- arrival schedule, tables, menu and deterministic selection;
- Regular-customer behaviour exercised through a `Customer` reference, with no concrete-type branching outside construction;
- customer, order and table lifecycle transitions;
- invalid input/actions preserving state and staff availability;
- Waiter-before-Chef ordering and preparation units;
- READY, service, eating and payment delays;
- oldest-first single Cashier payment;
- satisfaction loss, abandonment, cancellation and table release;
- paid-only revenue and paid-satisfaction average;
- immutable snapshots; and
- victory and defeat after exactly 18 turns.

## 8. Lightweight AI-prompt evidence

For each AI-assisted feature branch, copy `docs/ai-prompts/TEMPLATE.md` to a concise file named after the branch. Record only prompts that materially affected design, code or tests, together with what was accepted or rejected, affected files, human verification and related commits.

Do not store credentials, private information or entire chat transcripts. Link the prompt record in the pull request and merge it with the relevant work. This supports the individual GAI declarations but does not replace them or prove that AI advice was correct.

## 9. Stage 1 Git workflow and tag gate

Before contributing:

```bash
git config user.name
git config user.email
```

Start a short-lived task from the latest `main`:

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

Pull requests target `main`. Request the relevant owner when shared contracts or owned files change. Prefer a normal reviewed merge when preserving meaningful individual commits helps the development evidence; do not manufacture commits or rewrite authorship for contribution statistics.

Do not merge Stage 2 implementation before `stage-1` is tagged. The release coordinator creates the tag only after the Stage 1 acceptance checks pass—not merely because every member has committed:

```bash
git switch main
git pull --ff-only
./compile.sh --tests
git tag -a stage-1 -m "Stage 1: Basic restaurant operation"
git push origin stage-1
```

Finally, verify the grader's view from the tag and return to `main`:

```bash
git switch --detach stage-1
./compile.sh --tests
git switch main
```

## 10. Suggested Stage 1 integration order

1. Agree package names, exact CLI commands, constructors, exception conventions and the contracts in this document.
2. Each owner implements a small compiling vertical portion of their Stage 1 responsibility with focused tests.
3. Integrate immutable menu/customer/order basics, then Restaurant/Table state, then staff operations.
4. Integrate GameController/TurnManager timing before connecting the CLI.
5. Run lifecycle and invalid-action tests at each integration point instead of waiting for the full game.
6. Add reproducible victory and defeat inputs, update Draw.io and complete the Stage 1 test record.
7. Review the exact candidate commit, tag `stage-1`, and only then hold the Stage 2 design meeting.

## 11. Stage 1 decisions still to settle

| Decision | Owners | Default if not otherwise agreed |
| --- | --- | --- |
| Map names to M1-M5 | Whole team | Keep placeholders until recorded |
| Exact package layout and constructor wiring | M4/M5 | One shared object graph created by `RestaurantRushApp` |
| Exact CLI command wording and end-of-turn presentation | M4/M5 | Commands shown in the boundary contract are conceptual |
| Invalid argument/null exception details | M1-M4 | `IllegalArgumentException` for bad values; `IllegalStateException` for invalid transitions; player errors use `ActionResult.fail` |
| Whether snapshots show all historical customers or active customers only | M2/M5 | Show all arrived customers clearly marked by state |
| How turns-used is represented after `finishTurn()` | M4/M5 | Add completed-turn data rather than labelling the next turn as completed |

## 12. Contract change process

- Change a public signature or shared invariant only through a pull request reviewed by every affected owner.
- In the same pull request, update the contract, relevant tests and Draw.io source/export.
- Record why the change was needed and whether it changes observable Stage 1 behaviour.
- After `stage-1` is tagged, treat that snapshot as fixed evidence. Begin Stage 2 by reviewing the tagged design and adding only the contracts needed for the new requirements.
