package dungeoncrawler.model;

import dungeoncrawler.model.characters.Monster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Dungeon {
    private static final Random RANDOM = new Random();

    private Room[][] maze;
    private Room[][] discoveredMaze;
    private final int myWidth;
    private final int myHeight;
    private Room entrance;
    private int heroRow;
    private int heroCol;

    private static final int MONSTER_CHANCE = 40;

    public Dungeon(final int theWidth, final int theHeight) {
        if (theWidth <= 0 || theHeight <= 0) {
            throw new IllegalArgumentException("Dungeon dimensions must be positive.");
        }
        myWidth = theWidth;
        myHeight = theHeight;
        maze = new Room[myHeight][myWidth];
        discoveredMaze = new Room[myHeight][myWidth];
        generateMaze();
    }

    // used for loading in a saved game
    public Dungeon(Room[][] maze, int width, int height,
            int heroRow, int heroCol, boolean[][] discovered) {
        this.myWidth  = width;
        this.myHeight = height;
        this.maze     = maze;
        this.heroRow  = heroRow;
        this.heroCol  = heroCol;
        this.discoveredMaze = new Room[height][width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (discovered[r][c]) {
                    this.discoveredMaze[r][c] = maze[r][c];
                }
            }
        }
        this.entrance = maze[0][0];
    }

    private void generateMaze() {
        for (int row = 0; row < myHeight; row++) {
            for (int col = 0; col < myWidth; col++) {
                maze[row][col] = new Room(row, col);
            }
        }
        carveMaze(0, 0, new boolean[myHeight][myWidth]);

        entrance = maze[0][0];
        entrance.setEntrance();
        heroRow = entrance.getRow();
        heroCol = entrance.getCol();

        Room exit = maze[myHeight - 1][myWidth - 1];
        exit.setExit();

        removeUnavoidablePits();
        placePillars();
        placeMonsters();
        discoverCurrentRoom();
    }

    private void carveMaze(final int theRow,
                           final int theCol,
                           final boolean[][] theVisited) {
        theVisited[theRow][theCol] = true;

        List<Direction> directions = new ArrayList<>();
        Collections.addAll(directions, Direction.NORTH, Direction.EAST,
                Direction.SOUTH, Direction.WEST);
        Collections.shuffle(directions);

        for (Direction direction : directions) {
            int nextRow = theRow + rowDelta(direction);
            int nextCol = theCol + colDelta(direction);
            if (!inBounds(nextRow, nextCol) || theVisited[nextRow][nextCol]) {
                continue;
            }
            maze[theRow][theCol].setDoor(direction, true);
            maze[nextRow][nextCol].setDoor(opposite(direction), true);
            carveMaze(nextRow, nextCol, theVisited);
        }
    }

    private void placePillars() {
        for (Pillar pillar : Pillar.values()) {
            boolean placed = false;
            while (!placed) {
                Room room = maze[RANDOM.nextInt(myHeight)][RANDOM.nextInt(myWidth)];
                if (room.isEmpty()) {
                    room.setPillar(pillar);
                    placed = true;
                }
            }
        }
    }

    private void placeMonsters() {
        for (int i = 0; i < myHeight; i++) {
            for (int j = 0; j < myWidth; j++) {
                if (maze[i][j].isEmpty()
                        && RANDOM.nextInt(100) < MONSTER_CHANCE) {
                    maze[i][j].setMonsters();
                }
            }
        }
    }

    private void removeUnavoidablePits() {
        while (!nonPitRoomsStayConnected()) {
            Room pitToRemove = null;
            int bestReachableCount = -1;
            for (int row = 0; row < myHeight; row++) {
                for (int col = 0; col < myWidth; col++) {
                    Room room = maze[row][col];
                    if (room.hasPit()) {
                        room.removePit();
                        int reachableCount = countReachableNonPitRooms();
                        if (reachableCount > bestReachableCount) {
                            bestReachableCount = reachableCount;
                            pitToRemove = room;
                        }
                        room.setPit();
                    }
                }
            }
            if (pitToRemove == null) {
                return;
            }
            pitToRemove.removePit();
        }
    }

    private boolean nonPitRoomsStayConnected() {
        return countReachableNonPitRooms() == countNonPitRooms();
    }

    private int countReachableNonPitRooms() {
        boolean[][] visited = new boolean[myHeight][myWidth];
        Queue<int[]> queue = new LinkedList<>();

        if (entrance.hasPit()) {
            return 0;
        }
        queue.add(new int[]{entrance.getRow(), entrance.getCol()});
        visited[entrance.getRow()][entrance.getCol()] = true;
        int reachableCount = 1;

        Direction[] directions = {Direction.NORTH, Direction.EAST,
                Direction.SOUTH, Direction.WEST};
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0];
            int col = current[1];
            for (Direction direction : directions) {
                if (!maze[row][col].workingDoor(direction)) {
                    continue;
                }
                int nextRow = row + rowDelta(direction);
                int nextCol = col + colDelta(direction);
                if (!inBounds(nextRow, nextCol)
                        || visited[nextRow][nextCol]
                        || maze[nextRow][nextCol].hasPit()) {
                    continue;
                }
                visited[nextRow][nextCol] = true;
                reachableCount++;
                queue.add(new int[]{nextRow, nextCol});
            }
        }

        return reachableCount;
    }

    private int countNonPitRooms() {
        int count = 0;
        for (int row = 0; row < myHeight; row++) {
            for (int col = 0; col < myWidth; col++) {
                if (!maze[row][col].hasPit()) {
                    count++;
                }
            }
        }
        return count;
    }

    public Room getRoom(int theR, int theC) {
        return maze[theR][theC];
    }

    public Room getCurrentRoom() {
        return maze[heroRow][heroCol];
    }

    public boolean moveHero(Direction d) {
        if (!maze[heroRow][heroCol].workingDoor(d)) {
            return false;
        }

        int newRow = heroRow + rowDelta(d);
        int newCol = heroCol + colDelta(d);
        if (!inBounds(newRow, newCol)) {
            return false;
        }

        heroRow = newRow;
        heroCol = newCol;
        discoverCurrentRoom();
        return true;
    }

    public Room[][] getVisionGrid() {
        return discoveredMaze;
    }

    public boolean isDiscovered(final int theRow, final int theCol) {
        if (!inBounds(theRow, theCol)) {
            return false;
        }
        return discoveredMaze[theRow][theCol] != null;
    }

    public int getHeroRow() {
        return heroRow;
    }

    public int getHeroCol() {
        return heroCol;
    }

    public int getWidth() {
        return myWidth;
    }

    public int getHeight() {
        return myHeight;
    }

    @Override
    public String toString() {
        String result = "";
        for (int row = 0; row < myHeight; row++) {
            for (int col = 0; col < myWidth; col++) {
                result += maze[row][col].toString();
                if (col < myWidth - 1) {
                    result += " ";
                }
            }
            result += System.lineSeparator();
        }
        return result;
    }

    private void discoverCurrentRoom() {
        discoveredMaze[heroRow][heroCol] = maze[heroRow][heroCol];
    }

    private boolean inBounds(final int theRow, final int theCol) {
        return theRow >= 0 && theRow < myHeight
                && theCol >= 0 && theCol < myWidth;
    }

    private int rowDelta(final Direction theDirection) {
        switch (theDirection) {
            case NORTH:
                return -1;
            case SOUTH:
                return 1;
            default:
                return 0;
        }
    }

    private int colDelta(final Direction theDirection) {
        switch (theDirection) {
            case EAST:
                return 1;
            case WEST:
                return -1;
            default:
                return 0;
        }
    }

    private Direction opposite(final Direction theDirection) {
        switch (theDirection) {
            case NORTH:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.NORTH;
            case EAST:
                return Direction.WEST;
            case WEST:
                return Direction.EAST;
            default:
                throw new IllegalArgumentException("Unknown direction: " + theDirection);
        }
    }
}
