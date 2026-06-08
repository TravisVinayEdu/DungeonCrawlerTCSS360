package dungeoncrawler.model.characters;

/**
 * Test-only concrete {@link Hero} that exposes every stat through its
 * constructor so individual tests can pin hit chance, block chance, and the
 * damage range to deterministic values.
 *
 * <p>The special skill is a no-op by default, which lets {@code Battle} tests
 * exercise the generic special-skill branch ("no visible effect") without
 * pulling in the randomness of {@code Warrior}, {@code Thief}, or
 * {@code Priestess}.</p>
 */
public class ControllableHero extends Hero {

    public ControllableHero(final String theName,
                            final int theHitPoints,
                            final int theMinDmg,
                            final int theMaxDmg,
                            final int theAttackSpd,
                            final double theHitChance,
                            final double theChanceToBlock) {
        super(theName, theHitPoints, theMinDmg, theMaxDmg,
                theAttackSpd, theHitChance, theChanceToBlock);
    }

    @Override
    public void useSpecialSkill(final DungeonCharacter theOpp) {
        // Intentionally empty: keeps the generic special-skill path deterministic.
    }
}
