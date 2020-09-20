/*
MIT License

Copyright (c) 2020 Flaplings

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

*/
package com.flaplings.gamemode;

import org.bukkit.ChatColor;
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

    /* package-private */ String getPrefix() {
        return this.prefix;
    }

    /* package-private */ boolean useMinecraftNames() {
        return this.useMinecraftNames;
    }

    /* package-private */ boolean broadcastToPermissibles() {
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
            this.prefix = translateColors(this.fileConfiguration.getString("prefix"));
            this.useMinecraftNames = this.fileConfiguration.getBoolean("useMinecraftNames");

            return true;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("Error parsing configuration file");
            plugin.getLogger().throwing("GameModePluginConfiguration", "load", e);

            return false;
        }
    }

    private String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private void addDefaults() {
        this.fileConfiguration.addDefault("broadcastToPermissibles", true);
        this.fileConfiguration.addDefault("prefix", "&7[&bfGameMode&7] ");
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
