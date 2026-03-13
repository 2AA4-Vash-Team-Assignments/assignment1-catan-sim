package ca.mcmaster.se2aa4.catan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A computer-controlled player that makes random decisions.
 * Implements the agent behaviour: auto-roll, distribute resources,
 * then build randomly (R1.8: forced spending when hand > 7).
 */
public class AgentPlayer extends Player {

    private final Random random;

    public AgentPlayer(int id) {
        super(id);
        this.random = new Random();
    }

    @Override
    public void takeTurn(CatanGame game) {
        int diceRoll = game.rollDice();
        if (diceRoll == 7) {
            game.handleRollSeven(this);
        } else {
            game.distributeResources(diceRoll);
        }
        chooseRandomAction(game.getBoard(), game.getBank(), game.getCurrentRound());
        game.updateLongestRoad();
    }

    /**
     * R1.8: Implements a simple linear check of all actions that can be executed,
     * then picks one randomly. Agents with >7 cards must try to spend by building.
     */
    public void chooseRandomAction(Board board, Bank bank, int currentRound) {
        while (true) {
            List<Runnable> actions = collectPossibleActions(board, bank, currentRound);
            if (actions.isEmpty()) {
                break;
            }
            Runnable chosen = actions.get(random.nextInt(actions.size()));
            chosen.run();
            if (getTotalResourceCards() <= 7) {
                break;
            }
        }
    }

    private List<Runnable> collectPossibleActions(Board board, Bank bank, int currentRound) {
        List<Runnable> actions = new ArrayList<>();

        if (canBuildCity()) {
            for (Node node : board.getUpgradeableNodes(this)) {
                Node n = node;
                actions.add(() -> {
                    buildCity(n, bank);
                    System.out.println(currentRound + " / P" + id + ": Built city at node " + n.getId());
                });
            }
        }
        if (canBuildSettlement()) {
            for (Node node : board.getAvailableSettlementNodes(this)) {
                Node n = node;
                actions.add(() -> {
                    buildSettlement(n, bank);
                    System.out.println(currentRound + " / P" + id + ": Built settlement at node " + n.getId());
                });
            }
        }
        if (canBuildRoad()) {
            for (Edge edge : board.getAvailableRoadEdges(this)) {
                Edge e = edge;
                actions.add(() -> {
                    buildRoad(e, bank);
                    List<Node> endpoints = e.getEndpoints();
                    System.out.println(currentRound + " / P" + id + ": Built road between nodes "
                            + endpoints.get(0).getId() + " and " + endpoints.get(1).getId());
                });
            }
        }
        return actions;
    }
}
