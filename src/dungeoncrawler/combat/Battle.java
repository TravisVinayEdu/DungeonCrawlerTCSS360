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

    /** Chance for warrrior skill to hit the enemy **/
    private static final double CRUSHING_BLOW_CHANCE = 0.4;

    /** Minimum damage crushing blow can inflict **/
    private static final int MIN_CRUSHING_BLOW_DMG = 75;

    /** Maximum damagte crushing blow can inflict **/
    private static final int MAX_CRUSHING_BLOW_DMG = 175;

    /** Minimum healing priestess skill can heal **/
    private static final int MIN_PRIESTESS_HEAL = 25;

    /** Maximum healing priestess skill can heal **/
    private static final int MAX_PRIESTESS_HEAL = 45;

    /** Chance that the theif will surpise the monster **/
    private static final double SURPRISE_SUCCESS_CHANCE = 0.4;
    private static final double SURPRISE_CAUGHT_CHANCE = 0.2;

    /** Number of turns a special skill is cooling down for **/
    private static final int SPECIAL_SKILL_COOLDOWN_TURNS = 3;

    /** Number of turns a potion is cooling down for **/
    private static final int POTION_COOLDOWN_TURNS = 2;

    private final Hero myHero;
    private final Monster myMonster;
    private boolean myOver;
    private boolean myEscaped;
    private int mySpecialSkillCooldown;
    private int myPotionCooldown;

    /** Creates a battle between the given hero and monster. **/
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

    public Hero getHero() {
        return myHero;
    }

    public Monster getMonster() {
        return myMonster;
    }

    /** Returns true if the battle is still active. **/
    public boolean isActive() {
        return !myOver && !myEscaped
                && !myHero.isFainted()
                && !myMonster.isFainted();
    }

    /** Returns true if the battle is over. **/
    public boolean isOver() {
        return myOver || myEscaped
                || myHero.isFainted()
                || myMonster.isFainted();
    }

    /** Returns true if the hero can use healing potions. **/
    public boolean canUseHealingPotion() {
        return isActive() && myHero.getHealingPotions() > 0
                && myPotionCooldown == 0;
    }

    /** Returns true if the hero can use a special skill. **/
    public boolean canUseSpecialSkill() {
        return isActive() && mySpecialSkillCooldown == 0;
    }

    public int getSpecialSkillCooldown() {
        return mySpecialSkillCooldown;
    }

    public int getPotionCooldown() {
        return myPotionCooldown;
    }

    /**
     * Works through an attack process mid-battle
     * @return The result of the battle
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
     * Uses the special skill of that hero.
     * @return The result of the battle
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
     * Uses a healing potion.
     * @return The result of the battle
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
     * @return the result of the battle
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

    /** Returns true if the hero has fainted. **/
    private void advanceCooldowns() {
        if (mySpecialSkillCooldown > 0) {
            mySpecialSkillCooldown--;
        }
        if (myPotionCooldown > 0) {
            myPotionCooldown--;
        }
    }

    /** Returns "s" if the given number is not 1. **/
    private String plural(final int theTurns) {
        return theTurns == 1 ? "" : "s";
    }

    /**
    * Executes the monster's turn during the battle. The monster may attack the hero multiple times
    * based on its speed relative to the hero. Each attack is processed individually, and messages
    * describing the events are added to the battle result.
    *
    * @param theResult The result object to store messages and state changes related to the current
    *                  turn of the monster.
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
     * @param theResult The result object to store messages and state changes related to the current
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
 * Executes the hero's Crushing Blow attack during the battle. If successful,
 * it deals a random amount of damage to the monster. The result of the attack,
 * including misses, damage dealt, and any subsequent healing by the monster,
 * is added to the provided BattleResult object.
 *
 * @param theResult The result object to store messages and state updates
 *                  related to the Crushing Blow attack.
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
     * @param theResult The result object to store messages and state changes related to the current
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
 * Executes the hero's Surprise Attack during the battle. Depending on a random chance,
 * the hero may catch the monster off guard, fail and be caught, or execute a normal attack.
 * If successful, the hero may follow up with a second strike.
 *
 * @param theResult The result object to store messages and state changes related to
 *                  the Surprise Attack execution.
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
     * @param theAttacker The attacker character.
     * @param theDefender The defender character.
     * @param theResult The result object to store messages and state changes related to the current
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
     * @param theDefender The character that was healed.
     * @param theBefore Monster health before
     * @param theDamageTaken Amount of damage dealt to the character
     * @param theResult The result object to store messages and state changes related to the current
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
     * @param theCharacter The character whose hit point change is being described.
     * @param theBefore The hit point value before the change.
     * @param theResult The result object to store messages and state changes related to the current
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
     * @param theResult The result object to store messages related to the battle status.
     * @return true if the battle is still active, false otherwise.
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
     * @param theResult The result object to store messages related to the battle outcome.
     * @return The updated BattleResult object with the battle status and messages.
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
     * @param theMin The minimum value (inclusive).
     * @param theMax The maximum value (inclusive).
     * @return The randomly generated integer.
     */
    private int randomInRange(final int theMin, final int theMax) {
        if (theMax < theMin) {
            throw new IllegalArgumentException("Random range is invalid.");
        }
        return RANDOM.nextInt(theMax - theMin + 1) + theMin;
    }

    public static class BattleResult {
        private final List<String> myMessages;
        private boolean myBattleOver;
        private boolean myHeroDefeated;
        private boolean myMonsterDefeated;
        private boolean myEscaped;

        /** Creates a new BattleResult object with no messages. **/
        private BattleResult() {
            myMessages = new ArrayList<>();
        }

        /** Adds a message to the battle result. **/
        private void add(final String theMessage) {
            myMessages.add(theMessage);
        }

        public List<String> getMessages() {
            return Collections.unmodifiableList(myMessages);
        }

        public boolean isBattleOver() {
            return myBattleOver;
        }

        public boolean isHeroDefeated() {
            return myHeroDefeated;
        }

        public boolean isMonsterDefeated() {
            return myMonsterDefeated;
        }

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
