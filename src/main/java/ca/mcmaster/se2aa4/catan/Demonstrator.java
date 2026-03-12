package ca.mcmaster.se2aa4.catan;

/**
 * Demonstrator program for the Catan simulator.
 * <p>
 * Demonstrates the key functionality required by R1.9: runs one or more
 * simulations showing dice rolls, resource distribution, building actions,
 * victory point tracking, and game termination.
 */
public class Demonstrator {

    public static void main(String[] args) {
        CatanGame game = new CatanGame();
        if (args.length > 0) {
            game.getConfiguration().load(args[0]);
        }
        if (game.getConfiguration().isHumanGame()) {
            game.setStateFilePath("state.json");
        }
        game.play();
    }
}
