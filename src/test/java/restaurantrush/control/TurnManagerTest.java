package restaurantrush.control;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.MenuItem;
import restaurantrush.domain.RegularCustomer;
import restaurantrush.domain.Restaurant;
import restaurantrush.domain.Table;
import restaurantrush.staff.Cashier;

/** Offline regression checks for failed starts and completed-turn advancement. */
public final class TurnManagerTest {
    private static int checks;
    private static final List<String> failures = new ArrayList<>();

    private TurnManagerTest() {
    }

    public static void main(String[] args) {
        int before = checks;
        run("failed beginTurn preserves state and permits retry", TurnManagerTest::testFailedBeginTurn);
        run("finished game rejects repeated advancement", TurnManagerTest::testRepeatedFinalAdvance);
        run("normal 18-turn phase progression", TurnManagerTest::testNormalTurnProgression);
        if (!failures.isEmpty()) {
            throw new AssertionError(String.join(System.lineSeparator(), failures));
        }
        System.out.println("TurnManagerTest: PASS (" + (checks - before) + " checks)");
    }

    private static void testFailedBeginTurn() {
        Restaurant restaurant = new Restaurant();
        Customer existing = new RegularCustomer(1);
        restaurant.addArrival(existing);
        List<Table> tablesBefore = restaurant.getTables();
        List<MenuItem> menuBefore = restaurant.getMenu();
        TurnManager manager = new TurnManager(new Cashier(), new ArrivalSchedule());
        checkManager(manager, 1, 0, false, false, false);

        expectIllegalArgument(() -> manager.beginTurn(restaurant));
        check(restaurant.getCustomers().equals(List.of(existing)), "failed start must preserve customer history");
        check(restaurant.getWaitingQueue().equals(List.of(existing)), "failed start must preserve the queue");
        check(restaurant.getActiveCustomers().equals(List.of(existing)), "failed start must preserve active customers");
        check(restaurant.findCustomer(1) == existing, "failed start must preserve customer identity");
        check(existing.getId() == 1 && existing.getSatisfaction() == 100
                && existing.getStatus() == CustomerStatus.WAITING, "existing customer must be unchanged");
        check(existing.getServedTurn() == -1 && existing.getReadyToPayTurn() == -1
                && existing.getOrder() == null, "failed start must not change customer lifecycle details");
        check(restaurant.getOrders().isEmpty() && restaurant.findOrder(1) == null,
                "failed start must not add an order");
        check(restaurant.getTables().equals(tablesBefore), "failed start must preserve table identities");
        for (Table table : tablesBefore) {
            check(table.isAvailable() && table.getOccupant() == null, "failed start must leave tables free");
        }
        check(restaurant.findTableForCustomer(1) == null, "failed start must not seat the customer");
        check(restaurant.getMenu().equals(menuBefore), "failed start must preserve shared menu offerings");
        check(restaurant.getRevenueCents() == 0 && restaurant.getPaidCount() == 0
                && restaurant.getServedCount() == 0 && restaurant.getAbandonedCount() == 0,
                "failed start must preserve zero totals");
        check(restaurant.getPaidSatisfactionTotal() == 0 && restaurant.getAveragePaidSatisfaction() == 0.0,
                "failed start must preserve paid satisfaction totals");
        checkManager(manager, 1, 0, false, false, false);

        Restaurant retry = new Restaurant();
        manager.beginTurn(retry);
        checkManager(manager, 1, 0, false, true, false);
        Customer arrival = retry.findCustomer(1);
        check(arrival != null && arrival != existing, "retry must add the scheduled arrival to the fresh restaurant");
        check(retry.getCustomers().equals(List.of(arrival))
                && retry.getWaitingQueue().equals(List.of(arrival)), "retry must add exactly one arrival");
        check(restaurant.getWaitingQueue().equals(List.of(existing)), "retry must not change the original restaurant");
    }

    private static void testRepeatedFinalAdvance() {
        Restaurant restaurant = new Restaurant();
        TurnManager manager = new TurnManager(new Cashier(), new ArrivalSchedule());
        for (int turn = 1; turn <= 18; turn++) {
            manager.beginTurn(restaurant);
            manager.recordWaiterTask();
            manager.recordChefTask();
            manager.resolveTurn(restaurant);
            manager.advanceTurn();
        }
        checkManager(manager, 18, 18, true, false, false);
        for (int attempt = 0; attempt < 2; attempt++) {
            boolean rejected = false;
            try {
                manager.advanceTurn();
            } catch (IllegalStateException expected) {
                rejected = true;
            }
            checkManager(manager, 18, 18, true, false, false);
            check(rejected, "advanceTurn after completion must throw IllegalStateException");
        }
    }

    private static void testNormalTurnProgression() {
        Restaurant restaurant = new Restaurant();
        TurnManager manager = new TurnManager(new Cashier(), new ArrivalSchedule());
        for (int turn = 1; turn <= 18; turn++) {
            checkManager(manager, turn, turn - 1, false, false, false);
            manager.beginTurn(restaurant);
            checkManager(manager, turn, turn - 1, false, true, false);
            manager.recordWaiterTask();
            checkManager(manager, turn, turn - 1, false, false, true);
            manager.recordChefTask();
            checkManager(manager, turn, turn - 1, false, false, false);
            manager.resolveTurn(restaurant);
            checkManager(manager, turn, turn - 1, false, false, false);
            manager.advanceTurn();
            checkManager(manager, Math.min(turn + 1, 18), turn, turn == 18, false, false);
            expectIllegalState(manager::recordWaiterTask);
            expectIllegalState(manager::recordChefTask);
            expectIllegalState(() -> manager.resolveTurn(restaurant));
        }
        expectIllegalState(() -> manager.beginTurn(restaurant));
        checkManager(manager, 18, 18, true, false, false);
    }

    private static void checkManager(TurnManager manager, int turn, int completedTurns,
            boolean finished, boolean waiterAvailable, boolean chefAvailable) {
        check(manager.getCurrentTurn() == turn,
                "current turn: expected " + turn + ", got " + manager.getCurrentTurn());
        check(manager.getCompletedTurns() == completedTurns,
                "completed turns: expected " + completedTurns + ", got " + manager.getCompletedTurns());
        check(manager.isFinished() == finished, "finished: expected " + finished);
        check(manager.canAssignWaiter() == waiterAvailable, "Waiter availability: expected " + waiterAvailable);
        check(manager.canAssignChef() == chefAvailable, "Chef availability: expected " + chefAvailable);
    }

    private static void expectIllegalArgument(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError("expected IllegalArgumentException");
    }

    private static void expectIllegalState(Runnable action) {
        try {
            action.run();
        } catch (IllegalStateException expected) {
            checks++;
            return;
        }
        throw new AssertionError("expected IllegalStateException");
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
        } catch (AssertionError | RuntimeException failure) {
            failures.add(name + ": " + failure);
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }
}
