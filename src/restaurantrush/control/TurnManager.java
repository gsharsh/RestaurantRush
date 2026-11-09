package restaurantrush.control;

import restaurantrush.domain.HappyHour;
import restaurantrush.domain.Restaurant;
import restaurantrush.staff.Cashier;

/**
 * Sole authority for turn phase and per-turn staff availability.
 * Manages the 18-turn game flow and automatic events.
 *
 * OOP principles applied:
 * - Encapsulation: Private state, controlled access through methods
 * - Single Responsibility: Only manages turn state, not domain rules
 * - Immutability: Final references to collaborators
 */
public final class TurnManager {
    private static final int FINAL_TURN = 18;

    private int currentTurn;
    private int completedTurns;
    private boolean hostTaskUsed;
    private boolean waiterTaskUsed;
    private boolean chefTaskUsed;
    private boolean turnStarted;
    private boolean turnResolved;
    private boolean finished;

    private final Cashier cashier;
    private final ArrivalSchedule arrivals;
    private final HappyHour happyHour;

    /**
     * Constructs a new TurnManager.
     *
     * @param cashier non-null automatic payment handler
     * @param arrivals non-null customer arrival schedule
     * @param happyHour non-null shared Happy Hour policy
     * @throws IllegalArgumentException if any parameter is null
     */
    public TurnManager(Cashier cashier, ArrivalSchedule arrivals, HappyHour happyHour) {
        if (cashier == null) {
            throw new IllegalArgumentException("cashier must not be null");
        }
        if (arrivals == null) {
            throw new IllegalArgumentException("arrivals must not be null");
        }
        if (happyHour == null) {
            throw new IllegalArgumentException("happyHour must not be null");
        }
        this.cashier = cashier;
        this.arrivals = arrivals;
        this.happyHour = happyHour;

        this.currentTurn = 1;
        this.completedTurns = 0;
        this.hostTaskUsed = false;
        this.waiterTaskUsed = false;
        this.chefTaskUsed = false;
        this.turnStarted = false;
        this.turnResolved = false;
        this.finished = false;
    }

    /**
     * Begin a new turn after attempting to activate Happy Hour, adding any scheduled arrival,
     * and recovering waiting/seated customer satisfaction if Happy Hour is active.
     * A rejected arrival or failed activation leaves the turn state unchanged.
     *
     * @param restaurant non-null domain state
     * @param activateHappyHour true to attempt activation this turn
     * @throws IllegalArgumentException if restaurant is null
     * @throws IllegalStateException if turn already started, finished, or Happy Hour already activated
     */
    public void beginTurn(Restaurant restaurant, boolean activateHappyHour) {
        if (restaurant == null) {
            throw new IllegalArgumentException("restaurant must not be null");
        }
        if (turnStarted || finished) {
            throw new IllegalStateException("cannot begin turn: already started or finished");
        }

        if (activateHappyHour && happyHour.hasBeenActivated()) {
            throw new IllegalStateException("Happy Hour has already been activated");
        }

        // Add scheduled arrival if any
        var arrival = arrivals.arrivalFor(currentTurn);
        if (arrival != null) {
            restaurant.addArrival(arrival);
        }

        // Attempt activation before marking turn as started
        if (activateHappyHour) {
            happyHour.activate(currentTurn);
        }

        turnStarted = true;
        hostTaskUsed = false;
        waiterTaskUsed = false;
        chefTaskUsed = false;
        turnResolved = false;

        // Apply customer recovery if Happy Hour is now active
        if (happyHour.remainingTurns(currentTurn) > 0) {
            for (var customer : restaurant.getActiveCustomers()) {
                customer.recoverWaitingSatisfaction(15);
            }
        }
    }

    /**
     * Check if the Host choice is available this turn.
     *
     * @return true only if turn is active, unresolved, and Host choice unused
     */
    public boolean canAssignHost() {
        return turnStarted && !turnResolved && !hostTaskUsed;
    }

