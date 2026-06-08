package dungeoncrawler.model.characters;

/**
 * A balanced monster with steady damage and moderate healing.
 */
public class Skeleton extends Monster {
    /** Database and display name for skeletons. */
    public static final String SKELETON_NAME = "Skeleton";

    /**
     * Creates a skeleton with the documented default statistics.
     */
    public Skeleton() {
        super(SKELETON_NAME, 100, 30, 50, 3, 0.8, 0.3, 30, 50);
    }

    /**
     * Creates a skeleton from database-provided statistics.
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
    public Skeleton(final int hp, final int minDmg, final int maxDmg,
                    final int attackSpd, final double hitChance,
                    final double healChance, final int minHeal, final int maxHeal) {
        super(SKELETON_NAME, hp, minDmg, maxDmg, attackSpd, hitChance,
                healChance, minHeal, maxHeal);
    }
}
