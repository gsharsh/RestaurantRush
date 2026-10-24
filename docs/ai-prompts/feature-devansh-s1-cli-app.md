# AI prompt record

- **Branch:** `feature/devansh-s1-cli-app`
- **Member:** Devansh Poddar (M5)
- **Stage:** Stage 1
- **Related pull request:** To be added

## Prompt 1

**Prompt**

> Write the M5 classes (RestaurantRushApp, RestaurantCLI, GameSnapshot, CustomerView, TableView) exactly to the Stage 1 class contract, against Harshith's M1 domain types and Vedavit's Table.

**What was used or rejected**

The assistant (Claude) drafted the three immutable view records with compact-constructor validation and `List.copyOf` defensive copies, the CLI with phase-labelled Waiter/Chef prompts that follow the command wording in PR #3, and the app entry point that builds one shared object graph. Null arguments throw `IllegalArgumentException` to match the contract's failure conventions. A first draft labelled the Chef as "used" at the start of each turn; this was changed to "available after the Waiter" because `chefAvailable` is false until the Waiter acts.

**Files affected**

- `src/main/java/restaurantrush/app/RestaurantRushApp.java`
- `src/main/java/restaurantrush/boundary/RestaurantCLI.java`
- `src/main/java/restaurantrush/view/GameSnapshot.java`
- `src/main/java/restaurantrush/view/CustomerView.java`
- `src/main/java/restaurantrush/view/TableView.java`
- `src/test/java/restaurantrush/view/ViewRecordsTest.java`

**Human verification**

View records compiled against the M1 branch and `Table`, and `ViewRecordsTest` passed (46 checks). The CLI and app were compiled and smoke-run against temporary stand-ins for M3/M4 classes that were not committed; they must be re-verified once `GameController`, `TurnManager`, `ArrivalSchedule` and the staff classes are merged.

**Related commits**

- To be added after commit
