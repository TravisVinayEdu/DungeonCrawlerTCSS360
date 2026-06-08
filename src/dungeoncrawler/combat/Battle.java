package dungeoncrawler.combat;

import dungeoncrawler.model.HealingPotion;
import dungeoncrawler.model.characters.DungeonCharacter;
import dungeoncrawler.model.characters.Hero;
import dungeoncrawler.model.characters.Monster;
import dungeoncrawler.model.characters.Priestess;
import dungeoncrawler.model.characters.Thief;
import dungeoncrawler.model.characters.Warrior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Handles combat rules between one hero and one monster.
 *
 * <p>This class owns battle state and returns plain text messages for the view
 * to display. The Swing UI should ask this class to perform an action, then
 * refresh its panels from the hero and monster objects.</p>
 */
public class Battle {
    private static final Random RANDOM = new Random();

    /** Chance for Warrior's Crushing Blow to hit the enemy. */
    private static final double CRUSHING_BLOW_CHANCE = 0.4;

    /** Minimum damage Crushing Blow can inflict. */
    private static final int MIN_CRUSHING_BLOW_DMG = 75;

    /** Maximum damage Crushing Blow can inflict. */
    private static final int MAX_CRUSHING_BLOW_DMG = 175;

    /** Minimum healing Priestess skill can restore. */
    private static final int MIN_PRIESTESS_HEAL = 25;

    /** Maximum healing Priestess skill can restore. */
    private static final int MAX_PRIESTESS_HEAL = 45;

    /** Chance that the Thief successfully surprises the monster. */
    private static final double SURPRISE_SUCCESS_CHANCE = 0.4;
    /** Chance that the Thief is caught while preparing Surprise Attack. */
    private static final double SURPRISE_CAUGHT_CHANCE = 0.2;

    /** Number of turns a special skill is cooling down for. */
    private static final int SPECIAL_SKILL_COOLDOWN_TURNS = 3;

    /** Number of turns a potion is cooling down for. */
    private static final int POTION_COOLDOWN_TURNS = 2;

    private final Hero myHero;
    private final Monster myMonster;
    private boolean myOver;
    private boolean myEscaped;
    private int mySpecialSkillCooldown;
    private int myPotionCooldown;

    /**
     * Creates a battle between the given hero and monster.
     *
     * @param theHero hero participating in combat
     * @param theMonster monster participating in combat
     * @throws IllegalArgumentException if either combatant is null
     */
    public Battle(final Hero theHero, final Monster theMonster) {
        if (theHero == null) {
            throw new IllegalArgumentException("Hero cannot be null.");
        }
        if (theMonster == null) {
            throw new IllegalArgumentException("Monster cannot be null.");
        }
        myHero = theHero;
        myMonster = theMonster;
        myOver = theHero.isFainted() || theMonster.isFainted();
    }

    /**
     * Returns the hero in this battle.
     *
     * @return battle hero
     */
    public Hero getHero() {
        return myHero;
    }

    /**
     * Returns the monster in this battle.
     *
     * @return battle monster
     */
    public Monster getMonster() {
        return myMonster;
    }

    /**
     * Reports whether the battle can still accept combat actions.
     *
     * @return true while neither side has fainted and the hero has not escaped
     */
    public boolean isActive() {
        return !myOver && !myEscaped
                && !myHero.isFainted()
                && !myMonster.isFainted();
    }

    /**
     * Reports whether the battle has ended.
     *
     * @return true when the hero escaped or either combatant has fainted
     */
    public boolean isOver() {
        return myOver || myEscaped
                || myHero.isFainted()
                || myMonster.isFainted();
    }

    /**
     * Reports whether the hero can currently use a healing potion.
     *
     * @return true when active, stocked, and off cooldown
     */
    public boolean canUseHealingPotion() {
        return isActive() && myHero.getHealingPotions() > 0
                && myPotionCooldown == 0;
    }

    /**
     * Reports whether the hero can currently use a special skill.
     *
     * @return true when active and off cooldown
     */
    public boolean canUseSpecialSkill() {
        return isActive() && mySpecialSkillCooldown == 0;
    }

    /**
     * Returns remaining turns before a special skill can be reused.
     *
     * @return special skill cooldown turns
     */
    public int getSpecialSkillCooldown() {
        return mySpecialSkillCooldown;
    }

    /**
     * Returns remaining turns before a healing potion can be reused.
     *
     * @return potion cooldown turns
     */
    public int getPotionCooldown() {
        return myPotionCooldown;
    }

