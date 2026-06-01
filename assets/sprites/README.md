# Combat Sprites

`combat_sprites.png` is the runtime sprite sheet used by the Swing combat
screen. It keeps the original 6-row by 4-column contract expected by
`dungeoncrawler.view.CombatSpriteSheet`.

`combat_sprites_chroma.png` is the same replacement sheet on a flat chroma-key
background for inspection or future cleanup work.

These sprites are not AI-generated. Most rows are deterministic derivatives of
Kenney Tiny Dungeon source sprites. The Ogre row uses LordNeo's OpenGameArt
`Orc [Static] [64x64]` sprite because it reads as a bulky ogre/brute much more
clearly than the Tiny Dungeon creature tile. All listed sources are released
under Creative Commons Zero (CC0).

## Sheet Layout

The sheet is divided into 6 rows and 4 columns.

Rows:

1. Warrior
2. Thief
3. Priestess
4. Skeleton
5. Ogre
6. Gremlin

Columns:

1. Idle
2. Attack
3. Hit
4. Defeated

The loader slices the sheet by this contract. If the sheet is replaced, keep the
same row and column order.

Monster sprites are mirrored by `dungeoncrawler.view.CombatSpritePanel` at
render time so every monster faces the hero in combat.

## Source Credits

Primary source pack: Kenney Tiny Dungeon

Creator: Kenney

License: Creative Commons Zero (CC0)

Official source: https://kenney.nl/assets/tiny-dungeon

OpenGameArt release used for download:
https://opengameart.org/content/tiny-dungeon

Itch page with no-generative-AI metadata:
https://kenney-assets.itch.io/tiny-dungeon

Credit text, if desired:
`Sprites derived from Tiny Dungeon by Kenney and Orc [Static] by LordNeo, CC0.`

Additional Ogre source:

Asset: Orc [Static] [64x64]

Creator: LordNeo

License: Creative Commons Zero (CC0)

Source: https://opengameart.org/content/orc-static-64x64

## Source Tile Mapping

The original 16x16 source tiles are preserved in `source_tiles`.

| Game character | Source tile copy |
| --- | --- |
| Warrior | `source_tiles/kenney_tiny_dungeon_warrior.png` |
| Thief | `source_tiles/kenney_tiny_dungeon_thief.png` |
| Priestess | `source_tiles/kenney_tiny_dungeon_priestess.png` |
| Skeleton | `source_tiles/kenney_tiny_dungeon_skeleton.png` |
| Ogre | `source_tiles/opengameart_lordneo_orc_static_ogre.png` |
| Gremlin | `source_tiles/kenney_tiny_dungeon_gremlin.png` |

The attack, hit, and defeated columns are simple programmatic transforms of
those CC0 source tiles: movement offset, non-AI tint/recoil, and rotation.

The Priestess source tile uses a clearly feminine Kenney character sprite from
the same Tiny Dungeon pack so the battle UI reads closer to the class name.

The Ogre source uses a larger axe-and-shield brute sprite from OpenGameArt so it
does not resemble the crab-like Tiny Dungeon creature tile.
