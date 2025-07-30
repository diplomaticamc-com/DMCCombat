package com.diplomaticamc.dmccombat.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.diplomaticamc.dmccombat.manager.ResidentMetadataManager;
import com.diplomaticamc.dmccombat.object.CombatPref;
import com.diplomaticamc.dmccombat.DMCCombat;
import org.bukkit.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CombatPrefCommand implements TabExecutor {

    private final DMCCombat plugin;

    public CombatPrefCommand(DMCCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You must be an operator to use this command.");
                return true;
            }
            plugin.reloadAll();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("reload-message", "Configuration reloaded.")));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("No arguments provided", NamedTextColor.RED));
            return true;
        }

        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null) {
            player.sendMessage(Component.text("Command failed as your player has no associated Towny resident", NamedTextColor.RED));
            return true;
        }

        CombatPref combatPref;
        switch (args[0]) {
            case "safe" -> combatPref = CombatPref.SAFE;
            case "unsafe" -> combatPref = CombatPref.UNSAFE;
            default -> {
                player.sendMessage(Component.text("Invalid argument", NamedTextColor.RED));
                return true;
            }
        }

        ResidentMetadataManager rmm = new ResidentMetadataManager();
        rmm.setResidentCombatPref(resident, combatPref);
        if (args[0].equals("safe")){
            player.sendMessage(Component.text("Successfully changed your combat preference to SAFE.\n" +
                    "You will no longer be able to hit tagged players within claims to initiate combat.", NamedTextColor.GREEN));
        }
        if (args[0].equals("unsafe")){
            player.sendMessage(Component.text("Successfully changed your combat preference to UNSAFE.\n" +
                    "You will now be able to hit tagged players within claims to initiate combat.", NamedTextColor.GREEN));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> availableArguments = List.of("safe", "unsafe");

        if (args.length == 0) {
            return availableArguments; // If there are no arguments, provide all available options
        } else if (args.length == 1) {
            // Provide completions for the first argument
            if (args[0].isEmpty()) {
                List<String> list = new ArrayList<>(availableArguments);
                if (sender.isOp()) {
                    list.add("reload");
                }
                return list;
            } else {
                List<String> list = new ArrayList<>(availableArguments);
                if (sender.isOp()) {
                    list.add("reload");
                }
                return list.stream()
                        .filter(string -> string.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList(); // Return an empty list if the number of arguments is greater than 1
    }
}

