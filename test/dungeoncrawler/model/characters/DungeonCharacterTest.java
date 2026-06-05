package dungeoncrawler.model.characters;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Behavioral tests for the abstract {@link DungeonCharacter} base class.
 *
 * <p>The class lives in the production package so the test can subclass the
 * abstract type and reach its {@code protected} hooks ({@code generateDamage},
 * {@code randomInRange}). All randomness is pinned to deterministic values:
 * {@code hitChance == 1.0} guarantees an attack lands, and a collapsed damage
 * range ({@code min == max}) makes damage exact.</p>
 */
@DisplayName("DungeonCharacter")
class DungeonCharacterTest {

    /** Minimal concrete subclass that exposes the protected hooks for testing. */
    private static final class StubCharacter extends DungeonCharacter {
        StubCharacter(final String theName,
                      final int theHitPoints,
                      final int theMinDmg,
                      final int theMaxDmg,
                      final int theAttackSpd,
                      final double theHitChance) {
            super(theName, theHitPoints, theMinDmg, theMaxDmg, theAttackSpd, theHitChance);
        }

        int exposedGenerateDamage() {
            return generateDamage();
        }

        static int exposedRandomInRange(final int theMin, final int theMax) {
            return randomInRange(theMin, theMax);
        }

        static boolean exposedChanceSucceeds(final double theChance) {
            return chanceSucceeds(theChance);
        }
    }

