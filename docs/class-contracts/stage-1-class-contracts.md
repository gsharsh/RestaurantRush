# RestaurantRush Stage 1 class contracts

## Document status

This document is the team's working agreement for **Stage 1 only**. It defines the responsibilities, public interfaces, invariants, integration rules and evidence that the five members can implement against without waiting for one another.

The assignment brief remains authoritative; if this document and the brief disagree, the team must correct this document before merging the affected implementation.

Stage 2 and Stage 3 classes, methods and empty placeholders must not be added to the Stage 1 runnable version. Later-stage changes belong in their stage-specific contracts and releases; this document preserves the Stage 1 baseline.

## 1. Stage 1 scope and completion standard

Stage 1 delivers a complete command-line game with Regular customers, the three-item menu, two tables, one Waiter, one Chef, one automatic Cashier and the full 18-turn flow.


| Setting        | Required Stage 1 value                                                                                                   |
| -------------- | ------------------------------------------------------------------------------------------------------------------------ |
| Turns          | Exactly 18; no early victory or defeat                                                                                   |
| Tables         | Two, with IDs 1 and 2                                                                                                    |
| Arrivals       | One customer at the start of turns 1, 4, 7, 10, 13 and 16                                                                |
| Staff capacity | One Waiter choice, then one Chef choice, and at most one automatic Cashier payment per turn                              |
| Menu           | Salad: 900 cents/1 unit; Burger: 1500 cents/1 unit; Pasta: 1800 cents/2 units                                            |
| Food selection | `menu.get((customerId - 1) % menu.size())`; the manager never selects the food                                           |
| Victory        | After turn 18: at least 4 paid customers, at least 4500 cents revenue and paid-customer average satisfaction at least 60 |


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
- Production packages live under `src/restaurantrush/`.
- Tests live under `tests/restaurantrush/` and mirror their `restaurantrush.*` package declarations. They use a self-contained Java runner unless the team deliberately changes the build agreement.
- Draw.io is the editable UML source format.
- Money is stored as integer cents and displayed with two decimal places.
- Build output belongs under `out/` and is never committed.



## 2. Stage 1 ownership and collaboration

The five roles are placeholders until the team maps them to names. Ownership identifies the first implementer, maintainer and required reviewer for a type; it does not give exclusive control over a file.


| Owner         | Stage 1 area             | Principal types                                                                     | Evidence owned                                                                  |
| ------------- | ------------------------ | ----------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| M1 - Harshith | Customers and orders     | `Customer`, `RegularCustomer`, `Order`, `MenuItem`, `CustomerStatus`, `OrderStatus` | Lifecycle, satisfaction, polymorphic rule, food-selection and preparation tests |
| M2 - Vedavit  | Restaurant state         | `Restaurant`, `Table`                                                               | Queue, occupancy, payment, abandonment and statistics tests                     |
| M3 - Hunar    | Staff operations         | `Waiter`, `Chef`, `Cashier`, `ActionResult`                                         | Staff validation, action-result and timing tests                                |
| M4 - Viti     | Game control             | `GameController`, `TurnManager`, `ArrivalSchedule`                                  | Turn ordering, arrival, task-limit and result tests                             |
| M5 - Devansh  | Boundary and integration | `RestaurantRushApp`, `RestaurantCLI`, `GameSnapshot`, `CustomerView`, `TableView`   | CLI retries, displays, scripted runs and integration evidence                   |




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

### Package layout


| Package                   | Stage 1 types                                                                                                              |
| ------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| `restaurantrush.app`      | `RestaurantRushApp`                                                                                                        |
| `restaurantrush.boundary` | `RestaurantCLI`                                                                                                            |
| `restaurantrush.control`  | `GameController`, `TurnManager`, `ArrivalSchedule`                                                                         |
| `restaurantrush.domain`   | `Customer`, `RegularCustomer`, `Order`, `MenuItem`, `Restaurant`, `Table`, `CustomerStatus`, `OrderStatus`, `ActionResult` |
| `restaurantrush.staff`    | `Waiter`, `Chef`, `Cashier`                                                                                                |
| `restaurantrush.view`     | `GameSnapshot`, `CustomerView`, `TableView`                                                                                |




### Signature and failure conventions

