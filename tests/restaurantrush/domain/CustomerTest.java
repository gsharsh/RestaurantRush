package restaurantrush.domain;

import java.util.ArrayList;
import java.util.List;

/** Customer lifecycle, timing, satisfaction and shared-order handoff tests. */
public final class CustomerTest {
    private static int checks;

    private CustomerTest() {
    }

    public static void main(String[] args) {
        int before = checks;
        testCustomerStatusConstants();
        testCustomerConstruction();
        testCustomerLifecycle();
        testCustomerOrderAssignment();
        testAcceptedOrderStatuses();
        testInvalidCustomerTransitions();
        testCustomerTurnValidation();
        testFinishEatingNoOps();
        testWaitingLossBounds();
        testCustomerAbandonment();
        testM1CollaborationThroughPayment();
        testM1CollaborationAbandonment();
        testRejectedM1CollaborationHandoffs();
        System.out.println("CustomerTest: PASS (" + (checks - before) + " checks)");
    }

    private static void testCustomerConstruction() {
        expectIllegalArgument("zero customer ID", () -> new RegularCustomer(0));
        expectIllegalArgument("negative customer ID", () -> new RegularCustomer(-1));
        expectIllegalArgument("minimum integer customer ID", () -> new RegularCustomer(Integer.MIN_VALUE));
        int[] validIds = {1, 7, Integer.MAX_VALUE};
        for (int id : validIds) {
            Customer customer = new RegularCustomer(id);
            checkCustomer(customer, id, 100, CustomerStatus.WAITING, -1, -1, null);
        }
    }

    private static void testCustomerLifecycle() {
        Customer customer = new RegularCustomer(3);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        MenuItem selected = customer.chooseMenuItem(List.of(pasta));
        int quote = customer.quotePriceCents(selected);
        customer.applyWaitingLoss();
        customer.seat();
        checkCustomer(customer, 3, 92, CustomerStatus.SEATED, -1, -1, null);
        Order order = new Order(customer.getId(), selected, quote);
        customer.acceptOrder(order);
        checkCustomer(customer, 3, 92, CustomerStatus.SEATED, -1, -1, order);
        order.prepareOneUnit();
        checkCustomer(customer, 3, 92, CustomerStatus.SEATED, -1, -1, order);
        order.prepareOneUnit();
        checkCustomer(customer, 3, 92, CustomerStatus.SEATED, -1, -1, order);
        expectUnchangedIllegalState("customer service before order service", customer,
                () -> customer.markServed(40));
        order.markServed();
        customer.markServed(40);
        checkCustomer(customer, 3, 92, CustomerStatus.EATING, 40, -1, order);
        expectCustomerNoOp(customer, customer::applyWaitingLoss);
        expectCustomerNoOp(customer, () -> customer.finishEating(39));
        expectCustomerNoOp(customer, () -> customer.finishEating(40));
        customer.finishEating(41);
        checkCustomer(customer, 3, 92, CustomerStatus.READY_TO_PAY, 40, 41, order);
        expectCustomerNoOp(customer, customer::applyWaitingLoss);
        expectCustomerNoOp(customer, () -> customer.finishEating(42));
        expectUnchangedIllegalState("customer payment before order payment", customer, customer::markPaid);
        order.markPaid();
        customer.markPaid();
        checkCustomer(customer, 3, 92, CustomerStatus.PAID, 40, 41, order);
        expectCustomerNoOp(customer, customer::applyWaitingLoss);
        expectCustomerNoOp(customer, () -> customer.finishEating(Integer.MAX_VALUE));
    }

    private static void testCustomerOrderAssignment() {
        Customer customer = new RegularCustomer(3);
        Order accepted = customerOrderInState(3, OrderStatus.PLACED);
        Runnable acceptedUnchanged = snapshotOrder(accepted);
        expectUnchangedIllegalState("order assignment while waiting", customer, () -> customer.acceptOrder(accepted));
        acceptedUnchanged.run();
        customer.seat();
        expectUnchangedIllegalArgument("null accepted order", customer, () -> customer.acceptOrder(null));
        Order mismatched = customerOrderInState(4, OrderStatus.PLACED);
        Runnable mismatchedUnchanged = snapshotOrder(mismatched);
        expectUnchangedIllegalArgument("mismatched order ID", customer, () -> customer.acceptOrder(mismatched));
        mismatchedUnchanged.run();
        customer.acceptOrder(accepted);
        checkCustomer(customer, 3, 100, CustomerStatus.SEATED, -1, -1, accepted);
        Order second = customerOrderInState(3, OrderStatus.PLACED);
        Runnable secondUnchanged = snapshotOrder(second);
        expectUnchangedIllegalState("second order assignment", customer, () -> customer.acceptOrder(second));
        secondUnchanged.run();
        expectUnchangedIllegalState("same order assigned twice", customer, () -> customer.acceptOrder(accepted));
    }

