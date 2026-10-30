package restaurantrush.domain;

import java.util.ArrayList;
import java.util.List;

/** Offline VIP initial-state, waiting-loss, presentation, selection and quotation checks. */
public final class VIPCustomerTest {
    private static int checks;

    private VIPCustomerTest() {
    }

    public static void main(String[] args) {
        int before = checks;
        testConstruction();
        testDisplayAndWaitingLossRules();
        testWaitingLossBounds();
        testNoWaitingLossAfterDeparture();
        testCyclicSelection();
        testCustomReorderedAndImmutableMenus();
        testRuleValidation();
        testBaseMenuQuotes();
        testWorkingRoundingPolicy();
        testRulesPreserveExistingState();
        System.out.println("VIPCustomerTest: PASS (" + (checks - before) + " checks)");
    }

    private static void testConstruction() {
        int[] invalidIds = {0, -1, Integer.MIN_VALUE};
        for (int id : invalidIds) {
            expectIllegalArgument("invalid customer ID " + id, () -> new VIPCustomer(id));
        }
        int[] validIds = {1, 2, 6, 7, 100, Integer.MAX_VALUE};
        for (int id : validIds) {
            Customer customer = new VIPCustomer(id);
            checkCustomer(customer, id, 100, CustomerStatus.WAITING, -1, -1, null);
        }
    }

    private static void testDisplayAndWaitingLossRules() {
        Customer customer = new VIPCustomer(1);
        checkDisplayAndWaitingLoss(customer);
    }

    private static void testWaitingLossBounds() {
        Customer waiting = new VIPCustomer(3);
        Customer seated = new VIPCustomer(4);
        seated.seat();
        checkApplicableWaitingLoss(waiting, CustomerStatus.WAITING);
        checkApplicableWaitingLoss(seated, CustomerStatus.SEATED);
    }

    private static void checkApplicableWaitingLoss(Customer customer, CustomerStatus expectedStatus) {
        int id = customer.getId();
        // These are unit-level loss calls, not game turns; the game remains 18 turns.
        int[] expectedSatisfaction = {95, 90, 85, 80, 75, 70, 65, 60, 55, 50,
                45, 40, 35, 30, 25, 20, 15, 10, 5, 0};
        checkCustomer(customer, id, 100, expectedStatus, -1, -1, null);
        for (int expected : expectedSatisfaction) {
            customer.applyWaitingLoss();
            checkCustomer(customer, id, expected, expectedStatus, -1, -1, null);
            check(customer.getSatisfaction() >= 0 && customer.getSatisfaction() <= 100,
                    "VIP satisfaction must remain within 0 to 100 after every loss call");
        }
        // The twentieth call reaches zero without automatic departure; further calls clamp there.
        for (int count = 0; count < 3; count++) {
            customer.applyWaitingLoss();
            checkCustomer(customer, id, 0, expectedStatus, -1, -1, null);
        }
    }

    private static void testNoWaitingLossAfterDeparture() {
        Customer customer = new VIPCustomer(5);
        customer.applyWaitingLoss();
        checkCustomer(customer, 5, 95, CustomerStatus.WAITING, -1, -1, null);
        customer.abandon();
        for (int count = 0; count < 3; count++) {
            customer.applyWaitingLoss();
            checkCustomer(customer, 5, 95, CustomerStatus.LEFT, -1, -1, null);
        }
    }

