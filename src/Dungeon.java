public class Dungeon {
    private Room[][] maze;
    private final int myWidth;
    private final int myHeight;
    private Room entrance;
    private int heroRow;
    private int heroCol;

    public Dungeon(final int theWidth, final int theHeight) {
        myWidth = theWidth;
        myHeight = theHeight;
    }

    private void generateMaze(){}
    private boolean isTraversable(){return false;}
    private void placePillars(){}
    private void placeMonsters(){}


    public Room getRoom(int theR, int theC){return null;}
    public boolean moveHero(Direction d){return false;}
    public Room[][] getVisionGrid(){return null;}

    @Override
    public String toString() {
        return super.toString();
    }
}
