package ca.mcmaster.se2aa4.catan;

import java.util.EnumMap;
import java.util.Map;

public class Bank {

    private final Map<ResourceType, Integer> supply;

    public Bank() {
        supply = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            supply.put(type, 19);
        }
    }

    public boolean hasEnoughResources(ResourceType type, int amount) {
        return supply.get(type) >= amount;
    }

    public void distributeResource(ResourceType type, int amount) {
        int current = supply.get(type);
        int toGive = Math.min(amount, current);
        supply.put(type, current - toGive);
    }

    public void collectResource(ResourceType type, int amount) {
        supply.put(type, supply.get(type) + amount);
    }

    public int getRemainingCount(ResourceType type) {
        return supply.get(type);
    }
}
