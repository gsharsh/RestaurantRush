package restaurantrush.domain;

/**
 * An offering whose validated state is immutable through the agreed API.
 */
public class MenuItem {
    private final String name;
    private final int priceCents;
    private final int preparationUnits;

    public MenuItem(String name, int priceCents, int preparationUnits) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must be non-null and nonblank.");
        }
        if (priceCents < 0) {
            throw new IllegalArgumentException("Price must be nonnegative.");
        }
        if (preparationUnits <= 0) {
            throw new IllegalArgumentException("Preparation units must be positive.");
        }

        this.name = name;
        this.priceCents = priceCents;
        this.preparationUnits = preparationUnits;
    }

    public final String getName() {
        return name;
    }

    public final int getPriceCents() {
        return priceCents;
    }

    public final int getPreparationUnits() {
        return preparationUnits;
    }
}
