package ca.mcmaster.se2aa4.catan;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * CatanTestSuite — groups all unit test classes into single, runnable suite.
 * Test strategy:
 * - BankTest: Basic + Boundary (supply = 19 cards)
 * - PlayerTest: Basic + Partition (hand <= 7 vs > 7 per R1.8) + Boundary
 * (exactly 7)
 * - BuildingTest: Basic (VP and multiplier values, upgrade path)
 * - NodeTest: Basic + Distance rule (R1.6)
 * - BoardTest: Basic + structural assertions post-initialize()
 * - CatanGameTest: Boundary (9 VP vs 10 VP win threshold) + resource
 * distribution
 */
@Suite
@SelectClasses({
        BankTest.class,
        PlayerTest.class,
        BuildingTest.class,
        NodeTest.class,
        BoardTest.class,
        CatanGameTest.class
})
public class CatanTestSuite {
    // class intentionally left empty.
    // JUnit platform runs all @Test methods in the listed classes.
}
