package restaurantrush.view;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;
import restaurantrush.domain.OrderStatus;
import restaurantrush.domain.RegularCustomer;
import restaurantrush.domain.Table;

/** Offline checks for the immutable view records used by the CLI. */
public final class ViewRecordsTest {
    private static int checks;

    private ViewRecordsTest() {
    }

    public static void main(String[] args) {
        int before = checks;
        testTableViewFromFreeAndOccupiedTable();
        testTableViewValidation();
        testCustomerViewBeforeOrder();
        testCustomerViewWithOrder();
        testCustomerViewDoesNotFollowLaterChanges();
        testCustomerViewValidation();
        testSnapshotCopiesLists();
        testSnapshotValidation();
        System.out.println("ViewRecordsTest: PASS (" + (checks - before) + " checks)");
    }

    private static void testTableViewFromFreeAndOccupiedTable() {
        Table table = new Table(1);
        TableView free = TableView.from(table);
        check(free.id() == 1, "free table keeps its ID");
        check(free.occupantId() == null, "free table has no occupant ID");

        Customer customer = new RegularCustomer(3);
        table.assign(customer);
        TableView occupied = TableView.from(table);
        check(occupied.occupantId() != null && occupied.occupantId() == 3, "occupied table shows customer 3");
        check(free.occupantId() == null, "earlier view is unchanged after the table is assigned");
    }

    private static void testTableViewValidation() {
        expectIllegalArgument("zero table ID", () -> new TableView(0, null));
        expectIllegalArgument("negative occupant ID", () -> new TableView(1, -2));
        expectIllegalArgument("null table", () -> TableView.from(null));
    }

    private static void testCustomerViewBeforeOrder() {
        Customer customer = new RegularCustomer(1);
        CustomerView view = CustomerView.from(customer, null);
        check(view.id() == 1, "customer ID copied");
        check("Regular".equals(view.type()), "type label copied from Customer.typeLabel()");
        check(view.satisfaction() == 100, "new customer starts at 100");
        check(view.status() == CustomerStatus.WAITING, "new customer is WAITING");
        check(view.orderStatus() == null && view.itemName() == null && view.remainingUnits() == null,
                "order fields are all null before ordering");
        check(view.tableId() == null, "waiting customer has no table");
    }

    private static void testCustomerViewWithOrder() {
        Customer customer = new RegularCustomer(2);
        customer.seat();
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        customer.acceptOrder(new Order(2, pasta, 1800));
        CustomerView view = CustomerView.from(customer, 1);
        check(view.status() == CustomerStatus.SEATED, "seated status copied");
        check(view.orderStatus() == OrderStatus.PLACED, "order status copied");
        check("Pasta".equals(view.itemName()), "item name copied");
        check(view.remainingUnits() != null && view.remainingUnits() == 2, "remaining units copied");
        check(view.tableId() != null && view.tableId() == 1, "table ID copied");
    }

    private static void testCustomerViewDoesNotFollowLaterChanges() {
        Customer customer = new RegularCustomer(4);
        customer.seat();
        Order order = new Order(4, new MenuItem("Pasta", 1800, 2), 1800);
        customer.acceptOrder(order);
        CustomerView before = CustomerView.from(customer, 2);
        order.prepareOneUnit();
        customer.applyWaitingLoss();
        check(before.remainingUnits() == 2, "earlier view keeps 2 remaining units");
        check(before.orderStatus() == OrderStatus.PLACED, "earlier view keeps PLACED");
        check(before.satisfaction() == 100, "earlier view keeps satisfaction 100");
        CustomerView after = CustomerView.from(customer, 2);
        check(after.remainingUnits() == 1 && after.orderStatus() == OrderStatus.PREPARING,
                "new view shows the prepared unit");
        check(after.satisfaction() == 92, "new view shows the waiting loss");
    }

