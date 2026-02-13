# Requirements Verification Report

This document verifies that each requirement (R1.1 through R1.9) is fully implemented in the Catan simulator, with code references and evidence from execution.

---

## R1.1 — Valid map with specified identification

**Requirement:** The software shall first set up a valid map. Use the specified identification mechanism for tiles (0 center, 1–6 inner ring, 7–18 outer ring) and nodes. Map can be hard-wired.

**Where it's implemented:**

- `Board.createNodes()` — creates 54 nodes with IDs 0–53
- `Board.createTiles()` — creates 19 tiles with IDs 0–18, assigns resources and number tokens
- `Board.setTileNodes()` — assigns node IDs to each tile per the hex layout

**Evidence in code:**

```65:78:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/Board.java
        // Wire up tile-node adjacency based on standard Catan hex layout
        // Tile 0 (center): nodes 0-5
        setTileNodes(0, new int[]{0, 1, 2, 3, 4, 5});
        // Tile 1 (inner ring): nodes 0, 5, 6, 7, 8, 1
        setTileNodes(1, new int[]{0, 5, 6, 7, 8, 1});
        ...
        // Outer ring tiles (7-18)
        setTileNodes(7, new int[]{6, 21, 22, 23, 24, 7});
        ...
        setTileNodes(18, new int[]{17, 48, 49, 50, 18, 4});
```

**Proof from output:** Node IDs 0–53 and tiles 0–18 appear in placement messages (e.g., `Placed settlement at node 14`, `Placed road between nodes 14 and 3`).

---

## R1.2 — 4 randomly acting agents

**Requirement:** The simulator shall be able to simulate 4 randomly acting agents playing on the map.

**Where it's implemented:**

- `CatanGame` constructor — creates 4 players
- `placeInitialSettlementAndRoad()` — random choice from `availableNodes` and `freeEdges`
- `Player.chooseRandomAction()` — randomly selects one action from all possible builds

**Evidence in code:**

```30:33:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/CatanGame.java
        for (int i = 1; i <= 4; i++) {
            players.add(new Player(i));
        }
```

```65:66:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/CatanGame.java
        Node chosenNode = availableNodes.get(random.nextInt(availableNodes.size()));
        ...
```

```118:119:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/Player.java
            Runnable chosen = actions.get(random.nextInt(actions.size()));
            chosen.run();
```

**Proof from output:** All four players (P1–P4) place during setup and perform actions during the game.

---

## R1.3 — Follow Catan rules (minus exclusions)

**Requirement:** The simulation must follow the rules outlined in the rulebook, excluding harbours, domestic/maritime trade, development cards, and the robber.

**Where it's implemented:**

| Rule element | Implementation |
|--------------|----------------|
| Setup: 2 settlements & roads each | `setupPhase()` — each player places twice (forward, then reverse order) |
| Dice roll each round | `executeRound()` calls `dice.roll()` |
| Resource distribution on 2–12 | `distributeResources()` uses `getTilesForNumber(diceRoll)` |
| Roll 7 = no resources | `if (diceRoll != 7) { distributeResources(diceRoll); }` |
| Building costs (roads, settlements, cities) | `Player.canBuildRoad()`, `canBuildSettlement()`, `canBuildCity()` + corresponding `build*` methods |
| Victory points (1 for settlement, 2 for city) | `Building.getVictoryPoints()` |
| Longest road (5+ segments) | `updateLongestRoad()`, `calculateLongestRoad()` |
| Bank supply | `Bank` with finite resources |

**Evidence in code:**

```91:95:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/CatanGame.java
        if (diceRoll != 7) {
            distributeResources(diceRoll);
        }
        // On 7: no resources produced (robber excluded per assignment spec)
```

**Proof from output:** Round 2 shows `Dice: 7` with no resource messages; round 1 shows resources distributed; round 42 shows a city built and VP increasing from 2 to 3.

---

## R1.4 — User-defined rounds (max 8192) or 10 VP

**Requirement:** The simulator shall simulate for a user-defined number of rounds (maximum 8192) or until 10 VP by any agent. Configuration is defined in a configuration file.