    /**
     * Performs a full regular attack action for the hero.
     *
     * @return battle result containing messages and end-state flags
     */
    public BattleResult attack() {
        BattleResult result = new BattleResult();
        if (!ensureActive(result)) {
            return result;
        }

        advanceCooldowns();
        int attacks = myHero.attacksPerRoundAgainst(myMonster);
        if (attacks > 1) {
            result.add(myHero.getName() + "'s speed grants "
                    + attacks + " attacks this turn.");
        }
        for (int i = 0; i < attacks && isActive(); i++) {
            performAttack(myHero, myMonster, result);
        }

        if (isActive()) {
            performMonsterTurn(result);
        }
        return finish(result);
    }

    /**
     * Performs the hero's special skill action.
     *
     * @return battle result containing messages and end-state flags
     */
    public BattleResult specialSkill() {
        BattleResult result = new BattleResult();
        if (!ensureActive(result)) {
            return result;
        }
        if (mySpecialSkillCooldown > 0) {
            result.add("Special skill is cooling down for "
                    + mySpecialSkillCooldown + " more turn"
                    + plural(mySpecialSkillCooldown) + ".");
            return result;
        }

        advanceCooldowns();
        performSpecialSkill(result);
        mySpecialSkillCooldown = SPECIAL_SKILL_COOLDOWN_TURNS;

        if (isActive()) {
            performMonsterTurn(result);
        }
        return finish(result);
    }

    /**
     * Uses one healing potion as the hero's action.
     *
     * @return battle result containing messages and end-state flags
     */
    public BattleResult useHealingPotion() {
        BattleResult result = new BattleResult();
        if (!ensureActive(result)) {
            return result;
        }
        if (myHero.getHealingPotions() <= 0) {
            result.add("No healing potions remain.");
            return result;
        }
        if (myPotionCooldown > 0) {
            result.add("Potions are cooling down for "
                    + myPotionCooldown + " more turn"
                    + plural(myPotionCooldown) + ".");
            return result;
        }

        advanceCooldowns();
        int before = myHero.getHitPoints();
        myHero.usePotion(new HealingPotion());
        myPotionCooldown = POTION_COOLDOWN_TURNS;
        int healed = myHero.getHitPoints() - before;
        if (healed > 0) {
            result.add(myHero.getName() + " drinks a healing potion and recovers "
                    + healed + " HP.");
        } else {
            result.add(myHero.getName()
                    + " drinks a healing potion, but is already at full health.");
        }

        if (isActive()) {
            performMonsterTurn(result);
        }
        return finish(result);
    }

    /**
     * The hero attempts to escape from the monster.
     *
     * @return battle result describing escape or battle-over status
     */
    public BattleResult run() {
        BattleResult result = new BattleResult();
        if (myEscaped) {
            result.add("You are already away from the battle.");
        } else if (myOver || myHero.isFainted() || myMonster.isFainted()) {
            result.add("The battle is already over.");
        } else {
            myEscaped = true;
            result.add(myHero.getName() + " escapes from the "
                    + myMonster.getName() + ".");
        }
        return finish(result);
    }

    /**
     * Advances action cooldown counters after a valid player action.
     */
    private void advanceCooldowns() {
        if (mySpecialSkillCooldown > 0) {
            mySpecialSkillCooldown--;
        }
        if (myPotionCooldown > 0) {
            myPotionCooldown--;
        }
    }

    /**
     * Returns a plural suffix for turn-count messages.
     *
     * @param theTurns number of turns
     * @return empty string for one turn, otherwise "s"
     */
    private String plural(final int theTurns) {
        return theTurns == 1 ? "" : "s";
    }

    /**
     * Executes the monster's response turn.
     *
     * @param theResult result object receiving turn messages
     */
    private void performMonsterTurn(final BattleResult theResult) {
        int attacks = myMonster.attacksPerRoundAgainst(myHero);
        if (attacks > 1) {
            theResult.add(myMonster.getName() + "'s speed grants "
                    + attacks + " attacks this turn.");
        }
        for (int i = 0; i < attacks && isActive(); i++) {
            performAttack(myMonster, myHero, theResult);
        }
    }

    /**
     * Performs the special skill of the hero.
     *
     * @param theResult result object receiving messages and state changes
     */
    private void performSpecialSkill(final BattleResult theResult) {
        if (myHero instanceof Warrior) {
            performCrushingBlow(theResult);
        } else if (myHero instanceof Priestess) {
            performPriestessHeal(theResult);
        } else if (myHero instanceof Thief) {
            performSurpriseAttack(theResult);
        } else {
            int heroBefore = myHero.getHitPoints();
            int monsterBefore = myMonster.getHitPoints();
            theResult.add(myHero.getName() + " uses a special skill.");
            myHero.useSpecialSkill(myMonster);
            describeHitPointChange(myHero, heroBefore, theResult);
            describeHitPointChange(myMonster, monsterBefore, theResult);
            if (myHero.getHitPoints() == heroBefore
                    && myMonster.getHitPoints() == monsterBefore) {
                theResult.add("The special skill has no visible effect.");
            }
        }
    }

