package dungeoncrawler.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dungeoncrawler.model.characters.Monster;
import dungeoncrawler.model.characters.Ogre;
import dungeoncrawler.persistence.MonsterDatabase;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

/**
 * Tests for {@link Room}.
 *
 * <p>A freshly built room rolls random potion/pit features in its constructor,
 * so every test that depends on room contents first normalizes the room to a
 * known-empty state via {@link #cleanRoom()}.</p>
 */
@DisplayName("Room")
class RoomTest {

    /** Creates a normal room with all randomly-rolled features cleared. */
    private static Room cleanRoom() {
        final Room room = new Room(2, 3);
        room.removePit();
        room.setHealingPotion(false);
        room.setVisionPotion(false);
        return room;
    }

    @Nested
    @DisplayName("coordinates and emptiness")
    class Basics {

        @Test
        @DisplayName("stores its row and column")
        void storesCoordinates() {
            final Room room = new Room(2, 3);
            assertAll(
                    () -> assertEquals(2, room.getRow()),
                    () -> assertEquals(3, room.getCol()));
        }

        @Test
        @DisplayName("a cleared room is empty and has no item")
        void clearedRoomIsEmpty() {
            final Room room = cleanRoom();
            assertAll(
                    () -> assertTrue(room.isEmpty()),
                    () -> assertFalse(room.hasItem()));
        }
    }

    @Nested
    @DisplayName("potions and pit")
    class Contents {

        @Test
        @DisplayName("adding a healing potion makes the room non-empty and an item-holder")
        void healingPotionAddsItem() {
            final Room room = cleanRoom();
            room.setHealingPotion(true);

            assertAll(
                    () -> assertTrue(room.hasHealingPotion()),
                    () -> assertTrue(room.hasItem()),
                    () -> assertFalse(room.isEmpty()));
        }

        @Test
        @DisplayName("removePotion only clears the matching potion type")
        void removePotionByType() {
            final Room room = cleanRoom();
            room.setHealingPotion(true);
            room.setVisionPotion(true);

            room.removePotion(new HealingPotion());

            assertAll(
                    () -> assertFalse(room.hasHealingPotion(), "healing potion removed"),
                    () -> assertTrue(room.hasVisionPotion(), "vision potion untouched"));
        }

        @Test
        @DisplayName("a pit deals fixed damage, and none once removed")
        void pitDamage() {
            final Room room = cleanRoom();
            room.setPit();
            assertAll(
                    () -> assertTrue(room.hasPit()),
                    () -> assertEquals(20, room.fallInPit()));

            room.removePit();
            assertAll(
                    () -> assertFalse(room.hasPit()),
                    () -> assertEquals(0, room.fallInPit()));
        }
    }

    @Nested
    @DisplayName("pillar")
    class PillarTests {

        @Test
        @DisplayName("a pillar can be placed, read, and removed")
        void placeReadRemovePillar() {
            final Room room = cleanRoom();
            room.setPillar(Pillar.ABSTRACTION);

            assertAll(
                    () -> assertEquals(Pillar.ABSTRACTION, room.getPillar()),
                    () -> assertTrue(room.hasItem()));

            final Pillar removed = room.removePillar();

            assertAll(
                    () -> assertEquals(Pillar.ABSTRACTION, removed),
                    () -> assertNull(room.getPillar()));
        }
    }

    @Nested
    @DisplayName("doors")
    class Doors {

        @ParameterizedTest
        @EnumSource(Direction.class)
        @DisplayName("a door reports open in exactly the direction it was opened")
        void doorTogglesPerDirection(final Direction theDirection) {
            final Room room = cleanRoom();

            assertFalse(room.workingDoor(theDirection), "doors start closed");

            room.setDoor(theDirection, true);
            assertTrue(room.workingDoor(theDirection));
            assertTrue(room.isEscapable(), "an open door makes the room escapable");
        }

        @Test
        @DisplayName("a sealed room is not escapable")
        void sealedRoomIsNotEscapable() {
            assertFalse(cleanRoom().isEscapable());
        }

        @Test
        @DisplayName("a null direction is rejected")
        void nullDirectionRejected() {
            final Room room = cleanRoom();
            assertThrows(IllegalArgumentException.class, () -> room.workingDoor(null));
        }
    }

    @Nested
    @DisplayName("entrance and exit")
    class SpecialRooms {

        @Test
        @DisplayName("marking a room as the entrance clears its contents")
        void entranceClearsContents() {
            final Room room = cleanRoom();
            room.setPit();
            room.setHealingPotion(true);

            room.setEntrance();

            assertAll(
                    () -> assertTrue(room.isEntrance()),
                    () -> assertFalse(room.isExit()),
                    () -> assertFalse(room.hasPit()),
                    () -> assertFalse(room.hasHealingPotion()));
        }

        @Test
        @DisplayName("entrance and exit are mutually exclusive")
        void entranceAndExitAreExclusive() {
            final Room room = cleanRoom();

            room.setEntrance();
            room.setExit();

            assertAll(
                    () -> assertTrue(room.isExit()),
                    () -> assertFalse(room.isEntrance()));
        }

        @Test
        @DisplayName("special rooms reject new contents")
        void specialRoomsRejectContents() {
            final Room room = cleanRoom();
            room.setEntrance();

            room.setPit();
            room.setPillar(Pillar.POLYMORPHISM);
            room.setHealingPotion(true);

            assertAll(
                    () -> assertFalse(room.hasPit()),
                    () -> assertNull(room.getPillar()),
                    () -> assertFalse(room.hasHealingPotion()));
        }
    }

    @Nested
    @DisplayName("monster")
    class MonsterContents {

        @Test
        @DisplayName("a manually set monster can be read and removed")
        void manualMonster() {
            final Room room = cleanRoom();
            final Monster ogre = new Ogre();

            room.setMonstersManual(ogre);
            assertSame(ogre, room.getMonster());

            room.removeMonster();
            assertNull(room.getMonster());
        }

        @Test
        @DisplayName("setMonsters pulls a monster from the database by name")
        void setMonstersFromDatabase() throws Exception {
            final Room room = cleanRoom();
            final MonsterDatabase db = Mockito.mock(MonsterDatabase.class);
            final Monster ogre = new Ogre();
            when(db.getAllMonsterNames()).thenReturn(List.of("Ogre"));
            when(db.getMonsterByName("Ogre")).thenReturn(ogre);

            room.setMonsters(db);

            assertSame(ogre, room.getMonster());
            verify(db).getMonsterByName("Ogre");
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("a sealed empty room renders walls on every side")
        void sealedEmptyRoom() {
            final String expected = String.join(System.lineSeparator(),
                    "+---+", "|   |", "+---+");
            assertEquals(expected, cleanRoom().toString());
        }

        @Test
        @DisplayName("the entrance room renders the E marker")
        void entranceMarker() {
            final Room room = cleanRoom();
            room.setEntrance();
            assertTrue(room.toString().contains(" E "));
        }
    }
}
