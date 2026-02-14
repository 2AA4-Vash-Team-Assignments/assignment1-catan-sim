package ca.mcmaster.se2aa4.catan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {

    private final List<Tile> tiles;
    private final List<Node> nodes;
    private final List<Edge> edges;

    public Board() {
        this.tiles = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public void initialize() {
        createNodes();
        createTiles();
        createEdges();
    }

    private void createNodes() {
        for (int i = 0; i < 54; i++) {
            nodes.add(new Node(i));
        }
    }

    private void createTiles() {
        ResourceType W = ResourceType.WOOD;
        ResourceType B = ResourceType.BRICK;
        ResourceType H = ResourceType.WHEAT;
        ResourceType O = ResourceType.ORE;
        ResourceType S = ResourceType.SHEEP;

        int[][] tileData = {
            {0, 0, 10}, {1, 2, 11}, {2, 1, 8},  {3, 3, 3},
            {4, 4, 11}, {5, 4, 5},  {6, 4, 12}, {7, 2, 3},
            {8, 3, 6},  {9, 0, 4},  {10, 3, 6}, {11, 2, 9},
            {12, 0, 5}, {13, 1, 9}, {14, 1, 8}, {15, 2, 4},
            {16, -1, 0}, {17, 0, 2}, {18, 4, 10}
        };

        ResourceType[] typeMap = {W, B, H, O, S};

        for (int[] data : tileData) {
            int id = data[0];
            int resIndex = data[1];
            int number = data[2];

            if (resIndex == -1) {
                tiles.add(new Tile(id, 0, null));
            } else {
                tiles.add(new Tile(id, number, typeMap[resIndex]));
            }
        }

        // Wire up tile-node adjacency
        // Center tile
        setTileNodes(0, new int[]{5, 0, 1, 2, 3, 4});

        // Inner ring (tiles 1-6)
        setTileNodes(1, new int[]{1, 6, 7, 8, 9, 2});
        setTileNodes(2, new int[]{3, 2, 9, 10, 11, 12});
        setTileNodes(3, new int[]{15, 4, 3, 12, 13, 14});
        setTileNodes(4, new int[]{18, 16, 5, 4, 15, 17});
        setTileNodes(5, new int[]{21, 19, 20, 0, 5, 16});
        setTileNodes(6, new int[]{20, 22, 23, 6, 1, 0});

        // Outer ring (tiles 7-18)
        setTileNodes(7, new int[]{7, 24, 25, 26, 27, 8});
        setTileNodes(8, new int[]{9, 8, 27, 28, 29, 10});
        setTileNodes(9, new int[]{11, 10, 29, 30, 31, 12});
        setTileNodes(10, new int[]{13, 12, 11, 32, 33, 34});
        setTileNodes(11, new int[]{15, 14, 13, 34, 35, 36});
        setTileNodes(12, new int[]{17, 15, 14, 37, 38, 39});
        setTileNodes(13, new int[]{18, 40, 17, 39, 41, 42});
        setTileNodes(14, new int[]{21, 43, 16, 18, 40, 44});
        setTileNodes(15, new int[]{19, 21, 43, 45, 46, 47});
        setTileNodes(16, new int[]{20, 19, 46, 48, 49, 22});
        setTileNodes(17, new int[]{22, 23, 49, 50, 51, 52});
        setTileNodes(18, new int[]{6, 7, 23, 24, 52, 53});

        for (Tile tile : tiles) {
            for (Node node : tile.getAdjacentNodes()) {
                node.addAdjacentTile(tile);
            }
        }
    }

    private void setTileNodes(int tileId, int[] nodeIds) {
        Tile tile = tiles.get(tileId);
        for (int nodeId : nodeIds) {
            Node node = nodes.get(nodeId);
            tile.addAdjacentNode(node);
        }
    }

    private void createEdges() {
        Set<String> created = new HashSet<>();

        for (Tile tile : tiles) {
            List<Node> tileNodes = tile.getAdjacentNodes();
            for (int i = 0; i < tileNodes.size(); i++) {
                Node a = tileNodes.get(i);
                Node b = tileNodes.get((i + 1) % tileNodes.size());

                String key = Math.min(a.getId(), b.getId()) + "-" + Math.max(a.getId(), b.getId());
                if (!created.contains(key)) {
                    Edge edge = new Edge(a, b);
                    edges.add(edge);
                    a.addAdjacentNode(b);
                    b.addAdjacentNode(a);
                    created.add(key);
                }
            }
        }
    }

    public List<Tile> getTilesForNumber(int number) {
        List<Tile> result = new ArrayList<>();
        for (Tile tile : tiles) {
            if (tile.getNumberToken() == number && tile.getResourceType() != null) {
                result.add(tile);
            }
        }
        return result;
    }

    public List<Node> getAvailableSettlementNodes(Player player) {
        List<Node> available = new ArrayList<>();
        for (Node node : nodes) {
            if (!node.isOccupied() && node.satisfiesDistanceRule() && hasConnectedRoad(node, player)) {
                available.add(node);
            }
        }
        return available;
    }

    public List<Edge> getAvailableRoadEdges(Player player) {
        List<Edge> available = new ArrayList<>();
        for (Edge edge : edges) {
            if (!edge.isOccupied() && isConnectedToPlayer(edge, player)) {
                available.add(edge);
            }
        }
        return available;
    }

    public List<Node> getUpgradeableNodes(Player player) {
        List<Node> upgradeable = new ArrayList<>();
        for (Node node : nodes) {
            if (node.isOccupied()
                    && node.getBuilding().getOwner() == player
                    && node.getBuilding().getType() == BuildingType.SETTLEMENT) {
                upgradeable.add(node);
            }
        }
        return upgradeable;
    }

    public List<Node> getAvailableSetupNodes() {
        List<Node> available = new ArrayList<>();
        for (Node node : nodes) {
            if (!node.isOccupied() && node.satisfiesDistanceRule()) {
                available.add(node);
            }
        }
        return available;
    }

    public int calculateLongestRoad(Player player) {
        int longest = 0;
        for (Edge edge : edges) {
            if (edge.isOccupied() && edge.getRoad().getOwner() == player) {
                int length = dfsRoadLength(edge, player, new HashSet<>());
                longest = Math.max(longest, length);
            }
        }
        return longest;
    }

    private int dfsRoadLength(Edge current, Player player, Set<Edge> visited) {
        visited.add(current);
        int maxLength = 1;

        for (Node endpoint : current.getEndpoints()) {
            if (endpoint.isOccupied() && endpoint.getBuilding().getOwner() != player) {
                continue;
            }
            for (Edge adjacent : endpoint.getAdjacentEdges()) {
                if (!visited.contains(adjacent) && adjacent.isOccupied()
                        && adjacent.getRoad().getOwner() == player) {
                    int length = 1 + dfsRoadLength(adjacent, player, visited);
                    maxLength = Math.max(maxLength, length);
                }
            }
        }

        visited.remove(current);
        return maxLength;
    }

    private boolean hasConnectedRoad(Node node, Player player) {
        for (Edge edge : node.getAdjacentEdges()) {
            if (edge.isOccupied() && edge.getRoad().getOwner() == player) {
                return true;
            }
        }
        return false;
    }

    private boolean isConnectedToPlayer(Edge edge, Player player) {
        for (Node endpoint : edge.getEndpoints()) {
            if (endpoint.isOccupied() && endpoint.getBuilding().getOwner() == player) {
                return true;
            }
            for (Edge adj : endpoint.getAdjacentEdges()) {
                if (adj.isOccupied() && adj.getRoad().getOwner() == player) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}
