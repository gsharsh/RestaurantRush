package restaurantrush.boundary;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

import restaurantrush.control.GameController;
import restaurantrush.domain.ActionResult;
import restaurantrush.view.CustomerView;
import restaurantrush.view.GameSnapshot;
import restaurantrush.view.TableView;

/**
 * Command-line boundary for the game.
 *
 * <p>The CLI only reads the manager's commands and prints game state. It
 * checks that a command is well formed (known word, right number of
 * arguments, integer IDs) but never decides whether a move is legal: that is
 * the controller's job, and the CLI prints the controller's
 * {@link ActionResult#message()} as given.
 *
 * <p>Input and output are injected so the whole CLI can be driven from a
 * scripted input file or a test without touching {@code System.in}/{@code System.out}.
 */
public final class RestaurantCLI {

    private static final String WAITER_HELP = String.join(System.lineSeparator(),
            "Waiter commands:",
            "  seat <customerId> <tableId>  seat a queued customer at a free table",
            "  order <customerId>           take a seated customer's order",
            "  serve <customerId>           serve a READY order",
            "  wait                         skip the Waiter's task this turn",
            "  help                         show these commands");

    private static final String CHEF_HELP = String.join(System.lineSeparator(),
            "Chef commands:",
            "  cook <customerId>  prepare one unit of that customer's order",
            "  wait               skip the Chef's task this turn",
            "  help               show these commands");

    private final GameController controller;
    private final Scanner input;
    private final PrintStream output;

    public RestaurantCLI(GameController controller, Scanner input, PrintStream output) {
        this.controller = required(controller, "controller");
        this.input = required(input, "input");
        this.output = required(output, "output");
    }

    /**
     * Plays the game until turn 18 is finished, then shows the result. If the
     * input runs out mid-game, stops without finishing the turn or inventing
     * an outcome.
     */
    public void run() {
        output.println("Welcome to Restaurant Rush! Type 'help' at any prompt to see the commands.");
        while (!controller.isFinished()) {
            controller.startTurn();
            displayState(controller.getSnapshot());
            if (!readWaiterChoice() || !readChefChoice()) {
                output.println();
                output.println("Input ended before the game finished. Stopping without a result.");
                return;
            }
            controller.finishTurn();
            displayEndOfTurn(controller.getSnapshot());
        }
        displayOutcome(controller.getSnapshot(), controller.isVictory());
    }

    /** Prints the start-of-turn display required by the brief (Section 3.1). */
    public void displayState(GameSnapshot snapshot) {
        required(snapshot, "snapshot");
        output.println();
        output.println("========== Turn " + snapshot.turn() + " of 18 ==========");
        output.println("Revenue: " + formatMoney(snapshot.revenueCents())
                + "   Avg paid satisfaction: " + formatAverage(snapshot.averagePaidSatisfaction()));

        output.println("Waiting queue: " + (snapshot.waitingCustomerIds().isEmpty()
                ? "(empty)"
                : snapshot.waitingCustomerIds().stream()
                        .map(id -> "C" + id)
                        .collect(Collectors.joining(", "))));

        output.println("Tables:");
        for (TableView table : snapshot.tables()) {
            output.println("  Table " + table.id() + ": "
                    + (table.occupantId() == null ? "free" : "C" + table.occupantId()));
        }

        output.println("Customers:");
        if (snapshot.customers().isEmpty()) {
            output.println("  (none)");
        }
        for (CustomerView customer : snapshot.customers()) {
            output.println("  " + describe(customer));
        }

        String chef = snapshot.chefAvailable() ? "available"
                : snapshot.waiterAvailable() ? "available after the Waiter" : "used";
        output.println("Staff: Waiter " + (snapshot.waiterAvailable() ? "available" : "used")
                + ", Chef " + chef + ", Cashier automatic (one payment per turn)");
    }

    /**
     * Prints the final result after turn 18.
     *
     * @throws IllegalArgumentException if the snapshot is null or unfinished
     */
    public void displayOutcome(GameSnapshot snapshot, boolean victory) {
        required(snapshot, "snapshot");
        if (snapshot.completedTurns() != 18) {
            throw new IllegalArgumentException("An outcome requires 18 completed turns.");
        }
        output.println();
        output.println("================ " + (victory ? "VICTORY" : "DEFEAT") + " ================");
        output.println("Turns completed:        " + snapshot.completedTurns() + " of 18");
        output.println("Customers served:       " + snapshot.servedCount());
        output.println("Customers paid:         " + snapshot.paidCount() + " (target at least 4)");
        output.println("Unhappy departures:     " + snapshot.abandonedCount());
        output.println("Revenue:                " + formatMoney(snapshot.revenueCents())
                + " (target at least $45.00)");
        output.println("Avg paid satisfaction:  " + formatAverage(snapshot.averagePaidSatisfaction())
                + " (target at least 60)");
    }

    // ------------------------------------------------------------------
    // Reading manager choices
    // ------------------------------------------------------------------

