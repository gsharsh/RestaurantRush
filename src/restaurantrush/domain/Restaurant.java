package restaurantrush.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * The restaurant's shared state: waiting queue, tables, menu, order history
 * and running totals.
 *
 * <p>Every operation that changes more than one object (seating, linking an
 * order, serving, payment, abandonment) is coordinated here. Each one checks
 * the whole request first and only mutates once it is known to succeed, so a
 * rejected manager action leaves the game exactly as it was.
 */
public final class Restaurant {

    private final Deque<Customer> waitingQueue;
    private final List<Customer> customers;
    private final List<Table> tables;
    private final List<MenuItem> menu;
    private final List<Order> orders;

    private int revenueCents;
    private int servedCount;
    private int abandonedCount;
    private int paidSatisfactionTotal;
    private int paidCount;

    public Restaurant() {
        this.waitingQueue = new ArrayDeque<>();
        this.customers = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.tables = List.of(new Table(1), new Table(2));
        this.menu = List.of(
                new MenuItem("Salad", 900, 1),
                new MenuItem("Burger", 1500, 1),
                new MenuItem("Pasta", 1800, 2));
        this.revenueCents = 0;
        this.servedCount = 0;
        this.abandonedCount = 0;
        this.paidSatisfactionTotal = 0;
        this.paidCount = 0;
    }

    // ------------------------------------------------------------------
    // Arrivals and manager-driven actions
    // ------------------------------------------------------------------

