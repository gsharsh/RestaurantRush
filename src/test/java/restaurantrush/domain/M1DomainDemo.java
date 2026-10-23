package restaurantrush.domain;

import java.util.List;

/** A checked, test-only domain walkthrough, independent of game coordination. */
public final class M1DomainDemo {
    private static int checks;

    private M1DomainDemo() {
    }

    public static void main(String[] args) {
        List<MenuItem> menu = List.of(
                new MenuItem("Salad", 900, 1),
                new MenuItem("Burger", 1500, 1),
                new MenuItem("Pasta", 1800, 2));
        Customer customer = new RegularCustomer(3);
        checkMenu(menu);

        // Turn 7: inherited lifecycle operations through the abstract base type.
        customer.seat();
        customer.applyWaitingLoss();
        checkCustomer(customer, CustomerStatus.SEATED, 92, -1, -1);
        check(customer.getOrder() == null, "No order before selection and acceptance");
        printTurn(7, customer, null);

        // Turn 8: runtime dispatch selects and quotes the existing menu offering.
        MenuItem selected = customer.chooseMenuItem(menu);
        int quote = customer.quotePriceCents(selected);
        check(selected == menu.get(2), "Customer 3 must select the existing Pasta instance");
        check(quote == 1800, "RegularCustomer must quote 1800 cents for Pasta");
        Order order = new Order(customer.getId(), selected, quote);
        customer.acceptOrder(order);
        check(customer.getOrder() == order, "Customer must accept the exact Order instance");
        check(order.getItem() == selected, "Order must retain the selected MenuItem instance");
        checkOrder(order, OrderStatus.PLACED, 2);
        order.prepareOneUnit();
        checkOrder(order, OrderStatus.PREPARING, 1);
        checkCustomer(customer, CustomerStatus.SEATED, 92, -1, -1);
        customer.applyWaitingLoss();
        checkCustomer(customer, CustomerStatus.SEATED, 84, -1, -1);
        checkOrder(order, OrderStatus.PREPARING, 1);
        printTurn(8, customer, order);
        expectRejected("serve unfinished order", customer, order, menu, order::markServed);

        // Turn 9: READY food is still unserved, so the seated customer loses 8.
        order.prepareOneUnit();
        checkOrder(order, OrderStatus.READY, 0);
        checkCustomer(customer, CustomerStatus.SEATED, 84, -1, -1);
        customer.applyWaitingLoss();
        checkCustomer(customer, CustomerStatus.SEATED, 76, -1, -1);
        checkOrder(order, OrderStatus.READY, 0);
        printTurn(9, customer, order);
        expectRejected("prepare READY order", customer, order, menu, order::prepareOneUnit);
        expectRejected("serve customer before order", customer, order, menu, () -> customer.markServed(9));

        // Turn 10: the Order transition must precede the Customer transition.
        order.markServed();
        checkOrder(order, OrderStatus.SERVED, 0);
        checkCustomer(customer, CustomerStatus.SEATED, 76, -1, -1);
        customer.markServed(10);
        customer.finishEating(10);
        customer.applyWaitingLoss();
        checkCustomer(customer, CustomerStatus.EATING, 76, 10, -1);
        checkOrder(order, OrderStatus.SERVED, 0);
        printTurn(10, customer, order);

        // Turn 11: eating finishes later; another call preserves the timestamp.
        customer.finishEating(11);
        customer.finishEating(11);
        customer.applyWaitingLoss();
        checkCustomer(customer, CustomerStatus.READY_TO_PAY, 76, 10, 11);
        checkOrder(order, OrderStatus.SERVED, 0);
        printTurn(11, customer, order);
        expectRejected("pay customer before order", customer, order, menu, customer::markPaid);

        // Turn 12: simulate payment whose eligibility was already validated.
        // Cashier selection, revenue and per-turn limits are outside this demo.
        order.markPaid();
        checkOrder(order, OrderStatus.PAID, 0);
        checkCustomer(customer, CustomerStatus.READY_TO_PAY, 76, 10, 11);
        customer.markPaid();
        customer.applyWaitingLoss();
        checkCustomer(customer, CustomerStatus.PAID, 76, 10, 11);
        checkOrder(order, OrderStatus.PAID, 0);
        printTurn(12, customer, order);
        expectRejected("repeat order payment", customer, order, menu, order::markPaid);
        expectRejected("repeat customer payment", customer, order, menu, customer::markPaid);

        check(customer.getOrder() == order, "Shared Order identity must survive the whole timeline");
        check(order.getItem() == selected && selected == menu.get(2), "Shared menu item identity must survive");
        checkMenu(menu);
        System.out.println("M1DomainDemo: PASS (" + checks + " checks)");
        System.out.println("OOP: Customer abstracts the rules; chooseMenuItem and quotePriceCents dispatch to"
                + " RegularCustomer. Inherited seat/applyWaitingLoss methods reuse the guarded lifecycle;");
        System.out.println("applyWaitingLoss dispatches waitingLossPerTurn. prepareOneUnit/markServed/markPaid"
                + " encapsulate state changes. customer.getOrder() == order; order.getItem() == selected.");
        System.out.println("Scope: direct domain calls do not verify arrivals, staff limits, cashier selection,"
                + " payment eligibility, queue/table cleanup, revenue or complete-game correctness.");
    }

