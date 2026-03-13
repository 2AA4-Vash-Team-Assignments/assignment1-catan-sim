package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the TurnPhase enum values and CatanGame phase transitions.
 * Verifies that the automaton states exist and the game initializes
 * to the correct starting phase.
 *
 * @author Sammy Tourani
 */
class TurnPhaseTest {

    @Test
    void testAllPhasesExist() {
        // The automaton defines these states; verify they are all present
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
}
