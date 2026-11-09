package restaurantrush.staff;

import restaurantrush.domain.ActionResult;
import restaurantrush.domain.Restaurant;

/**
 * Seats queued customers at free tables, alongside the Waiter's own seating.
 * Stateless: the turn's single Host task is tracked by the turn manager, not here.
 */
public final class Host {

    public Host() {
    }

    /** Seats a queued customer at a free table, or explains why that is not possible. */
    public ActionResult seat(Restaurant restaurant, int customerId, int tableId) {
        if (restaurant == null) {
            return ActionResult.fail("No restaurant is available.");
        }
        return restaurant.seat(customerId, tableId);
    }
}
