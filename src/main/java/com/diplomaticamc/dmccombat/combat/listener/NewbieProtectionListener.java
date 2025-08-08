package com.diplomaticamc.dmccombat.combat.listener;

import com.diplomaticamc.dmccombat.DMCCombat;
import com.diplomaticamc.dmccombat.manager.NewbieManager;
import com.palmergames.bukkit.towny.event.resident.NewResidentEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class NewbieProtectionListener implements Listener {

    private final NewbieManager manager;

    public NewbieProtectionListener(NewbieManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPlayedBefore()) {
            if (manager.isProtected(player)) {
                manager.addProtectedList(player);

                DMCCombat.getInstance().getServer().getScheduler().runTaskLater(DMCCombat.getInstance(), () -> {
                    player.sendMessage(ChatColor.GREEN + "You are protected from players for " + manager.calculatedTimeRemaining(player) + "!");
                    player.sendMessage(ChatColor.GREEN + "Use /protectiontime to check remaining protection time.");
                }, 50L);
            }
        } else {
            manager.startRegistryTask(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (manager.isProtected(player)) {
            manager.removeProtectedList(player);
        }
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

