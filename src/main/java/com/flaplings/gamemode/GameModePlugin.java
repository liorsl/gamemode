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
