package com.flaplings.gamemode;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/* package-private */ class GameModePluginConfiguration {

    final File
            file;

    final FileConfiguration
            fileConfiguration;

    GameModePluginConfiguration(File file, FileConfiguration fileConfiguration) {
        this.file = file;
        this.fileConfiguration = fileConfiguration;

    }

    private void init() {

    }

}
