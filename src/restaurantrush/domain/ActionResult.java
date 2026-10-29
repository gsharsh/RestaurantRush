package restaurantrush.domain;

/**
 * Outcome of a player-requested action. A failure carries the reason, so the
 * boundary only has to display {@link #message()}. Holds no printing or domain
 * mutation.
 *
 * @param success whether the action was carried out
 * @param message non-null, nonblank explanation shown to the manager
 */
public record ActionResult(boolean success, String message) {

    public ActionResult {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must be non-null and nonblank.");
        }
    }

    /** Creates a successful result. */
    public static ActionResult ok(String message) {
        return new ActionResult(true, message);
    }

    /** Creates a failed result whose message explains why. */
    public static ActionResult fail(String message) {
        return new ActionResult(false, message);
    }
}
