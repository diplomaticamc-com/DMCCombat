package com.diplomaticamc.dmccombat.config;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static void init(FileConfiguration config) {
        config.addDefault("ender_pearl_cooldown_ticks", 240);
        config.addDefault("golden_apple_cooldown_ticks",100);
        config.addDefault("riptide_in_combat_cooldown_ticks",400);

        config.addDefault("newbie_protection.protection_time", 43200);
        config.addDefault("newbie_protection.cancel_duration", 10);

        config.addDefault("reload-message", "&aConfiguration reloaded");

        config.addDefault("spawn_protection.use_chunks", false);
        config.addDefault("spawn_protection.chunks_amount", 32);
        config.addDefault("spawn_protection.blocks_amount", 32);

        config.options().copyDefaults(true);
    }
}
