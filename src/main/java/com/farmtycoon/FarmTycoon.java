package com.farmtycoon;

import com.farmtycoon.commands.IsCommand;
import com.farmtycoon.storage.Database;
import com.farmtycoon.island.IslandManager;
import com.farmtycoon.listeners.MenuListener;
import com.farmtycoon.listeners.FirstJoinListener;
import com.farmtycoon.gui.IslandMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class FarmTycoon extends JavaPlugin {
    private Database database;
    private IslandManager islandManager;
    private IslandMenu islandMenu;

    @Override public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("islands.yml", false);
        try {
            database = new Database(this);
            islandManager = new IslandManager(this, database);
            islandMenu = new IslandMenu(this, islandManager, database);
            Objects.requireNonNull(getCommand("is")).setExecutor(new IsCommand(this, islandManager, islandMenu));
            getServer().getPluginManager().registerEvents(new MenuListener(this, islandMenu), this);
            getServer().getPluginManager().registerEvents(new FirstJoinListener(this), this);
            getLogger().info("FarmTycoon tahap 1 aktif.");
        } catch (Exception ex) {
            getLogger().severe("Gagal mengaktifkan FarmTycoon: " + ex.getMessage());
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override public void onDisable() { if (database != null) database.close(); }
    public Database database() { return database; }
    public IslandManager islands() { return islandManager; }
    public String msg(String key) {
        String value = getConfig().getString("messages." + key);
        if (value == null) value = key;
        return color(value);
    }
    public String message(String key) {
        var cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(new java.io.File(getDataFolder(), "messages.yml"));
        return color(cfg.getString("prefix", "") + cfg.getString(key, key));
    }
    public static String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    public void send(CommandSender sender, String key) { sender.sendMessage(message(key)); }
}
