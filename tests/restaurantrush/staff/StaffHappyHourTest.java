package restaurantrush.staff;

import restaurantrush.domain.ActionResult;
import restaurantrush.domain.CriticCustomer;
import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.HappyHour;
import restaurantrush.domain.Order;
import restaurantrush.domain.RegularCustomer;
import restaurantrush.domain.Restaurant;
import restaurantrush.domain.VIPCustomer;

/**
 * Stage 3 regression tests for the Waiter's pricing handoff: the customer quotes,
 * Happy Hour discounts that quote, and the final bill is locked into the order
 * and paid unchanged by the Cashier. Menu order is Salad 900, Burger 1500,
 * Pasta 1800, Salad + Pasta Combo 2430; Regular and VIP IDs cycle through it.
 */
public final class StaffHappyHourTest {
    private static int checks;

    private StaffHappyHourTest() {
    }

    public static void main(String[] args) {
        testConstructorRejectsNullPolicy();
        testQuotesForEveryCustomerTypeDuringHappyHour();
        testQuotesWithoutHappyHourAreUnchanged();
        testBillsAreLockedAtOrderTime();
        testInvalidOrdersPreserveStateAndPolicy();
        testCashierCollectsLockedHappyHourBills();
        testWaiterWithoutPolicyKeepsCustomerQuote();
        System.out.println("StaffHappyHourTest: PASS (" + checks + " checks)");
    }

