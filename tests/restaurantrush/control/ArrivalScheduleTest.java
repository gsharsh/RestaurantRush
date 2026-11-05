package restaurantrush.control;

import restaurantrush.domain.Customer;
import restaurantrush.domain.RegularCustomer;
import restaurantrush.domain.VIPCustomer;
import restaurantrush.domain.CriticCustomer;

/**
 * Verifies the Stage 2 arrival schedule for all 18 turns.
 * Tests that the correct customer type (Regular, VIP, Critic) is created at each arrival turn.
 */
public final class ArrivalScheduleTest {
    private static int checks;

    private ArrivalScheduleTest() {
    }

    public static void main(String[] args) {
        testInvalidTurns();
        testNoArrivalTurns();
        testArrivalTurns();
        testFreshInstancesPerCall();
        System.out.println("ArrivalScheduleTest: PASS (" + checks + " checks)");
    }

    private static void testInvalidTurns() {
        ArrivalSchedule schedule = new ArrivalSchedule();
        expectIllegalArgument(() -> schedule.arrivalFor(0), "turn 0 must be rejected");
        expectIllegalArgument(() -> schedule.arrivalFor(-1), "negative turn must be rejected");
        expectIllegalArgument(() -> schedule.arrivalFor(19), "turn 19 must be rejected");
        expectIllegalArgument(() -> schedule.arrivalFor(100), "turn 100 must be rejected");
    }

    private static void testNoArrivalTurns() {
        ArrivalSchedule schedule = new ArrivalSchedule();
        // Turns with no scheduled arrivals
        for (int turn : new int[]{2, 3, 5, 6, 8, 9, 11, 12, 14, 15, 17, 18}) {
            Customer arrival = schedule.arrivalFor(turn);
            check(arrival == null, "turn " + turn + " must have no arrival");
        }
    }

    private static void testArrivalTurns() {
        ArrivalSchedule schedule = new ArrivalSchedule();

        // Turn 1: Regular ID 1
        Customer turn1 = schedule.arrivalFor(1);
        check(turn1 instanceof RegularCustomer, "turn 1 must be RegularCustomer");
        check(turn1.getId() == 1, "turn 1 customer must have ID 1");
        check("Regular".equals(turn1.typeLabel()), "turn 1 must have Regular label");

        // Turn 4: VIP ID 2
        Customer turn4 = schedule.arrivalFor(4);
        check(turn4 instanceof VIPCustomer, "turn 4 must be VIPCustomer");
        check(turn4.getId() == 2, "turn 4 customer must have ID 2");
        check("VIP".equals(turn4.typeLabel()), "turn 4 must have VIP label");
        check(turn4.waitingLossPerTurn() == 5, "VIP must have 5 waiting loss per turn");

        // Turn 7: Regular ID 3
        Customer turn7 = schedule.arrivalFor(7);
        check(turn7 instanceof RegularCustomer, "turn 7 must be RegularCustomer");
        check(turn7.getId() == 3, "turn 7 customer must have ID 3");
        check("Regular".equals(turn7.typeLabel()), "turn 7 must have Regular label");

        // Turn 10: Critic ID 4
        Customer turn10 = schedule.arrivalFor(10);
        check(turn10 instanceof CriticCustomer, "turn 10 must be CriticCustomer");
        check(turn10.getId() == 4, "turn 10 customer must have ID 4");
        check("Critic".equals(turn10.typeLabel()), "turn 10 must have Critic label");
        check(turn10.waitingLossPerTurn() == 12, "Critic must have 12 waiting loss per turn");

        // Turn 13: Regular ID 5
        Customer turn13 = schedule.arrivalFor(13);
        check(turn13 instanceof RegularCustomer, "turn 13 must be RegularCustomer");
        check(turn13.getId() == 5, "turn 13 customer must have ID 5");
        check("Regular".equals(turn13.typeLabel()), "turn 13 must have Regular label");

        // Turn 16: Regular ID 6
        Customer turn16 = schedule.arrivalFor(16);
        check(turn16 instanceof RegularCustomer, "turn 16 must be RegularCustomer");
        check(turn16.getId() == 6, "turn 16 customer must have ID 6");
        check("Regular".equals(turn16.typeLabel()), "turn 16 must have Regular label");
    }

    private static void testFreshInstancesPerCall() {
        ArrivalSchedule schedule = new ArrivalSchedule();

        // Calling arrivalFor with the same turn multiple times must return distinct objects
        Customer first1 = schedule.arrivalFor(1);
        Customer second1 = schedule.arrivalFor(1);
        check(first1 != second1, "arrivalFor(1) must return fresh instances");
        check(first1.getId() == second1.getId(), "fresh instances must have the same ID");

        Customer first4 = schedule.arrivalFor(4);
        Customer second4 = schedule.arrivalFor(4);
        check(first4 != second4, "arrivalFor(4) must return fresh instances");
        check(first4.getId() == second4.getId(), "fresh instances must have the same ID");

        Customer first10 = schedule.arrivalFor(10);
        Customer second10 = schedule.arrivalFor(10);
        check(first10 != second10, "arrivalFor(10) must return fresh instances");
        check(first10.getId() == second10.getId(), "fresh instances must have the same ID");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Check failed: " + message);
        }
        checks++;
    }

    private static void expectIllegalArgument(Runnable task, String message) {
        try {
            task.run();
            throw new AssertionError("Expected IllegalArgumentException: " + message);
        } catch (IllegalArgumentException e) {
            checks++;
        }
    }
}
