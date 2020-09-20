package com.flaplings.gamemode;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

/* package-private */ class GameModePluginConfiguration {

    final File
            file;

    final FileConfiguration
            fileConfiguration;

    final GameModePlugin
            plugin;

    // Config values

    private boolean broadcastToPermissibles;

    private boolean useMinecraftNames;

    private String prefix;

    /* package-private */ GameModePluginConfiguration(GameModePlugin plugin, File file, FileConfiguration fileConfiguration) {
        this.file = file;
        this.fileConfiguration = fileConfiguration;
        this.plugin = plugin;

        init();

    }

    String getPrefix() {
        return this.prefix;
    }

    boolean useMinecraftNames() {
        return this.useMinecraftNames;
    }

    boolean broadcastToPermissibles() {
        return this.broadcastToPermissibles;
    }

    /* package-private */ boolean reload() {
        plugin.getLogger().info("Reloading configuration...");
        boolean value = load();
        if (value)
            plugin.getLogger().info("Done reloading configuration");
        else
            plugin.getLogger().severe("Error while reloading plugin configuration");

        return value;
    }

    private boolean load() {
        try {
            this.broadcastToPermissibles = this.fileConfiguration.getBoolean("broadcastToPermissibles");
            this.prefix = this.fileConfiguration.getString("prefix");
            this.useMinecraftNames = this.fileConfiguration.getBoolean("useMinecraftNames");

            return true;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("Error parsing configuration file");
            plugin.getLogger().throwing("GameModePluginConfiguration", "load", e);

            return false;
        }
    }

    private void addDefaults() {
        this.fileConfiguration.addDefault("broadcastToPermissibles", true);
        this.fileConfiguration.addDefault("prefix", "gAmEmOdE");
        this.fileConfiguration.addDefault("useMinecraftNames", false);
        this.fileConfiguration.options().copyDefaults(true);
        this.fileConfiguration.options().copyHeader(true);

    }

    private void init() {
        addDefaults();

        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdir())
            plugin.getLogger().severe("Error while creating plugin configuration folder");

        load();
        save();

    }

    private void save() {
        try {
            this.fileConfiguration.save(this.file);
        } catch (IOException e) {
            plugin.getLogger().throwing("FileConfiguration", "save", e);

        }

    }

}
