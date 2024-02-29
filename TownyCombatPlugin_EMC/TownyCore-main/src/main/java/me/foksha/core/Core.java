package me.foksha.core;

import me.foksha.core.combat.CombatHandler;
import me.foksha.core.combat.bossbar.BossBarTask;
import me.foksha.core.combat.listener.CombatListener;
import me.foksha.core.combat.listener.CommandListener;
import me.foksha.core.commands.CombatTagCommand;
import me.foksha.core.util.Translation;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class Core extends JavaPlugin {

    private static Core instance;

    public static Core getInstance() {
        return instance;
    }

    private static Logger log = Bukkit.getLogger();

    public CombatHandler combatHandler;

    public CombatHandler getCombatHandler() {
        return combatHandler;
    }

    @Override
    public void onEnable() {
        instance = this;
        log.info("§e======= §a TownyCombatTag §e=======");

        Translation.loadStrings();

        setupListeners();
        setupCommands();
        runTasks();

        Bukkit.broadcastMessage("TownyCombatTag has been loaded.");
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new CombatListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(),this);
    }

    private void setupCommands() {
        log.info("§5= §bRegistering Commands");
        Objects.requireNonNull(getCommand("combattag")).setExecutor(new CombatTagCommand());
    }

    private void runTasks() {
        new BossBarTask().runTaskTimer(this, 10, 10);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "TownyCombatTag has been disabled.");
    }

}
