package restaurantrush.domain;

/**
 * A shared menu offering with a display name, integer-cent price and preparation
 * requirement. Validated private final state and final accessors keep these values
 * immutable through the agreed API; there are no setters.
 */
public class MenuItem {
    private final String name;
    private final int priceCents;
    private final int preparationUnits;

    /**
     * Creates an offering, preserving a valid display name exactly as supplied.
     *
     * @param name non-null, nonblank display name; no trimming or normalization is applied
     * @param priceCents nonnegative base price in cents; zero is valid
     * @param preparationUnits positive number of Chef actions required
     * @throws IllegalArgumentException if the name is null or blank, the price is negative,
     *         or preparation units are not positive
     */
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

    /**
     * Returns the offering's display name.
     *
     * @return the supplied nonblank display name, unchanged
     */
    public final String getName() {
        return name;
    }

    /**
     * Reads the offering's base price.
     *
     * @return the immutable nonnegative base price in integer cents
     */
    public final int getPriceCents() {
        return priceCents;
    }

    /**
     * Reads the offering's preparation requirement.
     *
     * @return the immutable positive number of required preparation actions
     */
    public final int getPreparationUnits() {
        return preparationUnits;
    }
}
