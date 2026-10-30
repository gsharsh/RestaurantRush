package restaurantrush.control;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import restaurantrush.domain.ActionResult;
import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;
import restaurantrush.domain.OrderStatus;
import restaurantrush.domain.Restaurant;
import restaurantrush.staff.Cashier;
import restaurantrush.staff.Chef;
import restaurantrush.staff.Waiter;
import restaurantrush.view.CustomerView;
import restaurantrush.view.GameSnapshot;

/** Real-collaborator controller checks; threshold fixtures are separate from legal gameplay. */
public final class GameControllerTest {
    private static int checks;

    private GameControllerTest() {
    }

    public static void main(String[] args) {
        testConstructorNulls();
        testActionsRetriesAndStaffLimits();
        testWaitConsumesBothChoices();
        testPastaTimingThroughRealController();
        testVictoryBoundaries();
        System.out.println("GameControllerTest: PASS (" + checks + " checks)");
    }

    private static void testConstructorNulls() {
        Fixture f = new Fixture();
        expectIllegalArgument(() -> new GameController(null, f.manager, f.waiter, f.chef));
        expectIllegalArgument(() -> new GameController(f.restaurant, null, f.waiter, f.chef));
        expectIllegalArgument(() -> new GameController(f.restaurant, f.manager, null, f.chef));
        expectIllegalArgument(() -> new GameController(f.restaurant, f.manager, f.waiter, null));
    }

    private static void testActionsRetriesAndStaffLimits() {
        Fixture f = new Fixture();
        failedUnchanged(f, () -> f.controller.seatCustomer(1, 1), "seat before start");
        failedUnchanged(f, () -> f.controller.takeOrder(1), "order before start");
        failedUnchanged(f, () -> f.controller.serveOrder(1), "serve before start");
        failedUnchanged(f, () -> f.controller.prepareOrder(1), "cook before start");
        failedUnchanged(f, f.controller::waitWaiter, "Waiter wait before start");
        failedUnchanged(f, f.controller::waitChef, "Chef wait before start");

        f.controller.startTurn();
        GameSnapshot arrival = f.controller.getSnapshot();
        equal(List.of(1), arrival.waitingCustomerIds(), "arrival appears before manager choices");
        equal(new CustomerView(1, "Regular", 100, CustomerStatus.WAITING, null, null, null, null),
                arrival.customers().get(0), "start snapshot includes fresh arrival");
        check(arrival.averagePaidSatisfaction() == null, "no paid average before payment");
        check(arrival.waiterAvailable() && !arrival.chefAvailable(), "Waiter chooses first");

        failedUnchanged(f, () -> f.controller.prepareOrder(1), "Chef cannot act before Waiter");
        failedUnchanged(f, f.controller::waitChef, "Chef cannot wait before Waiter");
        failedUnchanged(f, () -> f.controller.seatCustomer(99, 1), "unknown customer");
        failedUnchanged(f, () -> f.controller.seatCustomer(1, 99), "unknown table");
        failedUnchanged(f, () -> f.controller.takeOrder(99), "unknown ordering customer");
        failedUnchanged(f, () -> f.controller.takeOrder(1), "queued customer cannot order");
        failedUnchanged(f, () -> f.controller.serveOrder(99), "unknown service customer");
        failedUnchanged(f, () -> f.controller.serveOrder(1), "queued customer cannot be served");
        succeeded(f.controller.seatCustomer(1, 1), "valid seat after failed attempts");
        check(!f.manager.canAssignWaiter() && f.manager.canAssignChef(), "seat consumes only Waiter");
        rejectWaiterChoices(f);
        failedUnchanged(f, () -> f.controller.prepareOrder(99), "unknown cooking customer");
        failedUnchanged(f, () -> f.controller.prepareOrder(1), "customer has no order");
        succeeded(f.controller.waitChef(), "Chef can wait after failed cooking requests");
        rejectChefChoices(f, 1);
        f.controller.finishTurn();
        equal(92, f.restaurant.findCustomer(1).getSatisfaction(), "one end-of-turn waiting penalty");
        equal(1, f.controller.getSnapshot().completedTurns(), "one completed turn");

        f.controller.startTurn();
        failedUnchanged(f, () -> f.controller.seatCustomer(1, 2), "cannot seat twice");
        failedUnchanged(f, () -> f.controller.serveOrder(1), "no order to serve");
        succeeded(f.controller.takeOrder(1), "valid order after failed attempts");
        rejectWaiterChoices(f);
        succeeded(f.controller.prepareOrder(1), "Chef can prepare an order taken this turn");
        equal(OrderStatus.READY, f.restaurant.findOrder(1).getStatus(), "Salad needs one unit");
        rejectChefChoices(f, 1);
        f.controller.finishTurn();
        equal(84, f.restaurant.findCustomer(1).getSatisfaction(), "READY but unserved still loses satisfaction");

        f.controller.startTurn();
        failedUnchanged(f, () -> f.controller.takeOrder(1), "duplicate order");
        succeeded(f.controller.serveOrder(1), "serve on a later turn");
        rejectWaiterChoices(f);
        failedUnchanged(f, () -> f.controller.prepareOrder(1), "served order cannot be cooked");
        succeeded(f.controller.waitChef(), "failed cook preserves Chef choice");
        f.controller.finishTurn();
        equal(CustomerStatus.EATING, f.restaurant.findCustomer(1).getStatus(), "no same-turn eating completion");

        f.controller.startTurn();
        failedUnchanged(f, () -> f.controller.seatCustomer(2, 1), "occupied table");
        succeeded(f.controller.seatCustomer(2, 2), "valid free table remains selectable");
        succeeded(f.controller.waitChef(), "Chef waits");
        f.controller.finishTurn();
        equal(CustomerStatus.READY_TO_PAY, f.restaurant.findCustomer(1).getStatus(), "eating completes next turn");
        equal(0, f.restaurant.getPaidCount(), "newly ready customer cannot pay yet");
        waitTurn(f);
        equal(1, f.restaurant.getPaidCount(), "automatic payment on following turn");
        equal(900, f.restaurant.getRevenueCents(), "paid Salad adds its locked bill");
        check(f.restaurant.findTable(1).isAvailable(), "payment frees table");
        check(f.controller.getSnapshot().customers().stream().noneMatch(c -> c.id() == 1),
                "paid customer is excluded from active snapshot");
        equal(CustomerStatus.PAID, f.restaurant.findCustomer(1).getStatus(), "paid customer stays in history");
        equal(100, arrival.customers().get(0).satisfaction(), "old controller snapshot remains unchanged");
        equal(CustomerStatus.WAITING, arrival.customers().get(0).status(), "old snapshot retains lifecycle");
    }

