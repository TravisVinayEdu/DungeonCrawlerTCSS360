package dungeoncrawler.persistence;

import dungeoncrawler.model.Direction;
import dungeoncrawler.model.Dungeon;
import dungeoncrawler.model.Pillar;
import dungeoncrawler.model.Room;
import dungeoncrawler.model.characters.Gremlin;
import dungeoncrawler.model.characters.Hero;
import dungeoncrawler.model.characters.Monster;
import dungeoncrawler.model.characters.Ogre;
import dungeoncrawler.model.characters.Priestess;
import dungeoncrawler.model.characters.Skeleton;
import dungeoncrawler.model.characters.Thief;
import dungeoncrawler.model.characters.Warrior;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaveManager extends DatabaseManager {

    public SaveManager() throws SQLException {
        super();
    }

    @Override
    protected void initSchema() throws SQLException {
        String createSaveFile = """
    CREATE TABLE IF NOT EXISTS save_file (
        id              INTEGER PRIMARY KEY AUTOINCREMENT,
        player_name     TEXT,
        hero_class      TEXT,
        hero_hp         INTEGER,
        dungeon_width   INTEGER,
        dungeon_height  INTEGER,
        hero_row        INTEGER,
        hero_col        INTEGER,
        healing_potions INTEGER DEFAULT 0,
        vision_potions  INTEGER DEFAULT 0,
        abstraction     INTEGER DEFAULT 0,
        encapsulation   INTEGER DEFAULT 0,
        inheritance     INTEGER DEFAULT 0,
        polymorphism    INTEGER DEFAULT 0,
        saved_at        DATETIME DEFAULT CURRENT_TIMESTAMP
    )
""";

        String createSavedRoom = """
            CREATE TABLE IF NOT EXISTS saved_room (
                id                 INTEGER PRIMARY KEY AUTOINCREMENT,
                save_id            INTEGER NOT NULL,
                room_row           INTEGER,
                room_col           INTEGER,
                is_entrance        INTEGER DEFAULT 0,
                is_exit            INTEGER DEFAULT 0,
                has_healing_potion INTEGER DEFAULT 0,
                has_vision_potion  INTEGER DEFAULT 0,
                has_pit            INTEGER DEFAULT 0,
                pillar             TEXT,
                monster_type       TEXT,
                monster_hp         INTEGER,
                discovered         INTEGER DEFAULT 0,
                door_north         INTEGER DEFAULT 0,
                door_east          INTEGER DEFAULT 0,
                door_south         INTEGER DEFAULT 0,
                door_west          INTEGER DEFAULT 0,
                FOREIGN KEY (save_id) REFERENCES save_file(id)
            )
        """;

        conn.createStatement().execute(createSaveFile);
        conn.createStatement().execute(createSavedRoom);
        conn.commit();
    }

    public int saveGame(Hero hero, Dungeon dungeon) throws SQLException {
        String saveFileSql = """
        INSERT INTO save_file
          (player_name, hero_class, hero_hp,
           dungeon_width, dungeon_height, hero_row, hero_col,
           healing_potions, vision_potions,
           abstraction, encapsulation, inheritance, polymorphism)
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
    """;

        PreparedStatement saveStmt = conn.prepareStatement(
                saveFileSql, Statement.RETURN_GENERATED_KEYS
        );
        saveStmt.setString(1, hero.getName());
        saveStmt.setString(2, hero.getClass().getSimpleName());
        saveStmt.setInt(3, hero.getHitPoints());
        saveStmt.setInt(4, dungeon.getWidth());
        saveStmt.setInt(5, dungeon.getHeight());
        saveStmt.setInt(6, dungeon.getHeroRow());
        saveStmt.setInt(7, dungeon.getHeroCol());
        saveStmt.setInt(8, hero.getHealingPotions());
        saveStmt.setInt(9, hero.getVisionPotions());
        saveStmt.setInt(10, hero.hasPillar(Pillar.ABSTRACTION)   ? 1 : 0);
        saveStmt.setInt(11, hero.hasPillar(Pillar.ENCAPSULATION) ? 1 : 0);
        saveStmt.setInt(12, hero.hasPillar(Pillar.INHERITANCE)   ? 1 : 0);
        saveStmt.setInt(13, hero.hasPillar(Pillar.POLYMORPHISM)  ? 1 : 0);
        saveStmt.executeUpdate();

        ResultSet keys = saveStmt.getGeneratedKeys();
        int saveId = keys.next() ? keys.getInt(1) : -1;

        // Insert every room in the dungeon
        String roomSql = """
            INSERT INTO saved_room
              (save_id, room_row, room_col, is_entrance, is_exit,
               has_healing_potion, has_vision_potion, has_pit,
               pillar, monster_type, monster_hp,
               discovered, door_north, door_east, door_south, door_west)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

        PreparedStatement roomStmt = conn.prepareStatement(roomSql);
        for (int row = 0; row < dungeon.getHeight(); row++) {
            for (int col = 0; col < dungeon.getWidth(); col++) {
                Room room = dungeon.getRoom(row, col);
                Monster m = room.getMonster();

                roomStmt.setInt(1, saveId);
                roomStmt.setInt(2, row);
                roomStmt.setInt(3, col);
                roomStmt.setInt(4, room.isEntrance() ? 1 : 0);
                roomStmt.setInt(5, room.isExit() ? 1 : 0);
                roomStmt.setInt(6, room.hasHealingPotion() ? 1 : 0);
                roomStmt.setInt(7, room.hasVisionPotion() ? 1 : 0);
                roomStmt.setInt(8, room.hasPit() ? 1 : 0);
                roomStmt.setString(9, room.getPillar() != null ? room.getPillar().name() : null);
                roomStmt.setString(10, m != null ? m.getClass().getSimpleName() : null);
                roomStmt.setInt(11, m != null ? m.getHitPoints() : 0);
                roomStmt.setInt(12, dungeon.isDiscovered(row, col) ? 1 : 0);
                roomStmt.setInt(13, room.workingDoor(Direction.NORTH) ? 1 : 0);
                roomStmt.setInt(14, room.workingDoor(Direction.EAST) ? 1 : 0);
                roomStmt.setInt(15, room.workingDoor(Direction.SOUTH) ? 1 : 0);
                roomStmt.setInt(16, room.workingDoor(Direction.WEST) ? 1 : 0);
                roomStmt.addBatch();
            }
        }
        roomStmt.executeBatch();
        conn.commit();
        return saveId;
    }

    public Dungeon loadDungeon(int saveId) throws SQLException {
        // 1. Load dungeon metadata
        PreparedStatement metaStmt = conn.prepareStatement(
                "SELECT * FROM save_file WHERE id = ?"
        );
        metaStmt.setInt(1, saveId);
        ResultSet meta = metaStmt.executeQuery();
        if (!meta.next()) {
            throw new SQLException("No save found with id: " + saveId);
        }

        int width   = meta.getInt("dungeon_width");
        int height  = meta.getInt("dungeon_height");
        int heroRow = meta.getInt("hero_row");
        int heroCol = meta.getInt("hero_col");

        // 2. Load all rooms
        PreparedStatement roomStmt = conn.prepareStatement(
                "SELECT * FROM saved_room WHERE save_id = ? ORDER BY room_row, room_col"
        );
        roomStmt.setInt(1, saveId);
        ResultSet rs = roomStmt.executeQuery();

        Room[][] maze       = new Room[height][width];
        boolean[][] discovered = new boolean[height][width];

        while (rs.next()) {
            int row = rs.getInt("room_row");
            int col = rs.getInt("room_col");
            Room room = new Room(row, col);

            // Restore doors first so setEntrance/setExit don't wipe them
            room.setDoor(Direction.NORTH, rs.getInt("door_north") == 1);
            room.setDoor(Direction.EAST,  rs.getInt("door_east")  == 1);
            room.setDoor(Direction.SOUTH, rs.getInt("door_south") == 1);
            room.setDoor(Direction.WEST,  rs.getInt("door_west")  == 1);

            // setEntrance/setExit call clearContents() so do these before restoring contents
            if (rs.getInt("is_entrance") == 1)      room.setEntrance();
            else if (rs.getInt("is_exit") == 1)     room.setExit();

            // Restore contents after entrance/exit flags
            room.removePit();
            if (rs.getInt("has_pit") == 1)          room.setPit();
            String pillarName = rs.getString("pillar");
            if (pillarName != null)                  room.setPillar(Pillar.valueOf(pillarName));

            room.setHealingPotion(rs.getInt("has_healing_potion") == 1);
            room.setVisionPotion(rs.getInt("has_vision_potion") == 1);

            String monsterType = rs.getString("monster_type");
            if (monsterType != null) {
                Monster m = createMonster(monsterType);
                m.setMyHitPoints(rs.getInt("monster_hp"));
                room.setMonstersManual(m);
            }

            // Note: healing/vision potions in rooms need setHealingPotion/setVisionPotion
            // added to Room — see note below

            discovered[row][col] = rs.getInt("discovered") == 1;
            maze[row][col] = room;
        }

        return new Dungeon(maze, width, height, heroRow, heroCol, discovered);
    }

    public Hero loadHero(int saveId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM save_file WHERE id = ?"
        );
        stmt.setInt(1, saveId);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new SQLException("No save found with id: " + saveId);
        }

        Hero hero = createHero(rs.getString("hero_class"), rs.getString("player_name"));
        hero.setMyHitPoints(rs.getInt("hero_hp"));

        hero.setHealingPotions(rs.getInt("healing_potions"));
        hero.setVisionPotions(rs.getInt("vision_potions"));

        if (rs.getInt("abstraction")   == 1) hero.addPillar(Pillar.ABSTRACTION);
        if (rs.getInt("encapsulation") == 1) hero.addPillar(Pillar.ENCAPSULATION);
        if (rs.getInt("inheritance")   == 1) hero.addPillar(Pillar.INHERITANCE);
        if (rs.getInt("polymorphism")  == 1) hero.addPillar(Pillar.POLYMORPHISM);

        return hero;
    }

    private Hero createHero(String heroClass, String name) {
        switch (heroClass) {
            case "Warrior":   return new Warrior(name);
            case "Priestess": return new Priestess(name);
            case "Thief":     return new Thief(name);
            default: throw new IllegalArgumentException("Unknown hero class: " + heroClass);
        }
    }

    public List<String> listSaves() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(
                "SELECT id, player_name, hero_class, saved_at FROM save_file ORDER BY saved_at DESC"
        );
        List<String> saves = new ArrayList<>();
        while (rs.next()) {
            saves.add(String.format("[%d] %s the %s — %s",
                    rs.getInt("id"),
                    rs.getString("player_name"),
                    rs.getString("hero_class"),
                    rs.getString("saved_at")
            ));
        }
        return saves;
    }

    public void deleteSave(int saveId) throws SQLException {
        PreparedStatement deleteRooms = conn.prepareStatement(
                "DELETE FROM saved_room WHERE save_id = ?"
        );
        deleteRooms.setInt(1, saveId);
        deleteRooms.executeUpdate();

        PreparedStatement deleteSave = conn.prepareStatement(
                "DELETE FROM save_file WHERE id = ?"
        );
        deleteSave.setInt(1, saveId);
        deleteSave.executeUpdate();
        conn.commit();
    }

    private Monster createMonster(String type) {
        switch (type) {
            case "Skeleton": return new Skeleton();
            case "Gremlin":  return new Gremlin();
            case "Ogre":     return new Ogre();
            default: throw new IllegalArgumentException("Unknown monster type: " + type);
        }
    }
}
