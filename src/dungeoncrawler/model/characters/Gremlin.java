package dungeoncrawler.model.characters;

/**
 * A fast monster with modest damage and a strong chance to recover.
 */
public class Gremlin extends Monster {
    /** Database and display name for gremlins. */
    public static final String GREMLIN_NAME = "Gremlin";

    /**
     * Creates a gremlin with the documented default statistics.
     */
    public Gremlin() {
        super(GREMLIN_NAME, 70, 15, 30, 5, 0.8, 0.4, 20, 40);
    }

    /**
     * Creates a gremlin from database-provided statistics.
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
    public Gremlin(final int hp, final int minDmg, final int maxDmg,
                   final int attackSpd, final double hitChance,
                   final double healChance, final int minHeal, final int maxHeal) {
        super(GREMLIN_NAME, hp, minDmg, maxDmg, attackSpd, hitChance,
                healChance, minHeal, maxHeal);
    }
}
