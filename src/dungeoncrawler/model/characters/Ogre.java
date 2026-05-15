package dungeoncrawler.model.characters;

/**
 * A high-health monster that hits hard and heals infrequently.
 */
public class Ogre extends Monster {
    public static final String OGRE_NAME = "Ogre";
    public static final int HP = 200;
    public static final int SPD = 2;
    public static final double HIT = 0.6;
    public static final int MIN_DMG = 30;
    private static final int MAX_DMG = 60;
    private static final double HEAL_CHANCE = 0.1;
    private static final int MIN_HEAL = 30;
    private static final int MAX_HEAL = 60;

    public Ogre() {
        super(OGRE_NAME, HP, MIN_DMG, MAX_DMG, SPD, HIT,
                HEAL_CHANCE, MIN_HEAL, MAX_HEAL);
    }
}
