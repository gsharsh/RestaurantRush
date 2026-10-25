package restaurantrush.staff;

import restaurantrush.domain.ActionResult;
import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;
import restaurantrush.domain.Restaurant;

/**
 * Seats customers, takes their orders and serves prepared dishes.
 *
 * <p>The Waiter is stateless and works only through the {@link Customer}
 * abstraction: the dish and the quoted price come from the customer's own
 * polymorphic rules, so no customer subtype is ever checked here. Every action
 * checks legality first and returns a reason on failure, leaving state unchanged.
 */
public final class Waiter {

    public Waiter() {
    }

    /** Seats a queued customer at a free table. */
    public ActionResult seat(Restaurant restaurant, int customerId, int tableId) {
        if (restaurant == null) {
            return ActionResult.fail("No restaurant is available.");
        }
        return restaurant.seat(customerId, tableId);
    }

    /**
     * Takes the order of a seated customer who has not ordered yet. The customer
     * chooses the dish and quotes the price that gets locked into the order.
     */
    public ActionResult takeOrder(Restaurant restaurant, int customerId) {
        if (restaurant == null) {
            return ActionResult.fail("No restaurant is available.");
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
        Order order = new Order(customerId, item, customer.quotePriceCents(item));
        return restaurant.registerOrder(customerId, order);
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
