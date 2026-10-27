package restaurantrush.control;

import restaurantrush.domain.*;
import restaurantrush.staff.*;
import restaurantrush.view.*;
import java.util.*;

/**
 * Basic verification that GameController compiles and core logic works.
 * Uses minimal stubs of dependencies.
 */
public class GameControllerTest {

    public static void main(String[] args) {
        System.out.println("=== GameController Verification ===\n");

        try {
            // Create minimal stubs
            Restaurant restaurant = new TestRestaurant();
            Cashier cashier = new TestCashier();
            ArrivalSchedule arrivals = new ArrivalSchedule();
            TurnManager turnManager = new TurnManager(cashier, arrivals);
            Waiter waiter = new TestWaiter();
            Chef chef = new TestChef();

            // Create controller
            GameController controller = new GameController(restaurant, turnManager, waiter, chef);
            System.out.println("✓ GameController created successfully");

            // Test initial state
            System.out.println("\n--- Initial State ---");
            var snapshot = controller.getSnapshot();
            System.out.println("Turn: " + snapshot.turn());
            System.out.println("Completed turns: " + snapshot.completedTurns());
            System.out.println("Revenue: " + snapshot.revenueCents() + " cents");
            System.out.println("Finished: " + controller.isFinished());
            System.out.println("Victory: " + controller.isVictory());

            // Test turn start
            System.out.println("\n--- Turn 1 Start ---");
            controller.startTurn();
            System.out.println("✓ Turn started");
            snapshot = controller.getSnapshot();
            System.out.println("Waiter available: " + snapshot.waiterAvailable());
            System.out.println("Chef available: " + snapshot.chefAvailable());

            // Test waiter wait
            System.out.println("\n--- Waiter Wait ---");
            var result = controller.waitWaiter();
            System.out.println("Result: " + result.success() + " - " + result.message());
            System.out.println("Waiter available now: " + controller.getSnapshot().waiterAvailable());

            // Test chef wait
            System.out.println("\n--- Chef Wait ---");
            result = controller.waitChef();
            System.out.println("Result: " + result.success() + " - " + result.message());
            System.out.println("Chef available now: " + controller.getSnapshot().chefAvailable());

            // Test finish turn
            System.out.println("\n--- Finish Turn ---");
            controller.finishTurn();
            System.out.println("✓ Turn finished");
            snapshot = controller.getSnapshot();
            System.out.println("Current turn: " + snapshot.turn());
            System.out.println("Completed turns: " + snapshot.completedTurns());

            // Test turn 2
            System.out.println("\n--- Turn 2 Start ---");
            controller.startTurn();
            System.out.println("✓ Turn 2 started");
            System.out.println("Waiter available: " + controller.getSnapshot().waiterAvailable());

            // Test unavailable action
            System.out.println("\n--- Test Unavailable Action ---");
            controller.waitWaiter();
            var failResult = controller.waitWaiter();
            System.out.println("Second wait result: " + failResult.success() + " - " + failResult.message());

            System.out.println("\n=== All basic verifications passed! ===");

        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

// ===== Minimal Test Stubs (for verification only) =====

class TestRestaurant extends Restaurant {
    @Override
    public void addArrival(Customer customer) {}

    @Override
    public List<Customer> getActiveCustomers() { return List.of(); }

    @Override
    public List<Customer> getWaitingQueue() { return List.of(); }

    @Override
    public List<Customer> getCustomers() { return List.of(); }

    @Override
    public List<Table> getTables() { return List.of(); }

    @Override
    public int getRevenueCents() { return 0; }

    @Override
    public int getPaidCount() { return 0; }

    @Override
    public int getServedCount() { return 0; }

    @Override
    public int getAbandonedCount() { return 0; }

    @Override
    public double getAveragePaidSatisfaction() { return 0.0; }

    @Override
    public Customer findCustomer(int id) { return null; }

    @Override
    public Table findTableForCustomer(int customerId) { return null; }
}

class TestWaiter extends Waiter {
    @Override
    public ActionResult seat(Restaurant restaurant, int customerId, int tableId) {
        return ActionResult.ok("Seated");
    }

    @Override
    public ActionResult takeOrder(Restaurant restaurant, int customerId) {
        return ActionResult.ok("Order taken");
    }

    @Override
    public ActionResult serve(Restaurant restaurant, int customerId, int turn) {
        return ActionResult.ok("Served");
    }
}

class TestChef extends Chef {
    @Override
    public ActionResult prepareOneUnit(Order order) {
        return ActionResult.ok("Prepared");
    }
}

class TestCashier extends Cashier {
    @Override
    public boolean collectOne(Restaurant restaurant, int turn) {
        return false;
    }
}
