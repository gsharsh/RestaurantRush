package restaurantrush.domain;

import java.util.List;

/** Shared lifecycle with customer-specific selection, pricing and waiting rules. */
public abstract class Customer {
    private final int id;
    private int satisfaction;
    private CustomerStatus status;
    private int servedTurn;
    private int readyToPayTurn;
    private Order order;

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

    public abstract int waitingLossPerTurn();

    public abstract MenuItem chooseMenuItem(List<MenuItem> menu);

    public abstract int quotePriceCents(MenuItem item);

    public final void seat() {
        if (status != CustomerStatus.WAITING) {
            throw new IllegalStateException("Only a waiting customer can be seated.");
        }
        status = CustomerStatus.SEATED;
    }

    public final void acceptOrder(Order order) {
        if (status != CustomerStatus.SEATED || this.order != null) {
            throw new IllegalStateException("A seated customer can accept only one order.");
        }
        if (order == null || order.getId() != id) {
            throw new IllegalArgumentException("Order must be non-null and match the customer ID.");
        }
        this.order = order;
    }

    public final void markServed(int turn) {
        requirePositiveTurn(turn);
        if (status != CustomerStatus.SEATED || order == null
                || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException("A seated customer's order must be served first.");
        }
        status = CustomerStatus.EATING;
        servedTurn = turn;
    }

    public final void finishEating(int turn) {
        requirePositiveTurn(turn);
        if (status == CustomerStatus.EATING && servedTurn < turn) {
            status = CustomerStatus.READY_TO_PAY;
            readyToPayTurn = turn;
        }
    }

    public final void applyWaitingLoss() {
        if (status == CustomerStatus.WAITING || status == CustomerStatus.SEATED) {
            int loss = waitingLossPerTurn();
            if (loss < 0) {
                throw new IllegalStateException("Waiting loss must be nonnegative.");
            }
            satisfaction = Math.max(0, satisfaction - loss);
        }
    }

    public final void markPaid() {
        if (status != CustomerStatus.READY_TO_PAY || order == null
                || order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("A payment-ready customer's order must be paid first.");
        }
        status = CustomerStatus.PAID;
    }

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

    public final int getId() {
        return id;
    }

    public final int getSatisfaction() {
        return satisfaction;
    }

    public final CustomerStatus getStatus() {
        return status;
    }

    public final int getServedTurn() {
        return servedTurn;
    }

    public final int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    public final Order getOrder() {
        return order;
    }

    private static void requirePositiveTurn(int turn) {
        if (turn <= 0) {
            throw new IllegalArgumentException("Turn must be positive.");
        }
    }
}
