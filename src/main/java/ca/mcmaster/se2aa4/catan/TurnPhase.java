package ca.mcmaster.se2aa4.catan;

/**
 * Enumerates the phases of a single player's turn.
 * Maps directly to the agent-turn-automaton state machine.
 * Guards which actions are legal at each point in the turn.
 */
public enum TurnPhase {
    AWAIT_ROLL,
    ROLL_DICE,
    ROBBER_DISCARD,
    ROBBER_PLACE,
    ROBBER_STEAL,
    POST_ROLL,
    BUILD_OR_TRADE,
    AWAIT_GO
}
