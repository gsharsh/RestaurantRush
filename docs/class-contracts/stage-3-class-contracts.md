# RestaurantRush Stage 3 class contracts draft

The [Stage 1 contract](stage-1-class-contracts.md) and [Stage 2 addendum](stage-2-class-contracts.md) remain the baseline for customer rules, lifecycle, staff actions, failure handling and ownership. This document proposes the Stage 3 APIs and team handoffs; it does not claim implementation or completed tests. The proposed choices below are design decisions, not additional requirements from the brief.

## Required scope

| Addition | Required behaviour |
| --- | --- |
| Host | Seat at most one queued customer per turn, in addition to the Waiter's task. The Waiter can still seat customers. |
| ComboMeal | Combine two existing menu items; preparation units are their sum, and price is 90% of their combined price, rounded to the nearest cent. Customers choose combos using a documented rule. |
| Happy Hour | Activate once per game at the start of a turn, lasting that turn and the next. Show remaining duration. Orders taken while active receive 20% off after the customer's VIP discount. At each active turn's start, customers waiting for a table or food recover 15 satisfaction, capped at 100. |

Retain two tables, the three base dishes, the six mixed arrivals on turns 1, 4, 7, 10, 13 and 16, all 18 turns, and the same victory thresholds: at least four paid customers, 4500 cents revenue and average paid satisfaction of at least 60. Existing orders keep their locked bills; departed customers are unaffected. The game remains independently playable offline at `stage-3`, using integer cents and the existing Java 17 build target.

### Proposed gameplay choices

- Add one `Salad + Pasta Combo` after Salad, Burger and Pasta in the menu. It costs 2430 cents and requires 3 preparation units. Keep the existing Regular/VIP ID-cycle selection over the resulting four-item menu and the Critic's preparation/price/menu-order preference. Customer 4 therefore chooses the combo; the manager never chooses a customer's food.
- Use the order **Host → Waiter → Chef**. A customer seated by the Host can have an order taken and its first preparation unit completed that turn. Both Host and Waiter may seat different customers when tables are free.
- Ask whether to activate Happy Hour before beginning the turn. Add the scheduled arrival, apply any activation and recovery, then display the state and request staff actions. Activation consumes no staff task.
- Derive Happy Hour's remaining duration from its activation turn instead of maintaining a second clock. Allow activation on turn 18; the game still ends after turn 18.
- Round each monetary step to the nearest cent, with half cents rounded up: combo price, customer quote, then Happy Hour. Preserve the existing VIP rounding policy.

## Proposed ownership and handoffs

| Owner | Stage 3 responsibility | Handoff / review |
| --- | --- | --- |
| M1 — Harshith | ComboMeal, Customer recovery method and focused domain tests | M2 registers the combo and reviews arithmetic; M3 consumes the existing item/quote APIs |
| M2 — Vedavit | HappyHour policy, Restaurant menu assembly and policy/state tests | M1 reviews rounding/recovery; M3/M4 share the same policy instance |
| M3 — Hunar | Host, Waiter pricing handoff and mixed staff/payment regressions | M2 reviews seating/accounting; M4 integrates task limits |
| M4 — Viti | TurnManager/GameController changes and timing/integration tests | Agree start/action order with M3/M5; preserve arrival and result rules |
| M5 — Devansh | App wiring, CLI/views, replay evidence, README transition and diagram coordination | Each owner supplies/verifies their class details and shared scenarios |

## Proposed new class contracts

Constructor nulls and invalid values throw `IllegalArgumentException`. Invalid internal lifecycle calls throw `IllegalStateException`. Rejected manager actions return `ActionResult.fail(...)`, preserve state and do not consume a staff choice. Fields remain private; collections and snapshots remain immutable.

### `ComboMeal` — domain / M1

**Declaration:** `public final class ComboMeal extends MenuItem`

`MenuItem` is already extensible, with private final values and final getters. A combo uses those existing price/preparation getters and can pass through `Customer`, `Order`, Chef and views as a `MenuItem`.

| Private field | Contract |
| --- | --- |
| `first`, `second` | `final MenuItem`; non-null component references, retained unchanged |

| Public operation | Contract |
| --- | --- |
| `public ComboMeal(String name, MenuItem first, MenuItem second)` | Apply inherited name validation. Set price to 90% of the combined component price, rounded as above; set preparation units to their sum. Calculate with `long` intermediates and reject results outside the existing `int` range. |
| `public MenuItem getFirst()` | Return the original first component. |
| `public MenuItem getSecond()` | Return the original second component. |

Compute price as `(9L * ((long) first.getPriceCents() + second.getPriceCents()) + 5L) / 10L`, checking the result before conversion to `int`. The base setting registers the single combo above using the existing Salad and Pasta instances. There is no manager-facing combo builder. Shared component references do not imply exclusive UML composition.

### `HappyHour` — domain / M2

**Declaration:** `public final class HappyHour`

| Private field | Contract |
| --- | --- |
| `activationTurn` | `int`, initially `0` for unused; otherwise the turn of the single successful activation |

