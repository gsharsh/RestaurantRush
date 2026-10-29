package restaurantrush.view;

import restaurantrush.domain.Customer;
import restaurantrush.domain.Table;

/**
 * Read-only copy of one table for display.
 *
 * <p>Holds only primitive values, so a snapshot that contains it cannot be
 * changed by later moves in the game.
 *
 * @param id         positive table ID
 * @param occupantId seated customer's ID, or {@code null} while the table is free
 */
public record TableView(int id, Integer occupantId) {

    public TableView {
        if (id <= 0) {
            throw new IllegalArgumentException("Table ID must be positive: " + id);
        }
        if (occupantId != null && occupantId <= 0) {
            throw new IllegalArgumentException("Occupant ID must be positive: " + occupantId);
        }
    }

    /** Copies the current state of a table. */
    public static TableView from(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table must not be null");
        }
        Customer occupant = table.getOccupant();
        return new TableView(table.getId(), occupant == null ? null : occupant.getId());
    }
}
