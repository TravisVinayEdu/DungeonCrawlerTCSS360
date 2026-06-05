package dungeoncrawler.model.characters;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dungeoncrawler.model.HealingPotion;
import dungeoncrawler.model.Pillar;
import dungeoncrawler.model.Potion;
import dungeoncrawler.model.VisionPotion;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Behavioral tests for the {@link Hero} base class, driven through the
 * deterministic {@link ControllableHero} fixture.
 *
 * <p>Block behavior is pinned with a 0.0 or 1.0 block chance so the random roll
 * collapses to a single outcome.</p>
 */
@DisplayName("Hero")
class HeroTest {

    /** Hero that always blocks (block chance 1.0). */
    private static ControllableHero alwaysBlockingHero() {
        return new ControllableHero("Block", 100, 5, 5, 4, 1.0, 1.0);
    }

    /** Hero that never blocks (block chance 0.0). */
    private static ControllableHero neverBlockingHero() {
        return new ControllableHero("NoBlock", 100, 5, 5, 4, 1.0, 0.0);
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("stores the block chance and starts with no potions or pillars")
        void storesBlockChanceAndEmptyInventory() {
            final ControllableHero hero = new ControllableHero("Hero", 100, 5, 9, 4, 0.8, 0.3);

            assertAll(
                    () -> assertEquals(0.3, hero.getChanceToBlock()),
                    () -> assertEquals(0, hero.getHealingPotions()),
                    () -> assertEquals(0, hero.getVisionPotions()),
                    () -> assertTrue(hero.getPillars().isEmpty()),
                    () -> assertFalse(hero.hasAllPillars()));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.01, 1.01})
        @DisplayName("rejects a block chance outside [0, 1]")
        void rejectsInvalidBlockChance(final double theBlockChance) {
            assertThrows(IllegalArgumentException.class,
                    () -> new ControllableHero("Hero", 100, 5, 5, 4, 1.0, theBlockChance));
        }
    }

    @Nested
    @DisplayName("block / defendAgainstAttack")
    class Blocking {

        @Test
        @DisplayName("block() is deterministic at the chance boundaries")
        void blockBoundaries() {
            assertAll(
                    () -> assertTrue(alwaysBlockingHero().block()),
                    () -> assertFalse(neverBlockingHero().block()));
        }

        @Test
        @DisplayName("a guaranteed block negates all incoming damage")
        void blockNegatesDamage() {
            final ControllableHero hero = alwaysBlockingHero();

            final int lost = hero.defendAgainstAttack(40);

            assertAll(
                    () -> assertEquals(0, lost),
                    () -> assertEquals(100, hero.getHitPoints()));
        }

        @Test
        @DisplayName("damage applies normally when the hero never blocks")
        void noBlockTakesDamage() {
            final ControllableHero hero = neverBlockingHero();

            final int lost = hero.defendAgainstAttack(40);

            assertAll(
                    () -> assertEquals(40, lost),
                    () -> assertEquals(60, hero.getHitPoints()));
        }

        @Test
        @DisplayName("zero damage is never blocked and never changes HP")
        void zeroDamageIsNotBlocked() {
            final ControllableHero hero = alwaysBlockingHero();

            assertEquals(0, hero.defendAgainstAttack(0));
            assertEquals(100, hero.getHitPoints());
        }
    }

    @Nested
    @DisplayName("attacksPerRoundAgainst")
    class AttacksPerRound {

        @Test
        @DisplayName("matches a faster monster's attack count")
        void matchesFasterMonster() {
            final ControllableHero hero = new ControllableHero("Hero", 100, 5, 5, 4, 1.0, 0.0);
            // Monster speed 8 vs hero speed 4 -> monster would attack twice.
            final Monster monster = new Skeleton(100, 5, 5, 8, 1.0, 0.0, 0, 0);

            assertEquals(2, hero.attacksPerRoundAgainst(monster),
                    "hero is granted at least as many attacks as the faster monster");
        }

        @Test
        @DisplayName("uses only the hero's own ratio against a non-monster opponent")
        void ignoresMonsterParityForHeroOpponent() {
            final ControllableHero fast = new ControllableHero("Fast", 100, 5, 5, 9, 1.0, 0.0);
            final ControllableHero slow = new ControllableHero("Slow", 100, 5, 5, 3, 1.0, 0.0);

            assertEquals(3, fast.attacksPerRoundAgainst(slow));
        }
    }

    @Nested
    @DisplayName("potions")
    class Potions {

        @Test
        @DisplayName("add and count healing and vision potions independently")
        void addAndCountPotions() {
            final ControllableHero hero = neverBlockingHero();

            hero.addHealingPotion();
            hero.addHealingPotion();
            hero.addVisionPotion();

            assertAll(
                    () -> assertEquals(2, hero.getHealingPotions()),
                    () -> assertEquals(1, hero.getVisionPotions()));
        }

        @Test
        @DisplayName("setters clamp negative counts to zero")
        void settersClampNegative() {
            final ControllableHero hero = neverBlockingHero();

            hero.setHealingPotions(-3);
            hero.setVisionPotions(-1);

            assertAll(
                    () -> assertEquals(0, hero.getHealingPotions()),
                    () -> assertEquals(0, hero.getVisionPotions()));
        }

        @Test
        @DisplayName("consuming a healing potion decrements the count and heals the hero")
        void usingHealingPotionConsumesAndHeals() {
            final ControllableHero hero = neverBlockingHero();
            hero.setMyHitPoints(40);
            hero.addHealingPotion();

            hero.usePotion(new HealingPotion());

            assertAll(
                    () -> assertEquals(0, hero.getHealingPotions(), "potion is consumed"),
                    () -> assertTrue(hero.getHitPoints() > 40, "HP increased"),
                    () -> assertTrue(hero.getHitPoints() <= 100, "never exceeds max"));
        }

        @Test
        @DisplayName("a healing potion is ignored when the hero has none in inventory")
        void healingPotionIgnoredWhenNoneHeld() {
            final ControllableHero hero = neverBlockingHero();
            hero.setMyHitPoints(40);

            hero.usePotion(new HealingPotion());

            assertEquals(40, hero.getHitPoints(), "no potion held, so no healing");
        }

        @Test
        @DisplayName("consuming a vision potion decrements the count without changing HP")
        void usingVisionPotionConsumesOnly() {
            final ControllableHero hero = neverBlockingHero();
            hero.setMyHitPoints(40);
            hero.addVisionPotion();

            hero.usePotion(new VisionPotion());

            assertAll(
                    () -> assertEquals(0, hero.getVisionPotions()),
                    () -> assertEquals(40, hero.getHitPoints(), "vision potion does not heal"));
        }

        @Test
        @DisplayName("a null potion is ignored")
        void nullPotionIgnored() {
            final ControllableHero hero = neverBlockingHero();
            hero.addHealingPotion();

            hero.usePotion((Potion) null);

            assertEquals(1, hero.getHealingPotions(), "nothing consumed");
        }
    }

    @Nested
    @DisplayName("pillars")
    class Pillars {

        @Test
        @DisplayName("collects pillars and reports completion only when all four are held")
        void collectsUntilComplete() {
            final ControllableHero hero = neverBlockingHero();

            hero.addPillar(Pillar.INHERITANCE);
            hero.addPillar(Pillar.POLYMORPHISM);
            hero.addPillar(Pillar.ABSTRACTION);
            assertFalse(hero.hasAllPillars(), "still missing ENCAPSULATION");

            hero.addPillar(Pillar.ENCAPSULATION);
            assertTrue(hero.hasAllPillars());
        }

        @Test
        @DisplayName("a duplicate pillar does not change the held set")
        void duplicatePillarIgnored() {
            final ControllableHero hero = neverBlockingHero();

            hero.addPillar(Pillar.INHERITANCE);
            hero.addPillar(Pillar.INHERITANCE);

            assertAll(
                    () -> assertTrue(hero.hasPillar(Pillar.INHERITANCE)),
                    () -> assertEquals(1, hero.getPillars().size()));
        }

        @Test
        @DisplayName("a null pillar is ignored")
        void nullPillarIgnored() {
            final ControllableHero hero = neverBlockingHero();

            hero.addPillar(null);

            assertTrue(hero.getPillars().isEmpty());
        }

        @Test
        @DisplayName("getPillars returns a defensive copy")
        void getPillarsIsDefensiveCopy() {
            final ControllableHero hero = neverBlockingHero();
            hero.addPillar(Pillar.ABSTRACTION);

            final Set<Pillar> snapshot = hero.getPillars();
            snapshot.clear();

            assertTrue(hero.hasPillar(Pillar.ABSTRACTION),
                    "mutating the returned set must not affect the hero");
        }
    }
}
