package dungeoncrawler.model;

import dungeoncrawler.model.characters.Hero;

import java.io.Serializable;
import java.util.Random;

public abstract class Potion implements Serializable {
    private static final Random RANDOM = new Random();
    private static final long serialVersionUID = 1L;

    private final int myPotency;

    public Potion(int theMinPot, int theMaxPot){
        myPotency = genPotency(theMinPot, theMaxPot);
    }
    public Potion(int thePotency){
        myPotency = thePotency;
    }

    private int genPotency(int theMinPot, int theMaxPot) {
        if (theMinPot < 0 || theMaxPot < theMinPot) {
            throw new IllegalArgumentException("Potion potency range is invalid.");
        }
        return RANDOM.nextInt(theMaxPot - theMinPot + 1) + theMinPot;
    }

    public void apply(Hero hero){}

    public int getPotency() {
        return myPotency;
    }
}
