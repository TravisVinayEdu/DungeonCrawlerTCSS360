import java.util.Random;

public class Dungeon {
    private Room[][] maze;
    private Room[][] discoveredMaze;
    private final int myWidth;
    private final int myHeight;
    private Room entrance;
    private int heroRow;
    private int heroCol;

    public Dungeon(final int theWidth, final int theHeight) {
        myWidth = theWidth;
        myHeight = theHeight;
    }

    private void generateMaze() {
        Random RNG = new Random();
        boolean flag = false;
        while (!flag) {
            for (int i = 0; i < myHeight; i++) {
                for (int j = 0; j < myWidth; j++) {
                    Room temp = new Room(i, j);
                    while (!temp.isEscapable()) {
                        for (int direction = 0; direction < 4; direction++) {
                            int doorChance = RNG.nextInt(3);
                            if (doorChance != 0) {
                                if (direction == 0)
                                {
                                    temp.setDoor(Direction.NORTH, true);
                                }
                                if (direction == 1)
                                {
                                    temp.setDoor(Direction.EAST, true);
                                }
                                if (direction == 2)
                                {
                                    temp.setDoor(Direction.SOUTH, true);
                                }
                                else
                                {
                                    temp.setDoor(Direction.WEST, true);
                                }
                            }
                        }
                    }
                    maze[i][j] = temp;
                }
            }
            flag = isTraversable();
        }
        placePillars();
    }
    private boolean isTraversable(){return false;}

    private void placePillars() {
        int pillars = 0;
        Random RNG = new Random();
        boolean A = false;
        boolean E = false;
        boolean I = false;
        boolean P = false;
        while (!A && !E
        && !I && !P)
        {
            Room temp = maze[RNG.nextInt(myHeight)][RNG.nextInt(myWidth)];
            if (temp.isEmpty() && !A)
            {
                temp.setPillar(Pillar.ABSTRACTION);
                A = true;
            }
            else if (temp.isEmpty() && !E)
            {
                temp.setPillar(Pillar.ENCAPSULATION);
                E = true;
            }
            else if (temp.isEmpty() && !I)
            {
                temp.setPillar(Pillar.INHERITANCE);
                I = true;
            }
            else if (temp.isEmpty() && !P)
            {
                temp.setPillar(Pillar.POLYMORPHISM);
                P = true;
            }
        }
    }
    private void placeMonsters(){}


    public Room getRoom(int theR, int theC) {
        return maze[theR][theC];
    }
    public boolean moveHero(Direction d) {
        return false;
    }
    public Room[][] getVisionGrid() {
        return discoveredMaze;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
