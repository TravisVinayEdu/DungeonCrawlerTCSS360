public class Ogre extends Monster{
    public static String ORGRE_NAME = "Ogre";
    public static int HP = 200;
    public static int SPD = 2;
    public static double HIT = 0.6;
    public static int DMG; // Needs random implementation
    public static final int MIN_DMG = 30;
    private static final int MAX_DMG = 60;
    public Ogre(){
        super(ORGRE_NAME, HP, MIN_DMG, MAX_DMG, SPD, HIT);

    }
}
