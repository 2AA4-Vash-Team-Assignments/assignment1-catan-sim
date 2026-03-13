package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests TurnPhase transitions and guard enforcement in CatanGame.
 * Verifies the automaton states exist, the game initializes to the
 * correct starting phase, and that build actions are blocked outside
 * the BUILD_OR_TRADE / POST_ROLL phases.
 *
 * @author Sammy Tourani
 */
class TurnPhaseTest {

    @Test
    void testAllPhasesExist() {
        TurnPhase[] expected = {
            TurnPhase.AWAIT_ROLL,
            TurnPhase.ROLL_DICE,
            TurnPhase.ROBBER_DISCARD,
            TurnPhase.ROBBER_PLACE,
            TurnPhase.ROBBER_STEAL,
            TurnPhase.POST_ROLL,
            TurnPhase.BUILD_OR_TRADE,
            TurnPhase.AWAIT_GO
        };
        assertEquals(8, TurnPhase.values().length,
                "TurnPhase should have exactly 8 states matching the automaton");
        for (TurnPhase phase : expected) {
            assertNotNull(phase);
        }
    }

    @Test
    void testGameStartsInAwaitRoll() {
        CatanGame game = new CatanGame();
        assertEquals(TurnPhase.AWAIT_ROLL, game.getCurrentTurnPhase(),
                "Game should start in AWAIT_ROLL phase");
    }

    @Test
    void testRollDiceTransitionsPhase() {
        CatanGame game = new CatanGame();
        game.rollDice();
        assertEquals(TurnPhase.ROLL_DICE, game.getCurrentTurnPhase(),
                "After rolling dice, phase should be ROLL_DICE");
    }

    @Test
    void testBuildBlockedInAwaitRollPhase() {
        // guard enforcement: building should be rejected when phase is AWAIT_ROLL
        CatanGame game = new CatanGame();
        game.getBoard().initialize();
        game.setupPhase();

        // game starts in AWAIT_ROLL — tryBuildSettlement should be blocked
        assertEquals(TurnPhase.AWAIT_ROLL, game.getCurrentTurnPhase());
        Player player = new AgentPlayer(99);
        player.addResource(ResourceType.BRICK, 1);
        player.addResource(ResourceType.WOOD, 1);
        player.addResource(ResourceType.WHEAT, 1);
        player.addResource(ResourceType.SHEEP, 1);

        int before = player.getTotalResourceCards();
        game.tryBuildSettlement(player, 0);
        // resources should be unchanged because the guard blocked the action
        assertEquals(before, player.getTotalResourceCards(),
                "Build should be blocked in AWAIT_ROLL phase — resources unchanged");
    }

    @Test
    void testDistributeResourcesTransitionsToBuildOrTrade() {
        CatanGame game = new CatanGame();
        game.getBoard().initialize();
        game.setupPhase();

        game.rollDice();
        game.distributeResources(10);
        assertEquals(TurnPhase.BUILD_OR_TRADE, game.getCurrentTurnPhase(),
                "After distributeResources, phase should be BUILD_OR_TRADE");
    }
}
