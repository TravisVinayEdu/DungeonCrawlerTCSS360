package dungeoncrawler.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dungeoncrawler.model.Direction;
import dungeoncrawler.model.Dungeon;
import dungeoncrawler.model.Pillar;
import dungeoncrawler.model.Room;
import dungeoncrawler.model.characters.Hero;
import dungeoncrawler.model.characters.Warrior;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GameSession")
class GameSessionTest {

    @Test
    @DisplayName("a winning session rejects further movement")
    void winningSessionRejectsFurtherMovement() {
        Room[][] maze = new Room[1][2];
        maze[0][0] = new Room(0, 0);
        maze[0][1] = new Room(0, 1);
        maze[0][0].setEntrance();
        maze[0][0].setDoor(Direction.EAST, true);
        maze[0][1].setDoor(Direction.WEST, true);
        maze[0][1].setExit();

        boolean[][] discovered = {{true, true}};
        Dungeon dungeon = new Dungeon(maze, 2, 1, 0, 1, discovered);
        Hero hero = new Warrior("Tester");
        for (Pillar pillar : Pillar.values()) {
            hero.addPillar(pillar);
        }

        GameSession session = new GameSession(hero, dungeon);
        GameSession.MoveResult result = session.moveHero(Direction.WEST);

        assertAll(
                () -> assertTrue(session.hasWon()),
                () -> assertFalse(result.moved()),
                () -> assertEquals(0, dungeon.getHeroRow()),
                () -> assertEquals(1, dungeon.getHeroCol()),
                () -> assertTrue(result.message().contains("already escaped")));
    }
}
