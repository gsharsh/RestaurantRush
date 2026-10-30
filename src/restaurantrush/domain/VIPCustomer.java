package restaurantrush.domain;

import java.util.List;

/**
 * Supplies VIP presentation, deterministic menu choice, discounted quotes and
 * five-point waiting loss while reusing the lifecycle implemented by {@link Customer}.
 * Adds no state of its own; callers can invoke these rules through a Customer reference.
 */
public final class VIPCustomer extends Customer {
    /**
     * Delegates identity validation and lifecycle initialization to {@code Customer}.
     *
     * @param id positive customer identity
     * @throws IllegalArgumentException if {@code id} is not positive
     */
    public VIPCustomer(int id) {
        super(id);
    }

    /**
     * @return the stable presentation label {@code "VIP"}
     */
    @Override
    public String typeLabel() {
        return "VIP";
    }

    /**
     * @return the VIP loss of 5 satisfaction points per waiting turn
     */
    @Override
    public int waitingLossPerTurn() {
        return 5;
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
     * Quotes the offering's integer-cent base price with a ten-percent discount.
     *
     * <p>For fractional-cent totals, the Stage 2 contract's proposed nearest-cent,
     * half-up policy is a working assumption pending team review. The discounted
     * total is rounded once, using long arithmetic to avoid overflow.
     *
     * @param item non-null selected offering
     * @return the discounted total in integer cents, including a valid zero price
     * @throws IllegalArgumentException if {@code item} is null
     */
    @Override
    public int quotePriceCents(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item must be non-null.");
        }
        return (int) ((9L * item.getPriceCents() + 5L) / 10L);
    }
}
