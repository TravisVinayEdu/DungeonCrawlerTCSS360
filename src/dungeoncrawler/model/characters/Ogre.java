package dungeoncrawler.model.characters;

/**
 * A high-health monster that hits hard and heals infrequently.
 */
public class Ogre extends Monster {
    /** Database and display name for ogres. */
    public static final String OGRE_NAME = "Ogre";

    /**
     * Creates an ogre with the documented default statistics.
     */
    public Ogre() {
        super(OGRE_NAME, 200, 30, 60, 2, 0.6, 0.1, 30, 60);
    }

    /**
     * Creates an ogre from database-provided statistics.
     *
     * @param hp maximum and starting hit points
     * @param minDmg minimum regular attack damage
     * @param maxDmg maximum regular attack damage
     * @param attackSpd attack speed
     * @param hitChance chance to hit
     * @param healChance chance to heal after taking damage
     * @param minHeal minimum heal amount
     * @param maxHeal maximum heal amount
     */
    public Ogre(final int hp, final int minDmg, final int maxDmg,
                final int attackSpd, final double hitChance,
                final double healChance, final int minHeal, final int maxHeal) {
        super(OGRE_NAME, hp, minDmg, maxDmg, attackSpd, hitChance, healChance, minHeal, maxHeal);
    }
}
