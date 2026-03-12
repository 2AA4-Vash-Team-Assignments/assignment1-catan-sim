# Assignment 2 – Design Documentation

## 1. Domain model (UML)

The extended domain model for A2 is in **`catan-domain-model-a2.mmd`**.

### 1.1 Main structural changes from A1

| Change | Purpose |
|--------|--------|
| **Player hierarchy** | `Player` is abstract; `AgentPlayer` and `HumanPlayer` extend it. The game calls `takeTurn(game, board, bank)` so human vs agent behaviour is encapsulated (OCP: open for extension without modifying `CatanGame`). |
| **HumanInputReader** | Interface for reading input; `ConsoleInputReader` implements it. Allows testing with a stub and satisfies R2.1 (read human input from command line). |
| **CommandParser** | Parses human commands using **regular expressions** (R2.1 [Spec]). Produces `ParsedCommand` (command type + optional node/edge IDs). |
| **Robber** | New entity associated with the board. Holds reference to the tile it is on. R2.5: place on random tile on 7; one qualifying player gives a random card to the roller. |
| **TurnPhase** | Enumeration of turn states (e.g. `AWAIT_ROLL`, `POST_ROLL`, `ROBBER_DISCARD`, `ROBBER_PLACE`, `ROBBER_STEAL`, `BUILD_OR_TRADE`, `AWAIT_GO`). Drives what the game allows at each moment and supports R2.4 (step-forward: wait for "go"). |
| **GameStateSnapshot / GameStateWriter** | Snapshot of roads, buildings, robber tile, round, current player; writer persists it to a JSON file. R2.3 (external JSON for game state) and R2.2 (feeds visualizer). |
| **TradeOffer / BankTrade** | Introduction of trading: interface for executable trades; bank trade (e.g. 4:1) as one implementation. Domestic (player–player) trade can be added later. |

### 1.2 SOLID and OO in the design

- **SRP:** CommandParser only parses; HumanInputReader only reads; GameStateWriter only writes JSON. Robber only holds placement and steal logic.
- **OCP:** New player types (e.g. different AI) added by new subclasses of `Player`, not by changing `CatanGame`.
- **LSP:** Any `Player` (Agent or Human) can be used where the game expects a player; `takeTurn` is the single extension point.
- **ISP:** HumanInputReader is a small interface (readLine, hasNextLine); clients don’t depend on console details.
- **DIP:** CatanGame and HumanPlayer depend on the HumanInputReader interface, not on `Scanner` or console directly.

---

## 2. Agent turn automaton

The turn automaton is in **`agent-turn-automaton.mmd`** (Mermaid `stateDiagram-v2`).

### 2.1 States and allowed actions

| State | Allowed actions / events |
|-------|---------------------------|
| **AwaitRoll** | Human: type `Roll`. Agent: roll automatically. |
| **RollDice** | (Transition only.) Dice result: 2–6 or 8–12 → PostRoll; 7 → RobberDiscard. |
| **RobberDiscard** | Each player with >7 cards discards half (round down). No build/trade. |
| **RobberPlace** | Robber is placed on a (random) tile; no production on that tile. |
| **RobberSteal** | One qualifying player (settlement/city adjacent to robber tile) gives one random resource card to the player who rolled 7. |
| **PostRoll** | Resources distributed for rolled number (tiles not blocked by robber). Then transition to BuildOrTrade. |
| **BuildOrTrade** | Build (settlement / city / road), trade (bank or domestic), or list hand. Multiple builds/trades allowed. No roll. |
| **AwaitGo** | Only action: receive `Go` (R2.4 step-forward). Game blocks here until "go" then advances to next player. |

### 2.2 Why use an automaton

- **Clarity:** One place that defines which actions are legal in which phase (e.g. no build before roll, no roll after roll).
- **Consistency:** Same states for human and agent; only the way the transition is triggered differs (input vs automatic).
- **Implementation:** The Java code can hold a `TurnPhase` (or equivalent) and only allow operations that are valid in that phase; the automaton is the specification.

### 2.3 From automaton to code

- **State variable:** e.g. `TurnPhase currentTurnPhase` in `CatanGame` (or a small `TurnController`).
- **Transitions:** After roll → set phase to `ROBBER_*` or `POST_ROLL`; after robber steal → `POST_ROLL`; after distribution → `BUILD_OR_TRADE`; when human/agent is done → `AWAIT_GO`; on "go" → next player and reset to `AWAIT_ROLL`.
- **Guards:** Before executing build/trade, check `currentTurnPhase == BUILD_OR_TRADE`; before accepting "go", check `currentTurnPhase == AWAIT_GO`.
- **Human vs agent:** In `AwaitRoll`, human’s `takeTurn` waits for parsed `ROLL`; agent’s `takeTurn` calls roll immediately. In `AwaitGo`, only human path blocks on "go"; agent can auto-issue go or game advances after agent’s build phase.

---

## 3. Visualizer integration (R2.2, R2.3)

- **state.json** (instructor format): `roads: [{ a, b, owner }]`, `buildings: [{ node, owner, type }]`. Owner is a colour string (e.g. RED, BLUE); we map player id to colour.
- **base_map.json**: Instructor-provided; we use it as-is for the visualizer. Our internal board (tile 0–18, node 0–53) must match the visualizer’s expectations or we provide a mapping when writing `state.json`.
- **GameStateWriter** creates/updates the external JSON file after state changes so the visualizer (single-render or watch mode) can display the current board.

---

## 4. File index

| File | Description |
|------|-------------|
| `catan-domain-model.mmd` | A1 domain model (unchanged reference). |
| `catan-domain-model-a2.mmd` | A2 extended domain model (human, robber, JSON state, trading, turn phase). |
| `agent-turn-automaton.mmd` | Turn automaton: states and transitions for one player’s turn. |
| `DESIGN-A2.md` | This document. |