- Signatures below are package-qualified by the table above; imports are omitted for readability.
- All listed fields are `private` unless explicitly marked otherwise. Constants are `private static final`.
- Constructor nulls and invalid primitive values throw `IllegalArgumentException` before an object is created.
- Invalid internal lifecycle transitions throw `IllegalStateException` without partial mutation.
- Invalid manager actions return `ActionResult.fail(...)`, preserve all state and do not consume a staff choice.
- A lookup documented as nullable returns `null` when no match exists. Other methods must not silently return `null`.
- Collection getters return `List.copyOf(...)` or an equivalent immutable copy; they never return the owned mutable collection.
- Owners may add private helper methods, but may not change a listed public/protected signature without the contract-change process in Section 10.



### 4.1 Boundary and immutable views



#### `RestaurantRushApp` — owner M5

Constructs one shared object graph and starts the application.

**Declaration:** `public final class RestaurantRushApp` with a private no-argument constructor.


| Public operation                         | Contract                                                                                                                                                            |
| ---------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `public static void main(String[] args)` | Create the Restaurant, ArrivalSchedule, Cashier, TurnManager, staff, GameController and RestaurantCLI, then call `run()`. Do not create duplicate domain instances. |


`RestaurantRushApp` has no instance fields and is not instantiated.

#### `RestaurantCLI` — owner M5

**Declaration:** `public final class RestaurantCLI`


| Private field | Type and initial contract                          |
| ------------- | -------------------------------------------------- |
| `controller`  | `final GameController`; non-null shared controller |
| `input`       | `final Scanner`; non-null command source           |
| `output`      | `final PrintStream`; non-null output destination   |



| Public operation                                                                     | Contract                                                                                                                                                                                                                                                 |
| ------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `public RestaurantCLI(GameController controller, Scanner input, PrintStream output)` | Store the three non-null collaborators. Injection keeps CLI behaviour testable without replacing `System.in` or `System.out`.                                                                                                                            |
| `public void run()`                                                                  | Start a turn, display its snapshot, retry Waiter input until one choice succeeds, retry Chef input until one choice succeeds, then finish the turn. Repeat through turn 18. Stop safely at end-of-input without inventing completed turns or an outcome. |
| `public void displayState(GameSnapshot snapshot)`                                    | Require non-null input. Show turn, revenue, paid-customer average or `N/A`, queue, tables, customer satisfaction/status, order progress and current staff availability.                                                                                  |
| `public void displayOutcome(GameSnapshot snapshot, boolean victory)`                 | Require a non-null finished snapshot. Show Victory or Defeat plus completed turns, served, paid, abandoned, revenue and paid-customer average.                                                                                                           |


The exact command words are agreed before CLI implementation. Malformed commands and non-integer IDs are explained and retried without calling a domain action. The CLI prints `ActionResult.message()` but does not recreate the validation rule.

##### Stage 1 CLI commands

Commands are case-insensitive and surrounding whitespace is ignored. IDs are the positive integers shown in the start-of-turn display.

| Phase | Command | Controller call |
| --- | --- | --- |
| Waiter | `seat <customerId> <tableId>` | `seatCustomer(customerId, tableId)` |
| Waiter | `order <customerId>` | `takeOrder(customerId)` |
| Waiter | `serve <customerId>` | `serveOrder(customerId)` |
| Waiter | `wait` | `waitWaiter()` |
| Chef | `cook <customerId>` | `prepareOrder(customerId)` |
| Chef | `wait` | `waitChef()` |
| Either | `help` | None; reprints the commands for the current phase and does not consume a choice |

- The prompt names the current phase, for example `Waiter> ` then `Chef> `, so the same `wait` word is unambiguous.
- An unknown command, wrong number of arguments or non-integer ID prints a usage message and re-prompts without calling the controller.
- A well-formed command that the domain rejects prints `ActionResult.message()` and re-prompts; the staff choice is not consumed.
- After `finishTurn()`, the CLI prints a one-line end-of-turn summary from the new snapshot: served, unhappy departures, revenue, paid-customer average (`N/A` before the first payment) and turns used.
- After turn 18, `displayOutcome()` prints Victory or Defeat with the final totals.

#### Immutable view records — owner M5

```java
public record GameSnapshot(
        int turn,
        int completedTurns,
        int revenueCents,
        Double averagePaidSatisfaction,
        List<Integer> waitingCustomerIds,
        List<CustomerView> customers,
        List<TableView> tables,
        boolean waiterAvailable,
        boolean chefAvailable,
        int paidCount,
        int servedCount,
        int abandonedCount) {}

public record CustomerView(
        int id,
        int satisfaction,
        CustomerStatus status,
        OrderStatus orderStatus,
        String itemName,
        Integer remainingUnits,
        Integer tableId) {}

public record TableView(int id, Integer occupantId) {}
```