    private static void testAcceptedOrderStatuses() {
        // The assignment contract does not require a PLACED order.
        for (OrderStatus status : OrderStatus.values()) {
            Customer customer = new RegularCustomer(3);
            customer.seat();
            Order order = customerOrderInState(3, status);
            Runnable orderUnchanged = snapshotOrder(order);
            customer.acceptOrder(order);
            checkCustomer(customer, 3, 100, CustomerStatus.SEATED, -1, -1, order);
            orderUnchanged.run();
            if (status != OrderStatus.SERVED) {
                expectUnchangedIllegalState("customer service with " + status + " order", customer,
                        () -> customer.markServed(1));
            }
        }
    }

    private static void testInvalidCustomerTransitions() {
        Customer customer = new RegularCustomer(3);
        Order order = customerOrderInState(3, OrderStatus.PLACED);
        expectUnchangedIllegalState("serve waiting customer", customer, () -> customer.markServed(1));
        expectUnchangedIllegalState("pay waiting customer", customer, customer::markPaid);
        customer.seat();
        expectUnchangedIllegalState("seat seated customer", customer, customer::seat);
        expectUnchangedIllegalState("serve customer without order", customer, () -> customer.markServed(1));
        expectUnchangedIllegalState("pay seated customer", customer, customer::markPaid);
        customer.acceptOrder(order);
        expectUnchangedIllegalState("serve customer with placed order", customer, () -> customer.markServed(1));
        order.prepareOneUnit();
        expectUnchangedIllegalState("serve customer with preparing order", customer, () -> customer.markServed(1));
        order.prepareOneUnit();
        expectUnchangedIllegalState("serve customer with ready order", customer, () -> customer.markServed(1));
        order.markServed();
        customer.markServed(1);
        checkInvalidCustomerActions(customer, false);
        expectUnchangedIllegalState("pay eating customer", customer, customer::markPaid);
        customer.finishEating(2);
        checkInvalidCustomerActions(customer, false);
        order.markPaid();
        customer.markPaid();
        checkInvalidCustomerActions(customer, true);

        Customer departed = new RegularCustomer(3);
        departed.abandon();
        checkInvalidCustomerActions(departed, true);
    }

    private static void testCustomerTurnValidation() {
        Customer customer = new RegularCustomer(Integer.MAX_VALUE);
        customer.seat();
        Order order = customerOrderInState(Integer.MAX_VALUE, OrderStatus.SERVED);
        customer.acceptOrder(order);
        int[] invalidTurns = {0, -1, Integer.MIN_VALUE};
        for (int turn : invalidTurns) {
            expectUnchangedIllegalArgument("invalid service turn " + turn, customer, () -> customer.markServed(turn));
        }
        customer.markServed(Integer.MAX_VALUE - 1);
        checkCustomer(customer, Integer.MAX_VALUE, 100, CustomerStatus.EATING, Integer.MAX_VALUE - 1, -1, order);
        for (int turn : invalidTurns) {
            expectUnchangedIllegalArgument("invalid eating-completion turn " + turn, customer,
                    () -> customer.finishEating(turn));
        }
        customer.finishEating(Integer.MAX_VALUE);
        checkCustomer(customer, Integer.MAX_VALUE, 100, CustomerStatus.READY_TO_PAY,
                Integer.MAX_VALUE - 1, Integer.MAX_VALUE, order);
        expectCustomerNoOp(customer, () -> customer.finishEating(Integer.MAX_VALUE));

        Customer maximumServiceTurn = new RegularCustomer(7);
        maximumServiceTurn.seat();
        Order laterOrder = customerOrderInState(7, OrderStatus.SERVED);
        maximumServiceTurn.acceptOrder(laterOrder);
        maximumServiceTurn.markServed(Integer.MAX_VALUE);
        checkCustomer(maximumServiceTurn, 7, 100, CustomerStatus.EATING, Integer.MAX_VALUE, -1, laterOrder);
        expectCustomerNoOp(maximumServiceTurn, () -> maximumServiceTurn.finishEating(Integer.MAX_VALUE));
    }

