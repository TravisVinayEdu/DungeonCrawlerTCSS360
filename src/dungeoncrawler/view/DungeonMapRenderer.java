package dungeoncrawler.view;

import dungeoncrawler.model.Direction;
import dungeoncrawler.model.Dungeon;
import dungeoncrawler.model.Pillar;
import dungeoncrawler.model.Room;

/**
 * Converts the discovered dungeon state into the terminal map display text.
 */
final class DungeonMapRenderer {
    String render(final Dungeon theDungeon) {
        String text = "";
        for (int row = 0; row < theDungeon.getHeight(); row++) {
            for (int col = 0; col < theDungeon.getWidth(); col++) {
                if (theDungeon.isDiscovered(row, col)) {
                    text += "+";
                    text += topMapWall(theDungeon, row, col);
                } else {
                    text += "    ";
                }
            }
            text += trailingMapCorner(theDungeon, row) + System.lineSeparator();

            for (int col = 0; col < theDungeon.getWidth(); col++) {
                Room room = theDungeon.getRoom(row, col);
                if (theDungeon.isDiscovered(row, col)) {
                    text += leftMapWall(room);
                    text += roomMapSymbol(theDungeon, room, row, col);
                } else {
                    text += "    ";
                }
            }
            text += trailingMapWall(theDungeon, row) + System.lineSeparator();
        }
        for (int col = 0; col < theDungeon.getWidth(); col++) {
            if (theDungeon.isDiscovered(theDungeon.getHeight() - 1, col)) {
                text += "+---";
            } else {
                text += "    ";
            }
        }
        return text + trailingBottomCorner(theDungeon);
    }

    private String trailingMapCorner(final Dungeon theDungeon,
                                     final int theRow) {
        if (theDungeon.isDiscovered(theRow, theDungeon.getWidth() - 1)) {
            return "+";
        }
        return "";
    }

    private String trailingMapWall(final Dungeon theDungeon,
                                   final int theRow) {
        if (theDungeon.isDiscovered(theRow, theDungeon.getWidth() - 1)) {
            return "|";
        }
        return "";
    }

    private String trailingBottomCorner(final Dungeon theDungeon) {
        if (theDungeon.isDiscovered(theDungeon.getHeight() - 1,
                theDungeon.getWidth() - 1)) {
            return "+";
        }
        return "";
    }

    private String topMapWall(final Dungeon theDungeon,
                              final int theRow,
                              final int theCol) {
        return theDungeon.getRoom(theRow, theCol).workingDoor(Direction.NORTH)
                ? "   " : "---";
    }

    private String leftMapWall(final Room theRoom) {
        return theRoom.workingDoor(Direction.WEST) ? " " : "|";
    }

    private String roomMapSymbol(final Dungeon theDungeon,
                                 final Room theRoom,
                                 final int theRow,
                                 final int theCol) {
        if (theRow == theDungeon.getHeroRow() && theCol == theDungeon.getHeroCol()) {
            return " @ ";
        }
        if (theRoom.isEntrance()) {
            return " S ";
        }
        if (theRoom.isExit()) {
            return " X ";
        }
        if (theRoom.getPillar() != null) {
            return " " + pillarLetter(theRoom.getPillar()) + " ";
        }
        if (theRoom.hasPit()) {
            return " L ";
        }
        if (theRoom.hasHealingPotion()) {
            return " H ";
        }
        if (theRoom.hasVisionPotion()) {
            return " V ";
        }
        if (theRoom.getMonster() != null) {
            return " M ";
        }
        return "   ";
    }

    private String pillarLetter(final Pillar thePillar) {
        switch (thePillar) {
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