    private static void testWaitConsumesBothChoices() {
        Fixture f = new Fixture();
        f.controller.startTurn();
        succeeded(f.controller.waitWaiter(), "Waiter explicitly waits");
        rejectWaiterChoices(f);
        check(f.manager.canAssignChef(), "Waiter wait unlocks Chef");
        succeeded(f.controller.waitChef(), "Chef explicitly waits");
        rejectChefChoices(f, 1);
        equal(100, f.restaurant.findCustomer(1).getSatisfaction(), "choices do not resolve a turn");
        f.controller.finishTurn();
        equal(92, f.restaurant.findCustomer(1).getSatisfaction(), "resolution happens once after both waits");

        // A READY order is a legal domain object, but an invalid Chef target.
        f.controller.startTurn();
        succeeded(f.controller.seatCustomer(1, 1), "seat on turn two");
        succeeded(f.controller.waitChef(), "no order yet");
        f.controller.finishTurn();
        f.controller.startTurn();
        succeeded(f.controller.takeOrder(1), "order on turn three");
        succeeded(f.controller.prepareOrder(1), "finish Salad");
        f.controller.finishTurn();
        f.controller.startTurn();
        succeeded(f.controller.waitWaiter(), "leave READY order unserved");
        failedUnchanged(f, () -> f.controller.prepareOrder(1), "READY order cannot be cooked again");
        succeeded(f.controller.waitChef(), "READY rejection preserves Chef choice");
        f.controller.finishTurn();
    }

