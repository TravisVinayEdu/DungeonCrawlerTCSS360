import java.util.EnumSet;
import java.util.Set;

public abstract class Hero extends DungeonCharacter {
    private final double myChanceToBlock;
    private int myHealingPotions;
    private int myVisionPotions;
    private final EnumSet<Pillar> myPillars;

    public Hero(String theName,
                int theHitPoints,
                int theMinDmg,
                int theMaxDmg,
                int theAttackSpd,
                double theHitChance,
                double theChanceToBlock) {
        super(theName, theHitPoints, theMinDmg, theMaxDmg, theAttackSpd, theHitChance);
        if (theChanceToBlock < 0.0 || theChanceToBlock > 1.0) {
            throw new IllegalArgumentException("Block chance must be between 0 and 1.");
        }
        myChanceToBlock = theChanceToBlock;
        myPillars = EnumSet.noneOf(Pillar.class);
    }

    public boolean block() {
        return chanceSucceeds(myChanceToBlock);
    }

    @Override
    public int defendAgainstAttack(final int theDamage) {
        if (theDamage > 0 && block()) {
            return 0;
        }
        return takeDamage(theDamage);
    }

    @Override
    public int attacksPerRoundAgainst(final DungeonCharacter theOpponent) {
        final int heroAttacks = Math.max(1, super.attacksPerRoundAgainst(theOpponent));
        if (theOpponent instanceof Monster) {
            return Math.max(heroAttacks, theOpponent.attacksPerRoundAgainst(this));
        }
        return heroAttacks;
    }

    public abstract void useSpecialSkill(DungeonCharacter theOpp);

    public void usePotion(final Potion thePotion) {
        if (thePotion == null) {
            return;
        }
        if (thePotion instanceof HealingPotion && myHealingPotions > 0) {
            myHealingPotions--;
            thePotion.apply(this);
        } else if (thePotion instanceof VisionPotion && myVisionPotions > 0) {
            myVisionPotions--;
            thePotion.apply(this);
        }
    }

    public void addHealingPotion() {
        myHealingPotions++;
    }

    public void addVisionPotion() {
        myVisionPotions++;
    }

    public int getHealingPotions() {
        return myHealingPotions;
    }

    public int getVisionPotions() {
        return myVisionPotions;
    }

    public double getChanceToBlock() {
        return myChanceToBlock;
    }

    public void addPillar(final Pillar thePillar) {
        if (thePillar != null) {
            myPillars.add(thePillar);
        }
    }

    public boolean hasPillar(final Pillar thePillar) {
        return myPillars.contains(thePillar);
    }

    public boolean hasAllPillars() {
        return myPillars.containsAll(EnumSet.allOf(Pillar.class));
    }

    public Set<Pillar> getPillars() {
        return EnumSet.copyOf(myPillars);
    }

    @Override
    public String toString() {
        return "Name: " + getName()
                + System.lineSeparator() + "Hit Points: " + getHitPoints()
                + System.lineSeparator() + "Total Healing Potions: " + myHealingPotions
                + System.lineSeparator() + "Total Vision Potions: " + myVisionPotions
                + System.lineSeparator() + "Pillar Pieces Found: " + myPillars;
    }

}
