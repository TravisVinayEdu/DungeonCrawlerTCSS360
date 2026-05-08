import java.io.IOException;

import javax.swing.SwingUtilities;

public class DungeonCrawler {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TerminalWindow(new DungeonCrawler()).open());
    }

    void runGame(Appendable terminal) {
        runGame(terminal, null);
    }

    void runGame(Appendable terminal, Hero hero) {
        writeLine(terminal, "Initializing dungeon systems...");
        Dungeon dungeon = new Dungeon(10, 10);
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

    Hero createHero(final String theClassName, final String theName) {
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

    Dungeon createDungeon() {
        return new Dungeon(10, 10);
    }

    public void saveGame(String filename){}
    public void loadGame(String filename){}
    private void playTurn(){}
    private void handleRoom(Appendable terminal, Room r) {
        writeLine(terminal, "Current Room:");
        writeLine(terminal, r.toString());
        writeLine(terminal, "");
        writeLine(terminal, "Available exits:");
        writeLine(terminal, exitsFor(r));
    }
    private void battle(Hero h, Monster m){}

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