    /** Builds a healthy, always-hitting character that deals exactly 5 damage. */
    private static StubCharacter newCharacter() {
        return new StubCharacter("Hero", 100, 5, 5, 4, 1.0);
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("stores the supplied stats and starts at full health")
        void storesStatsAndStartsAtFullHealth() {
            final StubCharacter c = new StubCharacter(" Hero ", 100, 5, 9, 4, 0.75);

            assertAll(
                    () -> assertEquals("Hero", c.getName(), "name is trimmed"),
                    () -> assertEquals(100, c.getMaxHitPoints()),
                    () -> assertEquals(100, c.getHitPoints(), "starts at full HP"),
                    () -> assertEquals(5, c.getMinDamage()),
                    () -> assertEquals(9, c.getMaxDamage()),
                    () -> assertEquals(4, c.getAttackSpeed()),
                    () -> assertEquals(0.75, c.getHitChance()),
                    () -> assertFalse(c.isFainted()));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("rejects a null, empty, or blank name")
        void rejectsBlankName(final String theName) {
            assertThrows(IllegalArgumentException.class,
                    () -> new StubCharacter(theName, 100, 5, 5, 4, 1.0));
        }

        @Test
        @DisplayName("rejects invalid numeric arguments")
        void rejectsInvalidNumericArguments() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new StubCharacter("H", 0, 5, 5, 4, 1.0), "HP must be positive"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new StubCharacter("H", -1, 5, 5, 4, 1.0), "HP must be positive"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new StubCharacter("H", 100, -1, 5, 4, 1.0), "min damage < 0"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new StubCharacter("H", 100, 9, 5, 4, 1.0), "max < min damage"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new StubCharacter("H", 100, 5, 5, 0, 1.0), "attack speed must be positive"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new StubCharacter("H", 100, 5, 5, 4, -0.01), "hit chance below 0"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new StubCharacter("H", 100, 5, 5, 4, 1.01), "hit chance above 1"));
        }

        @Test
        @DisplayName("accepts the boundary hit-chance values 0.0 and 1.0")
        void acceptsBoundaryHitChances() {
            assertAll(
                    () -> assertEquals(0.0, new StubCharacter("H", 1, 0, 0, 1, 0.0).getHitChance()),
                    () -> assertEquals(1.0, new StubCharacter("H", 1, 0, 0, 1, 1.0).getHitChance()));
        }
    }

    @Nested
    @DisplayName("setMyHitPoints")
    class SetHitPoints {

        @Test
        @DisplayName("clamps below 0 up to 0 and above max down to max")
        void clampsToValidRange() {
            final StubCharacter c = newCharacter();

            c.setMyHitPoints(-50);
            assertEquals(0, c.getHitPoints(), "negative clamps to 0");

            c.setMyHitPoints(9999);
            assertEquals(100, c.getHitPoints(), "over-max clamps to max");

            c.setMyHitPoints(42);
            assertEquals(42, c.getHitPoints(), "in-range value is kept");
        }
    }

    @Nested
    @DisplayName("takeDamage")
    class TakeDamage {

        @Test
        @DisplayName("reduces HP and returns the amount actually lost")
        void reducesHpAndReportsLoss() {
            final StubCharacter c = newCharacter();

            final int lost = c.takeDamage(30);

            assertAll(
                    () -> assertEquals(30, lost),
                    () -> assertEquals(70, c.getHitPoints()));
        }

        @Test
        @DisplayName("never lets HP fall below 0 and reports only the HP truly lost")
        void clampsLethalDamage() {
            final StubCharacter c = newCharacter();

            final int lost = c.takeDamage(250);

            assertAll(
                    () -> assertEquals(100, lost, "only the remaining 100 HP is lost"),
                    () -> assertEquals(0, c.getHitPoints()),
                    () -> assertTrue(c.isFainted()));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -5})
        @DisplayName("treats non-positive damage as zero")
        void ignoresNonPositiveDamage(final int theDamage) {
            final StubCharacter c = newCharacter();

            final int lost = c.takeDamage(theDamage);

            assertAll(
                    () -> assertEquals(0, lost),
                    () -> assertEquals(100, c.getHitPoints()));
        }

        @Test
        @DisplayName("defendAgainstAttack behaves like takeDamage for the base class")
        void defendDelegatesToTakeDamage() {
            final StubCharacter c = newCharacter();

            assertEquals(20, c.defendAgainstAttack(20));
            assertEquals(80, c.getHitPoints());
        }
    }

    @Nested
    @DisplayName("heal")
    class Heal {

        @Test
        @DisplayName("restores HP and returns the amount gained")
        void restoresHp() {
            final StubCharacter c = newCharacter();
            c.setMyHitPoints(40);

            final int gained = c.heal(25);

            assertAll(
                    () -> assertEquals(25, gained),
                    () -> assertEquals(65, c.getHitPoints()));
        }

        @Test
        @DisplayName("never heals above max HP")
        void cannotExceedMax() {
            final StubCharacter c = newCharacter();
            c.setMyHitPoints(90);

            final int gained = c.heal(50);

            assertAll(
                    () -> assertEquals(10, gained, "only the missing 10 HP is restored"),
                    () -> assertEquals(100, c.getHitPoints()));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -10})
        @DisplayName("treats non-positive heal amounts as zero")
        void ignoresNonPositiveHeal(final int theAmount) {
            final StubCharacter c = newCharacter();
            c.setMyHitPoints(50);

            assertEquals(0, c.heal(theAmount));
            assertEquals(50, c.getHitPoints());
        }
    }

    @Nested
    @DisplayName("attack")
    class Attack {

        @Test
        @DisplayName("damages a living opponent when the attack is guaranteed to land")
        void landsGuaranteedAttack() {
            final StubCharacter attacker = new StubCharacter("A", 100, 5, 5, 4, 1.0);
            final StubCharacter defender = newCharacter();

            attacker.attack(defender);

            assertEquals(95, defender.getHitPoints(), "exactly 5 damage applied");
        }

        @Test
        @DisplayName("does nothing when the opponent is null")
        void ignoresNullOpponent() {
            final StubCharacter attacker = new StubCharacter("A", 100, 5, 5, 4, 1.0);
            // No exception expected.
            attacker.attack(null);
        }

        @Test
        @DisplayName("does nothing when the opponent has already fainted")
        void ignoresFaintedOpponent() {
            final StubCharacter attacker = new StubCharacter("A", 100, 5, 5, 4, 1.0);
            final StubCharacter defender = newCharacter();
            defender.setMyHitPoints(0);

            attacker.attack(defender);

            assertEquals(0, defender.getHitPoints());
        }
    }

    @Nested
    @DisplayName("attacksPerRoundAgainst")
    class AttacksPerRound {

        @Test
        @DisplayName("returns at least one attack even against a slower opponent ratio")
        void neverBelowOne() {
            final StubCharacter slow = new StubCharacter("Slow", 100, 5, 5, 2, 1.0);
            final StubCharacter fast = new StubCharacter("Fast", 100, 5, 5, 8, 1.0);

            assertEquals(1, slow.attacksPerRoundAgainst(fast), "2/8 rounds down to 0, floored to 1");
        }

        @Test
        @DisplayName("grants multiple attacks when much faster than the opponent")
        void multipleAttacksWhenFaster() {
            final StubCharacter fast = new StubCharacter("Fast", 100, 5, 5, 9, 1.0);
            final StubCharacter slow = new StubCharacter("Slow", 100, 5, 5, 3, 1.0);

            assertEquals(3, fast.attacksPerRoundAgainst(slow));
        }

        @Test
        @DisplayName("returns one attack against a null opponent")
        void oneAttackAgainstNull() {
            assertEquals(1, newCharacter().attacksPerRoundAgainst(null));
        }
    }

    @Nested
    @DisplayName("protected helpers")
    class ProtectedHelpers {

        @Test
        @DisplayName("generateDamage returns the single value of a collapsed range")
        void generateDamageOnCollapsedRange() {
            final StubCharacter c = new StubCharacter("H", 100, 7, 7, 4, 1.0);
            assertEquals(7, c.exposedGenerateDamage());
        }

        @Test
        @DisplayName("randomInRange always returns the bound when min == max")
        void randomInRangeCollapsed() {
            assertEquals(3, StubCharacter.exposedRandomInRange(3, 3));
        }

        @Test
        @DisplayName("randomInRange rejects an inverted range")
        void randomInRangeRejectsInvertedRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> StubCharacter.exposedRandomInRange(9, 1));
        }

        @Test
        @DisplayName("chanceSucceeds is deterministic at the 0.0 and 1.0 boundaries")
        void chanceSucceedsBoundaries() {
            assertAll(
                    () -> assertTrue(StubCharacter.exposedChanceSucceeds(1.0), "1.0 always succeeds"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> StubCharacter.exposedChanceSucceeds(1.5)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> StubCharacter.exposedChanceSucceeds(-0.5)));
        }
    }
}