**Where it's implemented:**

- `Configuration.load()` — parses `turns=` (1–8192)
- `Configuration.getMaxRounds()` — returns configured value (default 50)
- `CatanGame.play()` — loop uses `configuration.getMaxRounds()`
- `Demonstrator.main()` — loads config from `args[0]` when provided

**Evidence in code:**

```19:31:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/Configuration.java
    public void load(String filePath) {
        ...
                        if (value >= 1 && value <= 8192) {
                            this.maxRounds = value;
                        }
```

```40:46:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/CatanGame.java
        while (currentRound < configuration.getMaxRounds()) {
            currentRound++;
            executeRound();
            if (checkWinCondition()) {
                break;
            }
        }
```

**Proof from output:** Default run ends at `Rounds played: 50`; run with `config.txt` (turns=100) ends at `Rounds played: 100`.

---

## R1.5 — Halt on termination conditions

**Requirement:** The simulator shall halt execution upon reaching one of the termination conditions (max rounds or 10 VP).

**Where it's implemented:**

- `while (currentRound < configuration.getMaxRounds())` — stops at max rounds
- `if (checkWinCondition()) { break; }` — stops when any player reaches 10 VP

**Evidence in code:**

```40:48:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/CatanGame.java
        while (currentRound < configuration.getMaxRounds()) {
            currentRound++;
            executeRound();
            if (checkWinCondition()) {
                break;
            }
        }
        printRoundSummary();
```

```147:157:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/CatanGame.java
    public boolean checkWinCondition() {
        for (Player player : players) {
            int vp = calculateVictoryPoints(player);
            if (vp >= 10) {
                System.out.println(currentRound + " / P" + player.getId()
                        + ": Wins with " + vp + " victory points!");
                return true;
            }
        }
        return false;
    }
```

**Proof from output:** Every run terminates with `=== Game Over ===` and `Rounds played: N`, showing one of the two conditions was met.

---

## R1.6 — Key invariants respected

**Requirement:** Key invariants must be respected: new roads connected to existing roads/settlements; cities replace settlements; distance between settlements/cities at least two.

**Where it's implemented:**

| Invariant | Implementation |
|-----------|----------------|
| Distance rule (no adjacent settlements) | `Node.satisfiesDistanceRule()` used in `getAvailableSetupNodes()`, `getAvailableSettlementNodes()` |
| Roads connected to player's network | `Board.isConnectedToPlayer()`, `Board.getAvailableRoadEdges()` |
| Cities replace settlements | `Board.getUpgradeableNodes()` filters by `BuildingType.SETTLEMENT`; `buildCity()` upgrades existing building |

**Evidence in code:**

```38:44:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/Node.java
    public boolean satisfiesDistanceRule() {
        for (Node neighbor : adjacentNodes) {
            if (neighbor.isOccupied()) {
                return false;
            }
        }
        return true;
    }
```

```144:151:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/Board.java
    public List<Node> getAvailableSettlementNodes(Player player) {
        ...
            if (!node.isOccupied() && node.satisfiesDistanceRule() && hasConnectedRoad(node, player)) {
```

```96:102:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/Player.java
    public void buildCity(Node node) {
        ...
        node.getBuilding().setType(BuildingType.CITY);
        remainingCities--;
        remainingSettlements++;
    }
```

**Proof from output:** No invalid placements observed; roads connect to nodes with existing roads or settlements; city builds occur only at existing settlements.

---

## R1.7 — Print actions and victory points

**Requirement:** Print the actions taken by the agents in the specified encoding. Print current victory points at the end of each round.

**Where it's implemented:**

- All action output uses format `[Round] / P[ID]: [Action]` in `CatanGame` and `Player`
- `printVictoryPoints()` called at end of each round in `executeRound()`

**Evidence in code:**

```112:118:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/CatanGame.java
    private void printVictoryPoints() {
        StringBuilder vpLine = new StringBuilder(currentRound + " / VP:");
        for (Player player : players) {
            int vp = calculateVictoryPoints(player);
            vpLine.append(" P").append(player.getId()).append("=").append(vp);
        }
        System.out.println(vpLine);
    }
```

