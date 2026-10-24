package restaurantrush.view;

import java.util.List;

/**
 * Read-only picture of the whole game at one moment, built by
 * {@code GameController.getSnapshot()} and shown by the CLI.
 *
 * <p>Every list is copied with {@link List#copyOf}, so neither the caller nor
 * later turns can change a snapshot once it exists.
 *
 * @param turn                    current or last turn, 1 to 18
 * @param completedTurns          turns fully finished, 0 to 18
 * @param revenueCents            revenue from successful payments, in cents
 * @param averagePaidSatisfaction average satisfaction recorded at payment, or
 *                                {@code null} before anyone has paid
 * @param waitingCustomerIds      queued customer IDs in arrival order
 * @param customers               customers to display
 * @param tables                  tables in ID order
 * @param waiterAvailable         whether the Waiter choice is still open this turn
 * @param chefAvailable           whether the Chef choice is still open this turn
 * @param paidCount               customers who have paid
 * @param servedCount             dishes served
 * @param abandonedCount          customers who left unhappy
 */
public record GameSnapshot(
        int turn,
        int completedTurns,
        int revenueCents,
        Double averagePaidSatisfaction,
        List<Integer> waitingCustomerIds,
        List<CustomerView> customers,
        List<TableView> tables,
        boolean waiterAvailable,
        boolean chefAvailable,
        int paidCount,
        int servedCount,
        int abandonedCount) {

    /** Turns in the base game. */
    private static final int FINAL_TURN = 18;

    public GameSnapshot {
        if (turn < 1 || turn > FINAL_TURN) {
            throw new IllegalArgumentException("Turn must be 1 to " + FINAL_TURN + ": " + turn);
        }
        if (completedTurns < 0 || completedTurns > FINAL_TURN) {
            throw new IllegalArgumentException(
                    "Completed turns must be 0 to " + FINAL_TURN + ": " + completedTurns);
        }
        if (revenueCents < 0) {
            throw new IllegalArgumentException("Revenue must not be negative: " + revenueCents);
        }
        if (averagePaidSatisfaction != null
                && (averagePaidSatisfaction.isNaN() || averagePaidSatisfaction < 0
                        || averagePaidSatisfaction > 100)) {
            throw new IllegalArgumentException(
                    "Average paid satisfaction must be 0 to 100: " + averagePaidSatisfaction);
        }
        if (paidCount < 0 || servedCount < 0 || abandonedCount < 0) {
            throw new IllegalArgumentException("Counts must not be negative");
        }
        if ((averagePaidSatisfaction == null) != (paidCount == 0)) {
            throw new IllegalArgumentException(
                    "Average paid satisfaction must be null exactly when no customer has paid");
        }
        waitingCustomerIds = List.copyOf(required(waitingCustomerIds, "waitingCustomerIds"));
        customers = List.copyOf(required(customers, "customers"));
        tables = List.copyOf(required(tables, "tables"));
    }

    private static <T> T required(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return value;
    }
}