`CustomerView` and `TableView` add these exact factories:


| Public operation                                                      | Contract                                                                  |
| --------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| `public static CustomerView from(Customer customer, Integer tableId)` | Require non-null customer and map its optional order fields consistently. |
| `public static TableView from(Table table)`                           | Require non-null table and map its optional occupant ID.                  |


Nullable `CustomerView` order fields are all `null` before ordering. `tableId` and `occupantId` are `null` when absent. `averagePaidSatisfaction` is `null` until the first payment. `GameSnapshot` uses a compact constructor to defensively copy list components with `List.copyOf(...)`; all three records reject invalid required values. `GameController.getSnapshot()` constructs the `GameSnapshot`, preventing a package dependency cycle between `control` and `view`. An existing snapshot must not change when the game later changes.

### 4.2 Control



#### `GameController` — owner M4

**Declaration:** `public final class GameController`


| Private field | Type and contract                              |
| ------------- | ---------------------------------------------- |
| `restaurant`  | `final Restaurant`; shared domain state        |
| `turnManager` | `final TurnManager`; sole turn-phase authority |
| `waiter`      | `final Waiter`; shared Waiter service          |
| `chef`        | `final Chef`; shared Chef service              |



| Public operation                                                                                  | Contract                                                                                                                                      |
| ------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| `public GameController(Restaurant restaurant, TurnManager turnManager, Waiter waiter, Chef chef)` | Store four non-null shared collaborators.                                                                                                     |
| `public void startTurn()`                                                                         | Delegate once to `turnManager.beginTurn(restaurant)` before the CLI displays the turn. Propagate lifecycle misuse as `IllegalStateException`. |
| `public ActionResult seatCustomer(int customerId, int tableId)`                                   | Fail if the Waiter choice is unavailable; otherwise delegate and record the choice only on success.                                           |
| `public ActionResult takeOrder(int customerId)`                                                   | Fail if the Waiter choice is unavailable; otherwise delegate and record the choice only on success.                                           |
| `public ActionResult serveOrder(int customerId)`                                                  | Fail if the Waiter choice is unavailable; otherwise delegate with the current turn and record the choice only on success.                     |
| `public ActionResult prepareOrder(int customerId)`                                                | Fail if the Chef choice is unavailable or no order exists; otherwise delegate and record the choice only on success.                          |
| `public ActionResult waitWaiter()`                                                                | Consume the available Waiter choice and return success; fail without mutation if unavailable.                                                 |
| `public ActionResult waitChef()`                                                                  | Consume the available Chef choice and return success; fail without mutation if unavailable.                                                   |
| `public void finishTurn()`                                                                        | Call `resolveTurn(restaurant)` and then `advanceTurn()` exactly once.                                                                         |
| `public GameSnapshot getSnapshot()`                                                               | Read Restaurant and TurnManager state, use the view factories, and return a new immutable snapshot.                                           |
| `public boolean isFinished()`                                                                     | Return `turnManager.isFinished()`.                                                                                                            |
| `public boolean isVictory()`                                                                      | Return false during play. After completion, return true only when all three objectives pass.                                                  |


Every manager action passes through the controller. An unavailable staff choice fails before invoking staff.

#### `TurnManager` — owner M4

**Declaration:** `public final class TurnManager`


| Private field    | Type and initial value                                                    |
| ---------------- | ------------------------------------------------------------------------- |
| `FINAL_TURN`     | `static final int`, value `18`                                            |
| `currentTurn`    | `int`, initially `1`, retained as `18` after completion                   |
| `completedTurns` | `int`, initially `0`, incremented once by each successful `advanceTurn()` |
| `waiterTaskUsed` | `boolean`, initially `false`                                              |
| `chefTaskUsed`   | `boolean`, initially `false`                                              |
| `turnStarted`    | `boolean`, initially `false`                                              |
| `turnResolved`   | `boolean`, initially `false`                                              |
| `finished`       | `boolean`, initially `false`                                              |
| `cashier`        | `final Cashier`, non-null automatic-payment collaborator                  |
| `arrivals`       | `final ArrivalSchedule`, non-null arrival collaborator                    |


`TurnManager` does not construct a concrete customer or maintain restaurant totals.


