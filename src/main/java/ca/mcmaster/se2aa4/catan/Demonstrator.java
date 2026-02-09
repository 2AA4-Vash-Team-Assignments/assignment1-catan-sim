package ca.mcmaster.se2aa4.catan;

public class Demonstrator {

    public static void main(String[] args) {
        CatanGame game = new CatanGame();
        if (args.length > 0) {
            game.getConfiguration().load(args[0]);
        }
        game.play();
    }
}
