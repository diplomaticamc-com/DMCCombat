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
            sender.sendMessage(ChatColor.AQUA + "Newbie Protection Commands:");
            sender.sendMessage(ChatColor.YELLOW + "/newbie disable" + ChatColor.GRAY + " - Disable your protection early");
            sender.sendMessage(ChatColor.YELLOW + "/newbie protectiontime" + ChatColor.GRAY + " - Check your remaining protection time");

            if (sender.hasPermission("newbie.admin")) {
                sender.sendMessage(ChatColor.YELLOW + "/newbie disable <player>" + ChatColor.GRAY + " - Disable protection for player (admin)");
                sender.sendMessage(ChatColor.YELLOW + "/newbie reset <player>" + ChatColor.GRAY + " - Reset protection for player (admin)");
                sender.sendMessage(ChatColor.YELLOW + "/newbie enable <player>" + ChatColor.GRAY + " - Enable protection for player (admin)");
                sender.sendMessage(ChatColor.YELLOW + "/newbie protectiontime <player>" + ChatColor.GRAY + " - Check another player's protection time");
                sender.sendMessage(ChatColor.YELLOW + "/newbie reload" + ChatColor.GRAY + " - Reload the plugin configuration");
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("disable") && sender instanceof Player player) {
            if (!manager.isProtected(player)) {
                player.sendMessage(ChatColor.RED + "You are not under newbie protection.");
                return true;
            }
            manager.removeProtection(player.getUniqueId());
            manager.saveData();
            player.sendMessage(ChatColor.YELLOW + "You have ended your newbie protection early.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("protectiontime") && sender instanceof Player player) {
            Long join = manager.getJoinTime(player);
            long now = System.currentTimeMillis();
            long elapsed = join == null ? -1 : (now - join) / (1000 * 60);
            if (!manager.isProtected(player)) {
                player.sendMessage(ChatColor.RED + "You are not under newbie protection.");
            } else {
                long remaining = manager.getRemainingMinutes(player);
                if (remaining >= 60) {
                    long hours = remaining / 60;
                    player.sendMessage(ChatColor.GREEN + "You have " + hours + (hours == 1 ? " hour" : " hours") + " of protection remaining.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "You have " + remaining + (remaining == 1 ? " minute" : " minutes") + " of protection remaining.");
                }
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("newbie.admin")) {
            plugin.reloadAll();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("reload-message", "Configuration reloaded.")));
            return true;
        }

        if (args.length == 2) {
            String sub = args[0];
            String targetName = args[1];

            Player targetPlayer = Bukkit.getPlayerExact(targetName);
            if (targetPlayer == null) {
                // Fall back to a case-insensitive search for an online player
                targetPlayer = Bukkit.getPlayer(targetName);
            }

            OfflinePlayer target;
            if (targetPlayer != null) {
                target = targetPlayer;
            } else {
                target = Bukkit.getOfflinePlayer(targetName);
                if (!target.hasPlayedBefore()) {
                    // Try case-insensitive lookup among known offline players
                    for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                        String name = op.getName();
                        if (name != null && name.equalsIgnoreCase(targetName)) {
                            target = op;
                            break;
                        }
                    }
                }
            }
            UUID uuid = target.getUniqueId();
            if (uuid == null) {
                uuid = manager.findUUIDByName(targetName);
            }

            // if the player has never joined before and isnt online, UUID may be invalid
            if (uuid == null || (!target.hasPlayedBefore() && targetPlayer == null)) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            if (!sender.hasPermission("newbie.admin")) {
                sender.sendMessage(ChatColor.RED + "You must be an operator to use this command.");
                return true;
            }

            switch (sub.toLowerCase()) {
                case "reset":

                case "enable":
                    manager.removeProtection(uuid);
                    manager.addProtection(uuid);
                    manager.saveData();
                    sender.sendMessage(ChatColor.GREEN + "Newbie protection enabled for " + target.getName());
                    if (target.isOnline()) {
                        target.getPlayer().sendMessage(ChatColor.GREEN + "Your newbie protection has been reset by an admin.");
                    }
                    break;

                case "disable":
                    manager.removeProtection(uuid);
                    manager.saveData();

                    sender.sendMessage(ChatColor.YELLOW + "Newbie protection disabled for " + target.getName());
                    if (target.isOnline()) {
                        target.getPlayer().sendMessage(ChatColor.RED + "Your newbie protection has been disabled by an admin.");
                    }
                    break;

                case "protectiontime":
                    long remain = manager.getRemainingMinutes(uuid);
                    if (!manager.isProtected(uuid)) {
                        sender.sendMessage(ChatColor.RED + target.getName() + " is not under newbie protection.");
                    } else if (remain >= 60) {
                        long h = remain / 60;
                        sender.sendMessage(ChatColor.GREEN + target.getName() + " has " + h + (h == 1 ? " hour" : " hours") + " of protection remaining.");
                    } else {
                        sender.sendMessage(ChatColor.GREEN + target.getName() + " has " + remain + (remain == 1 ? " minute" : " minutes") + " of protection remaining.");
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
                list.add("reset");
                list.add("reload");
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
}

