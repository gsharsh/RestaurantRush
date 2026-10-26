# AI prompt record

- **Branch:** `feature/devansh-s1-class-diagram`
- **Member:** Devansh Poddar (M5)
- **Stage:** Stage 1
- **Related pull request:** To be added

## Prompt 1

**Prompt**

> Generate the Stage 1 class diagram (docs/uml/class-diagram.drawio) from the merged Stage 1 code.

**What was used or rejected**

The assistant (Claude) read the merged classes on `main` at `f21512f` and generated an editable Draw.io file: 20 classes grouped by package, private fields and public operations with visibility, static members underlined, abstract members in italics, the `Customer`/`RegularCustomer` generalisation, associations with multiplicities, composition (Restaurant owns Tables and MenuItems; GameSnapshot owns its views), aggregation (Restaurant keeps Customers and Orders), «create»/«use» dependencies, and three notes explaining encapsulation, polymorphism and composition. Private helper methods (formatting and null-check helpers) were left out to keep the diagram readable. An early layout routed several lines through other classes; these were rerouted around the classes.

**Files affected**

- `docs/uml/class-diagram.drawio`

**Human verification**

Opened in Draw.io and checked each class's fields, operations and relationships against the code; owners M1–M4 asked to confirm their classes.

**Related commits**

- To be added after commit
