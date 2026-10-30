# RestaurantRush Stage 2 contract draft

## Status and baseline

Prepared on 2026-10-29 from the [assignment brief](../SC2002_Restaurant_Rush_Group_Assignment.docx), especially §§2–3, 4.2, 4.4 and 5, and the current Stage 1 implementation.

The published Stage 1 baseline is tag `stage-1` at `1b9ebf92330316628010880245f98a67416aaef9`. The folder reorganisation and script fixes from `main` commit `1dadd0bbda58403d33afb2b7f7dd795dabdd8792` are merged into this branch. This documentation work is on `docs/stage-2`. The user has confirmed the move to Stage 2, the Critic preference, the ownership split and the type-label API below. This is a design draft, not an implementation or test result. The VIP rounding policy and arrival mapping remain proposals until reviewed.

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

Stage 2 retains Java 17 compatibility from the Stage 1 team build agreement; the assignment brief specifies Java without prescribing a version.

Host, ComboMeal and Happy Hour remain Stage 3 work. Stage 2 does not require a staff superclass, new build dependencies or a stage-selection menu; each stage is independently runnable from its tag.

## Proposed customer contracts — M1 and M2

Add two final classes in `restaurantrush.domain`, each extending `Customer` directly and adding no lifecycle state. Their public surface mirrors `RegularCustomer`:

