package net.earthmc.emccom.combat.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.SpawnEvent;
import com.palmergames.bukkit.towny.event.teleport.CancelledTownyTeleportEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.WorldCoord;
import net.earthmc.emccom.manager.ResidentMetadataManager;
import net.earthmc.emccom.object.SpawnProtPref;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnProtectionListener implements Listener {
    private final Map<UUID, Integer> playerBlockCountMap = new HashMap<>();

    private static final int BLOCK_DISTANCE = 32;

    @EventHandler
    public void onSpawnEvent(SpawnEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();

            playerBlockCountMap.put(playerId, BLOCK_DISTANCE);
        }
    }

    @EventHandler
    public void onRespawnEvent(PlayerRespawnEvent event) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();

            playerBlockCountMap.put(playerId, BLOCK_DISTANCE);
    }

    @EventHandler
    public void onSpawnEventCancelled(CancelledTownyTeleportEvent event) {
        Resident teleporter = event.getResident();
        Player residentAsPlayer = TownyAPI.getInstance().getPlayer(teleporter);
        UUID playerId = residentAsPlayer.getUniqueId();

        playerBlockCountMap.remove(playerId);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Block fromBlock = event.getFrom().getBlock();
        Block toBlock = event.getTo().getBlock();

        Resident playerAsResident = TownyAPI.getInstance().getResident(player);
        ResidentMetadataManager rmm = new ResidentMetadataManager();
        SpawnProtPref SpawnProtPrefOfResident = rmm.getResidentSpawnProtPref(playerAsResident);

        if (playerBlockCountMap.containsKey(playerId)) {
            if (!fromBlock.equals(toBlock)) {
                int remainingBlocks = getRemainingBlocks(playerId);
                if (remainingBlocks > 0) {
                    remainingBlocks--;
                    updateRemainingBlocks(playerId, remainingBlocks);

                    if (!(SpawnProtPrefOfResident == SpawnProtPref.HIDE)) {
                        Component message;
                        if ((remainingBlocks > 0) && ((remainingBlocks%5) == 0)) {
                            message = Component.text()
                                    .append(Component.text("[Towny] ", NamedTextColor.GOLD))
                                    .append(Component.text("You have " + remainingBlocks + " blocks left before losing items on death!", NamedTextColor.RED))
                                    .build();
                        } else {
                            message = Component.text()
                                    .append(Component.text("[Towny] ", NamedTextColor.GOLD))
                                    .append(Component.text("You will now lose your items if you die! Run! ", NamedTextColor.RED))
                                    .append(Component.text("/spawnprotpref HIDE", NamedTextColor.GREEN))
                                    .append(Component.text(" to disable chunk notification warnings", NamedTextColor.RED))
                                    .build();
                        }
                        player.sendMessage(message);
                    }
                }
            }
        }
    }

    private int getRemainingBlocks(UUID playerId) {
        return playerBlockCountMap.getOrDefault(playerId, BLOCK_DISTANCE);
    }

    private void updateRemainingBlocks(UUID playerId, int remainingBlocks) {
        playerBlockCountMap.put(playerId, remainingBlocks);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        final WorldCoord coord = WorldCoord.parseWorldCoord(event.getEntity().getLocation());
        if (TownyAPI.getInstance().isWilderness(coord))
            return;

        if (playerBlockCountMap.containsKey(playerId)) {
            int remainingChunks = getRemainingBlocks(playerId);
            if (remainingChunks > 0) {
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                event.getDrops().clear();
                event.setDroppedExp(0);

                playerBlockCountMap.remove(playerId);
            }
        }
    }
}

