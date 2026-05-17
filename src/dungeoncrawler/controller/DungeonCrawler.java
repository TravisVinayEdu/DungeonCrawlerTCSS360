package dungeoncrawler.controller;

import dungeoncrawler.model.Direction;
import dungeoncrawler.model.Dungeon;
import dungeoncrawler.model.Room;
import dungeoncrawler.model.characters.Hero;
import dungeoncrawler.model.characters.Priestess;
import dungeoncrawler.model.characters.Thief;
import dungeoncrawler.model.characters.Warrior;
import dungeoncrawler.persistence.FileSaveManager;
import dungeoncrawler.persistence.MonsterDatabase;
import dungeoncrawler.persistence.SaveManager;
import dungeoncrawler.view.TerminalWindow;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.swing.SwingUtilities;

public class DungeonCrawler {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TerminalWindow(new DungeonCrawler()).open());
    }

    void runGame(Appendable terminal) throws SQLException {
        runGame(terminal, null);
    }

    void runGame(Appendable terminal, Hero hero) throws SQLException {
        writeLine(terminal, "Initializing dungeon systems...");
        MonsterDatabase temp = new MonsterDatabase();
        Dungeon dungeon = new Dungeon(10, 10, temp);
        if (hero == null) {
            writeLine(terminal, "Loading hero records...");
        } else {
            writeLine(terminal, "Loading hero record for " + hero.getName() + " the "
                    + hero.getClass().getSimpleName() + "...");
            writeLine(terminal, "Hit Points: " + hero.getMaxHitPoints());
            writeLine(terminal, "Damage: " + hero.getMinDamage() + "-" + hero.getMaxDamage());
            writeLine(terminal, "Attack Speed: " + hero.getAttackSpeed());
            writeLine(terminal, "Hit Chance: " + percent(hero.getHitChance()));
            writeLine(terminal, "Block Chance: " + percent(hero.getChanceToBlock()));
        }
        writeLine(terminal, "Dungeon Crawler started.");
        writeLine(terminal, "");
        handleRoom(terminal, dungeon.getCurrentRoom());
    }

    public Hero createHero(final String theClassName, final String theName) {
        switch (theClassName) {
            case "Warrior":
                return new Warrior(theName);
            case "Thief":
                return new Thief(theName);
            case "Priestess":
                return new Priestess(theName);
            default:
                throw new IllegalArgumentException("Unknown hero class: " + theClassName);
        }
    }

    public Dungeon createDungeon() throws SQLException {
        MonsterDatabase db = new MonsterDatabase();
        return new Dungeon(10, 10, db);
    }

    public GameSession createSession(final Hero theHero) throws SQLException {
        return new GameSession(theHero, createDungeon());
    }

    public long saveGame(final GameSession theSession) throws SQLException, IOException {
        try (SaveManager saveManager = new SaveManager()) {
            return saveManager.saveGame(theSession.getHero(), theSession.getDungeon());
        } catch (SQLException exception) {
            return new FileSaveManager().saveGame(theSession);
        }
    }

    public GameSession loadGame(final long theSaveId) throws SQLException, IOException {
        if (theSaveId <= Integer.MAX_VALUE) {
            try (SaveManager saveManager = new SaveManager()) {
                Hero hero = saveManager.loadHero((int) theSaveId);
                Dungeon dungeon = saveManager.loadDungeon((int) theSaveId);
                return new GameSession(hero, dungeon);
            } catch (SQLException exception) {
                return new FileSaveManager().loadGame(theSaveId);
            }
        }
        return new FileSaveManager().loadGame(theSaveId);
    }

    public List<String> listSaves() throws SQLException, IOException {
        try (SaveManager saveManager = new SaveManager()) {
            return saveManager.listSaves();
        } catch (SQLException exception) {
            return new FileSaveManager().listSaves();
        }
    }

    private void handleRoom(Appendable terminal, Room r) {
        writeLine(terminal, "Current Room:");
        writeLine(terminal, r.toString());
        writeLine(terminal, "");
        writeLine(terminal, "Available exits:");
        writeLine(terminal, exitsFor(r));
    }
    private static void writeLine(Appendable terminal, String text) {
        try {
            terminal.append(text).append(System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException("Unable to write to terminal.", e);
        }
    }

    private static String percent(final double theChance) {
        return Math.round(theChance * 100) + "%";
    }

    private static String exitsFor(final Room theRoom) {
        String exits = "";
        if (theRoom.workingDoor(Direction.NORTH)) {
            exits += "north ";
        }
        if (theRoom.workingDoor(Direction.EAST)) {
            exits += "east ";
        }
        if (theRoom.workingDoor(Direction.SOUTH)) {
            exits += "south ";
        }
        if (theRoom.workingDoor(Direction.WEST)) {
            exits += "west ";
        }
        if (exits.isEmpty()) {
            return "none";
        }
        return exits.trim();
    }
}