```java
public VIPCustomer(int id)
public CriticCustomer(int id)
// Both override the Customer methods, including the new display method:
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
- Proposed policy for other integer-cent prices: round the discounted total to the nearest cent, with half cents rounded up, using `(int) ((9L * item.getPriceCents() + 5L) / 10L)`. The `long` intermediate avoids overflow; zero remains valid. The brief's Stage 2 base prices do not require fractional-cent rounding, so this general-case policy is a team design choice.

### CriticCustomer — M2 / Vedavit

- `waitingLossPerTurn()` returns `12`.
- Confirmed distinct preference: choose the item with the most preparation units; when preparation units tie, choose the higher base-price item. If both values tie, retain the earliest item in the supplied menu. The base menu therefore produces Pasta (2 preparation units, 1800 cents). This defines preference through item properties rather than a hard-coded dish name.
- `chooseMenuItem(menu)` rejects a null/empty menu or any null entry with `IllegalArgumentException`, because the rule examines every item. Return the winning existing item instance without sorting or mutating the menu.
- `quotePriceCents(item)` rejects null and returns `item.getPriceCents()`, including zero.
- No additional service bonus or penalty is proposed. The distinct preference satisfies the brief's additional-behaviour requirement.

Add the type-label API described below to `Customer` and `RegularCustomer`. Otherwise keep the current `Customer`, `RegularCustomer`, `Order`, `MenuItem` and lifecycle enums unless implementation identifies a specific defect or necessary contract change. In particular, do not make VIP extend the final `RegularCustomer`, duplicate lifecycle methods, or add placeholder Stage 3 policies.

## Proposed arrival mapping — M4 / Viti

Update `ArrivalSchedule.arrivalFor(int)` while retaining its signature, valid-turn checks, fresh-object behaviour and null result on non-arrival turns.

| Turn | Customer ID | Type | Base-menu order |
| --- | --- | --- | --- |
| 1 | 1 | Regular | Salad |
| 4 | 2 | VIP | Burger |
| 7 | 3 | Regular | Pasta |
| 10 | 4 | Critic | Pasta |
| 13 | 5 | Regular | Burger |
| 16 | 6 | Regular | Pasta |

This placement makes both additions observable before the final arrival: ID 2 receives a discounted Burger, and ID 4 chooses Pasta where a Regular customer with that ID would choose Salad. These order expectations follow the proposed rules; no Stage 2 gameplay results have been recorded yet.

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

- M1 adds `public abstract String typeLabel()` to `Customer` and implements it in `RegularCustomer` and `VIPCustomer`, returning exactly `"Regular"` and `"VIP"` respectively. M2 implements it in `CriticCustomer`, returning exactly `"Critic"`.
- M5 adds a required `String type` record component immediately after `id` in `CustomerView`. Its `from(Customer customer, Integer tableId)` factory copies `customer.typeLabel()` into the immutable view; the CLI displays the resulting `type()` value wherever customers are listed.
- Each affected owner updates constructors, test customer subclasses, snapshot/view assertions and CLI tests for the new API, including M3's staff test doubles. Labels are presentation data: never put behavioural rules or concrete-type checks in the CLI or use a label to select gameplay rules.
- Include the method and view component in the Stage 2 class diagram. The final class-diagram annotations and sequence diagram are scheduled after Stage 3, as described below.

## Integration order

1. Carry forward the confirmed Critic preference, ownership split and required type-label API; review the VIP rounding policy and arrival mapping.
2. M1 implements VIP, the base type-label method and Regular/VIP labels; M2 implements Critic and its label. They add focused tests and review each other's rules. M5 implements the view/CLI handoff with M4 reviewing affected snapshot/controller tests. M3 prepares the staff/payment regression checks against the agreed APIs.
3. After both subclasses are available, M4 registers the mixed arrival schedule and verifies all 18 turns; M3 runs the mixed-customer regressions. Run existing tests alongside the new ones; adjust assertions only where the intentionally changed arrival composition makes them obsolete.
4. Each member supplies and verifies their UML changes. M5 coordinates the diagram/export, records the two Stage 2 gameplay runs and updates the README. The team reviews the exact release candidate before tagging `stage-2`.

Use focused feature branches from the current `main`, owner-reviewed pull requests and the existing AI-prompt/test evidence templates. Keep the published `stage-1` tag fixed while developing Stage 2.

## Acceptance and evidence checklist

These are planned checks, not completed results:

- [ ] Exercise Regular, VIP and Critic rules through `Customer` references. Regular behaviour remains unchanged.
- [ ] Verify exact `Regular`, `VIP` and `Critic` labels through `Customer` references, immutable `CustomerView.type` snapshots and CLI customer listings, without concrete-type checks. Update affected constructors and test customer subclasses.
- [ ] Verify VIP quotes for all base-menu prices, zero, fractional-cent rounding and a large valid price without overflow; reject null input.
- [ ] Verify Critic choice with reordered/custom menus: a higher-preparation item wins even when cheaper; equal preparation units are resolved by higher price; equal preparation units and price retain the first menu item. Include an ID where Regular would choose differently, and verify full-price quotes and invalid-input handling.
- [ ] Verify that rule methods preserve menu/item identity and do not mutate shared menu state.
- [ ] Verify satisfaction loss only while WAITING or SEATED, clamping at zero and no loss after service. Cover abandonment, cancellation of READY-but-unserved food, no revenue on departure and table release.
- [ ] Verify exactly six arrivals at the required turns and IDs, including the agreed VIP/Critic placements, no arrivals on other turns, invalid-turn rejection and independent fresh instances.
- [ ] Verify staff substitution with the actual subtypes, a single shared Order instance, locked VIP prices and paid-only revenue/satisfaction totals.
- [ ] Preserve invalid-action retries, one task per staff member, Waiter-before-Chef ordering, later-turn service/eating/payment and oldest-first single payment.
- [ ] Compile with `./compile.sh --tests`, then execute each existing and new test runner separately. Record compiler/runtime versions and exact candidate SHA; compilation targeting Java 17 does not establish execution on a Java 17 runtime.
- [ ] Record a mixed-customer victory run and defeat run through all 18 turns in `docs/testing/stage-2-testscript/stage-2-*.txt` / `.out`, with exact observed results in `docs/testing/stage-2-tests.md`. Both new types must actually appear; the victory evidence should demonstrate their ordering/pricing differences.
- [ ] Review source to ensure subtype behaviour is polymorphic and Stage 3 features are absent.
- [ ] Add the Stage 2 Draw.io source/export under `docs/uml/stage-2-uml/` to match implemented Stage 2 classes. Preserve Stage 1 scenario files as historical evidence; Stage 2 outputs will differ intentionally.
- [ ] Complete the README transition record with actual new/changed/deleted files, reasons and relevant implementation commit or compare links.
- [ ] Review the candidate, verify it runs independently, then create and publish `stage-2` through the team's release process.

## Diagram completion after Stage 3

Maintain the Stage 2 class diagram for its release. Schedule the following final-submission diagram work after Stage 3 is complete; these are not Stage 2 release gates:

- [ ] Annotate the final class diagram with genuine examples of encapsulation, polymorphism and composition, as required by brief §5. Each member supplies the details for their classes, with M5 coordinating the Draw.io source and export.
- [ ] Choose a scenario, assign ownership and complete the sequence diagram after Stage 3. It must include a Stage 2 or Stage 3 behaviour; VIP pricing is a possible scenario, with the final choice deferred until then.

No Stage 2 implementation, gameplay test run, pull request or release tag is claimed by this design draft.