| Public operation                                                | Contract                                                                                                                                                    |
| --------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `public TurnManager(Cashier cashier, ArrivalSchedule arrivals)` | Store both non-null collaborators and initialise the fields above.                                                                                          |
| `public void beginTurn(Restaurant restaurant)`                  | Require non-null Restaurant and a not-started, unfinished turn. Reset task/resolution flags, obtain at most one arrival and add it once.                    |
| `public boolean canAssignWaiter()`                              | True only during an unresolved active turn whose Waiter choice is unused.                                                                                   |
| `public boolean canAssignChef()`                                | True only during an unresolved active turn after the Waiter choice and before the Chef choice.                                                              |
| `public void recordWaiterTask()`                                | Require `canAssignWaiter()`; mark the choice used. Otherwise throw `IllegalStateException`.                                                                 |
| `public void recordChefTask()`                                  | Require `canAssignChef()`; mark the choice used. Otherwise throw `IllegalStateException`.                                                                   |
| `public void resolveTurn(Restaurant restaurant)`                | Require non-null Restaurant, an active unresolved turn and both completed choices. Execute Section 5 automatic events exactly once, then mark resolved.     |
| `public void advanceTurn()`                                     | Require a started, resolved, unfinished turn. Increment `completedTurns`; either advance `currentTurn` or mark finished after turn 18; then close the turn. |
| `public int getCurrentTurn()`                                   | Return the current/displayed turn, from 1 through 18.                                                                                                       |
| `public int getCompletedTurns()`                                | Return completed turns, from 0 through 18.                                                                                                                  |
| `public boolean isFinished()`                                   | Return true only after turn 18 has resolved and advanced.                                                                                                   |




#### `ArrivalSchedule` — owner M4

**Declaration:** `public final class ArrivalSchedule`


| Private field         | Type and contract                                                                                           |
| --------------------- | ----------------------------------------------------------------------------------------------------------- |
| `CUSTOMER_ID_BY_TURN` | immutable `static final Map<Integer, Integer>` containing `1->1`, `4->2`, `7->3`, `10->4`, `13->5`, `16->6` |



| Public operation                       | Contract                                                                                                             |
| -------------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| `public ArrivalSchedule()`             | Construct the stateless Stage 1 schedule.                                                                            |
| `public Customer arrivalFor(int turn)` | Require `1 <= turn <= 18`. Return a new `RegularCustomer` with the mapped ID, or `null` on a valid non-arrival turn. |


`TurnManager` calls `arrivalFor()` only from a guarded `beginTurn()`, so duplicate start attempts cannot add duplicate arrivals.

### 4.3 Customer and order domain



#### `Customer` — owner M1

**Declaration:** `public abstract class Customer`

`Customer` is the lifecycle base for every Stage 1 customer. Stage 1 instantiates only `RegularCustomer`, while Restaurant, staff and control code depend on the base type.


| Private field    | Type and initial value                               |
| ---------------- | ---------------------------------------------------- |
| `id`             | `final int`; positive; base arrivals use 1 through 6 |
| `satisfaction`   | `int`; initially `100`; always 0 through 100         |
| `status`         | `CustomerStatus`; initially `WAITING`                |
| `servedTurn`     | `int`; initially `-1`; set once when served          |
| `readyToPayTurn` | `int`; initially `-1`; set once after eating         |
| `order`          | `Order`; initially `null`; assigned at most once     |



