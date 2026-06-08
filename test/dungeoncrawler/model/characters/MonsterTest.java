package dungeoncrawler.model.characters;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for the {@link Monster} base behavior and the concrete monster stats.
 *
 * <p>{@code Skeleton} is used as the deterministic vehicle for base-class tests
 * because its data-driven constructor exposes every stat. Healing is made
 * deterministic by pinning the heal chance to 0.0 (never heals) or 1.0 with a
 * collapsed heal range (heals a fixed amount).</p>
 */
@DisplayName("Monster")
class MonsterTest {

    /** Skeleton that takes damage cleanly and never heals. */
    private static Skeleton nonHealingSkeleton(final int theHitPoints) {
        return new Skeleton(theHitPoints, 10, 10, 3, 1.0, 0.0, 0, 0);
    }

    @Nested
    @DisplayName("construction validation")
    class Construction {

        @ParameterizedTest
        @ValueSource(doubles = {-0.01, 1.01})
        @DisplayName("rejects a heal chance outside [0, 1]")
        void rejectsInvalidHealChance(final double theHealChance) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Skeleton(100, 10, 10, 3, 1.0, theHealChance, 5, 10));
        }

        @Test
        @DisplayName("rejects an invalid heal range")
        void rejectsInvalidHealRange() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new Skeleton(100, 10, 10, 3, 1.0, 0.5, -1, 10), "negative min heal"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new Skeleton(100, 10, 10, 3, 1.0, 0.5, 20, 10), "max < min heal"));
        }
    }

    @Nested
    @DisplayName("takeDamage")
    class TakeDamage {

        @Test
        @DisplayName("a non-healing monster loses exactly the damage dealt")
        void noHealMeansExactLoss() {
            final Skeleton skeleton = nonHealingSkeleton(100);

            final int lost = skeleton.takeDamage(40);

            assertAll(
                    () -> assertEquals(40, lost),
                    () -> assertEquals(60, skeleton.getHitPoints()));
        }

        @Test
        @DisplayName("a surviving monster heals a fixed amount after taking damage")
        void survivingMonsterHeals() {
            // Always heals (1.0) for exactly 10 HP.
            final Skeleton skeleton = new Skeleton(100, 10, 10, 3, 1.0, 1.0, 10, 10);

            final int lost = skeleton.takeDamage(20);

            assertAll(
                    () -> assertEquals(20, lost, "reported loss is the raw damage, before healing"),
                    () -> assertEquals(90, skeleton.getHitPoints(), "100 - 20 + 10 heal"));
        }

        @Test
        @DisplayName("a monster killed by the hit does not heal")
        void faintedMonsterDoesNotHeal() {
            final Skeleton skeleton = new Skeleton(30, 10, 10, 3, 1.0, 1.0, 10, 10);

            final int lost = skeleton.takeDamage(50);

            assertAll(
                    () -> assertEquals(30, lost),
                    () -> assertEquals(0, skeleton.getHitPoints(), "no heal once fainted"),
                    () -> assertTrue(skeleton.isFainted()));
        }

        @Test
        @DisplayName("zero damage triggers no heal")
        void zeroDamageNoHeal() {
            final Skeleton skeleton = new Skeleton(100, 10, 10, 3, 1.0, 1.0, 10, 10);
            skeleton.setMyHitPoints(80);

            final int lost = skeleton.takeDamage(0);

            assertAll(
                    () -> assertEquals(0, lost),
                    () -> assertEquals(80, skeleton.getHitPoints(), "no damage taken, so no heal"));
        }
    }

    @Nested
    @DisplayName("heal")
    class Heal {

        @Test
        @DisplayName("returns zero when the heal chance never triggers")
        void noHealWhenChanceZero() {
            final Skeleton skeleton = nonHealingSkeleton(100);
            skeleton.setMyHitPoints(50);

            assertEquals(0, skeleton.heal());
            assertEquals(50, skeleton.getHitPoints());
        }

        @Test
        @DisplayName("returns the fixed heal amount when the chance always triggers")
        void healsFixedAmount() {
            final Skeleton skeleton = new Skeleton(100, 10, 10, 3, 1.0, 1.0, 15, 15);
            skeleton.setMyHitPoints(50);

            assertEquals(15, skeleton.heal());
            assertEquals(65, skeleton.getHitPoints());
        }

        @Test
        @DisplayName("a fainted monster cannot heal")
        void faintedCannotHeal() {
            final Skeleton skeleton = new Skeleton(100, 10, 10, 3, 1.0, 1.0, 15, 15);
            skeleton.setMyHitPoints(0);

            assertEquals(0, skeleton.heal());
        }
    }

    @Nested
    @DisplayName("concrete monster defaults")
    class ConcreteDefaults {

        @Test
        @DisplayName("Ogre has its documented default stats")
        void ogreDefaults() {
            final Ogre ogre = new Ogre();

            assertAll(
                    () -> assertEquals(Ogre.OGRE_NAME, ogre.getName()),
                    () -> assertEquals(200, ogre.getMaxHitPoints()),
                    () -> assertEquals(30, ogre.getMinDamage()),
                    () -> assertEquals(60, ogre.getMaxDamage()),
                    () -> assertEquals(2, ogre.getAttackSpeed()),
                    () -> assertEquals(0.6, ogre.getHitChance()),
                    () -> assertEquals(0.1, ogre.getChanceToHeal()));
        }

        @Test
        @DisplayName("Gremlin has its documented default stats")
        void gremlinDefaults() {
            final Gremlin gremlin = new Gremlin();

            assertAll(
                    () -> assertEquals(Gremlin.GREMLIN_NAME, gremlin.getName()),
                    () -> assertEquals(70, gremlin.getMaxHitPoints()),
                    () -> assertEquals(5, gremlin.getAttackSpeed()),
                    () -> assertEquals(0.4, gremlin.getChanceToHeal()),
                    () -> assertEquals(20, gremlin.getMinHeal()),
                    () -> assertEquals(40, gremlin.getMaxHeal()));
        }

        @Test
        @DisplayName("Skeleton has its documented default stats")
        void skeletonDefaults() {
            final Skeleton skeleton = new Skeleton();

            assertAll(
                    () -> assertEquals(Skeleton.SKELETON_NAME, skeleton.getName()),
                    () -> assertEquals(100, skeleton.getMaxHitPoints()),
                    () -> assertEquals(3, skeleton.getAttackSpeed()),
                    () -> assertEquals(0.3, skeleton.getChanceToHeal()));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("summarizes the monster's current state")
        void summarizesState() {
            final Skeleton skeleton = new Skeleton(100, 30, 50, 3, 0.8, 0.3, 30, 50);

            final String text = skeleton.toString();

            assertAll(
                    () -> assertTrue(text.contains("Skeleton"), "includes the name"),
                    () -> assertTrue(text.contains("100/100"), "includes current/max HP"),
                    () -> assertTrue(text.contains("30-50"), "includes the damage range"));
        }
    }
}
