package restaurantrush.domain;

/** ActionResult validation and factory tests. */
public final class ActionResultTest {
    private static int checks;

    private ActionResultTest() {
    }

    public static void main(String[] args) {
        ActionResult ok = ActionResult.ok("Done.");
        check(ok.success() && ok.message().equals("Done."), "ok result");
        ActionResult fail = ActionResult.fail("Nope.");
        check(!fail.success() && fail.message().equals("Nope."), "fail result");
        check(new ActionResult(true, "x").equals(ActionResult.ok("x")), "record equality");
        expectRejected(() -> ActionResult.ok(null));
        expectRejected(() -> ActionResult.fail(""));
        expectRejected(() -> ActionResult.fail("   "));
        expectRejected(() -> new ActionResult(false, null));
        System.out.println("ActionResultTest: PASS (" + checks + " checks)");
    }

    private static void expectRejected(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError("expected IllegalArgumentException");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }
}