    private static void checkCustomer(Customer customer, CustomerStatus status, int satisfaction,
            int servedTurn, int readyToPayTurn) {
        check(customer.getId() == 3, "Customer identity must remain 3");
        check(customer.getStatus() == status, "Expected customer status " + status);
        check(customer.getSatisfaction() == satisfaction, "Expected satisfaction " + satisfaction);
        check(customer.getServedTurn() == servedTurn, "Unexpected served turn");
        check(customer.getReadyToPayTurn() == readyToPayTurn, "Unexpected ready-to-pay turn");
    }

    private static void checkOrder(Order order, OrderStatus status, int remainingUnits) {
        check(order.getId() == 3, "Order identity must match customer 3");
        check(order.getStatus() == status, "Expected order status " + status);
        check(order.getRemainingUnits() == remainingUnits, "Unexpected remaining preparation units");
        check(order.getBillCents() == 1800, "The bill must remain locked at 1800 cents");
        check(order.canPrepare() == (status == OrderStatus.PLACED || status == OrderStatus.PREPARING),
                "Unexpected preparation availability");
        check(order.isReady() == (status == OrderStatus.READY), "Readiness must follow order status");
    }

    private static void checkMenu(List<MenuItem> menu) {
        check(menu.size() == 3, "The menu must contain the three agreed offerings");
        String[] names = {"Salad", "Burger", "Pasta"};
        int[] prices = {900, 1500, 1800};
        int[] units = {1, 1, 2};
        for (int index = 0; index < menu.size(); index++) {
            MenuItem item = menu.get(index);
            check(item.getName().equals(names[index]), "Unexpected menu item name");
            check(item.getPriceCents() == prices[index], "Unexpected menu item price");
            check(item.getPreparationUnits() == units[index], "Unexpected menu item preparation units");
        }
    }

    private static void expectRejected(String description, Customer customer, Order order,
            List<MenuItem> menu, Runnable action) {
        int customerId = customer.getId();
        int satisfaction = customer.getSatisfaction();
        CustomerStatus customerStatus = customer.getStatus();
        int servedTurn = customer.getServedTurn();
        int readyToPayTurn = customer.getReadyToPayTurn();
        Order associatedOrder = customer.getOrder();
        int orderId = order.getId();
        MenuItem item = order.getItem();
        OrderStatus orderStatus = order.getStatus();
        int remainingUnits = order.getRemainingUnits();
        int billCents = order.getBillCents();
        boolean canPrepare = order.canPrepare();
        boolean ready = order.isReady();
        List<MenuItem> originalMenu = List.copyOf(menu);
        boolean rejected = false;
        try {
            action.run();
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        check(rejected, description + " must throw IllegalStateException");
        check(customer.getId() == customerId && customer.getSatisfaction() == satisfaction
                && customer.getStatus() == customerStatus, "Rejected operation changed customer state");
        check(customer.getServedTurn() == servedTurn && customer.getReadyToPayTurn() == readyToPayTurn,
                "Rejected operation changed customer timestamps");
        check(customer.getOrder() == associatedOrder && associatedOrder == order,
                "Rejected operation changed shared Order identity");
        check(order.getId() == orderId && order.getItem() == item && order.getStatus() == orderStatus,
                "Rejected operation changed order identity, item or status");
        check(order.getRemainingUnits() == remainingUnits && order.getBillCents() == billCents,
                "Rejected operation changed preparation progress or billing");
        check(order.canPrepare() == canPrepare && order.isReady() == ready,
                "Rejected operation changed order predicates");
        checkMenu(menu);
        for (int index = 0; index < originalMenu.size(); index++) {
            check(menu.get(index) == originalMenu.get(index), "Rejected operation changed a menu item identity");
        }
        System.out.println("  Rejected " + description + "; M1 state and identities preserved.");
    }

    private static void printTurn(int turn, Customer customer, Order order) {
        String orderState = order == null ? "none" : order.getStatus()
                + ", remaining=" + order.getRemainingUnits() + ", bill=" + order.getBillCents() + " cents";
        System.out.println("Turn " + turn + ": customer=" + customer.getStatus()
                + ", satisfaction=" + customer.getSatisfaction() + ", order=" + orderState
                + ", servedTurn=" + customer.getServedTurn() + ", readyToPayTurn=" + customer.getReadyToPayTurn());
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        checks++;
    }
}
