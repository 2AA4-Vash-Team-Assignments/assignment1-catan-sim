package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests player resource management, build eligibility, and the card hand-size
 * rules
 * PARTITION TESTING:
 * The class partitions hand size into two equivalence classes:
 * - Partition A: totalCards <= 7 -> player acts normally (w one optional build)
 * - Partition B: totalCards > 7 -> player is forced to keep spending until <= 7
 * It tests a representative from each partition plus the exact boundary value
 * 
 * @author Vaishnav Yandrapalli 400572601
 */

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(1);
    }

    @Test
    void testPlayerStartsWithNoResources() {
        // new player should have 0 total resource cards
        assertEquals(0, player.getTotalResourceCards());
    }

    @Test
    void testAddResource_incrementsCorrectly() {
        player.addResource(ResourceType.WOOD, 3);
        assertEquals(3, player.getResourceCount(ResourceType.WOOD));
    }

    @Test
    void testAddMultipleResourceTypes() {
        player.addResource(ResourceType.BRICK, 2);
        player.addResource(ResourceType.WHEAT, 1);
        assertEquals(3, player.getTotalResourceCards());
    }

    @Test
    void testRemoveResource_doesNotGoBelowZero() {
        // removeResource clamps to 0, removing more than available should not crash
        player.addResource(ResourceType.ORE, 1);
        player.removeResource(ResourceType.ORE, 5); // remove more than available
        assertEquals(0, player.getResourceCount(ResourceType.ORE));
    }

    // Partition Tests for hand size

    @Test
    void testHandSize_partitionA_belowThreshold() {
        // PARTITION A: 6 cards -> canBuildRoad check not forced
        // Player has BRICK + WOOD = 2 cards
        player.addResource(ResourceType.BRICK, 1);
        player.addResource(ResourceType.WOOD, 1);
        assertEquals(2, player.getTotalResourceCards());
        assertTrue(player.getTotalResourceCards() <= 7,
                "Partition A: hand <= 7, player should not be force spending");
    }

    @Test
    void testHandSize_partitionBoundary_exactlySeven() {
        // BOUNDARY between partitions: exactly 7 cards -> still NOT force spending
        player.addResource(ResourceType.WOOD, 7);
        assertEquals(7, player.getTotalResourceCards());
        assertTrue(player.getTotalResourceCards() <= 7,
                "Boundary: hand == 7, player should not be force spending");
    }

    @Test
    void testHandSize_partitionB_aboveThreshold() {
        // PARTITION B: 8 cards -> player is in the forced-spend zone
        player.addResource(ResourceType.WOOD, 8);
        assertEquals(8, player.getTotalResourceCards());
        assertTrue(player.getTotalResourceCards() > 7,
                "Partition B: hand > 7, player should be force-spending");
    }

    // build elig. tests

    @Test
    void testCanBuildRoad_withExactResources() {
        // road costs: 1 BRICK + 1 WOOD
        player.addResource(ResourceType.BRICK, 1);
        player.addResource(ResourceType.WOOD, 1);
        assertTrue(player.canBuildRoad());
    }

    @Test
    void testCanBuildSettlement_withExactResources() {
        // settlement costs: 1 BRICK + 1 WOOD + 1 WHEAT + 1 SHEEP
        player.addResource(ResourceType.BRICK, 1);
        player.addResource(ResourceType.WOOD, 1);
        player.addResource(ResourceType.WHEAT, 1);
        player.addResource(ResourceType.SHEEP, 1);
        assertTrue(player.canBuildSettlement());
    }

    @Test
    void testCanBuildCity_withExactResources() {
        // city costs: 2 WHEAT + 3 ORE; also requires remaining cities > 0
        player.addResource(ResourceType.WHEAT, 2);
        player.addResource(ResourceType.ORE, 3);
        assertTrue(player.canBuildCity());
    }

    @Test
    void testCannotBuildRoad_whenInsufficientResources() {
        // no resources -> cannot build anything
        assertFalse(player.canBuildRoad());
        assertFalse(player.canBuildSettlement());
        assertFalse(player.canBuildCity());
    }
}
