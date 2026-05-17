package dungeoncrawler.persistence;

import dungeoncrawler.model.characters.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonsterDatabase extends DatabaseManager {

    public MonsterDatabase() throws SQLException {
        super();
    }

    @Override
    protected void initSchema() throws SQLException {
        String createMonster = """
            CREATE TABLE IF NOT EXISTS monster (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                name         TEXT NOT NULL UNIQUE,
                hp           INTEGER,
                min_dmg      INTEGER,
                max_dmg      INTEGER,
                attack_speed INTEGER,
                hit_chance   REAL,
                heal_chance  REAL,
                min_heal     INTEGER,
                max_heal     INTEGER
            )
        """;
        conn.createStatement().execute(createMonster);
        conn.commit();
        seedMonsters();
    }

    private void seedMonsters() throws SQLException {
        String sql = """
        INSERT OR IGNORE INTO monster
          (name, hp, min_dmg, max_dmg, attack_speed, hit_chance, heal_chance, min_heal, max_heal)
        VALUES (?,?,?,?,?,?,?,?,?)
    """;
        Object[][] monsters = {
                { "Ogre",     200, 30, 60, 2, 0.6, 0.1, 30, 60 },
                { "Skeleton", 100, 30, 50, 3, 0.8, 0.3, 30, 50 },
                { "Gremlin",  70,  15, 30, 5, 0.8, 0.4, 20, 40 },
        };
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (Object[] row : monsters) {
            for (int i = 0; i < row.length; i++) stmt.setObject(i + 1, row[i]);
            stmt.addBatch();
        }
        stmt.executeBatch();
        conn.commit();
    }

    public List<String> getAllMonsterNames() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(
                "SELECT name FROM monster"
        );
        List<String> names = new ArrayList<>();
        while (rs.next()) {
            names.add(rs.getString("name"));
        }
        return names;
    }

    public Monster getMonsterByName(String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM monster WHERE name = ?"
        );
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new SQLException("Unknown monster: " + name);
        }
        return buildMonster(rs);
    }

    public List<Monster> getAllMonsters() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(
                "SELECT * FROM monster"
        );
        List<Monster> monsters = new ArrayList<>();
        while (rs.next()) {
            monsters.add(buildMonster(rs));
        }
        return monsters;
    }

    private Monster buildMonster(ResultSet rs) throws SQLException {
        String name     = rs.getString("name");
        int hp          = rs.getInt("hp");
        int minDmg      = rs.getInt("min_dmg");
        int maxDmg      = rs.getInt("max_dmg");
        int attackSpeed = rs.getInt("attack_speed");
        double hitChance  = rs.getDouble("hit_chance");
        double healChance = rs.getDouble("heal_chance");
        int minHeal     = rs.getInt("min_heal");
        int maxHeal     = rs.getInt("max_heal");

        switch (name) {
            case Ogre.OGRE_NAME:         return new Ogre(hp, minDmg, maxDmg, attackSpeed, hitChance, healChance, minHeal, maxHeal);
            case Skeleton.SKELETON_NAME: return new Skeleton(hp, minDmg, maxDmg, attackSpeed, hitChance, healChance, minHeal, maxHeal);
            case Gremlin.GREMLIN_NAME:   return new Gremlin(hp, minDmg, maxDmg, attackSpeed, hitChance, healChance, minHeal, maxHeal);
            default: throw new IllegalArgumentException("Unknown monster: " + name);
        }
    }
}