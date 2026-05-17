package dungeoncrawler.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Applies font roles to Swing components and refreshes them as the window
 * scale changes.
 */
final class ScalableFontManager {
    private static final String BASE_FONT_SIZE = "baseFontSize";
    private static final String FONT_STYLE = "fontStyle";

    private final JFrame myWindow;
    private final WindowScaler myWindowScaler;

    ScalableFontManager(final JFrame theWindow,
                        final WindowScaler theWindowScaler) {
        myWindow = theWindow;
        myWindowScaler = theWindowScaler;
    }

    void setScalableFont(final JComponent theComponent,
                         final int theStyle,
                         final int theBaseSize) {
        theComponent.putClientProperty(BASE_FONT_SIZE, theBaseSize);
        theComponent.putClientProperty(FONT_STYLE, theStyle);
        theComponent.setFont(buildScaledFont(theStyle, theBaseSize));
    }

    void updateScaledFonts(final Component theComponent) {
        Object baseSize = null;
        Object style = null;
        if (theComponent instanceof JComponent) {
            JComponent component = (JComponent) theComponent;
            baseSize = component.getClientProperty(BASE_FONT_SIZE);
            style = component.getClientProperty(FONT_STYLE);
        }

        if (baseSize instanceof Integer && style instanceof Integer) {
            theComponent.setFont(buildScaledFont((Integer) style, (Integer) baseSize));
        }

        if (theComponent instanceof Container) {
            for (Component child : ((Container) theComponent).getComponents()) {
                updateScaledFonts(child);
            }
        }
    }

    private Font buildScaledFont(final int theStyle, final int theBaseSize) {
        int scaledSize = Math.max(1, (int) Math.round(theBaseSize
                * myWindowScaler.currentFontScale(myWindow.getSize())));
        return new Font(Font.MONOSPACED, theStyle, scaledSize);
    }
}