    /**
     * Executes the Warrior's Crushing Blow attack during battle.
     *
     * @param theResult result object receiving messages and state updates
     */
    private void performCrushingBlow(final BattleResult theResult) {
        theResult.add(myHero.getName() + " attempts Crushing Blow.");
        if (RANDOM.nextDouble() > CRUSHING_BLOW_CHANCE) {
            theResult.add("Crushing Blow misses.");
            return;
        }

        int before = myMonster.getHitPoints();
        int damage = randomInRange(MIN_CRUSHING_BLOW_DMG, MAX_CRUSHING_BLOW_DMG);
        int damageTaken = myMonster.takeDamage(damage);
        if (damageTaken > 0) {
            theResult.add(myMonster.getName() + " takes "
                    + damageTaken + " damage from Crushing Blow.");
            describeMonsterHealing(myMonster, before, damageTaken, theResult);
        } else {
            theResult.add("Crushing Blow has no effect.");
        }
    }

    /**
     * Performs the hero's Priestess Heal skill.
     *
     * @param theResult result object receiving messages and state changes
     */
    private void performPriestessHeal(final BattleResult theResult) {
        int before = myHero.getHitPoints();
        int healed = myHero.heal(randomInRange(MIN_PRIESTESS_HEAL, MAX_PRIESTESS_HEAL));
        if (healed > 0) {
            theResult.add(myHero.getName() + " prays and recovers "
                    + healed + " HP.");
        } else if (before == myHero.getMaxHitPoints()) {
            theResult.add(myHero.getName()
                    + " prays, but is already at full health.");
        } else {
            theResult.add(myHero.getName() + "'s prayer has no effect.");
        }
    }

    /**
     * Executes the Thief's Surprise Attack.
     *
     * @param theResult result object receiving messages and state changes
     */
    private void performSurpriseAttack(final BattleResult theResult) {
        double roll = RANDOM.nextDouble();
        if (roll < SURPRISE_SUCCESS_CHANCE) {
            theResult.add(myHero.getName()
                    + " catches the monster off guard with Surprise Attack.");
            performAttack(myHero, myMonster, theResult);
            if (isActive()) {
                theResult.add(myHero.getName() + " follows up with a second strike.");
                performAttack(myHero, myMonster, theResult);
            }
        } else if (roll < SURPRISE_SUCCESS_CHANCE + SURPRISE_CAUGHT_CHANCE) {
            theResult.add(myHero.getName()
                    + " is caught setting up Surprise Attack and loses the action.");
        } else {
            theResult.add(myHero.getName()
                    + "'s Surprise Attack becomes a normal attack.");
            performAttack(myHero, myMonster, theResult);
        }
    }

    /**
     * Performs an attack action between two characters.
     *
     * @param theAttacker character making the attack
     * @param theDefender character receiving the attack
     * @param theResult result object receiving messages and state changes
     */
    private void performAttack(final DungeonCharacter theAttacker,
                               final DungeonCharacter theDefender,
                               final BattleResult theResult) {
        if (theDefender.isFainted()) {
            return;
        }

        theResult.add(theAttacker.getName() + " attacks.");
        if (RANDOM.nextDouble() > theAttacker.getHitChance()) {
            theResult.add(theAttacker.getName() + " misses.");
            return;
        }

        int before = theDefender.getHitPoints();
        int damage = randomInRange(theAttacker.getMinDamage(),
                theAttacker.getMaxDamage());
        int damageTaken = theDefender.defendAgainstAttack(damage);
        if (damageTaken <= 0) {
            theResult.add(theDefender.getName() + " blocks the attack.");
            return;
        }

        theResult.add(theDefender.getName() + " takes "
                + damageTaken + " damage.");
        describeMonsterHealing(theDefender, before, damageTaken, theResult);
    }

    /**
     * Describes the healing done by a monster to a character.
     *
     * @param theDefender defender that may have healed
     * @param theBefore defender health before damage
     * @param theDamageTaken amount of damage dealt before healing
     * @param theResult result object receiving messages
     */
    private void describeMonsterHealing(final DungeonCharacter theDefender,
                                        final int theBefore,
                                        final int theDamageTaken,
                                        final BattleResult theResult) {
        if (!(theDefender instanceof Monster) || theDefender.isFainted()) {
            return;
        }

        int expectedAfterDamage = Math.max(0, theBefore - theDamageTaken);
        int healed = theDefender.getHitPoints() - expectedAfterDamage;
        if (healed > 0) {
            theResult.add(theDefender.getName() + " heals "
                    + healed + " HP.");
        }
    }

