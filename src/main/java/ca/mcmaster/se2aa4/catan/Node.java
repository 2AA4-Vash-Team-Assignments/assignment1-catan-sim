package ca.mcmaster.se2aa4.catan;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private final int id;
    private Building building;
    private final List<Node> adjacentNodes;
    private final List<Tile> adjacentTiles;
    private final List<Edge> adjacentEdges;

    public Node(int id) {
        this.id = id;
        this.building = null;
        this.adjacentNodes = new ArrayList<>();
        this.adjacentTiles = new ArrayList<>();
        this.adjacentEdges = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public boolean isOccupied() {
        return building != null;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public boolean satisfiesDistanceRule() {
        for (Node neighbor : adjacentNodes) {
            if (neighbor.isOccupied()) {
                return false;
            }
        }
        return true;
    }

    public List<Node> getAdjacentNodes() {
        return adjacentNodes;
    }

    public List<Tile> getAdjacentTiles() {
        return adjacentTiles;
    }

    public List<Edge> getAdjacentEdges() {
        return adjacentEdges;
    }

    public void addAdjacentNode(Node node) {
        if (!adjacentNodes.contains(node)) {
            adjacentNodes.add(node);
        }
    }

    public void addAdjacentTile(Tile tile) {
        if (!adjacentTiles.contains(tile)) {
            adjacentTiles.add(tile);
        }
    }

    public void addAdjacentEdge(Edge edge) {
        if (!adjacentEdges.contains(edge)) {
            adjacentEdges.add(edge);
        }
    }
}
