package ca.mcmaster.se2aa4.catan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CatanGame {

    private int currentRound;
    private int longestRoadLength;
    private Player longestRoadHolder;
    private int diceRollThisTurn;
    private TurnPhase currentTurnPhase;

    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private final Bank bank;
    private final Configuration configuration;
    private final Random random;
    private final Robber robber;
    private String stateFilePath;

    public CatanGame() {
        this.board = new Board();
        this.players = new ArrayList<>();
        this.dice = new Dice();
        this.bank = new Bank();
        this.configuration = new Configuration();
        this.random = new Random();
        this.robber = new Robber();
        this.stateFilePath = null;
        this.currentRound = 0;
        this.longestRoadLength = 4;
        this.longestRoadHolder = null;
        this.diceRollThisTurn = 0;
        this.currentTurnPhase = TurnPhase.AWAIT_ROLL;

        for (int i = 1; i <= 4; i++) {
            players.add(new AgentPlayer(i));
        }
    }

    public void setStateFilePath(String path) {
        this.stateFilePath = path;
    }

    public void play() {
        if (configuration.isHumanGame()) {
            int id = configuration.getHumanPlayerId();
            players.set(id - 1, new HumanPlayer(id, new ConsoleInputReader(), new CommandParser()));
        }
        board.initialize();
        setupPhase();
        placeRobberOnDesert();
        writeState();

        while (currentRound < configuration.getMaxRounds()) {
            currentRound++;
            if (executeRound()) {
                break;
            }
        }

        printRoundSummary();
    }

    private void placeRobberOnDesert() {
        for (Tile t : board.getTiles()) {
            if (t.getResourceType() == null) {
                robber.placeOn(t);
                break;
            }
        }
    }

    public void setupPhase() {
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
        player.useSetupSettlement();
        System.out.println("0 / P" + player.getId() + ": Placed settlement at node " + chosenNode.getId());

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
            player.useSetupRoad();
            List<Node> endpoints = chosenEdge.getEndpoints();
            System.out.println("0 / P" + player.getId() + ": Placed road between nodes "
                    + endpoints.get(0).getId() + " and " + endpoints.get(1).getId());
        }
    }

    public boolean executeRound() {
        for (Player player : players) {
            diceRollThisTurn = 0;
            executeTurn(player);
            writeState();
            if (checkWinCondition()) {
                printVictoryPoints();
                return true;
            }
            waitForGo();
        }
        printVictoryPoints();
        return false;
    }

    private void printVictoryPoints() {
        StringBuilder vpLine = new StringBuilder(currentRound + " / VP:");
        for (Player player : players) {
            int vp = calculateVictoryPoints(player);
            vpLine.append(" P").append(player.getId()).append("=").append(vp);
        }
        System.out.println(vpLine);
    }

    /**
     * Delegates the turn to the player via polymorphism (OCP).
     * Resets turn phase to AWAIT_ROLL before handing off to the player.
     */
    public void executeTurn(Player player) {
        currentTurnPhase = TurnPhase.AWAIT_ROLL;
        player.takeTurn(this);
        currentTurnPhase = TurnPhase.AWAIT_GO;
    }

    /**
     * Rolls the dice, transitions to ROLL_DICE phase, and returns the value.
     * Called by both AgentPlayer and HumanPlayer during their turn.
     */
    public int rollDice() {
        diceRollThisTurn = dice.roll();
        currentTurnPhase = TurnPhase.ROLL_DICE;
        return diceRollThisTurn;
    }

    public void waitForGo() {
        if (!configuration.isHumanGame()) {
            return;
        }
        // Reuse the HumanPlayer's input reader and parser to avoid duplicate Scanners
        HumanPlayer human = null;
        for (Player p : players) {
            if (p instanceof HumanPlayer) {
                human = (HumanPlayer) p;
                break;
            }
        }
        if (human == null) return;
        HumanInputReader input = human.getInputReader();
        CommandParser parser = human.getCommandParser();
        while (true) {
            System.out.print("> ");
            String line = input.hasNextLine() ? input.readLine() : "";
            if (parser.parse(line).getCommandType() == CommandType.GO) {
                return;
            }
        }
    }

    public void handleRollSeven(Player roller) {
        // Phase: ROBBER_DISCARD -> ROBBER_PLACE -> ROBBER_STEAL -> POST_ROLL
        currentTurnPhase = TurnPhase.ROBBER_DISCARD;

        // Discard half for players with >7 cards (R2.5)
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

        // Move robber to a random non-desert tile (different from current)
        currentTurnPhase = TurnPhase.ROBBER_PLACE;
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

        // Steal from a qualifying player adjacent to the robber's new tile
        currentTurnPhase = TurnPhase.ROBBER_STEAL;
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
        currentTurnPhase = TurnPhase.POST_ROLL;
    }

    public void tryBuildSettlement(Player player, int nodeId) {
        if (nodeId < 0 || nodeId >= board.getNodes().size()) {
            System.out.println("Invalid node id.");
            return;
        }
        Node node = board.getNodes().get(nodeId);
        if (!player.canBuildSettlement()) {
            System.out.println("Cannot build settlement (resources or pieces).");
            return;
        }
        if (!board.getAvailableSettlementNodes(player).contains(node)) {
            System.out.println("Node not available for settlement.");
            return;
        }
        player.buildSettlement(node, bank);
        System.out.println(currentRound + " / P" + player.getId() + ": Built settlement at node " + nodeId);
    }

    public void tryBuildCity(Player player, int nodeId) {
        if (nodeId < 0 || nodeId >= board.getNodes().size()) {
            System.out.println("Invalid node id.");
            return;
        }
        Node node = board.getNodes().get(nodeId);
        if (!player.canBuildCity()) {
            System.out.println("Cannot build city (resources or pieces).");
            return;
        }
        if (!board.getUpgradeableNodes(player).contains(node)) {
            System.out.println("Node does not have your settlement to upgrade.");
            return;
        }
        player.buildCity(node, bank);
        System.out.println(currentRound + " / P" + player.getId() + ": Built city at node " + nodeId);
    }

    public void tryBuildRoad(Player player, int fromId, int toId) {
        if (fromId < 0 || toId < 0) {
            System.out.println("Invalid edge.");
            return;
        }
        Edge edge = findEdge(fromId, toId);
        if (edge == null) {
            System.out.println("No such edge.");
            return;
        }
        if (!player.canBuildRoad()) {
            System.out.println("Cannot build road (resources or pieces).");
            return;
        }
        if (!board.getAvailableRoadEdges(player).contains(edge)) {
            System.out.println("Edge not available for road.");
            return;
        }
        player.buildRoad(edge, bank);
        System.out.println(currentRound + " / P" + player.getId() + ": Built road between nodes " + fromId + " and " + toId);
    }

    private Edge findEdge(int a, int b) {
        for (Edge e : board.getEdges()) {
            List<Node> ep = e.getEndpoints();
            if ((ep.get(0).getId() == a && ep.get(1).getId() == b) || (ep.get(0).getId() == b && ep.get(1).getId() == a)) {
                return e;
            }
        }
        return null;
    }

    private void writeState() {
        if (stateFilePath == null) return;
        try {
            int currentPlayerId = 1;
            GameStateWriter writer = new GameStateWriter(board, players, robber, currentRound, currentPlayerId);
            writer.write(stateFilePath);
        } catch (IOException e) {
            System.err.println("Could not write state: " + e.getMessage());
        }
    }

    public void distributeResources(int diceRoll) {
        currentTurnPhase = TurnPhase.POST_ROLL;
        Tile blocked = robber.getCurrentTile();
        List<Tile> activeTiles = board.getTilesForNumber(diceRoll);
        for (Tile tile : activeTiles) {
            if (tile == blocked) continue;
            ResourceType resource = tile.getResourceType();
            if (resource == null) continue;
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
        currentTurnPhase = TurnPhase.BUILD_OR_TRADE;
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
        int bestLength = 0;
        Player bestPlayer = null;

        for (Player player : players) {
            int roadLength = board.calculateLongestRoad(player);
            if (roadLength > bestLength) {
                bestLength = roadLength;
                bestPlayer = player;
            } else if (roadLength == bestLength && player == longestRoadHolder) {
                bestPlayer = player;
            }
        }

        if (bestLength < 5) {
            bestPlayer = null;
        }

        if (bestPlayer != longestRoadHolder) {
            longestRoadHolder = bestPlayer;
            longestRoadLength = (bestPlayer != null) ? bestLength : 4;
            if (bestPlayer != null) {
                System.out.println(currentRound + " / P" + bestPlayer.getId()
                        + ": Claimed longest road (" + bestLength + ")");
            }
        } else if (bestPlayer != null) {
            longestRoadLength = bestLength;
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

    public Board getBoard() {
        return board;
    }

    public Bank getBank() {
        return bank;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public TurnPhase getCurrentTurnPhase() {
        return currentTurnPhase;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
