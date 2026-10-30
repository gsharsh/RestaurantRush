package restaurantrush.domain;

import java.util.ArrayList;
import java.util.List;

/** RegularCustomer selection, quotation and waiting-loss rule tests. */
public final class RegularCustomerTest {
    private static int checks;

    private RegularCustomerTest() {
    }

    public static void main(String[] args) {
        int before = checks;
        testTypeLabelThroughCustomer();
        testRegularCustomerRules();
        testCustomerRuleValidation();
        System.out.println("RegularCustomerTest: PASS (" + (checks - before) + " checks)");
    }

    private static void testTypeLabelThroughCustomer() {
        Customer customer = new RegularCustomer(1);
        check("Regular".equals(customer.typeLabel()), "regular type label through Customer");
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
