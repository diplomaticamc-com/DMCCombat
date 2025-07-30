package com.diplomaticamc.dmccombat.combat.listener;
import com.google.common.collect.ImmutableSet;
import com.diplomaticamc.dmccombat.combat.CombatHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

public class CommandListener implements Listener {

    private static final Set<String> BLACKLISTED_COMMANDS = ImmutableSet.of(
            "t spawn", "n spawn", "warp", "trade", "res spawn", "home", "tradesystem:trade","town spawn","nation spawn","resident spawn","homes","towny:nation spawn","towny:town spawn","towny:resident spawn","player spawn","towny:player spawn","towny:n spawn","towny:nat spawn","towny:tw spawn","towny:res spawn","towny:t spawn","suicide");

    @EventHandler
    public void onPreProcessCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Check if the player is in combat and doesn't have bypass permission
        if (CombatHandler.isTagged(player) && !player.hasPermission("emccom.combattag.bypass")) {
            String message = event.getMessage().substring(1); // Remove leading "/"

            // Check if the command is blacklisted
            for (String blacklistedCommand : BLACKLISTED_COMMANDS) {
                if (message.equalsIgnoreCase(blacklistedCommand) || message.toLowerCase().startsWith(blacklistedCommand + " ")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You can't use that command while being in combat.");
                    return;
                }
            }
        }
    }
}
