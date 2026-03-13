package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests CatanGame win condition and resource distribution
 * using the actual CatanGame methods (checkWinCondition, distributeResources).
 *
 * BOUNDARY TESTING
 * The win condition fires at exactly 10 VP (vp >= 10).
 * We test the boundary from both sides:
 * -9 VP -> should NOT trigger a win
 * -10 VP -> should trigger a win
 *
 * @author Vaishnav Yandrapalli 400572601
 */
class CatanGameTest {

    private CatanGame game;
    private Board board;

    @BeforeEach
    void setUp() {
        game = new CatanGame();
        board = game.getBoard();
        board.initialize();
        game.setupPhase();
    }

    // Boundary testing the vp win condition via CatanGame.checkWinCondition()

    @Test
    void testWinCondition_nineVP_doesNotTrigger() {
        // BOUNDARY (lower): 9 settlements = 9 VP -> do NOT trigger win
        // find one of the game's actual players from an existing building
        Player player = findGamePlayer();

        for (Node n : board.getNodes()) {
            if (n.isOccupied()) n.setBuilding(null);
        }

        int[] nodeIds = { 0, 2, 4, 6, 8, 10, 12, 24, 26 };
        for (int id : nodeIds) {
            board.getNodes().get(id).setBuilding(new Building(BuildingType.SETTLEMENT, player));
        }

        assertFalse(game.checkWinCondition(),
                "BOUNDARY: 9 VP should NOT satisfy the win condition (needs >= 10)");
    }

    @Test
    void testWinCondition_tenVP_triggers() {
        // BOUNDARY (Upper threshold): 10 settlements = 10 VP — MUST trigger win
        Player player = findGamePlayer();

        for (Node n : board.getNodes()) {
            if (n.isOccupied()) n.setBuilding(null);
        }

        int[] nodeIds = { 0, 2, 4, 6, 8, 10, 12, 24, 26, 28 };
        for (int id : nodeIds) {
            board.getNodes().get(id).setBuilding(new Building(BuildingType.SETTLEMENT, player));
        }

        assertTrue(game.checkWinCondition(),
                "BOUNDARY: exactly 10 VP MUST satisfy the win condition");
    }

    /**
     * Retrieves a Player instance from the game's internal player list
     * by finding an owner of an existing building placed during setupPhase().
     */
    private Player findGamePlayer() {
        for (Node n : board.getNodes()) {
            if (n.isOccupied()) {
                return n.getBuilding().getOwner();
            }
        }
        fail("Setup phase should have placed at least one building");
        return null;
    }

    // resource distribution testing via CatanGame.distributeResources()

    @Test
    void testDistributeResources_givesResourceToSettlementOnMatchingTile() {
        // place a settlement on a node adjacent to a tile with number token 11
        Player player = new AgentPlayer(99);
        for (Node n : board.getNodes()) {
            if (n.isOccupied()) n.setBuilding(null);
        }
        board.getNodes().get(1).setBuilding(new Building(BuildingType.SETTLEMENT, player));

        int before = player.getTotalResourceCards();
        game.distributeResources(11);

        assertTrue(player.getTotalResourceCards() > before,
                "Player with settlement adjacent to a tile with token 11 should receive a resource on roll of 11");
    }

    @Test
    void testDistributeResources_doesNotGiveResourceOnMismatch() {
        Player player = new AgentPlayer(99);
        for (Node n : board.getNodes()) {
            if (n.isOccupied()) n.setBuilding(null);
        }
        // node 0 is adjacent to tile 0 (token=10), tile 5 (token=5), tile 6 (token=12)
        board.getNodes().get(0).setBuilding(new Building(BuildingType.SETTLEMENT, player));

        int before = player.getTotalResourceCards();
        game.distributeResources(6);

        assertEquals(before, player.getTotalResourceCards(),
                "Player on node 0 should NOT receive resources on a roll of 6");
    }

    @Test
    void testDistributeResources_cityReceivesTwoResources() {
        Player player = new AgentPlayer(99);
        for (Node n : board.getNodes()) {
            if (n.isOccupied()) n.setBuilding(null);
        }
        board.getNodes().get(1).setBuilding(new Building(BuildingType.CITY, player));

        int before = player.getTotalResourceCards();
        game.distributeResources(11);

        assertTrue(player.getTotalResourceCards() >= before + 2,
                "A city should receive at least 2 resources for each adjacent tile with matching token");
    }
}