    private static void testFinishEatingNoOps() {
        Customer customer = new RegularCustomer(3);
        checkInvalidEatingTurns(customer);
        expectCustomerNoOp(customer, () -> customer.finishEating(1));
        expectCustomerNoOp(customer, () -> customer.finishEating(Integer.MAX_VALUE));
        customer.seat();
        checkInvalidEatingTurns(customer);
        expectCustomerNoOp(customer, () -> customer.finishEating(1));
        Order order = customerOrderInState(3, OrderStatus.SERVED);
        customer.acceptOrder(order);
        customer.markServed(20);
        expectCustomerNoOp(customer, () -> customer.finishEating(19));
        expectCustomerNoOp(customer, () -> customer.finishEating(20));
        customer.finishEating(21);
        checkCustomer(customer, 3, 100, CustomerStatus.READY_TO_PAY, 20, 21, order);
        checkInvalidEatingTurns(customer);
        expectCustomerNoOp(customer, () -> customer.finishEating(21));
        expectCustomerNoOp(customer, () -> customer.finishEating(22));
        expectCustomerNoOp(customer, () -> customer.finishEating(Integer.MAX_VALUE));
        order.markPaid();
        customer.markPaid();
        checkInvalidEatingTurns(customer);
        expectCustomerNoOp(customer, () -> customer.finishEating(1));
        expectCustomerNoOp(customer, () -> customer.finishEating(Integer.MAX_VALUE));
        Customer departed = new RegularCustomer(3);
        departed.abandon();
        checkInvalidEatingTurns(departed);
        expectCustomerNoOp(departed, () -> departed.finishEating(1));
        expectCustomerNoOp(departed, () -> departed.finishEating(Integer.MAX_VALUE));
    }

    private static void testWaitingLossBounds() {
        Customer waiting = new RegularCustomer(3);
        Customer seated = new RegularCustomer(3);
        seated.seat();
        Order order = customerOrderInState(3, OrderStatus.PLACED);
        seated.acceptOrder(order);
        for (int loss = 1; loss <= 14; loss++) {
            waiting.applyWaitingLoss();
            seated.applyWaitingLoss();
            int expectedSatisfaction = Math.max(0, 100 - loss * 8);
            checkCustomer(waiting, 3, expectedSatisfaction, CustomerStatus.WAITING, -1, -1, null);
            checkCustomer(seated, 3, expectedSatisfaction, CustomerStatus.SEATED, -1, -1, order);
        }
        checkOrder(order, 3, order.getItem(), OrderStatus.PLACED, 2, 1800, true, false);
        waiting.abandon();
        expectCustomerNoOp(waiting, waiting::applyWaitingLoss);
        seated.abandon();
        checkCustomer(seated, 3, 0, CustomerStatus.LEFT, -1, -1, order);
        checkOrder(order, 3, order.getItem(), OrderStatus.CANCELLED, 2, 1800, false, false);
        expectCustomerNoOp(seated, seated::applyWaitingLoss);
    }

    private static void testCustomerAbandonment() {
        Customer waiting = new RegularCustomer(3);
        waiting.abandon();
        checkCustomer(waiting, 3, 100, CustomerStatus.LEFT, -1, -1, null);
        expectCustomerNoOp(waiting, waiting::applyWaitingLoss);
        Customer seated = new RegularCustomer(3);
        seated.seat();
        seated.abandon();
        checkCustomer(seated, 3, 100, CustomerStatus.LEFT, -1, -1, null);

        for (OrderStatus status : OrderStatus.values()) {
            Customer customer = new RegularCustomer(3);
            customer.seat();
            Order order = customerOrderInState(3, status);
            MenuItem item = order.getItem();
            int remainingUnits = order.getRemainingUnits();
            Runnable orderUnchanged = snapshotOrder(order);
            customer.acceptOrder(order);
            customer.abandon();
            checkCustomer(customer, 3, 100, CustomerStatus.LEFT, -1, -1, order);
            if (status == OrderStatus.PLACED || status == OrderStatus.PREPARING || status == OrderStatus.READY) {
                checkOrder(order, 3, item, OrderStatus.CANCELLED, remainingUnits, 1800, false, false);
            } else {
                // Acceptance has no order-status restriction; abandonment cancels
                // only where applicable and leaves other attached orders unchanged.
                orderUnchanged.run();
            }
            expectUnchangedIllegalState("repeated customer abandonment", customer, customer::abandon);
            expectCustomerNoOp(customer, customer::applyWaitingLoss);
        }
    }

