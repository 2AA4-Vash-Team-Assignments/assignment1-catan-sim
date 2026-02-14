package ca.mcmaster.se2aa4.catan;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Player {

    private final int id;
    private final Map<ResourceType, Integer> resources;
    private int remainingSettlements;
    private int remainingCities;
    private int remainingRoads;
    private final Random random;

    public Player(int id) {
        this.id = id;
        this.resources = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0);
        }
        this.remainingSettlements = 5;
        this.remainingCities = 4;
        this.remainingRoads = 15;
        this.random = new Random();
    }

    public int getId() {
        return id;
    }

    /**
     * Stub: VP calculation requires board state (buildings on nodes + longest road),
     * so the real logic lives in CatanGame.calculateVictoryPoints(player).
     * Kept here for traceability to the UML model where this method is defined on Player.
     */
    public int getVictoryPoints() {
        return 0;
    }

    public int getTotalResourceCards() {
        int total = 0;
        for (int count : resources.values()) {
            total += count;
        }
        return total;
    }

    public int getResourceCount(ResourceType type) {
        return resources.get(type);
    }

    public void addResource(ResourceType type, int amount) {
        resources.put(type, resources.get(type) + amount);
    }

    public void removeResource(ResourceType type, int amount) {
        resources.put(type, Math.max(0, resources.get(type) - amount));
    }

    public boolean canBuildRoad() {
        return remainingRoads > 0
                && getResourceCount(ResourceType.BRICK) >= 1
                && getResourceCount(ResourceType.WOOD) >= 1;
    }

    public boolean canBuildSettlement() {
        return remainingSettlements > 0
                && getResourceCount(ResourceType.BRICK) >= 1
                && getResourceCount(ResourceType.WOOD) >= 1
                && getResourceCount(ResourceType.WHEAT) >= 1
                && getResourceCount(ResourceType.SHEEP) >= 1;
    }

    public boolean canBuildCity() {
        return remainingCities > 0
                && getResourceCount(ResourceType.WHEAT) >= 2
                && getResourceCount(ResourceType.ORE) >= 3;
    }

    public void buildRoad(Edge edge, Bank bank) {
        removeResource(ResourceType.BRICK, 1);
        removeResource(ResourceType.WOOD, 1);
        bank.collectResource(ResourceType.BRICK, 1);
        bank.collectResource(ResourceType.WOOD, 1);
        Road road = new Road(this, edge);
        edge.setRoad(road);
        remainingRoads--;
    }

    public void buildSettlement(Node node, Bank bank) {
        removeResource(ResourceType.BRICK, 1);
        removeResource(ResourceType.WOOD, 1);
        removeResource(ResourceType.WHEAT, 1);
        removeResource(ResourceType.SHEEP, 1);
        bank.collectResource(ResourceType.BRICK, 1);
        bank.collectResource(ResourceType.WOOD, 1);
        bank.collectResource(ResourceType.WHEAT, 1);
        bank.collectResource(ResourceType.SHEEP, 1);
        Building settlement = new Building(BuildingType.SETTLEMENT, this);
        node.setBuilding(settlement);
        remainingSettlements--;
    }

    public void buildCity(Node node, Bank bank) {
        removeResource(ResourceType.WHEAT, 2);
        removeResource(ResourceType.ORE, 3);
        bank.collectResource(ResourceType.WHEAT, 2);
        bank.collectResource(ResourceType.ORE, 3);
        node.getBuilding().setType(BuildingType.CITY);
        remainingCities--;
        remainingSettlements++;
    }

    /**
     * R1.8: Implements a simple linear check of all actions that can be executed,
     * then picks one randomly. Agents with >7 cards must try to spend by building.
     *
     * @param board        the game board
     * @param bank         the resource bank (resources returned when spent)
     * @param currentRound the current round (for output encoding)
     */
    public void chooseRandomAction(Board board, Bank bank, int currentRound) {
        // R1.8: agents with >7 cards must keep trying to spend until <=7 or no options
        while (true) {
            List<Runnable> actions = collectPossibleActions(board, bank, currentRound);
            if (actions.isEmpty()) {
                break;
            }
            // Pick one action randomly from all available options
            Runnable chosen = actions.get(random.nextInt(actions.size()));
            chosen.run();
            // Stop when no longer forced to spend (>7), or after one build for randomly acting
            if (getTotalResourceCards() <= 7) {
                break;
            }
        }
    }

    /**
     * Linear check: enumerates all possible build actions (city, settlement, road)
     * that the player can currently execute.
     */
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

    public void useSetupSettlement() {
        remainingSettlements--;
    }

    public void useSetupRoad() {
        remainingRoads--;
    }

    public int getRemainingSettlements() {
        return remainingSettlements;
    }

    public int getRemainingCities() {
        return remainingCities;
    }

    public int getRemainingRoads() {
        return remainingRoads;
    }
}
