package com.diplomaticamc.dmccombat.commands;

import com.diplomaticamc.dmccombat.DMCCombat;
import com.diplomaticamc.dmccombat.manager.NewbieManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

    // newbie

public class NewbieCommand implements TabExecutor {
    private final NewbieManager manager;
    private final DMCCombat plugin;

    public NewbieCommand(NewbieManager manager, DMCCombat plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("protectiontime")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = "protectiontime";
            System.arraycopy(args, 0, newArgs, 1, args.length);
            args = newArgs;
        }

        if (args.length == 0) {
            return helpCommand(sender);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("disable") && sender instanceof Player player) {
            return disableCommand(player);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("protectiontime") && sender instanceof Player player) {
            return protectionTimeCommand(player);
        }
//      Reload command is commented out since reloading the entire plugin will break newbie protection deductions on a live environment
//        if (args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("newbie.admin")) {
//            return reloadCommand(sender);
//        }

        if (args.length == 2) {
            String sub = args[0];
            String targetName = args[1];

            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                // Fall back to a case-insensitive search for an online player
                target = Bukkit.getPlayer(targetName);
            }
//            OfflinePlayer target;
//
//            if (targetPlayer != null) {
//                target = targetPlayer;
//            } else {
//                target = Bukkit.getOfflinePlayer(targetName);
//
//                if (!target.hasPlayedBefore()) {
//                    // Try case-insensitive lookup among known offline players
//                    for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
//                        String name = op.getName();
//
//                        if (name != null && name.equalsIgnoreCase(targetName)) {
//                            target = op;
//                            break;
//                        }
//                    }
//                }
//            }

            // if the player has never joined before and isnt online, UUID may be invalid
            if (target == null || (!target.hasPlayedBefore())) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            if (!sender.hasPermission("newbie.admin")) {
                sender.sendMessage(ChatColor.RED + "You must be an operator to use this command.");
                return true;
            }

            switch (sub.toLowerCase()) {
//                case "reset":

                //Subcommands

                case "enable":
                    manager.addProtection(target);
                    sender.sendMessage(ChatColor.GREEN + "Newbie protection enabled for " + target.getName());
                    if (target.isOnline()) {
                        target.getPlayer().sendMessage(ChatColor.GREEN + "Your newbie protection has been reset by an admin.");
                        manager.addProtectedList(target);
                    }
                    break;

                case "disable":
                    manager.removeProtection(target);

                    sender.sendMessage(ChatColor.YELLOW + "Newbie protection disabled for " + target.getName());
                    if (target.isOnline()) {
                        manager.removeProtectedList(target);
                        target.getPlayer().sendMessage(ChatColor.RED + "Your newbie protection has been disabled by an admin.");
                    } else {

                    }
                    break;

                case "protectiontime":
                    if (!manager.isProtected(target)) {
                        sender.sendMessage(ChatColor.RED + target.getName() + " is not under newbie protection.");
                    } else {
                        sender.sendMessage(ChatColor.GREEN + target.getName() + " has " + manager.calculatedTimeRemaining((Player) target) + " of protection remaining.");
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
            }
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("protectiontime")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = "protectiontime";
            System.arraycopy(args, 0, newArgs, 1, args.length);
            args = newArgs;
        }

        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("disable");
            list.add("protectiontime");
            if (sender.hasPermission("newbie.admin")) {
                list.add("enable");
//                list.add("reset");
//                list.add("reload");
            }
            return list;
        } else if (args.length == 2 && sender.hasPermission("newbie.admin")) {
            String[] finalArgs = args;
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(finalArgs[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    // Command functions

    private boolean helpCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + "DMC" + ChatColor.GRAY + "] " + ChatColor.WHITE + "Newbie Protection Help:");
        sender.sendMessage(ChatColor.YELLOW + "/newbie disable" + ChatColor.GRAY + " - Disable your protection early");
        sender.sendMessage(ChatColor.YELLOW + "/newbie protectiontime" + ChatColor.GRAY + " - Check your remaining protection time");

        if (sender.hasPermission("newbie.admin")) {
            sender.sendMessage(ChatColor.AQUA + "Admin Commands:");
            sender.sendMessage(ChatColor.YELLOW + "/newbie disable <player>" + ChatColor.GRAY + " - Disable protection for player (admin)");
//                sender.sendMessage(ChatColor.YELLOW + "/newbie reset <player>" + ChatColor.GRAY + " - Reset protection for player (admin)");
            sender.sendMessage(ChatColor.YELLOW + "/newbie enable <player>" + ChatColor.GRAY + " - Enable protection for player (admin)");
            sender.sendMessage(ChatColor.YELLOW + "/newbie protectiontime <player>" + ChatColor.GRAY + " - Check another player's protection time");
//                sender.sendMessage(ChatColor.YELLOW + "/newbie reload" + ChatColor.GRAY + " - Reload the plugin configuration");
        }
        return true;
    }

//    private boolean reloadCommand(CommandSender sender) {
//        plugin.reloadAll();
//        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
//                plugin.getConfig().getString("reload-message", "Configuration reloaded.")));
//        return true;
//    }

    private boolean disableCommand(Player player) {
        if (!manager.isProtected(player)) {
            player.sendMessage(ChatColor.RED + "You are not under newbie protection.");
        } else {
            //if the player is protected...
            if (manager.isCancelPending(player)) {
                //if the player has already run /newbie disable once
                manager.removeProtection(player);
                manager.removeProtectedList(player);
                manager.removeCancelList(player);
                player.sendMessage(ChatColor.YELLOW + "You have ended your newbie protection early. You are now vulnerable to attacks by other players!");
            } else {
                //...otherwise..
                manager.addCancelList(player);
                player.sendMessage(ChatColor.GOLD + "Are you sure you want to disable your newbie protection?");
                player.sendMessage(ChatColor.WHITE + "Disabling newbie protection early leaves you vulnerable to attacks by other players!");
                player.sendMessage(ChatColor.WHITE + "If a player asks you to disable protection, you may be killed and lose your items upon disabling!");
                player.sendMessage(ChatColor.GOLD + "To confirm the disabling of newbie protection, use " + ChatColor.WHITE + "/newbie disable " + ChatColor.GOLD + "again.");
            }
        }
        return true;
    }

    private boolean protectionTimeCommand(Player player) {
        if (!manager.isProtected(player)) {
            player.sendMessage(ChatColor.RED + "You are not under newbie protection.");
        } else {
            player.sendMessage(ChatColor.GREEN + "You are protected from players for " + manager.calculatedTimeRemaining(player) + "!");
        }
        return true;
    }

}

