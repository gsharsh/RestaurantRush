package restaurantrush.domain;

/** The dish's lifecycle, including preparation readiness and service. */
public enum OrderStatus {
    PLACED,
    PREPARING,
    READY,
    SERVED,
    PAID,
    CANCELLED
}
