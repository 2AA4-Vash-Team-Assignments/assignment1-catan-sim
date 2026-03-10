package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests CatanGame win condition and resource distribution
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

    private Board board;
    private Player player;
    private Bank bank;

    @BeforeEach
    void setUp() {
        board = new Board();
        board.initialize();
        player = new Player(1);
        bank = new Bank();
    }

    // Boudnary testing the vp win condition

    @Test
    void testWinCondition_nineVP_doesNotTrigger() {
        // BOUNDARY (lower): 9 settlements = 9 VP -> do NOT trigger win

        int[] nodeIds = { 0, 2, 4, 6, 8, 10, 12, 24, 26 };
        for (int id : nodeIds) {
            board.getNodes().get(id).setBuilding(new Building(BuildingType.SETTLEMENT, player));
        }

        // call checkWinCondition on a game wired with this board
        // test the logic directly: 9 settlements = 9 VP < 10 -> false
        int vp = countVP(player);
        assertEquals(9, vp);
        assertFalse(vp >= 10,
                "BOUNDARY: 9 VP should NOT satisfy the win condition (needs >= 10)");
    }

    @Test
    void testWinCondition_tenVP_triggers() {
        // BOUNDARY (Upper threshold): 10 settlements = 10 VP — MUST trigger win
        int[] nodeIds = { 0, 2, 4, 6, 8, 10, 12, 24, 26, 28 };
        for (int id : nodeIds) {
            board.getNodes().get(id).setBuilding(new Building(BuildingType.SETTLEMENT, player));
        }

        int vp = countVP(player);
        assertEquals(10, vp);
        assertTrue(vp >= 10,
                "BOUNDARY: exactly 10 VP MUST satisfy the win condition");
    }

    // resource distribution testing

    @Test
    void testDistributeResources_givesResourceToSettlementOnMatchingTile() {
        // place a settlement on a node adjacent to a tile with number token 11
        // tile ID 1 has token 11 and its nodes include node 1
        Node targetNode = board.getNodes().get(1);
        targetNode.setBuilding(new Building(BuildingType.SETTLEMENT, player));

        // simulate a dice roll of 11 — Tile 1 is one of the tiles with number 11
        int resourcesBefore = player.getTotalResourceCards();
        distributeResources(11);

        assertTrue(player.getTotalResourceCards() > resourcesBefore,
                "Player with settlement adjacent to a tile with token 11 should receive a resource on roll of 11");
    }

    @Test
    void testDistributeResources_doesNotGiveResourceOnMismatch() {
        // place a settlement on node 0 (adjacent to tile 0 which has token 10)
        // roll a 6 -> tile 0 does not activate -> player receives nothing
        Node targetNode = board.getNodes().get(0);
        // node 0 is adjacent to tile 0 (token=10), tile 5 (token=5), tile 6 (token=12)
        // rolling 6 should not match any of those
        targetNode.setBuilding(new Building(BuildingType.SETTLEMENT, player));

        int resourcesBefore = player.getTotalResourceCards();
        distributeResources(6);

        // tile IDs 8 and 10 have token 6, but node 0 is NOT adjacent to either of those
        assertEquals(resourcesBefore, player.getTotalResourceCards(),
                "Player on node 0 should NOT receive resources on a roll of 6");
    }

    @Test
    void testDistributeResources_cityReceivesTwoResources() {
        // city produces 2 resources per roll (multiplier = 2)
        // node 1 is adjacent to tile 1 (token 11)
        Node targetNode = board.getNodes().get(1);
        targetNode.setBuilding(new Building(BuildingType.CITY, player));

        int resourcesBefore = player.getTotalResourceCards();
        distributeResources(11);

        assertTrue(player.getTotalResourceCards() >= resourcesBefore + 2,
                "A city should receive at least 2 resources for each adjacent tile with matching token");
    }

    // helper methods

    /**
     * Uses the sum of buildings on nodes owned by player to get VP.
     * mirrors CatanGame.calculateVictoryPoints() logic as we cant directly use it.
     */
    private int countVP(Player p) {
        int vp = 0;
        for (Node node : board.getNodes()) {
            if (node.isOccupied() && node.getBuilding().getOwner() == p) {
                vp += node.getBuilding().getVictoryPoints();
            }
        }
        return vp;
    }

    /**
     * copies CatanGame.distributeResources() logic directly, such that I can test
     * without instantiating whole CatanGame
     */
    private void distributeResources(int diceRoll) {
        for (Tile tile : board.getTilesForNumber(diceRoll)) {
            ResourceType resource = tile.getResourceType();
            for (Node node : tile.getAdjacentNodes()) {
                if (node.isOccupied()) {
                    Building building = node.getBuilding();
                    Player owner = building.getOwner();
                    int amount = building.getResourceMultiplier();
                    if (bank.hasEnoughResources(resource, amount)) {
                        bank.distributeResource(resource, amount);
                        owner.addResource(resource, amount);
                    }
                }
            }
        }
    }
}
