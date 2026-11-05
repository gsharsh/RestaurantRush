package restaurantrush.staff;

import restaurantrush.domain.ActionResult;
import restaurantrush.domain.CriticCustomer;
import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.Order;
import restaurantrush.domain.OrderStatus;
import restaurantrush.domain.RegularCustomer;
import restaurantrush.domain.Restaurant;
import restaurantrush.domain.VIPCustomer;

/**
 * Stage 2 regression tests: Waiter, Chef and Cashier are driven, unchanged,
 * with the real Regular, VIP and Critic customers through the abstract
 * {@link Customer}. Evidence for Liskov substitution and the open-closed
 * principle: no staff code branches on the customer type.
 */
public final class StaffMixedCustomerTest {
    private static int checks;

    private StaffMixedCustomerTest() {
    }

    public static void main(String[] args) {
        testLockedBillsForEachCustomerType();
        testPaidOnlyRevenueAndSatisfaction();
        testInvalidActionsPreserveStateForEachType();
        testWaitingLossAndNoLossAfterService();
        testAbandonmentCancelsReadyOrderAndFreesTable();
        testCashierOldestFirstWithMixedCustomers();
        testStaffTimingRulesWithMixedCustomers();
        System.out.println("StaffMixedCustomerTest: PASS (" + checks + " checks)");
    }

    private static void testLockedBillsForEachCustomerType() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Customer regular = new RegularCustomer(1);
        Customer vip = new VIPCustomer(2);
        restaurant.addArrival(regular);
        restaurant.addArrival(vip);

        seatAndOrder(waiter, restaurant, 1, 1);
        seatAndOrder(waiter, restaurant, 2, 2);
        check(restaurant.getOrders().size() == 2, "two orders registered");

        Order regularOrder = regular.getOrder();
        check(regularOrder.getItem().getName().equals("Salad"), "regular 1 orders Salad");
        check(regularOrder.getBillCents() == 900, "regular pays full Salad price");

        Order vipOrder = vip.getOrder();
        check(vipOrder == restaurant.findOrder(2), "VIP customer and restaurant share one order");
        check(vipOrder.getItem().getName().equals("Burger"), "VIP 2 orders Burger");
        check(vipOrder.getBillCents() == 1350, "VIP Burger is locked at 90% (1350 cents)");
        check(vipOrder.getBillCents() == vip.quotePriceCents(vipOrder.getItem()),
                "waiter locked exactly the customer's own quote");

