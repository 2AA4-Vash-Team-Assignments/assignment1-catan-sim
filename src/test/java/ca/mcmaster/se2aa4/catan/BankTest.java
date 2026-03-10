package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests bank resource supply management
 * Covers initial supply, distribution, collection, and boundary (19 card supply
 * cap).
 * 
 * @author Vaishnav Yandrapalli 400572601
 */
class BankTest {

    private Bank bank;

    @BeforeEach
    void setUp() {
        bank = new Bank();
    }

    @Test
    void testBankInitialSupply() {
        // each resource type starts with exactly 19 cards in the bank
        for (ResourceType type : ResourceType.values()) {
            assertEquals(19, bank.getRemainingCount(type),
                    "Bank should start with 19 of " + type);
        }
    }

    @Test
    void testHasEnoughResources_sufficient() {
        assertTrue(bank.hasEnoughResources(ResourceType.WOOD, 19));
    }

    @Test
    void testDistributeResource_reducesSupply() {
        bank.distributeResource(ResourceType.BRICK, 5);
        assertEquals(14, bank.getRemainingCount(ResourceType.BRICK));
    }

    @Test
    void testCollectResource_increasesSupply() {
        bank.distributeResource(ResourceType.WHEAT, 10);
        bank.collectResource(ResourceType.WHEAT, 3);
        assertEquals(12, bank.getRemainingCount(ResourceType.WHEAT));
    }

    // Boundary test

    @Test
    void testHasEnoughResources_boundary_exactlyAvailable() {
        // BOUNDARY: requesting the full supply (19) -> should be true
        assertTrue(bank.hasEnoughResources(ResourceType.ORE, 19),
                "Requesting 19 from a bank of 19 should be sufficient");
    }

    @Test
    void testHasEnoughResources_boundary_oneTooMany() {
        // BOUNDARY: requesting 20 from a bank of 19 -> should be false
        assertFalse(bank.hasEnoughResources(ResourceType.SHEEP, 20),
                "Requesting 20 from a bank of 19 can not be sufficient");
    }
}
