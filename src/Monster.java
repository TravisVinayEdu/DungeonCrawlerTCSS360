public abstract class Monster extends DungeonCharacter {
    private double chanceToHeal;
    private int minHeal;
    private int maxHeal;
    public Monster(String theName,
                   int theHitPoints,
                   int theMinDmg,
                   int theMaxDmg,
                   int theAttackSpd,
                   double theHitChance) {
        super(theName, theHitPoints, theMinDmg, theMaxDmg, theAttackSpd, theHitChance);
    }
    public void heal(){}
    public double getChanceToHeal(){return 0;}


}
