package dungeoncrawler.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Loads and slices the generated combat sprite sheet.
 */
final class CombatSpriteSheet {
    private static final String RESOURCE_PATH = "/assets/sprites/combat_sprites.png";
    private static final String FILE_PATH = "assets/sprites/combat_sprites.png";
    private static final int ROWS = 6;
    private static final int COLUMNS = CombatSpritePose.values().length;
    private static final Map<String, Integer> ROW_BY_CHARACTER = buildRowMap();

    private final BufferedImage mySheet;
    private final int myCellWidth;
    private final int myCellHeight;

    private CombatSpriteSheet(final BufferedImage theSheet) {
        mySheet = theSheet;
        myCellWidth = theSheet.getWidth() / COLUMNS;
        myCellHeight = theSheet.getHeight() / ROWS;
    }

    static CombatSpriteSheet load() {
        try {
            BufferedImage sheet = readSheet();
            if (sheet != null) {
                return new CombatSpriteSheet(sheet);
            }
        } catch (IOException exception) {
            // Fall back to generated silhouettes below.
        }
        return new CombatSpriteSheet(buildFallbackSheet());
    }

    BufferedImage spriteFor(final String theCharacterName,
                            final CombatSpritePose thePose) {
        int row = ROW_BY_CHARACTER.getOrDefault(theCharacterName, 0);
        int col = thePose.ordinal();
        return mySheet.getSubimage(col * myCellWidth, row * myCellHeight,
                myCellWidth, myCellHeight);
    }

    private static BufferedImage readSheet() throws IOException {
        try (InputStream stream = CombatSpriteSheet.class
                .getResourceAsStream(RESOURCE_PATH)) {
            if (stream != null) {
                return ImageIO.read(stream);
            }
        }
        File file = new File(FILE_PATH);
        if (file.isFile()) {
            return ImageIO.read(file);
        }
        return null;
    }

    private static Map<String, Integer> buildRowMap() {
        Map<String, Integer> rows = new HashMap<>();
        rows.put("Warrior", 0);
        rows.put("Thief", 1);
        rows.put("Priestess", 2);
        rows.put("Skeleton", 3);
        rows.put("Ogre", 4);
        rows.put("Gremlin", 5);
        return rows;
    }

    private static BufferedImage buildFallbackSheet() {
        int cell = 128;
        BufferedImage image = new BufferedImage(cell * COLUMNS, cell * ROWS,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        Color[] colors = {
            new Color(70, 110, 190),
            new Color(110, 70, 150),
            new Color(230, 220, 180),
            new Color(190, 190, 175),
            new Color(90, 150, 70),
            new Color(120, 180, 80)
        };
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int x = col * cell;
                int y = row * cell;
                graphics.setColor(colors[row]);
                graphics.fillOval(x + 36 + col * 3, y + 24 + col * 4, 56, 80);
                graphics.setColor(Color.BLACK);
                graphics.drawOval(x + 36 + col * 3, y + 24 + col * 4, 56, 80);
            }
        }
        graphics.dispose();
        return image;
    }
}
