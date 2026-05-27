package dungeoncrawler.view;

import dungeoncrawler.combat.Battle;
import dungeoncrawler.model.characters.Hero;
import dungeoncrawler.model.characters.Monster;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * View-only combat stage that animates hero and monster sprite poses.
 */
final class CombatSpritePanel extends JPanel {
    private static final int TIMER_DELAY_MS = 120;
    private static final int DEFAULT_FRAME_TICKS = 3;
    private static final Color BACKGROUND_TOP = new Color(20, 20, 24);
    private static final Color BACKGROUND_BOTTOM = new Color(39, 40, 44);
    private static final Color PLATFORM = new Color(70, 73, 78);
    private static final Color PLATFORM_EDGE = new Color(189, 147, 249);
    private static final Color HEALTH_BACK = new Color(25, 26, 30);
    private static final Color HEALTH_FILL = new Color(80, 200, 120);
    private static final Color HEALTH_LOW = new Color(255, 85, 85);

    private final CombatSpriteSheet mySpriteSheet;
    private final Timer myTimer;
    private final Queue<SceneFrame> mySceneFrames;
    private Hero myHero;
    private Monster myMonster;
    private CombatSpritePose myHeroPose;
    private CombatSpritePose myMonsterPose;
    private int myFrameTicksRemaining;
    private int myIdleTick;

    CombatSpritePanel() {
        mySpriteSheet = CombatSpriteSheet.load();
        mySceneFrames = new ArrayDeque<>();
        myHeroPose = CombatSpritePose.IDLE;
        myMonsterPose = CombatSpritePose.IDLE;
        setOpaque(true);
        setBackground(BACKGROUND_BOTTOM);
        setPreferredSize(new Dimension(520, 260));
        setMinimumSize(new Dimension(360, 180));
        myTimer = new Timer(TIMER_DELAY_MS, event -> advanceAnimation());
        myTimer.start();
    }

    void setCombatants(final Battle theBattle) {
        if (theBattle == null) {
            myHero = null;
            myMonster = null;
            myHeroPose = CombatSpritePose.IDLE;
            myMonsterPose = CombatSpritePose.IDLE;
            mySceneFrames.clear();
        } else {
            myHero = theBattle.getHero();
            myMonster = theBattle.getMonster();
            myHeroPose = poseFor(myHero);
            myMonsterPose = poseFor(myMonster);
        }
        repaint();
    }

    void playBattleResult(final Battle.BattleResult theResult) {
        if (myHero == null || myMonster == null || theResult == null) {
            return;
        }
        mySceneFrames.clear();
        mySceneFrames.add(new SceneFrame(CombatSpritePose.ATTACK,
                CombatSpritePose.HIT, DEFAULT_FRAME_TICKS));
        if (theResult.isMonsterDefeated()) {
            mySceneFrames.add(new SceneFrame(CombatSpritePose.IDLE,
                    CombatSpritePose.DEFEATED, DEFAULT_FRAME_TICKS + 2));
        } else if (!theResult.isEscaped() && !theResult.isHeroDefeated()
                && !theResult.isBattleOver()) {
            mySceneFrames.add(new SceneFrame(CombatSpritePose.HIT,
                    CombatSpritePose.ATTACK, DEFAULT_FRAME_TICKS));
        }
        if (theResult.isHeroDefeated()) {
            mySceneFrames.add(new SceneFrame(CombatSpritePose.DEFEATED,
                    CombatSpritePose.IDLE, DEFAULT_FRAME_TICKS + 2));
        }
        myFrameTicksRemaining = 0;
        advanceAnimation();
    }

    private void advanceAnimation() {
        myIdleTick++;
        if (myFrameTicksRemaining > 0) {
            myFrameTicksRemaining--;
            repaint();
            return;
        }
        SceneFrame frame = mySceneFrames.poll();
        if (frame == null) {
            myHeroPose = poseFor(myHero);
            myMonsterPose = poseFor(myMonster);
        } else {
            myHeroPose = frame.myHeroPose;
            myMonsterPose = frame.myMonsterPose;
            myFrameTicksRemaining = frame.myTicks;
        }
        repaint();
    }

    private CombatSpritePose poseFor(final Hero theHero) {
        if (theHero != null && theHero.isFainted()) {
            return CombatSpritePose.DEFEATED;
        }
        return CombatSpritePose.IDLE;
    }

    private CombatSpritePose poseFor(final Monster theMonster) {
        if (theMonster != null && theMonster.isFainted()) {
            return CombatSpritePose.DEFEATED;
        }
        return CombatSpritePose.IDLE;
    }

