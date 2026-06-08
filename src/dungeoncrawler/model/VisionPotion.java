package dungeoncrawler.model;

import dungeoncrawler.model.characters.Hero;

/**
 * Potion that reveals nearby dungeon rooms when used during exploration.
 */
public class VisionPotion extends Potion {
    /** Number of rooms outward from the hero revealed by a vision potion. */
    public final static int VISION_RADIUS = 1;

    /**
     * Creates a vision potion with the configured reveal radius as potency.
     */
    public VisionPotion() {
        super(VISION_RADIUS);
    }

    /**
     * Leaves hero statistics unchanged.
     *
     * <p>The controller consumes the potion and asks the dungeon to reveal
     * rooms using {@link #VISION_RADIUS}.</p>
     *
     * @param theHero hero using the potion
     */
    @Override
    public void apply(final Hero theHero) {
        // The dungeon uses VISION_RADIUS to reveal nearby rooms.
    }
}
