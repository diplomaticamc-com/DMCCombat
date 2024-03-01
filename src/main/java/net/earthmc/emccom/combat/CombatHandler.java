package net.earthmc.emccom.combat;

import com.palmergames.util.TimeTools;
import net.earthmc.emccom.EMCCOM;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatHandler {

    public static final long TAG_TIME = 30 * 1000;
    private static Map<UUID, Long> combatTags = new ConcurrentHashMap<>();

    static {
        new BukkitRunnable() {

            @Override
            public void run() {
                Iterator<Entry<UUID, Long>> iterator = combatTags.entrySet().iterator();

                while (iterator.hasNext()) {
                    Entry<UUID, Long> entry = iterator.next();

                    if (entry.getValue() > System.currentTimeMillis())
                        continue;

                    iterator.remove();

                    UUID uuid = entry.getKey();
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline())
                        continue;

                    player.sendMessage(ChatColor.GREEN + "You are no longer in combat.");

                    // Remove the player from the combat tagged team
                    removePlayerFromCombatTeam(player);
                }
            }
        }.runTaskTimerAsynchronously(EMCCOM.getInstance(), 10L, 10L);
    }

    public static void applyTag(Player player) {
        if (!isTagged(player)) {
            player.closeInventory(Reason.PLUGIN);
            player.sendMessage(ChatColor.RED + "You have been combat tagged for " + (TAG_TIME / 1000) + " seconds! Do not log out or you will get killed instantly.");

            // Create or get the combat tagged team
            Team combatTaggedTeam = getCombatTaggedTeam();
            // Add the player to the combat tagged team
            addPlayerToCombatTeam(player, combatTaggedTeam);
        }

        combatTags.put(player.getUniqueId(), System.currentTimeMillis() + TAG_TIME);
    }

    public static void removeTag(Player player) {
        combatTags.remove(player.getUniqueId());

        // Remove the player from the combat tagged team
        removePlayerFromCombatTeam(player);
    }

    public static boolean isTagged(Player player) {
        return combatTags.containsKey(player.getUniqueId()) && combatTags.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public static long getRemaining(Player player) {
        if (!combatTags.containsKey(player.getUniqueId()))
            return -1;

        return combatTags.get(player.getUniqueId()) - System.currentTimeMillis();
    }

    private static Team getCombatTaggedTeam() {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard mainScoreboard = scoreboardManager.getMainScoreboard();

        Team combatTaggedTeam = mainScoreboard.getTeam("CombatTagged");
        if (combatTaggedTeam == null) {
            combatTaggedTeam = mainScoreboard.registerNewTeam("CombatTagged");
            combatTaggedTeam.setSuffix(ChatColor.RED.toString() + " ⚔");
        }

        return combatTaggedTeam;
    }

    private static void addPlayerToCombatTeam(Player player, Team team) {
        team.addEntry(player.getName());

        // Set the player's scoreboard
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private static void removePlayerFromCombatTeam(Player player) {
        Team combatTaggedTeam = getCombatTaggedTeam();
        if (combatTaggedTeam != null) {
            combatTaggedTeam.removeEntry(player.getName());


            // Set the player's scoreboard (optional, if you want to reset the scoreboard)
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
}
