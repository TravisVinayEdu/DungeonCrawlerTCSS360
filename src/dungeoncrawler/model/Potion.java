package dungeoncrawler.model;

import dungeoncrawler.model.characters.Hero;

import java.io.Serializable;
import java.util.Random;

/**
 * Base class for collectible potions.
 *
 * <p>A potion has a potency value determined either directly or by a random
 * inclusive range. Concrete potion types decide how that potency affects a
 * hero when applied.</p>
 */
public abstract class Potion implements Serializable {
    private static final Random RANDOM = new Random();
    private static final long serialVersionUID = 1L;

    private final int myPotency;

    /**
     * Creates a potion with a randomly generated potency.
     *
     * @param theMinPot minimum potency
     * @param theMaxPot maximum potency
     */
    public Potion(final int theMinPot, final int theMaxPot) {
        myPotency = genPotency(theMinPot, theMaxPot);
    }

    /**
     * Creates a potion with a fixed potency.
     *
     * @param thePotency fixed potency value
     */
    public Potion(final int thePotency) {
        myPotency = thePotency;
    }

    private int genPotency(int theMinPot, int theMaxPot) {
        if (theMinPot < 0 || theMaxPot < theMinPot) {
            throw new IllegalArgumentException("Potion potency range is invalid.");
        }
        return RANDOM.nextInt(theMaxPot - theMinPot + 1) + theMinPot;
    }

    /**
     * Applies this potion's effect to a hero.
     *
     * @param hero hero receiving the potion effect
     */
    public void apply(final Hero hero) { }

    /**
     * Returns this potion's potency value.
     *
     * @return potency value
     */
    public int getPotency() {
        return myPotency;
    }
}
