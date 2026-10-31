package restaurantrush.domain;

import java.util.List;

/**
 * Owns the shared customer lifecycle and satisfaction, with overridable rules
 * for menu selection, quoted prices and waiting loss.
 *
 * <p>Private state and final lifecycle methods preserve a positive identity,
 * satisfaction from 0 to 100 and at most one associated {@link Order} instance.
 * Queue and table cleanup, statistics and staff timing belong to collaborators.
 */
public abstract class Customer {
    private final int id;
    private int satisfaction;
    private CustomerStatus status;
    private int servedTurn;
    private int readyToPayTurn;
    private Order order;

    /**
     * Creates a waiting customer with satisfaction 100, no order and both turn
     * timestamps set to {@code -1}.
     *
     * @param id positive customer identity, also required for an accepted order
     * @throws IllegalArgumentException if {@code id} is not positive
     */
    protected Customer(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive.");
        }
        this.id = id;
        this.satisfaction = 100;
        this.status = CustomerStatus.WAITING;
        this.servedTurn = -1;
        this.readyToPayTurn = -1;
        this.order = null;
    }

    /**
     * Returns a stable, non-null presentation label for this customer type.
     * The label must not be used to select gameplay behaviour.
     *
     * @return the stable, non-null customer-type presentation label
     */
    public abstract String typeLabel();

    /**
     * Supplies the customer-specific rule used by {@link #applyWaitingLoss()}.
     *
     * @return a nonnegative satisfaction loss per waiting turn
     */
    public abstract int waitingLossPerTurn();

    /**
     * Selects an existing offering from the supplied menu.
     *
     * @param menu non-null, nonempty menu
     * @return a non-null item contained in {@code menu}
     * @throws IllegalArgumentException if {@code menu} is null or empty
     */
    public abstract MenuItem chooseMenuItem(List<MenuItem> menu);

    /**
     * Supplies the customer-specific bill to lock into a new order.
     *
     * @param item non-null selected offering
     * @return a nonnegative integer-cent quote; zero is valid
     * @throws IllegalArgumentException if {@code item} is null
     */
    public abstract int quotePriceCents(MenuItem item);

    /**
     * Changes {@code WAITING} to {@code SEATED}.
     *
     * @throws IllegalStateException if the customer is not waiting
     */
    public final void seat() {
        if (status != CustomerStatus.WAITING) {
            throw new IllegalStateException("Only a waiting customer can be seated.");
        }
        status = CustomerStatus.SEATED;
    }

    /**
     * Associates the supplied order once, retaining its exact object identity.
     * The customer stays seated; this operation imposes no order-status guard.
     *
     * @param order non-null order whose ID matches this customer's ID
     * @throws IllegalArgumentException if {@code order} is null or its ID differs
     * @throws IllegalStateException if the customer is not seated or already has an order
     */
    public final void acceptOrder(Order order) {
        if (status != CustomerStatus.SEATED || this.order != null) {
            throw new IllegalStateException("A seated customer can accept only one order.");
        }
        if (order == null || order.getId() != id) {
            throw new IllegalArgumentException("Order must be non-null and match the customer ID.");
        }
        this.order = order;
    }

    /**
     * Changes a seated customer to {@code EATING} and records the service turn.
     * The associated order must already have been marked {@code SERVED}.
     *
     * @param turn positive service turn
     * @throws IllegalArgumentException if {@code turn} is not positive
     * @throws IllegalStateException if the customer is not seated or has no served order
     */
    public final void markServed(int turn) {
        requirePositiveTurn(turn);
        if (status != CustomerStatus.SEATED || order == null
                || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException("A seated customer's order must be served first.");
        }
        status = CustomerStatus.EATING;
        servedTurn = turn;
    }

    /**
     * Changes {@code EATING} to {@code READY_TO_PAY} only on a turn strictly
     * later than service, recording that readiness turn once.
     *
     * <p>For a positive turn, all other cases are no-ops, including the service
     * turn and calls after eating has finished; recorded timestamps stay unchanged.
     *
     * @param turn positive turn at which eating completion is considered
     * @throws IllegalArgumentException if {@code turn} is not positive
     */
    public final void finishEating(int turn) {
        requirePositiveTurn(turn);
        if (status == CustomerStatus.EATING && servedTurn < turn) {
            status = CustomerStatus.READY_TO_PAY;
            readyToPayTurn = turn;
        }
    }

    /**
     * While {@code WAITING} or {@code SEATED}, subtracts the polymorphic
     * {@link #waitingLossPerTurn()} with a floor of zero. Other states are no-ops.
     * Reaching zero does not automatically abandon or cancel an order.
     *
     * @throws IllegalStateException if an applicable waiting-loss rule returns a negative value
     */
    public final void applyWaitingLoss() {
        if (status == CustomerStatus.WAITING || status == CustomerStatus.SEATED) {
            int loss = waitingLossPerTurn();
            if (loss < 0) {
                throw new IllegalStateException("Waiting loss must be nonnegative.");
            }
            satisfaction = Math.max(0, satisfaction - loss);
        }
    }

    /**
     * Changes {@code READY_TO_PAY} to {@code PAID} after the associated order
     * has been marked {@code PAID}. Collaborators decide payment eligibility timing.
     *
     * @throws IllegalStateException if the customer is not payment-ready or has no paid order
     */
    public final void markPaid() {
        if (status != CustomerStatus.READY_TO_PAY || order == null
                || order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("A payment-ready customer's order must be paid first.");
        }
        status = CustomerStatus.PAID;
    }

    /**
     * Changes {@code WAITING} or {@code SEATED} to {@code LEFT}, cancelling an
     * attached {@code PLACED}, {@code PREPARING} or {@code READY} order first.
     * Other attached order states remain unchanged, and the association is retained.
     *
     * <p>No zero-satisfaction condition is imposed here; Restaurant coordinates
     * automatic-abandonment eligibility and queue or table cleanup.
     *
     * @throws IllegalStateException if the customer is neither waiting nor seated
     */
    public final void abandon() {
        if (status != CustomerStatus.WAITING && status != CustomerStatus.SEATED) {
            throw new IllegalStateException("Only a waiting or seated customer can abandon.");
        }
        if (order != null) {
            OrderStatus orderStatus = order.getStatus();
            if (orderStatus == OrderStatus.PLACED || orderStatus == OrderStatus.PREPARING
                    || orderStatus == OrderStatus.READY) {
                order.cancel();
            }
        }
        status = CustomerStatus.LEFT;
    }

    /**
     * Returns the customer's stable identity.
     *
     * @return the immutable positive customer identity
     */
    public final int getId() {
        return id;
    }

    /**
     * Reads satisfaction without changing the lifecycle.
     *
     * @return current satisfaction, from 0 to 100 inclusive
     */
    public final int getSatisfaction() {
        return satisfaction;
    }

    /**
     * Reads the customer's lifecycle state.
     *
     * @return the current non-null customer lifecycle state
     */
    public final CustomerStatus getStatus() {
        return status;
    }

    /**
     * Reads when this customer was served.
     *
     * @return the recorded service turn, or {@code -1} before service
     */
    public final int getServedTurn() {
        return servedTurn;
    }

    /**
     * Reads when this customer finished eating.
     *
     * @return the recorded readiness turn, or {@code -1} before eating finishes
     */
    public final int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    /**
     * Returns the actual shared association, including after payment or abandonment.
     *
     * @return the accepted order instance, or {@code null} before order assignment
     */
    public final Order getOrder() {
        return order;
    }

    private static void requirePositiveTurn(int turn) {
        if (turn <= 0) {
            throw new IllegalArgumentException("Turn must be positive.");
        }
    }
}
