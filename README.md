# DungeonCrawlerTCSS360

A Java Swing dungeon crawler built for TCSS 360. The game presents a terminal-inspired GUI where the player creates a hero, explores a generated dungeon, fights monsters, collects the four pillars of object-oriented programming, and escapes through the exit.

Authors: Travis Vinay, Pavlo Puzik, Andrew DeFord.

## Features

- Hero selection with Warrior, Thief, and Priestess classes.
- Random 10x10 dungeon generation with rooms, doors, pits, potions, monsters, entrance, and exit.
- Four collectible pillars: abstraction, encapsulation, inheritance, and polymorphism.
- Turn-based battle flow with attack, special skill, healing potion, and vision potion actions.
- Animated custom combat sprites for hero and monster battle poses.
- Combat cooldowns for special skills and potion actions, shown with custom number badges.
- Dungeon map rendering with discovered-room visibility.
- Save and load support through SQLite, with a serialized file fallback.
- SQLite-backed monster data seeded automatically at runtime.

## Project Structure

```text
src/dungeoncrawler/
  combat/        Battle logic
  controller/    Application entry point and game session coordination
  model/         Dungeon, room, item, pillar, and character domain classes
  persistence/   SQLite database and fallback save managers
  view/          Swing terminal window, combat sprites, map renderer, and scaling helpers
lib/             Bundled third-party jars
assets/sprites/  Generated combat sprite sheets
```

The main class is:

```text
dungeoncrawler.controller.DungeonCrawler
```

## Requirements

- JDK 17 or newer.
- IntelliJ IDEA, or another Java IDE that can include local jar dependencies.
- SQLite JDBC jar included at `lib/sqlite-jdbc-3.53.1.0.jar`.

No external database server is required. The application creates `dungeon.db` in the project root when the SQLite managers initialize.

## Running in IntelliJ

1. Open the repository folder in IntelliJ.
2. Use the committed `DungeonCrawlerTCSS360.iml` module file.
3. Run the `DungeonCrawler` application configuration.

The committed run configuration uses:

```text
Main class: dungeoncrawler.controller.DungeonCrawler
VM options: --enable-native-access=ALL-UNNAMED
Module: DungeonCrawlerTCSS360
```

The module file marks `src/` as the source root and includes `lib/sqlite-jdbc-3.53.1.0.jar` as a module library, so the SQLite driver is available when running from IntelliJ.

## Running from the Command Line

Compile:

```bash
mkdir -p out/production/DungeonCrawlerTCSS360
javac -cp lib/sqlite-jdbc-3.53.1.0.jar -d out/production/DungeonCrawlerTCSS360 $(find src -name '*.java')
```

Run:

```bash
java --enable-native-access=ALL-UNNAMED \
  -cp out/production/DungeonCrawlerTCSS360:lib/sqlite-jdbc-3.53.1.0.jar \
  dungeoncrawler.controller.DungeonCrawler
```

On Windows, use `;` instead of `:` between classpath entries.

## Gameplay

Start a new game, choose a hero class, and enter a name. Move with the directional buttons or keyboard movement bindings. Each room can contain hazards, potions, a monster, or one of the four pillars. Collect every pillar, survive battles, then reach the exit room to win.

Saves can be created from the dungeon view and loaded from the main menu. SQLite saves are stored in `dungeon.db`. If SQLite saving is unavailable, fallback saves are written under `saves/`.

## Combat Sprites

Combat uses `assets/sprites/combat_sprites.png`, a 6 row by 4 column transparent sprite sheet. Rows map to Warrior, Thief, Priestess, Skeleton, Ogre, and Gremlin. Columns map to idle, attack, hit, and defeated poses. The Swing combat view mirrors monster sprites at render time so enemies face the hero consistently.

Cooldown numbers are rendered by `dungeoncrawler.view.CooldownSpriteIcon` as custom pixel-style button badges. The combat model exposes cooldown state through `Battle`, and the view owns the visual presentation.

## Generated Files

The following files are runtime or IDE output and should not be committed:

- `dungeon.db`
- `saves/`
- `out/`
- IntelliJ workspace-local files such as `.idea/workspace.xml`

The SQLite JDBC jar in `lib/` is intentionally tracked because the project depends on it at runtime.
