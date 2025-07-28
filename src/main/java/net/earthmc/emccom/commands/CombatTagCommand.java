package net.earthmc.emccom.commands;

import net.earthmc.emccom.combat.CombatHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.earthmc.emccom.EMCCOM;
import net.earthmc.emccom.config.Config;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class CombatTagCommand implements CommandExecutor {

    private final EMCCOM plugin;

    public CombatTagCommand(EMCCOM plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length < 1) {
            showHelp(sender);
            return true;
        }

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


        switch (args[0]) {
            case "help":
                showHelp(sender);
                break;
            case "tag":
                parseTagCommand(sender, args);
                break;
            case "untag":
                parseUntagCommand(sender, args);
                break;
            case "test":
                parseTestCommand(sender, args);
                break;
            default:
                sender.sendMessage("§7[§bCombatTag§7]: §eIncorrect Usage: §e/combattag help");
                break;
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§7[§bCombatTag§7]: §eHelp Command");
        sender.sendMessage("§7[§bCombatTag§7]: §e/combattag tag <username>");
        sender.sendMessage("§7[§bCombatTag§7]: §e/combattag untag <username>");
    }

    private void parseTagCommand(CommandSender sender, String[] args) {
        Player target;

        if (args.length < 2) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("§7[§bCombatTag§7]: §eIncorrect Usage: §e/combattag tag <username>");
                return;
            }
        } else {
            target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("§7[§bCombatTag§7]: §cPlayer doesn't exist.");
                return;
            }
        }

        CombatHandler.applyTag(target);
        sender.sendMessage("§7[§bCombatTag§7]: §e" + target.getName() + " has been tagged.");
    }

    private void parseUntagCommand(CommandSender sender, String[] args) {
        Player target;

        if (args.length < 2) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("§7[§bCombatTag§7]: §eIncorrect Usage: §e/combattag untag <username>");
                return;
            }
        } else {
            target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("§7[§bCombatTag§7]: §cPlayer doesn't exist.");
                return;
            }
        }

        if(!CombatHandler.isTagged(target)) {
            sender.sendMessage("§7[§bCombatTag§7]: §cPlayer is not combat tagged.");
            return;
        }

        CombatHandler.removeTag(target);
        sender.sendMessage("§7[§bCombatTag§7]: §e" + target.getName() + " has been untagged.");
    }
    private void parseTestCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§7[§bCombatTag§7]: §eIncorrect Usage: §e/combattag test <username>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§7[§bCombatTag§7]: §cPlayer doesn't exist or is not online.");
            return;
        }
        sender.sendMessage("That player SUUUCKS!!");
    }

}
