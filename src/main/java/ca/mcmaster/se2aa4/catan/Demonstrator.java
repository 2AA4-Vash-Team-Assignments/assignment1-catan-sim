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
        // Create the game with board, 4 players, dice, bank, and configuration
        CatanGame game = new CatanGame();

        // R1.4: Load user-defined max rounds (1-8192) from config file if provided
        if (args.length > 0) {
            game.getConfiguration().load(args[0]);
        }
        // Otherwise uses default 50 rounds from Configuration constructor

        // Run the simulation: setup phase, then rounds until win or max rounds (R1.5)
        // Actions are printed per R1.7; VPs printed at end of each round
        game.play();
    }
}
