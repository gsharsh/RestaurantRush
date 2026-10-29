package restaurantrush.domain;

import java.util.List;

/**
 * Supplies Stage 1's deterministic menu choice, full-price quote and eight-point
 * waiting loss while reusing the lifecycle implemented by {@link Customer}.
 * Adds no state of its own; callers can invoke these rules through a Customer reference.
 */
public final class RegularCustomer extends Customer {
    /**
     * Delegates identity validation and lifecycle initialization to {@code Customer}.
     *
     * @param id positive customer identity
     * @throws IllegalArgumentException if {@code id} is not positive
     */
    public RegularCustomer(int id) {
        super(id);
    }

    /**
     * @return the Stage 1 loss of 8 satisfaction points per waiting turn
     */
    @Override
    public int waitingLossPerTurn() {
        return 8;
    }

    /**
     * Returns {@code menu.get((getId() - 1) % menu.size())} without changing the
     * menu or copying the selected offering. Unused null entries are not inspected.
     *
     * @param menu non-null, nonempty menu
     * @return the existing non-null item at the deterministic selection index
     * @throws IllegalArgumentException if the menu is null or empty, or the selected item is null
     */
    @Override
    public MenuItem chooseMenuItem(List<MenuItem> menu) {
        if (menu == null || menu.isEmpty()) {
            throw new IllegalArgumentException("Menu must be non-null and nonempty.");
        }
        MenuItem selected = menu.get((getId() - 1) % menu.size());
        if (selected == null) {
            throw new IllegalArgumentException("The selected menu item must be non-null.");
        }
        return selected;
    }

    /**
     * Quotes the offering's full integer-cent base price.
     *
     * @param item non-null selected offering
     * @return {@code item.getPriceCents()}, including a valid zero price
     * @throws IllegalArgumentException if {@code item} is null
     */
    @Override
    public int quotePriceCents(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item must be non-null.");
        }
        return item.getPriceCents();
    }
}