    /** Adds a newly arrived customer to both the history and the back of the queue. */
    public void addArrival(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Arriving customer must not be null");
        }
        if (findCustomer(customer.getId()) != null) {
            throw new IllegalArgumentException("Customer " + customer.getId() + " has already arrived");
        }
        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalArgumentException("Customer " + customer.getId()
                    + " must arrive WAITING, not " + customer.getStatus());
        }
        customers.add(customer);
        waitingQueue.addLast(customer);
    }

    /** Moves a queued customer to a free table. */
    public ActionResult seat(int customerId, int tableId) {
        Customer customer = findCustomer(customerId);
        if (customer == null) {
            return ActionResult.fail("There is no customer " + customerId + ".");
        }
        if (customer.getStatus() != CustomerStatus.WAITING || !waitingQueue.contains(customer)) {
            return ActionResult.fail("Customer " + customerId + " is not in the waiting queue ("
                    + customer.getStatus() + ").");
        }
        Table table = findTable(tableId);
        if (table == null) {
            return ActionResult.fail("There is no table " + tableId + ".");
        }
        if (!table.isAvailable()) {
            return ActionResult.fail("Table " + tableId + " is occupied by customer "
                    + table.getOccupant().getId() + ".");
        }

        waitingQueue.remove(customer);
        table.assign(customer);
        customer.seat();
        return ActionResult.ok("Customer " + customerId + " is seated at table " + tableId + ".");
    }

    /**
     * Links a new order to its seated customer and records it. Customer and
     * Restaurant end up holding the same Order object.
     */
    public ActionResult registerOrder(int customerId, Order order) {
        Customer customer = findCustomer(customerId);
        if (customer == null) {
            return ActionResult.fail("There is no customer " + customerId + ".");
        }
        if (customer.getStatus() != CustomerStatus.SEATED) {
            return ActionResult.fail("Customer " + customerId + " must be seated before ordering ("
                    + customer.getStatus() + ").");
        }
        if (customer.getOrder() != null) {
            return ActionResult.fail("Customer " + customerId + " has already ordered.");
        }
        if (order == null) {
            return ActionResult.fail("No order was supplied for customer " + customerId + ".");
        }
        if (order.getId() != customerId) {
            return ActionResult.fail("Order " + order.getId() + " does not belong to customer "
                    + customerId + ".");
        }
        if (findOrder(order.getId()) != null) {
            return ActionResult.fail("Order " + order.getId() + " is already registered.");
        }

        customer.acceptOrder(order);
        orders.add(order);
        return ActionResult.ok("Customer " + customerId + " ordered " + order.getItem().getName()
                + " ($" + formatCents(order.getBillCents()) + ").");
    }

    /** Hands a READY dish to its seated customer, who then starts eating. */
    public ActionResult serve(int customerId, int turn) {
        requirePositiveTurn(turn);
        Customer customer = findCustomer(customerId);
        if (customer == null) {
            return ActionResult.fail("There is no customer " + customerId + ".");
        }
        if (customer.getStatus() != CustomerStatus.SEATED || findTableForCustomer(customerId) == null) {
            return ActionResult.fail("Customer " + customerId + " is not a seated customer waiting for food ("
                    + customer.getStatus() + ").");
        }
        Order order = customer.getOrder();
        if (order == null || findOrder(customerId) != order) {
            return ActionResult.fail("Customer " + customerId + " has not ordered yet.");
        }
        if (!order.isReady()) {
            return ActionResult.fail("Customer " + customerId + "'s " + order.getItem().getName()
                    + " is not READY (" + order.getStatus() + ", " + order.getRemainingUnits()
                    + " unit(s) left).");
        }

        order.markServed();
        customer.markServed(turn);
        servedCount++;
        return ActionResult.ok("Served " + order.getItem().getName() + " to customer " + customerId + ".");
    }

    // ------------------------------------------------------------------
    // Automatic end-of-turn events
    // ------------------------------------------------------------------

    /**
     * Returns the customer the Cashier should charge this turn: READY_TO_PAY
     * since an earlier turn, earliest first, ties broken by lower ID. Returns
     * {@code null} if nobody is eligible.
     */
    public Customer oldestEligiblePayer(int turn) {
        requirePositiveTurn(turn);
        return customers.stream()
                .filter(c -> c.getStatus() == CustomerStatus.READY_TO_PAY)
                .filter(c -> c.getReadyToPayTurn() < turn)
                .min(Comparator.comparingInt(Customer::getReadyToPayTurn)
                        .thenComparingInt(Customer::getId))
                .orElse(null);
    }

    /**
     * Charges an eligible customer: adds the locked bill to revenue, records
     * their satisfaction and frees their table. Returns {@code false} and
     * changes nothing if the customer is not eligible.
     */
    public boolean recordPayment(Customer customer, int turn) {
        if (customer == null || turn <= 0) {
            return false;
        }
        if (findCustomer(customer.getId()) != customer) {
            return false;
        }
        if (customer.getStatus() != CustomerStatus.READY_TO_PAY || customer.getReadyToPayTurn() >= turn) {
            return false;
        }
        Order order = customer.getOrder();
        if (order == null || order.getStatus() != OrderStatus.SERVED) {
            return false;
        }
        Table table = findTableForCustomer(customer.getId());
        if (table == null) {
            return false;
        }

        order.markPaid();
        customer.markPaid();
        table.release();
        revenueCents += order.getBillCents();
        paidSatisfactionTotal += customer.getSatisfaction();
        paidCount++;
        return true;
    }

    /**
     * Removes a customer whose satisfaction reached zero while waiting or
     * seated: cancels any unfinished order and frees their queue place or table.
     */
    public void removeAbandoned(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Abandoning customer must not be null");
        }
        if (findCustomer(customer.getId()) != customer) {
            throw new IllegalArgumentException("Customer " + customer.getId() + " is not in this restaurant");
        }
        if (customer.getSatisfaction() != 0) {
            throw new IllegalStateException("Customer " + customer.getId()
                    + " still has satisfaction " + customer.getSatisfaction());
        }
        CustomerStatus status = customer.getStatus();
        if (status != CustomerStatus.WAITING && status != CustomerStatus.SEATED) {
            throw new IllegalStateException("Customer " + customer.getId()
                    + " cannot abandon while " + status);
        }
        Table table = findTableForCustomer(customer.getId());
        if (status == CustomerStatus.SEATED && table == null) {
            throw new IllegalStateException("Seated customer " + customer.getId() + " has no table");
        }

        customer.abandon();
        if (status == CustomerStatus.WAITING) {
            waitingQueue.remove(customer);
        } else {
            table.release();
        }
        abandonedCount++;
    }

    // ------------------------------------------------------------------
    // Lookups (nullable)
    // ------------------------------------------------------------------

    public Customer findCustomer(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                return customer;
            }
        }
        return null;
    }

    public Order findOrder(int id) {
        for (Order order : orders) {
            if (order.getId() == id) {
                return order;
            }
        }
        return null;
    }

    public Table findTable(int id) {
        for (Table table : tables) {
            if (table.getId() == id) {
                return table;
            }
        }
        return null;
    }

    public Table findTableForCustomer(int customerId) {
        for (Table table : tables) {
            Customer occupant = table.getOccupant();
            if (occupant != null && occupant.getId() == customerId) {
                return table;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Read-only views and totals
    // ------------------------------------------------------------------

    /** Customers still in the restaurant (everyone except PAID and LEFT). */
    public List<Customer> getActiveCustomers() {
        return customers.stream()
                .filter(c -> c.getStatus() != CustomerStatus.PAID && c.getStatus() != CustomerStatus.LEFT)
                .toList();
    }

    public List<Customer> getWaitingQueue() {
        return List.copyOf(waitingQueue);
    }

    public List<Customer> getCustomers() {
        return List.copyOf(customers);
    }

    public List<Table> getTables() {
        return tables;
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    public List<Order> getOrders() {
        return List.copyOf(orders);
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getAbandonedCount() {
        return abandonedCount;
    }

    public int getPaidSatisfactionTotal() {
        return paidSatisfactionTotal;
    }

    /** Average satisfaction recorded at payment, or 0.0 before anyone has paid. */
    public double getAveragePaidSatisfaction() {
        if (paidCount == 0) {
            return 0.0;
        }
        return (double) paidSatisfactionTotal / paidCount;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void requirePositiveTurn(int turn) {
        if (turn <= 0) {
            throw new IllegalArgumentException("Turn must be positive: " + turn);
        }
    }

    private static String formatCents(int cents) {
        return String.format("%d.%02d", cents / 100, cents % 100);
    }
}