    private static void testCyclicSelection() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        List<MenuItem> menu = new ArrayList<>(List.of(salad, burger, pasta));
        int[] ids = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 100, 1_000_001, Integer.MAX_VALUE};
        MenuItem[] expected = {salad, burger, pasta, salad, burger, pasta, salad, burger, pasta,
                salad, salad, burger, salad};
        for (int index = 0; index < ids.length; index++) {
            Customer customer = new VIPCustomer(ids[index]);
            checkSelection(customer, menu, expected[index]);
        }
    }

    private static void testCustomReorderedAndImmutableMenus() {
        MenuItem soup = new MenuItem("Soup", 2500, 4);
        MenuItem tea = new MenuItem("Tea", 37, 1);
        MenuItem stew = new MenuItem("Stew", 999, 2);
        MenuItem bread = new MenuItem("Bread", 10, 1);
        Customer customer = new VIPCustomer(3);
        checkSelection(customer, new ArrayList<>(List.of(soup, tea, stew, bread)), stew);
        checkSelection(customer, new ArrayList<>(List.of(tea, bread, soup, stew)), soup);
        checkSelection(customer, List.of(bread, soup, tea, stew), tea);
        Customer laterCustomer = new VIPCustomer(6);
        checkSelection(laterCustomer, List.of(bread, soup, tea, stew), soup);
        Customer maximumIdCustomer = new VIPCustomer(Integer.MAX_VALUE);
        checkSelection(maximumIdCustomer, List.of(bread), bread);
    }

    private static void testRuleValidation() {
        Customer customer = new VIPCustomer(1);
        expectUnchangedIllegalArgument("null menu", customer, () -> customer.chooseMenuItem(null));
        checkRejectedMenu(customer, new ArrayList<>());
        checkRejectedMenu(customer, List.of());
        expectUnchangedIllegalArgument("null quoted item", customer, () -> customer.quotePriceCents(null));

        List<MenuItem> onlyNull = new ArrayList<>();
        onlyNull.add(null);
        checkRejectedMenu(customer, onlyNull);

        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        List<MenuItem> menu = new ArrayList<>();
        menu.add(salad);
        menu.add(null);
        menu.add(pasta);
        Customer secondCustomer = new VIPCustomer(2);
        checkRejectedMenu(secondCustomer, menu);
        checkSelection(customer, menu, salad);
        Customer thirdCustomer = new VIPCustomer(3);
        checkSelection(thirdCustomer, menu, pasta);
    }

    private static void testBaseMenuQuotes() {
        Customer customer = new VIPCustomer(1);
        checkQuote(customer, new MenuItem("Salad", 900, 1), 810);
        checkQuote(customer, new MenuItem("Burger", 1500, 1), 1350);
        checkQuote(customer, new MenuItem("Pasta", 1800, 2), 1620);
        checkQuote(customer, new MenuItem("Free", 0, 1), 0);
    }

    private static void testWorkingRoundingPolicy() {
        // Working assumption pending team review: round the discounted total
        // to the nearest cent, with half cents up. Expected values are literals.
        Customer customer = new VIPCustomer(7);
        int[] basePrices = {1, 4, 5, 6, 15, 16, 14, Integer.MAX_VALUE};
        int[] expectedQuotes = {1, 4, 5, 5, 14, 14, 13, 1932735282};
        // 6 -> 5.4 is below half a cent; 5 -> 4.5 is at half;
        // 4 -> 3.6 is above half. The maximum-price case catches int overflow.
        for (int index = 0; index < basePrices.length; index++) {
            checkQuote(customer, new MenuItem("Custom", basePrices[index], 1), expectedQuotes[index]);
        }
    }

    private static void testRulesPreserveExistingState() {
        Customer customer = new VIPCustomer(2);
        customer.applyWaitingLoss();
        checkCustomer(customer, 2, 95, CustomerStatus.WAITING, -1, -1, null);
        customer.seat();
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        Order order = new Order(2, burger, 1350);
        customer.acceptOrder(order);
        checkDisplayAndWaitingLoss(customer);
        checkSelection(customer, List.of(new MenuItem("Salad", 900, 1), burger), burger);
        checkQuote(customer, burger, 1350);
        expectUnchangedIllegalArgument("null menu with existing order", customer,
                () -> customer.chooseMenuItem(null));
        expectUnchangedIllegalArgument("null item with existing order", customer,
                () -> customer.quotePriceCents(null));
        check(order.getStatus() == OrderStatus.PLACED && order.getRemainingUnits() == 1
                && order.getItem() == burger && order.getBillCents() == 1350,
                "rule calls must preserve the associated order");
    }

    private static void checkDisplayAndWaitingLoss(Customer customer) {
        Runnable unchanged = snapshotCustomer(customer);
        check("VIP".equals(customer.typeLabel()), "exact VIP label through Customer");
        unchanged.run();
        check(customer.waitingLossPerTurn() == 5, "VIP waiting-loss rule through Customer");
        unchanged.run();
    }

    private static void checkSelection(Customer customer, List<MenuItem> menu, MenuItem expected) {
        Runnable unchanged = snapshotCustomer(customer);
        List<MenuItem> original = new ArrayList<>(menu);
        check(customer.chooseMenuItem(menu) == expected,
                "selected item identity for customer " + customer.getId());
        unchanged.run();
        checkMenuUnchanged(menu, original);
    }

    private static void checkRejectedMenu(Customer customer, List<MenuItem> menu) {
        List<MenuItem> original = new ArrayList<>(menu);
        expectUnchangedIllegalArgument("invalid menu for customer " + customer.getId(), customer,
                () -> customer.chooseMenuItem(menu));
        checkMenuUnchanged(menu, original);
    }

    private static void checkMenuUnchanged(List<MenuItem> menu, List<MenuItem> original) {
        check(menu.size() == original.size(), "rule call must preserve menu size");
        for (int index = 0; index < original.size(); index++) {
            check(menu.get(index) == original.get(index),
                    "rule call must preserve menu order and item identity at " + index);
        }
    }

    private static void checkQuote(Customer customer, MenuItem item, int expected) {
        Runnable unchanged = snapshotCustomer(customer);
        String name = item.getName();
        int price = item.getPriceCents();
        int units = item.getPreparationUnits();
        check(customer.quotePriceCents(item) == expected,
                "VIP quote for base price " + price + ": expected " + expected);
        unchanged.run();
        check(name.equals(item.getName()) && item.getPriceCents() == price
                && item.getPreparationUnits() == units, "quotation must preserve the item");
    }

    private static void checkCustomer(Customer customer, int id, int satisfaction, CustomerStatus status,
            int servedTurn, int readyToPayTurn, Order order) {
        check(customer.getId() == id, "customer ID must be " + id);
        check(customer.getSatisfaction() == satisfaction, "customer satisfaction must be " + satisfaction);
        check(customer.getStatus() == status, "customer status must be " + status);
        check(customer.getServedTurn() == servedTurn, "served turn must be " + servedTurn);
        check(customer.getReadyToPayTurn() == readyToPayTurn, "ready-to-pay turn must be " + readyToPayTurn);
        check(customer.getOrder() == order, "customer must retain the same order reference");
    }

    private static Runnable snapshotCustomer(Customer customer) {
        int id = customer.getId();
        int satisfaction = customer.getSatisfaction();
        CustomerStatus status = customer.getStatus();
        int servedTurn = customer.getServedTurn();
        int readyToPayTurn = customer.getReadyToPayTurn();
        Order order = customer.getOrder();
        return () -> checkCustomer(customer, id, satisfaction, status, servedTurn, readyToPayTurn, order);
    }

    private static void expectUnchangedIllegalArgument(String description, Customer customer, Runnable action) {
        Runnable unchanged = snapshotCustomer(customer);
        expectIllegalArgument(description, action);
        unchanged.run();
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
