package restaurantrush.domain;

import java.util.ArrayList;
import java.util.List;

/** Offline tests for M1's domain types; add later feature tests to main. */
public final class M1DomainTests {
    private static int checks;

    private M1DomainTests() {
    }

    public static void main(String[] args) {
        testAgreedMenuItems();
        testInvalidNames();
        testDisplayNamePreserved();
        testNonnegativePrices();
        testPositivePreparationUnits();
        testCustomerStatusConstants();
        testOrderStatusConstants();
        testInvalidOrderConstruction();
        testOneUnitOrderLifecycle();
        testTwoUnitOrderLifecycle();
        testOrderCancellation();
        testInvalidOrderTransitions();
        testLockedQuoteStorage();
        testSharedMenuItemAndIndependentProgress();
        testCustomerConstruction();
        testRegularCustomerRules();
        testCustomerRuleValidation();
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
        System.out.println("M1DomainTests: PASS (" + checks + " checks)");
    }

    private static void testAgreedMenuItems() {
        checkMenuItem(new MenuItem("Salad", 900, 1), "Salad", 900, 1);
        checkMenuItem(new MenuItem("Burger", 1500, 1), "Burger", 1500, 1);
        checkMenuItem(new MenuItem("Pasta", 1800, 2), "Pasta", 1800, 2);
    }

    private static void testInvalidNames() {
        expectIllegalArgument("null name", () -> new MenuItem(null, 900, 1));
        expectIllegalArgument("empty name", () -> new MenuItem("", 900, 1));
        expectIllegalArgument("space-only name", () -> new MenuItem("   ", 900, 1));
        expectIllegalArgument("mixed whitespace name", () -> new MenuItem(" \t\r\n ", 900, 1));
        expectIllegalArgument("Unicode whitespace name", () -> new MenuItem("\u2003", 900, 1));
    }

    private static void testDisplayNamePreserved() {
        String displayName = "  House Salad \t";
        checkMenuItem(new MenuItem(displayName, 900, 1), displayName, 900, 1);
    }

    private static void testNonnegativePrices() {
        expectIllegalArgument("negative price", () -> new MenuItem("Salad", -1, 1));
        expectIllegalArgument("minimum integer price", () -> new MenuItem("Salad", Integer.MIN_VALUE, 1));
        checkMenuItem(new MenuItem("Salad", 0, 1), "Salad", 0, 1);
    }

    private static void testPositivePreparationUnits() {
        expectIllegalArgument("zero preparation units", () -> new MenuItem("Pasta", 1800, 0));
        expectIllegalArgument("negative preparation units", () -> new MenuItem("Pasta", 1800, -1));
        expectIllegalArgument("minimum integer preparation units",
                () -> new MenuItem("Pasta", 1800, Integer.MIN_VALUE));
    }

    private static void testCustomerStatusConstants() {
        checkEnumConstants(CustomerStatus.values(),
                "WAITING", "SEATED", "EATING", "READY_TO_PAY", "PAID", "LEFT");
    }

    private static void testOrderStatusConstants() {
        checkEnumConstants(OrderStatus.values(),
                "PLACED", "PREPARING", "READY", "SERVED", "PAID", "CANCELLED");
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

    private static void testRegularCustomerRules() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        List<MenuItem> menu = new ArrayList<>(List.of(salad, burger, pasta));
        MenuItem[] expectedSelections = {salad, burger, pasta, salad, burger, pasta};
        for (int id = 1; id <= expectedSelections.length; id++) {
            Customer customer = new RegularCustomer(id);
            MenuItem selected = customer.chooseMenuItem(menu);
            check(selected == expectedSelections[id - 1], "selected object identity for customer " + id);
            check(customer.quotePriceCents(selected) == selected.getPriceCents(),
                    "regular customer must quote the full selected price");
            check(customer.waitingLossPerTurn() == 8, "regular waiting-loss rule through Customer");
            customer.applyWaitingLoss();
            checkCustomer(customer, id, 92, CustomerStatus.WAITING, -1, -1, null);
            check(menu.size() == 3, "selection must preserve menu size");
            check(menu.get(0) == salad && menu.get(1) == burger && menu.get(2) == pasta,
                    "selection must preserve menu order and object identities");
        }
        Customer laterCustomer = new RegularCustomer(7);
        check(laterCustomer.chooseMenuItem(List.of(salad, burger, pasta)) == salad,
                "selection must work with an immutable menu and IDs beyond the schedule");
        Customer maximumIdCustomer = new RegularCustomer(Integer.MAX_VALUE);
        check(maximumIdCustomer.chooseMenuItem(menu) == salad, "maximum positive ID selection");
        check(laterCustomer.quotePriceCents(new MenuItem("Salad", 0, 1)) == 0,
                "zero menu price must be quoted unchanged");
    }

