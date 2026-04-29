public class Skeleton extends Monster{
    public static String SKELETON_NAME = "Skeleton";
    public static int HP = 100;
    public static int SPD = 3;
    public static double HIT = 0.8;
    public static int DMG; // Needs random implementation
    public static final int MIN_DMG = 30;
    private static final int MAX_DMG = 50;
    public Skeleton(){
        super(SKELETON_NAME, HP, MIN_DMG, MAX_DMG, SPD, HIT);

    }
}
