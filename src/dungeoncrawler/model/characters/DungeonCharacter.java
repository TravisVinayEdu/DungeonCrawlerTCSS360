package dungeoncrawler.model.characters;

import java.io.Serializable;
import java.util.Random;

/**
 * Base type for every combatant that can participate in dungeon battles.
 *
 * <p>The class stores the shared combat statistics required by the assignment:
 * name, hit points, damage range, attack speed, and chance to hit. It also
 * supplies common attack, damage, healing, and random-roll helpers used by
 * heroes and monsters.</p>
 */
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

    /**
     * Creates a dungeon character with the supplied combat statistics.
     *
     * @param theName display name for the character
     * @param theHitPoints maximum and starting hit points
     * @param theMinDmg minimum attack damage
     * @param theMaxDmg maximum attack damage
     * @param theAttackSpd attack speed used to calculate attacks per round
     * @param theHitChance chance for a regular attack to hit, from 0.0 to 1.0
     * @throws IllegalArgumentException if any statistic is outside its valid range
     */
    protected DungeonCharacter(final String theName,
                               final int theHitPoints,
                               final int theMinDmg,
                               final int theMaxDmg,
                               final int theAttackSpd,
                               final double theHitChance) {
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

    /**
     * Returns the character's display name.
     *
     * @return character name
     */
    public String getName() {
        return myName;
    }

    /**
     * Returns the character's current hit points.
     *
     * @return current hit points
     */
    public int getMyHitPoints() {
        return myHitPoints;
    }

    /**
     * Returns the character's current hit points.
     *
     * @return current hit points
     */
    public int getHitPoints() {
        return myHitPoints;
    }

    /**
     * Returns the maximum hit points this character can have.
     *
     * @return maximum hit points
     */
    public int getMaxHitPoints() {
        return myMaxHitPoints;
    }

    /**
     * Sets current hit points, clamped to the range from 0 to max HP.
     *
     * @param theHitPoints requested current hit points
     */
    public void setMyHitPoints(final int theHitPoints) {
        myHitPoints = Math.max(0, Math.min(theHitPoints, myMaxHitPoints));
    }

    /**
     * Returns the minimum damage for a regular attack.
     *
     * @return minimum damage
     */
    public int getMinDamage() {
        return myMinDmg;
    }

    /**
     * Returns the maximum damage for a regular attack.
     *
     * @return maximum damage
     */
    public int getMaxDamage() {
        return myMaxDmg;
    }

    /**
     * Returns the attack speed used for attacks-per-round calculations.
     *
     * @return attack speed
     */
    public int getAttackSpeed() {
        return myAttackSpd;
    }

    /**
     * Returns this character's chance to land a regular attack.
     *
     * @return hit chance from 0.0 to 1.0
     */
    public double getHitChance() {
        return myHitChance;
    }

    /**
     * Attempts a regular attack against an opponent.
     *
     * <p>If the hit roll succeeds, random damage in this character's damage
     * range is generated and offered to the opponent's defense method.</p>
     *
     * @param theOpponent target character; ignored if null or already fainted
     */
    public void attack(final DungeonCharacter theOpponent) {
        if (theOpponent == null || theOpponent.isFainted()) {
            return;
        }
        if (RANDOM.nextDouble() <= myHitChance) {
            theOpponent.defendAgainstAttack(generateDamage());
        }
    }

    /**
     * Applies direct damage to this character.
     *
     * @param theDamage requested damage amount
     * @return actual hit points lost after clamping
     */
    public int takeDamage(final int theDamage) {
        final int before = myHitPoints;
        setMyHitPoints(myHitPoints - Math.max(0, theDamage));
        return before - myHitPoints;
    }

    /**
     * Handles incoming attack damage.
     *
     * <p>The base implementation simply takes damage. Subclasses such as
     * {@link Hero} may override this to block or otherwise modify damage.</p>
     *
     * @param theDamage incoming attack damage
     * @return actual hit points lost
     */
    public int defendAgainstAttack(final int theDamage) {
        return takeDamage(theDamage);
    }

    /**
     * Restores hit points without exceeding the character's maximum HP.
     *
     * @param theAmount requested healing amount
     * @return actual hit points restored
     */
    public int heal(final int theAmount) {
        final int before = myHitPoints;
        setMyHitPoints(myHitPoints + Math.max(0, theAmount));
        return myHitPoints - before;
    }

    /**
     * Reports whether this character has no remaining hit points.
     *
     * @return true when current hit points are zero
     */
    public boolean isFainted() {
        return myHitPoints <= 0;
    }

    /**
     * Calculates how many attacks this character receives against an opponent.
     *
     * @param theOpponent opponent whose speed is compared against this one
     * @return at least one attack per round
     */
    public int attacksPerRoundAgainst(final DungeonCharacter theOpponent) {
        if (theOpponent == null) {
            return 1;
        }
        return Math.max(1, myAttackSpd / theOpponent.getAttackSpeed());
    }

    /**
     * Generates regular attack damage within this character's damage range.
     *
     * @return generated damage amount
     */
    protected int generateDamage() {
        return randomInRange(myMinDmg, myMaxDmg);
    }

    /**
     * Generates a random integer within an inclusive range.
     *
     * @param theMin minimum allowed value
     * @param theMax maximum allowed value
     * @return random value between the minimum and maximum
     * @throws IllegalArgumentException if the range is inverted
     */
    protected static int randomInRange(final int theMin, final int theMax) {
        if (theMax < theMin) {
            throw new IllegalArgumentException("Random range is invalid.");
        }
        return RANDOM.nextInt(theMax - theMin + 1) + theMin;
    }

    /**
     * Tests a probability roll.
     *
     * @param theChance chance of success from 0.0 to 1.0
     * @return true if the random roll succeeds
     * @throws IllegalArgumentException if the chance is outside 0.0 to 1.0
     */
    protected static boolean chanceSucceeds(final double theChance) {
        if (theChance < 0.0 || theChance > 1.0) {
            throw new IllegalArgumentException("Chance must be between 0 and 1.");
        }
        return RANDOM.nextDouble() <= theChance;
    }

    /**
     * Returns a random double for subclasses that need custom probability logic.
     *
     * @return random value in the range [0.0, 1.0)
     */
    protected static double randomDouble() {
        return RANDOM.nextDouble();
    }
}
