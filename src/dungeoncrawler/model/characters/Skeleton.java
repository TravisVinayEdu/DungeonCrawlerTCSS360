package dungeoncrawler.model.characters;

/**
 * A balanced monster with steady damage and moderate healing.
 */
public class Skeleton extends Monster {
    public static final String SKELETON_NAME = "Skeleton";
    public static final int HP = 100;
    public static final int SPD = 3;
    public static final double HIT = 0.8;
    public static final int MIN_DMG = 30;
    private static final int MAX_DMG = 50;
    private static final double HEAL_CHANCE = 0.3;
    private static final int MIN_HEAL = 30;
    private static final int MAX_HEAL = 50;

    public Skeleton() {
        super(SKELETON_NAME, HP, MIN_DMG, MAX_DMG, SPD, HIT,
                HEAL_CHANCE, MIN_HEAL, MAX_HEAL);
    }
}
