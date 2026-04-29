public class Priest extends Hero {
    public static int HP = 125;
    public static int SPD = 4;
    public static double HIT = 0.8;
    public static double BLOCK = 0.2;
    public static int DMG; // Needs random implementation
    public static final int MIN_DMG = 35;
    private static final int MAX_DMG = 60;

    public Priest(String theName){
        super(theName, HP, MIN_DMG, MAX_DMG, SPD, HIT);
    }
    public void heal(DungeonCharacter theOpp){}
}
