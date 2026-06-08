# DungeonCrawlerTCSS360

A Java Swing dungeon crawler built for TCSS 360. The game presents a terminal-inspired GUI where the player creates a hero, explores a generated dungeon, fights monsters, collects the four pillars of object-oriented programming, and escapes through the exit.

Authors: Travis Vinay, Pavlo Puzik, Andrew DeFord.

## Features

- Hero selection with Warrior, Thief, and Priestess classes.
- Random 10x10 dungeon generation with rooms, doors, pits, potions, monsters, randomized entrance, and exit.
- Four collectible pillars: abstraction, encapsulation, inheritance, and polymorphism.
- MVC-oriented package layout separating game state, controller/session flow, and Swing presentation.
- Movement through directional buttons, arrow keys, or WASD, with green/red direction-button feedback.
- Turn-based battle flow with hero status, monster status, battle log, sprite panel, attack, special skill, healing potion, and run/return actions.
- Vision potions reveal nearby dungeon rooms from the map screen.
- Stronger guardian monsters are placed near pillar rooms and the exit.
- Animated custom combat sprites for hero and monster battle poses.
- Sound effects for movement, menu buttons, attacks, pillar pickup, battle victory, and defeat.
- Combat cooldowns for special skills and potion actions, shown with custom number badges.
- Dungeon map rendering with discovered-room visibility.
- Win-state UI that shows "You Win!" and leaves New Game as the only action after escaping with all four pillars.
- Save and load support through SQLite, with a serialized file fallback.
- SQLite-backed monster data seeded automatically at runtime.

## Project Structure

```text
src/dungeoncrawler/
  audio/         Sound-effect loading and playback
  combat/        Battle logic
  controller/    Application entry point and game session coordination
  model/         Dungeon, room, item, pillar, and character domain classes
  persistence/   SQLite database and fallback save managers
  view/          Swing terminal window, combat sprites, map renderer, and scaling helpers
lib/             Bundled third-party jars
assets/sprites/  Combat sprite sheets and source credits
assets/sound-effects/
                 Sound effect WAV files and source credits
```

The main class is:

```text
dungeoncrawler.controller.DungeonCrawler
```

## MVC Design

The project is organized around the MVC design pattern.

- Model: `dungeoncrawler.model`, `dungeoncrawler.model.characters`, and `dungeoncrawler.combat` hold dungeon state, rooms, items, heroes, monsters, and battle rules.
- View: `dungeoncrawler.view` owns the Swing screens, map rendering, combat sprite display, and visual feedback.
- Controller: `dungeoncrawler.controller` starts the application and coordinates player actions through `GameSession` and `DungeonCrawler`.
- Persistence and audio are supporting services kept outside the view/model core.

## Requirements

- JDK 21.
- IntelliJ IDEA, or another Java IDE that can include local jar dependencies.
- SQLite JDBC jar included at `lib/sqlite-jdbc-3.53.1.0.jar`.

No external database server is required. The application creates `dungeon.db` in the project root when the SQLite managers initialize.

## Running in IntelliJ

1. Install a JDK 21 distribution.
2. Open the repository folder in IntelliJ.
3. Use the committed `DungeonCrawlerTCSS360.iml` module file and the `21` project SDK.
4. Run the `DungeonCrawler` application configuration.

The committed run configuration uses:

```text
Main class: dungeoncrawler.controller.DungeonCrawler
VM options: --enable-native-access=ALL-UNNAMED
Module: DungeonCrawlerTCSS360
```

The project metadata sets the language level and bytecode target to Java 21. The module file marks `src/` as the source root and includes `lib/sqlite-jdbc-3.53.1.0.jar` as a module library, so the SQLite driver is available when running from IntelliJ.

## Running from the Command Line

The Makefile is the simplest path on macOS/Linux:

```bash
make run
```

Useful targets:

```bash
make compile
make run
make clean
```

On Windows, use the batch script from Command Prompt or PowerShell:

```bat
build.bat run
```

