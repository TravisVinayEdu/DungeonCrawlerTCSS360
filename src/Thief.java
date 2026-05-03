public class Thief extends Hero {
    public static final int HP = 75;
    public static final int SPD = 6;
    public static final double HIT = 0.8;
    public static final double BLOCK = 0.4;
    public static final int MIN_DMG = 20;
    private static final int MAX_DMG = 40;
    private static final double SURPRISE_SUCCESS_CHANCE = 0.4;
    private static final double CAUGHT_CHANCE = 0.2;

    public Thief(final String theName) {
        super(theName, HP, MIN_DMG, MAX_DMG, SPD, HIT, BLOCK);
    }

    @Override
    public void useSpecialSkill(final DungeonCharacter theOpp) {
        surpriseAttack(theOpp);
    }

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

    @Override
    public String toString() {
        return super.toString();
    }
}