    /**
     * Describes the hit point change of a character.
     *
     * @param theCharacter character whose hit point change is being described
     * @param theBefore hit point value before the change
     * @param theResult result object receiving messages
     */
    private void describeHitPointChange(final DungeonCharacter theCharacter,
                                        final int theBefore,
                                        final BattleResult theResult) {
        int after = theCharacter.getHitPoints();
        if (after < theBefore) {
            theResult.add(theCharacter.getName() + " loses "
                    + (theBefore - after) + " HP.");
        } else if (after > theBefore) {
            theResult.add(theCharacter.getName() + " recovers "
                    + (after - theBefore) + " HP.");
        }
    }

    /**
     * Ensures that the battle is still active. If not, adds an appropriate message to the result.
     *
     * @param theResult result object receiving status messages
     * @return true if the battle is still active, false otherwise
     */
    private boolean ensureActive(final BattleResult theResult) {
        if (isActive()) {
            return true;
        }
        theResult.add("The battle is already over.");
        return false;
    }

    /**
     * Finishes the battle and sets the appropriate flags based on the outcome.
     *
     * @param theResult result object receiving battle outcome messages
     * @return updated battle result with status flags and messages
     */
    private BattleResult finish(final BattleResult theResult) {
        if (myHero.isFainted()) {
            myOver = true;
            theResult.setHeroDefeated(true);
            theResult.add(myHero.getName() + " has fallen.");
            theResult.add("Game over. Return to the main menu or start a new game.");
        }
        if (myMonster.isFainted()) {
            myOver = true;
            theResult.setMonsterDefeated(true);
            theResult.add(myMonster.getName() + " is defeated.");
            theResult.add("Victory. Return to the dungeon when ready.");
        }
        if (myEscaped) {
            myOver = true;
            theResult.setEscaped(true);
        }
        theResult.setBattleOver(isOver());
        return theResult;
    }

    /**
     * Returns a random integer between the specified minimum and maximum values, inclusive.
     *
     * @param theMin minimum value, inclusive
     * @param theMax maximum value, inclusive
     * @return randomly generated integer
     */
    private int randomInRange(final int theMin, final int theMax) {
        if (theMax < theMin) {
            throw new IllegalArgumentException("Random range is invalid.");
        }
        return RANDOM.nextInt(theMax - theMin + 1) + theMin;
    }

    /**
     * Read-only result object returned after each battle action.
     */
    public static class BattleResult {
        private final List<String> myMessages;
        private boolean myBattleOver;
        private boolean myHeroDefeated;
        private boolean myMonsterDefeated;
        private boolean myEscaped;

        /** Creates a new battle result with no messages. */
        private BattleResult() {
            myMessages = new ArrayList<>();
        }

        /**
         * Adds a message to the battle result.
         *
         * @param theMessage message to append
         */
        private void add(final String theMessage) {
            myMessages.add(theMessage);
        }

        /**
         * Returns the battle messages produced by the action.
         *
         * @return unmodifiable list of messages
         */
        public List<String> getMessages() {
            return Collections.unmodifiableList(myMessages);
        }

        /**
         * Reports whether the battle ended after the action.
         *
         * @return true if the battle is over
         */
        public boolean isBattleOver() {
            return myBattleOver;
        }

        /**
         * Reports whether the hero was defeated.
         *
         * @return true if the hero fainted
         */
        public boolean isHeroDefeated() {
            return myHeroDefeated;
        }

        /**
         * Reports whether the monster was defeated.
         *
         * @return true if the monster fainted
         */
        public boolean isMonsterDefeated() {
            return myMonsterDefeated;
        }

        /**
         * Reports whether the hero escaped.
         *
         * @return true if the battle ended by running away
         */
        public boolean isEscaped() {
            return myEscaped;
        }

        private void setBattleOver(final boolean theBattleOver) {
            myBattleOver = theBattleOver;
        }

        private void setHeroDefeated(final boolean theHeroDefeated) {
            myHeroDefeated = theHeroDefeated;
        }

        private void setMonsterDefeated(final boolean theMonsterDefeated) {
            myMonsterDefeated = theMonsterDefeated;
        }

        private void setEscaped(final boolean theEscaped) {
            myEscaped = theEscaped;
        }
    }
}
