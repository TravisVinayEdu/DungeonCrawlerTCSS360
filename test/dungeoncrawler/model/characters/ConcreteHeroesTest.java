package dungeoncrawler.model.characters;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Tests for the concrete heroes {@link Warrior}, {@link Thief}, and
 * {@link Priestess}.
 *
 * <p>Their special skills depend on the shared static {@code Random}, so the
 * exact outcome of a single call cannot be pinned. Instead these tests assert
 * deterministic <em>invariants</em> that must hold for every possible roll, and
 * use {@link RepeatedTest} where exercising the random branches repeatedly adds
 * confidence without making any single assertion flaky.</p>
 */
@DisplayName("Concrete heroes")
class ConcreteHeroesTest {

    /** A durable, heal-free dummy monster used as a skill target. */
    private static Monster sturdyTarget(final int theHitPoints) {
        // healChance 0 and a collapsed 0-0 heal range remove all healing noise.
        return new Skeleton(theHitPoints, 0, 0, 3, 1.0, 0.0, 0, 0);
    }

    @Nested
    @DisplayName("Warrior")
    class WarriorTests {

        @Test
        @DisplayName("is created with the documented base stats")
        void hasExpectedStats() {
            final Warrior warrior = new Warrior("Conan");

            assertAll(
                    () -> assertEquals("Conan", warrior.getName()),
                    () -> assertEquals(Warrior.HP, warrior.getMaxHitPoints()),
                    () -> assertEquals(Warrior.MIN_DMG, warrior.getMinDamage()),
                    () -> assertEquals(60, warrior.getMaxDamage()),
                    () -> assertEquals(Warrior.SPD, warrior.getAttackSpeed()),
                    () -> assertEquals(Warrior.HIT, warrior.getHitChance()),
                    () -> assertEquals(Warrior.BLOCK, warrior.getChanceToBlock()));
        }

        @RepeatedTest(50)
        @DisplayName("crushing blow either misses or deals 75-175 damage")
        void crushingBlowDamageInvariant() {
            final Warrior warrior = new Warrior("Conan");
            final Monster target = sturdyTarget(300);

            warrior.crushingBlow(target);

            final int hp = target.getHitPoints();
            // Miss -> 300; hit -> 300 minus [75,175] => [125,225].
            assertTrue(hp == 300 || (hp >= 125 && hp <= 225),
                    "unexpected target HP after crushing blow: " + hp);
        }

        @Test
        @DisplayName("crushing blow does nothing against a null or fainted target")
        void crushingBlowGuards() {
            final Warrior warrior = new Warrior("Conan");
            final Monster fainted = sturdyTarget(50);
            fainted.setMyHitPoints(0);

            warrior.crushingBlow(null);
            warrior.crushingBlow(fainted);

            assertEquals(0, fainted.getHitPoints());
        }
    }

    @Nested
    @DisplayName("Thief")
    class ThiefTests {

        @Test
        @DisplayName("is created with the documented base stats")
        void hasExpectedStats() {
            final Thief thief = new Thief("Garrett");

            assertAll(
                    () -> assertEquals(Thief.HP, thief.getMaxHitPoints()),
                    () -> assertEquals(Thief.MIN_DMG, thief.getMinDamage()),
                    () -> assertEquals(40, thief.getMaxDamage()),
                    () -> assertEquals(Thief.SPD, thief.getAttackSpeed()),
                    () -> assertEquals(Thief.BLOCK, thief.getChanceToBlock()));
        }

        @RepeatedTest(50)
        @DisplayName("surprise attack never increases the target's HP and never hurts the thief")
        void surpriseAttackInvariant() {
            final Thief thief = new Thief("Garrett");
            final Monster target = sturdyTarget(500);
            final int targetBefore = target.getHitPoints();
            final int thiefBefore = thief.getHitPoints();

            thief.surpriseAttack(target);

            assertAll(
                    () -> assertTrue(target.getHitPoints() <= targetBefore,
                            "target HP must not rise"),
                    () -> assertEquals(thiefBefore, thief.getHitPoints(),
                            "the thief takes no self-damage"));
        }

        @Test
        @DisplayName("surprise attack does nothing against a null or fainted target")
        void surpriseAttackGuards() {
            final Thief thief = new Thief("Garrett");
            final Monster fainted = sturdyTarget(50);
            fainted.setMyHitPoints(0);

            thief.surpriseAttack(null);
            thief.surpriseAttack(fainted);

            assertEquals(0, fainted.getHitPoints());
        }
    }

    @Nested
    @DisplayName("Priestess")
    class PriestessTests {

        @Test
        @DisplayName("is created with the documented base stats")
        void hasExpectedStats() {
            final Priestess priestess = new Priestess("Mercy");

            assertAll(
                    () -> assertEquals(Priestess.HP, priestess.getMaxHitPoints()),
                    () -> assertEquals(Priestess.MIN_DMG, priestess.getMinDamage()),
                    () -> assertEquals(45, priestess.getMaxDamage()),
                    () -> assertEquals(Priestess.SPD, priestess.getAttackSpeed()),
                    () -> assertEquals(Priestess.BLOCK, priestess.getChanceToBlock()));
        }

        @RepeatedTest(50)
        @DisplayName("heal restores 25-45 HP to a wounded priestess without exceeding the max")
        void healInvariant() {
            final Priestess priestess = new Priestess("Mercy");
            priestess.setMyHitPoints(10);

            final int restored = priestess.heal();

            assertAll(
                    () -> assertTrue(restored >= 25 && restored <= 45,
                            "healed amount out of range: " + restored),
                    () -> assertEquals(10 + restored, priestess.getHitPoints()),
                    () -> assertTrue(priestess.getHitPoints() <= priestess.getMaxHitPoints()));
        }

        @Test
        @DisplayName("heal is capped at max HP for an almost-full priestess")
        void healCappedAtMax() {
            final Priestess priestess = new Priestess("Mercy");
            priestess.setMyHitPoints(priestess.getMaxHitPoints() - 1);

            final int restored = priestess.heal();

            assertAll(
                    () -> assertEquals(1, restored, "only the missing HP is restored"),
                    () -> assertEquals(priestess.getMaxHitPoints(), priestess.getHitPoints()));
        }
    }
}
