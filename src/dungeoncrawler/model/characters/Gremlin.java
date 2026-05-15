package dungeoncrawler.model.characters;

/**
 * A fast monster with modest damage and a strong chance to recover.
 */
public class Gremlin extends Monster {
    public static final String GREMLIN_NAME = "Gremlin";
    public static final int HP = 70;
    public static final int SPD = 5;
    public static final double HIT = 0.8;
    public static final int MIN_DMG = 15;
    private static final int MAX_DMG = 30;
    private static final double HEAL_CHANCE = 0.4;
    private static final int MIN_HEAL = 20;
    private static final int MAX_HEAL = 40;

    public Gremlin() {
        super(GREMLIN_NAME, HP, MIN_DMG, MAX_DMG, SPD, HIT,
                HEAL_CHANCE, MIN_HEAL, MAX_HEAL);
    }
}
