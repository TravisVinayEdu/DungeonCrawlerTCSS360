package dungeoncrawler.model;

import dungeoncrawler.model.characters.Hero;

public class VisionPotion extends Potion {
    public final static int VISION_RADIUS = 1;

    public VisionPotion() {
        super(VISION_RADIUS);
    }

    @Override
    public void apply(final Hero theHero) {
        // The dungeon uses VISION_RADIUS to reveal nearby rooms.
    }
}
