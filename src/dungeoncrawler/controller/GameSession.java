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

    /**
     * Creates a playable session around one hero and one dungeon.
     *
     * @param theHero hero controlled by the player
     * @param theDungeon dungeon being explored
     * @throws IllegalArgumentException if the hero or dungeon is null
     */
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

    /**
     * Returns the session hero.
     *
     * @return current hero
     */
    public Hero getHero() {
        return myHero;
    }

    /**
     * Returns the dungeon being explored.
     *
     * @return current dungeon
     */
    public Dungeon getDungeon() {
        return myDungeon;
    }

    /**
     * Returns the room occupied by the hero.
     *
     * @return current room
     */
    public Room getCurrentRoom() {
        return myDungeon.getCurrentRoom();
    }

    /**
     * Returns the current battle, if one exists.
     *
     * @return active or finished battle, or null while exploring
     */
    public Battle getBattle() {
        return myBattle;
    }

    /**
     * Reports whether the session currently has a battle object.
     *
     * @return true when the hero has entered a monster room
     */
    public boolean isInBattle() {
        return myBattle != null;
    }

    /**
     * Reports whether the current battle is still active.
     *
     * @return true when a battle exists and neither side has finished it
     */
    public boolean isBattleActive() {
        return myBattle != null && myBattle.isActive();
    }

    /**
     * Reports whether the hero has satisfied the win condition.
     *
     * @return true when the hero is on the exit room with every pillar
     */
    public boolean hasWon() {
        Room room = getCurrentRoom();
        return room.isExit() && myHero.hasAllPillars();
    }

    /**
     * Reports whether no more exploration actions should be allowed.
     *
     * @return true when the hero has fainted or won
     */
    public boolean isGameOver() {
        return myHero.isFainted() || hasWon();
    }

    /**
     * Resolves effects in the current room without moving first.
     *
     * <p>This is used when a new or loaded session begins so the entrance room
     * can pick up items, trigger battle, or display status consistently.</p>
     *
     * @param theBaseMessage message prefix for the result
     * @return summary of room effects and whether battle started
     */
    public MoveResult enterCurrentRoom(final String theBaseMessage) {
        int pillarCount = myHero.getPillars().size();
        String message = resolveCurrentRoom(theBaseMessage);
        boolean enteredBattle = enterBattleIfMonsterPresent();
        return new MoveResult(true, enteredBattle,
                myHero.getPillars().size() > pillarCount, message);
    }

    /**
     * Attempts to move the hero and resolves the destination room.
     *
     * @param theDirection direction requested by the player
     * @return movement result, room-effect message, and battle/pillar flags
     */
    public MoveResult moveHero(final Direction theDirection) {
        if (myHero.isFainted()) {
            return new MoveResult(false, false, false, "You have fallen.");
        }
        if (hasWon()) {
            return new MoveResult(false, false, false,
                    "You have already escaped with all four pillars.");
        }
        if (isBattleActive()) {
            return new MoveResult(false, false, false,
                    "You cannot move during battle.");
        }
        if (!myDungeon.moveHero(theDirection)) {
            return new MoveResult(false, false, false,
                    "There is no door that way.");
        }

        int pillarCount = myHero.getPillars().size();
        String message = resolveCurrentRoom("Moved "
                + theDirection.name().toLowerCase() + ".");
        boolean enteredBattle = enterBattleIfMonsterPresent();
        return new MoveResult(true, enteredBattle,
                myHero.getPillars().size() > pillarCount, message);
    }

    /**
     * Performs a regular attack action in the active battle.
     *
     * @return battle messages and state flags
     */
    public Battle.BattleResult attack() {
        return handleBattleResult(myBattle.attack());
    }

    /**
     * Performs the hero's special skill in the active battle.
     *
     * @return battle messages and state flags
     */
    public Battle.BattleResult specialSkill() {
        return handleBattleResult(myBattle.specialSkill());
    }

    /**
     * Uses a healing potion in the active battle.
     *
     * @return battle messages and state flags
     */
    public Battle.BattleResult useHealingPotion() {
        return handleBattleResult(myBattle.useHealingPotion());
    }

    /**
     * Uses a vision potion while exploring.
     *
     * @return message describing whether nearby rooms were revealed
     */
    public String useVisionPotion() {
        if (myHero.isFainted()) {
            return "You have fallen.";
        }
        if (isBattleActive()) {
            return "Vision potions can only be used while exploring.";
        }
        if (myHero.getVisionPotions() <= 0) {
            return "No vision potions remain.";
        }

        myHero.usePotion(new VisionPotion());
        int revealed = myDungeon.revealAroundHero(VisionPotion.VISION_RADIUS);
        if (revealed == 0) {
            return "Used a vision potion. No new rooms were revealed.";
        }
        return "Used a vision potion and revealed " + revealed
                + " nearby room" + plural(revealed) + ".";
    }

    /**
     * Attempts to run from the current battle.
     *
     * @return battle result describing the escape outcome
     */
    public Battle.BattleResult runFromBattle() {
        Battle.BattleResult result = myBattle.run();
        if (result.isEscaped()) {
            myBattle = null;
        }
        return result;
    }

    /**
     * Clears a finished battle so the UI can return to exploration.
     */
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
            message += " Fell into a pit for " + pitDamage + " damage.";
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

    private String plural(final int theCount) {
        return theCount == 1 ? "" : "s";
    }

    /**
     * Immutable result for a room-entry or movement action.
     */
    public static class MoveResult {
        private final boolean myMoved;
        private final boolean myEnteredBattle;
        private final boolean myFoundPillar;
        private final String myMessage;

        private MoveResult(final boolean theMoved,
                           final boolean theEnteredBattle,
                           final boolean theFoundPillar,
                           final String theMessage) {
            myMoved = theMoved;
            myEnteredBattle = theEnteredBattle;
            myFoundPillar = theFoundPillar;
            myMessage = theMessage;
        }

        /**
         * Reports whether the hero changed rooms or entered the room.
         *
         * @return true when the action succeeded
         */
        public boolean moved() {
            return myMoved;
        }

        /**
         * Reports whether the action started a battle.
         *
         * @return true when a monster battle began
         */
        public boolean enteredBattle() {
            return myEnteredBattle;
        }

        /**
         * Reports whether the action collected a new pillar.
         *
         * @return true when a pillar was found
         */
        public boolean foundPillar() {
            return myFoundPillar;
        }

        /**
         * Returns the user-facing action summary.
         *
         * @return action message
         */
        public String message() {
            return myMessage;
        }
    }
}
