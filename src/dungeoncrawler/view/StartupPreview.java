package dungeoncrawler.view;

/**
 * Provides the startup screen's static dungeon preview and symbol legend.
 */
final class StartupPreview {
    private static final String[] MAP_PREVIEW = {
            "+---+---+---+---+---+---+---+",
            "| S     | H     |       | M |",
            "+   +---+   +   +---+   +   +",
            "|   | A     |       | L     |",
            "+   +   +---+---+   +---+   +",
            "|       | @     | V     |   |",
            "+---+   +   +---+---+   +   +",
            "| I     |       | P     | X |",
            "+---+---+---+---+---+---+---+"
    };
    private static final String[] SYMBOL_TABLE = {
            "+--------+---------------+",
            "| Symbol | Meaning       |",
            "+--------+---------------+",
            "| S      | Start         |",
            "| X      | Exit          |",
            "| @      | Hero          |",
            "| H      | Healing       |",
            "| V      | Vision        |",
            "| L      | Lava          |",
            "| M      | Monster       |",
            "| A/E/I/P| Pillars       |",
            "+--------+---------------+"
    };
    private static final int MAP_WIDTH = 29;
    private static final String GAP = "    ";

    private StartupPreview() {
    }

    static String[] lines() {
        String[] lines = new String[SYMBOL_TABLE.length];
        for (int i = 0; i < SYMBOL_TABLE.length; i++) {
            String mapLine = i < MAP_PREVIEW.length ? MAP_PREVIEW[i]
                    : " ".repeat(MAP_WIDTH);
            lines[i] = mapLine + GAP + SYMBOL_TABLE[i];
        }
        return lines;
    }
}