    private static void testCustomerViewValidation() {
        expectIllegalArgument("zero customer ID",
                () -> new CustomerView(0, "Regular", 100, CustomerStatus.WAITING, null, null, null, null));
        expectIllegalArgument("satisfaction above 100",
                () -> new CustomerView(1, "Regular", 101, CustomerStatus.WAITING, null, null, null, null));
        expectIllegalArgument("negative satisfaction",
                () -> new CustomerView(1, "Regular", -1, CustomerStatus.WAITING, null, null, null, null));
        expectIllegalArgument("null status", () -> new CustomerView(1, "Regular", 100, null, null, null, null, null));
        expectIllegalArgument("order status without item",
                () -> new CustomerView(1, "Regular", 100, CustomerStatus.SEATED, OrderStatus.PLACED, null, 1, 1));
        expectIllegalArgument("negative remaining units",
                () -> new CustomerView(1, "Regular", 100, CustomerStatus.SEATED, OrderStatus.PLACED, "Salad", -1, 1));
        expectIllegalArgument("blank item name",
                () -> new CustomerView(1, "Regular", 100, CustomerStatus.SEATED, OrderStatus.PLACED, " ", 1, 1));
        expectIllegalArgument("zero table ID",
                () -> new CustomerView(1, "Regular", 100, CustomerStatus.SEATED, null, null, null, 0));
        expectIllegalArgument("null customer", () -> CustomerView.from(null, null));
        expectIllegalArgument("null type label",
                () -> new CustomerView(1, null, 100, CustomerStatus.WAITING, null, null, null, null));
        expectIllegalArgument("blank type label",
                () -> new CustomerView(1, " ", 100, CustomerStatus.WAITING, null, null, null, null));
    }

    private static void testSnapshotCopiesLists() {
        List<Integer> waiting = new ArrayList<>(List.of(1));
        List<CustomerView> customers = new ArrayList<>(
                List.of(CustomerView.from(new RegularCustomer(1), null)));
        List<TableView> tables = new ArrayList<>(List.of(new TableView(1, null), new TableView(2, null)));
        GameSnapshot snapshot = new GameSnapshot(1, 0, 0, null, waiting, customers, tables,
                true, false, 0, 0, 0);

        waiting.add(2);
        customers.clear();
        tables.remove(0);
        check(snapshot.waitingCustomerIds().equals(List.of(1)), "queue list copied at construction");
        check(snapshot.customers().size() == 1, "customer list copied at construction");
        check(snapshot.tables().size() == 2, "table list copied at construction");

        expectUnsupported("modify snapshot queue", () -> snapshot.waitingCustomerIds().add(3));
        expectUnsupported("modify snapshot customers", () -> snapshot.customers().clear());
        expectUnsupported("modify snapshot tables", () -> snapshot.tables().clear());
    }

    private static void testSnapshotValidation() {
        List<TableView> tables = List.of(new TableView(1, null), new TableView(2, null));
        expectIllegalArgument("turn 0",
                () -> new GameSnapshot(0, 0, 0, null, List.of(), List.of(), tables, true, false, 0, 0, 0));
        expectIllegalArgument("turn 19",
                () -> new GameSnapshot(19, 18, 0, null, List.of(), List.of(), tables, true, false, 0, 0, 0));
        expectIllegalArgument("negative revenue",
                () -> new GameSnapshot(1, 0, -1, null, List.of(), List.of(), tables, true, false, 0, 0, 0));
        expectIllegalArgument("average without payments",
                () -> new GameSnapshot(5, 4, 0, 80.0, List.of(), List.of(), tables, true, false, 0, 0, 0));
        expectIllegalArgument("payments without average",
                () -> new GameSnapshot(7, 6, 1800, null, List.of(), List.of(), tables, true, false, 1, 1, 0));
        expectIllegalArgument("average above 100",
                () -> new GameSnapshot(7, 6, 1800, 101.0, List.of(), List.of(), tables, true, false, 1, 1, 0));
        expectIllegalArgument("negative count",
                () -> new GameSnapshot(1, 0, 0, null, List.of(), List.of(), tables, true, false, 0, -1, 0));
        expectIllegalArgument("null queue",
                () -> new GameSnapshot(1, 0, 0, null, null, List.of(), tables, true, false, 0, 0, 0));

        GameSnapshot finished = new GameSnapshot(18, 18, 6300, 76.0, List.of(), List.of(), tables,
                false, false, 4, 5, 1);
        check(finished.completedTurns() == 18 && finished.averagePaidSatisfaction() == 76.0,
                "finished game snapshot accepted");
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

    private static void expectUnsupported(String description, Runnable action) {
        try {
            action.run();
        } catch (UnsupportedOperationException expected) {
            checks++;
            return;
        }
        throw new AssertionError(description + ": expected UnsupportedOperationException");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }
}
