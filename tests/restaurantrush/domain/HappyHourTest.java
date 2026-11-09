package restaurantrush.domain;

public final class HappyHourTest {
    private static int checks;

    private HappyHourTest() {
    }

    public static void main(String[] args) {
        testStartsUnused();
        testTwoTurnWindow();
        testActivationAtGameEdges();
        testSingleActivation();
        testDiscountAndRounding();
        testDiscountAppliesAfterCustomerQuote();
        testValidation();
        System.out.println("HappyHourTest: PASS (" + checks + " checks)");
    }

    private static void testStartsUnused() {
        HappyHour happyHour = new HappyHour();
        check(!happyHour.hasBeenActivated(), "starts unused");
        for (int turn = 1; turn <= 18; turn++) {
            check(happyHour.remainingTurns(turn) == 0, "inactive on turn " + turn);
            check(happyHour.quotePriceCents(1500, turn) == 1500, "no discount while unused on turn " + turn);
        }
    }

    private static void testTwoTurnWindow() {
        HappyHour happyHour = new HappyHour();
        happyHour.activate(5);
        check(happyHour.hasBeenActivated(), "activated");
        check(happyHour.remainingTurns(4) == 0, "not active before activation");
        check(happyHour.remainingTurns(5) == 2, "2 turns remaining on the activation turn");
        check(happyHour.remainingTurns(6) == 1, "1 turn remaining on the next turn");
        check(happyHour.remainingTurns(7) == 0, "expired after two turns");
        check(happyHour.remainingTurns(18) == 0, "still expired later");
        check(happyHour.remainingTurns(5) == 2 && happyHour.remainingTurns(5) == 2,
                "querying does not change state");
        check(happyHour.quotePriceCents(1500, 4) == 1500, "full price before");
        check(happyHour.quotePriceCents(1500, 5) == 1200, "20% off on activation turn");
        check(happyHour.quotePriceCents(1500, 6) == 1200, "20% off on the following turn");
        check(happyHour.quotePriceCents(1500, 7) == 1500, "full price after expiry");
        check(happyHour.hasBeenActivated(), "still counts as used after expiry");
    }

    private static void testActivationAtGameEdges() {
        HappyHour first = new HappyHour();
        first.activate(1);
        check(first.remainingTurns(1) == 2 && first.remainingTurns(2) == 1 && first.remainingTurns(3) == 0,
                "activation on turn 1");

        HappyHour last = new HappyHour();
        last.activate(18);
        check(last.remainingTurns(17) == 0, "not active before turn 18");
        check(last.remainingTurns(18) == 2, "activation on turn 18 is allowed");
        check(last.quotePriceCents(1800, 18) == 1440, "discount applies on turn 18");
        expectIllegalArgument("no turn 19 to query", () -> last.remainingTurns(19));
    }

    private static void testSingleActivation() {
        HappyHour happyHour = new HappyHour();
        happyHour.activate(3);
        expectIllegalState("second activation", () -> happyHour.activate(10));
        expectIllegalState("reactivating the same turn", () -> happyHour.activate(3));
        check(happyHour.remainingTurns(3) == 2 && happyHour.remainingTurns(10) == 0,
                "rejected activation keeps the original window");
    }

    private static void testDiscountAndRounding() {
        HappyHour happyHour = new HappyHour();
        happyHour.activate(1);
        check(happyHour.quotePriceCents(900, 1) == 720, "Salad 900 -> 720");
        check(happyHour.quotePriceCents(1500, 1) == 1200, "Burger 1500 -> 1200");
        check(happyHour.quotePriceCents(1800, 1) == 1440, "Pasta 1800 -> 1440");
        check(happyHour.quotePriceCents(2430, 1) == 1944, "Combo 2430 -> 1944");
        check(happyHour.quotePriceCents(0, 1) == 0, "zero stays zero");
        check(happyHour.quotePriceCents(4, 1) == 3, "3.2 rounds down to 3");
        check(happyHour.quotePriceCents(7, 1) == 6, "5.6 rounds up to 6");
        check(happyHour.quotePriceCents(2187, 1) == 1750, "1749.6 rounds up to 1750");
        check(happyHour.quotePriceCents(Integer.MAX_VALUE, 1) == 1717986918,
                "largest int quote does not overflow");
    }

    private static void testDiscountAppliesAfterCustomerQuote() {
        HappyHour happyHour = new HappyHour();
        happyHour.activate(2);
        Restaurant restaurant = new Restaurant();
        MenuItem burger = restaurant.getMenu().get(1);
        MenuItem combo = restaurant.getMenu().get(3);

        Customer vip = new VIPCustomer(2);
        check(happyHour.quotePriceCents(vip.quotePriceCents(burger), 2) == 1080,
                "VIP Burger 1500 -> 1350 -> 1080");
        check(happyHour.quotePriceCents(vip.quotePriceCents(combo), 2) == 1750,
                "VIP combo 2430 -> 2187 -> 1750");
        check(happyHour.quotePriceCents(vip.quotePriceCents(combo), 4) == 2187,
                "VIP combo after expiry keeps only the VIP discount");

        Customer critic = new CriticCustomer(4);
        check(happyHour.quotePriceCents(critic.quotePriceCents(combo), 3) == 1944,
                "Critic combo 2430 -> 1944");
        Customer regular = new RegularCustomer(1);
        check(happyHour.quotePriceCents(regular.quotePriceCents(burger), 1) == 1500,
                "no discount on the turn before activation");
    }

    private static void testValidation() {
        HappyHour happyHour = new HappyHour();
        expectIllegalArgument("activate turn 0", () -> happyHour.activate(0));
        expectIllegalArgument("activate turn 19", () -> happyHour.activate(19));
        check(!happyHour.hasBeenActivated(), "rejected activation leaves it unused");
        expectIllegalArgument("remaining for turn 0", () -> happyHour.remainingTurns(0));
        expectIllegalArgument("remaining for turn 19", () -> happyHour.remainingTurns(19));
        expectIllegalArgument("negative quote", () -> happyHour.quotePriceCents(-1, 1));
        expectIllegalArgument("quote for turn 0", () -> happyHour.quotePriceCents(900, 0));
        expectIllegalArgument("quote for turn 19", () -> happyHour.quotePriceCents(900, 19));
        happyHour.activate(1);
        expectIllegalArgument("negative quote while active", () -> happyHour.quotePriceCents(-5, 1));
        check(happyHour.remainingTurns(1) == 2, "rejected calls leave the window unchanged");
    }

    private static void expectIllegalArgument(String description, Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError(description + ": expected IllegalArgumentException");
    }

    private static void expectIllegalState(String description, Runnable action) {
        try {
            action.run();
        } catch (IllegalStateException expected) {
            checks++;
            return;
        }
        throw new AssertionError(description + ": expected IllegalStateException");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }
}
