package com.diplomaticamc.dmccombat;

import com.diplomaticamc.dmccombat.manager.NewbieManager;
import com.diplomaticamc.dmccombat.commands.NewbieCommand;
import com.diplomaticamc.dmccombat.combat.listener.NewbieProtectionListener;
import com.diplomaticamc.dmccombat.combat.CombatHandler;
import com.diplomaticamc.dmccombat.combat.bossbar.BossBarTask;
import com.diplomaticamc.dmccombat.combat.listener.CombatListener;
import com.diplomaticamc.dmccombat.combat.listener.CommandListener;
import com.diplomaticamc.dmccombat.combat.listener.PlayerItemCooldownListener;
import com.diplomaticamc.dmccombat.combat.listener.SpawnProtectionListener;
import com.diplomaticamc.dmccombat.commands.CombatPrefCommand;
import com.diplomaticamc.dmccombat.commands.CombatTagCommand;
import com.diplomaticamc.dmccombat.commands.SpawnProtPrefCommand;
import com.diplomaticamc.dmccombat.config.Config;
import com.diplomaticamc.dmccombat.util.Translation;
import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class DMCCombat extends JavaPlugin {

    private NewbieManager newbieManager;

    private static DMCCombat instance;

    public static DMCCombat getInstance() {
        return instance;
    }

    private static final Logger log = Bukkit.getLogger();

    public CombatHandler combatHandler;

    public CombatHandler getCombatHandler() {
        return combatHandler;
    }

    //newbie protection metadata
    private static String keyname = "dmccombat_newbieprotection";
    private static int defaultVal = 60;
    private static String label = "Newbie Protection";
    private static IntegerDataField newbieMetaData = new IntegerDataField(keyname, defaultVal, label);

    @Override
    public void onEnable() {
        instance = this;
        log.info("§e======= §a DMCCombat §e=======");

        Translation.loadStrings();

        Config.init(getConfig());
        saveConfig();
        newbieManager = new NewbieManager(this);
        newbieManager.init(newbieMetaData);

        setupListeners();
        setupCommands();
        runTasks();


        //newbie prot initialize towny metadata
        try {

        } catch (KeyAlreadyRegisteredException e) {

        }

        log.info("DMCCombat has been loaded.");
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new CombatListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(),this);
        getServer().getPluginManager().registerEvents(new PlayerItemCooldownListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new NewbieProtectionListener(newbieManager), this);
    }

    private void setupCommands() {
        log.info("§5= §bRegistering Commands");
        Objects.requireNonNull(getCommand("combattag")).setExecutor(new CombatTagCommand(this));
        Objects.requireNonNull(getCommand("combatpref")).setExecutor(new CombatPrefCommand(this));
        Objects.requireNonNull(getCommand("spawnprotpref")).setExecutor(new SpawnProtPrefCommand(this));

        NewbieCommand newbieCommand = new NewbieCommand(newbieManager, this);
        Objects.requireNonNull(getCommand("newbie")).setExecutor(newbieCommand);
        Objects.requireNonNull(getCommand("newbie")).setTabCompleter(newbieCommand);
        Objects.requireNonNull(getCommand("protectiontime")).setExecutor(newbieCommand);
    }

    private void runTasks() {
        new BossBarTask().runTaskTimer(this, 10, 10);
    }

    public void reloadAll() {
        reloadConfig();
        Config.init(getConfig());
        saveConfig();

        if (newbieManager != null) {
            newbieManager.shutdown();
            newbieManager.init(newbieMetaData);
        }

        // Clear existing boss bars before restarting the task
        BossBarTask.clearAll();
        runTasks();
    }

    @Override
    public void onDisable() {
        if (newbieManager != null) {
            newbieManager.shutdown();
        }
        BossBarTask.clearAll();
        // Plugin shutdown logic
        log.warning("DMCCombat has been disabled.");
    }

}
