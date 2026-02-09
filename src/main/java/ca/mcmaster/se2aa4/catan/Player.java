package ca.mcmaster.se2aa4.catan;

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

    public int getVictoryPoints() {
        // VP will be calculated by iterating over buildings on the board
        // This is a placeholder; CatanGame tracks this via board state
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
        resources.put(type, resources.get(type) - amount);
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

    public void buildRoad(Edge edge) {
        removeResource(ResourceType.BRICK, 1);
        removeResource(ResourceType.WOOD, 1);
        Road road = new Road(this, edge);
        edge.setRoad(road);
        remainingRoads--;
    }

    public void buildSettlement(Node node) {
        removeResource(ResourceType.BRICK, 1);
        removeResource(ResourceType.WOOD, 1);
        removeResource(ResourceType.WHEAT, 1);
        removeResource(ResourceType.SHEEP, 1);
        Building settlement = new Building(BuildingType.SETTLEMENT, this);
        node.setBuilding(settlement);
        remainingSettlements--;
    }

    public void buildCity(Node node) {
        removeResource(ResourceType.WHEAT, 2);
        removeResource(ResourceType.ORE, 3);
        node.getBuilding().setType(BuildingType.CITY);
        remainingCities--;
        remainingSettlements++;
    }

    public void chooseRandomAction(Board board) {
        boolean acted = true;
        while (acted) {
            acted = false;

            if (canBuildCity()) {
                List<Node> upgradeable = board.getUpgradeableNodes(this);
                if (!upgradeable.isEmpty()) {
                    Node chosen = upgradeable.get(random.nextInt(upgradeable.size()));
                    buildCity(chosen);
                    System.out.println("P" + id + ": Built city at node " + chosen.getId());
                    acted = true;
                    continue;
                }
            }

            if (canBuildSettlement()) {
                List<Node> available = board.getAvailableSettlementNodes(this);
                if (!available.isEmpty()) {
                    Node chosen = available.get(random.nextInt(available.size()));
                    buildSettlement(chosen);
                    System.out.println("P" + id + ": Built settlement at node " + chosen.getId());
                    acted = true;
                    continue;
                }
            }

            if (canBuildRoad()) {
                List<Edge> available = board.getAvailableRoadEdges(this);
                if (!available.isEmpty()) {
                    Edge chosen = available.get(random.nextInt(available.size()));
                    buildRoad(chosen);
                    List<Node> endpoints = chosen.getEndpoints();
                    System.out.println("P" + id + ": Built road between nodes "
                            + endpoints.get(0).getId() + " and " + endpoints.get(1).getId());
                    acted = true;
                    continue;
                }
            }
        }
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
