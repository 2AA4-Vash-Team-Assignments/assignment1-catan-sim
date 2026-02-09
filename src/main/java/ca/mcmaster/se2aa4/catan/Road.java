package ca.mcmaster.se2aa4.catan;

public class Road {

    private final Player owner;
    private final Edge edge;

    public Road(Player owner, Edge edge) {
        this.owner = owner;
        this.edge = edge;
    }

    public Player getOwner() {
        return owner;
    }

    public Edge getEdge() {
        return edge;
    }
}
