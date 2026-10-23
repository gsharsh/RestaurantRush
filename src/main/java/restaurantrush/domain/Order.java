package restaurantrush.domain;

/** Owns preparation and lifecycle for an order of a shared immutable menu item. */
public final class Order {
    private final int id;
    private final MenuItem item;
    private OrderStatus status;
    private int remainingUnits;
    private final int billCents;

    /**
     * Locks the supplied quote; the Stage 1 caller supplies the customer's quote.
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

    public boolean canPrepare() {
        return (status == OrderStatus.PLACED || status == OrderStatus.PREPARING)
                && remainingUnits > 0;
    }

    public void prepareOneUnit() {
        if (!canPrepare()) {
            throw new IllegalStateException("Order cannot be prepared in its current state.");
        }
        remainingUnits--;
        status = remainingUnits == 0 ? OrderStatus.READY : OrderStatus.PREPARING;
    }

    public void markServed() {
        if (status != OrderStatus.READY) {
            throw new IllegalStateException("Only a ready order can be served.");
        }
        status = OrderStatus.SERVED;
    }

    public void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Only a served order can be paid.");
        }
        status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status != OrderStatus.PLACED && status != OrderStatus.PREPARING
                && status != OrderStatus.READY) {
            throw new IllegalStateException("Only an unserved order can be cancelled.");
        }
        status = OrderStatus.CANCELLED;
    }

    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    public int getId() {
        return id;
    }

    public MenuItem getItem() {
        return item;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getRemainingUnits() {
        return remainingUnits;
    }

    public int getBillCents() {
        return billCents;
    }
}
