import java.io.IOException;

import javax.swing.SwingUtilities;

public class DungeonCrawler {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TerminalWindow(new DungeonCrawler()).open());
    }

    void runGame(Appendable terminal) {
        writeLine(terminal, "Initializing dungeon systems...");
        writeLine(terminal, "Loading hero records...");
        writeLine(terminal, "Dungeon Crawler started.");
        writeLine(terminal, "");
        writeLine(terminal, "Game loop is ready to connect to the dungeon implementation.");
    }

    public void saveGame(String filename){}
    public void loadGame(String filename){}
    private Hero chooseHero(){return null;}
    private void playTurn(){}
    private void handleRoom(Room r){}
    private void battle(Hero h, Monster m){}

    private static void writeLine(Appendable terminal, String text) {
        try {
            terminal.append(text).append(System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException("Unable to write to terminal.", e);
        }
    }
}
