package restaurantrush.domain;

import java.util.ArrayList;
import java.util.List;

public final class RestaurantStateTest {
    private static int passed = 0;
    private static final List<String> failures = new ArrayList<>();

    private RestaurantStateTest() {
    }

    public static void main(String[] args) {
        run("Table starts free and validates its ID", RestaurantStateTest::tableBasics);
        run("Table rejects double assign and empty release", RestaurantStateTest::tableGuards);
        run("Restaurant starts with 2 tables, 3-item menu, zero totals", RestaurantStateTest::initialState);
        run("addArrival keeps queue order and rejects duplicates", RestaurantStateTest::arrivals);
        run("seat moves customer from queue to table", RestaurantStateTest::seatSuccess);
        run("invalid seat requests change nothing", RestaurantStateTest::seatFailuresPreserveState);
        run("registerOrder links one shared Order object", RestaurantStateTest::registerOrder);
        run("invalid orders change nothing", RestaurantStateTest::registerOrderFailures);
        run("serve only works on a READY order", RestaurantStateTest::serveRequiresReady);
        run("full flow: serve, eat, pay, table freed", RestaurantStateTest::fullPaymentFlow);
        run("cannot pay on the same turn as becoming READY_TO_PAY", RestaurantStateTest::paymentTiming);
        run("oldest payer first, ties by lower ID", RestaurantStateTest::oldestPayerOrdering);
        run("abandon from queue and from table", RestaurantStateTest::abandonment);
        run("abandon cancels a READY-but-unserved order", RestaurantStateTest::abandonCancelsReadyOrder);
        run("returned lists cannot be modified", RestaurantStateTest::immutableViews);

        System.out.println();
        System.out.println(passed + " passed, " + failures.size() + " failed");
        if (!failures.isEmpty()) {
            failures.forEach(f -> System.out.println("  FAILED: " + f));
            System.exit(1);
        }
    }

    private static void tableBasics() {
        Table table = new Table(1);
        check(table.isAvailable(), "new table is free");
        check(table.getOccupant() == null, "no occupant");
        expectThrows(IllegalArgumentException.class, () -> new Table(0), "ID 0 rejected");
    }

    private static void tableGuards() {
        Table table = new Table(2);
        expectThrows(IllegalStateException.class, table::release, "release on free table");
        Customer c = new RegularCustomer(1);
        table.assign(c);
        check(table.getOccupant() == c, "occupant stored");
        expectThrows(IllegalStateException.class, () -> table.assign(new RegularCustomer(2)), "double assign");
        expectThrows(IllegalArgumentException.class, () -> new Table(3).assign(null), "null customer");
        table.release();
        check(table.isAvailable(), "free after release");
    }

    private static void initialState() {
        Restaurant r = new Restaurant();
        check(r.getTables().size() == 2, "two tables");
        check(r.getTables().get(0).getId() == 1 && r.getTables().get(1).getId() == 2, "table IDs 1, 2");
        List<MenuItem> menu = r.getMenu();
        check(menu.size() == 3, "three menu items");
        checkItem(menu.get(0), "Salad", 900, 1);
        checkItem(menu.get(1), "Burger", 1500, 1);
        checkItem(menu.get(2), "Pasta", 1800, 2);
        check(r.getRevenueCents() == 0 && r.getPaidCount() == 0 && r.getServedCount() == 0
                && r.getAbandonedCount() == 0, "zero totals");
        check(r.getAveragePaidSatisfaction() == 0.0, "average is 0.0 before payments");
    }

    private static void arrivals() {
        Restaurant r = new Restaurant();
        Customer c1 = new RegularCustomer(1);
        Customer c2 = new RegularCustomer(2);
        r.addArrival(c1);
        r.addArrival(c2);
        check(r.getWaitingQueue().equals(List.of(c1, c2)), "queue in arrival order");
        check(r.findCustomer(2) == c2, "findCustomer returns same object");
        check(r.findCustomer(99) == null, "missing customer is null");
        expectThrows(IllegalArgumentException.class, () -> r.addArrival(new RegularCustomer(1)), "duplicate ID");
        expectThrows(IllegalArgumentException.class, () -> r.addArrival(null), "null arrival");
        check(r.getCustomers().size() == 2 && r.getWaitingQueue().size() == 2, "rejected arrivals changed nothing");
    }

    private static void seatSuccess() {
        Restaurant r = new Restaurant();
        Customer c = arrive(r, 1);
        ActionResult result = r.seat(1, 2);
        check(result.success(), "seat succeeds: " + result.message());
        check(c.getStatus() == CustomerStatus.SEATED, "customer SEATED");
        check(r.getWaitingQueue().isEmpty(), "removed from queue");
        check(r.findTable(2).getOccupant() == c, "at table 2");
        check(r.findTableForCustomer(1).getId() == 2, "findTableForCustomer");
    }