    private static void checkInvalidCustomerActions(Customer customer, boolean paymentInvalid) {
        Order replacement = customerOrderInState(customer.getId(), OrderStatus.PLACED);
        Runnable replacementUnchanged = snapshotOrder(replacement);
        expectUnchangedIllegalState("seat " + customer.getStatus() + " customer", customer, customer::seat);
        expectUnchangedIllegalState("assign order to " + customer.getStatus() + " customer", customer,
                () -> customer.acceptOrder(replacement));
        replacementUnchanged.run();
        expectUnchangedIllegalState("serve " + customer.getStatus() + " customer", customer,
                () -> customer.markServed(3));
        expectUnchangedIllegalState("abandon " + customer.getStatus() + " customer", customer, customer::abandon);
        if (paymentInvalid) {
            expectUnchangedIllegalState("pay " + customer.getStatus() + " customer", customer, customer::markPaid);
        }
    }

    private static void checkInvalidEatingTurns(Customer customer) {
        expectUnchangedIllegalArgument("zero eating-completion turn in " + customer.getStatus(), customer,
                () -> customer.finishEating(0));
        expectUnchangedIllegalArgument("negative eating-completion turn in " + customer.getStatus(), customer,
                () -> customer.finishEating(-1));
    }

    private static Order customerOrderInState(int id, OrderStatus status) {
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        Order order = new Order(id, pasta, 1800);
        if (status == OrderStatus.CANCELLED) {
            order.cancel();
            return order;
        }
        if (status != OrderStatus.PLACED) {
            order.prepareOneUnit();
        }
        if (status == OrderStatus.READY || status == OrderStatus.SERVED || status == OrderStatus.PAID) {
            order.prepareOneUnit();
        }
        if (status == OrderStatus.SERVED || status == OrderStatus.PAID) {
            order.markServed();
        }
        if (status == OrderStatus.PAID) {
            order.markPaid();
        }
        return order;
    }

    private static void checkCustomer(Customer customer, int id, int satisfaction, CustomerStatus status,
            int servedTurn, int readyToPayTurn, Order order) {
        check(customer.getId() == id, "customer ID: expected " + id + ", got " + customer.getId());
        check(customer.getSatisfaction() == satisfaction,
                "satisfaction: expected " + satisfaction + ", got " + customer.getSatisfaction());
        check(customer.getSatisfaction() >= 0 && customer.getSatisfaction() <= 100,
                "satisfaction must stay in range");
        check(customer.getStatus() == status,
                "customer status: expected " + status + ", got " + customer.getStatus());
        check(customer.getServedTurn() == servedTurn,
                "served turn: expected " + servedTurn + ", got " + customer.getServedTurn());
        check(customer.getReadyToPayTurn() == readyToPayTurn,
                "ready-to-pay turn: expected " + readyToPayTurn + ", got " + customer.getReadyToPayTurn());
        check(customer.getOrder() == order, "customer must retain the accepted Order instance");
    }

    private static Runnable snapshotOrder(Order order) {
        int id = order.getId();
        MenuItem item = order.getItem();
        OrderStatus status = order.getStatus();
        int remainingUnits = order.getRemainingUnits();
        int billCents = order.getBillCents();
        boolean canPrepare = order.canPrepare();
        boolean ready = order.isReady();
        return () -> checkOrder(order, id, item, status, remainingUnits, billCents, canPrepare, ready);
    }

    private static Runnable snapshotCustomer(Customer customer) {
        int id = customer.getId();
        int satisfaction = customer.getSatisfaction();
        CustomerStatus status = customer.getStatus();
        int servedTurn = customer.getServedTurn();
        int readyToPayTurn = customer.getReadyToPayTurn();
        Order order = customer.getOrder();
        Runnable orderUnchanged = order == null ? () -> { } : snapshotOrder(order);
        return () -> {
            checkCustomer(customer, id, satisfaction, status, servedTurn, readyToPayTurn, order);
            orderUnchanged.run();
        };
    }

    private static void expectUnchangedIllegalArgument(String description, Customer customer, Runnable action) {
        Runnable unchanged = snapshotCustomer(customer);
        expectIllegalArgument(description, action);
        unchanged.run();
    }

