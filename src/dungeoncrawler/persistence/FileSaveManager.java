package dungeoncrawler.persistence;

import dungeoncrawler.controller.GameSession;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Built-in Java serialization save fallback.
 *
 * <p>The SQLite save manager still exists, but this class lets save/load work
 * on machines where the SQLite JDBC driver has not been added to the project.</p>
 */
public class FileSaveManager {
    private static final Path SAVE_DIR = Path.of("saves");
    private static final DateTimeFormatter SAVE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public long saveGame(final GameSession theSession) throws IOException {
        Files.createDirectories(SAVE_DIR);
        long saveId = System.currentTimeMillis();
        SaveRecord record = new SaveRecord(saveId, LocalDateTime.now().format(SAVE_TIME),
                theSession);
        try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(savePath(saveId)))) {
            out.writeObject(record);
        }
        return saveId;
    }

    public GameSession loadGame(final long theSaveId) throws IOException {
        try (ObjectInputStream in = new ObjectInputStream(
                Files.newInputStream(savePath(theSaveId)))) {
            Object object = in.readObject();
            if (!(object instanceof SaveRecord)) {
                throw new IOException("Save file is not a Dungeon Crawler save.");
            }
            return ((SaveRecord) object).getSession();
        } catch (ClassNotFoundException exception) {
            throw new IOException("Save file uses an unknown class.", exception);
        }
    }

    public List<String> listSaves() throws IOException {
        List<SaveRecord> records = new ArrayList<>();
        if (!Files.isDirectory(SAVE_DIR)) {
            return new ArrayList<>();
        }

        try (var paths = Files.list(SAVE_DIR)) {
            for (Path path : paths.filter(path -> path.getFileName().toString()
                    .startsWith("save-")).toList()) {
                try (ObjectInputStream in = new ObjectInputStream(
                        Files.newInputStream(path))) {
                    Object object = in.readObject();
                    if (object instanceof SaveRecord) {
                        records.add((SaveRecord) object);
                    }
                } catch (ClassNotFoundException | IOException ignored) {
                    // Ignore broken save files so one bad file does not hide the list.
                }
            }
        }

        records.sort(Comparator.comparingLong(SaveRecord::getId).reversed());
        List<String> saves = new ArrayList<>();
        for (SaveRecord record : records) {
            GameSession session = record.getSession();
            saves.add("[" + record.getId() + "] "
                    + session.getHero().getName() + " the "
                    + session.getHero().getClass().getSimpleName()
                    + " - " + record.getSavedAt());
        }
        return saves;
    }

    private Path savePath(final long theSaveId) {
        return SAVE_DIR.resolve("save-" + theSaveId + ".ser");
    }

    private static class SaveRecord implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private final long myId;
        private final String mySavedAt;
        private final GameSession mySession;

        SaveRecord(final long theId,
                   final String theSavedAt,
                   final GameSession theSession) {
            myId = theId;
            mySavedAt = theSavedAt;
            mySession = theSession;
        }

        long getId() {
            return myId;
        }

        String getSavedAt() {
            return mySavedAt;
        }

        GameSession getSession() {
            return mySession;
        }
    }
}
