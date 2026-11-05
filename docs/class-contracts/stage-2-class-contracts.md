# RestaurantRush Stage 2 class contracts

## Status and baseline

Originally prepared on 2026-10-29 from the [assignment brief](../SC2002_Restaurant_Rush_Group_Assignment.docx), especially §§2–3, 4.2, 4.4 and 5. Updated on 2026-11-06 to describe the implemented Stage 2 behaviour and its verification.

The published Stage 1 baseline is tag `stage-1` at `1b9ebf92330316628010880245f98a67416aaef9`. Stage 2 implementation, mixed-customer tests, replay evidence and UML are integrated in candidate `97463b1ce1481806c5861047f7a16bb71b0674e2`, which includes the folder reorganisation from `1dadd0b`. The [Stage 2 test record](../testing/stage-2-tests.md) records the observed checks and release gates. The Critic preference, ownership split and type-label API are confirmed; the arrival mapping below is implemented and exercised by the saved runs. General-price VIP rounding is implemented as the documented working policy; separate human agreement on that design choice is not recorded. The `stage-2` tag has not yet been created or published.

The [Stage 1 contract](stage-1-class-contracts.md) remains the baseline for lifecycle, state ownership, staff actions, failure handling and collaboration. Stage 2 extends customer rules and arrival construction. Stage 1 evidence remains available at its published tag and in `docs/testing/stage-1-tests.md`.

## Required scope

| Customer | Satisfaction loss per waiting turn | Bill | Additional behaviour |
| --- | --- | --- | --- |
| Regular | 8 | Full base price | Existing deterministic menu choice |
| VIP | 5 | 10% off base price | Deterministic customer-owned menu choice |
| Critic | 12 | Full base price | A distinct documented order preference or reaction to service |

The brief requires:

- `VIPCustomer` and `CriticCustomer` alongside Regular customers, using the same order and service lifecycle.
- Six arrivals on turns 1, 4, 7, 10, 13 and 16, including at least one VIP and one Critic in a reproducible base-setting run.
- Behaviour dispatched through customer objects, without chains of customer-type checks in the turn controller.
- An independently playable `stage-2` version with the same two tables, menu, staff limits, full 18-turn duration and victory thresholds: at least four paid customers, 4500 cents revenue and average paid satisfaction of at least 60.
- Offline operation, integer-cent money, robust action retries and the existing timing rules.

The team also requires customer-type labels in the CLI, supplied polymorphically through `Customer.typeLabel()` and the immutable `CustomerView.type` component.

Stage 2 targets Java 17 under the Stage 1 team build agreement; the assignment brief specifies Java without prescribing a version. Compilation uses `javac --release 17`. The integrated verification ran on Java 26.0.2, so execution on a Java 17 runtime remains unverified.

Host, ComboMeal and Happy Hour remain Stage 3 work. Stage 2 does not require a staff superclass, new build dependencies or a stage-selection menu; each stage is independently runnable from its tag.

## Implemented customer contracts — M1 and M2

The two final classes in `restaurantrush.domain` each extend `Customer` directly and add no lifecycle state. Their public surface mirrors `RegularCustomer`:

```java
public VIPCustomer(int id)
public CriticCustomer(int id)
// Both override the Customer methods, including the display method:
public String typeLabel()
public int waitingLossPerTurn()
public MenuItem chooseMenuItem(List<MenuItem> menu)
public int quotePriceCents(MenuItem item)
```

Constructors delegate positive-ID validation and initial state to `super(id)`. Customer objects start with satisfaction 100. Inherited final methods own seating, order assignment, service, eating, payment, satisfaction clamping and departure.

### VIPCustomer — M1 / Harshith

- `waitingLossPerTurn()` returns `5`.
- `chooseMenuItem(menu)` uses the existing ID cycle: `menu.get((getId() - 1) % menu.size())`. Reject a null/empty menu or null selected item with `IllegalArgumentException`; return the existing item instance and leave the menu unchanged.
- `quotePriceCents(item)` rejects null and returns 90% of the base price. Required base-menu quotes are Salad 810, Burger 1350 and Pasta 1620 cents.
- Implemented working policy for other integer-cent prices: round the discounted total to the nearest cent, with half cents rounded up, using `(int) ((9L * item.getPriceCents() + 5L) / 10L)`. The `long` intermediate avoids overflow; zero remains valid. Tests cover the rounding boundaries and large valid prices. The brief's Stage 2 base prices do not require fractional-cent rounding, so this general-case policy is a team design choice awaiting recorded human agreement.

### CriticCustomer — M2 / Vedavit

