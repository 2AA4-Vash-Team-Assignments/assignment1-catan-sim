# Assignment 1 — Catan Simulator

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=2AA4-Vash-Team-Assignments_assignment1-catan-sim&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=2AA4-Vash-Team-Assignments_assignment1-catan-sim)

SFWRENG 2AA4 — Software Design I (McMaster University, Winter 2026)

## Overview

This project implements a simplified Settlers of Catan board game simulator. Four randomly-acting agents play on a standard 19-tile hex board, collecting resources, building roads, settlements, and cities, and competing to be the first to reach 10 victory points.

The simulator follows the official Catan rulebook with the following exclusions (as specified in the assignment): harbours, domestic/maritime trade, development cards, and the robber mechanic. When a 7 is rolled, the game simply continues without producing resources.

## Requirements

- Java 21 (JDK)
- Apache Maven 3.9+

## Building and Running

Compile the project:
```bash
mvn compile
```

Run the simulator with default settings (50 rounds):
```bash
mvn exec:java
```

Run with a configuration file (e.g. `config.txt` with `turns=100`):
```bash
mvn exec:java -Dexec.args="config.txt"
```
On Windows PowerShell, use: `mvn exec:java "-Dexec.args=config.txt"`

### Configuration

The simulator accepts a configuration file with the following format:
```
turns=100
```
The `turns` parameter accepts values from 1 to 8192. If no configuration file is provided, the simulator defaults to 50 rounds.

## Project Structure

```
assignment1-catan-sim/
├── src/main/java/ca/mcmaster/se2aa4/catan/   # Java source code
│   ├── CatanGame.java          # Main game loop and orchestration
│   ├── Board.java              # Hex board topology (19 tiles, 54 nodes, 72 edges)
│   ├── Tile.java               # Hexagonal land tile
│   ├── Node.java               # Intersection (settlement/city location)
│   ├── Edge.java               # Path between nodes (road location)
│   ├── Player.java             # Agent with resources and build logic
│   ├── Building.java           # Settlement or city
│   ├── Road.java               # Road placed on an edge
│   ├── Bank.java               # Resource supply manager
│   ├── Dice.java               # Two six-sided dice
│   ├── Configuration.java      # Config file parser
│   ├── Demonstrator.java       # Entry point (static void main)
│   ├── ResourceType.java       # Enum: WOOD, BRICK, WHEAT, ORE, SHEEP
│   └── BuildingType.java       # Enum: SETTLEMENT, CITY
├── model/                      # UML domain model (Mermaid source + PNG)
├── genai/                      # GenAI prompt and output documentation (Task 3)
├── pom.xml                     # Maven build configuration
└── README.md
```

## Output Format

The simulator prints actions in the following format:
```
[RoundNumber] / P[PlayerID]: [Action]
```

Victory points are printed at the end of each round and a summary is displayed when the game ends.