    /** Repeats until one Waiter command succeeds. Returns false if input ends first. */
    private boolean readWaiterChoice() {
        while (true) {
            String[] words = prompt("Waiter> ");
            if (words == null) {
                return false;
            }
            if (words.length == 0) {
                continue;
            }
            ActionResult result;
            switch (words[0]) {
                case "help":
                    output.println(WAITER_HELP);
                    continue;
                case "seat":
                    Integer[] seatIds = parseIds(words, 2, "seat <customerId> <tableId>");
                    if (seatIds == null) {
                        continue;
                    }
                    result = controller.seatCustomer(seatIds[0], seatIds[1]);
                    break;
                case "order":
                    Integer[] orderIds = parseIds(words, 1, "order <customerId>");
                    if (orderIds == null) {
                        continue;
                    }
                    result = controller.takeOrder(orderIds[0]);
                    break;
                case "serve":
                    Integer[] serveIds = parseIds(words, 1, "serve <customerId>");
                    if (serveIds == null) {
                        continue;
                    }
                    result = controller.serveOrder(serveIds[0]);
                    break;
                case "wait":
                    if (words.length != 1) {
                        printUsage("wait");
                        continue;
                    }
                    result = controller.waitWaiter();
                    break;
                default:
                    output.println("Unknown Waiter command '" + words[0] + "'. Type 'help' to see the commands.");
                    continue;
            }
            if (report(result)) {
                return true;
            }
        }
    }

    /** Repeats until one Chef command succeeds. Returns false if input ends first. */
    private boolean readChefChoice() {
        while (true) {
            String[] words = prompt("Chef> ");
            if (words == null) {
                return false;
            }
            if (words.length == 0) {
                continue;
            }
            ActionResult result;
            switch (words[0]) {
                case "help":
                    output.println(CHEF_HELP);
                    continue;
                case "cook":
                    Integer[] cookIds = parseIds(words, 1, "cook <customerId>");
                    if (cookIds == null) {
                        continue;
                    }
                    result = controller.prepareOrder(cookIds[0]);
                    break;
                case "wait":
                    if (words.length != 1) {
                        printUsage("wait");
                        continue;
                    }
                    result = controller.waitChef();
                    break;
                default:
                    output.println("Unknown Chef command '" + words[0] + "'. Type 'help' to see the commands.");
                    continue;
            }
            if (report(result)) {
                return true;
            }
        }
    }

    /**
     * Prints a prompt and reads one line, split into lower-case words.
     * Returns {@code null} at end of input.
     */
    private String[] prompt(String label) {
        output.print(label);
        output.flush();
        if (!input.hasNextLine()) {
            return null;
        }
        String line = input.nextLine().trim().toLowerCase(Locale.ROOT);
        return line.isEmpty() ? new String[0] : line.split("\\s+");
    }

    /**
     * Reads {@code count} integer IDs after the command word. Prints the usage
     * and returns {@code null} if the count is wrong or an ID is not an integer.
     */
    private Integer[] parseIds(String[] words, int count, String usage) {
        if (words.length != count + 1) {
            printUsage(usage);
            return null;
        }
        Integer[] ids = new Integer[count];
        for (int i = 0; i < count; i++) {
            try {
                ids[i] = Integer.parseInt(words[i + 1]);
            } catch (NumberFormatException e) {
                output.println("'" + words[i + 1] + "' is not a whole-number ID.");
                printUsage(usage);
                return null;
            }
        }
        return ids;
    }

    private void printUsage(String usage) {
        output.println("Usage: " + usage);
    }

    /** Prints the controller's message and returns whether the action succeeded. */
    private boolean report(ActionResult result) {
        output.println(result.success() ? result.message() : "Not allowed: " + result.message());
        return result.success();
    }

    // ------------------------------------------------------------------
    // Formatting
    // ------------------------------------------------------------------

    /** Prints the end-of-turn results required by the brief (Section 3.5). */
    private void displayEndOfTurn(GameSnapshot snapshot) {
        output.println("End of turn " + snapshot.completedTurns() + ": served " + snapshot.servedCount()
                + ", unhappy departures " + snapshot.abandonedCount()
                + ", revenue " + formatMoney(snapshot.revenueCents())
                + ", avg paid satisfaction " + formatAverage(snapshot.averagePaidSatisfaction())
                + ", turns used " + snapshot.completedTurns() + "/18");
    }

    private static String describe(CustomerView customer) {
        StringBuilder line = new StringBuilder()
                .append("C").append(customer.id())
                .append("  satisfaction ").append(customer.satisfaction())
                .append("  ").append(customer.status());
        if (customer.tableId() != null) {
            line.append("  table ").append(customer.tableId());
        }
        if (customer.orderStatus() == null) {
            line.append("  no order yet");
        } else {
            line.append("  order: ").append(customer.itemName())
                    .append(" ").append(customer.orderStatus());
            if (customer.remainingUnits() > 0) {
                line.append(" (").append(customer.remainingUnits())
                        .append(customer.remainingUnits() == 1 ? " unit" : " units")
                        .append(" left)");
            }
        }
        return line.toString();
    }

    private static String formatMoney(int cents) {
        return String.format(Locale.ROOT, "$%d.%02d", cents / 100, cents % 100);
    }

    private static String formatAverage(Double average) {
        return average == null ? "N/A" : String.format(Locale.ROOT, "%.1f", average);
    }

    private static <T> T required(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return value;
    }
}
