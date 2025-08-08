package com.diplomaticamc.dmccombat.manager;

import com.diplomaticamc.dmccombat.DMCCombat;
import com.diplomaticamc.dmccombat.combat.listener.NewbieProtectionListener;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import it.unimi.dsi.fastutil.Hash;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

// handles loading, saving and querying of newbie protection data

public class NewbieManager {
    private final DMCCombat plugin;

    private static long protectionTime;
    private static long cancelVal;

    //newbie protection metadata
    private static String keyname = "dmccombat_newbieprotection";
    private static IntegerDataField newbieMetaData;

    // List that contains online users to deduct noob prot from
    private final HashSet<Player> protectedList = new HashSet<>();
    // List that contains users pending cancellation of newbie protection
    private final HashMap<Player, Long> cancelList = new HashMap<>();


    // deduction counter task
    private BukkitTask expiryTask;

    public NewbieManager(DMCCombat plugin) {
        this.plugin = plugin;
    }

    public void init() {
        loadConfig();
        startExpiryTask();
        registerMetaData();
    }

    public void shutdown() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
    }

    public void registerMetaData() {
        //newbie prot initialize towny metadata
        try {
            TownyAPI.getInstance().registerCustomDataField(newbieMetaData);
        } catch (KeyAlreadyRegisteredException e) {
            plugin.getLogger().info("newbieMetaData failed to register!");
            plugin.getLogger().warning(e.getMessage());
        }
        plugin.getLogger().info("newbieMetaData successfully registered to Towny!");
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        protectionTime = config.getLong("newbie_protection.protection-time", 43200);
        cancelVal = config.getLong("newbie_protection.cancel_duration", 10);

        newbieMetaData =  new IntegerDataField(keyname, (int) protectionTime);
    }

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

    // cancellation deduction function things

    public void addCancelList(Player player) {
        cancelList.put(player, cancelVal);
    }

    public void removeCancelList(Player player) {
        cancelList.remove(player);
    }

    // the checks

    // returns true if the player still has protection left
    public boolean isProtected(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);

        if (resident != null) {
            return resident.hasMeta(newbieMetaData.getKey());
        }
        return false;
    }

    //returns true if the player has tried to cancel their newbie protection via /newbie disable
    public boolean isCancelPending(Player player) {
        for (Map.Entry<Player, Long> entry : cancelList.entrySet()) {
            Player matchedPlayer = entry.getKey();
            return matchedPlayer == player;
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
                //Newbie protection deduction
                for (Player player : protectedList) {
                    if (player != null && player.isOnline()) {
                        int currentTime = readData(player);

                        if (currentTime >= 1) {
                            saveData(player, currentTime - 1);
                        } else {
                            removeProtection(player);
                            ProtectionEnded(player);
                            protectedList.remove(player);
                        }
                    }
                }
                //Cancel list deduction
                for (Map.Entry<Player, Long> entry : cancelList.entrySet()) {
                    Player player = entry.getKey();
                    long currentTime = entry.getValue();

                    if (player != null && player.isOnline()) {
                        if (currentTime >= 1) {
                            cancelList.put(player, currentTime - 1);
                        } else {
                            cancelList.remove(player);
                            CancelEnded(player);
                        }
                    } else {
                        cancelList.remove(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void startRegistryTask(Player player) {
        new BukkitRunnable() {
            int attempts = 0;
            public void run() {
                attempts++;
                Resident resident = TownyAPI.getInstance().getResident(player);

                if (resident != null) {
                    addProtection(player);
                    addProtectedList(player);
                    NewProtection(player);
                    plugin.getLogger().info("Success adding protection to new player " + player.getName());

                    cancel();
                }
                if (attempts >=25) {
                    cancel();
                    plugin.getLogger().warning("Failed to add newbie protection to new player! Resident data not found after max attempts!");
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
        //NOT ASYNC. This will need to be changed for folia compatibility or to handle multiple players
    }

    //chat things
    public void ProtectionEnded(Player player) {
        player.sendMessage(ChatColor.RED  + "You have lost the power of Newbie Protection! You are now vulnerable to attacks by other players!");

    }
    public void NewProtection(Player player) {
        player.sendMessage(ChatColor.GREEN + "You have acquired the power of " + ChatColor.WHITE + "Newbie Protection" + ChatColor.GREEN + "! You are protected from damage by players for a short period of time!");
        player.sendMessage(ChatColor.GREEN + "Use /protectiontime to check remaining protection time.");
    }
    public void CancelEnded(Player player) {
        player.sendMessage(ChatColor.RED + "Your request to disable newbie protection has expired.");
    }

}


