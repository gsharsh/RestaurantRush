package restaurantrush.staff;

import java.util.List;
import restaurantrush.domain.ActionResult;
import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;
import restaurantrush.domain.OrderStatus;
import restaurantrush.domain.RegularCustomer;
import restaurantrush.domain.Restaurant;
import restaurantrush.domain.Table;

/**
 * Waiter, Chef and Cashier tests. Staff are always driven through the abstract
 * {@link Customer}; {@link DiscountedCustomer} is a test-only subtype with a
 * different waiting loss, food choice and price, proving the staff classes work
 * unchanged for any customer (Liskov substitution / open-closed).
 */
public final class StaffTest {
    private static int checks;

    private StaffTest() {
    }

    public static void main(String[] args) {
        testWaiterSeatsAndRejects();
        testWaiterTakeOrder();
        testWaiterServe();
        testChefPreparation();
        testCashierTimingAndOrder();
        testStaffWorkUnchangedForOtherCustomerSubtype();
        testCashierWithTwoEligiblePayers();
        System.out.println("StaffTest: PASS (" + checks + " checks)");
    }

    private static void testWaiterSeatsAndRejects() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        restaurant.addArrival(new RegularCustomer(1));
        restaurant.addArrival(new RegularCustomer(2));
        restaurant.addArrival(new RegularCustomer(3));

