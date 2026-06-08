package dungeoncrawler.model.characters;

/**
 * Durable hero class with high damage and the Crushing Blow special skill.
 */
public class Warrior extends Hero {

    /** Warrior's default maximum hit points. */
    public static final int HP = 125;
    /** Warrior's default attack speed. */
    public static final int SPD = 4;
    /** Warrior's default hit chance. */
    public static final double HIT = 0.8;
    /** Warrior's default block chance. */
    public static final double BLOCK = 0.2;
    /** Warrior's default minimum regular damage. */
    public static final int MIN_DMG = 35;
    private static final int MAX_DMG = 60;
    private static final double CRUSHING_BLOW_CHANCE = 0.4;
    private static final int MIN_CRUSHING_BLOW_DMG = 75;
    private static final int MAX_CRUSHING_BLOW_DMG = 175;

    /**
     * Creates a warrior with the documented default statistics.
     *
     * @param theName player-entered hero name
     */
    public Warrior(final String theName) {
        super(theName, HP, MIN_DMG, MAX_DMG, SPD, HIT, BLOCK);
    }

    /**
     * Uses the warrior's Crushing Blow special skill.
     *
     * @param theOpp opponent targeted by the special skill
     */
    @Override
    public void useSpecialSkill(final DungeonCharacter theOpp) {
        crushingBlow(theOpp);
    }

    /**
     * Attempts a high-damage attack with a reduced chance to succeed.
     *
     * @param theOpp opponent to damage
     */
    public void crushingBlow(final DungeonCharacter theOpp) {
        if (theOpp == null || theOpp.isFainted()) {
            return;
        }
        if (chanceSucceeds(CRUSHING_BLOW_CHANCE)) {
            theOpp.takeDamage(randomInRange(MIN_CRUSHING_BLOW_DMG, MAX_CRUSHING_BLOW_DMG));
        }
    }

    /**
     * Returns the standard hero summary for this warrior.
     *
     * @return formatted hero summary
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
