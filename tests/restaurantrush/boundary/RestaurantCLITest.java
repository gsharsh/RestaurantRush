package restaurantrush.boundary;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import restaurantrush.control.ArrivalSchedule;
import restaurantrush.control.GameController;
import restaurantrush.control.TurnManager;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.OrderStatus;
import restaurantrush.domain.Restaurant;
import restaurantrush.staff.Cashier;
import restaurantrush.staff.Chef;
import restaurantrush.staff.Waiter;
import restaurantrush.view.CustomerView;
import restaurantrush.view.GameSnapshot;
import restaurantrush.view.TableView;

/** Offline regression checks for finished-snapshot outcome display. */
public final class RestaurantCLITest {
    private static int checks;

    private RestaurantCLITest() {
    }

    public static void main(String[] args) {
        int before = checks;
        testUnfinishedSnapshotsProduceNoOutput();
        testFinishedSnapshotsDisplayOutcomes();
        testNullSnapshotProducesNoOutput();
        testStateDisplayShowsCustomerTypeLabels();
        System.out.println("RestaurantCLITest: PASS (" + (checks - before) + " checks)");
    }

    private static void testUnfinishedSnapshotsProduceNoOutput() {
        int[][] turns = {{1, 0}, {18, 17}};
        for (int[] turn : turns) {
            GameSnapshot snapshot = emptySnapshot(turn[0], turn[1]);
            for (boolean victory : new boolean[] {false, true}) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (Scanner input = new Scanner("");
                        PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
                    RestaurantCLI cli = newCLI(input, output);
                    expectIllegalArgument("unfinished turn " + turn[0] + ", victory=" + victory,
                            () -> cli.displayOutcome(snapshot, victory));
                    output.flush();
                    check(bytes.size() == 0, "unfinished snapshots must produce no output");
                }
            }
        }
    }

    private static void testFinishedSnapshotsDisplayOutcomes() {
        for (boolean victory : new boolean[] {true, false}) {
            GameSnapshot snapshot = victory
                    ? new GameSnapshot(18, 18, 6300, 76.0, List.of(), List.of(),
                            List.of(new TableView(1, null), new TableView(2, null)),
                            false, false, 4, 5, 1)
                    : emptySnapshot(18, 18);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (Scanner input = new Scanner("");
                    PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
                RestaurantCLI cli = newCLI(input, output);
                cli.displayOutcome(snapshot, victory);
                output.flush();
                String rendered = bytes.toString(StandardCharsets.UTF_8);
                check(rendered.contains(victory ? "VICTORY" : "DEFEAT"), "expected outcome banner");
                check(!rendered.contains(victory ? "DEFEAT" : "VICTORY"), "opposite banner must be absent");
                check(rendered.lines().anyMatch(line -> line.matches("Turns completed:\\s+18 of 18")),
                        "finished outcome must report 18 completed turns");
            }
        }
    }

    private static void testNullSnapshotProducesNoOutput() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (Scanner input = new Scanner("");
                PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            RestaurantCLI cli = newCLI(input, output);
            expectIllegalArgument("null outcome snapshot", () -> cli.displayOutcome(null, false));
            output.flush();
            check(bytes.size() == 0, "null snapshot must produce no output");
        }
    }

    private static void testStateDisplayShowsCustomerTypeLabels() {
        List<CustomerView> customers = List.of(
                new CustomerView(1, "Regular", 84, CustomerStatus.SEATED, OrderStatus.READY, "Salad", 0, 1),
                new CustomerView(2, "VIP", 100, CustomerStatus.WAITING, null, null, null, null),
                new CustomerView(4, "Critic", 88, CustomerStatus.WAITING, null, null, null, null));
        GameSnapshot snapshot = new GameSnapshot(10, 9, 900, 84.0, List.of(2, 4), customers,
                List.of(new TableView(1, 1), new TableView(2, null)), true, false, 1, 1, 0);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (Scanner input = new Scanner("");
                PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            newCLI(input, output).displayState(snapshot);
            output.flush();
            String rendered = bytes.toString(StandardCharsets.UTF_8);
            check(rendered.contains("Waiting queue: C2 (VIP), C4 (Critic)"),
                    "queue shows each waiting customer's type label");
            check(rendered.contains("C1 (Regular)  satisfaction 84"), "customer list shows Regular label");
            check(rendered.contains("C2 (VIP)  satisfaction 100"), "customer list shows VIP label");
            check(rendered.contains("C4 (Critic)  satisfaction 88"), "customer list shows Critic label");
        }
    }

    private static RestaurantCLI newCLI(Scanner input, PrintStream output) {
        Restaurant restaurant = new Restaurant();
        ArrivalSchedule arrivals = new ArrivalSchedule();
        Cashier cashier = new Cashier();
        TurnManager turnManager = new TurnManager(cashier, arrivals);
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        GameController controller = new GameController(restaurant, turnManager, waiter, chef);
        return new RestaurantCLI(controller, input, output);
    }

    private static GameSnapshot emptySnapshot(int turn, int completedTurns) {
        return new GameSnapshot(turn, completedTurns, 0, null, List.of(), List.of(),
                List.of(new TableView(1, null), new TableView(2, null)),
                false, false, 0, 0, 0);
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
