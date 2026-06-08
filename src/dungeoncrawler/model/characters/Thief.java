package dungeoncrawler.model.characters;

/**
 * Fast hero class with the Surprise Attack special skill.
 */
public class Thief extends Hero {
    /** Thief's default maximum hit points. */
    public static final int HP = 75;
    /** Thief's default attack speed. */
    public static final int SPD = 6;
    /** Thief's default hit chance. */
    public static final double HIT = 0.8;
    /** Thief's default block chance. */
    public static final double BLOCK = 0.4;
    /** Thief's default minimum regular damage. */
    public static final int MIN_DMG = 20;
    private static final int MAX_DMG = 40;
    private static final double SURPRISE_SUCCESS_CHANCE = 0.4;
    private static final double CAUGHT_CHANCE = 0.2;

    /**
     * Creates a thief with the documented default statistics.
     *
     * @param theName player-entered hero name
     */
    public Thief(final String theName) {
        super(theName, HP, MIN_DMG, MAX_DMG, SPD, HIT, BLOCK);
    }

    /**
     * Uses the thief's Surprise Attack special skill.
     *
     * @param theOpp opponent targeted by the special skill
     */
    @Override
    public void useSpecialSkill(final DungeonCharacter theOpp) {
        surpriseAttack(theOpp);
    }

    /**
     * Attempts a surprise attack.
     *
     * <p>The thief may strike twice, be caught and do nothing, or make a
     * normal attack depending on the random roll.</p>
     *
     * @param theOpp opponent to attack
     */
    public void surpriseAttack(final DungeonCharacter theOpp) {
        if (theOpp == null || theOpp.isFainted()) {
            return;
        }

        final double roll = randomDouble();
        if (roll < SURPRISE_SUCCESS_CHANCE) {
            attack(theOpp);
            attack(theOpp);
        } else if (roll < SURPRISE_SUCCESS_CHANCE + CAUGHT_CHANCE) {
            return;
        } else {
            attack(theOpp);
        }
    }

    /**
     * Returns the standard hero summary for this thief.
     *
     * @return formatted hero summary
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
