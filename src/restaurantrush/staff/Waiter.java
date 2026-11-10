package restaurantrush.staff;

import restaurantrush.domain.ActionResult;
import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.HappyHour;
import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;
import restaurantrush.domain.Restaurant;

/**
 * Seats customers, takes their orders and serves prepared dishes.
 *
 * <p>Apart from the shared Happy Hour policy it holds no state, and works only through the {@link Customer}
 * abstraction: the dish and the quoted price come from the customer's own
 * polymorphic rules, so no customer subtype is ever checked here. Every action
 * checks legality first and returns a reason on failure, leaving state unchanged.
 */
public final class Waiter {
    private static final int FIRST_TURN = 1;
    private static final int LAST_TURN = 18;

    private final HappyHour happyHour;

    /**
     * Creates a Waiter that applies the shared Happy Hour policy to every order it takes.
     *
     * @throws IllegalArgumentException if the policy is null
     */
    public Waiter(HappyHour happyHour) {
        if (happyHour == null) {
            throw new IllegalArgumentException("Happy Hour policy must be non-null.");
        }
        this.happyHour = happyHour;
    }

    /**
     * Creates a Waiter with its own never-activated policy, so orders keep the
     * customer's quote. Kept for callers not yet passing the shared policy.
     */
    public Waiter() {
        this(new HappyHour());
    }

    /** Seats a queued customer at a free table. */
    public ActionResult seat(Restaurant restaurant, int customerId, int tableId) {
        if (restaurant == null) {
            return ActionResult.fail("No restaurant is available.");
        }
        return restaurant.seat(customerId, tableId);
    }

    /**
     * Takes the order of a seated customer on the current turn using the quote the
     * customer gives, with no turn-dependent discount. Prefer
     * {@link #takeOrder(Restaurant, int, int)}.
     */
    public ActionResult takeOrder(Restaurant restaurant, int customerId) {
        return takeOrder(restaurant, customerId, FIRST_TURN);
    }

    /**
     * Takes the order of a seated customer who has not ordered yet. The customer
     * chooses the dish and quotes its price; Happy Hour then discounts that quote
     * when active on {@code turn}. The result is the bill locked into the order.
     */
    public ActionResult takeOrder(Restaurant restaurant, int customerId, int turn) {
        if (restaurant == null) {
            return ActionResult.fail("No restaurant is available.");
        }
        if (turn < FIRST_TURN || turn > LAST_TURN) {
            return ActionResult.fail("Turn must be between " + FIRST_TURN + " and " + LAST_TURN + ".");
        }
        Customer customer = restaurant.findCustomer(customerId);
        if (customer == null) {
            return ActionResult.fail("There is no customer " + customerId + ".");
        }
        if (customer.getStatus() != CustomerStatus.SEATED) {
            return ActionResult.fail("Customer " + customerId + " must be seated before ordering ("
                    + customer.getStatus() + ").");
        }
        if (customer.getOrder() != null) {
            return ActionResult.fail("Customer " + customerId + " has already ordered.");
        }
        MenuItem item = customer.chooseMenuItem(restaurant.getMenu());
        int bill = happyHour.quotePriceCents(customer.quotePriceCents(item), turn);
        return restaurant.registerOrder(customerId, new Order(customerId, item, bill));
    }

    /** Hands a READY dish to its seated customer. */
    public ActionResult serve(Restaurant restaurant, int customerId, int turn) {
        if (restaurant == null) {
            return ActionResult.fail("No restaurant is available.");
        }
        if (turn <= 0) {
            return ActionResult.fail("Turn must be positive.");
        }
        return restaurant.serve(customerId, turn);
    }
}
