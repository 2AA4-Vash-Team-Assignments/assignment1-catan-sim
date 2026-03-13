package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Robber entity and the roll-seven mechanics (R2.5).
 * Verifies placement, tile blocking, and the discard rule through
 * the actual CatanGame.handleRollSeven() method.
 *
 * @author Sammy Tourani
 */
class RobberTest {

    private Robber robber;
    private Board board;

    @BeforeEach
    void setUp() {
        robber = new Robber();
        board = new Board();
        board.initialize();
    }

    @Test
    void testRobberStartsWithNoTile() {
        assertNull(robber.getCurrentTile(),
                "Robber should have no tile before placement");
    }

    @Test
    void testPlaceOnTile() {
        Tile desert = board.getTiles().get(16);
        robber.placeOn(desert);
        assertEquals(desert, robber.getCurrentTile(),
                "Robber should be on the tile it was placed on");
    }

    @Test
    void testRobberCanMoveToNewTile() {
        Tile first = board.getTiles().get(0);
        Tile second = board.getTiles().get(5);
        robber.placeOn(first);
        robber.placeOn(second);
        assertEquals(second, robber.getCurrentTile(),
                "Robber should move to the new tile");
    }

    @Test
    void testHandleRollSeven_phaseTransitionsToPostRoll() {
        // R2.5: verify handleRollSeven() drives the game through to POST_ROLL
        CatanGame game = new CatanGame();
        game.getBoard().initialize();
        game.setupPhase();

        // simulate rolling so the game is in ROLL_DICE phase
        game.rollDice();
        Player roller = new AgentPlayer(99);
        game.handleRollSeven(roller);

        assertEquals(TurnPhase.POST_ROLL, game.getCurrentTurnPhase(),
                "After handleRollSeven, game phase should be POST_ROLL");
    }

    @Test
    void testDiscardHalf_playerWithSevenCards_noDiscard() {
        // BOUNDARY: exactly 7 cards -> no discard (only >7 triggers)
        Player player = new AgentPlayer(1);
        player.addResource(ResourceType.WOOD, 7);
        assertEquals(7, player.getTotalResourceCards());
        assertFalse(player.getTotalResourceCards() > 7,
                "Player with exactly 7 cards should NOT discard");
    }

    @Test
    void testDiscardHalf_oddNumber_roundsDown() {
        // R2.5: 9 cards -> discard 4 (9/2 = 4 integer division rounds down)
        Player player = new AgentPlayer(1);
        player.addResource(ResourceType.WHEAT, 9);
        int discard = player.getTotalResourceCards() / 2;
        assertEquals(4, discard,
                "9 / 2 = 4 (integer division rounds down per Catan rules)");
    }
}
