package restaurantrush.domain;

/** The customer's lifecycle, including eating and readiness to pay. */
public enum CustomerStatus {
    WAITING,
    SEATED,
    EATING,
    READY_TO_PAY,
    PAID,
    LEFT
}