| Protected/public operation                                     | Contract                                                                                                                         |
| -------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| `protected Customer(int id)`                                   | Initialise the shared lifecycle for a positive immutable ID. Only a concrete customer subclass is instantiated.                  |
| `public abstract int waitingLossPerTurn()`                     | Return a nonnegative waiting loss. Called polymorphically by the final lifecycle method.                                         |
| `public abstract MenuItem chooseMenuItem(List<MenuItem> menu)` | Require a non-null, nonempty menu and return one item contained in it. The manager never supplies the selected item.             |
| `public abstract int quotePriceCents(MenuItem item)`           | Require non-null item and return a nonnegative bill to lock.                                                                     |
| `public final void seat()`                                     | Require `WAITING`; become `SEATED`.                                                                                              |
| `public final void acceptOrder(Order order)`                   | Require `SEATED`, no existing order, non-null order and matching IDs; store the order once.                                      |
| `public final void markServed(int turn)`                       | Require positive turn, `SEATED` and associated order already `SERVED`; become `EATING` and record the turn once.                 |
| `public final void finishEating(int turn)`                     | Require positive turn. If `EATING` and `servedTurn < turn`, become `READY_TO_PAY` and record the turn; otherwise make no change. |
| `public final void applyWaitingLoss()`                         | If `WAITING` or `SEATED`, subtract `waitingLossPerTurn()` with a floor of zero; otherwise make no change.                        |
| `public final void markPaid()`                                 | Require `READY_TO_PAY` and associated order already `PAID`; become `PAID`.                                                       |
| `public final void abandon()`                                  | Require `WAITING` or `SEATED`; cancel any unserved order and become `LEFT`. Restaurant coordinates queue/table cleanup.          |
| `public final int getId()`                                     | Return the immutable ID.                                                                                                         |
| `public final int getSatisfaction()`                           | Return current satisfaction.                                                                                                     |
| `public final CustomerStatus getStatus()`                      | Return current lifecycle state.                                                                                                  |
| `public final int getServedTurn()`                             | Return service turn, or `-1` before service.                                                                                     |
| `public final int getReadyToPayTurn()`                         | Return ready-to-pay turn, or `-1` before eating completes.                                                                       |
| `public final Order getOrder()`                                | Return the associated order, or `null` before ordering.                                                                          |


Lifecycle mutation stays in the base class. Subclasses supply only the documented behavioural rules unless a later contract deliberately changes that boundary.

#### `RegularCustomer` — owner M1

**Declaration:** `public final class RegularCustomer extends Customer`

`RegularCustomer` adds no fields.


| Public operation                                      | Contract                                                                  |
| ----------------------------------------------------- | ------------------------------------------------------------------------- |
| `public RegularCustomer(int id)`                      | Delegate positive-ID validation and shared initialisation to `Customer`.  |
| `public int waitingLossPerTurn()`                     | Return `8`.                                                               |
| `public MenuItem chooseMenuItem(List<MenuItem> menu)` | Return `menu.get((getId() - 1) % menu.size())` after validating the menu. |
| `public int quotePriceCents(MenuItem item)`           | Return `item.getPriceCents()` after validating the item.                  |




#### `Order` — owner M1

**Declaration:** `public final class Order`


| Private field    | Type and initial value                                        |
| ---------------- | ------------------------------------------------------------- |
| `id`             | `final int`; positive and equal to its customer's ID          |
| `item`           | `final MenuItem`; non-null                                    |
| `status`         | `OrderStatus`; initially `PLACED`                             |
| `remainingUnits` | `int`; initially `item.getPreparationUnits()`; never negative |
| `billCents`      | `final int`; explicit nonnegative locked quote                |



| Public operation                                           | Contract                                                                                                 |
| ---------------------------------------------------------- | -------------------------------------------------------------------------------------------------------- |
| `public Order(int id, MenuItem item, int lockedBillCents)` | Validate positive ID, non-null item, positive item units and nonnegative quote; create a `PLACED` order. |
| `public boolean canPrepare()`                              | True only for `PLACED` or `PREPARING` with positive remaining units.                                     |
| `public void prepareOneUnit()`                             | Require `canPrepare()`, subtract exactly one unit, then become `READY` at zero or `PREPARING` otherwise. |
| `public void markServed()`                                 | Require `READY`; become `SERVED`.                                                                        |
| `public void markPaid()`                                   | Require `SERVED`; become `PAID`.                                                                         |
| `public void cancel()`                                     | Require `PLACED`, `PREPARING` or `READY`; become `CANCELLED`.                                            |
| `public boolean isReady()`                                 | Return whether status is exactly `READY`; zero units alone is insufficient.                              |
| `public int getId()`                                       | Return immutable order ID.                                                                               |
| `public MenuItem getItem()`                                | Return immutable item reference.                                                                         |
| `public OrderStatus getStatus()`                           | Return current order state.                                                                              |
| `public int getRemainingUnits()`                           | Return remaining Chef units.                                                                             |
| `public int getBillCents()`                                | Return the locked bill.                                                                                  |


In Stage 1, Waiter must pass exactly `customer.quotePriceCents(item)`, which for `RegularCustomer` equals `item.getPriceCents()`. The separate constructor value is an exercised pricing boundary, not permission for arbitrary prices.

#### `MenuItem` — owner M1

**Declaration:** `public class MenuItem`


| Private field      | Type and contract                            |
| ------------------ | -------------------------------------------- |
| `name`             | `final String`; nonblank display name        |
| `priceCents`       | `final int`; nonnegative base price          |
| `preparationUnits` | `final int`; positive number of Chef actions |



