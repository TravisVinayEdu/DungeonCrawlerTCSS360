
public class Room {
    private boolean healingPotion;
    private boolean visionPotion;
    private boolean pit;
    private boolean isEntrance;
    private boolean isExit;
    private Pillar pillar;
    private Monster monsters;
    private boolean[] doors;
    private int myRow;
    private int myCol;

    public Room(int theRow, int theCol) {
        myRow = theRow;
        myCol = theCol;
    }

    public boolean hasItem(){return false;}
    public void removePotion(Potion potion){}
    public int fallInPit(){return -1;}
    public void setDoor(Direction d, boolean open){}
    public String toString(){return null;}
}
