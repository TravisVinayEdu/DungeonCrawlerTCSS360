package dungeoncrawler.controller;

import dungeoncrawler.combat.Battle;
import dungeoncrawler.model.Direction;
import dungeoncrawler.model.Dungeon;
import dungeoncrawler.model.HealingPotion;
import dungeoncrawler.model.Pillar;
import dungeoncrawler.model.Room;
import dungeoncrawler.model.VisionPotion;
import dungeoncrawler.model.characters.Hero;
import dungeoncrawler.model.characters.Monster;

import java.io.Serializable;

/**
 * Coordinates the current playable game state.
 *
 * <p>The view owns display widgets, while this class owns game-flow decisions:
 * movement, room effects, battle entry/exit, monster removal, and win/loss
 * state.</p>
 */
public class GameSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Hero myHero;
    private final Dungeon myDungeon;
    private transient Battle myBattle;

    public GameSession(final Hero theHero, final Dungeon theDungeon) {
        if (theHero == null) {
            throw new IllegalArgumentException("Hero cannot be null.");
        }
        if (theDungeon == null) {
            throw new IllegalArgumentException("Dungeon cannot be null.");
        }
        myHero = theHero;
        myDungeon = theDungeon;
    }

    public Hero getHero() {
        return myHero;
    }

    public Dungeon getDungeon() {
        return myDungeon;
    }

    public Room getCurrentRoom() {
        return myDungeon.getCurrentRoom();
    }

    public Battle getBattle() {
        return myBattle;
    }

    public boolean isInBattle() {
        return myBattle != null;
    }

    public boolean isBattleActive() {
        return myBattle != null && myBattle.isActive();
    }

    public boolean hasWon() {
        Room room = getCurrentRoom();
        return room.isExit() && myHero.hasAllPillars();
    }

    public boolean isGameOver() {
        return myHero.isFainted() || hasWon();
    }

    public MoveResult enterCurrentRoom(final String theBaseMessage) {
        String message = resolveCurrentRoom(theBaseMessage);
        boolean enteredBattle = enterBattleIfMonsterPresent();
        return new MoveResult(true, enteredBattle, message);
    }

    public MoveResult moveHero(final Direction theDirection) {
        if (myHero.isFainted()) {
            return new MoveResult(false, false, "You have fallen.");
        }
        if (isBattleActive()) {
            return new MoveResult(false, false, "You cannot move during battle.");
        }
        if (!myDungeon.moveHero(theDirection)) {
            return new MoveResult(false, false, "There is no door that way.");
        }

        String message = resolveCurrentRoom("Moved "
                + theDirection.name().toLowerCase() + ".");
        boolean enteredBattle = enterBattleIfMonsterPresent();
        return new MoveResult(true, enteredBattle, message);
    }

    public Battle.BattleResult attack() {
        return handleBattleResult(myBattle.attack());
    }

    public Battle.BattleResult specialSkill() {
        return handleBattleResult(myBattle.specialSkill());
    }

    public Battle.BattleResult useHealingPotion() {
        return handleBattleResult(myBattle.useHealingPotion());
    }

    public Battle.BattleResult useVisionPotion() {
        return handleBattleResult(myBattle.useVisionPotion());
    }

    public Battle.BattleResult runFromBattle() {
        Battle.BattleResult result = myBattle.run();
        if (result.isEscaped()) {
            myBattle = null;
        }
        return result;
    }

    public void leaveFinishedBattle() {
        if (myBattle != null && !myBattle.isActive()) {
            myBattle = null;
        }
    }

    private Battle.BattleResult handleBattleResult(final Battle.BattleResult theResult) {
        if (theResult.isMonsterDefeated()
                && getCurrentRoom().getMonster() == myBattle.getMonster()) {
            getCurrentRoom().removeMonster();
        }
        return theResult;
    }

    private boolean enterBattleIfMonsterPresent() {
        Monster monster = getCurrentRoom().getMonster();
        if (monster != null && !monster.isFainted()) {
            myBattle = new Battle(myHero, monster);
            return true;
        }
        return false;
    }

    private String resolveCurrentRoom(final String theBaseMessage) {
        Room room = getCurrentRoom();
        String message = theBaseMessage;

        if (room.hasHealingPotion()) {
            myHero.addHealingPotion();
            room.removePotion(new HealingPotion());
            message += " Picked up a healing potion.";
        }
        if (room.hasVisionPotion()) {
            myHero.addVisionPotion();
            room.removePotion(new VisionPotion());
            message += " Picked up a vision potion.";
        }
        Pillar pillar = room.removePillar();
        if (pillar != null) {
            myHero.addPillar(pillar);
            message += " Found " + formatPillar(pillar) + ".";
        }
        int pitDamage = room.fallInPit();
        if (pitDamage > 0) {
            myHero.takeDamage(pitDamage);
            message += " Stepped in lava for " + pitDamage + " damage.";
        }
        if (room.isExit()) {
            if (myHero.hasAllPillars()) {
                message += " You escaped with all four pillars.";
            } else {
                message += " The exit is here, but you still need every pillar.";
            }
        }
        if (myHero.isFainted()) {
            message += " You have fallen.";
        }

        return message;
    }

    private String formatPillar(final Pillar thePillar) {
        String name = thePillar.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static class MoveResult {
        private final boolean myMoved;
        private final boolean myEnteredBattle;
        private final String myMessage;

        private MoveResult(final boolean theMoved,
                           final boolean theEnteredBattle,
                           final String theMessage) {
            myMoved = theMoved;
            myEnteredBattle = theEnteredBattle;
            myMessage = theMessage;
        }

        public boolean moved() {
            return myMoved;
        }

        public boolean enteredBattle() {
            return myEnteredBattle;
        }

        public String message() {
            return myMessage;
        }
    }
}
