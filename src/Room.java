
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
        doors = new boolean[4];
        for (int i = 0; i < 4; i++)
        {
            doors[i] = false;
        }
    }

    public boolean isEmpty()
    {
        if (!healingPotion && !visionPotion
        && !pit && !isEntrance && !isExit
        && monsters == null)
        {
            return true;
        }
        return false;
    }

    public boolean hasItem() {
        return healingPotion || visionPotion;
    }
    public void removePotion(Potion potion) {
        healingPotion = false;
        visionPotion = false;
    }
    public int fallInPit(){return -1;}

    public void setPillar(Pillar p) {
        pillar = p;
    }

    public void setDoor(Direction d, boolean open) {
        if (d == Direction.NORTH)
        {
            doors[0] = open;
        }
        if (d == Direction.EAST)
        {
            doors[1] = open;
        }
        if (d == Direction.SOUTH)
        {
            doors[2] = open;
        }
        else
        {
            doors[3] = open;
        }
    }

    public boolean isEscapable()
    {
        if ((!doors[0]) && (!doors[1])
                && (!doors[2]) && (!doors[3]))
        {
            return false;
        }
        return true;
    }

    public String toString() {
        return null;
    }
}
