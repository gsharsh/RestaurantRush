package restaurantrush.app;

import java.util.Scanner;

import restaurantrush.boundary.RestaurantCLI;
import restaurantrush.control.ArrivalSchedule;
import restaurantrush.control.GameController;
import restaurantrush.control.TurnManager;
import restaurantrush.domain.HappyHour;
import restaurantrush.domain.Restaurant;
import restaurantrush.staff.Cashier;
import restaurantrush.staff.Chef;
import restaurantrush.staff.Host;
import restaurantrush.staff.Waiter;

/**
 * Entry point. Builds exactly one instance of each game object, wires them
 * together and hands control to the CLI.
 */
public final class RestaurantRushApp {

    private RestaurantRushApp() {
    }

    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant();
        ArrivalSchedule arrivals = new ArrivalSchedule();
        Cashier cashier = new Cashier();
        HappyHour happyHour = new HappyHour();
        TurnManager turnManager = new TurnManager(cashier, arrivals, happyHour);
        Waiter waiter = new Waiter(happyHour);
        Chef chef = new Chef();
        Host host = new Host();
        GameController controller = new GameController(restaurant, turnManager, host, waiter, chef);

        RestaurantCLI cli = new RestaurantCLI(controller, new Scanner(System.in), System.out);
        cli.run();
    }
}