| Public operation | Contract |
| --- | --- |
| `public HappyHour()` | Start unused and inactive. |
| `public boolean hasBeenActivated()` | True after activation, including after the two-turn period expires. |
| `public void activate(int turn)` | Require a turn from 1 to 18 and no earlier activation. Set the activation turn once; repeated activation throws `IllegalStateException` without mutation. |
| `public int remainingTurns(int turn)` | Require a turn from 1 to 18. Return 2 on the activation turn, 1 on the next, and 0 otherwise. This query does not change state. |
| `public int quotePriceCents(int customerQuoteCents, int turn)` | Reject a negative quote or invalid turn. When active, return `(int) ((8L * customerQuoteCents + 5L) / 10L)`; otherwise return the supplied quote unchanged. Zero is valid. |

Happy Hour receives an already customer-discounted integer-cent quote. For example, a VIP Burger is 1500 → 1350 → 1080 cents. A VIP Salad + Pasta Combo is 2700 → 2430 → 2187 → 1750 cents. Each step rounds independently; do not replace the sequence with a single combined percentage. The policy does not change an existing `Order` or own a turn counter.

### `Host` — staff / M3

**Declaration:** `public final class Host`, with no mutable fields.

| Public operation | Contract |
| --- | --- |
| `public Host()` | Construct the stateless service. |
| `public ActionResult seat(Restaurant restaurant, int customerId, int tableId)` | Reject a null Restaurant or illegal seating without mutation; otherwise delegate to `Restaurant.seat(...)`. |

The Host does not track its own availability. `TurnManager` enforces its one-task limit, just as it does for the other staff.

## Changes to existing class contracts

Unlisted APIs retain their Stage 2 contracts. Update affected Stage 3 callers and tests together when replacing a signature; earlier stages remain available from their tags.

### Customer and restaurant domain — M1 and M2

| Class / operation | Stage 3 contract |
| --- | --- |
| `Customer`: add `public final void recoverWaitingSatisfaction(int amount)` | Reject negative amounts. Add the amount only while `WAITING` or `SEATED`, capped at 100 using overflow-safe arithmetic. All other states are unchanged. A customer with READY-but-unserved food still qualifies; a departed customer is never restored. |
| `Restaurant`: existing constructor / `getMenu()` | Register the single combo after the existing three dishes, preserving immutable menu exposure and component identity. No new public menu-management API is required. |
| `RegularCustomer`, `VIPCustomer`, `CriticCustomer` | Keep their existing selection and quotation methods. They operate on the expanded `List<MenuItem>` without combo/type checks. |
| `Order`, `MenuItem`, lifecycle enums and `Table` | Keep their existing APIs and lifecycle. An order for a combo is one order, with summed preparation units and a single locked bill. |

### `Waiter` — M3

Add a private `final HappyHour happyHour` reference. Replace the constructor and order-taking signatures below; seating and serving are unchanged.

| Public operation | Stage 3 contract |
| --- | --- |
| `public Waiter(HappyHour happyHour)` | Store the non-null shared policy. |
| `public ActionResult takeOrder(Restaurant restaurant, int customerId, int turn)` | Preserve existing validation and reject an invalid turn before mutation. Ask the customer to select an item and quote its price, apply `happyHour.quotePriceCents(...)`, then create/register the shared `Order` with that final bill. |

The controller supplies the current turn. Neither Cashier nor payment accounting reapplies a discount. A failed order action leaves the customer, orders, totals and staff choice unchanged.

### `GameController` and `TurnManager` — M4

`GameController` adds a private `final Host host`. `TurnManager` adds a private `final HappyHour happyHour` reference and a private `boolean hostTaskUsed`, initially false. It remains the sole authority for staff availability and turn phase.

| Class / public operation | Stage 3 contract |
| --- | --- |
| `public GameController(Restaurant restaurant, TurnManager turnManager, Host host, Waiter waiter, Chef chef)` | Replace the existing constructor; store the five non-null shared collaborators. |
| `GameController`: `public ActionResult startTurn(boolean activateHappyHour)` | Replace `startTurn()`. Delegate to the new `beginTurn(...)`; return its result. |
| `GameController`: `public ActionResult seatCustomerWithHost(int customerId, int tableId)` | Fail before invoking Host if its choice is unavailable; record the Host task only after successful seating. |
| `GameController`: `public ActionResult waitHost()` | Consume the available Host choice; fail without mutation if unavailable. |
| `GameController`: existing `takeOrder(int customerId)` | Pass the current turn to Waiter and record the Waiter choice only after success. |
| `public TurnManager(Cashier cashier, ArrivalSchedule arrivals, HappyHour happyHour)` | Replace the existing constructor; store the three non-null collaborators. |
| `TurnManager`: `public ActionResult beginTurn(Restaurant restaurant, boolean activateHappyHour)` | Replace `beginTurn(Restaurant)`. Reject repeated activation with a failure result before any mutation. Null Restaurant and duplicate/finished turn starts retain their existing exceptions. On success, begin the turn according to the sequence below. |
| `TurnManager`: `public boolean canAssignHost()` | True only during an active unresolved turn before the Host choice is used. |
| `TurnManager`: `public void recordHostTask()` | Require `canAssignHost()`; otherwise throw `IllegalStateException` without mutation. |
| `TurnManager`: existing `canAssignWaiter()` / `canAssignChef()` | Waiter requires the Host choice to be complete; Chef still requires the Waiter choice to be complete. Each choice is usable once. |
| `TurnManager`: `public boolean hasHappyHourBeenActivated()` | Delegate to the shared policy for the snapshot's used flag. |
| `TurnManager`: `public int getHappyHourTurnsRemaining()` | Delegate using the current turn; return 0 once the game has finished. |
| `TurnManager`: existing `resolveTurn(...)` / `advanceTurn()` | Require all three staff choices before resolution. Preserve the existing automatic events, single-resolution guards and full 18-turn duration. |

