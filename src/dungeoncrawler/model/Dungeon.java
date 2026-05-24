package dungeoncrawler.model;

import dungeoncrawler.model.characters.Monster;
import dungeoncrawler.persistence.MonsterDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.io.Serializable;

public class Dungeon implements Serializable {

    /** Used for serialization. */
    private static final long serialVersionUID = 1L;

    /** Random object for deciding whether a feature is present. */
    private static final Random RANDOM = new Random();

    /** Chance of a monster being present in a room. */
    private static final int MONSTER_CHANCE = 40;

    private Room[][] maze;
    private Room[][] discoveredMaze;
    private final int myWidth;
    private final int myHeight;
    private Room entrance;
    private int heroRow;
    private int heroCol;

    /**
     * Constructor for the Dungeon class.
     * @param theWidth 1-based width of the dungeon
     * @param theHeight 1-based height of the dungeon
     * @param db MonsterDatabase object
     * @throws SQLException if there is a problem connecting to the database
     */
    public Dungeon(final int theWidth, final int theHeight, MonsterDatabase db) throws SQLException {
        if (theWidth <= 0 || theHeight <= 0) {
            throw new IllegalArgumentException("Dungeon dimensions must be positive.");
        }
        myWidth = theWidth;
        myHeight = theHeight;
        maze = new Room[myHeight][myWidth];
        discoveredMaze = new Room[myHeight][myWidth];
        generateMaze(db);
    }

    /**
     * Constructor for the Dungeon class. Used for loading a saved dungeon.
     * @param maze 2D array of Room objects
     * @param width 1-based width of the dungeon
     * @param height 1-based height of the dungeon
     * @param heroRow 1-based row number of the hero
     * @param heroCol 1-based column number of the hero
     * @param discovered The dungeon's vision grid.
     */
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

    /**
     * Generates the maze.
     * @param db MonsterDatabase object
     * @throws SQLException if there is a problem connecting to the database
     */
    private void generateMaze(MonsterDatabase db) throws SQLException {
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
        placeMonsters(db);
        discoverCurrentRoom();
    }

    /**
     * Carves the maze recursively.
     * @param theRow 1-based row number
     * @param theCol 1-based column number
     * @param theVisited 2D array of booleans indicating whether a room has been visited
     */
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

    /**
     * Places pillars in the maze.
     */
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

    /**
     * Places monsters in the maze, deprecated in favor of MonsterDatabase.
     */
    /*private void placeMonsters() {
        for (int i = 0; i < myHeight; i++) {
            for (int j = 0; j < myWidth; j++) {
                if (maze[i][j].isEmpty()
                        && RANDOM.nextInt(100) < MONSTER_CHANCE) {
                    maze[i][j].setMonsters();
                }
            }
        }
    }*/

    /**
     * Places monsters in the maze.
     * @param db MonsterDatabase object
     * @throws SQLException if there is a problem connecting to the database
     */
    private void placeMonsters(MonsterDatabase db) throws SQLException {
        for (int i = 0; i < myHeight; i++) {
            for (int j = 0; j < myWidth; j++) {
                if (maze[i][j].isEmpty() && RANDOM.nextInt(100) < MONSTER_CHANCE) {
                    maze[i][j].setMonsters(db);
                }
            }
        }
    }

    /**
     * Removes pits from the maze until all rooms are connected.
     */
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

    /**
     * Returns true if all non-pit rooms are connected.
     * @return true if all non-pit rooms are connected
     */
    private boolean nonPitRoomsStayConnected() {
        return countReachableNonPitRooms() == countNonPitRooms();
    }

    /**
     * Counts the number of reachable non-pit rooms.
     * @return the number of reachable non-pit rooms
     */
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

    /**
     * Counts the number of non-pit rooms.
     * @return the number of non-pit rooms
     */
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

    /**
     * Returns the room at the specified coordinates.
     * @param theR 1-based row number
     * @param theC 1-based column number
     * @return the room at the specified coordinates
     */
    public Room getRoom(int theR, int theC) {
        return maze[theR][theC];
    }

    /**
     * Returns the room the hero is currently in.
     * @return the room the hero is currently in
     */
    public Room getCurrentRoom() {
        return maze[heroRow][heroCol];
    }

    /**
     * Moves the hero in the specified direction.
     * @param d the direction to move the hero
     * @return true if the hero moved, false if the hero cannot move in that direction
     */
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

    /**
     * Returns the vision grid.
     * @return the vision grid
     */
    public Room[][] getVisionGrid() {
        return discoveredMaze;
    }

    /**
     * Returns true if the specified coordinates are in the vision grid.
     * @param theRow 1-based row number
     * @param theCol 1-based column number
     * @return true if the specified coordinates are in the vision grid
     */
    public boolean isDiscovered(final int theRow, final int theCol) {
        if (!inBounds(theRow, theCol)) {
            return false;
        }
        return discoveredMaze[theRow][theCol] != null;
    }

    /**
     * Gets the hero's row number.
     * @return the hero's row number
     */
    public int getHeroRow() {
        return heroRow;
    }

    /**
     * Gets the hero's column number.
     * @return the hero's column number
     */
    public int getHeroCol() {
        return heroCol;
    }

    /**
     * Gets the width of the dungeon.
     * @return the width of the dungeon
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * Gets the height of the dungeon.
     * @return the height of the dungeon
     */
    public int getHeight() {
        return myHeight;
    }

    /**
     * Returns a string representation of the dungeon.
     * @return a string representation of the dungeon
     */
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

    /**
     * Discovers the current room.
     */
    private void discoverCurrentRoom() {
        discoveredMaze[heroRow][heroCol] = maze[heroRow][heroCol];
    }

    /**
     * Returns true if the specified coordinates are in the maze.
     * @param theRow 1-based row number
     * @param theCol 1-based column number
     * @return true if the specified coordinates are in the maze
     */
    private boolean inBounds(final int theRow, final int theCol) {
        return theRow >= 0 && theRow < myHeight
                && theCol >= 0 && theCol < myWidth;
    }

    /**
     * Returns the delta for moving in the specified direction.
     * @param theDirection the direction to move in
     * @return the delta for moving in the specified direction
     */
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

    /**
     * Returns the delta for moving in the specified direction.
     * @param theDirection the direction to move in
     * @return the delta for moving in the specified direction
     */
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

    /**
     * Returns the opposite direction of the specified direction.
     * @param theDirection the direction to get the opposite of
     * @return the opposite direction of the specified direction
     */
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
