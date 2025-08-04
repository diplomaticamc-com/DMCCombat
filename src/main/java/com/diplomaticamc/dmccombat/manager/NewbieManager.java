package com.diplomaticamc.dmccombat.manager;

import com.diplomaticamc.dmccombat.DMCCombat;
import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

 // handles loading, saving and querying of newbie protection data

public class NewbieManager {
    private final DMCCombat plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private long protectionTimeMinutes;
    private String protectionMessage;
    private String blockedMessage;
    private String endMessage;

    // Stores remaining protection time in minutes for each player
//    private final Map<UUID, Long> protectionMap = new HashMap<>();
    // Tracks the last login timestamp (ms) for online players
//    private final Map<UUID, Long> loginTimes = new HashMap<>();
    private final HashSet<UUID> protectedList = new HashSet<>();

    // Task that checks for expired protections
    private BukkitTask expiryTask;

    public NewbieManager(DMCCombat plugin) {
        this.plugin = plugin;
    }

    public void init(IntegerDataField idf) {
        loadConfig();
//        createDataFile();
        loadData();
        startExpiryTask();
    }

    public void shutdown() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
        saveData();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        protectionTimeMinutes = config.getLong("protection-time-minutes", 720);
        protectionMessage = ChatColor.translateAlternateColorCodes('&',
                config.getString("protection-message",
                        "&aYou are under newbie protection for %minutes% more minutes!"));
        blockedMessage = ChatColor.translateAlternateColorCodes('&',
                config.getString("attack-blocked-message",
                        "&cYou cannot fight while protected!"));
        endMessage = ChatColor.translateAlternateColorCodes('&',
                config.getString("protection-ended-message",
                        "&eYour newbie protection has ended."));
    }

//    private void createDataFile() {
//        dataFile = new File(plugin.getDataFolder(), "players.yml");
//        if (!dataFile.exists()) {
//            try {
//                dataFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
//    }

    private void loadData() {
        for (String key : dataConfig.getKeys(false)) {
//            protectionMap.put(UUID.fromString(key), dataConfig.getLong(key + ".time"));
        }
    }


    public void saveData() {
        // Clear old entries
//        for (String key : new HashSet<>(dataConfig.getKeys(false))) {
//            if (!protectionMap.containsKey(UUID.fromString(key))) {
//                dataConfig.set(key, null);
//            }
//        }
//
//        for (UUID uuid : protectionMap.keySet()) {
//            String path = uuid.toString();
//            dataConfig.set(path + ".time", protectionMap.get(uuid));
//            String name = Bukkit.getOfflinePlayer(uuid).getName();
//            if (name != null) {
//                dataConfig.set(path + ".name", name);
//            }
//        }
//        try {
//            dataConfig.save(dataFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void addProtection(Player player) {
        if (player != null) {
            addProtection(player.getUniqueId());
        }
    }

    public void addProtection(UUID uuid) {
//        protectionMap.put(uuid, protectionTimeMinutes);
//        loginTimes.put(uuid, System.currentTimeMillis());
    }

    public void removeProtection(UUID uuid) {
//        protectionMap.remove(uuid);
//        loginTimes.remove(uuid);
//        if (dataConfig != null) {
//            dataConfig.set(uuid.toString(), null);
//        }
    }

    // Called when a protected player joins
    // Starts tracking their session start time (used to subtract playtime on quit)
    public void playerJoined(UUID uuid) {
//        if (protectionMap.containsKey(uuid)) {
//            loginTimes.put(uuid, System.currentTimeMillis());
//        }
        protectedList.add(player.getUniqueId());
    }

    // Called when a protected player quits
    // Subtracts this session’s playtime from their remaining protection
    // and removes protection entirely if it expires
    public void playerQuit(UUID uuid) {
//        Long login = loginTimes.remove(uuid);
//        if (login != null && protectionMap.containsKey(uuid)) {
//            long elapsed = (System.currentTimeMillis() - login) / (1000 * 60);
//            long remaining = protectionMap.get(uuid) - elapsed;
//            if (remaining <= 0) {
//                removeProtection(uuid);
//            } else {
//                protectionMap.put(uuid, remaining);
//            }
//            saveData();
//        }
    }

    public boolean isProtected(Player player) {
        return isProtected(player.getUniqueId());
    }

    public long getRemainingMinutes(Player player) {
        return getRemainingMinutes(player.getUniqueId());
    }

    public String getProtectionMessage(long minutes) {
        return protectionMessage.replace("%minutes%", String.valueOf(minutes));
    }

    public String getBlockedMessage() {
        return blockedMessage;
    }

    public String calculatedTimeRemaining() {
        String finalTime;
        return finalTime;
    }

    private void startExpiryTask() {
//        expiryTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//            for (UUID uuid : new HashSet<>(loginTimes.keySet())) {
//                if (isProtected(uuid)) {
//                    long remaining = getRemainingMinutes(uuid);
//                    if (remaining <= 0) {
//                        Player player = Bukkit.getPlayer(uuid);
//                        if (player != null) {
//                            player.sendMessage(endMessage);
//                        }
//                        removeProtection(uuid);
//                        saveData();
//                    }
//                }
//            }
//        }, 1200L, 1200L);
        expiryTask = new BukkitRunnable() {
            public void run() {
                for (UUID uuid : protectedList) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {

                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // gets the time (in ms) when the player joined, used to track session length
//    public Long getJoinTime(UUID uuid) {
//        return loginTimes.get(uuid);
//    }

    // same as above but accepts a Player object
//    public Long getJoinTime(Player player) {
//        return getJoinTime(player.getUniqueId());
//    }

    // how many minutes of protection players get by default (from config)
//    public long getProtectionTimeMinutes() {
//        return protectionTimeMinutes;
//    }

    // returns true if the player still has protection left, factoring in time played
    public boolean isProtected(UUID uuid) {
//        Long remaining = protectionMap.get(uuid);
//        if (remaining == null || remaining <= 0) return false;
//        Long login = loginTimes.get(uuid);
//        if (login != null) {
//            long elapsed = (System.currentTimeMillis() - login) / (1000 * 60);
//            return remaining - elapsed > 0;
//        }
//        return remaining > 0;
        return false;
    }

    // calculates how many minutes of protection are left for the player right now
    public long getRemainingMinutes(UUID uuid) {
//        Long remaining = protectionMap.get(uuid);
//        if (remaining == null) return 0;
//        Long login = loginTimes.get(uuid);
//        if (login != null) {
//            long elapsed = (System.currentTimeMillis() - login) / (1000 * 60);
//            return Math.max(remaining - elapsed, 0);
//        }
//        return Math.max(remaining, 0);
        return 0;
    }

    // finds an uuid by name (case insensitive) useful for offline players
    public UUID findUUIDByName(String name) {
        for (UUID uuid : protectionMap.keySet()) {
            String stored = Bukkit.getOfflinePlayer(uuid).getName();
//            if (stored != null && stored.equalsIgnoreCase(name)) {
//                return uuid;
//            }
        }
        return null;
    }
}


