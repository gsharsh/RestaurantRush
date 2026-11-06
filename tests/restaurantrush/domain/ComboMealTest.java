package restaurantrush.domain;

/** Offline checks for combo values, shared components and arithmetic boundaries. */
public final class ComboMealTest {
    private static int checks;

    private ComboMealTest() {
    }

    public static void main(String[] args) {
        int before = checks;
        testSaladAndPastaCombo();
        testComponentIdentity();
        testInvalidInputs();
        testDisplayNamePreserved();
        testZeroPrices();
        testHalfUpRounding();
        testPriceBoundaries();
        testPreparationBoundaries();
        testOrderUsesComboAsMenuItem();
        System.out.println("ComboMealTest: PASS (" + (checks - before) + " checks)");
    }

    private static void testSaladAndPastaCombo() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        ComboMeal combo = new ComboMeal("Salad + Pasta Combo", salad, pasta);
        checkMenuItem(combo, "Salad + Pasta Combo", 2430, 3);
        check(combo.getFirst() == salad, "first component must be the existing Salad");
        check(combo.getSecond() == pasta, "second component must be the existing Pasta");
        checkMenuItem(salad, "Salad", 900, 1);
        checkMenuItem(pasta, "Pasta", 1800, 2);
    }

    private static void testComponentIdentity() {
        MenuItem first = new MenuItem("Dish", 100, 1);
        MenuItem second = new MenuItem("Dish", 100, 1);
        ComboMeal distinct = new ComboMeal("Distinct components", first, second);
        check(distinct.getFirst() == first, "first identity must survive equal component values");
        check(distinct.getSecond() == second, "second identity must survive equal component values");

        ComboMeal repeated = new ComboMeal("Repeated component", first, first);
        check(repeated.getFirst() == first, "same instance may be used as the first component");
        check(repeated.getSecond() == first, "same instance may also be the second component");
        checkMenuItem(repeated, "Repeated component", 180, 2);

        ComboMeal shared = new ComboMeal("Shared components", second, first);
        check(shared.getFirst() == second, "components may be shared across combos");
        check(shared.getSecond() == first, "component order must be preserved");
        checkMenuItem(first, "Dish", 100, 1);
        checkMenuItem(second, "Dish", 100, 1);
    }

    private static void testInvalidInputs() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        expectIllegalArgument("null name", () -> new ComboMeal(null, salad, pasta));
        expectIllegalArgument("empty name", () -> new ComboMeal("", salad, pasta));
        expectIllegalArgument("space-only name", () -> new ComboMeal("   ", salad, pasta));
        expectIllegalArgument("mixed whitespace name", () -> new ComboMeal(" \t\r\n ", salad, pasta));
        expectIllegalArgument("Unicode whitespace name", () -> new ComboMeal("\u2003", salad, pasta));
        expectIllegalArgument("null first component", () -> new ComboMeal("Combo", null, pasta));
        expectIllegalArgument("null second component", () -> new ComboMeal("Combo", salad, null));
        expectIllegalArgument("both components null", () -> new ComboMeal("Combo", null, null));
        checkMenuItem(salad, "Salad", 900, 1);
        checkMenuItem(pasta, "Pasta", 1800, 2);
    }

    private static void testDisplayNamePreserved() {
        String name = "  House Combo \t";
        MenuItem item = new MenuItem("Dish", 100, 1);
        checkMenuItem(new ComboMeal(name, item, item), name, 180, 2);
    }

    private static void testZeroPrices() {
        MenuItem free = new MenuItem("Free", 0, 1);
        MenuItem paid = new MenuItem("Paid", 100, 2);
        checkMenuItem(new ComboMeal("Free combo", free, free), "Free combo", 0, 2);
        checkMenuItem(new ComboMeal("Free first", free, paid), "Free first", 90, 3);
        checkMenuItem(new ComboMeal("Free second", paid, free), "Free second", 90, 3);
    }

    private static void testHalfUpRounding() {
        // Round the combined total once: 5 cents gives 4.5 -> 5; 6 gives 5.4 -> 5.
        int[] totals = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 14, 15, 16};
        int[] expectedPrices = {1, 2, 3, 4, 5, 5, 6, 7, 8, 9, 10, 13, 14, 14};
        MenuItem free = new MenuItem("Free", 0, 1);
        for (int index = 0; index < totals.length; index++) {
            MenuItem priced = new MenuItem("Priced", totals[index], 1);
            ComboMeal combo = new ComboMeal("Rounded", priced, free);
            check(combo.getPriceCents() == expectedPrices[index],
                    "rounded price for " + totals[index] + " cents: expected " + expectedPrices[index]
                            + ", got " + combo.getPriceCents());
        }

        MenuItem three = new MenuItem("Three cents", 3, 1);
        checkMenuItem(new ComboMeal("Round combined total", three, three),
                "Round combined total", 5, 2);
        MenuItem two = new MenuItem("Two cents", 2, 1);
        checkMenuItem(new ComboMeal("Combined half cent", two, three),
                "Combined half cent", 5, 2);
    }

    private static void testPriceBoundaries() {
        MenuItem maximum = new MenuItem("Maximum price", Integer.MAX_VALUE, 1);
        MenuItem free = new MenuItem("Free", 0, 1);
        checkMenuItem(new ComboMeal("Maximum component", maximum, free),
                "Maximum component", 1_932_735_282, 2);

        // 2,200,000,000 cents exceeds int range, but its discounted result fits.
        MenuItem large = new MenuItem("Large price", 1_100_000_000, 1);
        checkMenuItem(new ComboMeal("Large combined price", large, large),
                "Large combined price", 1_980_000_000, 2);

        // These adjacent combined prices round to MAX_VALUE and MAX_VALUE + 1.
        MenuItem lastFitting = new MenuItem("Last fitting price", 238_609_294, 1);
        checkMenuItem(new ComboMeal("Maximum combo price", maximum, lastFitting),
                "Maximum combo price", Integer.MAX_VALUE, 2);
        MenuItem firstOverflow = new MenuItem("First overflowing price", 238_609_295, 1);
        expectIllegalArgument("discounted price just above maximum",
                () -> new ComboMeal("Overflow", maximum, firstOverflow));
        expectIllegalArgument("two maximum prices",
                () -> new ComboMeal("Overflow", maximum, maximum));
        checkMenuItem(maximum, "Maximum price", Integer.MAX_VALUE, 1);
    }

    private static void testPreparationBoundaries() {
        MenuItem one = new MenuItem("One unit", 0, 1);
        MenuItem almostMaximum = new MenuItem("Almost maximum units", 0, Integer.MAX_VALUE - 1);
        checkMenuItem(new ComboMeal("Maximum combo units", almostMaximum, one),
                "Maximum combo units", 0, Integer.MAX_VALUE);
        MenuItem maximum = new MenuItem("Maximum units", 0, Integer.MAX_VALUE);
        expectIllegalArgument("preparation units just above maximum",
                () -> new ComboMeal("Overflow", maximum, one));
        expectIllegalArgument("two maximum preparation counts",
                () -> new ComboMeal("Overflow", maximum, maximum));
        checkMenuItem(maximum, "Maximum units", 0, Integer.MAX_VALUE);
    }

    private static void testOrderUsesComboAsMenuItem() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        MenuItem combo = new ComboMeal("Salad + Pasta Combo", salad, pasta);
        Order order = new Order(1, combo, combo.getPriceCents());
        check(order.getItem() == combo, "order must retain the combo as its single MenuItem");
        check(order.getRemainingUnits() == 3, "combo order starts with all three preparation units");
        check(order.getBillCents() == 2430, "combo order locks one combined bill");
        for (int remaining = 2; remaining >= 0; remaining--) {
            order.prepareOneUnit();
            check(order.getRemainingUnits() == remaining, "combo order remaining units: " + remaining);
            check(order.getStatus() == (remaining == 0 ? OrderStatus.READY : OrderStatus.PREPARING),
                    "combo is ready only after the third preparation action");
        }
        order.markServed();
        order.markPaid();
        check(order.getStatus() == OrderStatus.PAID, "combo order follows the existing payment lifecycle");
        check(order.getBillCents() == 2430, "combo bill remains locked through payment");
        checkMenuItem(combo, "Salad + Pasta Combo", 2430, 3);
        checkMenuItem(salad, "Salad", 900, 1);
        checkMenuItem(pasta, "Pasta", 1800, 2);
    }

    private static void checkMenuItem(MenuItem item, String name, int priceCents, int preparationUnits) {
        check(name.equals(item.getName()), "display name: expected [" + name + "], got [" + item.getName() + "]");
        check(item.getPriceCents() == priceCents,
                name + " price: expected " + priceCents + ", got " + item.getPriceCents());
        check(item.getPreparationUnits() == preparationUnits,
                name + " preparation units: expected " + preparationUnits + ", got " + item.getPreparationUnits());
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

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }
}
