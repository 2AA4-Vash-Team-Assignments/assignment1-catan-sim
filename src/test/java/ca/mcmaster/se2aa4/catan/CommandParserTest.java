package ca.mcmaster.se2aa4.catan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    @Test
    void parseRoll_lowercase() {
        ParsedCommand cmd = parser.parse("roll");
        assertEquals(CommandType.ROLL, cmd.getCommandType());
    }

    @Test
    void parseRoll_uppercase() {
        ParsedCommand cmd = parser.parse("ROLL");
        assertEquals(CommandType.ROLL, cmd.getCommandType());
    }

    @Test
    void parseRoll_withSpaces() {
        ParsedCommand cmd = parser.parse("  Roll  ");
        assertEquals(CommandType.ROLL, cmd.getCommandType());
    }

    @Test
    void parseGo() {
        assertEquals(CommandType.GO, parser.parse("go").getCommandType());
        assertEquals(CommandType.GO, parser.parse("  Go  ").getCommandType());
    }

    @Test
    void parseList() {
        assertEquals(CommandType.LIST, parser.parse("list").getCommandType());
        assertEquals(CommandType.LIST, parser.parse("List").getCommandType());
    }

    @Test
    void parseBuildSettlement() {
        ParsedCommand cmd = parser.parse("Build settlement 12");
        assertEquals(CommandType.BUILD_SETTLEMENT, cmd.getCommandType());
        assertEquals(12, cmd.getNodeId());
    }

    @Test
    void parseBuildSettlement_withSpaces() {
        ParsedCommand cmd = parser.parse("  build   settlement   0  ");
        assertEquals(CommandType.BUILD_SETTLEMENT, cmd.getCommandType());
        assertEquals(0, cmd.getNodeId());
    }

    @Test
    void parseBuildCity() {
        ParsedCommand cmd = parser.parse("Build city 5");
        assertEquals(CommandType.BUILD_CITY, cmd.getCommandType());
        assertEquals(5, cmd.getNodeId());
    }

    @Test
    void parseBuildRoad() {
        ParsedCommand cmd = parser.parse("Build road 3,7");
        assertEquals(CommandType.BUILD_ROAD, cmd.getCommandType());
        assertEquals(3, cmd.getFromNodeId());
        assertEquals(7, cmd.getToNodeId());
    }

    @Test
    void parseBuildRoad_withSpaceAfterComma() {
        ParsedCommand cmd = parser.parse("build road 10, 20");
        assertEquals(CommandType.BUILD_ROAD, cmd.getCommandType());
        assertEquals(10, cmd.getFromNodeId());
        assertEquals(20, cmd.getToNodeId());
    }

    @Test
    void parseUnknown_returnsUnknown() {
        assertEquals(CommandType.UNKNOWN, parser.parse("foo").getCommandType());
        assertEquals(CommandType.UNKNOWN, parser.parse("build knight").getCommandType());
    }

    @Test
    void parseNullOrBlank_returnsUnknown() {
        assertEquals(CommandType.UNKNOWN, parser.parse(null).getCommandType());
        assertEquals(CommandType.UNKNOWN, parser.parse("").getCommandType());
        assertEquals(CommandType.UNKNOWN, parser.parse("   ").getCommandType());
    }
}
