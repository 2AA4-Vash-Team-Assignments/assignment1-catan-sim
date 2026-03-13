package ca.mcmaster.se2aa4.catan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Encapsulates the robber sequence triggered when a 7 is rolled (R2.5).
 * Extracted from CatanGame to satisfy the Single Responsibility Principle:
 * CatanGame orchestrates the game loop, while RobberHandler owns the
 * discard / place / steal sub-sequence.
 */
public class RobberHandler {

    private final Board board;
    private final List<Player> players;
    private final Robber robber;
    private final Bank bank;
    private final Random random;

    public RobberHandler(Board board, List<Player> players, Robber robber, Bank bank, Random random) {
        this.board = board;
        this.players = players;
        this.robber = robber;
        this.bank = bank;
        this.random = random;
    }

    /**
     * Executes the full roll-seven sequence: discard, place robber, steal.
     * Returns each phase transition so CatanGame can update currentTurnPhase.
     */
    public void execute(Player roller, int currentRound) {
        discardFromPlayersOverLimit(currentRound);
        moveRobber();
        stealFromAdjacentPlayer(roller, currentRound);
    }

    private void discardFromPlayersOverLimit(int currentRound) {
        for (Player p : players) {
            int total = p.getTotalResourceCards();
            if (total > 7) {
                int discard = total / 2;
                int discarded = 0;
                for (ResourceType type : ResourceType.values()) {
                    if (discarded >= discard) break;
                    int have = p.getResourceCount(type);
                    int toDiscard = Math.min(have, discard - discarded);
                    if (toDiscard > 0) {
                        p.removeResource(type, toDiscard);
                        bank.collectResource(type, toDiscard);
                        discarded += toDiscard;
                        System.out.println(currentRound + " / P" + p.getId() + ": Discarded " + toDiscard + " " + type);
                    }
                }
            }
        }
    }

    private void moveRobber() {
        Tile robberTile = robber.getCurrentTile();
        List<Tile> placeable = new ArrayList<>();
        for (Tile t : board.getTiles()) {
            if (t.getResourceType() != null && t != robberTile) {
                placeable.add(t);
            }
        }
        if (!placeable.isEmpty()) {
            Tile newTile = placeable.get(random.nextInt(placeable.size()));
            robber.placeOn(newTile);
        }
    }

    private void stealFromAdjacentPlayer(Player roller, int currentRound) {
        List<Player> qualifying = new ArrayList<>();
        for (Node node : robber.getCurrentTile().getAdjacentNodes()) {
            if (node.isOccupied()) {
                Player owner = node.getBuilding().getOwner();
                if (owner != roller && owner.getTotalResourceCards() > 0 && !qualifying.contains(owner)) {
                    qualifying.add(owner);
                }
            }
        }
        if (!qualifying.isEmpty()) {
            Player victim = qualifying.get(random.nextInt(qualifying.size()));
            List<ResourceType> options = new ArrayList<>();
            for (ResourceType t : ResourceType.values()) {
                if (victim.getResourceCount(t) > 0) options.add(t);
            }
            if (!options.isEmpty()) {
                ResourceType stolen = options.get(random.nextInt(options.size()));
                victim.removeResource(stolen, 1);
                roller.addResource(stolen, 1);
                System.out.println(currentRound + " / P" + roller.getId() + ": Stole 1 " + stolen + " from P" + victim.getId());
            }
        }
    }
}
