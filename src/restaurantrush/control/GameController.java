package restaurantrush.control;

import restaurantrush.domain.ActionResult;
import restaurantrush.domain.Restaurant;
import restaurantrush.staff.Host;
import restaurantrush.staff.Waiter;
import restaurantrush.staff.Chef;
import restaurantrush.view.GameSnapshot;
import restaurantrush.view.CustomerView;
import restaurantrush.view.TableView;
import java.util.List;

/**
 * Coordinates game flow and delegates to domain specialists.
 *
 * OOP principles applied:
 * - Encapsulation: Private fields, controlled public interface
 * - Composition: Has-a relationships with dependencies (Restaurant, TurnManager, Host, Waiter, Chef)
 * - Delegation: Forwards domain operations to specialists, doesn't duplicate logic
 * - Single Responsibility: Orchestrates flow, validates staff availability, not domain rules
 * - Dependency Injection: All collaborators provided via constructor
 * - Immutability: Final references to collaborators
 * - Fail-safe: Invalid actions return results without state mutation
 */
public final class GameController {
    private final Restaurant restaurant;
    private final TurnManager turnManager;
    private final Host host;
    private final Waiter waiter;
    private final Chef chef;

    /**
     * Construct the game controller.
     *
     * @param restaurant non-null shared domain state
     * @param turnManager non-null sole turn-phase authority
     * @param host non-null host service
     * @param waiter non-null waiter service
     * @param chef non-null chef service
     * @throws IllegalArgumentException if any parameter is null
     */
    public GameController(Restaurant restaurant, TurnManager turnManager, Host host, Waiter waiter, Chef chef) {
        if (restaurant == null) {
            throw new IllegalArgumentException("restaurant must not be null");
        }
        if (turnManager == null) {
            throw new IllegalArgumentException("turnManager must not be null");
        }
        if (host == null) {
            throw new IllegalArgumentException("host must not be null");
        }
        if (waiter == null) {
            throw new IllegalArgumentException("waiter must not be null");
        }
        if (chef == null) {
            throw new IllegalArgumentException("chef must not be null");
        }
        this.restaurant = restaurant;
        this.turnManager = turnManager;
        this.host = host;
        this.waiter = waiter;
        this.chef = chef;
    }

    /**
     * Start a new turn by delegating to TurnManager with Happy Hour activation decision.
     * Must be called before CLI displays the turn.
     *
     * @param activateHappyHour true to attempt Happy Hour activation this turn
     * @throws IllegalStateException if turn cannot be started (already started, finished, or other lifecycle violation)
     */
    public ActionResult startTurn(boolean activateHappyHour) {
        try {
            turnManager.beginTurn(restaurant, activateHappyHour);
            return ActionResult.ok("Turn started");
        } catch (IllegalStateException e) {
            return ActionResult.fail(e.getMessage());
        }
    }

    /**
     * Attempt to seat a customer using the Host.
     * Fails if Host choice is unavailable; otherwise delegates and records the choice only on success.
     *
     * @param customerId the customer to seat
     * @param tableId the table to seat them at
     * @return ActionResult indicating success/failure
     */
    public ActionResult seatCustomerWithHost(int customerId, int tableId) {
        if (!turnManager.canAssignHost()) {
            return ActionResult.fail("Host choice is unavailable this turn");
        }

        ActionResult result = host.seat(restaurant, customerId, tableId);
        if (result.success()) {
            turnManager.recordHostTask();
        }
        return result;
    }

    /**
     * Consume the Host choice without taking action.
     * Fails without mutation if choice is unavailable.
     *
     * @return ActionResult indicating success/failure
     */
    public ActionResult waitHost() {
        if (!turnManager.canAssignHost()) {
            return ActionResult.fail("Host choice is unavailable this turn");
        }

        turnManager.recordHostTask();
        return ActionResult.ok("Host skipped this turn");
    }

    /**
     * Attempt to seat a customer using the Waiter.
     * Fails if Waiter choice is unavailable; otherwise delegates and records the choice only on success.
     *
     * @param customerId the customer to seat
     * @param tableId the table to seat them at
     * @return ActionResult indicating success/failure
     */
    public ActionResult seatCustomer(int customerId, int tableId) {
        if (!turnManager.canAssignWaiter()) {
            return ActionResult.fail("Waiter choice is unavailable this turn");
        }

        ActionResult result = waiter.seat(restaurant, customerId, tableId);
        if (result.success()) {
            turnManager.recordWaiterTask();
        }
        return result;
    }

    /**
     * Attempt to take a customer's order.
     * Fails if Waiter choice is unavailable; otherwise delegates with current turn and records the choice only on success.
     *
     * @param customerId the customer to take order from
     * @return ActionResult indicating success/failure
     */
    public ActionResult takeOrder(int customerId) {
        if (!turnManager.canAssignWaiter()) {
            return ActionResult.fail("Waiter choice is unavailable this turn");
        }

        ActionResult result = waiter.takeOrder(restaurant, customerId, turnManager.getCurrentTurn());
        if (result.success()) {
            turnManager.recordWaiterTask();
        }
        return result;
    }

