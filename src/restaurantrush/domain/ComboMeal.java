package restaurantrush.domain;

/**
 * A menu offering that retains two shared components, sums their preparation
 * units and charges 90% of their combined price, rounded half up to a cent.
 * Name, price and preparation units use the inherited {@link MenuItem} accessors.
 */
public final class ComboMeal extends MenuItem {
    private final MenuItem first;
    private final MenuItem second;

    /**
     * Creates a combo using overflow-safe integer arithmetic. The discounted
     * price and summed preparation units must each fit in an {@code int}; the
     * undiscounted combined price may exceed that range.
     *
     * @param name display name, validated and preserved by {@link MenuItem}
     * @param first non-null first component, retained unchanged
     * @param second non-null second component, retained unchanged
     * @throws IllegalArgumentException if the name is null or blank, either
     *         component is null, or a computed result exceeds {@link Integer#MAX_VALUE}
     */
    public ComboMeal(String name, MenuItem first, MenuItem second) {
        super(name, combinedPriceCents(first, second), combinedPreparationUnits(first, second));
        this.first = first;
        this.second = second;
    }

    /**
     * @return the original first component instance
     */
    public MenuItem getFirst() {
        return first;
    }

    /**
     * @return the original second component instance
     */
    public MenuItem getSecond() {
        return second;
    }

    private static int combinedPriceCents(MenuItem first, MenuItem second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Combo components must be non-null.");
        }
        long priceCents = (9L * ((long) first.getPriceCents() + second.getPriceCents()) + 5L) / 10L;
        if (priceCents > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Combo price exceeds the integer range.");
        }
        return (int) priceCents;
    }

    private static int combinedPreparationUnits(MenuItem first, MenuItem second) {
        long preparationUnits = (long) first.getPreparationUnits() + second.getPreparationUnits();
        if (preparationUnits > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Combo preparation units exceed the integer range.");
        }
        return (int) preparationUnits;
    }
}
