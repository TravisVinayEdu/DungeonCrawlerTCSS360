public class Warrior extends Hero{

    public static final int HP = 125;
    public static final int SPD = 4;
    public static final double HIT = 0.8;
    public static final double BLOCK = 0.2;
    public static final int MIN_DMG = 35;
    private static final int MAX_DMG = 60;
    private static final double CRUSHING_BLOW_CHANCE = 0.4;
    private static final int MIN_CRUSHING_BLOW_DMG = 75;
    private static final int MAX_CRUSHING_BLOW_DMG = 175;

    public Warrior(String theName){
        super(theName, HP, MIN_DMG, MAX_DMG, SPD, HIT, BLOCK);
    }

    @Override
    public void useSpecialSkill(final DungeonCharacter theOpp) {
        crushingBlow(theOpp);
    }

    public void crushingBlow(final DungeonCharacter theOpp) {
        if (theOpp == null || theOpp.isFainted()) {
            return;
        }
        if (chanceSucceeds(CRUSHING_BLOW_CHANCE)) {
            theOpp.takeDamage(randomInRange(MIN_CRUSHING_BLOW_DMG, MAX_CRUSHING_BLOW_DMG));
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
