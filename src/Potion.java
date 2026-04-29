public abstract class Potion {
    private final int myPotency;

    public Potion(int theMinPot, int theMaxPot){
        myPotency = genPotency(theMinPot, theMaxPot);
    }
    public Potion(int thePotency){
        myPotency = thePotency;
    }
    private int genPotency(int theMinPot, int theMaxPot) {
        // Need to create a ran generator
        return 0;
    }
    public void apply(Hero hero){}
    public int getPotency() {
        return myPotency;
    }
}