    @Override
    protected void paintComponent(final Graphics theGraphics) {
        super.paintComponent(theGraphics);
        Graphics2D graphics = (Graphics2D) theGraphics.create();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        paintStage(graphics);
        if (myHero != null && myMonster != null) {
            paintCombatant(graphics, myHero.getClass().getSimpleName(), myHeroPose,
                    true, myHero.getHitPoints(), myHero.getMaxHitPoints());
            paintCombatant(graphics, myMonster.getClass().getSimpleName(),
                    myMonsterPose, false, myMonster.getHitPoints(),
                    myMonster.getMaxHitPoints());
        }
        graphics.dispose();
    }

    private void paintStage(final Graphics2D theGraphics) {
        for (int y = 0; y < getHeight(); y++) {
            float ratio = (float) y / Math.max(1, getHeight());
            int red = blend(BACKGROUND_TOP.getRed(), BACKGROUND_BOTTOM.getRed(), ratio);
            int green = blend(BACKGROUND_TOP.getGreen(), BACKGROUND_BOTTOM.getGreen(), ratio);
            int blue = blend(BACKGROUND_TOP.getBlue(), BACKGROUND_BOTTOM.getBlue(), ratio);
            theGraphics.setColor(new Color(red, green, blue));
            theGraphics.drawLine(0, y, getWidth(), y);
        }
        int baseY = getHeight() - 42;
        theGraphics.setColor(PLATFORM);
        theGraphics.fillRoundRect(28, baseY, getWidth() - 56, 24, 8, 8);
        theGraphics.setColor(PLATFORM_EDGE);
        theGraphics.setStroke(new BasicStroke(2));
        theGraphics.drawLine(42, baseY, getWidth() - 42, baseY);
    }

    private int blend(final int theStart, final int theEnd, final float theRatio) {
        return Math.round(theStart + (theEnd - theStart) * theRatio);
    }

    private void paintCombatant(final Graphics2D theGraphics,
                                final String theCharacter,
                                final CombatSpritePose thePose,
                                final boolean theHero,
                                final int theHitPoints,
                                final int theMaxHitPoints) {
        BufferedImage sprite = mySpriteSheet.spriteFor(theCharacter, thePose);
        int spriteSize = Math.min(getWidth() / 3, getHeight() - 62);
        int y = getHeight() - spriteSize - 42 + idleOffset(thePose);
        int x = theHero ? getWidth() / 5 - spriteSize / 2
                : getWidth() * 4 / 5 - spriteSize / 2;
        if (theHero) {
            theGraphics.drawImage(sprite, x, y, spriteSize, spriteSize, null);
        } else {
            theGraphics.drawImage(sprite, x + spriteSize, y,
                    -spriteSize, spriteSize, null);
        }
        paintHealthBar(theGraphics, x + spriteSize / 8, y - 12,
                spriteSize * 3 / 4, theHitPoints, theMaxHitPoints);
    }

    private int idleOffset(final CombatSpritePose thePose) {
        if (thePose != CombatSpritePose.IDLE) {
            return 0;
        }
        return myIdleTick % 8 < 4 ? 0 : -2;
    }

    private void paintHealthBar(final Graphics2D theGraphics,
                                final int theX,
                                final int theY,
                                final int theWidth,
                                final int theHitPoints,
                                final int theMaxHitPoints) {
        int height = 8;
        int fillWidth = Math.max(0, Math.round(theWidth
                * (theHitPoints / (float) Math.max(1, theMaxHitPoints))));
        theGraphics.setColor(HEALTH_BACK);
        theGraphics.fillRect(theX, theY, theWidth, height);
        theGraphics.setColor(theHitPoints <= theMaxHitPoints / 3
                ? HEALTH_LOW : HEALTH_FILL);
        theGraphics.fillRect(theX, theY, fillWidth, height);
        theGraphics.setColor(PLATFORM_EDGE);
        theGraphics.drawRect(theX, theY, theWidth, height);
    }

    private static final class SceneFrame {
        private final CombatSpritePose myHeroPose;
        private final CombatSpritePose myMonsterPose;
        private final int myTicks;

        private SceneFrame(final CombatSpritePose theHeroPose,
                           final CombatSpritePose theMonsterPose,
                           final int theTicks) {
            myHeroPose = theHeroPose;
            myMonsterPose = theMonsterPose;
            myTicks = theTicks;
        }
    }
}
