package dungeoncrawler.model;

import dungeoncrawler.persistence.MonsterDatabase;
import dungeoncrawler.model.characters.Gremlin;
import dungeoncrawler.model.characters.Monster;
import dungeoncrawler.model.characters.Ogre;
import dungeoncrawler.model.characters.Skeleton;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * Represents a single location in the dungeon.
 *
 * <p>A room may contain potions, a pit, one pillar piece, a monster, and doors
 * to adjacent rooms. Entrance and exit rooms are exclusive special rooms and
 * therefore contain no other room features.</p>
 */
public class Room implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Random Object for deciding whether a feature is present. */
    private static final Random RANDOM = new Random();

    /** Chance of a feature being present in a room. */
    private static final int FEATURE_CHANCE = 10;

    /** Damage taken when falling into a pit. */
    private static final int PIT_DAMAGE = 20;

    /** Directions of doors with integers corresponding to their index in the doors array. */
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

    /**
     * Basic Constructor. Not currently used.
     */
    public Room() {
        this(-1, -1);
    }

    /**
     * Constructor for the Room class.
     * @param theRow 1-based row number
     * @param theCol 1-based column number
     */
    public Room(int theRow, int theCol) {
        myRow = theRow;
        myCol = theCol;
        doors = new boolean[4];
        generateRandomFeatures();
    }

    /**
     * Generates random features for the room. If the
     * room hits the random number, adds that feature to the room.
     */
    private void generateRandomFeatures() {
        healingPotion = RANDOM.nextInt(100) < FEATURE_CHANCE;
        visionPotion = RANDOM.nextInt(100) < FEATURE_CHANCE;
        pit = RANDOM.nextInt(100) < FEATURE_CHANCE;
    }

    /**
     * Returns true if the room is empty.
     * @return boolean if the room is empty
     */
    public boolean isEmpty() {
        return !healingPotion
                && !visionPotion
                && !pit
                && !isEntrance
                && !isExit
                && pillar == null
                && monster == null;
    }

    /*public void setMonsters() {
        int monsterChoice = RANDOM.nextInt(3);
        if (monsterChoice == 0) {
            monster = new Skeleton();
        } else if (monsterChoice == 1) {
            monster = new Gremlin();
        } else {
            monster = new Ogre();
        }
    }*/

    /**
     * Sets the monster in the room to a random monster from the database.
     * @param db the database to pull the monster from
     * @throws SQLException if the database cannot be accessed
     */
    public void setMonsters(MonsterDatabase db) throws SQLException {
        List<String> names = db.getAllMonsterNames();
        String chosen = names.get(RANDOM.nextInt(names.size()));
        monster = db.getMonsterByName(chosen);
    }

    /**
     * Sets the monster in the room to a manually chosen monster.
     * @param m the monster to set
     */
    public void setMonstersManual(Monster m) {
        monster = m;
    }

    /**
     * Returns true if the room has an item.
     * @return boolean if the room has an item
     */
    public boolean hasItem() {
        return healingPotion || visionPotion || pillar != null;
    }

    /**
     * Removes a potion from the room.
     * @param potion the potion to remove
     */
    public void removePotion(Potion potion) {
        if (potion instanceof HealingPotion) {
            healingPotion = false;
        } else if (potion instanceof VisionPotion) {
            visionPotion = false;
        }
    }

    /**
     * Makes the hero take damage upon falling into a pit.
     * @return the amount of damage taken
     */
    public int fallInPit() {
        return pit ? PIT_DAMAGE : 0;
    }

    /**
     * Sets the room as an entrance or exit room.
     */
    public void setEntrance() {
        clearContents();
        isEntrance = true;
        isExit = false;
    }

    /**
     * Returns true if the room is an entrance room.
     * @return boolean if the room is an entrance room
     */
    public boolean isEntrance() {
        return isEntrance;
    }

    /**
     * Sets the room as an exit room.
     */
    public void setExit() {
        clearContents();
        isExit = true;
        isEntrance = false;
    }

    /**
     * Returns true if the room is an exit room.
     * @return boolean if the room is an exit room
     */
    public boolean isExit() {
        return isExit;
    }

    /**
     * Sets the pillar in the room.
     * @param p the pillar to set
     */
    public void setPillar(Pillar p) {
        if (!isEntrance && !isExit) {
            pillar = p;
        }
    }

    /**
     * Returns the pillar in the room.
     * @return the pillar in the room
     */
    public Pillar getPillar() {
        return pillar;
    }

    /**
     * Removes the pillar from the room.
     * @return the pillar that was removed
     */
    public Pillar removePillar() {
        Pillar foundPillar = pillar;
        pillar = null;
        return foundPillar;
    }

    /**
     * Returns true if the room has a healing potion.
     * @return boolean if the room has a healing potion
     */
    public boolean hasHealingPotion() {
        return healingPotion;
    }

    /**
     * Returns true if the room has a vision potion.
     * @return boolean if the room has a vision potion
     */
    public boolean hasVisionPotion() {
        return visionPotion;
    }

    /**
     * Returns true if the room has a pit.
     * @return boolean if the room has a pit
     */
    public boolean hasPit() {
        return pit;
    }

    /**
     * Sets the pit in the room.
     */
    public void setPit() {
        if (!isEntrance && !isExit) {
            pit = true;
        }
    }

    /**
     * Removes the pit from the room.
     */
    public void removePit() {
        pit = false;
    }

    /**
     * Returns the monster in the room.
     * @return the monster in the room
     */
    public Monster getMonster() {
        return monster;
    }

    /**
     * Removes the monster from the room.
     */
    public void removeMonster() {
        monster = null;
    }

    /**
     * Returns true if the door is open.
     * @param d in the direction of the door
     * @return true if the door is open
     */
    public boolean workingDoor(Direction d) {
        return doors[doorIndex(d)];
    }

    /**
     * Sets the door to open or closed.
     * @param d in the direction of the door
     * @param open true if the door should be open, false if closed
     */
    public void setDoor(Direction d, boolean open) {
        doors[doorIndex(d)] = open;
    }

    /**
     * Returns true if the room is escapable.
     * @return true if the room is escapable
     */
    public boolean isEscapable() {
        return doors[NORTH_INDEX] || doors[EAST_INDEX]
                || doors[SOUTH_INDEX] || doors[WEST_INDEX];
    }

    /**
     * Returns the row number of the room.
     * @return the row number of the room
     */
    public int getRow() {
        return myRow;
    }

    /**
     * Returns the column number of the room.
     * @return the column number of the room
     */
    public int getCol() {
        return myCol;
    }

    /**
     * Returns a string representation of the room.
     * @return a string representation of the room
     */
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

    /**
     * Clears the contents of the room.
     */
    private void clearContents() {
        healingPotion = false;
        visionPotion = false;
        pit = false;
        pillar = null;
        monster = null;
    }

    /**
     * Sets the healing potion in the room.
     * @param b true if the potion should be set, false if not
     */
    public void setHealingPotion(boolean b) {
        if (!isEntrance && !isExit) healingPotion = b;
    }

    /**
     * Sets the vision potion in the room.
     * @param b true if the potion should be set, false if not
     */
    public void setVisionPotion(boolean b) {
        if (!isEntrance && !isExit) visionPotion = b;
    }

    /**
     * Returns the index of the door in the doors array.
     * @param d in the direction of the door
     * @return the index of the door in the doors array
     */
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

    /**
     * Returns the contents of the room.
     * @return the contents of the room
     */
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

    /**
     * Returns the symbol for the pillar.
     * @return the symbol for the pillar
     */
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
