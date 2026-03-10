package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Board after initialization.
 * Verifies structural correctness -> tile count, node count, edge count,
 * and desert tile id'ing
 * 
 * @author Vaishnav Yandrapalli 400572601
 */
class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
        board.initialize();
    }

    @Test
    void testBoardHasCorrectTileCount() {
        // std. Catan board: 19 tiles (1 centre + 6 inner + 12 outer)
        assertEquals(19, board.getTiles().size(),
                "Board must have 19 tiles.");
    }

    @Test
    void testBoardHasCorrectNodeCount() {
        assertEquals(54, board.getNodes().size(),
                "Board must have exactly 54 nodes");
    }

    @Test
    void testBoardHasNonZeroEdges() {
        // std. Catan board has 72 road edges
        assertEquals(72, board.getEdges().size(),
                "Board must have 72 edges.");
    }

    @Test
    void testDesertTile_hasNoResource() {
        // tile ID 16 is the desert in the hardwired layout — resourceType must be null
        Tile desert = board.getTiles().get(16);
        assertNull(desert.getResourceType(),
                "Desert tile (ID 16) must have null resource type");
    }

    @Test
    void testNonDesertTiles_haveResource() {
        // every tile that is not the desert must carry a non null resource
        for (Tile tile : board.getTiles()) {
            if (tile.getId() != 16) { // 16 is the desert
                assertNotNull(tile.getResourceType(),
                        "Tile " + tile.getId() + " should have a non-null resource");
            }
        }
    }

    @Test
    void testGetTilesForNumber_returnsCorrectTiles() {
        // number token 6 appears on tile IDs 8 and 10 in the hardwired layout
        List<Tile> tilesWithSix = board.getTilesForNumber(6);
        assertEquals(2, tilesWithSix.size(),
                "Exactly 2 tiles should have number token 6");
    }

    @Test
    void testGetTilesForNumber_sevenReturnsEmpty() {
        // 7 never placed as a number token on any tile
        List<Tile> tilesWithSeven = board.getTilesForNumber(7);
        assertTrue(tilesWithSeven.isEmpty(),
                "No tile should have number token 7 (it triggers the robber)");
    }

    @Test
    void testEachTileHasSixAdjacentNodes() {
        // every hexagonal tile must have exactly 6 corner nodes
        for (Tile tile : board.getTiles()) {
            assertEquals(6, tile.getAdjacentNodes().size(),
                    "Tile " + tile.getId() + " must have exactly 6 adjacent nodes");
        }
    }
}
