package restaurantrush.domain;

/** Offline tests for M1's domain types; add later feature tests to main. */
public final class M1DomainTests {
    private static int checks;

    private M1DomainTests() {
    }

    public static void main(String[] args) {
        testAgreedMenuItems();
        testInvalidNames();
        testDisplayNamePreserved();
        testNonnegativePrices();
        testPositivePreparationUnits();
        testCustomerStatusConstants();
        testOrderStatusConstants();
        System.out.println("M1DomainTests: PASS (" + checks + " checks)");
    }

    private static void testAgreedMenuItems() {
        checkMenuItem(new MenuItem("Salad", 900, 1), "Salad", 900, 1);
        checkMenuItem(new MenuItem("Burger", 1500, 1), "Burger", 1500, 1);
        checkMenuItem(new MenuItem("Pasta", 1800, 2), "Pasta", 1800, 2);
    }

    private static void testInvalidNames() {
        expectIllegalArgument("null name", () -> new MenuItem(null, 900, 1));
        expectIllegalArgument("empty name", () -> new MenuItem("", 900, 1));
        expectIllegalArgument("space-only name", () -> new MenuItem("   ", 900, 1));
        expectIllegalArgument("mixed whitespace name", () -> new MenuItem(" \t\r\n ", 900, 1));
        expectIllegalArgument("Unicode whitespace name", () -> new MenuItem("\u2003", 900, 1));
    }

    private static void testDisplayNamePreserved() {
        String displayName = "  House Salad \t";
        checkMenuItem(new MenuItem(displayName, 900, 1), displayName, 900, 1);
    }

    private static void testNonnegativePrices() {
        expectIllegalArgument("negative price", () -> new MenuItem("Salad", -1, 1));
        expectIllegalArgument("minimum integer price", () -> new MenuItem("Salad", Integer.MIN_VALUE, 1));
        checkMenuItem(new MenuItem("Salad", 0, 1), "Salad", 0, 1);
    }

    private static void testPositivePreparationUnits() {
        expectIllegalArgument("zero preparation units", () -> new MenuItem("Pasta", 1800, 0));
        expectIllegalArgument("negative preparation units", () -> new MenuItem("Pasta", 1800, -1));
        expectIllegalArgument("minimum integer preparation units",
                () -> new MenuItem("Pasta", 1800, Integer.MIN_VALUE));
    }

    private static void testCustomerStatusConstants() {
        checkEnumConstants(CustomerStatus.values(),
                "WAITING", "SEATED", "EATING", "READY_TO_PAY", "PAID", "LEFT");
    }

    private static void testOrderStatusConstants() {
        checkEnumConstants(OrderStatus.values(),
                "PLACED", "PREPARING", "READY", "SERVED", "PAID", "CANCELLED");
    }

    private static void checkMenuItem(MenuItem item, String name, int priceCents, int preparationUnits) {
        check(name.equals(item.getName()), "display name: expected [" + name + "], got [" + item.getName() + "]");
        check(item.getPriceCents() == priceCents,
                name + " price: expected " + priceCents + ", got " + item.getPriceCents());
        check(item.getPreparationUnits() == preparationUnits,
                name + " preparation units: expected " + preparationUnits + ", got " + item.getPreparationUnits());
    }

    private static void checkEnumConstants(Enum<?>[] actual, String... expected) {
        check(actual.length == expected.length,
                "enum constant count: expected " + expected.length + ", got " + actual.length);
        for (int index = 0; index < expected.length; index++) {
            check(expected[index].equals(actual[index].name()),
                    "enum constant at index " + index + ": expected " + expected[index]
                            + ", got " + actual[index].name());
        }
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
