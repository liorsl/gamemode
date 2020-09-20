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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* package-private */ class GameModeCommand implements CommandExecutor, TabCompleter {

    private static final ChatColor
            ARGUMENT_COLOR = ChatColor.YELLOW,
            BODY_COLOR = ChatColor.GREEN,
            ERROR_COLOR = ChatColor.RED;

    final GameModePlugin plugin;

    final GameModePluginConfiguration config;

    final GameMode gameMode;

    /* package-private */  GameModeCommand(GameModePlugin plugin, GameModePluginConfiguration config, GameMode gameMode) {
        this.plugin = plugin;
        this.config = config;
        this.gameMode = gameMode;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage(config.getPrefix() + ERROR_COLOR + "Insufficient permissions");
            return false;
        }

        GameMode gameMode;
        Player target;

        if (args.length == 1 && "reload".equalsIgnoreCase(args[0]))
            return aliasReload(sender, command, label, args);

        if (this.gameMode != null && args.length < 2) {
            gameMode = this.gameMode;
            target = readPlayer(sender, getFromArray(args, 0), sender instanceof Player ? (Player) sender : null);
        } else {
            gameMode = readGameMode(sender, getFromArray(args, 0));
            target = readPlayer(sender, getFromArray(args, 1), sender instanceof Player ? (Player) sender : null);
        }

        if (target == null) {
            sender.sendMessage(config.getPrefix() + ERROR_COLOR + "Invalid target player");
            return false;
        }

        if (target != sender && !checkPermission(sender, "fgamemode.others"))
            return false;

        if (gameMode == null) {
            sender.sendMessage(config.getPrefix() + ERROR_COLOR + "Game mode not specified or is invalid");
            return false;
        }

        if (!checkGameModePermission(sender, gameMode))
            return false;

        target.setGameMode(gameMode);

        sender.sendMessage(config.getPrefix() + BODY_COLOR + "Setting " + displayForm(sender, target) + BODY_COLOR + " game mode to " + displayForm(gameMode));
        if (sender != target)
            target.sendMessage(config.getPrefix() + displayForm(sender) + BODY_COLOR + " changed your game mode to " + displayForm(gameMode));

        if (config.broadcastToPermissibles()) {
            String message =
                    config.getPrefix() + displayForm(sender) + BODY_COLOR + " set " + displayForm(target) + BODY_COLOR + " game mode to " + displayForm(gameMode);

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission("fgamemode.announce"))
                    .filter(player -> player != sender)
                    .filter(player -> player != target)
                    .forEach(player -> player.sendMessage(message));

        }

        return true;
    }

    private boolean aliasReload(CommandSender sender, Command command, String label, String[] args) {
        if (!checkPermission(sender, "fgamemode.reload"))
            return false;

        if (sender instanceof ConsoleCommandSender) // no need to notify in game
            this.config.reload();
        else {
            if (this.config.reload())
                sender.sendMessage(config.getPrefix() + ChatColor.GREEN + "Successfully reloaded configuration");
            else
                sender.sendMessage(config.getPrefix() + ERROR_COLOR + "Error while reloading configuration");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!hasPermission(sender))
            return Collections.emptyList();

        List<String> list = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("fgamemode.reload"))
            list.add("reload");

        if (args.length == 0 || args.length == 1) { // handle first arg
            list.addAll(
                    this.gameMode == null ?
                            completeGameModes(sender, args, 0) :
                            completePlayers(sender, args, 0)
            );
        } else if (this.gameMode == null) // handle 2nd arg
            list.addAll(completePlayers(sender, args, 1));

        return list;
    }

    private boolean hasPermission(CommandSender commandSender) {
        return plugin.getDescription().getPermissions().stream()
                .filter(permission -> permission.getName().equalsIgnoreCase("fgamemode.reload")) // for whatever reason
                .anyMatch(commandSender::hasPermission);
    }

    private List<String> completePlayers(CommandSender sender, String[] args, int index) {
        String arg = getFromArray(args, index);

        return Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> player == sender)
                .map(Player::getName)
                .filter(string -> arg == null || string.startsWith(arg))
                .collect(Collectors.toList());
    }

    private List<String> completeGameModes(CommandSender sender, String[] args, int index) {
        String arg = getFromArray(args, index);

        return Stream.of(GameMode.values())
                .map(GameMode::name)
                .filter(string -> arg == null || string.startsWith(arg))
                .map(String::toLowerCase)
                .filter(gameMode -> sender.hasPermission("fgamemode." + gameMode))
                .collect(Collectors.toList());
    }

    private <T> T getFromArray(T[] array, int index) {
        try {
            return array[index];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private Player readPlayer(CommandSender sender, String value, Player def) {
        if (value == null)
            return def;

        return Optional.ofNullable(Bukkit.getPlayer(value)).orElse(def);
    }

    private String displayForm(CommandSender commandSender) {
        return ARGUMENT_COLOR + (commandSender instanceof Player ? displayForm((Player) commandSender) : "CONSOLE") + ChatColor.RESET;
    }

    private String displayForm(CommandSender sender, Player target) {
        return ARGUMENT_COLOR + (sender == target ? "your" : displayForm(target)) + ChatColor.RESET;
    }

    private String displayForm(Player target) {
        return ARGUMENT_COLOR + (config.useMinecraftNames() ? target.getName() : target.getDisplayName()) + ChatColor.RESET;
    }

    private String displayForm(GameMode gameMode) {
        String name = gameMode.name();
        return ARGUMENT_COLOR + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private GameMode readGameMode(CommandSender sender, String value) {
        if (value == null)
            return null;

        OptionalInt number = getNumber(value);
        if (number.isPresent() && number.getAsInt() < GameMode.values().length)
            return GameMode.getByValue(number.getAsInt());

        String fValue = value.toUpperCase();

        return Stream.of(GameMode.values())
                .filter(gameModeE -> gameModeE.name().startsWith(fValue))
                .findFirst()
                .orElse(null);
    }

    private OptionalInt getNumber(String arg) {
        try {
            return OptionalInt.of(Integer.parseInt(arg));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    private boolean checkGameModePermission(CommandSender commandSender, GameMode gameMode) {
        return checkPermission(commandSender, "fgamemode." + gameMode.name().toLowerCase());
    }

    private boolean checkPermission(CommandSender commandSender, String permission) {
        if (commandSender.hasPermission(permission))
            return true;

        commandSender.sendMessage(config.getPrefix() + ERROR_COLOR + "You are lacking the " + ARGUMENT_COLOR +  permission + ERROR_COLOR + " permission node");
        return false;
    }
}