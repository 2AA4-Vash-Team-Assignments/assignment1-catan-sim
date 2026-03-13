package ca.mcmaster.se2aa4.catan;

/**
 * A human-controlled player that reads commands from the console.
 * Implements R2.1: human input via command line with regex parsing.
 * Uses HumanInputReader and CommandParser (DIP: depends on interface, not Scanner).
 */
public class HumanPlayer extends Player {

    private final HumanInputReader inputReader;
    private final CommandParser commandParser;

    public HumanPlayer(int id, HumanInputReader inputReader, CommandParser commandParser) {
        super(id);
        this.inputReader = inputReader;
        this.commandParser = commandParser;
    }

    @Override
    public void takeTurn(CatanGame game) {
        boolean rolled = false;
        while (true) {
            System.out.print("P" + id + "> ");
            String line = inputReader.hasNextLine() ? inputReader.readLine() : "";
            ParsedCommand cmd = commandParser.parse(line);
            switch (cmd.getCommandType()) {
                case ROLL:
                    if (rolled) {
                        System.out.println("Already rolled this turn.");
                        break;
                    }
                    int diceRoll = game.rollDice();
                    rolled = true;
                    if (diceRoll == 7) {
                        game.handleRollSeven(this);
                    } else {
                        game.distributeResources(diceRoll);
                    }
                    break;
                case GO:
                    if (!rolled) {
                        System.out.println("Roll first.");
                        break;
                    }
                    game.updateLongestRoad();
                    return;
                case LIST:
                    listHand();
                    break;
                case BUILD_SETTLEMENT:
                    if (!rolled) { System.out.println("Roll first."); break; }
                    game.tryBuildSettlement(this, cmd.getNodeId());
                    break;
                case BUILD_CITY:
                    if (!rolled) { System.out.println("Roll first."); break; }
                    game.tryBuildCity(this, cmd.getNodeId());
                    break;
                case BUILD_ROAD:
                    if (!rolled) { System.out.println("Roll first."); break; }
                    game.tryBuildRoad(this, cmd.getFromNodeId(), cmd.getToNodeId());
                    break;
                default:
                    if (!line.isBlank()) {
                        System.out.println("Unknown command. Use: Roll, Go, List, Build settlement <id>, Build city <id>, Build road <from>,<to>");
                    }
            }
        }
    }

    private void listHand() {
        StringBuilder sb = new StringBuilder();
        for (ResourceType t : ResourceType.values()) {
            int c = getResourceCount(t);
            if (c > 0) sb.append(t).append("=").append(c).append(" ");
        }
        System.out.println(sb.length() > 0 ? sb.toString().trim() : "No resources");
    }

    public HumanInputReader getInputReader() {
        return inputReader;
    }

    public CommandParser getCommandParser() {
        return commandParser;
    }
}
