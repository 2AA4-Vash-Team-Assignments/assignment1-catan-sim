package ca.mcmaster.se2aa4.catan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CatanGame {

    private int currentRound;
    private int longestRoadLength;
    private Player longestRoadHolder;

    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private final Bank bank;
    private final Configuration configuration;
    private final Random random;

    public CatanGame() {
        this.board = new Board();
        this.players = new ArrayList<>();
        this.dice = new Dice();
        this.bank = new Bank();
        this.configuration = new Configuration();
        this.random = new Random();
        this.currentRound = 0;
        this.longestRoadLength = 4; // Need at least 5 to claim
        this.longestRoadHolder = null;

        for (int i = 1; i <= 4; i++) {
            players.add(new Player(i));
        }
    }

    public void play() {
        board.initialize();
        setupPhase();

        while (currentRound < configuration.getMaxRounds()) {
            currentRound++;
            executeRound();
            if (checkWinCondition()) {
                break;
            }
        }

        printRoundSummary();
    }

    public void setupPhase() {
        // Snake draft: each player places 2 settlements + 2 roads in 1-2-3-4-4-3-2-1 order
        // First placement: no resources. Second placement: receive resources from adjacent tiles
        for (Player player : players) {
            placeInitialSettlementAndRoad(player, false);
        }
        for (int i = players.size() - 1; i >= 0; i--) {
            placeInitialSettlementAndRoad(players.get(i), true);
        }
    }

    private void placeInitialSettlementAndRoad(Player player, boolean isSecondPlacement) {
        List<Node> availableNodes = board.getAvailableSetupNodes();
        if (availableNodes.isEmpty()) return;

        Node chosenNode = availableNodes.get(random.nextInt(availableNodes.size()));
        Building settlement = new Building(BuildingType.SETTLEMENT, player);
        chosenNode.setBuilding(settlement);
        System.out.println("0 / P" + player.getId() + ": Placed settlement at node " + chosenNode.getId());

        // Second settlement: receive 1 resource per adjacent tile (skipping desert)
        if (isSecondPlacement) {
            for (Tile tile : chosenNode.getAdjacentTiles()) {
                ResourceType resource = tile.getResourceType();
                if (resource != null && bank.hasEnoughResources(resource, 1)) {
                    bank.distributeResource(resource, 1);
                    player.addResource(resource, 1);
                    System.out.println("0 / P" + player.getId() + ": Received 1 " + resource + " (starting resources)");
                }
            }
        }

        // Place a road on an adjacent edge
        List<Edge> adjacentEdges = chosenNode.getAdjacentEdges();
        List<Edge> freeEdges = new ArrayList<>();
        for (Edge edge : adjacentEdges) {
            if (!edge.isOccupied()) {
                freeEdges.add(edge);
            }
        }
        if (!freeEdges.isEmpty()) {
            Edge chosenEdge = freeEdges.get(random.nextInt(freeEdges.size()));
            Road road = new Road(player, chosenEdge);
            chosenEdge.setRoad(road);
            List<Node> endpoints = chosenEdge.getEndpoints();
            System.out.println("0 / P" + player.getId() + ": Placed road between nodes "
                    + endpoints.get(0).getId() + " and " + endpoints.get(1).getId());
        }
    }

    public void executeRound() {
        int diceRoll = dice.roll();
        System.out.println(currentRound + " / Dice: " + diceRoll);

        if (diceRoll != 7) {
            distributeResources(diceRoll);
        }
        // On 7: no resources produced (robber excluded per assignment spec)

        for (Player player : players) {
            executeTurn(player);
            if (checkWinCondition()) {
                return;
            }
        }

        // R1.7: Print current victory points at end of each round
        printVictoryPoints();
    }

    /**
     * Prints the current victory points for all players.
     * Called at the end of each round per R1.7.
     */
    private void printVictoryPoints() {
        StringBuilder vpLine = new StringBuilder(currentRound + " / VP:");
        for (Player player : players) {
            int vp = calculateVictoryPoints(player);
            vpLine.append(" P").append(player.getId()).append("=").append(vp);
        }
        System.out.println(vpLine);
    }

    public void executeTurn(Player player) {
        // R1.8: agents with >7 cards must try to spend; all agents may build
        // chooseRandomAction does linear check of possible actions and picks one randomly
        player.chooseRandomAction(board, bank, currentRound);
        updateLongestRoad();
    }

    public void distributeResources(int diceRoll) {
        List<Tile> activeTiles = board.getTilesForNumber(diceRoll);
        for (Tile tile : activeTiles) {
            ResourceType resource = tile.getResourceType();
            for (Node node : tile.getAdjacentNodes()) {
                if (node.isOccupied()) {
                    Building building = node.getBuilding();
                    Player owner = building.getOwner();
                    int amount = building.getResourceMultiplier();
                    if (bank.hasEnoughResources(resource, amount)) {
                        bank.distributeResource(resource, amount);
                        owner.addResource(resource, amount);
                        System.out.println(currentRound + " / P" + owner.getId()
                                + ": Received " + amount + " " + resource);
                    }
                }
            }
        }
    }

    public boolean checkWinCondition() {
        for (Player player : players) {
            int vp = calculateVictoryPoints(player);
            if (vp >= 10) {
                System.out.println(currentRound + " / P" + player.getId()
                        + ": Wins with " + vp + " victory points!");
                return true;
            }
        }
        return false;
    }

    private int calculateVictoryPoints(Player player) {
        int vp = 0;
        for (Node node : board.getNodes()) {
            if (node.isOccupied() && node.getBuilding().getOwner() == player) {
                vp += node.getBuilding().getVictoryPoints();
            }
        }
        if (longestRoadHolder == player) {
            vp += 2;
        }
        return vp;
    }

    public void updateLongestRoad() {
        for (Player player : players) {
            int roadLength = board.calculateLongestRoad(player);
            if (roadLength > longestRoadLength) {
                longestRoadLength = roadLength;
                if (longestRoadHolder != player) {
                    longestRoadHolder = player;
                    System.out.println(currentRound + " / P" + player.getId()
                            + ": Claimed longest road (" + roadLength + ")");
                }
            }
        }
    }

    public void printRoundSummary() {
        System.out.println("=== Game Over ===");
        System.out.println("Rounds played: " + currentRound);
        for (Player player : players) {
            int vp = calculateVictoryPoints(player);
            System.out.println("P" + player.getId() + ": " + vp + " VP, "
                    + player.getTotalResourceCards() + " resource cards");
        }
        if (longestRoadHolder != null) {
            System.out.println("Longest road: P" + longestRoadHolder.getId()
                    + " (" + longestRoadLength + ")");
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
