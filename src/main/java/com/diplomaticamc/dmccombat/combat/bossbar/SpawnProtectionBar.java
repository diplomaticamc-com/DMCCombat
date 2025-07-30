package com.diplomaticamc.dmccombat.combat.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnProtectionBar {

    private static final Map<UUID, BossBar> barMap = new ConcurrentHashMap<>();

    public static void update(Player player, int remaining, int total, String unit) {
        if (player == null) {
            return;
        }

        BossBar bar = barMap.computeIfAbsent(player.getUniqueId(), id -> {
            BossBar b = Bukkit.createBossBar(ChatColor.GREEN + ChatColor.BOLD.toString() + "Spawn Protection", BarColor.GREEN, BarStyle.SOLID);
            b.addPlayer(player);
            return b;
        });

        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }

        if (!bar.isVisible()) {
            bar.setVisible(true);
        }

        bar.setTitle(ChatColor.GREEN + ChatColor.BOLD.toString() + "Spawn Protection" + ChatColor.GRAY + ": " + ChatColor.GREEN + remaining + " " + unit);
        bar.setProgress(total > 0 ? (double) remaining / total : 0);
    }

    public static void remove(Player player) {
        if (player == null) {
            return;
        }

        BossBar bar = barMap.remove(player.getUniqueId());
        if (bar != null) {
            bar.removePlayer(player);
        }
    }
}
