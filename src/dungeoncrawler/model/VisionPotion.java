package dungeoncrawler.model;

import dungeoncrawler.model.characters.Hero;

public class VisionPotion extends Potion {
    public final static int VISION_RADIUS = 1;

    public VisionPotion() {
        super(VISION_RADIUS);
    }

    @Override
    public void apply(final Hero theHero) {
        for (int i = 0; i < (8 * VISION_RADIUS); i++) {

        }
        // The dungeon uses this potion by showing the surrounding rooms.
    }
}
