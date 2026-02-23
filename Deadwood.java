import java.util.Scanner;

public class Deadwood {

    // RUN IN CONSOLE
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Deadwood <numPlayers>");
            return;
        }

        int numPlayers = Integer.parseInt(args[0]);

        Game game = new Game(numPlayers);
        GameController controller = new GameController(game);

        System.out.println("Deadwood started with " + numPlayers + " players.");
        System.out.println("Type 'help' for commands.");

        Scanner sc = new Scanner(System.in);
        while (!game.isGameOver()) {
            System.out.print("> ");
            String line = sc.nextLine();
            boolean keepGoing = controller.handleCommand(line);
            if (!keepGoing) break;
        }

        System.out.println("Exiting.");
    }
}
