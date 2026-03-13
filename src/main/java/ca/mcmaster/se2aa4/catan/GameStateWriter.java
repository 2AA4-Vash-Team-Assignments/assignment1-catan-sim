package ca.mcmaster.se2aa4.catan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Serializes the current game state to a JSON file for the visualizer (R2.2, R2.3).
 * Translates internal node IDs to Catanatron's numbering scheme so the
 * visualizer renders settlements and roads in the correct positions.
 */
public class GameStateWriter {

    private static final String[] PLAYER_COLOURS = {"RED", "BLUE", "ORANGE", "WHITE"};

    /**
     * Mapping from our internal node IDs (0-53) to the Catanatron visualizer's
     * node IDs. The visualizer auto-increments node IDs as it processes tiles
     * in base_map.json order, which produces a different numbering than our
     * hard-coded Board.setTileNodes() layout.
     */
    private static final int[] TO_VISUALIZER = {
        1, 2, 3, 4, 5, 0, 6, 7, 8, 9,
        10, 11, 12, 14, 15, 13, 17, 18, 16, 20,
        21, 19, 22, 23, 24, 25, 26, 27, 28, 29,
        30, 31, 32, 33, 34, 36, 37, 35, 39, 38,
        41, 42, 40, 44, 43, 45, 47, 46, 48, 49,
        50, 51, 52, 53
    };

    private final Board board;
    private final List<Player> players;
    private final Robber robber;
    private final int currentRound;
    private final int currentPlayerId;

    public GameStateWriter(Board board, List<Player> players, Robber robber, int currentRound, int currentPlayerId) {
        this.board = board;
        this.players = players;
        this.robber = robber;
        this.currentRound = currentRound;
        this.currentPlayerId = currentPlayerId;
    }

    public void write(String filePath) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"roads\": [");
        appendRoads(json);
        json.append("],\n");
        json.append("  \"buildings\": [");
        appendBuildings(json);
        json.append("]\n");
        json.append("}\n");
        Files.writeString(Path.of(filePath), json.toString());
    }

    private void appendRoads(StringBuilder json) {
        boolean first = true;
        for (Edge edge : board.getEdges()) {
            if (!edge.isOccupied()) continue;
            Road road = edge.getRoad();
            List<Node> endpoints = edge.getEndpoints();
            int a = TO_VISUALIZER[endpoints.get(0).getId()];
            int b = TO_VISUALIZER[endpoints.get(1).getId()];
            String owner = PLAYER_COLOURS[Math.min(road.getOwner().getId() - 1, PLAYER_COLOURS.length - 1)];
            if (!first) json.append(",");
            json.append("\n    {\"a\": ").append(a).append(", \"b\": ").append(b).append(", \"owner\": \"").append(owner).append("\"}");
            first = false;
        }
    }

    private void appendBuildings(StringBuilder json) {
        boolean first = true;
        for (Node node : board.getNodes()) {
            if (!node.isOccupied()) continue;
            Building building = node.getBuilding();
            int vizNodeId = TO_VISUALIZER[node.getId()];
            String owner = PLAYER_COLOURS[Math.min(building.getOwner().getId() - 1, PLAYER_COLOURS.length - 1)];
            String type = building.getType().name();
            if (!first) json.append(",");
            json.append("\n    {\"node\": ").append(vizNodeId).append(", \"owner\": \"").append(owner).append("\", \"type\": \"").append(type).append("\"}");
            first = false;
        }
    }
}
