package restaurantrush.domain;

import java.util.List;

public final class CriticCustomer extends Customer {

    public CriticCustomer(int id) {
        super(id);
    }

    public String typeLabel() {
        return "Critic";
    }

    @Override
    public int waitingLossPerTurn() {
        return 12;
    }

    @Override
    public MenuItem chooseMenuItem(List<MenuItem> menu) {
        if (menu == null || menu.isEmpty()) {
            throw new IllegalArgumentException("Menu must be non-null and nonempty.");
        }
        for (MenuItem item : menu) {
            if (item == null) {
                throw new IllegalArgumentException("Menu must not contain null items.");
            }
        }
        MenuItem preferred = menu.get(0);
        for (MenuItem item : menu) {
            if (isPreferredOver(item, preferred)) {
                preferred = item;
            }
        }
        return preferred;
    }

    @Override
    public int quotePriceCents(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item must be non-null.");
        }
        return item.getPriceCents();
    }

    private static boolean isPreferredOver(MenuItem candidate, MenuItem current) {
        if (candidate.getPreparationUnits() != current.getPreparationUnits()) {
            return candidate.getPreparationUnits() > current.getPreparationUnits();
        }
        return candidate.getPriceCents() > current.getPriceCents();
    }
}
