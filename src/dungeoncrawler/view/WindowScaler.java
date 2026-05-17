package dungeoncrawler.view;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;

/**
 * Centralizes screen-aware window sizing and scale calculations.
 */
final class WindowScaler {
    private static final Dimension STARTING_SIZE = new Dimension(900, 650);
    private static final Dimension BASE_SCREEN_SIZE = new Dimension(1366, 768);
    private static final Dimension BASE_MINIMUM_SIZE = new Dimension(720, 460);
    private static final Dimension LOWEST_MINIMUM_SIZE = new Dimension(520, 360);
    private static final int SCREEN_MARGIN = 40;
    private static final int BASE_DPI = 96;
    private static final double ZOOM_STEP = 0.10;
    private static final double MIN_SCALE = 0.60;
    private static final double MAX_SCALE = 2.40;

    private double myUserScale = 1.0;

    Dimension scaledStartingSize() {
        Rectangle screen = usableScreenBounds();
        int width = Math.min(scaledLength(STARTING_SIZE.width), availableWidth(screen));
        int height = Math.min(scaledLength(STARTING_SIZE.height), availableHeight(screen));
        Dimension minimum = scaledMinimumSize();
        return new Dimension(Math.max(minimum.width, width),
                Math.max(minimum.height, height));
    }

    Dimension scaledMinimumSize() {
        Rectangle screen = usableScreenBounds();
        int width = Math.max(LOWEST_MINIMUM_SIZE.width,
                Math.min(scaledLength(BASE_MINIMUM_SIZE.width), availableWidth(screen)));
        int height = Math.max(LOWEST_MINIMUM_SIZE.height,
                Math.min(scaledLength(BASE_MINIMUM_SIZE.height), availableHeight(screen)));
        return new Dimension(width, height);
    }

    double currentFontScale(final Dimension theWindowSize) {
        int width = Math.max(1, theWindowSize.width);
        int height = Math.max(1, theWindowSize.height);
        double currentArea = (double) width * height;
        Dimension startingSize = scaledStartingSize();
        double startingArea = (double) startingSize.width * startingSize.height;
        double windowScale = Math.sqrt(currentArea / startingArea);
        return clamp(deviceScale() * windowScale * myUserScale);
    }

    Dimension scaledDimension(final int theWidth, final int theHeight) {
        return new Dimension(scaledLength(theWidth), scaledLength(theHeight));
    }

    int scaledLength(final int theBaseLength) {
        return Math.max(1, (int) Math.round(theBaseLength * deviceScale()));
    }

    void increaseUserScale() {
        myUserScale = clampUserScale(myUserScale + ZOOM_STEP);
    }

    void decreaseUserScale() {
        myUserScale = clampUserScale(myUserScale - ZOOM_STEP);
    }

    void resetUserScale() {
        myUserScale = 1.0;
    }

    private double deviceScale() {
        return clamp(Math.max(screenScale(), dpiScale()));
    }

    private double screenScale() {
        Rectangle screen = usableScreenBounds();
        double widthScale = screen.getWidth() / BASE_SCREEN_SIZE.getWidth();
        double heightScale = screen.getHeight() / BASE_SCREEN_SIZE.getHeight();
        return Math.min(widthScale, heightScale);
    }

    private double dpiScale() {
        return Toolkit.getDefaultToolkit().getScreenResolution() / (double) BASE_DPI;
    }

    private Rectangle usableScreenBounds() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
    }

    private int availableWidth(final Rectangle theScreen) {
        return Math.max(LOWEST_MINIMUM_SIZE.width, theScreen.width - SCREEN_MARGIN);
    }

    private int availableHeight(final Rectangle theScreen) {
        return Math.max(LOWEST_MINIMUM_SIZE.height, theScreen.height - SCREEN_MARGIN);
    }

    private double clamp(final double theValue) {
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, theValue));
    }

    private double clampUserScale(final double theValue) {
        return Math.max(0.70, Math.min(1.60, theValue));
    }
}
