package dungeoncrawler.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dungeoncrawler.model.characters.ControllableHero;
import dungeoncrawler.model.characters.Hero;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link Potion} hierarchy: {@link HealingPotion},
 * {@link VisionPotion}, and the potency-range validation in {@link Potion}.
 */
@DisplayName("Potions")
class PotionTest {

    /** Test-only potion that exercises the random potency-range constructor. */
    private static final class RangePotion extends Potion {
        RangePotion(final int theMin, final int theMax) {
            super(theMin, theMax);
        }
    }

    /** A wounded hero used as a potion target. */
    private static Hero woundedHero(final int theCurrentHp) {
        final ControllableHero hero = new ControllableHero("Hero", 100, 5, 5, 4, 1.0, 0.0);
        hero.setMyHitPoints(theCurrentHp);
        return hero;
    }

    @Nested
    @DisplayName("Potion potency range")
    class PotencyRange {

        @RepeatedTest(20)
        @DisplayName("a random potency stays within the requested inclusive range")
        void potencyWithinRange() {
            final int potency = new RangePotion(5, 8).getPotency();
            assertTrue(potency >= 5 && potency <= 8, "potency out of range: " + potency);
        }

        @Test
        @DisplayName("a collapsed range yields exactly that value")
        void collapsedRange() {
            assertEquals(7, new RangePotion(7, 7).getPotency());
        }

        @Test
        @DisplayName("rejects an invalid potency range")
        void rejectsInvalidRange() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new RangePotion(-1, 5), "negative minimum"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new RangePotion(9, 3), "max below min"));
        }
    }

    @Nested
    @DisplayName("HealingPotion")
    class Healing {

        @RepeatedTest(20)
        @DisplayName("rolls a potency within the healing bounds")
        void potencyWithinHealingBounds() {
            final int potency = new HealingPotion().getPotency();
            assertTrue(potency >= HealingPotion.MIN_HEAL && potency <= HealingPotion.MAX_HEAL,
                    "potency out of range: " + potency);
        }

        @Test
        @DisplayName("apply restores the hero's HP by exactly the potion's potency")
        void applyHealsByPotency() {
            final HealingPotion potion = new HealingPotion();
            final Hero hero = woundedHero(40);

            potion.apply(hero);

            assertEquals(40 + potion.getPotency(), hero.getHitPoints());
        }

        @Test
        @DisplayName("apply tolerates a null hero")
        void applyNullHeroIsSafe() {
            // No exception expected.
            new HealingPotion().apply(null);
        }
    }

    @Nested
    @DisplayName("VisionPotion")
    class Vision {

        @Test
        @DisplayName("has a potency equal to the vision radius")
        void potencyEqualsRadius() {
            assertEquals(VisionPotion.VISION_RADIUS, new VisionPotion().getPotency());
        }

        @Test
        @DisplayName("apply does not change the hero's HP")
        void applyDoesNotHeal() {
            final Hero hero = woundedHero(40);

            new VisionPotion().apply(hero);

            assertEquals(40, hero.getHitPoints());
        }
    }
}
