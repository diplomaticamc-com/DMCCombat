package net.earthmc.emccom;

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
        log.info("§e======= §a EMCCOM §e=======");

        Translation.loadStrings();

        Config.init(getConfig());
        saveConfig();
        setupListeners();
        setupCommands();
        runTasks();

        log.info("EMCCOM has been loaded.");
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new CombatListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(),this);
        getServer().getPluginManager().registerEvents(new PlayerItemCooldownListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(), this);
    }

    private void setupCommands() {
        log.info("§5= §bRegistering Commands");
        Objects.requireNonNull(getCommand("combattag")).setExecutor(new CombatTagCommand());
        Objects.requireNonNull(getCommand("combatpref")).setExecutor(new CombatPrefCommand());
        Objects.requireNonNull(getCommand("spawnprotpref")).setExecutor(new SpawnProtPrefCommand());
    }

    private void runTasks() {
        new BossBarTask().runTaskTimer(this, 10, 10);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        log.warning("EMCCOM has been disabled.");
    }

}
