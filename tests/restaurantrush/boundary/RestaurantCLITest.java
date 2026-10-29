package restaurantrush.boundary;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import restaurantrush.control.ArrivalSchedule;
import restaurantrush.control.GameController;
import restaurantrush.control.TurnManager;
import restaurantrush.domain.Restaurant;
import restaurantrush.staff.Cashier;
import restaurantrush.staff.Chef;
import restaurantrush.staff.Waiter;
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
