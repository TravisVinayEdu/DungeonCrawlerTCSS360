package dungeoncrawler.combat;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dungeoncrawler.combat.Battle.BattleResult;
import dungeoncrawler.model.characters.ControllableHero;
import dungeoncrawler.model.characters.Hero;
import dungeoncrawler.model.characters.Monster;
import dungeoncrawler.model.characters.Priestess;
import dungeoncrawler.model.characters.Skeleton;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Behavioral tests for the {@link Battle} combat engine.
 *
 * <p>{@code Battle} rolls its own hit/skill randomness, so determinism is
 * achieved by pinning combatant stats: a hero with hit chance 1.0 always lands,
 * and a "harmless" monster with a collapsed 0-0 damage range and no healing can
 * never change the hero's HP. Heroes use {@link ControllableHero} so block
 * chance can be set to 0 and the special skill is a deterministic no-op.</p>
 */
@DisplayName("Battle")
class BattleTest {

    /** A hero that always lands its attack for a fixed amount and never blocks. */
    private static ControllableHero hero(final int theHp, final int theDamage) {
        return new ControllableHero("Hero", theHp, theDamage, theDamage, 3, 1.0, 0.0);
    }

    /** A monster that deals no damage and never heals (cannot affect the hero). */
    private static Monster harmlessMonster(final int theHp) {
        return new Skeleton(theHp, 0, 0, 3, 1.0, 0.0, 0, 0);
    }

    /** A monster that always lands a fixed amount of damage and never heals. */
    private static Monster deadlyMonster(final int theHp, final int theDamage) {
        return new Skeleton(theHp, theDamage, theDamage, 3, 1.0, 0.0, 0, 0);
    }

