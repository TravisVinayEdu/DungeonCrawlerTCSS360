package dungeoncrawler.model;

import dungeoncrawler.model.characters.Gremlin;
import dungeoncrawler.model.characters.Monster;
import dungeoncrawler.model.characters.Ogre;
import dungeoncrawler.model.characters.Skeleton;

import java.io.Serializable;
import java.util.Random;

/**
 * Represents a single location in the dungeon.
 *
 * <p>A room may contain potions, a pit, one pillar piece, a monster, and doors
 * to adjacent rooms. Entrance and exit rooms are exclusive special rooms and
 * therefore contain no other room features.</p>
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Random RANDOM = new Random();
    private static final int FEATURE_CHANCE = 10;
    private static final int PIT_DAMAGE = 20;
    private static final int NORTH_INDEX = 0;
    private static final int EAST_INDEX = 1;
    private static final int SOUTH_INDEX = 2;
    private static final int WEST_INDEX = 3;

    private boolean healingPotion;
    private boolean visionPotion;
    private boolean pit;
    private boolean isEntrance;
    private boolean isExit;
    private Pillar pillar;
    private Monster monster;
    private final boolean[] doors;
    private final int myRow;
    private final int myCol;

    public Room() {
        this(-1, -1);
    }

    public Room(int theRow, int theCol) {
        myRow = theRow;
        myCol = theCol;
        doors = new boolean[4];
        generateRandomFeatures();
    }

    private void generateRandomFeatures() {
        healingPotion = RANDOM.nextInt(100) < FEATURE_CHANCE;
        visionPotion = RANDOM.nextInt(100) < FEATURE_CHANCE;
        pit = RANDOM.nextInt(100) < FEATURE_CHANCE;
    }

    public boolean isEmpty() {
        return !healingPotion
                && !visionPotion
                && !pit
                && !isEntrance
                && !isExit
                && pillar == null
                && monster == null;
    }

    public void setMonsters() {
        int monsterChoice = RANDOM.nextInt(3);
        if (monsterChoice == 0) {
            monster = new Skeleton();
        } else if (monsterChoice == 1) {
            monster = new Gremlin();
        } else {
            monster = new Ogre();
        }
    }

    public void setMonstersManual(Monster m) {
        monster = m;
    }

    public boolean hasItem() {
        return healingPotion || visionPotion || pillar != null;
    }

    public void removePotion(Potion potion) {
        if (potion instanceof HealingPotion) {
            healingPotion = false;
        } else if (potion instanceof VisionPotion) {
            visionPotion = false;
        }
    }

    public int fallInPit() {
        return pit ? PIT_DAMAGE : 0;
    }

    public void setEntrance() {
        clearContents();
        isEntrance = true;
        isExit = false;
    }

    public boolean isEntrance() {
        return isEntrance;
    }

    public void setExit() {
        clearContents();
        isExit = true;
        isEntrance = false;
    }

    public boolean isExit() {
        return isExit;
    }

    public void setPillar(Pillar p) {
        if (!isEntrance && !isExit) {
            pillar = p;
        }
    }

    public Pillar getPillar() {
        return pillar;
    }

    public Pillar removePillar() {
        Pillar foundPillar = pillar;
        pillar = null;
        return foundPillar;
    }

    public boolean hasHealingPotion() {
        return healingPotion;
    }

    public boolean hasVisionPotion() {
        return visionPotion;
    }

    public boolean hasPit() {
        return pit;
    }

    public void setPit() {
        if (!isEntrance && !isExit) {
            pit = true;
        }
    }

    public void removePit() {
        pit = false;
    }

    public Monster getMonster() {
        return monster;
    }

    public void removeMonster() {
        monster = null;
    }

    public boolean workingDoor(Direction d) {
        return doors[doorIndex(d)];
    }

    public void setDoor(Direction d, boolean open) {
        doors[doorIndex(d)] = open;
    }

    public boolean isEscapable() {
        return doors[NORTH_INDEX] || doors[EAST_INDEX]
                || doors[SOUTH_INDEX] || doors[WEST_INDEX];
    }

    public int getRow() {
        return myRow;
    }

    public int getCol() {
        return myCol;
    }

    @Override
    public String toString() {
        String lineSeparator = System.lineSeparator();
        String northWall = doors[NORTH_INDEX] ? "+   +" : "+---+";
        String middle = (doors[WEST_INDEX] ? " " : "|")
                + centerContents()
                + (doors[EAST_INDEX] ? " " : "|");
        String southWall = doors[SOUTH_INDEX] ? "+   +" : "+---+";
        return northWall + lineSeparator + middle + lineSeparator + southWall;
    }

    private void clearContents() {
        healingPotion = false;
        visionPotion = false;
        pit = false;
        pillar = null;
        monster = null;
    }

    public void setHealingPotion(boolean b) {
        if (!isEntrance && !isExit) healingPotion = b;
    }

    public void setVisionPotion(boolean b) {
        if (!isEntrance && !isExit) visionPotion = b;
    }

    private int doorIndex(Direction d) {
        if (d == null) {
            throw new IllegalArgumentException("Direction cannot be null.");
        }
        switch (d) {
            case NORTH:
                return NORTH_INDEX;
            case EAST:
                return EAST_INDEX;
            case SOUTH:
                return SOUTH_INDEX;
            case WEST:
                return WEST_INDEX;
            default:
                throw new IllegalArgumentException("Unknown direction: " + d);
        }
    }

    private String centerContents() {
        if (isEntrance) {
            return " E ";
        }
        if (isExit) {
            return " X ";
        }

        String contents = "";
        if (pillar != null) {
            contents += pillarSymbol();
        }
        if (pit) {
            contents += "L";
        }
        if (healingPotion) {
            contents += "H";
        }
        if (visionPotion) {
            contents += "V";
        }
        if (monster != null) {
            contents += "M";
        }

        if (contents.length() > 3) {
            contents = contents.substring(0, 3);
        }
        while (contents.length() < 3) {
            contents += " ";
        }
        return contents;
    }

    private String pillarSymbol() {
        switch (pillar) {
            case ABSTRACTION:
                return "A";
            case ENCAPSULATION:
                return "E";
            case INHERITANCE:
                return "I";
            case POLYMORPHISM:
                return "P";
            default:
                return "?";
        }
    }
}
