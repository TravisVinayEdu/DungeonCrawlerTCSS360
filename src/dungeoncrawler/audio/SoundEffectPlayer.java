package dungeoncrawler.audio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Loads and plays short sound effects used as UI and gameplay feedback.
 *
 * <p>The player deliberately fails quietly if an asset is unavailable so a
 * missing optional audio file never prevents the game from launching.</p>
 */
public final class SoundEffectPlayer implements AutoCloseable {
    private static final Path SOUND_DIRECTORY =
            Path.of("assets", "sound-effects");

    private final Map<Cue, Clip> myClips;

    public SoundEffectPlayer() {
        myClips = new EnumMap<>(Cue.class);
        for (Cue cue : Cue.values()) {
            load(cue);
        }
    }

    /**
     * Restarts a sound from the beginning, allowing quick repeated input to
     * produce immediate feedback.
     *
     * @param theCue sound to play
     */
    public void play(final Cue theCue) {
        Clip clip = myClips.get(theCue);
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    @Override
    public void close() {
        for (Clip clip : myClips.values()) {
            clip.close();
        }
        myClips.clear();
    }

    private void load(final Cue theCue) {
        File soundFile = SOUND_DIRECTORY.resolve(theCue.fileName()).toFile();
        if (!soundFile.isFile()) {
            return;
        }

        try (AudioInputStream audioStream =
                     AudioSystem.getAudioInputStream(soundFile)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            myClips.put(theCue, clip);
        } catch (UnsupportedAudioFileException
                 | LineUnavailableException
                 | IOException
                 | IllegalArgumentException exception) {
            // Audio is optional feedback; leave this cue unavailable.
        }
    }

    /**
     * User-facing events with a corresponding sound-effect asset.
     */
    public enum Cue {
        MENU_BUTTON("menu-button.wav"),
        MOVEMENT("movement.wav"),
        PILLAR_FOUND("pillar-found.wav"),
        ATTACK("attack.wav"),
        BATTLE_WIN("battle-win.wav"),
        BATTLE_LOSE("battle-lose.wav");

        private final String myFileName;

        Cue(final String theFileName) {
            myFileName = theFileName;
        }

        private String fileName() {
            return myFileName;
        }
    }
}
