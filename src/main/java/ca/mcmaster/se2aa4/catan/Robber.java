package ca.mcmaster.se2aa4.catan;

public class Robber {

    private Tile currentTile;

    public Tile getCurrentTile() {
        return currentTile;
    }

    public void placeOn(Tile tile) {
        this.currentTile = tile;
    }
}