    /**
     * Attempt to serve a customer's order.
     * Fails if Waiter choice is unavailable; otherwise delegates with current turn and records the choice only on success.
     *
     * @param customerId the customer to serve
     * @return ActionResult indicating success/failure
     */
    public ActionResult serveOrder(int customerId) {
        if (!turnManager.canAssignWaiter()) {
            return ActionResult.fail("Waiter choice is unavailable this turn");
        }

        ActionResult result = waiter.serve(restaurant, customerId, turnManager.getCurrentTurn());
        if (result.success()) {
            turnManager.recordWaiterTask();
        }
        return result;
    }

    /**
     * Attempt to prepare one unit of a customer's order.
     * Fails if Chef choice is unavailable or no order exists; otherwise delegates and records the choice only on success.
     *
     * @param customerId the customer whose order to prepare
     * @return ActionResult indicating success/failure
     */
    public ActionResult prepareOrder(int customerId) {
        if (!turnManager.canAssignChef()) {
            return ActionResult.fail("Chef choice is unavailable this turn");
        }

        var customer = restaurant.findCustomer(customerId);
        if (customer == null) {
            return ActionResult.fail("There is no customer " + customerId + ".");
        }

        var order = customer.getOrder();
        if (order == null) {
            return ActionResult.fail("Customer " + customerId + " has not ordered yet.");
        }

        ActionResult result = chef.prepareOneUnit(order);
        if (result.success()) {
            turnManager.recordChefTask();
        }
        return result;
    }

    /**
     * Consume the Waiter choice without taking action.
     * Fails without mutation if choice is unavailable.
     *
     * @return ActionResult indicating success/failure
     */
    public ActionResult waitWaiter() {
        if (!turnManager.canAssignWaiter()) {
            return ActionResult.fail("Waiter choice is unavailable this turn");
        }

        turnManager.recordWaiterTask();
        return ActionResult.ok("Waiter skipped this turn");
    }

    /**
     * Consume the Chef choice without taking action.
     * Fails without mutation if choice is unavailable.
     *
     * @return ActionResult indicating success/failure
     */
    public ActionResult waitChef() {
        if (!turnManager.canAssignChef()) {
            return ActionResult.fail("Chef choice is unavailable this turn");
        }

        turnManager.recordChefTask();
        return ActionResult.ok("Chef skipped this turn");
    }

    /**
     * Finish the current turn by executing automatic events and advancing.
     * Calls resolveTurn() then advanceTurn() exactly once.
     */
    public void finishTurn() {
        turnManager.resolveTurn(restaurant);
        turnManager.advanceTurn();
    }

    /**
     * Build an immutable snapshot of the current game state.
     * Reads from Restaurant and TurnManager, uses view factories.
     *
     * @return GameSnapshot with copied collections
     */
    public GameSnapshot getSnapshot() {
        int turn = turnManager.getCurrentTurn();
        int completedTurns = turnManager.getCompletedTurns();
        int revenueCents = restaurant.getRevenueCents();
        Double averagePaidSatisfaction = restaurant.getPaidCount() == 0
            ? null
            : restaurant.getAveragePaidSatisfaction();

        var waitingQueue = restaurant.getWaitingQueue();
        List<Integer> waitingCustomerIds = waitingQueue.stream()
            .map(restaurantrush.domain.Customer::getId)
            .toList();

        var customers = restaurant.getActiveCustomers().stream()
            .map(c -> {
                Integer tableId = null;
                var table = restaurant.findTableForCustomer(c.getId());
                if (table != null) {
                    tableId = table.getId();
                }
                return CustomerView.from(c, tableId);
            })
            .toList();

        var tables = restaurant.getTables().stream()
            .map(TableView::from)
            .toList();

        boolean hostAvailable = turnManager.canAssignHost();
        boolean waiterAvailable = turnManager.canAssignWaiter();
        boolean chefAvailable = turnManager.canAssignChef();
        int paidCount = restaurant.getPaidCount();
        int servedCount = restaurant.getServedCount();
        int abandonedCount = restaurant.getAbandonedCount();
        int happyHourTurnsRemaining = turnManager.getHappyHourTurnsRemaining();
        boolean happyHourUsed = turnManager.hasHappyHourBeenActivated();

        return new GameSnapshot(
            turn,
            completedTurns,
            revenueCents,
            averagePaidSatisfaction,
            waitingCustomerIds,
            customers,
            tables,
            hostAvailable,
            waiterAvailable,
            chefAvailable,
            paidCount,
            servedCount,
            abandonedCount,
            happyHourTurnsRemaining,
            happyHourUsed
        );
    }

    /**
     * Check if the game is finished.
     *
     * @return true only after turn 18 has resolved and advanced
     */
    public boolean isFinished() {
        return turnManager.isFinished();
    }

    /**
     * Check if the game resulted in victory.
     * Returns false during play. After completion, returns true only when all three objectives pass.
     *
     * @return true if all victory conditions are met after turn 18
     */
    public boolean isVictory() {
        if (!isFinished()) {
            return false;
        }

        int paidCount = restaurant.getPaidCount();
        int revenueCents = restaurant.getRevenueCents();
        double averagePaidSatisfaction = restaurant.getAveragePaidSatisfaction();

        return paidCount >= 4
            && revenueCents >= 4500
            && averagePaidSatisfaction >= 60.0;
    }
}
