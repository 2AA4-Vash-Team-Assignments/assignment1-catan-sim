package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * tests node occupancy and distance rule
 * ^a node cannot be occupied if any adjacent node is already occupied
 * 
 * @author Vaishnav Yandrapalli 400572601
 */
class NodeTest {

    private Node node;

    @BeforeEach
    void setUp() {
        node = new Node(0);
    }

    @Test
    void testNewNodeIsNotOccupied() {
        assertFalse(node.isOccupied(),
                "A freshly created node should have no building on it");
    }

    @Test
    void testSetBuilding_marksNodeOccupied() {
        Player player = new Player(1);
        Building settlement = new Building(BuildingType.SETTLEMENT, player);
        node.setBuilding(settlement);
        assertTrue(node.isOccupied());
    }

    @Test
    void testDistanceRule_noNeighbours_passes() {
        // node with no adjacent nodes satisfies the distance rule
        assertTrue(node.satisfiesDistanceRule(),
                "An isolated node with no neighbours should pass the distance rule");
    }

    @Test
    void testDistanceRule_occupiedNeighbour_fails() {
        // place settlement on an adjacent node -> distance rule must be violated
        Node neighbour = new Node(1);
        node.addAdjacentNode(neighbour);

        Player player = new Player(2);
        Building existing = new Building(BuildingType.SETTLEMENT, player);
        neighbour.setBuilding(existing);

        assertFalse(node.satisfiesDistanceRule(),
                "Node adjacent to an occupied node should FAIL the distance rule.");
    }

    @Test
    void testDistanceRule_emptyNeighbour_passes() {
        // adjacent node exists but is unoccupied -> distance rule should pass
        Node neighbour = new Node(2);
        node.addAdjacentNode(neighbour);
        // neighbour has no building set

        assertTrue(node.satisfiesDistanceRule(),
                "Node adjacent to an unoccupied node should PASS the distance rule.");
    }
}
