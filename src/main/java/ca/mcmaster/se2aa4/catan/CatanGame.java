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

        for (int i = 1; i <= 4; i++) {
            players.add(new Player(i));
        }
    }

    public void setStateFilePath(String path) {
        this.stateFilePath = path;
    }

    public void play() {
        if (configuration.isHumanGame()) {
            int id = configuration.getHumanPlayerId();
            players.set(id - 1, new Player(id, new ConsoleInputReader(), new CommandParser()));
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
        player.useSetupSettlement();
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
        if (player.isHuman()) {
            executeHumanTurn(player);
        } else {
            diceRollThisTurn = dice.roll();
            System.out.println(currentRound + " / P" + player.getId() + " / Dice: " + diceRollThisTurn);
            if (diceRollThisTurn == 7) {
                handleRollSeven(player);
            } else {
                distributeResources(diceRollThisTurn);
            }
            player.chooseRandomAction(board, bank, currentRound);
            updateLongestRoad();
        }
    }

    private void executeHumanTurn(Player player) {
        HumanInputReader input = player.getInputReader();
        CommandParser parser = player.getCommandParser();
        while (true) {
            System.out.print("P" + player.getId() + "> ");
            String line = input.hasNextLine() ? input.readLine() : "";
            ParsedCommand cmd = parser.parse(line);
            switch (cmd.getCommandType()) {
                case ROLL:
                    if (diceRollThisTurn != 0) {
                        System.out.println("Already rolled this turn.");
                        break;
                    }
                    diceRollThisTurn = dice.roll();
                    System.out.println(currentRound + " / P" + player.getId() + " / Dice: " + diceRollThisTurn);
                    if (diceRollThisTurn == 7) {
                        handleRollSeven(player);
                    } else {
                        distributeResources(diceRollThisTurn);
                    }
                    break;
                case GO:
                    if (diceRollThisTurn == 0) {
                        System.out.println("Roll first.");
                        break;
                    }
                    updateLongestRoad();
                    return;
                case LIST:
                    listHand(player);
                    break;
                case BUILD_SETTLEMENT:
                    tryBuildSettlement(player, cmd.getNodeId());
                    break;
                case BUILD_CITY:
                    tryBuildCity(player, cmd.getNodeId());
                    break;
                case BUILD_ROAD:
                    tryBuildRoad(player, cmd.getFromNodeId(), cmd.getToNodeId());
                    break;
                default:
                    if (!line.isBlank()) {
                        System.out.println("Unknown command. Use: Roll, Go, List, Build settlement <id>, Build city <id>, Build road <from>,<to>");
                    }
            }
        }
    }

    private void listHand(Player player) {
        StringBuilder sb = new StringBuilder();
        for (ResourceType t : ResourceType.values()) {
            int c = player.getResourceCount(t);
            if (c > 0) sb.append(t).append("=").append(c).append(" ");
        }
        System.out.println(sb.length() > 0 ? sb.toString() : "No resources");
    }

    private void tryBuildSettlement(Player player, int nodeId) {
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

    private void tryBuildCity(Player player, int nodeId) {
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

    private void tryBuildRoad(Player player, int fromId, int toId) {
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

    public void waitForGo() {
        if (!configuration.isHumanGame()) {
            return;
        }
        HumanInputReader input = new ConsoleInputReader();
        CommandParser parser = new CommandParser();
        while (true) {
            System.out.print("> ");
            String line = input.hasNextLine() ? input.readLine() : "";
            if (parser.parse(line).getCommandType() == CommandType.GO) {
                return;
            }
        }
    }

    private void handleRollSeven(Player roller) {
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
                bestPlayer = player; // current holder retains on tie
            }
        }

        // Must have at least 5 roads to hold longest road
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

    public Configuration getConfiguration() {
        return configuration;
    }
}
