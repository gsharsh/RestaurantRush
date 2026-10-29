package restaurantrush.staff;

import restaurantrush.domain.ActionResult;
import restaurantrush.domain.Order;

/**
 * Prepares one preparation unit of one order per action. Stateless; the turn's
 * single Chef choice is tracked by the turn manager, not here.
 */
public final class Chef {

    public Chef() {
    }

    /** Cooks one unit of the order, or explains why it cannot be cooked. */
    public ActionResult prepareOneUnit(Order order) {
        if (order == null) {
            return ActionResult.fail("There is no order to prepare.");
        }
        if (!order.canPrepare()) {
            return ActionResult.fail("The " + order.getItem().getName() + " order is "
                    + order.getStatus() + " and cannot be prepared.");
        }
        order.prepareOneUnit();
        String dish = order.getItem().getName();
        if (order.isReady()) {
            return ActionResult.ok(dish + " is READY to serve.");
        }
        return ActionResult.ok("Prepared one unit of " + dish + "; "
                + order.getRemainingUnits() + " unit(s) left.");
    }
}
