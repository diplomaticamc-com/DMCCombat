package net.earthmc.emccom;

import net.earthmc.emccom.manager.NewbieManager;
import net.earthmc.emccom.commands.NewbieCommand;
import net.earthmc.emccom.combat.listener.NewbieProtectionListener;
import net.earthmc.emccom.combat.CombatHandler;
import net.earthmc.emccom.combat.bossbar.BossBarTask;
import net.earthmc.emccom.combat.listener.CombatListener;
import net.earthmc.emccom.combat.listener.CommandListener;
import net.earthmc.emccom.combat.listener.PlayerItemCooldownListener;
import net.earthmc.emccom.combat.listener.SpawnProtectionListener;
import net.earthmc.emccom.commands.CombatPrefCommand;
import net.earthmc.emccom.commands.CombatTagCommand;
import net.earthmc.emccom.commands.SpawnProtPrefCommand;
import net.earthmc.emccom.config.Config;
import net.earthmc.emccom.util.Translation;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class EMCCOM extends JavaPlugin {

    private NewbieManager newbieManager;

    private static EMCCOM instance;

    public static EMCCOM getInstance() {
        return instance;
    }

    private static final Logger log = Bukkit.getLogger();

    public CombatHandler combatHandler;

    public CombatHandler getCombatHandler() {
        return combatHandler;
    }

    @Override
    public void onEnable() {
        instance = this;
        log.info("§e======= §a EMCCOM (DMC Fork) §e=======");

        Translation.loadStrings();

        Config.init(getConfig());
        saveConfig();
        newbieManager = new NewbieManager(this);
        newbieManager.init();

        setupListeners();
        setupCommands();
        runTasks();




        log.info("EMCCOM (DMC Fork) has been loaded.");
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
            newbieManager.init();
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
        log.warning("EMCCOM has been disabled.");
    }

}
