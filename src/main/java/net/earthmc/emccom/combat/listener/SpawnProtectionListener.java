package net.earthmc.emccom.combat.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.SpawnEvent;
import com.palmergames.bukkit.towny.event.teleport.CancelledTownyTeleportEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.WorldCoord;
import net.earthmc.emccom.EMCCOM;
import net.earthmc.emccom.combat.bossbar.SpawnProtectionBar;
import net.earthmc.emccom.manager.ResidentMetadataManager;
import net.earthmc.emccom.object.SpawnProtPref;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnProtectionListener implements Listener {

    private final EMCCOM plugin;
    private final Map<UUID, Integer> distanceMap = new HashMap<>();
    private final boolean useChunks;
    private final int chunkDistance;
    private final int blockDistance;

    public SpawnProtectionListener(EMCCOM plugin) {
        this.plugin = plugin;
        this.useChunks = plugin.getConfig().getBoolean("spawn_protection.use_chunks", false);
        this.chunkDistance = plugin.getConfig().getInt("spawn_protection.chunks_amount", 8);
        this.blockDistance = plugin.getConfig().getInt("spawn_protection.blocks_amount", 32);
    }

    @EventHandler
    public void onSpawnEvent(SpawnEvent event) {
        if (!event.isCancelled()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> startProtection(event.getPlayer()));
        }
    }

    @EventHandler
    public void onRespawnEvent(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> startProtection(event.getPlayer()));
    }

    private void startProtection(Player player) {
        distanceMap.put(player.getUniqueId(), startingDistance());
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident != null) {
            SpawnProtPref pref = new ResidentMetadataManager().getResidentSpawnProtPref(resident);
            if (pref != SpawnProtPref.HIDE) {
                SpawnProtectionBar.update(player, startingDistance(), startingDistance(), unit());
            }
        }
    }

    @EventHandler
    public void onSpawnEventCancelled(CancelledTownyTeleportEvent event) {
        Resident teleporter = event.getResident();
        Player residentAsPlayer = TownyAPI.getInstance().getPlayer(teleporter);
        UUID playerId = residentAsPlayer.getUniqueId();
        distanceMap.remove(playerId);
        SpawnProtectionBar.remove(residentAsPlayer);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        handleMovement(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleMovement(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle().getPassengers().isEmpty()) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (useChunks ? from.getChunk().equals(to.getChunk()) : from.getBlock().equals(to.getBlock())) {
            return;
        }
        event.getVehicle().getPassengers().forEach(entity -> {
            if (entity instanceof Player p) {
                handleMovement(p, from, to);
            }
        });
    }

    private void handleMovement(Player player, Location from, Location to) {
        UUID id = player.getUniqueId();
        Integer remaining = distanceMap.get(id);
        if (remaining == null || remaining <= 0) {
            return;
        }

        boolean changed = useChunks ? !from.getChunk().equals(to.getChunk()) : !from.getBlock().equals(to.getBlock());
        if (!changed) {
            return;
        }

        remaining--;
        distanceMap.put(id, remaining);

        Resident resident = TownyAPI.getInstance().getResident(player);
        SpawnProtPref pref = new ResidentMetadataManager().getResidentSpawnProtPref(resident);
        if (pref != SpawnProtPref.HIDE) {
            SpawnProtectionBar.update(player, remaining, startingDistance(), unit());
            if (remaining == 0) {
                Component msg = Component.text()
                        .append(Component.text("[Towny] ", NamedTextColor.GOLD))
                        .append(Component.text("You will now lose your items if you die! Run! ", NamedTextColor.RED))
                        .append(Component.text("/spawnprotpref HIDE", NamedTextColor.GREEN))
                        .append(Component.text(" to disable notification warnings", NamedTextColor.RED))
                        .build();
                player.sendMessage(msg);
            }
        }

        if (remaining == 0) {
            distanceMap.remove(id);
            SpawnProtectionBar.remove(player);
        }
    }

    private int startingDistance() {
        return useChunks ? chunkDistance : blockDistance;
    }

    private String unit() {
        return useChunks ? "chunks" : "blocks";
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        final WorldCoord coord = WorldCoord.parseWorldCoord(event.getEntity().getLocation());
        if (TownyAPI.getInstance().isWilderness(coord))
            return;

        if (distanceMap.containsKey(playerId)) {
            int remaining = distanceMap.get(playerId);
            if (remaining > 0) {
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                event.getDrops().clear();
                event.setDroppedExp(0);

                distanceMap.remove(playerId);
                SpawnProtectionBar.remove(player);
            }
        }
    }
}