    private static void seatFailuresPreserveState() {
        Restaurant r = new Restaurant();
        Customer c1 = arrive(r, 1);
        Customer c2 = arrive(r, 2);
        r.seat(1, 1);

        check(!r.seat(99, 2).success(), "unknown customer");
        check(!r.seat(2, 5).success(), "unknown table");
        check(!r.seat(2, 1).success(), "occupied table");
        check(!r.seat(1, 2).success(), "already seated");

        check(c2.getStatus() == CustomerStatus.WAITING, "c2 still waiting");
        check(r.getWaitingQueue().equals(List.of(c2)), "queue unchanged");
        check(r.findTable(1).getOccupant() == c1 && r.findTable(2).isAvailable(), "tables unchanged");
    }

    private static void registerOrder() {
        Restaurant r = new Restaurant();
        Customer c = arriveAndSeat(r, 3, 1);
        Order order = orderFor(r, c);
        ActionResult result = r.registerOrder(3, order);
        check(result.success(), "order accepted: " + result.message());
        check(c.getOrder() == order && r.findOrder(3) == order, "same Order object in both places");
        check(order.getItem().getName().equals("Pasta"), "customer 3 chose Pasta");
    }

    private static void registerOrderFailures() {
        Restaurant r = new Restaurant();
        Customer waiting = arrive(r, 1);
        check(!r.registerOrder(1, new Order(1, r.getMenu().get(0), 900)).success(), "not seated");
        Customer seated = arriveAndSeat(r, 2, 1);
        check(!r.registerOrder(2, null).success(), "null order");
        check(!r.registerOrder(2, new Order(5, r.getMenu().get(0), 900)).success(), "mismatched ID");
        check(!r.registerOrder(77, new Order(77, r.getMenu().get(0), 900)).success(), "unknown customer");
        check(waiting.getOrder() == null && seated.getOrder() == null && r.getOrders().isEmpty(), "nothing linked");

        r.registerOrder(2, orderFor(r, seated));
        check(!r.registerOrder(2, new Order(2, r.getMenu().get(0), 900)).success(), "second order");
        check(r.getOrders().size() == 1, "still one order");
    }

    private static void serveRequiresReady() {
        Restaurant r = new Restaurant();
        Customer c = arriveAndSeat(r, 3, 1);
        Order order = placeOrder(r, c);
        check(!r.serve(3, 2).success(), "PLACED cannot be served");
        order.prepareOneUnit();
        check(!r.serve(3, 2).success(), "PREPARING cannot be served");
        order.prepareOneUnit();
        check(order.isReady(), "READY after 2 units");
        ActionResult result = r.serve(3, 4);
        check(result.success(), "READY can be served: " + result.message());
        check(order.getStatus() == OrderStatus.SERVED && c.getStatus() == CustomerStatus.EATING, "SERVED / EATING");
        check(r.getServedCount() == 1, "served count 1");
        check(!r.serve(3, 5).success(), "cannot serve twice");
        check(r.getServedCount() == 1, "served count still 1");
        expectThrows(IllegalArgumentException.class, () -> r.serve(3, 0), "turn must be positive");
    }

    private static void fullPaymentFlow() {
        Restaurant r = new Restaurant();
        Customer c = arrive(r, 3);
        r.seat(3, 1);                       // turn 1
        Order order = placeOrder(r, c);     // turn 2
        order.prepareOneUnit();             // turn 2
        order.prepareOneUnit();             // turn 3 -> READY
        r.serve(3, 4);                      // turn 4
        c.finishEating(5);                  // turn 5 -> READY_TO_PAY

        check(r.oldestEligiblePayer(5) == null, "not eligible on turn 5");
        check(!r.recordPayment(c, 5), "cannot pay on turn 5");
        check(r.oldestEligiblePayer(6) == c, "eligible on turn 6");
        check(r.recordPayment(c, 6), "pays on turn 6");

        check(c.getStatus() == CustomerStatus.PAID && order.getStatus() == OrderStatus.PAID, "PAID / PAID");
        check(r.findTable(1).isAvailable(), "table freed");
        check(r.getRevenueCents() == 1800, "revenue $18.00");
        check(r.getPaidCount() == 1, "paid count 1");
        check(r.getPaidSatisfactionTotal() == 100, "satisfaction recorded (no loss applied in this test)");
        check(!r.recordPayment(c, 7), "cannot pay twice");
        check(r.getRevenueCents() == 1800, "revenue unchanged after second attempt");
        check(r.getActiveCustomers().isEmpty(), "paid customer no longer active");
    }

    private static void paymentTiming() {
        Restaurant r = new Restaurant();
        Customer c = readyToPay(r, 1, 1, 3);
        check(!r.recordPayment(c, 3), "same turn rejected");
        check(!r.recordPayment(c, 0), "turn 0 rejected");
        check(!r.recordPayment(null, 4), "null rejected");
        check(!r.recordPayment(new RegularCustomer(1), 4), "a different object with same ID rejected");
        check(r.getRevenueCents() == 0 && r.getPaidCount() == 0, "nothing recorded");
        check(r.recordPayment(c, 4), "next turn accepted");
    }

