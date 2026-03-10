package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Building class (SETTLEMENT and CITY).
 * Covers VP values, resource multipliers, and type mutation.
 * 
 * @author Vaishnav Yandrapalli 400572601
 */
class BuildingTest {

    @Test
    void testSettlementVictoryPoints() {
        Player player = new Player(1);
        Building settlement = new Building(BuildingType.SETTLEMENT, player);
        assertEquals(1, settlement.getVictoryPoints(),
                "A settlement should be worth exactly 1 VP");
    }

    @Test
    void testCityVictoryPoints() {
        Player player = new Player(1);
        Building city = new Building(BuildingType.CITY, player);
        assertEquals(2, city.getVictoryPoints(),
                "A city should be worth exactly 2 VPs");
    }

    @Test
    void testSettlementResourceMultiplier() {
        Player player = new Player(1);
        Building settlement = new Building(BuildingType.SETTLEMENT, player);
        assertEquals(1, settlement.getResourceMultiplier(),
                "Settlement should produce 1 resource per roll");
    }

    @Test
    void testCityResourceMultiplier() {
        Player player = new Player(1);
        Building city = new Building(BuildingType.CITY, player);
        assertEquals(2, city.getResourceMultiplier(),
                "City should produce 2 resources per roll");
    }

    @Test
    void testUpgradeSettlementToCity_changesVP() {
        // simulate upgrade path: settlement -> city thru setType
        Player player = new Player(1);
        Building building = new Building(BuildingType.SETTLEMENT, player);
        assertEquals(1, building.getVictoryPoints());// before upgrading

        building.setType(BuildingType.CITY);
        assertEquals(2, building.getVictoryPoints());// after
    }

    @Test
    void testBuildingOwner() {
        Player player = new Player(3);
        Building building = new Building(BuildingType.SETTLEMENT, player);
        assertEquals(player, building.getOwner(),
                "Building owner should be the player it was constructed for");
    }
}
