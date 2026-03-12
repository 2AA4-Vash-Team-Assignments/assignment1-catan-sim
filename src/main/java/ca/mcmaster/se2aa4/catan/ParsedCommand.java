package ca.mcmaster.se2aa4.catan;

public class ParsedCommand {

    private final CommandType commandType;
    private final int nodeId;
    private final int fromNodeId;
    private final int toNodeId;

    public ParsedCommand(CommandType commandType) {
        this(commandType, -1, -1, -1);
    }

    public ParsedCommand(CommandType commandType, int nodeId) {
        this(commandType, nodeId, -1, -1);
    }

    public ParsedCommand(CommandType commandType, int fromNodeId, int toNodeId) {
        this(commandType, -1, fromNodeId, toNodeId);
    }

    public ParsedCommand(CommandType commandType, int nodeId, int fromNodeId, int toNodeId) {
        this.commandType = commandType;
        this.nodeId = nodeId;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getFromNodeId() {
        return fromNodeId;
    }

    public int getToNodeId() {
        return toNodeId;
    }
}
