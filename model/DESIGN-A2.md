# Assignment 2 – Design Documentation

## 1. Domain model (UML)

The extended domain model for A2 is in **`catan-domain-model-a2.mmd`**.

### 1.1 Main structural changes from A1

| Change | Purpose |
|--------|--------|
| **Player hierarchy** | `Player` is now abstract; `AgentPlayer` and `HumanPlayer` extend it. The game calls `takeTurn(game)` polymorphically so human vs agent behaviour is encapsulated (OCP: open for extension without modifying `CatanGame`). |
| **HumanInputReader** | Interface for reading input; `ConsoleInputReader` implements it. Allows testing with a stub and satisfies R2.1 (read human input from command line). |
| **CommandParser** | Parses human commands using **regular expressions** (R2.1). Produces `ParsedCommand` (command type + optional node/edge IDs). Supports: `Roll`, `Go`, `List`, `Build settlement N`, `Build city N`, `Build road N1 N2`. |
| **Robber** | New entity on the board. Holds reference to its current tile. R2.5: placed on a random (different) tile when 7 is rolled; one qualifying player gives a random card to the roller. |
| **TurnPhase** | Enumeration of 8 turn states (`AWAIT_ROLL`, `ROLL_DICE`, `ROBBER_DISCARD`, `ROBBER_PLACE`, `ROBBER_STEAL`, `POST_ROLL`, `BUILD_OR_TRADE`, `AWAIT_GO`). Drives what the game allows at each moment and supports R2.4 (step-forward: wait for "go"). |
| **GameStateWriter** | Writes roads, buildings, robber tile, round, and current player to a JSON file. R2.3 (external JSON for game state) and R2.2 (feeds the Catanatron-based visualizer). Includes a static node-ID translation table mapping internal Board IDs (0–53) to Catanatron visualizer IDs. |

### 1.2 SOLID and OO in the design

- **SRP:** CommandParser only parses; HumanInputReader only reads; GameStateWriter only writes JSON. Robber only holds placement and steal logic. Each class has a single reason to change.
- **OCP:** New player types (e.g. different AI strategies) are added by creating new subclasses of `Player`, not by modifying `CatanGame`. The polymorphic `takeTurn()` method is the single extension point.
- **LSP:** Any `Player` subclass (Agent or Human) can be used wherever the game expects a `Player`; both honour the `takeTurn` contract.
- **ISP:** `HumanInputReader` is a small interface (`readLine`, `hasNextLine`); clients don't depend on console internals like `Scanner`.
- **DIP:** `HumanPlayer` depends on the `HumanInputReader` interface, not on `Scanner` or console directly. This enables testing with stubs.

---

## 2. Agent turn automaton

The turn automaton is in **`agent-turn-automaton.mmd`** (Mermaid `stateDiagram-v2`).

### 2.1 States and allowed actions

| State | Allowed actions / events |
|-------|---------------------------|
| **AWAIT_ROLL** | Human: type `Roll`. Agent: roll automatically. |
| **ROLL_DICE** | (Transition only.) Dice result: 2–6 or 8–12 → POST_ROLL; 7 → ROBBER_DISCARD. |
| **ROBBER_DISCARD** | Each player with >7 cards discards half (round down). No build allowed. |
| **ROBBER_PLACE** | Robber is placed on a random tile (must differ from current tile). |
| **ROBBER_STEAL** | One qualifying player (building adjacent to robber tile) gives one random resource card to the roller. |
| **POST_ROLL** | Resources distributed for the rolled number (tiles blocked by robber produce nothing). Transition to BUILD_OR_TRADE. |
| **BUILD_OR_TRADE** | Build (settlement / city / road) or list hand. Multiple builds allowed per turn. |
| **AWAIT_GO** | Only action: receive `Go` (R2.4 step-forward). Game blocks here until "go" then advances to next player. |

### 2.2 Why use an automaton

- **Clarity:** One place that defines which actions are legal in which phase (e.g. no build before roll, no roll after roll).
- **Consistency:** Same states for human and agent; only the way the transition is triggered differs (input vs automatic).
- **Implementation:** The Java code holds a `TurnPhase currentTurnPhase` field and only allows operations valid in the current phase; the automaton is the specification.

### 2.3 From automaton to code

- **State variable:** `TurnPhase currentTurnPhase` in `CatanGame`, initialized to `AWAIT_ROLL` at the start of each turn.
- **Transitions:** `rollDice()` → `ROLL_DICE`; `handleRollSeven()` → `ROBBER_DISCARD` → `ROBBER_PLACE` → `ROBBER_STEAL` → `POST_ROLL`; `distributeResources()` → `POST_ROLL` → `BUILD_OR_TRADE`; end of build phase → `AWAIT_GO`; on "go" → next player and reset to `AWAIT_ROLL`.
- **Guards:** Before executing build, check `currentTurnPhase == BUILD_OR_TRADE`; before accepting "go", check `currentTurnPhase == AWAIT_GO`.
- **Human vs agent:** In `AWAIT_ROLL`, human's `takeTurn` waits for parsed `ROLL`; agent's `takeTurn` calls roll immediately. In `AWAIT_GO`, only the human path blocks on "go"; the agent path advances automatically.

---

## 3. Visualizer integration (R2.2, R2.3)

- **state.json** (instructor format): `roads: [{ a, b, owner }]`, `buildings: [{ node, owner, type }]`. Owner is a colour string (`RED`, `BLUE`, `ORANGE`, `WHITE`); we map player id 1–4 to these colours.
- **base_map.json**: Contains instructor's exact tile data with cube coordinates for all 19 tiles. Used by the Catanatron-based Python visualizer to render the hex board.
- **Node ID mapping:** Our internal `Board.java` node numbering (0–53) differs from the Catanatron visualizer's node numbering for approximately 20 of the 54 nodes. `GameStateWriter` contains a static `TO_VISUALIZER` translation array that maps each internal node ID to its corresponding visualizer ID when writing `state.json`. This approach avoids changing `Board.java` (which would break existing tests).
- **GameStateWriter** creates/updates the JSON file after state changes so the visualizer (single-render or watch mode) can display the current board.

---

## 4. File index

| File | Description |
|------|-------------|
| `catan-domain-model.mmd` | A1 domain model (unchanged reference). |
| `catan-domain-model-a2.mmd` | A2 extended domain model (player hierarchy, robber, JSON state, turn phase). |
| `agent-turn-automaton.mmd` | Turn automaton: states and transitions for one player's turn. |
| `DESIGN-A2.md` | This document. |
