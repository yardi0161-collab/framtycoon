package com.farmtycoon.storage;

import com.farmtycoon.FarmTycoon;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public final class Database {
    private final Connection connection;
    private final double startingBalance;

    public Database(FarmTycoon plugin) throws Exception {
        File db = new File(plugin.getDataFolder(), "farmtycoon.db");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        Class.forName("com.farmtycoon.libs.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
        startingBalance = plugin.getConfig().getDouble("economy.starting-balance", 100000.0);
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("PRAGMA journal_mode=WAL");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, balance REAL NOT NULL, island_level INTEGER NOT NULL DEFAULT 1, world_name TEXT, island_x INTEGER NOT NULL DEFAULT 0, island_y INTEGER NOT NULL DEFAULT 100, island_z INTEGER NOT NULL DEFAULT 0)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL, type TEXT NOT NULL, amount REAL NOT NULL, note TEXT, created_at INTEGER NOT NULL)");
        }
    }

    public synchronized PlayerData getOrCreate(UUID uuid) throws SQLException {
        PlayerData existing = get(uuid);
        if (existing != null) return existing;
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO players(uuid,balance,island_level) VALUES(?,?,1)")) {
            ps.setString(1, uuid.toString()); ps.setDouble(2, startingBalance); ps.executeUpdate();
        }
        return get(uuid);
    }
    public synchronized PlayerData get(UUID uuid) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM players WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new PlayerData(rs.getDouble("balance"), rs.getInt("island_level"), rs.getString("world_name"), rs.getInt("island_x"), rs.getInt("island_y"), rs.getInt("island_z"));
            }
        }
    }
    public synchronized boolean setIsland(UUID uuid, String world, int x, int y, int z) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET world_name=?, island_x=?, island_y=?, island_z=? WHERE uuid=?")) {
            ps.setString(1, world); ps.setInt(2,x); ps.setInt(3,y); ps.setInt(4,z); ps.setString(5,uuid.toString()); return ps.executeUpdate()==1;
        }
    }
    public synchronized boolean upgrade(UUID uuid, int nextLevel, double cost) throws SQLException {
        connection.setAutoCommit(false);
        try {
            double balance;
            try (PreparedStatement q=connection.prepareStatement("SELECT balance FROM players WHERE uuid=?")) { q.setString(1,uuid.toString()); try(ResultSet r=q.executeQuery()){ if(!r.next()){connection.rollback();return false;} balance=r.getDouble(1); } }
            if (balance < cost) { connection.rollback(); return false; }
            try (PreparedStatement p=connection.prepareStatement("UPDATE players SET balance=balance-?, island_level=? WHERE uuid=? AND island_level=?")) {
                p.setDouble(1,cost); p.setInt(2,nextLevel); p.setString(3,uuid.toString()); p.setInt(4,nextLevel-1);
                if(p.executeUpdate()!=1){connection.rollback();return false;}
            }
            try(PreparedStatement t=connection.prepareStatement("INSERT INTO transactions(uuid,type,amount,note,created_at) VALUES(?,?,?,?,?)")){
                t.setString(1,uuid.toString());t.setString(2,"ISLAND_UPGRADE");t.setDouble(3,-cost);t.setString(4,"Upgrade ke level "+nextLevel);t.setLong(5,System.currentTimeMillis());t.executeUpdate();
            }
            connection.commit(); return true;
        } catch(SQLException e){connection.rollback();throw e;} finally {connection.setAutoCommit(true);}
    }
    public synchronized void close() { try { if(connection!=null && !connection.isClosed()) connection.close(); } catch(SQLException ignored){} }
}
