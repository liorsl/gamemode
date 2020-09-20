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

import org.bukkit.GameMode;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class GameModePlugin extends JavaPlugin {

    private GameModePluginConfiguration config;

    @Override
    public void onEnable() {
        config = new GameModePluginConfiguration(this, new File(getDataFolder(), "config.yml"), getConfig());

        register(null, "gamemode");
        register(GameMode.CREATIVE, "gmc");
        register(GameMode.SURVIVAL, "gms");
        register(GameMode.ADVENTURE, "gma");
        register(GameMode.SPECTATOR, "gmsp");
    }

    private void register(GameMode defaultGameMode, String alias) {
        GameModeCommand gameModeCommand = new GameModeCommand(this, config, defaultGameMode);
        PluginCommand command = getCommand(alias);
        command.setExecutor(gameModeCommand);
        command.setTabCompleter(gameModeCommand);

    }

    @Override
    public void onDisable() {
    }

}
