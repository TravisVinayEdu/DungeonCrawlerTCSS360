package dungeoncrawler.model;

import dungeoncrawler.model.characters.Hero;

public class HealingPotion extends Potion{
    public final static int MIN_HEAL = 15;
    public final static int MAX_HEAL = 30;

    public HealingPotion(){
        super(MIN_HEAL, MAX_HEAL);
    }

    @Override
    public void apply(final Hero theHero) {
        if (theHero != null) {
            theHero.heal(getPotency());
        }
    }
}
