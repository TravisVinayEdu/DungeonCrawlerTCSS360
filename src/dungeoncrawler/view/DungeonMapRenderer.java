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
        return render(theDungeon, false);
    }

    String renderFull(final Dungeon theDungeon) {
        return render(theDungeon, true);
    }

    private String render(final Dungeon theDungeon, final boolean theRevealAll) {
        String text = "";
        for (int row = 0; row < theDungeon.getHeight(); row++) {
            for (int col = 0; col < theDungeon.getWidth(); col++) {
                if (isVisible(theDungeon, row, col, theRevealAll)) {
                    text += "+";
                    text += topMapWall(theDungeon, row, col);
                } else {
                    text += "    ";
                }
            }
            text += trailingMapCorner(theDungeon, row, theRevealAll)
                    + System.lineSeparator();

            for (int col = 0; col < theDungeon.getWidth(); col++) {
                Room room = theDungeon.getRoom(row, col);
                if (isVisible(theDungeon, row, col, theRevealAll)) {
                    text += leftMapWall(room);
                    text += roomMapSymbol(theDungeon, room, row, col);
                } else {
                    text += "    ";
                }
            }
            text += trailingMapWall(theDungeon, row, theRevealAll)
                    + System.lineSeparator();
        }
        for (int col = 0; col < theDungeon.getWidth(); col++) {
            if (isVisible(theDungeon, theDungeon.getHeight() - 1,
                    col, theRevealAll)) {
                text += "+---";
            } else {
                text += "    ";
            }
        }
        return text + trailingBottomCorner(theDungeon, theRevealAll);
    }

    private String trailingMapCorner(final Dungeon theDungeon,
                                     final int theRow,
                                     final boolean theRevealAll) {
        if (isVisible(theDungeon, theRow, theDungeon.getWidth() - 1,
                theRevealAll)) {
            return "+";
        }
        return "";
    }

    private String trailingMapWall(final Dungeon theDungeon,
                                   final int theRow,
                                   final boolean theRevealAll) {
        if (isVisible(theDungeon, theRow, theDungeon.getWidth() - 1,
                theRevealAll)) {
            return "|";
        }
        return "";
    }

    private String trailingBottomCorner(final Dungeon theDungeon,
                                        final boolean theRevealAll) {
        if (isVisible(theDungeon, theDungeon.getHeight() - 1,
                theDungeon.getWidth() - 1, theRevealAll)) {
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
            return " i ";
        }
        if (theRoom.isExit()) {
            return " O ";
        }
        if (theRoom.getMonster() != null) {
            return " M ";
        }
        if (theRoom.getPillar() != null) {
            return " " + pillarLetter(theRoom.getPillar()) + " ";
        }
        if (theRoom.hasPit()) {
            return " X ";
        }
        if (theRoom.hasHealingPotion()) {
            return " H ";
        }
        if (theRoom.hasVisionPotion()) {
            return " V ";
        }
        return "   ";
    }

    private boolean isVisible(final Dungeon theDungeon,
                              final int theRow,
                              final int theCol,
                              final boolean theRevealAll) {
        return theRevealAll || theDungeon.isDiscovered(theRow, theCol);
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
