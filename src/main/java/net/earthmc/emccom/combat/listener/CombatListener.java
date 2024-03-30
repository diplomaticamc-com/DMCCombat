package net.earthmc.emccom.combat.listener;

import com.palmergames.bukkit.towny.event.player.PlayerKilledPlayerEvent;
import com.palmergames.util.TimeTools;
import net.earthmc.emccom.combat.CombatHandler;
import com.google.common.collect.ImmutableSet;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import net.earthmc.emccom.combat.bossbar.BossBarTask;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import java.util.*;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Material.DIAMOND_SWORD;

public class CombatListener implements Listener {

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
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (!world.isPVP() && !isCombatWeapon(weapon.getType()))
            return;

        if (!CombatHandler.isTagged(victim))
            return;

        event.setCancelled(false);
    }
    //ItemStack weapon = attacker.getInventory().getItemInMainHand();
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player damaged = (Player) event.getEntity();
        Player damager;

        if ((event.getDamager() instanceof Player)) {
            damager = (Player) event.getDamager();
            //ItemStack weapon = damager.getInventory().getItemInMainHand();
            //if (isCombatWeapon((weapon.getType()))){
            CombatHandler.applyTag(damager);

        } else if (event.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter == null || !(shooter instanceof Player))
                return;

            damager = (Player) shooter;
        }

        else {
            return;
        }

        if (damager.equals(damaged))
            return;




        CombatHandler.applyTag(damaged);
    }
    private boolean isCombatWeapon(Material material) {
        Set<Material> combatWeapons = EnumSet.of(
                Material.DIAMOND_SWORD,
                Material.GOLDEN_SWORD,
                Material.IRON_SWORD,
                Material.STONE_SWORD,
                Material.NETHERITE_SWORD,
                Material.WOODEN_SWORD,
                Material.WOODEN_AXE,
                Material.STONE_AXE,
                Material.GOLDEN_AXE,
                Material.IRON_AXE,
                Material.DIAMOND_AXE,
                Material.NETHERITE_AXE,
                Material.TRIDENT
                
        );

        return combatWeapons.contains(material);
    }

    /*@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCobweb(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.COBWEB)
            return;

        if (!CombatHandler.isTagged(event.getPlayer()))
            return;

        event.setCancelled(true);

        event.getPlayer().sendMessage(ChatColor.RED + "You can't place cobwebs while being in combat.");
    }*/

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

    // Lowercase
    //private static final Set<String> BLACKLISTED_COMMANDS = ImmutableSet.of("t spawn","n spawn","warp","trade","res spawn","home","tradesystem:trade");

/*    @EventHandler
    public void onPreProcessCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!CombatHandler.isTagged(player) || player.hasPermission("earthpol.combattag.bypass"))
            return;
        String message = event.getMessage().substring(1);
        for (String value : BLACKLISTED_COMMANDS) {
            if (message.toLowerCase().startsWith(value + " "))
                return;
            if(message.equalsIgnoreCase(value));
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can't use that command while being in combat.");
        }
    }
*/


    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
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
    public void onRiptide(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!CombatHandler.isTagged(player))
            return;

        if (!player.isRiptiding())
            return;

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "The riptide enchantment is disabled in combat.");
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

    /*Just edit purpur configuration and put enderpearl cooldown on 320 ticks (16 seconds)
    @EventHandler
    public void onPearl(ProjectileLaunchEvent event) {
        if(!(event.getEntity() instanceof EnderPearl))
            return;

        EnderPearl pearl = (EnderPearl) event.getEntity();

        if(!(pearl.getShooter() instanceof Player))
            return;

        Player player = (Player) pearl.getShooter();
        // Pearl cooldown (16 seconds)
        player.setCooldown(Material.ENDER_PEARL, 16 * 20);

    @EventHandler
    public void onPearl(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if(event.getItem().getType() != Material.ENDER_PEARL)
            return;

        // Pearl cooldown (16 seconds)
        event.getPlayer().setCooldown(Material.ENDER_PEARL, 16 * 20);
    } */

}
