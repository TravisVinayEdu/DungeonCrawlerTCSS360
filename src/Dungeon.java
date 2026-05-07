import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Dungeon {
    private Room[][] maze;
    private Room[][] discoveredMaze;
    private final int myWidth;
    private final int myHeight;
    private Room entrance;
    private int heroRow;
    private int heroCol;

    private final int MONSTER_CHANCE = 40;

    public Dungeon(final int theWidth, final int theHeight) {
        myWidth = theWidth;
        myHeight = theHeight;
        maze = new Room[myHeight][myWidth];
        generateMaze();
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
            Room entrance = maze[RNG.nextInt(myHeight)][RNG.nextInt(myWidth)];
            entrance.setEntrance();
            boolean exitRoom = false;
            while (!exitRoom) {
                Room exit = maze[RNG.nextInt(myHeight)][RNG.nextInt(myWidth)];
                if (!exit.isEntrance())
                {
                    exit.setExit();
                    exitRoom = true;
                }
            }
            flag = isTraversable();
        }
        placePillars();
        placeMonsters();
    }

    private boolean isTraversable() {
        boolean[][] visited = new boolean[myHeight][myWidth];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{0, 0});
        visited[0][0] = true;
        int[] dRow = {-1, 0, 1, 0};
        int[] dCol = {0, 1, 0, -1};
        Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0], col = current[1];
            for (int d = 0; d < 4; d++) {
                int newRow = row + dRow[d];
                int newCol = col + dCol[d];
                if (newRow < 0 || newRow >= myHeight || newCol < 0 || newCol >= myWidth) {
                    continue;
                }
                if (maze[row][col].workingDoor(dirs[d]) && !visited[newRow][newCol]) {
                    visited[newRow][newCol] = true;
                    queue.add(new int[]{newRow, newCol});
                }
            }
        }
        for (int i = 0; i < myHeight; i++) {
            for (int j = 0; j < myWidth; j++) {
                if (!visited[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void placePillars() {
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

    private void placeMonsters() {
        Random RNG = new Random();
        for (int i = 0; i < myHeight; i++) {
            for (int j = 0; j < myWidth; j++) {
                if (maze[i][j].isEmpty())
                {
                    if (RNG.nextInt(100) >= MONSTER_CHANCE)
                    {

                    }
                }
            }
        }
    }

    public Room getRoom(int theR, int theC) {
        return maze[theR][theC];
    }

    public boolean moveHero(Direction d) {
        return maze[heroRow][heroCol].workingDoor(d);
    }

    public Room[][] getVisionGrid() {
        return discoveredMaze;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