| Public operation                                                     | Contract                                                    |
| -------------------------------------------------------------------- | ----------------------------------------------------------- |
| `public MenuItem(String name, int priceCents, int preparationUnits)` | Validate all three values and create an immutable offering. |
| `public final String getName()`                                      | Return display name.                                        |
| `public final int getPriceCents()`                                   | Return base price in cents.                                 |
| `public final int getPreparationUnits()`                             | Return required preparation units.                          |


There are no setters. The class is not an invitation to add later-stage subclasses now; Stage 1 uses three direct instances only.

#### Lifecycle enumerations — owner M1


| Exact declaration                                                                  | Normal flow                                                                                                       |
| ---------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| `public enum CustomerStatus { WAITING, SEATED, EATING, READY_TO_PAY, PAID, LEFT }` | `WAITING -> SEATED -> EATING -> READY_TO_PAY -> PAID`; `WAITING` or `SEATED` may instead become `LEFT`            |
| `public enum OrderStatus { PLACED, PREPARING, READY, SERVED, PAID, CANCELLED }`    | `PLACED -> PREPARING -> READY -> SERVED -> PAID`; `PLACED`, `PREPARING` or `READY` may instead become `CANCELLED` |


A one-unit order moves directly from `PLACED` to `READY`. `PAID`, `LEFT` and `CANCELLED` are terminal in their respective lifecycles.

### 4.4 Restaurant state



#### `Table` — owner M2

**Declaration:** `public final class Table`


| Private field | Type and initial value         |
| ------------- | ------------------------------ |
| `id`          | `final int`; positive table ID |
| `occupant`    | `Customer`; initially `null`   |



| Public operation                        | Contract                                                                                                                          |
| --------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| `public Table(int id)`                  | Require positive ID and create an available table.                                                                                |
| `public boolean isAvailable()`          | Return whether `occupant == null`.                                                                                                |
| `public void assign(Customer customer)` | Require non-null customer and an available table; store the occupant. Restaurant coordinates queue removal and `customer.seat()`. |
| `public void release()`                 | Require an occupant; clear it. Restaurant calls this only after valid payment or abandonment.                                     |
| `public int getId()`                    | Return immutable table ID.                                                                                                        |
| `public Customer getOccupant()`         | Return occupant, or `null` while available.                                                                                       |




#### `Restaurant` — owner M2

**Declaration:** `public final class Restaurant`


| Private field           | Type and initial value                                     |
| ----------------------- | ---------------------------------------------------------- |
| `waitingQueue`          | `final Deque<Customer>`; empty; arrival order              |
| `customers`             | `final List<Customer>`; empty; retains all arrivals        |
| `tables`                | `final List<Table>`; table IDs 1 and 2                     |
| `menu`                  | `final List<MenuItem>`; Salad, Burger, Pasta in that order |
| `orders`                | `final List<Order>`; empty; retains paid/cancelled history |
| `revenueCents`          | `int`; initially `0`                                       |
| `servedCount`           | `int`; initially `0`                                       |
| `abandonedCount`        | `int`; initially `0`                                       |
| `paidSatisfactionTotal` | `int`; initially `0`                                       |
| `paidCount`             | `int`; initially `0`                                       |