        expectFail(waiter.seat(null, 1, 1), "restaurant");
        expectFail(waiter.seat(restaurant, 9, 1), "customer 9");
        expectFail(waiter.seat(restaurant, 1, 9), "table 9");
        check(restaurant.findCustomer(1).getStatus() == CustomerStatus.WAITING,
                "failed seat must leave the customer waiting");
        expectOk(waiter.seat(restaurant, 1, 1));
        check(restaurant.findCustomer(1).getStatus() == CustomerStatus.SEATED, "seated");
        expectFail(waiter.seat(restaurant, 1, 2), "not in the waiting queue");
        expectFail(waiter.seat(restaurant, 2, 1), "occupied");
        expectOk(waiter.seat(restaurant, 2, 2));
        expectFail(waiter.seat(restaurant, 3, 1), "occupied");
        check(restaurant.getWaitingQueue().size() == 1, "customer 3 still queued");
    }

    private static void testWaiterTakeOrder() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        restaurant.addArrival(new RegularCustomer(1));
        restaurant.addArrival(new RegularCustomer(2));

        expectFail(waiter.takeOrder(null, 1), "restaurant");
        expectFail(waiter.takeOrder(restaurant, 7), "customer 7");
        expectFail(waiter.takeOrder(restaurant, 1), "must be seated");
        check(restaurant.getOrders().isEmpty(), "failed orders register nothing");

        waiter.seat(restaurant, 1, 1);
        ActionResult ordered = waiter.takeOrder(restaurant, 1);
        expectOk(ordered);
        Customer customer = restaurant.findCustomer(1);
        Order order = customer.getOrder();
        check(order != null && order == restaurant.findOrder(1), "one shared order object");
        check(order.getItem() == restaurant.getMenu().get(0), "customer 1 chooses the first menu item");
        check(order.getBillCents() == customer.quotePriceCents(order.getItem()),
                "bill is the customer's quote");
        check(order.getStatus() == OrderStatus.PLACED, "new order is PLACED");
        expectFail(waiter.takeOrder(restaurant, 1), "already ordered");
        check(restaurant.getOrders().size() == 1, "duplicate order not registered");
    }

    private static void testWaiterServe() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        restaurant.addArrival(new RegularCustomer(3));
        restaurant.addArrival(new RegularCustomer(2));
        expectFail(waiter.serve(restaurant, 3, 1), "seated");
        waiter.seat(restaurant, 3, 1);
        expectFail(waiter.serve(restaurant, 3, 1), "not ordered");
        waiter.takeOrder(restaurant, 3);
        Order pasta = restaurant.findOrder(3);
        check(pasta.getItem().getPreparationUnits() == 2, "customer 3 orders Pasta");
        expectFail(waiter.serve(restaurant, 3, 1), "not READY");
        expectFail(waiter.serve(null, 3, 1), "restaurant");
        expectFail(waiter.serve(restaurant, 3, 0), "positive");
        check(pasta.getStatus() == OrderStatus.PLACED, "failed serve must not change the order");
        chef.prepareOneUnit(pasta);
        expectFail(waiter.serve(restaurant, 3, 2), "not READY");
        chef.prepareOneUnit(pasta);
        expectOk(waiter.serve(restaurant, 3, 3));
        check(pasta.getStatus() == OrderStatus.SERVED, "order served");
        check(restaurant.findCustomer(3).getStatus() == CustomerStatus.EATING, "customer eating");
        check(restaurant.getServedCount() == 1, "served counted once");
        expectFail(waiter.serve(restaurant, 3, 3), "seated");
        check(restaurant.getServedCount() == 1, "repeat serve not counted");
    }

    private static void testChefPreparation() {
        Chef chef = new Chef();
        expectFail(chef.prepareOneUnit(null), "no order");

        Order salad = new Order(1, new MenuItem("Salad", 900, 1), 900);
        ActionResult saladResult = chef.prepareOneUnit(salad);
        expectOk(saladResult);
        check(saladResult.message().contains("READY"), "one-unit dish reports READY");
        check(salad.getStatus() == OrderStatus.READY, "salad ready after one choice");
        ActionResult again = chef.prepareOneUnit(salad);
        expectFail(again, "READY");
        check(salad.getRemainingUnits() == 0 && salad.getStatus() == OrderStatus.READY,
                "failed cook leaves the order unchanged");

        Order pasta = new Order(2, new MenuItem("Pasta", 1800, 2), 1800);
        ActionResult first = chef.prepareOneUnit(pasta);
        expectOk(first);
        check(!first.message().contains("READY") && pasta.getStatus() == OrderStatus.PREPARING,
                "two-unit dish is still PREPARING after one unit");
        check(pasta.getRemainingUnits() == 1, "one unit left");
        expectOk(chef.prepareOneUnit(pasta));
        check(pasta.getStatus() == OrderStatus.READY, "pasta ready after two choices");

        Order cancelled = new Order(3, new MenuItem("Burger", 1500, 1), 1500);
        cancelled.cancel();
        expectFail(chef.prepareOneUnit(cancelled), "CANCELLED");
        check(cancelled.getRemainingUnits() == 1, "cancelled order not cooked");
    }

    private static void testCashierTimingAndOrder() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        restaurant.addArrival(new RegularCustomer(1));
        restaurant.addArrival(new RegularCustomer(2));
        check(!cashier.collectOne(restaurant, 1), "nobody to pay");
        expectIllegalArgument(() -> cashier.collectOne(null, 1));
        expectIllegalArgument(() -> cashier.collectOne(restaurant, 0));

        for (int id = 1; id <= 2; id++) {
            waiter.seat(restaurant, id, id);
            waiter.takeOrder(restaurant, id);
        }
        chef.prepareOneUnit(restaurant.findOrder(1));
        chef.prepareOneUnit(restaurant.findOrder(2));
        chef.prepareOneUnit(restaurant.findOrder(2));
        waiter.serve(restaurant, 2, 3);
        waiter.serve(restaurant, 1, 4);
        restaurant.findCustomer(2).finishEating(4);
        restaurant.findCustomer(1).finishEating(5);

        check(!cashier.collectOne(restaurant, 4), "customer 2 ready this turn is not payable yet");
        check(cashier.collectOne(restaurant, 5), "customer 2 pays on a later turn");
        check(restaurant.findCustomer(2).getStatus() == CustomerStatus.PAID, "oldest payer first");
        check(restaurant.findCustomer(1).getStatus() == CustomerStatus.READY_TO_PAY,
                "customer 1 became ready this turn");
        check(!cashier.collectOne(restaurant, 5), "at most one payment per turn");
        check(restaurant.getPaidCount() == 1, "one payment recorded");
        check(cashier.collectOne(restaurant, 6), "customer 1 pays next turn");
        check(!cashier.collectOne(restaurant, 7), "nobody left to pay");
        check(restaurant.getRevenueCents() == restaurant.findOrder(1).getBillCents()
                + restaurant.findOrder(2).getBillCents(), "revenue is the sum of locked bills");
    }

    private static void testStaffWorkUnchangedForOtherCustomerSubtype() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        Customer special = new DiscountedCustomer(1);
        restaurant.addArrival(special);

        expectOk(waiter.seat(restaurant, 1, 1));
        expectOk(waiter.takeOrder(restaurant, 1));
        Order order = special.getOrder();
        check(order.getItem() == restaurant.getMenu().get(2), "subtype's own food choice is used");
        check(order.getBillCents() == order.getItem().getPriceCents() / 2,
                "subtype's own price is locked into the order");
        expectOk(chef.prepareOneUnit(order));
        expectOk(chef.prepareOneUnit(order));
        expectOk(waiter.serve(restaurant, 1, 2));
        special.finishEating(3);
        check(cashier.collectOne(restaurant, 4), "cashier pays the subtype");
        check(special.getStatus() == CustomerStatus.PAID, "subtype paid");
        check(restaurant.getRevenueCents() == order.getBillCents(), "discounted revenue recorded");

        Customer other = new DiscountedCustomer(2);
        restaurant.addArrival(other);
        waiter.seat(restaurant, 2, 1);
        check(other.waitingLossPerTurn() == 5, "subtype loses less while waiting");
    }

    private static void testCashierWithTwoEligiblePayers() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        Customer newer = new RegularCustomer(1);
        Customer older = new RegularCustomer(2);
        restaurant.addArrival(newer);
        restaurant.addArrival(older);
        expectOk(waiter.seat(restaurant, 1, 1));
        expectOk(waiter.seat(restaurant, 2, 2));
        newer.applyWaitingLoss();
        expectOk(waiter.takeOrder(restaurant, 1));
        expectOk(waiter.takeOrder(restaurant, 2));
        Order newerOrder = newer.getOrder();
        Order olderOrder = older.getOrder();
        expectOk(chef.prepareOneUnit(newerOrder));
        expectOk(chef.prepareOneUnit(olderOrder));
        expectOk(waiter.serve(restaurant, 2, 3));
        older.finishEating(4);
        expectOk(waiter.serve(restaurant, 1, 4));
        newer.finishEating(5);

        check(newer.getStatus() == CustomerStatus.READY_TO_PAY && newer.getReadyToPayTurn() == 5
                && newer.getReadyToPayTurn() < 6 && newerOrder.getStatus() == OrderStatus.SERVED
                && restaurant.findTable(1).getOccupant() == newer, "customer 1 is eligible before turn 6 collection");
        check(older.getStatus() == CustomerStatus.READY_TO_PAY && older.getReadyToPayTurn() == 4
                && older.getReadyToPayTurn() < 6 && olderOrder.getStatus() == OrderStatus.SERVED
                && restaurant.findTable(2).getOccupant() == older, "customer 2 is eligible before turn 6 collection");
        check(cashier.collectOne(restaurant, 6), "one of two eligible customers pays on turn 6");
        check(older.getStatus() == CustomerStatus.PAID && olderOrder.getStatus() == OrderStatus.PAID,
                "older readiness wins over lower ID and insertion order");
        check(newer.getStatus() == CustomerStatus.READY_TO_PAY && newerOrder.getStatus() == OrderStatus.SERVED
                && restaurant.oldestEligiblePayer(6) == newer, "customer 1 remains eligible on turn 6");

        int customerIdBefore = newer.getId();
        int satisfactionBefore = newer.getSatisfaction();
        CustomerStatus customerStatusBefore = newer.getStatus();
        int servedTurnBefore = newer.getServedTurn();
        int readyTurnBefore = newer.getReadyToPayTurn();
        int orderIdBefore = newerOrder.getId();
        MenuItem itemBefore = newerOrder.getItem();
        OrderStatus orderStatusBefore = newerOrder.getStatus();
        int remainingUnitsBefore = newerOrder.getRemainingUnits();
        int billBefore = newerOrder.getBillCents();
        boolean canPrepareBefore = newerOrder.canPrepare();
        boolean isReadyBefore = newerOrder.isReady();
        Table tableBefore = restaurant.findTableForCustomer(customerIdBefore);
        Customer occupantBefore = tableBefore.getOccupant();
        boolean tableAvailableBefore = tableBefore.isAvailable();
        int revenueBefore = restaurant.getRevenueCents();
        int paidBefore = restaurant.getPaidCount();
        int paidSatisfactionBefore = restaurant.getPaidSatisfactionTotal();
        double averageBefore = restaurant.getAveragePaidSatisfaction();
        int servedBefore = restaurant.getServedCount();
        int abandonedBefore = restaurant.getAbandonedCount();

        check(!cashier.collectOne(restaurant, 6), "a second turn 6 payment is rejected despite an eligible payer");
        check(restaurant.findCustomer(customerIdBefore) == newer && newer.getId() == customerIdBefore,
                "rejected second payment preserves customer identity");
        check(newer.getOrder() == newerOrder && restaurant.findOrder(orderIdBefore) == newerOrder,
                "rejected second payment preserves the shared order identity");
        check(newer.getStatus() == customerStatusBefore && newer.getSatisfaction() == satisfactionBefore,
                "rejected second payment preserves customer status and satisfaction");
        check(newer.getServedTurn() == servedTurnBefore && newer.getReadyToPayTurn() == readyTurnBefore,
                "rejected second payment preserves customer timestamps");
        check(newerOrder.getId() == orderIdBefore && newerOrder.getItem() == itemBefore
                && newerOrder.getBillCents() == billBefore, "rejected second payment preserves order identity and quote");
        check(newerOrder.getStatus() == orderStatusBefore && newerOrder.getRemainingUnits() == remainingUnitsBefore
                && newerOrder.canPrepare() == canPrepareBefore && newerOrder.isReady() == isReadyBefore,
                "rejected second payment preserves order lifecycle and preparation state");
        check(restaurant.findTableForCustomer(customerIdBefore) == tableBefore
                && tableBefore.getOccupant() == occupantBefore && tableBefore.isAvailable() == tableAvailableBefore,
                "rejected second payment preserves table identity and occupancy");
        check(restaurant.oldestEligiblePayer(6) == newer, "customer 1 remains eligible after rejection");
        check(restaurant.getRevenueCents() == revenueBefore, "rejected second payment preserves revenue");
        check(restaurant.getPaidCount() == paidBefore, "rejected second payment preserves paid count");
        check(restaurant.getPaidSatisfactionTotal() == paidSatisfactionBefore,
                "rejected second payment preserves paid satisfaction total");
        check(restaurant.getAveragePaidSatisfaction() == averageBefore,
                "rejected second payment preserves paid satisfaction average");
        check(restaurant.getServedCount() == servedBefore && restaurant.getAbandonedCount() == abandonedBefore,
                "rejected second payment preserves served and abandoned counts");

        check(cashier.collectOne(restaurant, 7), "remaining eligible customer pays on turn 7");
        check(newer.getStatus() == CustomerStatus.PAID && newerOrder.getStatus() == OrderStatus.PAID,
                "remaining customer and shared order are paid");
        check(tableBefore.isAvailable() && tableBefore.getOccupant() == null, "remaining customer's table is released");
        check(restaurant.getPaidCount() == paidBefore + 1 && restaurant.getRevenueCents() == revenueBefore + billBefore,
                "next-turn payment adds one customer and the remaining locked bill");
        check(restaurant.getPaidSatisfactionTotal() == paidSatisfactionBefore + satisfactionBefore,
                "next-turn payment records the remaining customer's satisfaction");
    }

    private static void expectOk(ActionResult result) {
        check(result.success(), "expected success but got: " + result.message());
    }

    private static void expectFail(ActionResult result, String reasonFragment) {
        check(!result.success(), "expected failure containing '" + reasonFragment + "'");
        check(result.message().contains(reasonFragment),
                "reason '" + result.message() + "' should contain '" + reasonFragment + "'");
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

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }

    /** Test-only customer: always picks the last menu item and pays half price. */
    private static final class DiscountedCustomer extends Customer {
        DiscountedCustomer(int id) {
            super(id);
        }

        @Override
        public String typeLabel() {
            return "Discounted";
        }

        @Override
        public int waitingLossPerTurn() {
            return 5;
        }

        @Override
        public MenuItem chooseMenuItem(List<MenuItem> menu) {
            return menu.get(menu.size() - 1);
        }

        @Override
        public int quotePriceCents(MenuItem item) {
            return item.getPriceCents() / 2;
        }
    }
}