    private static void testCustomerRuleValidation() {
        Customer customer = new RegularCustomer(1);
        expectUnchangedIllegalArgument("null menu", customer, () -> customer.chooseMenuItem(null));
        expectUnchangedIllegalArgument("empty menu", customer, () -> customer.chooseMenuItem(List.of()));
        expectUnchangedIllegalArgument("null quoted item", customer, () -> customer.quotePriceCents(null));
        List<MenuItem> menu = new ArrayList<>();
        menu.add(null);
        expectUnchangedIllegalArgument("null selected menu entry", customer, () -> customer.chooseMenuItem(menu));
        check(menu.size() == 1 && menu.get(0) == null, "rejected selection must preserve the menu");
        MenuItem salad = new MenuItem("Salad", 900, 1);
        menu.add(0, salad);
        check(customer.chooseMenuItem(menu) == salad, "an unused null entry does not invalidate a selected item");
        check(menu.size() == 2 && menu.get(0) == salad && menu.get(1) == null,
                "selection must preserve a menu containing an unused null entry");
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

    private static void checkInvalidEatingTurns(Customer customer) {
        expectUnchangedIllegalArgument("zero eating-completion turn in " + customer.getStatus(), customer,
                () -> customer.finishEating(0));
        expectUnchangedIllegalArgument("negative eating-completion turn in " + customer.getStatus(), customer,
                () -> customer.finishEating(-1));
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

    private static void testM1CollaborationThroughPayment() {
        List<MenuItem> menu = agreedMenuFixture();
        Runnable menuUnchanged = snapshotMenu(menu);
        Customer customer = new RegularCustomer(3);
        customer.seat();
        MenuItem selected = customer.chooseMenuItem(menu);
        check(selected == menu.get(2), "collaboration must use the shared Pasta offering");
        int quote = customer.quotePriceCents(selected);
        check(quote == 1800, "collaboration must use the customer's regular quote");
        Order order = new Order(customer.getId(), selected, quote);
        customer.acceptOrder(order);
        checkCustomer(customer, 3, 100, CustomerStatus.SEATED, -1, -1, order);
        checkOrder(order, 3, selected, OrderStatus.PLACED, 2, quote, true, false);

        customer.applyWaitingLoss();
        order.prepareOneUnit();
        checkCustomer(customer, 3, 92, CustomerStatus.SEATED, -1, -1, order);
        checkOrder(order, 3, selected, OrderStatus.PREPARING, 1, quote, true, false);
        customer.applyWaitingLoss();
        order.prepareOneUnit();
        checkCustomer(customer, 3, 84, CustomerStatus.SEATED, -1, -1, order);
        checkOrder(order, 3, selected, OrderStatus.READY, 0, quote, false, true);
        customer.applyWaitingLoss();
        checkCustomer(customer, 3, 76, CustomerStatus.SEATED, -1, -1, order);
        checkOrder(order, 3, selected, OrderStatus.READY, 0, quote, false, true);
        expectUnchangedIllegalState("collaboration customer service before order service", customer,
                () -> customer.markServed(9));
        menuUnchanged.run();

        order.markServed();
        checkCustomer(customer, 3, 76, CustomerStatus.SEATED, -1, -1, order);
        customer.markServed(9);
        checkCustomer(customer, 3, 76, CustomerStatus.EATING, 9, -1, order);
        expectCustomerNoOp(customer, () -> customer.finishEating(9));
        customer.finishEating(10);
        checkCustomer(customer, 3, 76, CustomerStatus.READY_TO_PAY, 9, 10, order);
        expectUnchangedIllegalState("collaboration customer payment before order payment", customer,
                customer::markPaid);
        menuUnchanged.run();
        order.markPaid();
        checkCustomer(customer, 3, 76, CustomerStatus.READY_TO_PAY, 9, 10, order);
        customer.markPaid();
        checkCustomer(customer, 3, 76, CustomerStatus.PAID, 9, 10, order);
        checkOrder(order, 3, selected, OrderStatus.PAID, 0, quote, false, false);
        menuUnchanged.run();
    }

    private static void testM1CollaborationAbandonment() {
        List<MenuItem> menu = agreedMenuFixture();
        Runnable menuUnchanged = snapshotMenu(menu);
        OrderStatus[] sources = {OrderStatus.PLACED, OrderStatus.PREPARING, OrderStatus.READY};
        for (int prepared = 0; prepared < sources.length; prepared++) {
            Customer customer = new RegularCustomer(6);
            customer.seat();
            MenuItem selected = customer.chooseMenuItem(menu);
            check(selected == menu.get(2), "abandonment must retain the selected shared offering");
            int quote = customer.quotePriceCents(selected);
            Order order = new Order(customer.getId(), selected, quote);
            customer.acceptOrder(order);
            for (int unit = 0; unit < prepared; unit++) {
                order.prepareOneUnit();
            }
            customer.applyWaitingLoss();
            checkCustomer(customer, 6, 92, CustomerStatus.SEATED, -1, -1, order);
            checkOrder(order, 6, selected, sources[prepared], 2 - prepared, 1800,
                    prepared < 2, prepared == 2);
            customer.abandon();
            checkCustomer(customer, 6, 92, CustomerStatus.LEFT, -1, -1, order);
            checkOrder(order, 6, selected, OrderStatus.CANCELLED, 2 - prepared, 1800, false, false);
            expectUnchangedIllegalState("collaboration preparation after abandonment", customer,
                    order::prepareOneUnit);
            menuUnchanged.run();
        }
    }

    private static void testRejectedM1CollaborationHandoffs() {
        List<MenuItem> menu = agreedMenuFixture();
        Runnable menuUnchanged = snapshotMenu(menu);
        Customer customer = new RegularCustomer(3);
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

    private static void checkMenuItem(MenuItem item, String name, int priceCents, int preparationUnits) {
        check(name.equals(item.getName()), "display name: expected [" + name + "], got [" + item.getName() + "]");
        check(item.getPriceCents() == priceCents,
                name + " price: expected " + priceCents + ", got " + item.getPriceCents());
        check(item.getPreparationUnits() == preparationUnits,
                name + " preparation units: expected " + preparationUnits + ", got " + item.getPreparationUnits());
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
