public abstract class Hero extends DungeonCharacter {
    private double myChanceToBlock;
    private int myHealingPotions;
    private int myVisionPotions;
    private Set<Pillar> myPillars;

    public Hero(String theName,
                int theHitPoints,
                int theMinDmg,
                int theMaxDmg,
                int theAttackSpd,
                double theHitChance) {
        super(theName, theHitPoints, theMinDmg, theMaxDmg, theAttackSpd, theHitChance);
    }
    public boolean block(){return false;}
    public void useSpecialSkill(DungeonCharacter theOpp){}
    public void usePotion(Potion thePotion){}
    public void addPillar(Pillar thePillar){}
    public String toString(){return null;}

}
