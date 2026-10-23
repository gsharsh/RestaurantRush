package restaurantrush.domain;

/**
 * Order lifecycle states, separate from customer eating and payment readiness.
 * Preparation reaches READY before service and payment; a one-unit order skips
 * PREPARING. PLACED, PREPARING and READY orders may be cancelled. PAID and
 * CANCELLED are terminal.
 */
public enum OrderStatus {
    /** Created with its full preparation requirement. */
    PLACED,
    /** At least one unit has been prepared and more units remain. */
    PREPARING,
    /** Preparation is complete and the dish has not yet been served. */
    READY,
    /** Handed over to the customer. */
    SERVED,
    /** Payment for the locked bill has completed. */
    PAID,
    /** Cancelled before service; collaborating systems retain the order in history. */
    CANCELLED
}