    private static boolean hasMessageContaining(final BattleResult theResult,
                                                final String theFragment) {
        return theResult.getMessages().stream().anyMatch(m -> m.contains(theFragment));
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("rejects a null hero or monster")
        void rejectsNulls() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new Battle(null, harmlessMonster(50))),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new Battle(hero(100, 10), null)));
        }

        @Test
        @DisplayName("a battle starting with a fainted combatant is already over")
        void faintedCombatantEndsBattle() {
            final Monster fainted = harmlessMonster(30);
            fainted.setMyHitPoints(0);

            final Battle battle = new Battle(hero(100, 10), fainted);

            assertAll(
                    () -> assertFalse(battle.isActive()),
                    () -> assertTrue(battle.isOver()));
        }

        @Test
        @DisplayName("a fresh battle between healthy combatants is active")
        void freshBattleIsActive() {
            final Battle battle = new Battle(hero(100, 10), harmlessMonster(50));

            assertAll(
                    () -> assertTrue(battle.isActive()),
                    () -> assertFalse(battle.isOver()));
        }
    }

    @Nested
    @DisplayName("attack")
    class Attack {

        @Test
        @DisplayName("the hero defeats the monster and the battle records the victory")
        void heroDefeatsMonster() {
            final Battle battle = new Battle(hero(100, 50), harmlessMonster(30));

            final BattleResult result = battle.attack();

            assertAll(
                    () -> assertTrue(result.isMonsterDefeated()),
                    () -> assertFalse(result.isHeroDefeated()),
                    () -> assertTrue(result.isBattleOver()),
                    () -> assertTrue(battle.isOver()),
                    () -> assertTrue(hasMessageContaining(result, "is defeated")));
        }

        @Test
        @DisplayName("the monster defeats the hero on its return swing")
        void monsterDefeatsHero() {
            // Hero deals 0 so the 100-HP monster survives to strike back for 50.
            final Battle battle = new Battle(hero(10, 0), deadlyMonster(100, 50));

            final BattleResult result = battle.attack();

            assertAll(
                    () -> assertTrue(result.isHeroDefeated()),
                    () -> assertFalse(result.isMonsterDefeated()),
                    () -> assertTrue(battle.isOver()),
                    () -> assertTrue(hasMessageContaining(result, "has fallen")));
        }

        @Test
        @DisplayName("a fast hero is granted multiple attacks in one turn")
        void fastHeroGetsMultipleAttacks() {
            final ControllableHero fastHero =
                    new ControllableHero("Hero", 100, 0, 0, 9, 1.0, 0.0);
            final Battle battle = new Battle(fastHero, harmlessMonster(100));

            final BattleResult result = battle.attack();

            assertTrue(hasMessageContaining(result, "grants 3 attacks"),
                    "speed 9 vs 3 should grant 3 attacks");
        }

        @Test
        @DisplayName("attacking an already-finished battle reports it is over")
        void attackAfterBattleOver() {
            final Monster fainted = harmlessMonster(30);
            fainted.setMyHitPoints(0);
            final Battle battle = new Battle(hero(100, 10), fainted);

            final BattleResult result = battle.attack();

            assertTrue(hasMessageContaining(result, "battle is already over"));
        }
    }

    @Nested
    @DisplayName("special skill")
    class SpecialSkill {

        @Test
        @DisplayName("using a generic special skill starts the cooldown")
        void genericSkillStartsCooldown() {
            final Battle battle = new Battle(hero(100, 0), harmlessMonster(100));

            battle.specialSkill();

            assertAll(
                    () -> assertEquals(3, battle.getSpecialSkillCooldown()),
                    () -> assertFalse(battle.canUseSpecialSkill()));
        }

        @Test
        @DisplayName("a second skill use during cooldown is rejected with a message")
        void skillBlockedDuringCooldown() {
            final Battle battle = new Battle(hero(100, 0), harmlessMonster(100));
            battle.specialSkill();

            final BattleResult result = battle.specialSkill();

            assertTrue(hasMessageContaining(result, "cooling down"));
        }

        @Test
        @DisplayName("each subsequent action ticks the cooldown down")
        void cooldownDecrementsWithActions() {
            final Battle battle = new Battle(hero(100, 0), harmlessMonster(100));
            battle.specialSkill();

            battle.attack();

            assertEquals(2, battle.getSpecialSkillCooldown());
        }

        @Test
        @DisplayName("a Priestess special skill heals the wounded hero")
        void priestessHeals() {
            final Priestess priestess = new Priestess("Mercy");
            priestess.setMyHitPoints(20);
            final Battle battle = new Battle(priestess, harmlessMonster(100));

            battle.specialSkill();

            assertTrue(priestess.getHitPoints() > 20, "the priestess should recover HP");
        }
    }

    @Nested
    @DisplayName("healing potion")
    class HealingPotion {

        @Test
        @DisplayName("drinking a potion heals the hero, consumes it, and starts the cooldown")
        void healingPotionHeals() {
            final ControllableHero hero = hero(100, 0);
            hero.setMyHitPoints(50);
            hero.addHealingPotion();
            final Battle battle = new Battle(hero, harmlessMonster(100));

            final BattleResult result = battle.useHealingPotion();

            assertAll(
                    () -> assertTrue(hero.getHitPoints() > 50, "HP recovered"),
                    () -> assertEquals(0, hero.getHealingPotions(), "potion consumed"),
                    () -> assertEquals(2, battle.getPotionCooldown()),
                    () -> assertTrue(hasMessageContaining(result, "recovers")));
        }

        @Test
        @DisplayName("with no potions in inventory the request is refused")
        void noHealingPotions() {
            final Battle battle = new Battle(hero(100, 0), harmlessMonster(100));

            final BattleResult result = battle.useHealingPotion();

            assertTrue(hasMessageContaining(result, "No healing potions remain"));
        }

        @Test
        @DisplayName("a full-health hero still consumes the potion but recovers nothing")
        void healingPotionAtFullHealth() {
            final ControllableHero hero = hero(100, 0);
            hero.addHealingPotion();
            final Battle battle = new Battle(hero, harmlessMonster(100));

            final BattleResult result = battle.useHealingPotion();

            assertAll(
                    () -> assertEquals(100, hero.getHitPoints()),
                    () -> assertEquals(0, hero.getHealingPotions()),
                    () -> assertTrue(hasMessageContaining(result, "already at full health")));
        }

        @Test
        @DisplayName("canUseHealingPotion reflects inventory and battle state")
        void canUseHealingPotionGating() {
            final ControllableHero hero = hero(100, 0);
            final Battle battle = new Battle(hero, harmlessMonster(100));
            assertFalse(battle.canUseHealingPotion(), "no potions yet");

            hero.addHealingPotion();
            assertTrue(battle.canUseHealingPotion());
        }
    }

    @Nested
    @DisplayName("vision potion")
    class VisionPotion {

        @Test
        @DisplayName("vision potions are rejected during battle without being consumed")
        void visionPotionNotUsableInBattle() {
            final ControllableHero hero = hero(100, 0);
            hero.addVisionPotion();
            final Battle battle = new Battle(hero, harmlessMonster(100));

            final BattleResult result = battle.useVisionPotion();

            assertAll(
                    () -> assertTrue(hasMessageContaining(result, "not battle")),
                    () -> assertEquals(1, hero.getVisionPotions(), "vision potion is not consumed"));
        }

        @Test
        @DisplayName("with no vision potions the request is refused")
        void noVisionPotions() {
            final Battle battle = new Battle(hero(100, 0), harmlessMonster(100));

            final BattleResult result = battle.useVisionPotion();

            assertTrue(hasMessageContaining(result, "No vision potions remain"));
        }
    }

    @Nested
    @DisplayName("run")
    class Run {

        @Test
        @DisplayName("running ends the battle as an escape")
        void runEscapes() {
            final Battle battle = new Battle(hero(100, 0), harmlessMonster(100));

            final BattleResult result = battle.run();

            assertAll(
                    () -> assertTrue(result.isEscaped()),
                    () -> assertTrue(battle.isOver()),
                    () -> assertFalse(battle.isActive()),
                    () -> assertTrue(hasMessageContaining(result, "escapes")));
        }

        @Test
        @DisplayName("running a second time reports the hero is already away")
        void runTwice() {
            final Battle battle = new Battle(hero(100, 0), harmlessMonster(100));
            battle.run();

            final BattleResult result = battle.run();

            assertTrue(hasMessageContaining(result, "already away"));
        }
    }

    @Nested
    @DisplayName("BattleResult")
    class Result {

        @Test
        @DisplayName("exposes an unmodifiable message list")
        void messagesAreUnmodifiable() {
            final Battle battle = new Battle(hero(100, 0), harmlessMonster(100));
            final BattleResult result = battle.run();

            assertThrows(UnsupportedOperationException.class,
                    () -> result.getMessages().add("tampering"));
        }
    }
}
