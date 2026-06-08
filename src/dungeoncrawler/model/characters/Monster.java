package dungeoncrawler.model.characters;

/**
 * Base class for every monster in the dungeon.
 *
 * <p>Monsters share the normal combat behavior from {@link DungeonCharacter},
 * plus a chance to heal after taking damage. A monster only attempts to heal
 * when it survives the hit that damaged it.</p>
 */
public abstract class Monster extends DungeonCharacter {
    private final double myChanceToHeal;
    private final int myMinHeal;
    private final int myMaxHeal;

    /**
     * Creates a monster with combat and self-healing statistics.
     *
     * @param theName monster display name
     * @param theHitPoints maximum and starting hit points
     * @param theMinDmg minimum regular attack damage
     * @param theMaxDmg maximum regular attack damage
     * @param theAttackSpd attack speed used in battle calculations
     * @param theHitChance chance for regular attacks to hit
     * @param theChanceToHeal chance to heal after surviving damage
     * @param theMinHeal minimum self-heal amount
     * @param theMaxHeal maximum self-heal amount
     * @throws IllegalArgumentException if heal chance or heal range is invalid
     */
    protected Monster(final String theName,
                      final int theHitPoints,
                      final int theMinDmg,
                      final int theMaxDmg,
                      final int theAttackSpd,
                      final double theHitChance,
                      final double theChanceToHeal,
                      final int theMinHeal,
                      final int theMaxHeal) {
        super(theName, theHitPoints, theMinDmg, theMaxDmg, theAttackSpd, theHitChance);
        if (theChanceToHeal < 0.0 || theChanceToHeal > 1.0) {
            throw new IllegalArgumentException("Heal chance must be between 0 and 1.");
        }
        if (theMinHeal < 0 || theMaxHeal < theMinHeal) {
            throw new IllegalArgumentException("Heal range is invalid.");
        }
        myChanceToHeal = theChanceToHeal;
        myMinHeal = theMinHeal;
        myMaxHeal = theMaxHeal;
    }

    /**
     * Applies damage, then attempts monster healing if the monster survives.
     *
     * @param theDamage the incoming damage amount
     * @return the hit points actually lost before any healing occurs
     */
    @Override
    public int takeDamage(final int theDamage) {
        final int damageTaken = super.takeDamage(theDamage);
        if (damageTaken > 0 && !isFainted()) {
            heal();
        }
        return damageTaken;
    }

    /**
     * Attempts this monster's self-heal.
     *
     * @return the hit points restored, or 0 if the heal does not trigger
     */
    public int heal() {
        if (isFainted() || !chanceSucceeds(myChanceToHeal)) {
            return 0;
        }
        return super.heal(randomInRange(myMinHeal, myMaxHeal));
    }

    /**
     * Returns the monster's chance to heal after taking damage.
     *
     * @return heal chance from 0.0 to 1.0
     */
    public double getChanceToHeal() {
        return myChanceToHeal;
    }

    /**
     * Returns the minimum heal amount for this monster.
     *
     * @return minimum heal amount
     */
    public int getMinHeal() {
        return myMinHeal;
    }

    /**
     * Returns the maximum heal amount for this monster.
     *
     * @return maximum heal amount
     */
    public int getMaxHeal() {
        return myMaxHeal;
    }

    /**
     * Builds a compact monster status summary.
     *
     * @return formatted monster summary
     */
    @Override
    public String toString() {
        return getName()
                + " HP: " + getHitPoints() + "/" + getMaxHitPoints()
                + ", Damage: " + getMinDamage() + "-" + getMaxDamage()
                + ", Attack Speed: " + getAttackSpeed()
                + ", Hit Chance: " + Math.round(getHitChance() * 100) + "%"
                + ", Heal Chance: " + Math.round(myChanceToHeal * 100) + "%";
    }
}
