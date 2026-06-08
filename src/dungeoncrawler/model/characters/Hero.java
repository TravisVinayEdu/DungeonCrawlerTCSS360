package dungeoncrawler.model.characters;

import dungeoncrawler.model.HealingPotion;
import dungeoncrawler.model.Pillar;
import dungeoncrawler.model.Potion;
import dungeoncrawler.model.VisionPotion;

import java.util.EnumSet;
import java.util.Set;

/**
 * Abstract parent for player-controlled hero classes.
 *
 * <p>A hero extends the shared dungeon-character statistics with potion
 * inventory, collected Pillars of OO, a chance to block incoming attacks, and
 * a class-specific special skill.</p>
 */
public abstract class Hero extends DungeonCharacter {
    private final double myChanceToBlock;
    private int myHealingPotions;
    private int myVisionPotions;
    private final EnumSet<Pillar> myPillars;

    /**
     * Creates a hero with the supplied combat statistics and block chance.
     *
     * @param theName hero name entered by the player
     * @param theHitPoints maximum and starting hit points
     * @param theMinDmg minimum regular attack damage
     * @param theMaxDmg maximum regular attack damage
     * @param theAttackSpd attack speed used in battle turn calculations
     * @param theHitChance chance to hit with a regular attack
     * @param theChanceToBlock chance to block incoming attack damage
     * @throws IllegalArgumentException if the block chance is outside 0.0 to 1.0
     */
    public Hero(final String theName,
                final int theHitPoints,
                final int theMinDmg,
                final int theMaxDmg,
                final int theAttackSpd,
                final double theHitChance,
                final double theChanceToBlock) {
        super(theName, theHitPoints, theMinDmg, theMaxDmg, theAttackSpd, theHitChance);
        if (theChanceToBlock < 0.0 || theChanceToBlock > 1.0) {
            throw new IllegalArgumentException("Block chance must be between 0 and 1.");
        }
        myChanceToBlock = theChanceToBlock;
        myPillars = EnumSet.noneOf(Pillar.class);
    }

    /**
     * Attempts to block incoming attack damage.
     *
     * @return true when the block roll succeeds
     */
    public boolean block() {
        return chanceSucceeds(myChanceToBlock);
    }

    /**
     * Applies incoming attack damage after checking the hero's block chance.
     *
     * @param theDamage incoming attack damage
     * @return actual hit points lost
     */
    @Override
    public int defendAgainstAttack(final int theDamage) {
        if (theDamage > 0 && block()) {
            return 0;
        }
        return takeDamage(theDamage);
    }

    /**
     * Calculates hero attacks per round.
     *
     * <p>Against monsters, heroes are never allowed fewer attacks than the
     * monster would receive from its own speed ratio.</p>
     *
     * @param theOpponent opponent whose speed is compared against the hero
     * @return number of hero attacks for one player attack action
     */
    @Override
    public int attacksPerRoundAgainst(final DungeonCharacter theOpponent) {
        final int heroAttacks = Math.max(1, super.attacksPerRoundAgainst(theOpponent));
        if (theOpponent instanceof Monster) {
            return Math.max(heroAttacks, theOpponent.attacksPerRoundAgainst(this));
        }
        return heroAttacks;
    }

    /**
     * Performs this hero class's special skill.
     *
     * @param theOpp target of the skill when the skill requires one
     */
    public abstract void useSpecialSkill(DungeonCharacter theOpp);

    /**
     * Uses a potion from inventory if one is available.
     *
     * @param thePotion potion type to consume and apply
     */
    public void usePotion(final Potion thePotion) {
        if (thePotion == null) {
            return;
        }
        if (thePotion instanceof HealingPotion && myHealingPotions > 0) {
            myHealingPotions--;
            thePotion.apply(this);
        } else if (thePotion instanceof VisionPotion && myVisionPotions > 0) {
            myVisionPotions--;
            thePotion.apply(this);
        }
    }

    /**
     * Adds one healing potion to the hero's inventory.
     */
    public void addHealingPotion() {
        myHealingPotions++;
    }

    /**
     * Adds one vision potion to the hero's inventory.
     */
    public void addVisionPotion() {
        myVisionPotions++;
    }

    /**
     * Returns the number of healing potions carried by the hero.
     *
     * @return healing potion count
     */
    public int getHealingPotions() {
        return myHealingPotions;
    }

    /**
     * Sets the healing potion count, clamping negative values to zero.
     *
     * @param theHealingPotions requested healing potion count
     */
    public void setHealingPotions(final int theHealingPotions) {
        myHealingPotions = Math.max(0, theHealingPotions);
    }

    /**
     * Returns the number of vision potions carried by the hero.
     *
     * @return vision potion count
     */
    public int getVisionPotions() {
        return myVisionPotions;
    }

    /**
     * Sets the vision potion count, clamping negative values to zero.
     *
     * @param theVisionPotions requested vision potion count
     */
    public void setVisionPotions(final int theVisionPotions) {
        myVisionPotions = Math.max(0, theVisionPotions);
    }

    /**
     * Returns this hero's chance to block incoming attacks.
     *
     * @return block chance from 0.0 to 1.0
     */
    public double getChanceToBlock() {
        return myChanceToBlock;
    }

    /**
     * Adds a Pillar of OO to the hero's collection.
     *
     * @param thePillar pillar to add; ignored when null
     */
    public void addPillar(final Pillar thePillar) {
        if (thePillar != null) {
            myPillars.add(thePillar);
        }
    }

    /**
     * Reports whether the hero has collected a specific pillar.
     *
     * @param thePillar pillar to check
     * @return true if the hero has the pillar
     */
    public boolean hasPillar(final Pillar thePillar) {
        return myPillars.contains(thePillar);
    }

    /**
     * Reports whether the hero has collected all four Pillars of OO.
     *
     * @return true when every pillar has been collected
     */
    public boolean hasAllPillars() {
        return myPillars.containsAll(EnumSet.allOf(Pillar.class));
    }

    /**
     * Returns a defensive copy of the collected pillars.
     *
     * @return collected pillars
     */
    public Set<Pillar> getPillars() {
        return EnumSet.copyOf(myPillars);
    }

    /**
     * Builds the hero status text displayed in the game UI.
     *
     * @return formatted hero summary
     */
    @Override
    public String toString() {
        return "Name: " + getName()
                + System.lineSeparator() + "Hit Points: " + getHitPoints()
                + System.lineSeparator() + "Total Healing Potions: " + myHealingPotions
                + System.lineSeparator() + "Total Vision Potions: " + myVisionPotions
                + System.lineSeparator() + "Pillar Pieces Found: " + myPillars;
    }
}