Useful Windows targets:

```bat
build.bat compile
build.bat run
build.bat clean
```

Manual compile and run commands are below for environments without `make`.

Compile:

```bash
mkdir -p out/production/DungeonCrawlerTCSS360
javac --release 21 -cp lib/sqlite-jdbc-3.53.1.0.jar -d out/production/DungeonCrawlerTCSS360 $(find src -name '*.java')
```

Run:

```bash
java --enable-native-access=ALL-UNNAMED \
  -cp out/production/DungeonCrawlerTCSS360:lib/sqlite-jdbc-3.53.1.0.jar \
  dungeoncrawler.controller.DungeonCrawler
```

On Windows, use `;` instead of `:` between classpath entries.

## Gameplay

Start a new game, choose a hero class, and enter a name. Move with the directional buttons, arrow keys, or WASD. After each movement input, the direction button border turns green for a valid move and red for a blocked direction.

Each room can contain hazards, potions, a monster, or one of the four pillars. Potions and pillars are picked up automatically. Pits deal random damage when entered. Monster rooms open the battle screen. Collect every pillar, survive battles, then reach the exit room to win. When the hero reaches the exit with all four pillars, the dungeon screen displays "You Win!" and the only remaining action is New Game.

The hidden test option is Ctrl+Shift+D from the dungeon screen, or `debug dungeon` from the terminal after a session exists, which displays the full dungeon map.

Saves can be created from the dungeon view and loaded from the main menu. SQLite saves are stored in `dungeon.db`. If SQLite saving is unavailable, fallback saves are written under `saves/`.

## Room Symbols

The text map and room view use the following symbols:

| Symbol | Meaning |
| --- | --- |
| `i` | Entrance |
| `O` | Exit |
| `X` | Pit |
| `M` | Monster |
| `H` | Healing potion |
| `V` | Vision potion |
| `A` | Abstraction pillar |
| `E` | Encapsulation pillar |
| `I` | Inheritance pillar |
| `P` | Polymorphism pillar |
| space | Empty room |

If a room contains multiple visible contents, the room representation may show `M` for multiple items.

## Battle Screen

Entering a room with a monster opens the battle UI. The battle screen keeps the same Swing application frame and shows:

- Hero status panel.
- Monster status panel.
- Combat sprite panel.
- Battle log panel for turn messages.
- Action buttons for Attack, Special, Healing Potion, and Run/Return.

Hero attack speed can produce multiple hero attacks in a turn. The monster responds once per completed player action. Run exits the battle and returns to dungeon exploration.

## Combat Sprites

Combat uses `assets/sprites/combat_sprites.png`, a 6 row by 4 column transparent sprite sheet. Rows map to Warrior, Thief, Priestess, Skeleton, Ogre, and Gremlin. Columns map to idle, attack, hit, and defeated poses. The Swing combat view mirrors monster sprites at render time so enemies face the hero consistently.

Cooldown numbers are rendered by `dungeoncrawler.view.CooldownSpriteIcon` as custom pixel-style button badges. The combat model exposes cooldown state through `Battle`, and the view owns the visual presentation.

The combat sprites are not AI-generated. They are derived from CC0 source art listed in `assets/sprites/README.md` and `assets/sprites/SPRITE_SOURCES.txt`.

## Sound Effects

Sound effects live under `assets/sound-effects/`. They are derived from CC0 Kenney audio packs and are documented in `assets/sound-effects/ASSET_SOURCES.txt`.

## Testing

The Makefile includes a JUnit test target on macOS/Linux:

```bash
make test
```

The test suite uses the bundled JUnit, Mockito, Byte Buddy, Objenesis, and SQLite jars in `lib/`.

## Generated Files

The following files are runtime or IDE output:

- `dungeon.db`
- `saves/`
- `out/`
- IntelliJ workspace-local files such as `.idea/workspace.xml`

The SQLite JDBC jar in `lib/` is intentionally tracked because the project depends on it at runtime.
