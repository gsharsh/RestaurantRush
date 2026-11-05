package restaurantrush.control;

import restaurantrush.domain.Customer;
import restaurantrush.domain.RegularCustomer;
import restaurantrush.domain.VIPCustomer;
import restaurantrush.domain.CriticCustomer;
import java.util.Map;

/**
 * Stateless Stage 2 arrival schedule.
 * Owns the deterministic arrival rule and constructs Customer instances of appropriate types.
 * Returns customers through the abstract Customer base type.
 *
 * OOP principles applied:
 * - Encapsulation: Static mapping is immutable, behavior hidden
 * - Single Responsibility: Only provides arrivals, doesn't track state
 * - Polymorphism: Returns Customer, not concrete subtypes; dispatches type through polymorphic construction
 */
public final class ArrivalSchedule {
    private static final Map<Integer, Integer> CUSTOMER_ID_BY_TURN = Map.ofEntries(
        Map.entry(1, 1),
        Map.entry(4, 2),
        Map.entry(7, 3),
        Map.entry(10, 4),
        Map.entry(13, 5),
        Map.entry(16, 6)
    );

    /**
     * Construct the stateless Stage 2 schedule.
     */
    public ArrivalSchedule() {
    }

    /**
     * Get the scheduled arrival for a turn.
     *
     * @param turn the turn number (1 to 18)
     * @return a new Customer of the appropriate type for this turn, or null if no arrival scheduled
     * @throws IllegalArgumentException if turn is outside valid range
     */
    public Customer arrivalFor(int turn) {
        if (turn < 1 || turn > 18) {
            throw new IllegalArgumentException("turn must be 1 to 18: " + turn);
        }

        Integer customerId = CUSTOMER_ID_BY_TURN.get(turn);
        if (customerId == null) {
            return null;
        }

        return switch (turn) {
            case 4 -> new VIPCustomer(customerId);
            case 10 -> new CriticCustomer(customerId);
            default -> new RegularCustomer(customerId);
        };
    }
}
