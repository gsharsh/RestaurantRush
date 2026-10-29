package restaurantrush.view;

import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.Order;
import restaurantrush.domain.OrderStatus;

/**
 * Read-only copy of one customer and their order for display.
 *
 * <p>The three order fields are either all {@code null} (no order yet) or all
 * present. The record stores values, not references to the live
 * {@link Customer} or {@link Order}.
 *
 * @param id             positive customer ID
 * @param satisfaction   current satisfaction, 0 to 100
 * @param status         customer lifecycle state
 * @param orderStatus    order state, or {@code null} before ordering
 * @param itemName       ordered dish, or {@code null} before ordering
 * @param remainingUnits Chef units still needed, or {@code null} before ordering
 * @param tableId        table the customer occupies, or {@code null}
 */
public record CustomerView(
        int id,
        int satisfaction,
        CustomerStatus status,
        OrderStatus orderStatus,
        String itemName,
        Integer remainingUnits,
        Integer tableId) {

    public CustomerView {
        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive: " + id);
        }
        if (satisfaction < 0 || satisfaction > 100) {
            throw new IllegalArgumentException("Satisfaction must be 0 to 100: " + satisfaction);
        }
        if (status == null) {
            throw new IllegalArgumentException("Customer status must not be null");
        }
        boolean noOrder = orderStatus == null && itemName == null && remainingUnits == null;
        boolean fullOrder = orderStatus != null && itemName != null && remainingUnits != null;
        if (!noOrder && !fullOrder) {
            throw new IllegalArgumentException(
                    "Order status, item name and remaining units must be all present or all null");
        }
        if (itemName != null && itemName.isBlank()) {
            throw new IllegalArgumentException("Item name must not be blank");
        }
        if (remainingUnits != null && remainingUnits < 0) {
            throw new IllegalArgumentException("Remaining units must not be negative: " + remainingUnits);
        }
        if (tableId != null && tableId <= 0) {
            throw new IllegalArgumentException("Table ID must be positive: " + tableId);
        }
    }

    /**
     * Copies the current state of a customer.
     *
     * @param customer the customer to copy
     * @param tableId  the table they occupy, or {@code null} if none
     */
    public static CustomerView from(Customer customer, Integer tableId) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }
        Order order = customer.getOrder();
        if (order == null) {
            return new CustomerView(customer.getId(), customer.getSatisfaction(), customer.getStatus(),
                    null, null, null, tableId);
        }
        return new CustomerView(customer.getId(), customer.getSatisfaction(), customer.getStatus(),
                order.getStatus(), order.getItem().getName(), order.getRemainingUnits(), tableId);
    }
}