| Public operation                                                 | Contract                                                                                                                                                                                                            |
| ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `public Restaurant()`                                            | Create the exact Stage 1 tables/menu and initialise empty collections and zero totals.                                                                                                                              |
| `public void addArrival(Customer customer)`                      | Require non-null customer and unique positive ID; add the same object to history and queue. Invalid input changes neither.                                                                                          |
| `public ActionResult seat(int customerId, int tableId)`          | Require an existing queued customer and free table. Validate everything first, then remove from queue, assign table and call `customer.seat()` atomically.                                                          |
| `public ActionResult registerOrder(int customerId, Order order)` | Require a seated customer with no order, non-null matching-ID order and no registered duplicate. Validate first, then call `customer.acceptOrder(order)` and add the same object to order history.                  |
| `public ActionResult serve(int customerId, int turn)`            | Require positive turn, a seated table occupant and their registered `READY` order. Validate first, mark order served, mark customer eating and increment served count once.                                         |
| `public Customer oldestEligiblePayer(int turn)`                  | Require positive turn. Return the `READY_TO_PAY` customer with the earliest ready-to-pay turn strictly before this turn, breaking ties by lower customer ID, or `null`.                                             |
| `public boolean recordPayment(Customer customer, int turn)`      | Return false without mutation unless the supplied restaurant customer is eligible. Otherwise mark order then customer paid, release the table, add the locked bill and satisfaction, and increment paid count once. |
| `public void removeAbandoned(Customer customer)`                 | Require a restaurant customer at zero satisfaction in `WAITING` or `SEATED`. Remove from queue or release table, call `abandon()` and increment abandoned count once.                                               |
| `public Customer findCustomer(int id)`                           | Return matching historical customer, or `null`.                                                                                                                                                                     |
| `public Order findOrder(int id)`                                 | Return matching historical order, or `null`.                                                                                                                                                                        |
| `public Table findTable(int id)`                                 | Return matching table, or `null`.                                                                                                                                                                                   |
| `public Table findTableForCustomer(int customerId)`              | Return the customer's occupied table, or `null`.                                                                                                                                                                    |
| `public List<Customer> getActiveCustomers()`                     | Immutable copy excluding `PAID` and `LEFT`.                                                                                                                                                                         |
| `public List<Customer> getWaitingQueue()`                        | Immutable arrival-order copy of the queue.                                                                                                                                                                          |
| `public List<Customer> getCustomers()`                           | Immutable copy of all arrived customers.                                                                                                                                                                            |
| `public List<Table> getTables()`                                 | Immutable copy in table-ID order.                                                                                                                                                                                   |
| `public List<MenuItem> getMenu()`                                | Immutable copy in deterministic selection order.                                                                                                                                                                    |
| `public List<Order> getOrders()`                                 | Immutable copy of all orders.                                                                                                                                                                                       |
| `public int getRevenueCents()`                                   | Return successful-payment revenue.                                                                                                                                                                                  |
| `public int getPaidCount()`                                      | Return successful payment count.                                                                                                                                                                                    |
| `public int getServedCount()`                                    | Return successful service count.                                                                                                                                                                                    |
| `public int getAbandonedCount()`                                 | Return unhappy-departure count.                                                                                                                                                                                     |
| `public int getPaidSatisfactionTotal()`                          | Return sum of satisfaction recorded at payment.                                                                                                                                                                     |
| `public double getAveragePaidSatisfaction()`                     | Return floating-point total/count, or `0.0` when count is zero; snapshot converts the zero-count case to `null`.                                                                                                    |


All cross-object operations validate their complete request before mutation. `registerOrder()` makes Customer and Restaurant share one `Order` instance; Waiter never links them separately.

### 4.5 Staff and results

No common `Staff` superclass is required in Stage 1: Waiter, Chef and Cashier do not expose one genuinely substitutable action. They do not read input, print output or own duplicate staff-availability flags.

#### `Waiter` — owner M3

**Declaration:** `public final class Waiter`

Waiter has no fields or independent availability flag.


| Public operation                                                               | Contract                                                                                                                                                                                                                                  |
| ------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `public Waiter()`                                                              | Construct a stateless Waiter.                                                                                                                                                                                                             |
| `public ActionResult seat(Restaurant restaurant, int customerId, int tableId)` | Require non-null Restaurant and delegate seating.                                                                                                                                                                                         |
| `public ActionResult takeOrder(Restaurant restaurant, int customerId)`         | Fail if customer is missing, not `SEATED` or already has an order. Otherwise ask the customer to choose and quote, create `Order(customerId, item, quote)`, and delegate atomic linking to `restaurant.registerOrder(customerId, order)`. |
| `public ActionResult serve(Restaurant restaurant, int customerId, int turn)`   | Require non-null Restaurant and delegate service.                                                                                                                                                                                         |




#### `Chef` — owner M3

**Declaration:** `public final class Chef`

Chef has no fields or independent availability flag.


| Public operation                                  | Contract                                                                                                                                              |
| ------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `public Chef()`                                   | Construct a stateless Chef.                                                                                                                           |
| `public ActionResult prepareOneUnit(Order order)` | Fail without mutation for `null` or `!order.canPrepare()`. Otherwise call `order.prepareOneUnit()` exactly once and report whether it became `READY`. |




#### `Cashier` — owner M3

**Declaration:** `public final class Cashier`


| Private field  | Type and initial value                                       |
| -------------- | ------------------------------------------------------------ |
| `lastPaidTurn` | `int`; initially `-1`; updated only after successful payment |



