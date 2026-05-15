package dungeoncrawler.model.characters;

import java.io.Serializable;
import java.util.Random;

public abstract class DungeonCharacter implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Random RANDOM = new Random();

    private final String myName;
    private final int myMaxHitPoints;
    private int myHitPoints;
    private final int myMinDmg;
    private final int myMaxDmg;
    private final int myAttackSpd;
    private final double myHitChance;

    protected DungeonCharacter(String theName,
                               int theHitPoints,
                               int theMinDmg,
                               int theMaxDmg,
                               int theAttackSpd,
                               double theHitChance) {
        if (theName == null || theName.trim().isEmpty()) {
            throw new IllegalArgumentException("Character name cannot be blank.");
        }
        if (theHitPoints <= 0) {
            throw new IllegalArgumentException("Hit points must be positive.");
        }
        if (theMinDmg < 0 || theMaxDmg < theMinDmg) {
            throw new IllegalArgumentException("Damage range is invalid.");
        }
        if (theAttackSpd <= 0) {
            throw new IllegalArgumentException("Attack speed must be positive.");
        }
        if (theHitChance < 0.0 || theHitChance > 1.0) {
            throw new IllegalArgumentException("Hit chance must be between 0 and 1.");
        }

        myName = theName.trim();
        myMaxHitPoints = theHitPoints;
        myHitPoints = theHitPoints;
        myMinDmg = theMinDmg;
        myMaxDmg = theMaxDmg;
        myAttackSpd = theAttackSpd;
        myHitChance = theHitChance;
    }

    public String getName() {
        return myName;
    }

    public int getMyHitPoints() {
        return myHitPoints;
    }

    public int getHitPoints() {
        return myHitPoints;
    }

    public int getMaxHitPoints() {
        return myMaxHitPoints;
    }

    public void setMyHitPoints(final int theHitPoints) {
        myHitPoints = Math.max(0, Math.min(theHitPoints, myMaxHitPoints));
    }

    public int getMinDamage() {
        return myMinDmg;
    }

    public int getMaxDamage() {
        return myMaxDmg;
    }

    public int getAttackSpeed() {
        return myAttackSpd;
    }

    public double getHitChance() {
        return myHitChance;
    }

    public void attack(final DungeonCharacter theOpponent) {
        if (theOpponent == null || theOpponent.isFainted()) {
            return;
        }
        if (RANDOM.nextDouble() <= myHitChance) {
            theOpponent.defendAgainstAttack(generateDamage());
        }
    }

    public int takeDamage(final int theDamage) {
        final int before = myHitPoints;
        setMyHitPoints(myHitPoints - Math.max(0, theDamage));
        return before - myHitPoints;
    }

    public int defendAgainstAttack(final int theDamage) {
        return takeDamage(theDamage);
    }

    public int heal(final int theAmount) {
        final int before = myHitPoints;
        setMyHitPoints(myHitPoints + Math.max(0, theAmount));
        return myHitPoints - before;
    }

    public boolean isFainted() {
        return myHitPoints <= 0;
    }

    public int attacksPerRoundAgainst(final DungeonCharacter theOpponent) {
        if (theOpponent == null) {
            return 1;
        }
        return Math.max(1, myAttackSpd / theOpponent.getAttackSpeed());
    }

    protected int generateDamage() {
        return randomInRange(myMinDmg, myMaxDmg);
    }

    protected static int randomInRange(final int theMin, final int theMax) {
        if (theMax < theMin) {
            throw new IllegalArgumentException("Random range is invalid.");
        }
        return RANDOM.nextInt(theMax - theMin + 1) + theMin;
    }

    protected static boolean chanceSucceeds(final double theChance) {
        if (theChance < 0.0 || theChance > 1.0) {
            throw new IllegalArgumentException("Chance must be between 0 and 1.");
        }
        return RANDOM.nextDouble() <= theChance;
    }

    protected static double randomDouble() {
        return RANDOM.nextDouble();
    }
}
