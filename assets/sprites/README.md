# Combat Sprites

`combat_sprites.png` is the runtime sprite sheet used by the Swing combat screen. It is a transparent PNG generated for this project from a chroma-key source.

`combat_sprites_chroma.png` is the original generated source image with the flat key background preserved for future cleanup or regeneration work.

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

The loader in `dungeoncrawler.view.CombatSpriteSheet` slices the sheet by this contract. If the sheet is replaced, keep the same row and column order.

Monster sprites are mirrored by `dungeoncrawler.view.CombatSpritePanel` at render time so every monster faces the hero in combat. Replacement sheets can keep the same facing convention or use neutral source orientation; the battle view is responsible for final direction.

## Generation Prompt

Original built-in image generation prompt summary:

```text
Create an original polished 32-bit pixel-art fantasy combat sprite sheet for a Java Swing dungeon crawler. Use exactly 6 rows and 4 columns with equal-size cells, no text, no labels, no grid lines, and no scenery. Rows are Warrior, Thief, Priestess, Skeleton, Ogre, Gremlin. Columns are idle, attack, hit/recoil, defeated. Heroes face right and monsters face left. Generate on a flat solid chroma-key background for transparency cleanup.
```