        // The Critic is checked in a second restaurant so both tables above stay in use.
        Restaurant other = new Restaurant();
        Customer critic = new CriticCustomer(4);
        other.addArrival(critic);
        seatAndOrder(waiter, other, 4, 1);
        Order criticOrder = critic.getOrder();
        check(criticOrder.getItem().getName().equals("Pasta"), "Critic chooses the most-prep item");
        check(criticOrder.getBillCents() == 1800, "Critic pays full Pasta price");
        check(criticOrder.getStatus() == OrderStatus.PLACED, "new orders start PLACED");
    }

    private static void testPaidOnlyRevenueAndSatisfaction() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        Customer vip = new VIPCustomer(2);
        Customer critic = new CriticCustomer(4);
        restaurant.addArrival(vip);
        restaurant.addArrival(critic);

        seatAndOrder(waiter, restaurant, 2, 1);
        seatAndOrder(waiter, restaurant, 4, 2);
        vip.applyWaitingLoss();
        critic.applyWaitingLoss();
        check(vip.getSatisfaction() == 95 && critic.getSatisfaction() == 88, "type-specific loss");

        cook(chef, restaurant, 2, 1);
        cook(chef, restaurant, 4, 2);
        check(restaurant.getRevenueCents() == 0 && restaurant.getPaidCount() == 0,
                "nothing counts before payment");
        expectOk(waiter.serve(restaurant, 2, 3));
        expectOk(waiter.serve(restaurant, 4, 4));
        check(restaurant.getRevenueCents() == 0, "serving earns no revenue");

        vip.finishEating(4);
        critic.finishEating(5);
        check(cashier.collectOne(restaurant, 5), "VIP pays first (ready earlier)");
        check(restaurant.getRevenueCents() == 1350, "revenue is the VIP's discounted locked bill");
        check(restaurant.getPaidSatisfactionTotal() == 95, "VIP satisfaction recorded at payment");
        check(restaurant.findTableForCustomer(2) == null, "VIP table released");
        check(cashier.collectOne(restaurant, 6), "Critic pays next turn");
        check(restaurant.getRevenueCents() == 1350 + 1800, "revenue sums the locked bills");
        check(restaurant.getPaidCount() == 2, "two payments");
        check(restaurant.getPaidSatisfactionTotal() == 95 + 88, "satisfaction total");
        check(restaurant.getAveragePaidSatisfaction() == (95 + 88) / 2.0, "average at payment");
        check(vip.getStatus() == CustomerStatus.PAID && critic.getStatus() == CustomerStatus.PAID,
                "both customers PAID");
    }

    private static void testInvalidActionsPreserveStateForEachType() {
        Customer[] customers = {new RegularCustomer(1), new VIPCustomer(2), new CriticCustomer(3)};
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        for (Customer customer : customers) {
            restaurant.addArrival(customer);
        }
        for (Customer customer : customers) {
            int id = customer.getId();
            // Not seated yet: ordering and serving must be refused.
            expectFail(waiter.takeOrder(restaurant, id));
            expectFail(waiter.serve(restaurant, id, 1));
            check(customer.getStatus() == CustomerStatus.WAITING && customer.getOrder() == null,
                    "refused actions leave customer " + id + " untouched");
        }
        check(restaurant.getOrders().isEmpty(), "no order registered by refused actions");

        expectOk(waiter.seat(restaurant, 1, 1));
        expectOk(waiter.seat(restaurant, 2, 2));
        expectFail(waiter.seat(restaurant, 3, 1));
        expectFail(waiter.seat(restaurant, 3, 2));
        check(customers[2].getStatus() == CustomerStatus.WAITING, "Critic still queued");
        check(restaurant.getWaitingQueue().size() == 1, "queue unchanged by refused seating");

        for (Customer customer : new Customer[] {customers[0], customers[1]}) {
            int id = customer.getId();
            expectFail(waiter.serve(restaurant, id, 1));
            expectOk(waiter.takeOrder(restaurant, id));
            Order order = customer.getOrder();
            int bill = order.getBillCents();
            expectFail(waiter.takeOrder(restaurant, id));
            expectFail(waiter.serve(restaurant, id, 2));
            check(customer.getOrder() == order && order.getBillCents() == bill
                    && order.getStatus() == OrderStatus.PLACED, "order unchanged by refusals");
            check(restaurant.getOrders().size() == (id == 1 ? 1 : 2), "no duplicate registration");
            expectFail(waiter.serve(restaurant, id, 0));
        }
        expectFail(chef.prepareOneUnit(null));
        check(restaurant.getServedCount() == 0 && restaurant.getRevenueCents() == 0,
                "statistics untouched by refusals");
    }

    private static void testWaitingLossAndNoLossAfterService() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Customer regular = new RegularCustomer(1);
        Customer vip = new VIPCustomer(2);
        Customer critic = new CriticCustomer(3);
        restaurant.addArrival(regular);
        restaurant.addArrival(vip);
        restaurant.addArrival(critic);

        for (Customer customer : new Customer[] {regular, vip, critic}) {
            customer.applyWaitingLoss();
        }
        check(regular.getSatisfaction() == 92, "regular loses 8");
        check(vip.getSatisfaction() == 95, "VIP loses 5");
        check(critic.getSatisfaction() == 88, "Critic loses 12");

        seatAndOrder(waiter, restaurant, 2, 1);
        cook(chef, restaurant, 2, 1);
        expectOk(waiter.serve(restaurant, 2, 2));
        vip.applyWaitingLoss();
        check(vip.getSatisfaction() == 95, "no loss once served (EATING)");

        // Floor at zero: the Critic loses 12 a turn and never goes negative.
        for (int turn = 0; turn < 12; turn++) {
            critic.applyWaitingLoss();
        }
        check(critic.getSatisfaction() == 0, "Critic satisfaction floors at 0");
    }

    private static void testAbandonmentCancelsReadyOrderAndFreesTable() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Customer critic = new CriticCustomer(4);
        Customer vip = new VIPCustomer(2);
        restaurant.addArrival(critic);
        restaurant.addArrival(vip);

        seatAndOrder(waiter, restaurant, 4, 1);
        cook(chef, restaurant, 4, 2);
        Order criticOrder = critic.getOrder();
        check(criticOrder.getStatus() == OrderStatus.READY, "Critic's Pasta is READY but unserved");
        for (int turn = 0; turn < 9; turn++) {
            critic.applyWaitingLoss();
        }
        check(critic.getSatisfaction() == 0, "Critic reaches zero after 9 turns");

        restaurant.removeAbandoned(critic);
        check(critic.getStatus() == CustomerStatus.LEFT, "Critic left");
        check(criticOrder.getStatus() == OrderStatus.CANCELLED, "READY order cancelled");
        check(restaurant.findTableForCustomer(4) == null
                && restaurant.findTable(1).isAvailable(), "table released");
        check(restaurant.getRevenueCents() == 0 && restaurant.getPaidCount() == 0,
                "abandonment adds no revenue");
        check(restaurant.getAbandonedCount() == 1, "abandonment counted once");
        expectFail(chef.prepareOneUnit(criticOrder));
        expectFail(waiter.serve(restaurant, 4, 5));

        // A waiting VIP abandons from the queue after 20 turns of 5 points.
        for (int turn = 0; turn < 20; turn++) {
            vip.applyWaitingLoss();
        }
        check(vip.getSatisfaction() == 0, "VIP reaches zero after 20 turns");
        restaurant.removeAbandoned(vip);
        check(restaurant.getWaitingQueue().isEmpty() && vip.getStatus() == CustomerStatus.LEFT,
                "queued VIP removed");
        check(restaurant.getAbandonedCount() == 2, "second abandonment counted");
    }

    private static void testCashierOldestFirstWithMixedCustomers() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        Customer critic = new CriticCustomer(4);
        Customer vip = new VIPCustomer(2);
        restaurant.addArrival(critic);
        restaurant.addArrival(vip);

        seatAndOrder(waiter, restaurant, 4, 1);
        seatAndOrder(waiter, restaurant, 2, 2);
        cook(chef, restaurant, 4, 2);
        cook(chef, restaurant, 2, 1);
        expectOk(waiter.serve(restaurant, 4, 3));
        expectOk(waiter.serve(restaurant, 2, 3));
        critic.finishEating(4);
        vip.finishEating(4);

        check(!cashier.collectOne(restaurant, 4), "tied customers are not payable on the ready turn");
        check(cashier.collectOne(restaurant, 5), "one customer pays");
        check(vip.getStatus() == CustomerStatus.PAID, "tie broken by lower customer ID (VIP 2)");
        check(critic.getStatus() == CustomerStatus.READY_TO_PAY, "Critic waits");
        check(!cashier.collectOne(restaurant, 5), "only one payment per turn");
        check(cashier.collectOne(restaurant, 6), "Critic pays next turn");
        check(restaurant.getRevenueCents() == 1350 + 1800, "locked bills collected");
    }

    private static void testStaffTimingRulesWithMixedCustomers() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Customer vip = new VIPCustomer(2);
        restaurant.addArrival(vip);
        seatAndOrder(waiter, restaurant, 2, 1);
        Order order = vip.getOrder();

        expectFail(waiter.serve(restaurant, 2, 1));
        expectOk(chef.prepareOneUnit(order));
        check(order.isReady(), "one-unit Burger is READY after one Chef action");
        expectOk(waiter.serve(restaurant, 2, 2));
        vip.finishEating(2);
        check(vip.getStatus() == CustomerStatus.EATING, "cannot finish eating on the service turn");
        vip.finishEating(3);
        check(vip.getStatus() == CustomerStatus.READY_TO_PAY, "finishes eating on a later turn");
        Cashier cashier = new Cashier();
        check(!cashier.collectOne(restaurant, 3), "cannot pay on the ready turn");
        check(cashier.collectOne(restaurant, 4), "pays on a later turn");
    }

    private static void seatAndOrder(Waiter waiter, Restaurant restaurant, int customerId, int tableId) {
        expectOk(waiter.seat(restaurant, customerId, tableId));
        expectOk(waiter.takeOrder(restaurant, customerId));
    }

    private static void cook(Chef chef, Restaurant restaurant, int customerId, int actions) {
        Order order = restaurant.findOrder(customerId);
        for (int i = 0; i < actions; i++) {
            expectOk(chef.prepareOneUnit(order));
        }
    }

    private static void expectOk(ActionResult result) {
        check(result.success(), "expected success but got: " + result.message());
    }

    private static void expectFail(ActionResult result) {
        check(!result.success(), "expected failure");
        check(!result.message().isBlank(), "a failure must carry a reason");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }
}
