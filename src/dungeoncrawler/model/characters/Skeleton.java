package dungeoncrawler.model.characters;

/**
 * A balanced monster with steady damage and moderate healing.
 */
public class Skeleton extends Monster {
    public static final String SKELETON_NAME = "Skeleton";

    public Skeleton() {
        super(SKELETON_NAME, 100, 30, 50, 3, 0.8, 0.3, 30, 50);
    }

    public Skeleton(int hp, int minDmg, int maxDmg, int attackSpd,
                    double hitChance, double healChance, int minHeal, int maxHeal) {
        super(SKELETON_NAME, hp, minDmg, maxDmg, attackSpd, hitChance,
                healChance, minHeal, maxHeal);
    }
}
