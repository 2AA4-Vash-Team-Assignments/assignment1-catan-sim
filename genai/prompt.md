# Prompt Used for Task 3: Generative AI Code Generation

## Input Given to the AI

The following was provided to Claude Opus 4.5:

1. **The UML class diagram** — provided as the Mermaid source file (`catan-domain-model.mmd`), which is a semi-structured textual representation of the domain model.
2. **The assignment specification** — the PDF describing requirements R1.1 through R1.9.

## Exact Prompt

> Here is my UML class diagram for a Settlers of Catan board game simulator, written in Mermaid syntax. I've also attached the assignment specification PDF that describes the requirements.
>
> Please generate complete Java source code that implements all of the classes, enums, attributes, methods, and relationships shown in this class diagram. The code should:
>
> - Use the package `ca.mcmaster.se2aa4.catan`
> - Include a Maven `pom.xml` so it compiles with `mvn compile`
> - Implement the actual game logic where possible (dice rolling, resource distribution, building, etc.) based on the Catan rules described in the assignment spec
> - Follow standard Java conventions
> - Not be over-engineered — keep it clean and straightforward
>
> The domain model includes: ResourceType (enum), BuildingType (enum), CatanGame, Configuration, Dice, Bank, Demonstrator, Board, Tile, Node, Edge, Building, Road, and Player.

## Tool and Hyperparameters

- **Tool:** Claude Opus 4.5 (Anthropic)
- **Interface:** Claude chat (claude.ai)
- **Temperature:** Default (not modified)
- **System prompt:** None (default Claude behaviour)
- **Additional context:** No few-shot examples or custom instructions were provided beyond the prompt above
- **Number of iterations:** Single prompt — the code was generated in one pass without follow-up corrections

## Output

The AI generated 14 Java source files and a `pom.xml`, all placed under `src/main/java/ca/mcmaster/se2aa4/catan/`. The generated code compiles and runs without modification. See the `generated-output.md` file for the full list of generated files.