- `waitingLossPerTurn()` returns `12`.
- Confirmed distinct preference: choose the item with the most preparation units; when preparation units tie, choose the higher base-price item. If both values tie, retain the earliest item in the supplied menu. The base menu therefore produces Pasta (2 preparation units, 1800 cents). This defines preference through item properties rather than a hard-coded dish name.
- `chooseMenuItem(menu)` rejects a null/empty menu or any null entry with `IllegalArgumentException`, because the rule examines every item. Return the winning existing item instance without sorting or mutating the menu.
- `quotePriceCents(item)` rejects null and returns `item.getPriceCents()`, including zero.
- No additional service bonus or penalty is implemented. The distinct preference satisfies the brief's additional-behaviour requirement.

`Customer` and `RegularCustomer` include the type-label API described below. The existing `Customer`, `RegularCustomer`, `Order`, `MenuItem` and lifecycle enums otherwise retain their shared contracts. VIP extends `Customer` directly, lifecycle methods remain inherited, and no placeholder Stage 3 policies are included.

## Implemented arrival mapping — M4 / Viti

`ArrivalSchedule.arrivalFor(int)` uses the mapping below while retaining its signature, valid-turn checks, fresh-object behaviour and null result on non-arrival turns.

| Turn | Customer ID | Type | Base-menu order |
| --- | --- | --- | --- |
| 1 | 1 | Regular | Salad |
| 4 | 2 | VIP | Burger |
| 7 | 3 | Regular | Pasta |
| 10 | 4 | Critic | Pasta |
| 13 | 5 | Regular | Burger |
| 16 | 6 | Regular | Pasta |

This placement makes both additions observable before the final arrival: ID 2 receives a discounted Burger, and ID 4 chooses Pasta where a Regular customer with that ID would choose Salad. The saved victory run demonstrates both behaviours; the [test record](../testing/stage-2-tests.md) documents the exact bills, satisfaction values and final outcomes.

Concrete construction belongs in `ArrivalSchedule`. `TurnManager` continues using `Customer` and invoking the existing lifecycle operations. Do not branch on a customer's class, class name, type label or ID to calculate satisfaction, selection or billing outside its rule implementation.

## Stage 2 handoffs and ownership

The following division shares implementation, regression checks and evidence across all five members. Stage 1 ownership remains the baseline for reviewing changes to existing classes; Stage 2 assigns VIP and Critic implementation separately. Members need not change production classes merely to create a contribution.

| Owner | Stage 2 responsibility | Handoff / review |
| --- | --- | --- |
| M1 — Harshith | `VIPCustomer`, discount/selection/waiting-loss rules, `Customer.typeLabel()` contract and Regular/VIP labels, focused tests and VIP contract details | M2 reviews rule validation; M3 checks discounted bills; M4 registers the class; M5 consumes the labels |
| M2 — Vedavit | `CriticCustomer`, preference/waiting-loss/full-price rules and Critic label, focused Critic tests and Critic contract details | M1 reviews preference and tie-breaking; M3 checks staff/payment behaviour; M4 registers the class; M5 consumes the label |
| M3 — Hunar | Focused staff/payment regressions with all customer types: locked bills, invalid-action preservation, satisfaction, abandonment and table release | Review with M1/M2 and M4; change existing staff/Restaurant/Table code only if a demonstrated gap requires it |
| M4 — Viti | Arrival mapping and tests for all 18 turns, plus controller/turn regression checks | Integrate after M1 and M2 classes are available; retain staff/task/timing rules |
| M5 — Devansh | Required customer-type display, immutable `CustomerView.type` component, affected view/CLI tests, reproducible victory/defeat evidence, README transition record and shared UML coordination | Coordinate the agreed API with M1/M2 and snapshot/controller changes with M4; coordinate evidence review with all members |

Each member supplies and verifies the UML attributes, operations and relationships for their own changes. M5 coordinates the single editable Draw.io file and export; diagram content is a shared responsibility.

The existing pricing handoff remains `Customer.chooseMenuItem` → `Customer.quotePriceCents` → `Order` with a locked bill → `Restaurant` payment accounting. Neither Waiter nor Cashier should recalculate a subtype's price.

### Required type-label handoff — M1, M2, M4 and M5

Customer-type labels are adopted as a Stage 2 team requirement so players can identify VIPs and Critics when prioritising service, and video segment 4 can show their behaviour:

- `Customer` declares `public abstract String typeLabel()`. `RegularCustomer` and `VIPCustomer` return exactly `"Regular"` and `"VIP"` respectively; `CriticCustomer` returns exactly `"Critic"`.
- `CustomerView` has a required `String type` record component immediately after `id`. Its `from(Customer customer, Integer tableId)` factory copies `customer.typeLabel()` into the immutable view; the CLI displays the resulting `type()` value wherever customers are listed.
- Affected constructors, test customer subclasses, snapshot/view assertions and CLI tests include the API, including M3's staff test doubles. Labels are presentation data: never put behavioural rules or concrete-type checks in the CLI or use a label to select gameplay rules.
- The method and view component are included in the Stage 2 class diagram. The final class-diagram annotations and sequence diagram are scheduled after Stage 3, as described below.

