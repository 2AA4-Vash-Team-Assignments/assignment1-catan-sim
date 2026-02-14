package ca.mcmaster.se2aa4.catan;

/**
 * Demonstrator for the Catan board game simulator.
 * Runs one simulation showing dice rolls, resource distribution,
 * building actions, and victory point tracking.
 */
public class Demonstrator {

    public static void main(String[] args) {
        CatanGame game = new CatanGame();

        if (args.length > 0) {
            game.getConfiguration().load(args[0]);
        }

        game.play();
    }
}
