package dungeoncrawler.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * Pixel-style cooldown number badge for battle action buttons.
 */
final class CooldownSpriteIcon implements Icon {
    private static final int SIZE = 30;
    private static final int PIXEL = 3;
    private static final int DIGIT_WIDTH = 3;
    private static final int DIGIT_HEIGHT = 5;
    private static final Color SHADOW = new Color(15, 16, 20);
    private static final Color BACKGROUND = new Color(42, 31, 45);
    private static final Color INNER_BACKGROUND = new Color(67, 49, 73);
    private static final Color BORDER_DARK = new Color(111, 79, 135);
    private static final Color BORDER_LIGHT = new Color(226, 201, 112);
    private static final Color DIGIT_SHADOW = new Color(101, 55, 72);
    private static final Color DIGIT = new Color(255, 236, 148);
    private static final String[][] DIGITS = {
        {"111", "101", "101", "101", "111"},
        {"010", "110", "010", "010", "111"},
        {"111", "001", "111", "100", "111"},
        {"111", "001", "111", "001", "111"},
        {"101", "101", "111", "001", "001"},
        {"111", "100", "111", "001", "111"},
        {"111", "100", "111", "101", "111"},
        {"111", "001", "010", "010", "010"},
        {"111", "101", "111", "101", "111"},
        {"111", "101", "111", "001", "111"}
    };

    private final int myNumber;

    CooldownSpriteIcon(final int theNumber) {
        myNumber = Math.max(0, Math.min(DIGITS.length - 1, theNumber));
    }

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }

    @Override
    public void paintIcon(final Component theComponent,
                          final Graphics theGraphics,
                          final int theX,
                          final int theY) {
        Graphics2D graphics = (Graphics2D) theGraphics.create();
        try {
            paintBadge(graphics, theX, theY);
            int digitWidth = DIGIT_WIDTH * PIXEL;
            int digitHeight = DIGIT_HEIGHT * PIXEL;
            int digitX = theX + (SIZE - digitWidth) / 2;
            int digitY = theY + (SIZE - digitHeight) / 2;
            paintDigit(graphics, digitX + 1, digitY + 1, DIGIT_SHADOW);
            paintDigit(graphics, digitX, digitY, DIGIT);
        } finally {
            graphics.dispose();
        }
    }

    private void paintBadge(final Graphics2D theGraphics,
                            final int theX,
                            final int theY) {
        theGraphics.setColor(SHADOW);
        theGraphics.fillRect(theX + 2, theY + 3, SIZE - 3, SIZE - 3);
        theGraphics.setColor(BORDER_DARK);
        theGraphics.fillRect(theX + 1, theY + 1, SIZE - 3, SIZE - 3);
        theGraphics.setColor(BORDER_LIGHT);
        theGraphics.fillRect(theX + 2, theY + 2, SIZE - 5, 2);
        theGraphics.fillRect(theX + 2, theY + 2, 2, SIZE - 5);
        theGraphics.setColor(BACKGROUND);
        theGraphics.fillRect(theX + 4, theY + 4, SIZE - 9, SIZE - 9);
        theGraphics.setColor(INNER_BACKGROUND);
        theGraphics.fillRect(theX + 7, theY + 7, SIZE - 15, SIZE - 15);
    }

    private void paintDigit(final Graphics2D theGraphics,
                            final int theX,
                            final int theY,
                            final Color theColor) {
        theGraphics.setColor(theColor);
        String[] rows = DIGITS[myNumber];
        for (int row = 0; row < rows.length; row++) {
            String pixels = rows[row];
            for (int column = 0; column < pixels.length(); column++) {
                if (pixels.charAt(column) == '1') {
                    theGraphics.fillRect(theX + column * PIXEL,
                            theY + row * PIXEL,
                            PIXEL,
                            PIXEL);
                }
            }
        }
    }
}
