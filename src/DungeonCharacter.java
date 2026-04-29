public abstract class DungeonCharacter {
    private String myName;
    private int myHitPoints;
    private int myMinDmg;
    private int myMaxDmg;
    private int myAttackSpd;
    private double myHitChance;

    public DungeonCharacter(String theName,
                            int theHitPoints,
                            int theMinDmg,
                            int theMaxDmg,
                            int theAttackSpd,
                            double theHitChance) {
        myName = theName;
        myHitPoints = theHitPoints;
        myMinDmg = theMinDmg;
        myMaxDmg = theMaxDmg;
        myAttackSpd = theAttackSpd;
        myHitChance = theHitChance;
    }

    public String getName() {
        return myName;
    }
    public int getMyHitPoints(){
        return myHitPoints;
    }
    public void setMyHitPoints(int hp) {
        myHitPoints = hp;
    }
    public void attack(DungeonCharacter opp){}
    public boolean isFainted(){return false;}

}