    private static void expectUnchangedIllegalState(String description, Customer customer, Runnable action) {
        Runnable unchanged = snapshotCustomer(customer);
        boolean rejected = false;
        try {
            action.run();
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        check(rejected, description + ": expected IllegalStateException");
        unchanged.run();
    }

    private static void expectCustomerNoOp(Customer customer, Runnable action) {
        Runnable unchanged = snapshotCustomer(customer);
        action.run();
        unchanged.run();
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

    private static void testCustomerStatusConstants() {
        checkEnumConstants(CustomerStatus.values(),
                "WAITING", "SEATED", "EATING", "READY_TO_PAY", "PAID", "LEFT");
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

    private static void testM1CollaborationThroughPayment() {
        checkM1CollaborationThroughPayment(new RegularCustomer(3), "Regular", 1800, 8,
                new int[] {92, 84, 76});
        checkM1CollaborationThroughPayment(new VIPCustomer(6), "VIP", 1620, 5,
                new int[] {95, 90, 85});
    }

    private static void checkM1CollaborationThroughPayment(Customer customer, String expectedLabel,
            int expectedBill, int expectedLoss, int[] expectedSatisfaction) {
        List<MenuItem> menu = agreedMenuFixture();
        Runnable menuUnchanged = snapshotMenu(menu);
        int id = customer.getId();
        check(expectedLabel.equals(customer.typeLabel()), "customer label through the shared contract");
        check(customer.waitingLossPerTurn() == expectedLoss, "waiting-loss rule through the shared contract");
        customer.seat();
        MenuItem selected = customer.chooseMenuItem(menu);
        check(selected == menu.get(2), "collaboration must use the shared Pasta offering");
        int quote = customer.quotePriceCents(selected);
        check(quote == expectedBill, "collaboration must use the customer's quoted bill");
        Order order = new Order(id, selected, quote);
        customer.acceptOrder(order);
        checkCustomer(customer, id, 100, CustomerStatus.SEATED, -1, -1, order);
        checkOrder(order, id, selected, OrderStatus.PLACED, 2, expectedBill, true, false);

        customer.applyWaitingLoss();
        order.prepareOneUnit();
        checkCustomer(customer, id, expectedSatisfaction[0], CustomerStatus.SEATED, -1, -1, order);
        checkOrder(order, id, selected, OrderStatus.PREPARING, 1, expectedBill, true, false);
        customer.applyWaitingLoss();
        order.prepareOneUnit();
        checkCustomer(customer, id, expectedSatisfaction[1], CustomerStatus.SEATED, -1, -1, order);
        checkOrder(order, id, selected, OrderStatus.READY, 0, expectedBill, false, true);
        customer.applyWaitingLoss();
        int servedSatisfaction = expectedSatisfaction[2];
        checkCustomer(customer, id, servedSatisfaction, CustomerStatus.SEATED, -1, -1, order);
        checkOrder(order, id, selected, OrderStatus.READY, 0, expectedBill, false, true);
        expectUnchangedIllegalState("collaboration customer service before order service", customer,
                () -> customer.markServed(9));
        menuUnchanged.run();

        order.markServed();
        checkCustomer(customer, id, servedSatisfaction, CustomerStatus.SEATED, -1, -1, order);
        checkOrder(order, id, selected, OrderStatus.SERVED, 0, expectedBill, false, false);
        customer.markServed(9);
        checkCustomer(customer, id, servedSatisfaction, CustomerStatus.EATING, 9, -1, order);
        expectCustomerNoOp(customer, customer::applyWaitingLoss);
        expectUnchangedIllegalState("collaboration payment before eating completes", customer, customer::markPaid);
        expectCustomerNoOp(customer, () -> customer.finishEating(9));
        customer.finishEating(10);
        checkCustomer(customer, id, servedSatisfaction, CustomerStatus.READY_TO_PAY, 9, 10, order);
        expectCustomerNoOp(customer, customer::applyWaitingLoss);
        expectUnchangedIllegalState("collaboration customer payment before order payment", customer,
                customer::markPaid);
        menuUnchanged.run();
        // Entity collaboration only: Restaurant owns payer eligibility, accounting and table release.
        order.markPaid();
        checkCustomer(customer, id, servedSatisfaction, CustomerStatus.READY_TO_PAY, 9, 10, order);
        customer.markPaid();
        checkCustomer(customer, id, servedSatisfaction, CustomerStatus.PAID, 9, 10, order);
        checkOrder(order, id, selected, OrderStatus.PAID, 0, expectedBill, false, false);
        expectCustomerNoOp(customer, customer::applyWaitingLoss);
        menuUnchanged.run();
    }

    private static void testM1CollaborationAbandonment() {
        List<MenuItem> menu = agreedMenuFixture();
        Runnable menuUnchanged = snapshotMenu(menu);
        OrderStatus[] sources = {OrderStatus.PLACED, OrderStatus.PREPARING, OrderStatus.READY};
        for (int prepared = 0; prepared < sources.length; prepared++) {
            checkM1CollaborationAbandonment(new RegularCustomer(6), menu, prepared, sources[prepared], 92, 1800);
            menuUnchanged.run();
        }
        checkM1CollaborationAbandonment(new VIPCustomer(3), menu, 2, OrderStatus.READY, 95, 1620);
        menuUnchanged.run();
    }

    private static void checkM1CollaborationAbandonment(Customer customer, List<MenuItem> menu,
            int prepared, OrderStatus expectedStatus, int expectedSatisfaction, int expectedBill) {
        int id = customer.getId();
        customer.seat();
        MenuItem selected = customer.chooseMenuItem(menu);
        check(selected == menu.get(2), "abandonment must retain the selected shared offering");
        int quote = customer.quotePriceCents(selected);
        check(quote == expectedBill, "abandonment must retain the customer's quoted bill");
        Order order = new Order(id, selected, quote);
        customer.acceptOrder(order);
        for (int unit = 0; unit < prepared; unit++) {
            order.prepareOneUnit();
        }
        customer.applyWaitingLoss();
        checkCustomer(customer, id, expectedSatisfaction, CustomerStatus.SEATED, -1, -1, order);
        checkOrder(order, id, selected, expectedStatus, 2 - prepared, expectedBill,
                prepared < 2, prepared == 2);
        // Explicit entity departure; Restaurant's automatic zero-satisfaction cleanup is separate.
        customer.abandon();
        checkCustomer(customer, id, expectedSatisfaction, CustomerStatus.LEFT, -1, -1, order);
        checkOrder(order, id, selected, OrderStatus.CANCELLED, 2 - prepared, expectedBill, false, false);
        expectCustomerNoOp(customer, customer::applyWaitingLoss);
        expectUnchangedIllegalState("collaboration preparation after abandonment", customer, order::prepareOneUnit);
    }

    private static void testRejectedM1CollaborationHandoffs() {
        checkRejectedM1CollaborationHandoffs(new RegularCustomer(3));
        checkRejectedM1CollaborationHandoffs(new VIPCustomer(6));
    }

    private static void checkRejectedM1CollaborationHandoffs(Customer customer) {
        List<MenuItem> menu = agreedMenuFixture();
        Runnable menuUnchanged = snapshotMenu(menu);
        customer.seat();
        MenuItem selected = customer.chooseMenuItem(menu);
        int quote = customer.quotePriceCents(selected);
        Order mismatched = new Order(4, selected, quote);
        Runnable mismatchedUnchanged = snapshotOrder(mismatched);
        expectUnchangedIllegalArgument("collaboration mismatched order handoff", customer,
                () -> customer.acceptOrder(mismatched));
        mismatchedUnchanged.run();
        menuUnchanged.run();

        Order accepted = new Order(customer.getId(), selected, quote);
        customer.acceptOrder(accepted);
        Order replacement = new Order(customer.getId(), selected, quote);
        Runnable replacementUnchanged = snapshotOrder(replacement);
        expectUnchangedIllegalState("collaboration replacement order handoff", customer,
                () -> customer.acceptOrder(replacement));
        replacementUnchanged.run();
        mismatchedUnchanged.run();
        menuUnchanged.run();
        expectUnchangedIllegalState("collaboration payment of an unserved associated order", customer,
                accepted::markPaid);
        replacementUnchanged.run();
        mismatchedUnchanged.run();
        menuUnchanged.run();
    }

    private static List<MenuItem> agreedMenuFixture() {
        return new ArrayList<>(List.of(
                new MenuItem("Salad", 900, 1),
                new MenuItem("Burger", 1500, 1),
                new MenuItem("Pasta", 1800, 2)));
    }

    private static Runnable snapshotMenu(List<MenuItem> menu) {
        List<MenuItem> items = List.copyOf(menu);
        List<Runnable> offeringChecks = new ArrayList<>();
        for (MenuItem item : items) {
            String name = item.getName();
            int priceCents = item.getPriceCents();
            int preparationUnits = item.getPreparationUnits();
            offeringChecks.add(() -> checkMenuItem(item, name, priceCents, preparationUnits));
        }
        return () -> {
            check(menu.size() == items.size(), "collaboration must preserve menu size");
            for (int index = 0; index < items.size(); index++) {
                check(menu.get(index) == items.get(index), "collaboration must preserve menu item identity/order");
                offeringChecks.get(index).run();
            }
        };
    }
}