## Implementation and release sequence

1. The confirmed Critic preference, ownership split and required type-label API guided the implementation. VIP uses the documented general-price rounding policy; its separate human agreement remains to be recorded.
2. VIP, Critic, the base type-label API and the view/CLI handoff are implemented with focused tests. Existing lifecycle and staff contracts are preserved.
3. The mixed arrival schedule is integrated. All 15 test runners, including actual-subtype staff/payment regressions, passed alongside the saved 18-turn victory and defeat replays.
4. The Stage 2 Draw.io source/export and gameplay evidence are present. The README records the implemented transition. The team still reviews the final documentation candidate and records human sign-off before creating and publishing `stage-2`.

Use focused feature branches from the current `main`, owner-reviewed pull requests and the existing AI-prompt/test evidence templates. Keep the published `stage-1` tag fixed while developing Stage 2.

## Acceptance and evidence checklist

The technical checks below were verified in the integrated audit; the [Stage 2 test record](../testing/stage-2-tests.md) supplies the candidate SHA, commands, observed results and limitations. Checked items record technical evidence, not human approval or a published release.

- [x] Exercise Regular, VIP and Critic rules through `Customer` references. Regular behaviour remains unchanged.
- [x] Verify exact `Regular`, `VIP` and `Critic` labels through `Customer` references, immutable `CustomerView.type` snapshots and CLI customer listings, without concrete-type checks. Update affected constructors and test customer subclasses.
- [x] Verify VIP quotes for all base-menu prices, zero, fractional-cent rounding and a large valid price without overflow; reject null input.
- [x] Verify Critic choice with reordered/custom menus: a higher-preparation item wins even when cheaper; equal preparation units are resolved by higher price; equal preparation units and price retain the first menu item. Include an ID where Regular would choose differently, and verify full-price quotes and invalid-input handling.
- [x] Verify that rule methods preserve menu/item identity and do not mutate shared menu state.
- [x] Verify satisfaction loss only while WAITING or SEATED, clamping at zero and no loss after service. Cover abandonment, cancellation of READY-but-unserved food, no revenue on departure and table release.
- [x] Verify exactly six arrivals at the required turns and IDs, including the implemented VIP/Critic placements, no arrivals on other turns, invalid-turn rejection and independent fresh instances.
- [x] Verify staff substitution with the actual subtypes, a single shared Order instance, locked VIP prices and paid-only revenue/satisfaction totals.
- [x] Preserve invalid-action retries, one task per staff member, Waiter-before-Chef ordering, later-turn service/eating/payment and oldest-first single payment.
- [x] Compile production and test sources, then execute every test runner with `./test.sh`. Record compiler/runtime versions and exact candidate SHA; compilation targeting Java 17 does not establish execution on a Java 17 runtime.
- [x] Record a mixed-customer victory run and defeat run through all 18 turns in `docs/testing/stage-2-testscript/stage-2-*.txt` / `.out`, with exact observed results in `docs/testing/stage-2-tests.md`. Both new types appear; the victory evidence demonstrates their ordering/pricing differences.
- [x] Review source to ensure subtype behaviour is polymorphic and Stage 3 features are absent.
- [x] Add the Stage 2 Draw.io source/export under `docs/uml/stage-2-uml/` to match implemented Stage 2 classes. Preserve Stage 1 scenario files as historical evidence; Stage 2 outputs differ intentionally.
- [x] Complete the README transition record with actual new/changed/deleted files, reasons and relevant implementation commit or compare links.
- [ ] Record human evidence review, including agreement on the implemented general-price VIP rounding policy and UML owner confirmation.
- [ ] Commit the final documentation, verify the resulting candidate and clean working tree, then create and publish `stage-2` through the team's release process.

## Diagram completion after Stage 3

Maintain the Stage 2 class diagram for its release. Schedule the following final-submission diagram work after Stage 3 is complete; these are not Stage 2 release gates:

- [ ] Annotate the final class diagram with genuine examples of encapsulation, polymorphism and composition, as required by brief §5. Each member supplies the details for their classes, with M5 coordinating the Draw.io source and export.
- [ ] Choose a scenario, assign ownership and complete the sequence diagram after Stage 3. It must include a Stage 2 or Stage 3 behaviour; VIP pricing is a possible scenario, with the final choice deferred until then.

Stage 2 gameplay and technical verification are complete as recorded above. Human release sign-off and publication of `stage-2` remain pending; this document does not claim either has occurred.
