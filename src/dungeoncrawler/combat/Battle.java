package dungeoncrawler.combat;

import dungeoncrawler.model.HealingPotion;
import dungeoncrawler.model.characters.DungeonCharacter;
import dungeoncrawler.model.characters.Hero;
import dungeoncrawler.model.characters.Monster;

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

    private final Hero myHero;
    private final Monster myMonster;
    private boolean myOver;
    private boolean myEscaped;

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

    public boolean isActive() {
        return !myOver && !myEscaped
                && !myHero.isFainted()
                && !myMonster.isFainted();
    }

    public boolean isOver() {
        return myOver || myEscaped
                || myHero.isFainted()
                || myMonster.isFainted();
    }

    public boolean canUseHealingPotion() {
        return isActive() && myHero.getHealingPotions() > 0;
    }

    public boolean canUseVisionPotion() {
        return isActive() && myHero.getVisionPotions() > 0;
    }

    public BattleResult attack() {
        BattleResult result = new BattleResult();
        if (!ensureActive(result)) {
            return result;
        }

        int attacks = myHero.attacksPerRoundAgainst(myMonster);
        for (int i = 0; i < attacks && isActive(); i++) {
            performAttack(myHero, myMonster, result);
        }

        if (isActive()) {
            performMonsterResponse(result);
        }
        return finish(result);
    }

    public BattleResult specialSkill() {
        BattleResult result = new BattleResult();
        if (!ensureActive(result)) {
            return result;
        }

        int heroBefore = myHero.getHitPoints();
        int monsterBefore = myMonster.getHitPoints();
        result.add(myHero.getName() + " uses a special skill.");
        myHero.useSpecialSkill(myMonster);
        describeHitPointChange(myHero, heroBefore, result);
        describeHitPointChange(myMonster, monsterBefore, result);
        if (myHero.getHitPoints() == heroBefore
                && myMonster.getHitPoints() == monsterBefore) {
            result.add("The special skill has no visible effect.");
        }

        if (isActive()) {
            performMonsterResponse(result);
        }
        return finish(result);
    }

    public BattleResult useHealingPotion() {
        BattleResult result = new BattleResult();
        if (!ensureActive(result)) {
            return result;
        }
        if (myHero.getHealingPotions() <= 0) {
            result.add("No healing potions remain.");
            return result;
        }

        int before = myHero.getHitPoints();
        myHero.usePotion(new HealingPotion());
        int healed = myHero.getHitPoints() - before;
        if (healed > 0) {
            result.add(myHero.getName() + " drinks a healing potion and recovers "
                    + healed + " HP.");
        } else {
            result.add(myHero.getName()
                    + " drinks a healing potion, but is already at full health.");
        }

        if (isActive()) {
            performMonsterResponse(result);
        }
        return finish(result);
    }

    public BattleResult useVisionPotion() {
        BattleResult result = new BattleResult();
        if (!ensureActive(result)) {
            return result;
        }
        if (myHero.getVisionPotions() <= 0) {
            result.add("No vision potions remain.");
            return result;
        }

        result.add("Vision potions are for exploring the dungeon, not battle.");
        return result;
    }

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

    private void performMonsterResponse(final BattleResult theResult) {
        if (isActive()) {
            performAttack(myMonster, myHero, theResult);
        }
    }

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

    private boolean ensureActive(final BattleResult theResult) {
        if (isActive()) {
            return true;
        }
        theResult.add("The battle is already over.");
        return false;
    }

    private BattleResult finish(final BattleResult theResult) {
        if (myHero.isFainted()) {
            myOver = true;
            theResult.setHeroDefeated(true);
            theResult.add(myHero.getName() + " has fallen.");
        }
        if (myMonster.isFainted()) {
            myOver = true;
            theResult.setMonsterDefeated(true);
            theResult.add(myMonster.getName() + " is defeated.");
        }
        if (myEscaped) {
            myOver = true;
            theResult.setEscaped(true);
        }
        theResult.setBattleOver(isOver());
        return theResult;
    }

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

        private BattleResult() {
            myMessages = new ArrayList<>();
        }

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
