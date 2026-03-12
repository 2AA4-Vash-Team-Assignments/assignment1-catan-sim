package ca.mcmaster.se2aa4.catan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GameStateWriter {

    private static final String[] PLAYER_COLOURS = {"RED", "BLUE", "ORANGE", "WHITE"};

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
        json.append("],\n");
        json.append("  \"robberTileId\": ").append(robber.getCurrentTile() != null ? robber.getCurrentTile().getId() : -1).append(",\n");
        json.append("  \"currentRound\": ").append(currentRound).append(",\n");
        json.append("  \"currentPlayerId\": ").append(currentPlayerId).append("\n");
        json.append("}\n");
        Files.writeString(Path.of(filePath), json.toString());
    }

    private void appendRoads(StringBuilder json) {
        boolean first = true;
        for (Edge edge : board.getEdges()) {
            if (!edge.isOccupied()) continue;
            Road road = edge.getRoad();
            List<Node> endpoints = edge.getEndpoints();
            int a = endpoints.get(0).getId();
            int b = endpoints.get(1).getId();
            String owner = PLAYER_COLOURS[Math.min(road.getOwner().getId() - 1, PLAYER_COLOURS.length - 1)];
            if (!first) json.append(", ");
            json.append("\n    {\"a\": ").append(a).append(", \"b\": ").append(b).append(", \"owner\": \"").append(owner).append("\"}");
            first = false;
        }
    }

    private void appendBuildings(StringBuilder json) {
        boolean first = true;
        for (Node node : board.getNodes()) {
            if (!node.isOccupied()) continue;
            Building building = node.getBuilding();
            String owner = PLAYER_COLOURS[Math.min(building.getOwner().getId() - 1, PLAYER_COLOURS.length - 1)];
            String type = building.getType().name();
            if (!first) json.append(", ");
            json.append("\n    {\"node\": ").append(node.getId()).append(", \"owner\": \"").append(owner).append("\", \"type\": \"").append(type).append("\"}");
            first = false;
        }
    }
}
