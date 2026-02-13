# Engineering Decisions and Invariants

## R1.6 — Implemented Invariants

The following game invariants are enforced by the simulator:

### 1. Settlement/City Placement — Distance Rule
**Invariant:** No two settlements or cities may be adjacent. Each node's neighbours (via edges) must be unoccupied when placing a new settlement or city.

**Implementation:** `Node.satisfiesDistanceRule()` checks that no adjacent node has a building. Used by `Board.getAvailableSetupNodes()`, `Board.getAvailableSettlementNodes()`, and setup placement.

### 2. Road Connection
**Invariant:** New roads must be connected to an existing road or settlement/city of the same player.

**Implementation:** `Board.getAvailableRoadEdges(Player)` returns only edges where at least one endpoint has the player's building or is connected to the player's road network via adjacent edges. `Board.isConnectedToPlayer(Edge, Player)` encapsulates this check.

### 3. City Replaces Settlement
**Invariant:** A city must replace an existing settlement of the same player.

**Implementation:** `Board.getUpgradeableNodes(Player)` returns only nodes occupied by that player's settlements. `Player.buildCity(Node)` calls `node.getBuilding().setType(BuildingType.CITY)` on the existing building.

### 4. Resource and Piece Limits
**Invariant:** Players cannot build without sufficient resources or without remaining pieces.

**Implementation:** `Player.canBuildRoad()`, `canBuildSettlement()`, and `canBuildCity()` check resource counts and `remainingRoads`, `remainingSettlements`, and `remainingCities`. The bank (`Bank.hasEnoughResources`) validates resource distribution.

### 5. Board Topology
**Invariant:** Tiles (0–18), nodes (0–53), and edges follow the specified Catan hex layout.

**Implementation:** `Board.createNodes()`, `createTiles()`, and `createEdges()` establish the fixed topology. Tile–node and node–edge associations are hard-wired in `Board.createTiles()` per the assignment specification.

---

## Engineering Decisions

### R1.8 — Agent Action Selection
**Decision:** Linear enumeration of all possible build actions, then random selection.

**Rationale:** The assignment requires "a simple linear check of all the actions that can be executed, and pick one randomly." We collect all executable actions (each city upgrade, settlement placement, and road placement as distinct options) into a list, then select one with uniform probability. Agents with >7 cards must keep building until ≤7 or no actions remain.

### Configuration Loading
**Decision:** Configuration is loaded from a file path passed as a command-line argument. Default 50 rounds when no file is provided.

**Rationale:** Matches R1.4 and the [Spec] for configuration. Keeps setup logic in `Configuration` and separates it from game logic.

### Output Encoding
**Decision:** Format `[RoundNumber] / P[PlayerID]: [Action]` for all actions. Victory points printed as `[RoundNumber] / VP: P1=X P2=Y P3=Z P4=W` at the end of each round.

**Rationale:** Consistent with R1.7 and the described output format. Round number prefixes allow tracing actions to rounds.

### Board as Single Source of Truth
**Decision:** Board holds tiles, nodes, and edges; placement validity is checked through Board methods.

**Rationale:** Centralizes topology and validity logic (SRP). Players request available options from Board; Board enforces invariants.

### Fixed Map
**Decision:** Map layout (tiles, numbers, resources) is fixed in `Board.createTiles()`.

**Rationale:** R1.1 allows a fixed setup for simpler testing and debugging. Map data is isolated in Board.
