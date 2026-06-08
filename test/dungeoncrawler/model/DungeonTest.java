package dungeoncrawler.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import dungeoncrawler.model.characters.Ogre;
import dungeoncrawler.model.characters.Skeleton;
import dungeoncrawler.persistence.MonsterDatabase;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link Dungeon}.
 *
 * <p>Maze generation is random, so movement and vision are tested through the
 * "load a saved dungeon" constructor with a hand-built {@code Room} grid whose
 * doors are known. A single structural test exercises the generating
 * constructor with a mocked {@link MonsterDatabase} to confirm the generated
 * invariants (dimensions, entrance/exit placement, hero start).</p>
 */
@DisplayName("Dungeon")
class DungeonTest {

    /**
     * Builds a 2x2 dungeon with the hero at (0,0). Open doors:
     * (0,0)&lt;-&gt;(0,1) east/west and (0,0)&lt;-&gt;(1,0) south/north. The
     * two doors touching (1,1) are left closed.
     */
    private static Dungeon twoByTwoDungeon() {
        final Room[][] maze = new Room[2][2];
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                maze[r][c] = new Room(r, c);
            }
        }
        maze[0][0].setDoor(Direction.EAST, true);
        maze[0][1].setDoor(Direction.WEST, true);
        maze[0][0].setDoor(Direction.SOUTH, true);
        maze[1][0].setDoor(Direction.NORTH, true);

        final boolean[][] discovered = new boolean[2][2];
        discovered[0][0] = true; // start room already seen
        return new Dungeon(maze, 2, 2, 0, 0, discovered);
    }

    @Nested
    @DisplayName("construction validation")
    class Construction {

        @Test
        @DisplayName("rejects non-positive dimensions")
        void rejectsNonPositiveDimensions() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new Dungeon(0, 5, null)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new Dungeon(5, 0, null)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new Dungeon(-1, 5, null)));
        }
    }

    @Nested
    @DisplayName("movement")
    class Movement {

        @Test
        @DisplayName("moving through an open door updates the hero's position")
        void moveThroughOpenDoor() {
            final Dungeon dungeon = twoByTwoDungeon();

            final boolean moved = dungeon.moveHero(Direction.EAST);

            assertAll(
                    () -> assertTrue(moved),
                    () -> assertEquals(0, dungeon.getHeroRow()),
                    () -> assertEquals(1, dungeon.getHeroCol()),
                    () -> assertSame(dungeon.getRoom(0, 1), dungeon.getCurrentRoom()));
        }

        @Test
        @DisplayName("moving against a closed door leaves the hero in place")
        void blockedByClosedDoor() {
            final Dungeon dungeon = twoByTwoDungeon();

            final boolean moved = dungeon.moveHero(Direction.NORTH);

            assertAll(
                    () -> assertFalse(moved),
                    () -> assertEquals(0, dungeon.getHeroRow()),
                    () -> assertEquals(0, dungeon.getHeroCol()));
        }

        @Test
        @DisplayName("the hero can traverse several rooms in sequence")
        void traverseMultipleRooms() {
            final Dungeon dungeon = twoByTwoDungeon();

            assertTrue(dungeon.moveHero(Direction.SOUTH), "south door is open");
            assertEquals(1, dungeon.getHeroRow());
            // From (1,0) only the north door is open, so east is blocked.
            assertFalse(dungeon.moveHero(Direction.EAST), "no east door from (1,0)");
            assertTrue(dungeon.moveHero(Direction.NORTH), "back north to the start");
            assertEquals(0, dungeon.getHeroRow());
        }
    }

    @Nested
    @DisplayName("vision")
    class Vision {

        @Test
        @DisplayName("entering a room marks it as discovered")
        void movingDiscoversRoom() {
            final Dungeon dungeon = twoByTwoDungeon();
            assertFalse(dungeon.isDiscovered(0, 1), "not yet visited");

            dungeon.moveHero(Direction.EAST);

            assertTrue(dungeon.isDiscovered(0, 1));
        }

        @Test
        @DisplayName("revealAroundHero exposes undiscovered rooms within the radius")
        void revealAroundHero() {
            final Dungeon dungeon = twoByTwoDungeon();

            final int revealed = dungeon.revealAroundHero(1);

            assertAll(
                    () -> assertEquals(3, revealed, "the other three rooms of the 2x2 grid"),
                    () -> assertTrue(dungeon.isDiscovered(0, 1)),
                    () -> assertTrue(dungeon.isDiscovered(1, 0)),
                    () -> assertTrue(dungeon.isDiscovered(1, 1)));
        }

        @Test
        @DisplayName("revealAroundHero reveals nothing the second time")
        void revealIsIdempotent() {
            final Dungeon dungeon = twoByTwoDungeon();

            dungeon.revealAroundHero(1);
            final int secondPass = dungeon.revealAroundHero(1);

            assertEquals(0, secondPass);
        }

        @Test
        @DisplayName("isDiscovered is false for out-of-bounds coordinates")
        void isDiscoveredOutOfBounds() {
            final Dungeon dungeon = twoByTwoDungeon();
            assertAll(
                    () -> assertFalse(dungeon.isDiscovered(-1, 0)),
                    () -> assertFalse(dungeon.isDiscovered(0, 99)));
        }
    }

    @Nested
    @DisplayName("generated dungeon invariants")
    class GeneratedInvariants {

        @Test
        @DisplayName("a generated dungeon has the requested size, an entrance, an exit, and a hero at the start")
        void structuralInvariants() throws Exception {
            final MonsterDatabase db = Mockito.mock(MonsterDatabase.class);
            when(db.getAllMonsterNames()).thenReturn(List.of("Skeleton"));
            when(db.getMonsterByName("Skeleton"))
                    .thenAnswer(invocation -> new Skeleton());
            when(db.getMonsterByName("Ogre"))
                    .thenAnswer(invocation -> new Ogre());

            final Dungeon dungeon = new Dungeon(6, 6, db);
            final int[] entranceCount = {0};
            final int[] exitCount = {0};
            final Room[] entrance = {null};
            final Room[] exit = {null};
            for (int row = 0; row < dungeon.getHeight(); row++) {
                for (int col = 0; col < dungeon.getWidth(); col++) {
                    final Room room = dungeon.getRoom(row, col);
                    if (room.isEntrance()) {
                        entranceCount[0]++;
                        entrance[0] = room;
                    }
                    if (room.isExit()) {
                        exitCount[0]++;
                        exit[0] = room;
                    }
                }
            }

            assertAll(
                    () -> assertEquals(6, dungeon.getWidth()),
                    () -> assertEquals(6, dungeon.getHeight()),
                    () -> assertEquals(1, entranceCount[0], "one entrance"),
                    () -> assertEquals(1, exitCount[0], "one exit"),
                    () -> assertFalse(entrance[0].isExit(), "entrance and exit are distinct"),
                    () -> assertSame(entrance[0], dungeon.getCurrentRoom()),
                    () -> assertSame(exit[0], dungeon.getRoom(exit[0].getRow(), exit[0].getCol())),
                    () -> assertTrue(dungeon.isDiscovered(dungeon.getHeroRow(),
                            dungeon.getHeroCol()), "the start room is discovered"));
        }
    }
}
