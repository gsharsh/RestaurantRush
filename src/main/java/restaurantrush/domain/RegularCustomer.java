package restaurantrush.domain;

import java.util.List;

/** The Stage 1 rules applied through the shared Customer lifecycle. */
public final class RegularCustomer extends Customer {
    public RegularCustomer(int id) {
        super(id);
    }

    @Override
    public int waitingLossPerTurn() {
        return 8;
    }

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

    @Override
    public int quotePriceCents(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item must be non-null.");
        }
        return item.getPriceCents();
    }
}
