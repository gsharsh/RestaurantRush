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

/** Host behaviour and how it combines with the Waiter and Chef within one turn. */
public final class HostTest {
    private static int checks;

    private HostTest() {
    }

    public static void main(String[] args) {
        testHostSeatsAndRejectsWithReasons();
        testHostSeatsEveryCustomerType();
        testHostAndWaiterEachSeatADifferentCustomer();
        testHostThenWaiterThenChefInOneTurn();
        System.out.println("HostTest: PASS (" + checks + " checks)");
    }

    private static void testHostSeatsAndRejectsWithReasons() {
        Restaurant restaurant = new Restaurant();
        Host host = new Host();
        restaurant.addArrival(new RegularCustomer(1));
        restaurant.addArrival(new RegularCustomer(2));
        restaurant.addArrival(new RegularCustomer(3));

        expectFail(host.seat(null, 1, 1));
        expectFail(host.seat(restaurant, 9, 1));
        expectFail(host.seat(restaurant, 1, 9));
        check(restaurant.getWaitingQueue().size() == 3, "refused seating leaves the queue unchanged");
        check(restaurant.findCustomer(1).getStatus() == CustomerStatus.WAITING, "still waiting");

        expectOk(host.seat(restaurant, 1, 1));
        check(restaurant.findCustomer(1).getStatus() == CustomerStatus.SEATED, "seated");
        check(restaurant.findTableForCustomer(1).getId() == 1, "at table 1");
        expectFail(host.seat(restaurant, 1, 2));
        expectFail(host.seat(restaurant, 2, 1));
        expectOk(host.seat(restaurant, 2, 2));
        expectFail(host.seat(restaurant, 3, 1));
        expectFail(host.seat(restaurant, 3, 2));
        check(restaurant.getWaitingQueue().size() == 1, "customer 3 still queued, both tables full");
    }

    private static void testHostSeatsEveryCustomerType() {
        Restaurant restaurant = new Restaurant();
        Host host = new Host();
        Customer[] customers = {new RegularCustomer(1), new VIPCustomer(2)};
        Customer critic = new CriticCustomer(4);
        for (Customer customer : customers) {
            restaurant.addArrival(customer);
        }
        restaurant.addArrival(critic);
        expectOk(host.seat(restaurant, 1, 1));
        expectOk(host.seat(restaurant, 2, 2));
        check(customers[0].getStatus() == CustomerStatus.SEATED
                && customers[1].getStatus() == CustomerStatus.SEATED, "Regular and VIP seated");
        expectFail(host.seat(restaurant, 4, 1));
        check(critic.getStatus() == CustomerStatus.WAITING, "Critic waits for a free table");
    }

    private static void testHostAndWaiterEachSeatADifferentCustomer() {
        Restaurant restaurant = new Restaurant();
        Host host = new Host();
        Waiter waiter = new Waiter(new restaurantrush.domain.HappyHour());
        restaurant.addArrival(new RegularCustomer(1));
        restaurant.addArrival(new VIPCustomer(2));
        expectOk(host.seat(restaurant, 1, 1));
        expectOk(waiter.seat(restaurant, 2, 2));
        check(restaurant.getWaitingQueue().isEmpty(), "both customers seated");
        check(restaurant.findTableForCustomer(1).getId() == 1
                && restaurant.findTableForCustomer(2).getId() == 2, "each at their own table");
        expectFail(waiter.seat(restaurant, 1, 2));
    }

    private static void testHostThenWaiterThenChefInOneTurn() {
        Restaurant restaurant = new Restaurant();
        Host host = new Host();
        Waiter waiter = new Waiter(new restaurantrush.domain.HappyHour());
        Chef chef = new Chef();
        restaurant.addArrival(new VIPCustomer(2));

        expectOk(host.seat(restaurant, 2, 1));
        expectOk(waiter.takeOrder(restaurant, 2, 4));
        Order order = restaurant.findOrder(2);
        check(order.getStatus() == OrderStatus.PLACED && order.getBillCents() == 1350,
                "ordered on the same turn it was seated, at the VIP price");
        expectOk(chef.prepareOneUnit(order));
        check(order.isReady(), "one-unit Burger is READY the same turn");
        expectOk(waiter.serve(restaurant, 2, 5));
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