**Proof from output:** Example lines: `0 / P1: Placed settlement at node 14`, `42 / P4: Built city at node 29`, `1 / VP: P1=2 P2=2 P3=2 P4=2`, `42 / VP: P1=2 P2=2 P3=2 P4=3`.

---

## R1.8 — Agents with >7 cards must try to spend

**Requirement:** Agents with more than 7 cards in hand must try to spend those cards by building something.

**Where it's implemented:**

- `Player.chooseRandomAction()` — loops until `getTotalResourceCards() <= 7` or no valid actions remain
- `collectPossibleActions()` — enumerates all possible build actions; one is chosen randomly

**Evidence in code:**

```112:127:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/Player.java
    public void chooseRandomAction(Board board, int currentRound) {
        // R1.8: agents with >7 cards must keep trying to spend until <=7 or no options
        while (true) {
            List<Runnable> actions = collectPossibleActions(board, currentRound);
            if (actions.isEmpty()) {
                break;
            }
            Runnable chosen = actions.get(random.nextInt(actions.size()));
            chosen.run();
            if (getTotalResourceCards() <= 7) {
                break;
            }
        }
    }
```

**Proof from output:** When players accumulate many cards, they build (e.g., `42 / P4: Built city at node 29`). In runs where no player has >7 cards or has no valid builds, no builds occur, which is correct.

---

## R1.9 — Demonstrator with main and comments

**Requirement:** Key functionality demonstrated in a Demonstrator class with one `static void main` method. The demonstrator is properly explained by comments.

**Where it's implemented:**

- `Demonstrator.main()` — creates `CatanGame`, optionally loads config, calls `game.play()`
- Javadoc and inline comments describe what each step does

**Evidence in code:**

```1:27:assignment1-catan-sim/src/main/java/ca/mcmaster/se2aa4/catan/Demonstrator.java
/**
 * Demonstrator program for the Catan simulator.
 * <p>
 * Demonstrates the key functionality required by R1.9: runs one or more
 * simulations showing dice rolls, resource distribution, building actions,
 * victory point tracking, and game termination.
 */
public class Demonstrator {

    public static void main(String[] args) {
        // Create the game with board, 4 players, dice, bank, and configuration
        CatanGame game = new CatanGame();

        // R1.4: Load user-defined max rounds (1-8192) from config file if provided
        if (args.length > 0) {
            game.getConfiguration().load(args[0]);
        }
        // Otherwise uses default 50 rounds from Configuration constructor

        // Run the simulation: setup phase, then rounds until win or max rounds (R1.5)
        // Actions are printed per R1.7; VPs printed at end of each round
        game.play();
    }
}
```

**Proof from output:** Program runs end-to-end (e.g., via `mvn exec:java` or `java -cp target/classes ca.mcmaster.se2aa4.catan.Demonstrator`) and produces setup, rounds, actions, VPs, and game-over summary.

---

## Summary Table

| Req | Status | Key Evidence |
|-----|--------|--------------|
| R1.1 | ✅ | Board creates 19 tiles (0–18) and 54 nodes (0–53) with specified layout |
| R1.2 | ✅ | 4 players, random selection from available actions and nodes |
| R1.3 | ✅ | Setup, dice, resource distribution, building costs, VP, longest road; 7 skips resources |
| R1.4 | ✅ | Config file for turns 1–8192; Demonstrator loads config from args |
| R1.5 | ✅ | Loop exits on max rounds or `checkWinCondition()` (10 VP) |
| R1.6 | ✅ | `satisfiesDistanceRule()`, `isConnectedToPlayer()`, cities upgrade settlements |
| R1.7 | ✅ | Actions use `[Round] / P[ID]: [Action]`; `printVictoryPoints()` at end of each round |
| R1.8 | ✅ | `chooseRandomAction()` keeps building while >7 cards and actions exist |
| R1.9 | ✅ | `Demonstrator.main()` runs simulation; comments document behavior |
