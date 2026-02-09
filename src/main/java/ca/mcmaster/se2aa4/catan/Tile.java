package ca.mcmaster.se2aa4.catan;

import java.util.ArrayList;
import java.util.List;

public class Tile {

    private final int id;
    private final int numberToken;
    private final ResourceType resourceType;
    private final List<Node> adjacentNodes;

    public Tile(int id, int numberToken, ResourceType resourceType) {
        this.id = id;
        this.numberToken = numberToken;
        this.resourceType = resourceType;
        this.adjacentNodes = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getNumberToken() {
        return numberToken;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public List<Node> getAdjacentNodes() {
        return adjacentNodes;
    }

    public void addAdjacentNode(Node node) {
        if (!adjacentNodes.contains(node)) {
            adjacentNodes.add(node);
        }
    }
}
