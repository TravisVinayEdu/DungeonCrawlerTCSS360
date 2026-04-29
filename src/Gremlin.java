public class Gremlin extends Monster{
    public static String GREMLIN_NAME = "Gremlin";
    public static int HP = 70;
    public static int SPD = 5;
    public static double HIT = 0.8;
    public static int DMG; // Needs random implementation
    public static final int MIN_DMG = 15;
    private static final int MAX_DMG = 30;
    public Gremlin(){
        super(GREMLIN_NAME, HP, MIN_DMG, MAX_DMG, SPD, HIT);

    }
}