    private static void testConstructorRejectsNullPolicy() {
        try {
            new Waiter(null);
        } catch (IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError("expected IllegalArgumentException");
    }

    private static void testQuotesForEveryCustomerTypeDuringHappyHour() {
        HappyHour happyHour = activeFrom(3);
        check(billFor(new RegularCustomer(2), happyHour, 3) == 1200, "Regular Burger 1500 -> 1200");
        check(billFor(new VIPCustomer(2), happyHour, 3) == 1080,
                "VIP Burger 1500 -> 1350 -> 1080 (the agreed check value)");
        check(billFor(new VIPCustomer(1), happyHour, 4) == 648, "VIP Salad 900 -> 810 -> 648");
        check(billFor(new RegularCustomer(4), happyHour, 4) == 1944, "Regular Combo 2430 -> 1944");
        check(billFor(new VIPCustomer(4), happyHour, 3) == 1750,
                "VIP Combo 2430 -> 2187 -> 1750 (each step rounded once)");
        check(billFor(new CriticCustomer(1), happyHour, 3) == 1944,
                "Critic picks the Combo and pays 2430 -> 1944");
    }

    private static void testQuotesWithoutHappyHourAreUnchanged() {
        HappyHour happyHour = activeFrom(3);
        check(billFor(new VIPCustomer(2), happyHour, 2) == 1350, "turn before activation: VIP price only");
        check(billFor(new VIPCustomer(2), happyHour, 5) == 1350, "turn after the two active turns");
        check(billFor(new RegularCustomer(2), new HappyHour(), 3) == 1500, "never activated");
        check(billFor(new CriticCustomer(4), happyHour, 18) == 2430, "Critic full price outside Happy Hour");
    }

    private static void testBillsAreLockedAtOrderTime() {
        HappyHour happyHour = new HappyHour();
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter(happyHour);
        restaurant.addArrival(new RegularCustomer(2));
        restaurant.addArrival(new RegularCustomer(6));
        expectOk(waiter.seat(restaurant, 2, 1));
        expectOk(waiter.seat(restaurant, 6, 2));

        expectOk(waiter.takeOrder(restaurant, 2, 2));
        happyHour.activate(3);
        expectOk(waiter.takeOrder(restaurant, 6, 3));
        check(restaurant.findOrder(2).getBillCents() == 1500, "ordered before Happy Hour keeps the full bill");
        check(restaurant.findOrder(6).getBillCents() == 1200, "ordered during Happy Hour is discounted");
        // Time passes beyond the active period: neither existing bill moves.
        check(happyHour.remainingTurns(6) == 0, "Happy Hour has ended");
        check(restaurant.findOrder(2).getBillCents() == 1500
                && restaurant.findOrder(6).getBillCents() == 1200, "locked bills never change");
    }

    private static void testInvalidOrdersPreserveStateAndPolicy() {
        HappyHour happyHour = activeFrom(3);
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter(happyHour);
        Customer vip = new VIPCustomer(2);
        restaurant.addArrival(vip);

        expectFail(waiter.takeOrder(null, 2, 3));
        expectFail(waiter.takeOrder(restaurant, 9, 3));
        expectFail(waiter.takeOrder(restaurant, 2, 3));
        check(vip.getStatus() == CustomerStatus.WAITING && vip.getOrder() == null,
                "unseated customer cannot order");
        expectOk(waiter.seat(restaurant, 2, 1));
        expectFail(waiter.takeOrder(restaurant, 2, 0));
        expectFail(waiter.takeOrder(restaurant, 2, 19));
        check(vip.getOrder() == null && restaurant.getOrders().isEmpty(), "invalid turn registers nothing");

        expectOk(waiter.takeOrder(restaurant, 2, 3));
        Order order = vip.getOrder();
        expectFail(waiter.takeOrder(restaurant, 2, 4));
        check(vip.getOrder() == order && order.getBillCents() == 1080
                && restaurant.getOrders().size() == 1, "second order refused, first unchanged");
        check(happyHour.remainingTurns(3) == 2 && happyHour.remainingTurns(4) == 1,
                "the shared policy is never changed by the Waiter");
    }

    private static void testCashierCollectsLockedHappyHourBills() {
        HappyHour happyHour = activeFrom(3);
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter(happyHour);
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        Customer vip = new VIPCustomer(2);
        Customer critic = new CriticCustomer(4);
        restaurant.addArrival(vip);
        restaurant.addArrival(critic);
        expectOk(waiter.seat(restaurant, 2, 1));
        expectOk(waiter.seat(restaurant, 4, 2));
        expectOk(waiter.takeOrder(restaurant, 2, 3));
        expectOk(waiter.takeOrder(restaurant, 4, 4));
        Order vipOrder = restaurant.findOrder(2);
        Order criticOrder = restaurant.findOrder(4);
        check(vipOrder.getBillCents() == 1080 && criticOrder.getBillCents() == 1944, "discounted bills");

        while (vipOrder.canPrepare()) {
            expectOk(chef.prepareOneUnit(vipOrder));
        }
        while (criticOrder.canPrepare()) {
            expectOk(chef.prepareOneUnit(criticOrder));
        }
        expectOk(waiter.serve(restaurant, 2, 6));
        expectOk(waiter.serve(restaurant, 4, 6));
        vip.finishEating(7);
        critic.finishEating(7);
        check(cashier.collectOne(restaurant, 8), "VIP pays first (lower ID)");
        check(restaurant.getRevenueCents() == 1080, "revenue is the VIP's locked Happy Hour bill");
        check(cashier.collectOne(restaurant, 9), "Critic pays next turn");
        check(restaurant.getRevenueCents() == 1080 + 1944, "revenue sums locked bills, no second discount");
        check(restaurant.getPaidCount() == 2, "two payments");
    }

    private static void testWaiterWithoutPolicyKeepsCustomerQuote() {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter();
        restaurant.addArrival(new VIPCustomer(2));
        expectOk(waiter.seat(restaurant, 2, 1));
        expectOk(waiter.takeOrder(restaurant, 2));
        check(restaurant.findOrder(2).getBillCents() == 1350, "compatibility overload: VIP price only");
    }

    private static HappyHour activeFrom(int turn) {
        HappyHour happyHour = new HappyHour();
        happyHour.activate(turn);
        return happyHour;
    }

    /** Seats the customer in a fresh restaurant and returns the locked bill of their order. */
    private static int billFor(Customer customer, HappyHour happyHour, int turn) {
        Restaurant restaurant = new Restaurant();
        Waiter waiter = new Waiter(happyHour);
        restaurant.addArrival(customer);
        expectOk(waiter.seat(restaurant, customer.getId(), 1));
        expectOk(waiter.takeOrder(restaurant, customer.getId(), turn));
        return customer.getOrder().getBillCents();
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
