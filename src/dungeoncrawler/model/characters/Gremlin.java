package dungeoncrawler.model.characters;

/**
 * A fast monster with modest damage and a strong chance to recover.
 */
public class Gremlin extends Monster {
    public static final String GREMLIN_NAME = "Gremlin";

    public Gremlin() {
        super(GREMLIN_NAME, 70, 15, 30, 5, 0.8, 0.4, 20, 40);
    }

    public Gremlin(int hp, int minDmg, int maxDmg, int attackSpd,
                   double hitChance, double healChance, int minHeal, int maxHeal) {
        super(GREMLIN_NAME, hp, minDmg, maxDmg, attackSpd, hitChance,
                healChance, minHeal, maxHeal);
    }
}
