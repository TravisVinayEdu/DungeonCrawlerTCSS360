package dungeoncrawler.model;

import dungeoncrawler.model.characters.Hero;

/**
 * Potion that restores hero hit points when consumed.
 */
public class HealingPotion extends Potion {
    /** Minimum hit points restored by a healing potion. */
    public final static int MIN_HEAL = 15;
    /** Maximum hit points restored by a healing potion. */
    public final static int MAX_HEAL = 30;

    /**
     * Creates a healing potion with random healing potency.
     */
    public HealingPotion() {
        super(MIN_HEAL, MAX_HEAL);
    }

    /**
     * Restores hit points to the supplied hero.
     *
     * @param theHero hero to heal; ignored when null
     */
    @Override
    public void apply(final Hero theHero) {
        if (theHero != null) {
            theHero.heal(getPotency());
        }
    }
}
