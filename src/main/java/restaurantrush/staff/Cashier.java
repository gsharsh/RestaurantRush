package restaurantrush.staff;

import restaurantrush.domain.Customer;
import restaurantrush.domain.Restaurant;

/**
 * Collects payment automatically, at most one customer per turn, always the
 * customer who has been ready to pay the longest.
 */
public final class Cashier {
    private int lastPaidTurn;

    public Cashier() {
        this.lastPaidTurn = -1;
    }

    /**
     * Charges the oldest eligible READY_TO_PAY customer.
     *
     * @return true if a payment succeeded; false if one already succeeded this
     *         turn or nobody is eligible
     * @throws IllegalArgumentException if the restaurant is null or the turn is not positive
     */
    public boolean collectOne(Restaurant restaurant, int turn) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant must be non-null.");
        }
        if (turn <= 0) {
            throw new IllegalArgumentException("Turn must be positive.");
        }
        if (lastPaidTurn == turn) {
            return false;
        }
        Customer payer = restaurant.oldestEligiblePayer(turn);
        if (payer == null || !restaurant.recordPayment(payer, turn)) {
            return false;
        }
        lastPaidTurn = turn;
        return true;
    }
}
