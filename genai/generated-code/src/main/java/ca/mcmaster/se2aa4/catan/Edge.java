package ca.mcmaster.se2aa4.catan;

import java.util.List;

public class Edge {

    private final Node node1;
    private final Node node2;
    private Road road;

    public Edge(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
        this.road = null;
        node1.addAdjacentEdge(this);
        node2.addAdjacentEdge(this);
    }

    public boolean isOccupied() {
        return road != null;
    }

    public Road getRoad() {
        return road;
    }

    public void setRoad(Road road) {
        this.road = road;
    }

    public List<Node> getEndpoints() {
        return List.of(node1, node2);
    }
}
