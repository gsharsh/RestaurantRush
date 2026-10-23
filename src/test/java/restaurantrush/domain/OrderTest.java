package restaurantrush.domain;

/** Offline checks for order preparation and lifecycle behavior. */
public final class OrderTest {
    private static int checks;

    private OrderTest() {
    }

    public static void main(String[] args) {
        int before = checks;
        testInvalidOrderConstruction();
        testOneUnitOrderLifecycle();
        testTwoUnitOrderLifecycle();
        testOrderCancellation();
        testInvalidOrderTransitions();
        testLockedQuoteStorage();
        testSharedMenuItemAndIndependentProgress();
        testOrderStatusConstants();
        System.out.println("OrderTest: PASS (" + (checks - before) + " checks)");
    }

    private static void testInvalidOrderConstruction() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        expectIllegalArgument("zero order ID", () -> new Order(0, salad, 900));
        expectIllegalArgument("negative order ID", () -> new Order(-1, salad, 900));
        expectIllegalArgument("null order item", () -> new Order(1, null, 900));
        expectIllegalArgument("negative locked bill", () -> new Order(1, salad, -1));
        // MenuItem's constructor and final getter prevent an invalid unit count
        // through the public API; testPositivePreparationUnits covers that boundary.
    }

    private static void testOneUnitOrderLifecycle() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        Order order = new Order(1, salad, 900);
        checkOrder(order, 1, salad, OrderStatus.PLACED, 1, 900, true, false);
        order.prepareOneUnit();
        checkOrder(order, 1, salad, OrderStatus.READY, 0, 900, false, true);
        order.markServed();
        checkOrder(order, 1, salad, OrderStatus.SERVED, 0, 900, false, false);
        order.markPaid();
        checkOrder(order, 1, salad, OrderStatus.PAID, 0, 900, false, false);
    }

    private static void testTwoUnitOrderLifecycle() {
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        Order order = new Order(3, pasta, 1800);
        checkOrder(order, 3, pasta, OrderStatus.PLACED, 2, 1800, true, false);
        order.prepareOneUnit();
        checkOrder(order, 3, pasta, OrderStatus.PREPARING, 1, 1800, true, false);
        order.prepareOneUnit();
        checkOrder(order, 3, pasta, OrderStatus.READY, 0, 1800, false, true);
        order.markServed();
        checkOrder(order, 3, pasta, OrderStatus.SERVED, 0, 1800, false, false);
        order.markPaid();
        checkOrder(order, 3, pasta, OrderStatus.PAID, 0, 1800, false, false);
    }

    private static void testOrderCancellation() {
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        OrderStatus[] sources = {OrderStatus.PLACED, OrderStatus.PREPARING, OrderStatus.READY};
        for (int prepared = 0; prepared < sources.length; prepared++) {
            Order order = new Order(3, pasta, 1800);
            for (int unit = 0; unit < prepared; unit++) {
                order.prepareOneUnit();
            }
            checkOrder(order, 3, pasta, sources[prepared], 2 - prepared, 1800,
                    prepared < 2, prepared == 2);
            order.cancel();
            checkOrder(order, 3, pasta, OrderStatus.CANCELLED, 2 - prepared, 1800, false, false);
            expectUnchangedIllegalState("prepare cancelled order", order, order::prepareOneUnit);
            expectUnchangedIllegalState("serve cancelled order", order, order::markServed);
            expectUnchangedIllegalState("pay cancelled order", order, order::markPaid);
            expectUnchangedIllegalState("cancel cancelled order", order, order::cancel);
        }
    }

    private static void testInvalidOrderTransitions() {
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        Order order = new Order(3, pasta, 1800);
        expectUnchangedIllegalState("serve placed order", order, order::markServed);
        expectUnchangedIllegalState("pay placed order", order, order::markPaid);

        order.prepareOneUnit();
        expectUnchangedIllegalState("serve preparing order", order, order::markServed);
        expectUnchangedIllegalState("pay preparing order", order, order::markPaid);

        order.prepareOneUnit();
        expectUnchangedIllegalState("prepare ready order", order, order::prepareOneUnit);
        expectUnchangedIllegalState("pay ready order", order, order::markPaid);

        order.markServed();
        expectUnchangedIllegalState("prepare served order", order, order::prepareOneUnit);
        expectUnchangedIllegalState("serve served order", order, order::markServed);
        expectUnchangedIllegalState("cancel served order", order, order::cancel);

        order.markPaid();
        expectUnchangedIllegalState("prepare paid order", order, order::prepareOneUnit);
        expectUnchangedIllegalState("serve paid order", order, order::markServed);
        expectUnchangedIllegalState("pay paid order", order, order::markPaid);
        expectUnchangedIllegalState("cancel paid order", order, order::cancel);
    }

    private static void testLockedQuoteStorage() {
        // Constructor-storage tests only: Stage 1 Waiter must later pass exactly
        // customer.quotePriceCents(item), the full menu price for RegularCustomer.
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        int[] explicitQuotes = {725, 0};
        for (int quote : explicitQuotes) {
            Order order = new Order(3, pasta, quote);
            checkOrder(order, 3, pasta, OrderStatus.PLACED, 2, quote, true, false);
            order.prepareOneUnit();
            checkOrder(order, 3, pasta, OrderStatus.PREPARING, 1, quote, true, false);
            order.prepareOneUnit();
            checkOrder(order, 3, pasta, OrderStatus.READY, 0, quote, false, true);
            order.markServed();
            checkOrder(order, 3, pasta, OrderStatus.SERVED, 0, quote, false, false);
            order.markPaid();
            checkOrder(order, 3, pasta, OrderStatus.PAID, 0, quote, false, false);

            Order cancelled = new Order(3, pasta, quote);
            cancelled.prepareOneUnit();
            cancelled.prepareOneUnit();
            cancelled.cancel();
            checkOrder(cancelled, 3, pasta, OrderStatus.CANCELLED, 0, quote, false, false);
        }
    }

    private static void testSharedMenuItemAndIndependentProgress() {
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        Order first = new Order(3, pasta, 1800);
        Order second = new Order(6, pasta, 1800);
        first.prepareOneUnit();
        checkOrder(first, 3, pasta, OrderStatus.PREPARING, 1, 1800, true, false);
        checkOrder(second, 6, pasta, OrderStatus.PLACED, 2, 1800, true, false);

        second.prepareOneUnit();
        second.prepareOneUnit();
        checkOrder(second, 6, pasta, OrderStatus.READY, 0, 1800, false, true);
        checkOrder(first, 3, pasta, OrderStatus.PREPARING, 1, 1800, true, false);
        checkMenuItem(pasta, "Pasta", 1800, 2);
    }

    private static void expectUnchangedIllegalState(String description, Order order, Runnable action) {
        int id = order.getId();
        MenuItem item = order.getItem();
        OrderStatus status = order.getStatus();
        int remainingUnits = order.getRemainingUnits();
        int billCents = order.getBillCents();
        boolean canPrepare = order.canPrepare();
        boolean ready = order.isReady();
        boolean rejected = false;
        try {
            action.run();
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        check(rejected, description + ": expected IllegalStateException");
        checkOrder(order, id, item, status, remainingUnits, billCents, canPrepare, ready);
    }

    private static void testOrderStatusConstants() {
        checkEnumConstants(OrderStatus.values(),
                "PLACED", "PREPARING", "READY", "SERVED", "PAID", "CANCELLED");
    }

    private static void checkEnumConstants(Enum<?>[] actual, String... expected) {
        check(actual.length == expected.length,
                "enum constant count: expected " + expected.length + ", got " + actual.length);
        for (int index = 0; index < expected.length; index++) {
            check(expected[index].equals(actual[index].name()),
                    "enum constant at index " + index + ": expected " + expected[index]
                            + ", got " + actual[index].name());
        }
    }

    private static void checkOrder(Order order, int id, MenuItem item, OrderStatus status,
            int remainingUnits, int billCents, boolean canPrepare, boolean ready) {
        check(order.getId() == id, "order ID: expected " + id + ", got " + order.getId());
        check(order.getItem() == item, "order must retain the same MenuItem instance");
        check(order.getStatus() == status,
                "order status: expected " + status + ", got " + order.getStatus());
        check(order.getRemainingUnits() == remainingUnits,
                "remaining units: expected " + remainingUnits + ", got " + order.getRemainingUnits());
        check(order.getRemainingUnits() >= 0, "remaining units must never be negative");
        check(order.getBillCents() == billCents,
                "locked bill: expected " + billCents + ", got " + order.getBillCents());
        check(order.canPrepare() == canPrepare, "canPrepare in " + status + ": expected " + canPrepare);
        check(order.isReady() == ready, "isReady in " + status + ": expected " + ready);
    }

    private static void checkMenuItem(MenuItem item, String name, int priceCents, int preparationUnits) {
        check(name.equals(item.getName()), "display name: expected [" + name + "], got [" + item.getName() + "]");
        check(item.getPriceCents() == priceCents,
                name + " price: expected " + priceCents + ", got " + item.getPriceCents());
        check(item.getPreparationUnits() == preparationUnits,
                name + " preparation units: expected " + preparationUnits + ", got " + item.getPreparationUnits());
    }

    private static void expectIllegalArgument(String description, Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError(description + ": expected IllegalArgumentException");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }
}