Keep `GameController.seatCustomer(...)` as the Waiter's seating action. The arrival schedule and victory checks remain unchanged.

### App, CLI and immutable views — M5

- `RestaurantRushApp` constructs one `HappyHour`, shared by TurnManager and Waiter, plus one Host. Update controller wiring; do not create separate policy instances.
- `GameSnapshot` adds `boolean hostAvailable` immediately before `waiterAvailable`, then appends `int happyHourTurnsRemaining` and `boolean happyHourUsed` after its existing components. Validate remaining duration as 0–2. `GameController.getSnapshot()` copies these values from TurnManager; completed-game snapshots show 0 remaining turns. Update every record constructor and test fixture.
- Keep `CustomerView` and `TableView` unchanged. Combo name/status display uses the existing item fields. Display Host availability and Happy Hour duration, distinguishing staff waiting for an earlier phase from staff already used.
- Before starting a turn, prompt `Happy Hour> on/skip` while the policy is unused. `on` calls `startTurn(true)`; `skip` calls `startTurn(false)`. Once used, call `startTurn(false)` directly on later turns. Retry rejected choices without starting a turn twice.
- Add `Host> seat <customerId> <tableId>` and `Host> wait`, calling `seatCustomerWithHost(...)` and `waitHost()`. Retain the Waiter and Chef commands and non-consuming `help`. Malformed input and rejected actions retry; end-of-input stops without inventing completed turns or an outcome.

## Turn and integration contract

1. Collect the Happy Hour choice. Validate turn lifecycle and activation eligibility before changing any state.
2. Obtain and add the scheduled arrival. If this fails, preserve the turn flags, Happy Hour state and existing customer satisfaction.
3. If requested, activate Happy Hour. Reset the three staff flags and begin the turn. During an active period, call `recoverWaitingSatisfaction(15)` once for each eligible customer. A new arrival is included but remains capped at 100.
4. Display the updated snapshot, including Happy Hour duration and staff availability.
5. Complete one Host action or wait, then one Waiter action or wait, then one Chef action or wait.
6. Finish earlier-turn eating, process at most one earlier-ready payment, apply normal waiting losses, and handle abandonment in the existing order.
7. Advance the turn once. After turn 18, evaluate all three existing victory conditions; no turn 19 is played.

Happy Hour is active only on its activation turn and the following turn. Recovery occurs before staff actions and does not replace the normal end-of-turn losses. An order taken during the active period keeps its discounted bill after expiry; one taken earlier keeps its earlier bill. Waiter-before-Chef still prevents serving a dish completed later in the same turn, and the existing eating/payment delays remain in force.

## Acceptance and evidence checklist

These are planned checks; no Stage 3 result is claimed yet.

- [ ] Verify combo component identity, 2430-cent/3-unit base values, rounding, null/name validation and overflow handling through the existing MenuItem/Order APIs.
- [ ] Verify customer-owned combo selection, unchanged VIP/Critic rules and no concrete-type branching in controller or staff behaviour.
- [ ] Verify Host and Waiter can each seat once, same-turn Host seating/Waiter ordering/Chef preparation, task gates and rejected-action preservation.
- [ ] Verify Happy Hour activates once, lasts exactly two turns, displays 2 → 1 → 0, rejects reactivation without mutation and does not extend turn 18.
- [ ] Verify exactly one 15-point recovery per active turn, capping at 100; include queued, seated and READY-but-unserved customers and exclude eating, payment-ready, paid and departed customers. Preserve normal waiting losses.
- [ ] Verify VIP-before-Happy-Hour quotation, combo pricing, rounding boundaries, locked earlier orders, post-expiry bills and paid-only totals.
- [ ] Run the existing and new Java tests, plus separate saved 18-turn Stage 3 victory/defeat scenarios. Record expected/observed results, candidate SHA and Java version in `docs/testing/stage-3-tests.md`, following the concise Stage 1/2 format.
- [ ] Update the final class diagram to match Stage 3, with visibility, relationships/multiplicities and genuine encapsulation, polymorphism and composition examples. Complete one sequence diagram showing a manager decision, arrival/order, staff assignments, READY/SERVED, eating, payment, result check and a Stage 2/3 behaviour, as required by brief §5.
- [ ] Record the Stage 2 → Stage 3 file changes, reasons and commit/compare links in the README; review the runnable final candidate and publish `stage-3`, preserving earlier stage tags.