    private static void oldestPayerOrdering() {
        Restaurant r = new Restaurant();
        Customer later = readyToPay(r, 2, 1, 5);
        Customer earlier = readyToPay(r, 1, 2, 4);
        check(r.oldestEligiblePayer(6) == earlier, "earlier ready-to-pay turn wins");

        Restaurant tie = new Restaurant();
        Customer high = readyToPay(tie, 5, 1, 4);
        Customer low = readyToPay(tie, 4, 2, 4);
        check(tie.oldestEligiblePayer(5) == low, "tie goes to lower ID");
        check(later != null && high != null, "setup ok");
    }

    private static void abandonment() {
        Restaurant r = new Restaurant();
        Customer queued = arrive(r, 1);
        Customer seated = arriveAndSeat(r, 2, 2);

        expectThrows(IllegalStateException.class, () -> r.removeAbandoned(queued), "satisfaction not zero");
        drain(queued);
        drain(seated);

        r.removeAbandoned(queued);
        check(queued.getStatus() == CustomerStatus.LEFT && r.getWaitingQueue().isEmpty(), "left queue");
        r.removeAbandoned(seated);
        check(seated.getStatus() == CustomerStatus.LEFT && r.findTable(2).isAvailable(), "table freed");
        check(r.getAbandonedCount() == 2, "abandoned count 2");
        check(r.getRevenueCents() == 0, "no revenue from abandoned customers");
        check(r.getCustomers().size() == 2, "kept in history");
        expectThrows(IllegalStateException.class, () -> r.removeAbandoned(queued), "cannot abandon twice");
    }

    private static void abandonCancelsReadyOrder() {
        Restaurant r = new Restaurant();
        Customer c = arriveAndSeat(r, 1, 1);
        Order order = placeOrder(r, c);
        order.prepareOneUnit();
        check(order.isReady(), "order READY");
        drain(c);
        r.removeAbandoned(c);
        check(order.getStatus() == OrderStatus.CANCELLED, "READY order cancelled");
        check(r.findTable(1).isAvailable(), "table freed");
    }

    private static void immutableViews() {
        Restaurant r = new Restaurant();
        arrive(r, 1);
        expectThrows(UnsupportedOperationException.class, () -> r.getWaitingQueue().clear(), "queue copy");
        expectThrows(UnsupportedOperationException.class, () -> r.getCustomers().clear(), "customers copy");
        expectThrows(UnsupportedOperationException.class, () -> r.getTables().clear(), "tables");
        expectThrows(UnsupportedOperationException.class, () -> r.getMenu().clear(), "menu");
        expectThrows(UnsupportedOperationException.class, () -> r.getOrders().add(null), "orders copy");
        expectThrows(UnsupportedOperationException.class, () -> r.getActiveCustomers().clear(), "active copy");
        check(r.getWaitingQueue().size() == 1, "queue still intact");
    }

    private static Customer arrive(Restaurant r, int id) {
        Customer c = new RegularCustomer(id);
        r.addArrival(c);
        return c;
    }

    private static Customer arriveAndSeat(Restaurant r, int id, int tableId) {
        Customer c = arrive(r, id);
        ActionResult result = r.seat(id, tableId);
        if (!result.success()) {
            throw new IllegalStateException("setup seat failed: " + result.message());
        }
        return c;
    }

    private static Order orderFor(Restaurant r, Customer c) {
        MenuItem item = c.chooseMenuItem(r.getMenu());
        return new Order(c.getId(), item, c.quotePriceCents(item));
    }

    private static Order placeOrder(Restaurant r, Customer c) {
        Order order = orderFor(r, c);
        ActionResult result = r.registerOrder(c.getId(), order);
        if (!result.success()) {
            throw new IllegalStateException("setup order failed: " + result.message());
        }
        return order;
    }

    private static Customer readyToPay(Restaurant r, int id, int tableId, int readyTurn) {
        Customer c = arriveAndSeat(r, id, tableId);
        Order order = placeOrder(r, c);
        while (order.canPrepare()) {
            order.prepareOneUnit();
        }
        r.serve(id, readyTurn - 1);
        c.finishEating(readyTurn);
        if (c.getStatus() != CustomerStatus.READY_TO_PAY) {
            throw new IllegalStateException("setup did not reach READY_TO_PAY");
        }
        return c;
    }

    private static void drain(Customer c) {
        while (c.getSatisfaction() > 0) {
            c.applyWaitingLoss();
        }
    }

    private static void run(String name, Runnable test) {
        int failuresBefore = failures.size();
        try {
            test.run();
        } catch (RuntimeException e) {
            failures.add(name + " -> threw " + e);
        }
        if (failures.size() == failuresBefore) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            System.out.println("FAIL  " + name);
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            failures.add(description);
        }
    }

    private static void checkItem(MenuItem item, String name, int cents, int units) {
        check(item.getName().equals(name) && item.getPriceCents() == cents
                && item.getPreparationUnits() == units, "menu item " + name);
    }

    private static void expectThrows(Class<? extends Throwable> type, Runnable action, String description) {
        try {
            action.run();
            failures.add(description + " (expected " + type.getSimpleName() + ")");
        } catch (Throwable t) {
            if (!type.isInstance(t)) {
                failures.add(description + " (expected " + type.getSimpleName() + ", got " + t + ")");
            }
        }
    }
}
