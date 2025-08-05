package com.diplomaticamc.dmccombat.combat.listener;

import com.diplomaticamc.dmccombat.DMCCombat;
import com.diplomaticamc.dmccombat.manager.NewbieManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NewbieProtectionListener implements Listener {

    private final NewbieManager manager;

    public NewbieProtectionListener(NewbieManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            manager.addProtection(player);
            manager.saveData();
            DMCCombat.getInstance().getServer().getScheduler().runTaskLater(DMCCombat.getInstance(), () -> {
                long total = manager.getProtectionTimeMinutes();
                String timeMsg = total >= 60 ? (total / 60 + " hours") : (total + " minutes");
                player.sendMessage(ChatColor.GREEN + "You are under newbie protection for " + timeMsg + "!");
                player.sendMessage(ChatColor.GREEN + "Use /protectiontime to check your time and /newbie disable to end it early");
            }, 50L);
        } else { //if player has played before...
            manager.playerJoined(player.getUniqueId());
            if (manager.isProtected(player)) {
                long remaining = manager.getRemainingMinutes(player);
                DMCCombat.getInstance().getServer().getScheduler().runTaskLater(DMCCombat.getInstance(), () -> {
                    if (remaining >= 60) {
                        long hours = remaining / 60;
                        player.sendMessage(ChatColor.GREEN + "You have " + hours + (hours == 1 ? " hour" : " hours") + " of newbie protection remaining");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "You have " + remaining + (remaining == 1 ? " minute" : " minutes") + " of newbie protection remaining");
                    }
                    player.sendMessage(ChatColor.GREEN + "Use /protectiontime to check your time and /newbie disable to end it early");
                }, 50L);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.playerQuit(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (manager.isProtected(victim)) {
            attacker.sendMessage(ChatColor.RED + "That player is under newbie protection!");
            event.setCancelled(true);
            return;
        }
        if (manager.isProtected(attacker)) {
            attacker.sendMessage(ChatColor.RED + "You can't attack while under newbie protection!");
            event.setCancelled(true);
        }
    }
}