    /**
     * Check if the Waiter choice is available this turn.
     *
     * @return true only if Host has acted and Waiter choice is unused
     */
    public boolean canAssignWaiter() {
        return turnStarted && !turnResolved && hostTaskUsed && !waiterTaskUsed;
    }

    /**
     * Check if the Chef choice is available this turn.
     *
     * @return true only if Waiter has acted and Chef choice is unused
     */
    public boolean canAssignChef() {
        return turnStarted && !turnResolved && waiterTaskUsed && !chefTaskUsed;
    }

    /**
     * Record that the Host choice was used.
     *
     * @throws IllegalStateException if Host choice is not available
     */
    public void recordHostTask() {
        if (!canAssignHost()) {
            throw new IllegalStateException("cannot record Host task: not available");
        }
        hostTaskUsed = true;
    }

    /**
     * Record that the Waiter choice was used.
     *
     * @throws IllegalStateException if Waiter choice is not available
     */
    public void recordWaiterTask() {
        if (!canAssignWaiter()) {
            throw new IllegalStateException("cannot record Waiter task: not available");
        }
        waiterTaskUsed = true;
    }

    /**
     * Record that the Chef choice was used.
     *
     * @throws IllegalStateException if Chef choice is not available
     */
    public void recordChefTask() {
        if (!canAssignChef()) {
            throw new IllegalStateException("cannot record Chef task: not available");
        }
        chefTaskUsed = true;
    }

    /**
     * Execute end-of-turn automatic events: eating, payment, satisfaction loss, abandonment.
     *
     * @param restaurant non-null domain state
     * @throws IllegalArgumentException if restaurant is null
     * @throws IllegalStateException if turn is not in the correct state
     */
    public void resolveTurn(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("restaurant must not be null");
        }
        if (!turnStarted || turnResolved) {
            throw new IllegalStateException("cannot resolve turn: not started or already resolved");
        }
        if (!hostTaskUsed || !waiterTaskUsed || !chefTaskUsed) {
            throw new IllegalStateException("cannot resolve turn: tasks not completed");
        }

        turnResolved = true;

        // Step 5: Customers served earlier finish eating
        var activeCustomers = restaurant.getActiveCustomers();
        for (var customer : activeCustomers) {
            customer.finishEating(currentTurn);
        }

        // Step 6: Automatic Cashier payment (at most one)
        cashier.collectOne(restaurant, currentTurn);

        // Step 7: Satisfaction loss for waiting/seated
        for (var customer : activeCustomers) {
            customer.applyWaitingLoss();
        }

        // Step 8: Abandon customers at zero satisfaction
        var toAbandon = activeCustomers.stream()
            .filter(c -> c.getSatisfaction() == 0)
            .toList();
        for (var customer : toAbandon) {
            restaurant.removeAbandoned(customer);
        }
    }

    /**
     * Advance to the next turn or mark game finished.
     *
     * @throws IllegalStateException if turn is not resolved, already advanced or the game is finished
     */
    public void advanceTurn() {
        if (finished || !turnStarted || !turnResolved) {
            throw new IllegalStateException("cannot advance turn: not resolved or already finished");
        }

        completedTurns++;

        if (currentTurn >= FINAL_TURN) {
            finished = true;
        } else {
            currentTurn++;
        }
        turnStarted = false;
    }

    /**
     * Get the current turn number (1-18).
     */
    public int getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Get the number of completed turns (0-18).
     */
    public int getCompletedTurns() {
        return completedTurns;
    }

    /**
     * Check if the game is finished (all 18 turns completed and resolved).
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Check if Happy Hour has been activated at any point.
     *
     * @return true if Happy Hour was activated, false otherwise
     */
    public boolean hasHappyHourBeenActivated() {
        return happyHour.hasBeenActivated();
    }

    /**
     * Get the remaining turns of Happy Hour on the current turn.
     * Returns 0 once the game is finished.
     *
     * @return 2 on activation turn, 1 on next turn, 0 otherwise
     */
    public int getHappyHourTurnsRemaining() {
        if (finished) {
            return 0;
        }
        return happyHour.remainingTurns(currentTurn);
    }
}
