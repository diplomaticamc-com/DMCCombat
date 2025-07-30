package com.diplomaticamc.dmccombat.combat.listener;

import io.papermc.paper.event.player.PlayerItemCooldownEvent;
import com.diplomaticamc.dmccombat.DMCCombat;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRiptideEvent;

import static com.diplomaticamc.dmccombat.combat.CombatHandler.isTagged;

public class PlayerItemCooldownListener implements Listener {
    private final DMCCombat plugin;

    public PlayerItemCooldownListener(DMCCombat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerEnderPearlUse (PlayerItemCooldownEvent event) {
        if (event.getType() != Material.ENDER_PEARL)
            return;
        event.setCooldown(plugin.getConfig().getInt("ender_pearl_cooldown_ticks",240));
    }
    @EventHandler
    public void onGAppleEat(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.GOLDEN_APPLE || event.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE ){
            event.getPlayer().setCooldown(event.getItem().getType(), plugin.getConfig().getInt("golden_apple_cooldown_ticks",100));
        }
    }
    @EventHandler
    public void onRiptide(PlayerRiptideEvent event) {
        event.getItem();
        if (event.getItem().containsEnchantment(Enchantment.RIPTIDE)){
            if(isTagged(event.getPlayer())){
                event.getPlayer().setCooldown(Material.TRIDENT,plugin.getConfig().getInt("riptide_in_combat_cooldown_ticks",1200));
                event.getPlayer().sendMessage("§cRiptide is on cooldown due to combat!");
            }
        }
    }
}

