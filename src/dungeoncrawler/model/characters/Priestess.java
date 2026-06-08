package dungeoncrawler.model.characters;

/**
 * Support-oriented hero class with a healing special skill.
 */
public class Priestess extends Hero {
    /** Priestess's default maximum hit points. */
    public static final int HP = 75;
    /** Priestess's default attack speed. */
    public static final int SPD = 5;
    /** Priestess's default hit chance. */
    public static final double HIT = 0.7;
    /** Priestess's default block chance. */
    public static final double BLOCK = 0.3;
    /** Priestess's default minimum regular damage. */
    public static final int MIN_DMG = 25;
    private static final int MAX_DMG = 45;
    private static final int MIN_HEAL = 25;
    private static final int MAX_HEAL = 45;

    /**
     * Creates a priestess with the documented default statistics.
     *
     * @param theName player-entered hero name
     */
    public Priestess(final String theName) {
        super(theName, HP, MIN_DMG, MAX_DMG, SPD, HIT, BLOCK);
    }

    /**
     * Uses the priestess healing special skill.
     *
     * @param theOpp unused opponent parameter required by the hero API
     */
    @Override
    public void useSpecialSkill(final DungeonCharacter theOpp) {
        heal();
    }

    /**
     * Heals the priestess by a random amount in the special-skill range.
     *
     * @return actual hit points restored
     */
    public int heal() {
        return super.heal(randomInRange(MIN_HEAL, MAX_HEAL));
    }

    /**
     * Returns the standard hero summary for this priestess.
     *
     * @return formatted hero summary
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