    private static void testPastaTimingThroughRealController() {
        Fixture f = new Fixture();
        for (int turn = 1; turn < 7; turn++) {
            waitTurn(f);
        }
        f.controller.startTurn();
        Customer pastaCustomer = f.restaurant.findCustomer(3);
        equal(100, pastaCustomer.getSatisfaction(), "turn-seven arrival starts at 100");
        succeeded(f.controller.seatCustomer(3, 1), "seat Pasta customer");
        succeeded(f.controller.waitChef(), "no order yet");
        f.controller.finishTurn();
        equal(92, pastaCustomer.getSatisfaction(), "arrival/seat turn loses eight");

        f.controller.startTurn();
        succeeded(f.controller.takeOrder(3), "customer chooses Pasta by its own rule");
        succeeded(f.controller.prepareOrder(3), "first Pasta preparation unit");
        Order order = pastaCustomer.getOrder();
        equal("Pasta", order.getItem().getName(), "deterministic third menu choice");
        equal(OrderStatus.PREPARING, order.getStatus(), "one unit does not finish Pasta");
        equal(1, order.getRemainingUnits(), "one unit remains");
        f.controller.finishTurn();
        equal(84, pastaCustomer.getSatisfaction(), "order turn loses eight");

        f.controller.startTurn();
        failedUnchanged(f, () -> f.controller.serveOrder(3), "PREPARING cannot be served");
        succeeded(f.controller.waitWaiter(), "Waiter waits before final cooking unit");
        succeeded(f.controller.prepareOrder(3), "second unit makes Pasta READY");
        equal(OrderStatus.READY, order.getStatus(), "Pasta ready after two Chef choices");
        failedUnchanged(f, () -> f.controller.serveOrder(3), "cannot serve after Chef in same turn");
        f.controller.finishTurn();
        equal(76, pastaCustomer.getSatisfaction(), "READY turn still loses eight");

        f.controller.startTurn();
        succeeded(f.controller.serveOrder(3), "serve Pasta on turn ten");
        succeeded(f.controller.waitChef(), "Chef waits after service");
        f.controller.finishTurn();
        equal(CustomerStatus.EATING, pastaCustomer.getStatus(), "service turn does not finish eating");
        equal(10, pastaCustomer.getServedTurn(), "controller forwards actual service turn");
        equal(0, f.restaurant.getRevenueCents(), "service adds no revenue");
        waitTurn(f);
        equal(CustomerStatus.READY_TO_PAY, pastaCustomer.getStatus(), "turn eleven finishes eating");
        equal(11, pastaCustomer.getReadyToPayTurn(), "payment readiness recorded once");
        equal(0, f.restaurant.getPaidCount(), "no payment in readiness turn");
        waitTurn(f);
        equal(CustomerStatus.PAID, pastaCustomer.getStatus(), "turn twelve automatically pays");
        equal(OrderStatus.PAID, order.getStatus(), "same shared order is paid");
        equal(1800, f.restaurant.getRevenueCents(), "Pasta payment adds $18");
        equal(76.0, f.restaurant.getAveragePaidSatisfaction(), "paid satisfaction freezes after service");
        check(f.restaurant.findTable(1).isAvailable(), "Pasta payment frees its table");
        while (!f.controller.isFinished()) {
            check(!f.controller.isVictory(), "no victory before turn eighteen completes");
            waitTurn(f);
        }
        equal(18, f.manager.getCompletedTurns(), "game completes exactly eighteen turns");
        check(!f.controller.isVictory(), "one payment is insufficient for victory");
        failedUnchanged(f, f.controller::waitWaiter, "Waiter unavailable after game");
        failedUnchanged(f, f.controller::waitChef, "Chef unavailable after game");
        failedUnchanged(f, () -> f.controller.seatCustomer(6, 1), "seat unavailable after game");
        failedUnchanged(f, () -> f.controller.takeOrder(6), "order unavailable after game");
        failedUnchanged(f, () -> f.controller.serveOrder(6), "serve unavailable after game");
        failedUnchanged(f, () -> f.controller.prepareOrder(6), "cook unavailable after game");
    }

    private static void testVictoryBoundaries() {
        victoryCase("exact thresholds", new int[] {900, 900, 900, 1800},
                new int[] {60, 60, 60, 60}, true);
        victoryCase("paid count independently required", new int[] {1500, 1500, 1500},
                new int[] {60, 60, 60}, false);
        victoryCase("one cent below revenue target", new int[] {900, 900, 900, 1799},
                new int[] {60, 60, 60, 60}, false);
        victoryCase("fractional average must not round up", new int[] {900, 900, 900, 1800},
                new int[] {60, 60, 60, 59}, false);
        victoryCase("above all thresholds", new int[] {900, 900, 900, 1800, 900},
                new int[] {80, 80, 80, 80, 80}, true);
    }

