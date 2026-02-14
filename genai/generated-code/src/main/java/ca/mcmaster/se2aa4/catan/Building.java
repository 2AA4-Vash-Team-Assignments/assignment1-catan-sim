package ca.mcmaster.se2aa4.catan;

public class Building {

    private BuildingType type;
    private final Player owner;

    public Building(BuildingType type, Player owner) {
        this.type = type;
        this.owner = owner;
    }

    public BuildingType getType() {
        return type;
    }

    public void setType(BuildingType type) {
        this.type = type;
    }

    public Player getOwner() {
        return owner;
    }

    public int getVictoryPoints() {
        return (type == BuildingType.CITY) ? 2 : 1;
    }

    public int getResourceMultiplier() {
        return (type == BuildingType.CITY) ? 2 : 1;
    }
}
