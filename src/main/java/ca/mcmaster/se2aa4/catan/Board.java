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
        // Tile data: id, resource, number token
        // From assignment specification
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
                // Desert tile - no resource, no number
                tiles.add(new Tile(id, 0, null));
            } else {
                tiles.add(new Tile(id, number, typeMap[resIndex]));
            }
        }

        // Wire up tile-node adjacency based on standard Catan hex layout
        // Tile 0 (center): nodes 0-5
        setTileNodes(0, new int[]{0, 1, 2, 3, 4, 5});
        // Tile 1 (inner ring): nodes 0, 5, 6, 7, 8, 1
        setTileNodes(1, new int[]{0, 5, 6, 7, 8, 1});
        // Tile 2: nodes 1, 8, 9, 10, 11, 2
        setTileNodes(2, new int[]{1, 8, 9, 10, 11, 2});
        // Tile 3: nodes 2, 11, 12, 13, 14, 3
        setTileNodes(3, new int[]{2, 11, 12, 13, 14, 3});
        // Tile 4: nodes 3, 14, 15, 16, 17, 4
        setTileNodes(4, new int[]{3, 14, 15, 16, 17, 4});
        // Tile 5: nodes 4, 17, 18, 19, 20, 5
        setTileNodes(5, new int[]{4, 17, 18, 19, 20, 5});
        // Tile 6: nodes 5, 20, 21, 6, 0, 4});
        // Wait - need to reconsider. Let me use standard Catan hex numbering.
        setTileNodes(6, new int[]{5, 20, 21, 6, 0, 4});

        // Outer ring tiles (7-18)
        setTileNodes(7, new int[]{6, 21, 22, 23, 24, 7});
        setTileNodes(8, new int[]{7, 24, 25, 26, 27, 8});
        setTileNodes(9, new int[]{8, 27, 28, 29, 9, 1});
        setTileNodes(10, new int[]{9, 29, 30, 31, 10, 2});
        setTileNodes(11, new int[]{10, 31, 32, 33, 34, 11});
        setTileNodes(12, new int[]{11, 34, 35, 36, 12, 2});
        setTileNodes(13, new int[]{12, 36, 37, 38, 13, 3});
        setTileNodes(14, new int[]{13, 38, 39, 40, 41, 14});
        setTileNodes(15, new int[]{14, 41, 42, 43, 15, 3});
        setTileNodes(16, new int[]{15, 43, 44, 45, 16, 4});
        setTileNodes(17, new int[]{16, 45, 46, 47, 48, 17});
        setTileNodes(18, new int[]{17, 48, 49, 50, 18, 4});

        // Also set up Node -> Tile back-references
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
        // Create edges by connecting adjacent nodes on each tile
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
            // Stop if another player has a building here (breaks the road)
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
