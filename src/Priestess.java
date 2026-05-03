public class Priestess extends Hero {
    public static final int HP = 75;
    public static final int SPD = 5;
    public static final double HIT = 0.7;
    public static final double BLOCK = 0.3;
    public static final int MIN_DMG = 25;
    private static final int MAX_DMG = 45;
    private static final int MIN_HEAL = 25;
    private static final int MAX_HEAL = 45;

    public Priestess(final String theName) {
        super(theName, HP, MIN_DMG, MAX_DMG, SPD, HIT, BLOCK);
    }

    @Override
    public void useSpecialSkill(final DungeonCharacter theOpp) {
        heal();
    }

    public int heal() {
        return super.heal(randomInRange(MIN_HEAL, MAX_HEAL));
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
