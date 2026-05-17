package dungeoncrawler.model.characters;

/**
 * A high-health monster that hits hard and heals infrequently.
 */
public class Ogre extends Monster {
    public static final String OGRE_NAME = "Ogre";

    public Ogre() {
        super(OGRE_NAME, 200, 30, 60, 2, 0.6, 0.1, 30, 60);
    }

    // New constructor driven by DB values
    public Ogre(int hp, int minDmg, int maxDmg, int attackSpd,
                double hitChance, double healChance, int minHeal, int maxHeal) {
        super(OGRE_NAME, hp, minDmg, maxDmg, attackSpd, hitChance, healChance, minHeal, maxHeal);
    }
}
