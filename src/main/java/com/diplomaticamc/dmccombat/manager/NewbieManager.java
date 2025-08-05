package com.diplomaticamc.dmccombat.manager;

import com.diplomaticamc.dmccombat.DMCCombat;
import com.diplomaticamc.dmccombat.combat.listener.NewbieProtectionListener;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;

 // handles loading, saving and querying of newbie protection data

public class NewbieManager {
    private final DMCCombat plugin;

    //newbie protection metadata
    private static String keyname = "dmccombat_newbieprotection";
    private static int defaultVal = 12400;
    private static String label = "Newbie Protection";
    private static IntegerDataField newbieMetaData = new IntegerDataField(keyname, defaultVal, label);

    // List that contains online users to deduct noob prot from
    private final HashSet<Player> protectedList = new HashSet<>();
    // List that contains users pending cancellation of newbie protection
    private final HashMap<Player, Integer> cancelList = new HashMap<>();

    // deduction counter task
    private BukkitTask expiryTask;

    public NewbieManager(DMCCombat plugin) {
        this.plugin = plugin;
    }

    public void init() {
//        loadConfig();
        startExpiryTask();
        registerMetaData();
    }

    public void shutdown() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
    }

    public void registerMetaData() {
        //newbie prot initialize towny metadata. i have no idea if this will work when the plugin starts up
        try {
            TownyAPI.getInstance().registerCustomDataField(newbieMetaData);
        } catch (KeyAlreadyRegisteredException e) {
            plugin.getLogger().info("newbieMetaData failed to register!");
            plugin.getLogger().warning(e.getMessage());
        }
        plugin.getLogger().info("newbieMetaData successfully registered to Towny!");
    }

//    private void loadConfig() {
//        FileConfiguration config = plugin.getConfig();
//        protectionTime = config.getLong("newbie_protection.protection-time", 12400);
//        protectionMessage = ChatColor.translateAlternateColorCodes('&',
//                config.getString("newbie_protection.protection-message",
//                        "&aYou are under newbie protection for %minutes% more minutes!"));
//        blockedMessage = ChatColor.translateAlternateColorCodes('&',
//                config.getString("newbie_protection.attack-blocked-message",
//                        "&cYou cannot fight while protected!"));
//        endMessage = ChatColor.translateAlternateColorCodes('&',
//                config.getString("newbie_protection.protection-ended-message",
//                        "&eYour newbie protection has ended."));
//    }

    //CRUD
    public int readData(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident != null) {
            if (resident.hasMeta(newbieMetaData.getKey())) {
                CustomDataField placeholderData = resident.getMetadata(newbieMetaData.getKey());

                if (placeholderData instanceof IntegerDataField) {
                    IntegerDataField newData = (IntegerDataField) placeholderData;
                    return newData.getValue();
                }
            }
        }
        return 0; //default
    }

    public void saveData(Player player, int updatedData) {
        Resident resident = TownyAPI.getInstance().getResident(player);

        if (resident.hasMeta(newbieMetaData.getKey())) {
            CustomDataField placeholderData = resident.getMetadata(newbieMetaData.getKey());

            if (placeholderData instanceof IntegerDataField) {
                IntegerDataField newData = (IntegerDataField) placeholderData;
                newData.setValue(updatedData);
            }
        }
    }

    public void addProtection(Player player) {
        if (player != null) {
            Resident resident = TownyAPI.getInstance().getResident(player);

            if (resident != null) {
                resident.addMetaData(newbieMetaData.clone());
            } else {
                plugin.getLogger().warning("Failed to add newbie protection to " + player.getName() + "!");
                plugin.getLogger().warning("Error: Resident object returns as null");
            }
        }
    }

    public void removeProtection(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident != null) {
            resident.removeMetaData(newbieMetaData);
        } else {
            plugin.getLogger().warning("Failed to remove newbie protection to " + player.getName() + "!");
            plugin.getLogger().warning("Error: Resident object returns as null");
        }
    }

    // online player list deduction function things

    public void addProtectedList(Player player) {
        protectedList.add(player);
    }

    public void removeProtectedList(Player player) {
        protectedList.remove(player);
    }

    // the checks

    // returns true if the player still has protection left. uuid is used to check offline players (not like we are ever using that)
    public boolean isProtected(Player player) {
//        for (Player v : protectedList) {
//            if (player == v) {
//                return true;
//            }
//        }
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident != null) {
            return resident.hasMeta(newbieMetaData.getKey());
        }
        return false;
    }

    public String calculatedTimeRemaining(Player player) {
        //probably want to include translation features in the future for the minutes/hours/seconds --stoffeh
        int time = readData(player);
        int timeHour;
        int timeMinutes;
        int timeSeconds;

        String formatHour;
        String formatMinute;
        String calculatedTime = "0"; //default

        if (time >= 3600)  {
            //1 hour+ math
            timeHour = (int) Math.floor((double) time / 3600);
            timeMinutes = (int) Math.floor((double) (time % 3600) / 60);
            if (timeHour <= 1) {
                formatHour = "hour";
            } else {
                formatHour = "hours";
            }
            if (timeMinutes <= 1) {
                formatMinute = "minute";
            } else {
                formatMinute = "minutes";
            }
            calculatedTime = timeHour + " " + formatHour + " and " + timeMinutes + " " + formatMinute;

        } else if (time < 3600 && time > 60) {
            //<1 hour math
            timeMinutes = (int) Math.floor((double) time / 60);
            timeSeconds = (int) Math.floor(time % 60);
            if (timeMinutes <= 1) {
                formatMinute = "minute";
            } else {
                formatMinute = "minutes";
            }
            calculatedTime = timeMinutes + " " + formatMinute + " and " + timeSeconds + " seconds";

        } else if (time <= 60) {
            //<1 minute math
            calculatedTime = time + " seconds";
        }
        return calculatedTime;
    }

    //deduction timer doohickey thing
    private void startExpiryTask() {
        expiryTask = new BukkitRunnable() {
            public void run() {
                for (Player player : protectedList) {
                    if (player != null && player.isOnline()) {
                        int currentTime = readData(player);

                        if (currentTime < 1) {
                            int newTime = currentTime - 1;
                            saveData(player, newTime);
                        } else {
                            removeProtection(player);
                            ProtectionEnded(player);
                            protectedList.remove(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    //chat things
    public void ProtectionEnded(Player player) {
        player.sendMessage(ChatColor.GOLD + "Your newbie protection has ended. You are now vulnerable to attacks by other players!");
    }

}


