package restaurantrush.domain;

public final class HappyHour {
    private static final int FIRST_TURN = 1;
    private static final int LAST_TURN = 18;
    private static final int DURATION_TURNS = 2;

    private int activationTurn;

    public HappyHour() {
        this.activationTurn = 0;
    }

    public boolean hasBeenActivated() {
        return activationTurn != 0;
    }

    public void activate(int turn) {
        requireValidTurn(turn);
        if (hasBeenActivated()) {
            throw new IllegalStateException("Happy Hour was already activated on turn " + activationTurn + ".");
        }
        activationTurn = turn;
    }

    public int remainingTurns(int turn) {
        requireValidTurn(turn);
        if (!hasBeenActivated() || turn < activationTurn) {
            return 0;
        }
        return Math.max(0, DURATION_TURNS - (turn - activationTurn));
    }

    public int quotePriceCents(int customerQuoteCents, int turn) {
        if (customerQuoteCents < 0) {
            throw new IllegalArgumentException("Quote must not be negative: " + customerQuoteCents);
        }
        if (remainingTurns(turn) == 0) {
            return customerQuoteCents;
        }
        return (int) ((8L * customerQuoteCents + 5L) / 10L);
    }

    private static void requireValidTurn(int turn) {
        if (turn < FIRST_TURN || turn > LAST_TURN) {
            throw new IllegalArgumentException("Turn must be between " + FIRST_TURN + " and " + LAST_TURN
                    + ": " + turn);
        }
    }
}
