package restaurantrush.domain;

import java.util.ArrayList;
import java.util.List;

public final class CriticCustomerTest {
    private static int checks;

    private CriticCustomerTest() {
    }

    public static void main(String[] args) {
        testBaseMenuChoiceAndPrice();
        testComboMenuChoiceAndPrice();
        testPreferenceTieBreaking();
        testWaitingLossAndAbandonment();
        testNoLossAfterService();
        testFullLifecycleThroughRestaurant();
        testValidation();
        System.out.println("CriticCustomerTest: PASS (" + checks + " checks)");
    }

    private static void testBaseMenuChoiceAndPrice() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        List<MenuItem> menu = new ArrayList<>(List.of(salad, burger, pasta));
        for (int id = 1; id <= 6; id++) {
            Customer critic = new CriticCustomer(id);
            MenuItem chosen = critic.chooseMenuItem(menu);
            check(chosen == pasta, "critic " + id + " chooses Pasta");
            check(critic.quotePriceCents(chosen) == 1800, "critic pays full price for Pasta");
        }
        check(new RegularCustomer(4).chooseMenuItem(menu) == salad,
                "a Regular customer with ID 4 chooses Salad, so the Critic's choice is distinct");
        check(menu.size() == 3 && menu.get(0) == salad && menu.get(1) == burger && menu.get(2) == pasta,
                "choosing does not reorder or change the menu");
        check(new CriticCustomer(1).chooseMenuItem(List.of(pasta, salad, burger)) == pasta,
                "choice does not depend on menu order");
        check(new CriticCustomer(1).quotePriceCents(new MenuItem("Free", 0, 1)) == 0,
                "a zero price is quoted unchanged");
        Customer critic = new CriticCustomer(1);
        check("Critic".equals(critic.typeLabel()), "type label is Critic");
        check(critic.waitingLossPerTurn() == 12, "waiting loss is 12");
    }

    private static void testComboMenuChoiceAndPrice() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        MenuItem combo = new ComboMeal("Salad + Pasta Combo", salad, pasta);
        List<List<MenuItem>> menus = List.of(
                new ArrayList<>(List.of(salad, burger, pasta, combo)),
                new ArrayList<>(List.of(pasta, combo, burger, salad)));
        Customer critic = new CriticCustomer(1);
        for (List<MenuItem> menu : menus) {
            List<MenuItem> original = new ArrayList<>(menu);
            MenuItem chosen = critic.chooseMenuItem(menu);
            check(chosen == combo, "critic chooses the three-unit combo over the two-unit Pasta");
            check(critic.quotePriceCents(chosen) == 2430, "critic quotes the full combo price");
            check(menu.size() == original.size(), "combo selection must preserve menu size");
            for (int index = 0; index < original.size(); index++) {
                check(menu.get(index) == original.get(index),
                        "combo selection must preserve menu order and object identity at " + index);
            }
        }
    }

    private static void testPreferenceTieBreaking() {
        Customer critic = new CriticCustomer(1);
        MenuItem cheapSlow = new MenuItem("Stew", 500, 3);
        MenuItem pricyFast = new MenuItem("Steak", 5000, 1);
        check(critic.chooseMenuItem(List.of(pricyFast, cheapSlow)) == cheapSlow,
                "more preparation units wins even when cheaper");

        MenuItem cheaper = new MenuItem("Risotto", 1700, 2);
        MenuItem dearer = new MenuItem("Pasta", 1800, 2);
        check(critic.chooseMenuItem(List.of(cheaper, dearer)) == dearer,
                "equal units: higher price wins");
        check(critic.chooseMenuItem(List.of(dearer, cheaper)) == dearer,
                "equal units: higher price wins regardless of order");

        MenuItem first = new MenuItem("Pasta", 1800, 2);
        MenuItem second = new MenuItem("Gnocchi", 1800, 2);
        check(critic.chooseMenuItem(List.of(first, second)) == first,
                "equal units and price: earliest item wins");
        check(critic.chooseMenuItem(List.of(second, first)) == second,
                "equal units and price: earliest item wins in either order");

        MenuItem only = new MenuItem("Salad", 900, 1);
        check(critic.chooseMenuItem(List.of(only)) == only, "single-item menu");
    }

    private static void testWaitingLossAndAbandonment() {
        Customer critic = new CriticCustomer(1);
        int[] expected = {88, 76, 64, 52, 40, 28, 16, 4, 0, 0};
        for (int value : expected) {
            critic.applyWaitingLoss();
            check(critic.getSatisfaction() == value, "waiting satisfaction should be " + value);
        }

        Restaurant restaurant = new Restaurant();
        Customer seated = new CriticCustomer(2);
        restaurant.addArrival(seated);
        restaurant.seat(2, 1);
        for (int turn = 0; turn < 9; turn++) {
            seated.applyWaitingLoss();
        }
        check(seated.getSatisfaction() == 0, "a seated Critic reaches 0 after 9 turns");
        restaurant.removeAbandoned(seated);
        check(seated.getStatus() == CustomerStatus.LEFT, "the Critic leaves");
        check(restaurant.findTable(1).isAvailable(), "the table is freed");
        check(restaurant.getRevenueCents() == 0, "no revenue from a Critic who left");
    }

    private static void testNoLossAfterService() {
        Restaurant restaurant = new Restaurant();
        Customer critic = new CriticCustomer(1);
        restaurant.addArrival(critic);
        restaurant.seat(1, 1);
        MenuItem item = critic.chooseMenuItem(restaurant.getMenu());
        restaurant.registerOrder(1, new Order(1, item, critic.quotePriceCents(item)));
        while (critic.getOrder().canPrepare()) {
            critic.getOrder().prepareOneUnit();
        }
        restaurant.serve(1, 4);
        int before = critic.getSatisfaction();
        critic.applyWaitingLoss();
        check(critic.getSatisfaction() == before, "no loss while eating");
    }

    private static void testFullLifecycleThroughRestaurant() {
        Restaurant restaurant = new Restaurant();
        Customer critic = new CriticCustomer(4);
        restaurant.addArrival(critic);
        check(restaurant.seat(4, 2).success(), "seat");
        critic.applyWaitingLoss();
        MenuItem item = critic.chooseMenuItem(restaurant.getMenu());
        check(item == restaurant.getMenu().get(3), "the Critic orders the combo from the real menu");
        check(item.getPreparationUnits() == 3 && item.getPriceCents() == 2430, "combo takes 3 units and costs 2430");
        Order order = new Order(4, item, critic.quotePriceCents(item));
        check(restaurant.registerOrder(4, order).success(), "order registered");
        for (int unit = 0; unit < 3; unit++) {
            order.prepareOneUnit();
            critic.applyWaitingLoss();
        }
        check(critic.getSatisfaction() == 52, "four waiting turns at 12 each");
        check(restaurant.serve(4, 5).success(), "served once READY");
        critic.finishEating(6);
        check(restaurant.recordPayment(critic, 7), "paid on the following turn");
        check(restaurant.getRevenueCents() == 2430, "full combo price added to revenue");
        check(restaurant.getPaidSatisfactionTotal() == 52, "satisfaction recorded at payment");
        check(restaurant.findTable(2).isAvailable(), "table freed after payment");
    }

    private static void testValidation() {
        Customer critic = new CriticCustomer(1);
        expectIllegalArgument("null menu", () -> critic.chooseMenuItem(null));
        expectIllegalArgument("empty menu", () -> critic.chooseMenuItem(List.of()));
        expectIllegalArgument("null price item", () -> critic.quotePriceCents(null));
        expectIllegalArgument("zero ID", () -> new CriticCustomer(0));

        List<MenuItem> withNull = new ArrayList<>();
        withNull.add(new MenuItem("Pasta", 1800, 2));
        withNull.add(null);
        expectIllegalArgument("menu containing null", () -> critic.chooseMenuItem(withNull));
        check(withNull.size() == 2 && withNull.get(1) == null, "rejected menu is left unchanged");
        check(critic.getSatisfaction() == 100 && critic.getStatus() == CustomerStatus.WAITING,
                "rejected calls leave the customer unchanged");
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
