package restaurantrush.domain;

/**
 * Customer lifecycle states, separate from dish preparation in {@link OrderStatus}.
 * Normal flow is WAITING, SEATED, EATING, READY_TO_PAY, PAID; waiting or seated
 * customers may instead become LEFT. PAID and LEFT are terminal.
 */
public enum CustomerStatus {
    /** Queued and awaiting a table. */
    WAITING,
    /** Seated but not yet eating, including while an order is being prepared or is ready. */
    SEATED,
    /** Served and waiting for a later turn to finish eating. */
    EATING,
    /** Finished eating; collaborating systems determine payment eligibility. */
    READY_TO_PAY,
    /** Completed payment. */
    PAID,
    /** Departed through abandonment. */
    LEFT
}
