package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests GameStateWriter JSON output for the visualizer (R2.2, R2.3).
 * Verifies that the JSON matches the format expected by light_visualizer.py.
 *
 * @author Sammy Tourani
 */
class GameStateWriterTest {

    private Board board;
    private List<Player> players;
    private Robber robber;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        board = new Board();
        board.initialize();
        players = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            players.add(new AgentPlayer(i));
        }
        robber = new Robber();
        robber.placeOn(board.getTiles().get(16)); // desert
    }

    @Test
    void testWriteEmptyState_containsRoadsAndBuildings() throws IOException {
        Path file = tempDir.resolve("state.json");
        GameStateWriter writer = new GameStateWriter(board, players, robber, 1, 1);
        writer.write(file.toString());

        String json = Files.readString(file);
        assertTrue(json.contains("\"roads\""),
                "JSON must contain roads array");
        assertTrue(json.contains("\"buildings\""),
                "JSON must contain buildings array");
    }

    @Test
    void testWriteWithSettlement_containsBuildingEntry() throws IOException {
        // Place a settlement for player 1 on node 0
        Node node = board.getNodes().get(0);
        node.setBuilding(new Building(BuildingType.SETTLEMENT, players.get(0)));

        Path file = tempDir.resolve("state.json");
        GameStateWriter writer = new GameStateWriter(board, players, robber, 1, 1);
        writer.write(file.toString());

        String json = Files.readString(file);
        assertTrue(json.contains("\"SETTLEMENT\""),
                "JSON should contain SETTLEMENT type");
        assertTrue(json.contains("\"RED\""),
                "Player 1 should map to RED");
    }

    @Test
    void testWriteWithRoad_containsRoadEntry() throws IOException {
        // Place a road for player 2
        Edge edge = board.getEdges().get(0);
        edge.setRoad(new Road(players.get(1), edge));

        Path file = tempDir.resolve("state.json");
        GameStateWriter writer = new GameStateWriter(board, players, robber, 1, 1);
        writer.write(file.toString());

        String json = Files.readString(file);
        assertTrue(json.contains("\"BLUE\""),
                "Player 2 should map to BLUE");
        assertTrue(json.contains("\"a\""),
                "Road JSON should contain 'a' field");
        assertTrue(json.contains("\"b\""),
                "Road JSON should contain 'b' field");
    }

    @Test
    void testPlayerColorMapping() throws IOException {
        // Place buildings for all 4 players to verify colour mapping
        Node n0 = board.getNodes().get(0);
        Node n6 = board.getNodes().get(6);
        Node n10 = board.getNodes().get(10);
        Node n24 = board.getNodes().get(24);
        n0.setBuilding(new Building(BuildingType.SETTLEMENT, players.get(0)));
        n6.setBuilding(new Building(BuildingType.SETTLEMENT, players.get(1)));
        n10.setBuilding(new Building(BuildingType.SETTLEMENT, players.get(2)));
        n24.setBuilding(new Building(BuildingType.SETTLEMENT, players.get(3)));

        Path file = tempDir.resolve("state.json");
        GameStateWriter writer = new GameStateWriter(board, players, robber, 1, 1);
        writer.write(file.toString());

        String json = Files.readString(file);
        assertTrue(json.contains("\"RED\""), "P1 -> RED");
        assertTrue(json.contains("\"BLUE\""), "P2 -> BLUE");
        assertTrue(json.contains("\"ORANGE\""), "P3 -> ORANGE");
        assertTrue(json.contains("\"WHITE\""), "P4 -> WHITE");
    }
}