| Public operation                                             | Contract                                                                                                                                                                                                 |
| ------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `public Cashier()`                                           | Initialise `lastPaidTurn` to `-1`.                                                                                                                                                                       |
| `public boolean collectOne(Restaurant restaurant, int turn)` | Require non-null Restaurant and positive turn. Return false if a payment already succeeded this turn or no eligible payer exists. Otherwise call `recordPayment`; update `lastPaidTurn` only on success. |


A customer becoming `READY_TO_PAY` this turn is not eligible until a later turn. `lastPaidTurn` is defensive protection; normal flow also invokes Cashier only once from `TurnManager.resolveTurn()`.

#### `ActionResult` — owner M3

**Exact declaration:** `public record ActionResult(boolean success, String message)`


| Public operation                                  | Contract                                             |
| ------------------------------------------------- | ---------------------------------------------------- |
| `public ActionResult { ... }` compact constructor | Require a non-null, nonblank message.                |
| `public static ActionResult ok(String message)`   | Return a successful result after message validation. |
| `public static ActionResult fail(String message)` | Return a failed result after message validation.     |
| `public boolean success()`                        | Generated record accessor.                           |
| `public String message()`                         | Generated record accessor.                           |


The record contains no printing or domain mutation.

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

- `docs/uml/stage-1-uml/class-diagram.drawio`
- a reviewed PDF or high-resolution PNG export for evidence

M5 coordinates the single editable file to avoid XML merge conflicts. M1-M4 supply and verify the attributes, operations and relationships for their own types. Only one person edits the Draw.io source at a time.

The Stage 1 diagram must match the `stage-1` code and show visibility, important attributes/operations, enums, the `Customer`/`RegularCustomer` inheritance, associations, multiplicities and meaningful composition. Annotate genuine examples of encapsulation, polymorphism and composition; do not add interfaces or relationships that are absent from the code.

The final submission will eventually require a Stage 3 class diagram and a sequence diagram, but their later-stage content is not designed in this Stage 1 contract. The team may draft a Stage 1 sequence for integration discussion without presenting it as the final design.

## 7. Stage 1 testing and functional evidence

Use a self-contained Java test runner unless the team explicitly agrees to a different offline-capable build. JUnit is optional; the brief assesses testing evidence and reproducibility rather than requiring a particular framework. Tests must never be marked passed unless their commands were actually run.

Each owner writes focused tests for their area. Cross-subsystem scenarios are reviewed jointly; M5 coordinates the scripted end-to-end runs but is not solely responsible for integration quality.

Maintain `docs/testing/stage-1-testscript/stage-1-tests.md` using the shared [test template](../testing/TEST-TEMPLATE.md). Keep the replay inputs and saved outputs in the same Stage 1 testing folder. Record:

- tester, reviewer, date, Java version and exact commit SHA;
- the command or repeatable steps;
- expected and observed outcomes;
- `PASS` or `FAIL`;
- unresolved defects or limitations; and
- the saved inputs and final output for separate victory and defeat runs.

Minimum Stage 1 coverage:

- compilation and launch;
- constructor validation, documented nullable lookups and immutable collection results;
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

For each AI-assisted Stage 1 feature branch, copy `docs/ai-prompts/TEMPLATE.md` to a concise file named after the branch in `docs/ai-prompts/stage-1-prompts/`. Record only prompts that materially affected design, code or tests, together with what was accepted or rejected, affected files, human verification and related commits.

Do not store credentials, private information or entire chat transcripts. Link the prompt record in the pull request and merge it with the relevant work. This supports the individual GAI declarations but does not replace them or prove that AI advice was correct.

## 9. Stage 1 release baseline

The published `stage-1` tag points to commit `1b9ebf92330316628010880245f98a67416aaef9`. The current folder reorganisation is uncommitted and is separate from that release. Keep the existing tag fixed when reviewing and committing the reorganisation.

Current build, test, scenario-replay and contribution instructions are in the [README](../../README.md). Observed results are in the [Stage 1 test record](../testing/stage-1-testscript/stage-1-tests.md).

## 10. Contract change process

- Change a public signature or shared invariant only through a pull request reviewed by every affected owner.
- In the same pull request, update the contract, relevant tests and Draw.io source/export.
- Record why the change was needed and whether it changes observable Stage 1 behaviour.
- After `stage-1` is tagged, treat that snapshot as fixed evidence. Begin Stage 2 by reviewing the tagged design and adding only the contracts needed for the new requirements.

