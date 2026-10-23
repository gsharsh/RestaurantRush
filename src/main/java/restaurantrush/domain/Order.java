package restaurantrush.domain;

/**
 * Owns preparation progress and order-status transitions for a shared {@link MenuItem}.
 * The positive ID, exact item reference and nonnegative quoted bill are fixed at
 * construction; remaining preparation units never become negative.
 *
 * <p>Lifecycle operations reject invalid transitions, including repeated service,
 * payment and cancellation, before mutation.
 * Customer eating, payment readiness and wider restaurant coordination are separate.
 */
public final class Order {
    private final int id;
    private final MenuItem item;
    private OrderStatus status;
    private int remainingUnits;
    private final int billCents;

    /**
     * Creates a {@code PLACED} order with all of the item's preparation units and
     * locks the supplied bill without recomputing it from the item's base price.
     * The Stage 1 caller supplies exactly {@code customer.quotePriceCents(item)}.
     *
     * @param id positive order identity, matching its customer at association time
     * @param item non-null shared offering with positive preparation units
     * @param lockedBillCents nonnegative quoted bill in cents; zero is valid
     * @throws IllegalArgumentException if the ID or item units are not positive,
     *         the item is null, or the quoted bill is negative
     */
    public Order(int id, MenuItem item, int lockedBillCents) {
        if (id <= 0) {
            throw new IllegalArgumentException("Order ID must be positive.");
        }
        if (item == null) {
            throw new IllegalArgumentException("Menu item must be non-null.");
        }
        int preparationUnits = item.getPreparationUnits();
        if (preparationUnits <= 0) {
            throw new IllegalArgumentException("Preparation units must be positive.");
        }
        if (lockedBillCents < 0) {
            throw new IllegalArgumentException("Locked bill must be nonnegative.");
        }

        this.id = id;
        this.item = item;
        this.status = OrderStatus.PLACED;
        this.remainingUnits = preparationUnits;
        this.billCents = lockedBillCents;
    }

    /**
     * Reports whether one preparation action is currently allowed.
     *
     * @return true only for {@code PLACED} or {@code PREPARING} with positive remaining units
     */
    public boolean canPrepare() {
        return (status == OrderStatus.PLACED || status == OrderStatus.PREPARING)
                && remainingUnits > 0;
    }

    /**
     * Consumes exactly one preparation unit, becoming {@code READY} at zero or
     * {@code PREPARING} otherwise. A one-unit order goes directly from PLACED to READY.
     *
     * @throws IllegalStateException if {@link #canPrepare()} is false
     */
    public void prepareOneUnit() {
        if (!canPrepare()) {
            throw new IllegalStateException("Order cannot be prepared in its current state.");
        }
        remainingUnits--;
        status = remainingUnits == 0 ? OrderStatus.READY : OrderStatus.PREPARING;
    }

    /**
     * Changes {@code READY} to {@code SERVED}; customer service is coordinated separately.
     *
     * @throws IllegalStateException if the order is not ready
     */
    public void markServed() {
        if (status != OrderStatus.READY) {
            throw new IllegalStateException("Only a ready order can be served.");
        }
        status = OrderStatus.SERVED;
    }

    /**
     * Changes {@code SERVED} to terminal {@code PAID} without changing the locked bill.
     *
     * @throws IllegalStateException if the order is not served, including repeated payment
     */
    public void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Only a served order can be paid.");
        }
        status = OrderStatus.PAID;
    }

    /**
     * Changes {@code PLACED}, {@code PREPARING} or {@code READY} to terminal
     * {@code CANCELLED}, retaining the item, remaining units and locked bill.
     *
     * @throws IllegalStateException if the order is served, paid or already cancelled
     */
    public void cancel() {
        if (status != OrderStatus.PLACED && status != OrderStatus.PREPARING
                && status != OrderStatus.READY) {
            throw new IllegalStateException("Only an unserved order can be cancelled.");
        }
        status = OrderStatus.CANCELLED;
    }

    /**
     * Reports whether the prepared dish is awaiting service.
     *
     * @return true only when status is exactly {@code READY}; zero units alone is insufficient
     */
    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    /**
     * Returns the order's stable identity.
     *
     * @return the immutable positive order identity
     */
    public int getId() {
        return id;
    }

    /**
     * Exposes the shared offering without copying it.
     *
     * @return the same non-null immutable offering reference supplied at construction
     */
    public MenuItem getItem() {
        return item;
    }

    /**
     * Reads the order's lifecycle state.
     *
     * @return the current non-null order lifecycle state
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Reads the preparation work still required.
     *
     * @return the current nonnegative number of preparation units still required
     */
    public int getRemainingUnits() {
        return remainingUnits;
    }

    /**
     * Reads the quote locked when this order was created.
     *
     * @return the immutable quoted bill in integer cents
     */
    public int getBillCents() {
        return billCents;
    }
}
