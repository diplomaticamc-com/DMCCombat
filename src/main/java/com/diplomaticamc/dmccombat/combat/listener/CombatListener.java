package com.diplomaticamc.dmccombat.combat.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.util.TimeTools;
import com.diplomaticamc.dmccombat.combat.CombatHandler;
import com.diplomaticamc.dmccombat.combat.bossbar.BossBarTask;
import com.diplomaticamc.dmccombat.manager.ResidentMetadataManager;
import com.diplomaticamc.dmccombat.object.CombatPref;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;

import static com.diplomaticamc.dmccombat.object.CombatPref.UNSAFE;

public class CombatListener implements Listener {
    public CombatListener() {
    }

    List<String> messagesList = Arrays.asList(
            "used Combat Log! It's a One-Hit KO!",
            "was killed for logging out in combat.",
            "surrendered to the disconnect button.",
            "combat-logged! Shame on them!"
    );

    Random random = new Random();
    CombatLogMessages messageSelector = new CombatLogMessages(random, messagesList);

    public static final long TAG_TIME = 30 * 1000;
    public final static int effectDurationTicks = (int)(TimeTools.convertToTicks(TAG_TIME/1000));

    private Set<UUID> deathsForLoggingOut = new HashSet<>();

    // Prevent claim hopping
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPvP(TownyPlayerDamagePlayerEvent event) {
        if (!event.isCancelled())
            return;

        TownyWorld world = TownyAPI.getInstance().getTownyWorld(event.getVictimPlayer().getWorld().getName());
        Player attacker = event.getAttackingPlayer();
        Player victim = event.getVictimPlayer();
        Resident attackerAsResident = TownyAPI.getInstance().getResident(attacker);
        ResidentMetadataManager rmm = new ResidentMetadataManager();
        CombatPref combatPrefOfAttacker = rmm.getResidentCombatPref(attackerAsResident);

        if (!world.isPVP() || !CombatHandler.isTagged(victim)) {
            return;
        }

        if (CombatHandler.isTagged(victim)) { // If the victim is tagged
            if (combatPrefOfAttacker == UNSAFE || CombatHandler.isTagged(attacker)) {
                event.setCancelled(false); // Allow combat if attacker is UNSAFE or already tagged
            } else {
                return; // Otherwise, return without allowing combat
            }
        }
        event.setCancelled(false);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player damaged = (Player) event.getEntity();
        Player damager;

        if ((event.getDamager() instanceof Player)) {
            damager = (Player) event.getDamager();
            CombatHandler.applyTag(damager);

        } else if (event.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter == null || !(shooter instanceof Player) || (shooter == damaged)){
                return;
            }
            if (event.getDamager() instanceof EnderPearl) {
                return;
            }

            damager = (Player) shooter;
            CombatHandler.applyTag(damager);
        }

        else {
            return;
        }

        if (damager.equals(damaged))
            return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && damaged.getLastDamageCause() != null && damaged.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.FALL)
            return;




        CombatHandler.applyTag(damaged);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!CombatHandler.isTagged(player))
            return;
        if (!(player == null)) {
            BossBarTask.remove(player);
            CombatHandler.removeTag(player);

            TownBlock townBlock = TownyAPI.getInstance().getTownBlock(player.getLocation());
            if (townBlock != null && townBlock.getType() == TownBlockType.ARENA && townBlock.hasTown())
                return;

            deathsForLoggingOut.add(player.getUniqueId());
            player.setHealth(0.0);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (deathsForLoggingOut.contains(player.getUniqueId())) {
            deathsForLoggingOut.remove(player.getUniqueId());
            event.deathMessage(Component.text(player.getName() +" "+ messageSelector.getRandomMessage()));
        }

        if (!CombatHandler.isTagged(player))
            return;

        CombatHandler.removeTag(player);


    }
    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (!(killer == null)) {
            BossBarTask.remove(killer);
            killer.sendMessage(ChatColor.GREEN + "Your enemy is dead. You are no longer in combat.");
            CombatHandler.removeTag(killer);

        }
    }
    @EventHandler
    public void onOpenEnderChest(InventoryOpenEvent event) {
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST)
            return;

        if (!(event.getPlayer() instanceof Player))
            return;

        Player player = (Player) event.getPlayer();

        if (!CombatHandler.isTagged(player))
            return;

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You can't use your ender chest while being in combat.");
    }


    @EventHandler
    public void onElytraFly(PlayerMoveEvent event){
        Player player = event.getPlayer();

        if (!CombatHandler.isTagged(player))
            return;
        if(!player.isGliding())
            return;
        event.setCancelled(true);
        player.sendMessage((ChatColor.RED + "Elytras aren't enabled in combat."));
    }

    /* vvv Anti-Debuff stuff below vvv */


    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event){
        Player player = event.getPlayer();
        checkAndApplyEffect(player);

    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        checkAndApplyEffect(player);
    }
    private boolean isAntiDebuff(ItemStack item) {
        return item !=null && item.getType() == Material.TOTEM_OF_UNDYING;
    }
    private void checkAndApplyEffect(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (isAntiDebuff(mainHand) || isAntiDebuff(offHand)) {

            PotionEffectType[] effectsToRemove = {
                    PotionEffectType.SLOW_FALLING,
                    PotionEffectType.WEAKNESS,
                    PotionEffectType.WITHER,
                    PotionEffectType.SLOW,//PotionEffectType.SLOWNESS, //PotionEffectType.SLOW 1.20.4
                    PotionEffectType.BLINDNESS,
                    PotionEffectType.DAMAGE_RESISTANCE,//PotionEffectType.RESISTANCE, //PotionEffectType.DAMAGE_RESISTANCE 1.20.4
                    PotionEffectType.JUMP,//PotionEffectType.JUMP_BOOST, //PotionEffectType.JUMP 1.20.4
                    PotionEffectType.POISON,
                    PotionEffectType.CONFUSION //1.20.4
            };
            for (PotionEffectType effect : effectsToRemove) {
                player.removePotionEffect(effect);
            }


        }
    }
}