    /**
     * Builds precise statistics through real order/payment APIs, not by simulating
     * a legal eighteen-turn playthrough. The only fixture subtype supplies quotes
     * and one-point losses; it cannot override lifecycle or accounting methods.
     */
    private static void victoryCase(String description, int[] bills, int[] satisfactions, boolean expected) {
        Fixture f = new Fixture();
        int revenue = 0;
        int satisfactionTotal = 0;
        for (int i = 0; i < bills.length; i++) {
            Customer customer = new QuotedCustomer(100 + i, bills[i]);
            f.restaurant.addArrival(customer);
            succeeded(f.waiter.seat(f.restaurant, customer.getId(), 1), "threshold fixture seat");
            succeeded(f.waiter.takeOrder(f.restaurant, customer.getId()), "threshold fixture quoted order");
            for (int loss = satisfactions[i]; loss < 100; loss++) {
                customer.applyWaitingLoss();
            }
            succeeded(f.chef.prepareOneUnit(customer.getOrder()), "threshold fixture prepares Salad");
            succeeded(f.waiter.serve(f.restaurant, customer.getId(), 1), "threshold fixture serve");
            customer.finishEating(2);
            check(f.restaurant.recordPayment(customer, 3), "threshold fixture records real payment");
            revenue += bills[i];
            satisfactionTotal += satisfactions[i];
        }
        equal(bills.length, f.restaurant.getPaidCount(), description + ": exact paid count setup");
        equal(revenue, f.restaurant.getRevenueCents(), description + ": exact revenue setup");
        equal((double) satisfactionTotal / bills.length, f.restaurant.getAveragePaidSatisfaction(),
                description + ": exact unrounded average setup");
        check(!f.controller.isVictory(), description + ": totals alone cannot end an unstarted game");
        for (int turn = 1; turn <= 18; turn++) {
            f.controller.startTurn();
            check(!f.controller.isFinished() && !f.controller.isVictory(),
                    description + ": game remains active until final resolution");
            succeeded(f.controller.waitWaiter(), "threshold run Waiter wait");
            succeeded(f.controller.waitChef(), "threshold run Chef wait");
            f.controller.finishTurn();
            equal(turn == 18, f.controller.isFinished(), description + ": completion turn");
        }
        equal(expected, f.controller.isVictory(), description);
        equal(revenue, f.restaurant.getRevenueCents(), description + ": unpaid arrivals add no revenue");
    }

    private static void waitTurn(Fixture f) {
        f.controller.startTurn();
        succeeded(f.controller.waitWaiter(), "Waiter waits");
        succeeded(f.controller.waitChef(), "Chef waits");
        f.controller.finishTurn();
    }

    private static void rejectWaiterChoices(Fixture f) {
        failedUnchanged(f, () -> f.controller.seatCustomer(1, 2), "second seat choice");
        failedUnchanged(f, () -> f.controller.takeOrder(1), "second order choice");
        failedUnchanged(f, () -> f.controller.serveOrder(1), "second service choice");
        failedUnchanged(f, f.controller::waitWaiter, "second Waiter wait");
    }

    private static void rejectChefChoices(Fixture f, int customerId) {
        failedUnchanged(f, () -> f.controller.prepareOrder(customerId), "second Chef preparation");
        failedUnchanged(f, f.controller::waitChef, "second Chef wait");
    }

    private static void failedUnchanged(Fixture f, Supplier<ActionResult> action, String description) {
        State before = State.capture(f);
        ActionResult result = action.get();
        check(!result.success(), description + ": must fail");
        check(result.message() != null && !result.message().isBlank(), description + ": failure explains why");
        equal(before, State.capture(f), description + ": state and staff availability unchanged");
    }

    private static void succeeded(ActionResult result, String description) {
        check(result.success(), description + ": " + result.message());
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

    private static void equal(Object expected, Object actual, String description) {
        check(Objects.equals(expected, actual), description + ": expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }

    private static final class Fixture {
        private final Restaurant restaurant = new Restaurant();
        private final Waiter waiter = new Waiter();
        private final Chef chef = new Chef();
        private final TurnManager manager = new TurnManager(new Cashier(), new ArrivalSchedule());
        private final GameController controller = new GameController(restaurant, manager, waiter, chef);
    }

    private record State(GameSnapshot snapshot, List<Customer> customerIdentities, List<Order> orderIdentities,
            List<Integer> servedTurns, List<Integer> readyToPayTurns, int paidSatisfactionTotal) {
        private static State capture(Fixture f) {
            return new State(f.controller.getSnapshot(), f.restaurant.getCustomers(), f.restaurant.getOrders(),
                    f.restaurant.getCustomers().stream().map(Customer::getServedTurn).toList(),
                    f.restaurant.getCustomers().stream().map(Customer::getReadyToPayTurn).toList(),
                    f.restaurant.getPaidSatisfactionTotal());
        }
    }

    private static final class QuotedCustomer extends Customer {
        private final int quote;

        private QuotedCustomer(int id, int quote) {
            super(id);
            this.quote = quote;
        }

        @Override
        public int waitingLossPerTurn() {
            return 1;
        }

        @Override
        public MenuItem chooseMenuItem(List<MenuItem> menu) {
            return menu.get(0);
        }

        @Override
        public int quotePriceCents(MenuItem item) {
            return quote;
        }
    }
}
