package com.harukiyu.clarityeconomy.listeners;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.api.model.JobType;
import com.harukiyu.clarityeconomy.api.model.PlayerJobData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class JobActivityListener implements Listener {

    private final ClarityEconomy plugin;
    private final Map<UUID, Location> lastExplorerLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Double> distanceTraveled = new ConcurrentHashMap<>();

    public JobActivityListener(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    // 1. HUNTER: Mob Kills
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        PlayerJobData data = plugin.getJobsManager().getPlayerData(killer.getUniqueId());
        if (data.getJobType() != JobType.HUNTER) return;

        Entity dead = event.getEntity();
        long exp = 10L;
        if (dead instanceof Monster) {
            exp = 20L;
        }
        if (dead.getType().name().contains("BOSS") || dead.getType().name().contains("WITHER") || dead.getType().name().contains("DRAGON") || dead.getType().name().contains("WARDEN")) {
            exp = 150L;
        }

        plugin.getJobsManager().addExp(killer, exp);
    }

    // 2. FARMER, MINER, LUMBERJACK, DIGGER: Block Break Events
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material mat = block.getType();
        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        JobType job = data.getJobType();

        // FARMER check
        if (isCrop(mat)) {
            if (isFullyGrown(block)) {
                if (job == JobType.FARMER) {
                    plugin.getJobsManager().addExp(player, 5L);

                    // Level 50 Self Buff: Double Crop Drop
                    if (data.getLevel() >= plugin.getConfigManager().getJobMaxLevel()) {
                        for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) {
                            block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
                        }
                    }
                } else {
                    // Check nearby Level 50 Farmer aura (30% extra drop chance)
                    if (isNearMaxJob(player, JobType.FARMER) && ThreadLocalRandom.current().nextDouble() < 0.30) {
                        for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) {
                            ItemStack extra = drop.clone();
                            extra.setAmount(1);
                            block.getWorld().dropItemNaturally(block.getLocation(), extra);
                            break;
                        }
                    }
                }
            }
            return;
        }

        // MINER check
        if (isOre(mat)) {
            if (job == JobType.MINER) {
                long exp = getOreExp(mat);
                plugin.getJobsManager().addExp(player, exp);
            }
            return;
        }

        // LUMBERJACK check
        if (isLog(mat)) {
            if (job == JobType.LUMBERJACK) {
                plugin.getJobsManager().addExp(player, 4L);
            }
            return;
        }

        // DIGGER check
        if (isDiggable(mat)) {
            if (job == JobType.DIGGER) {
                plugin.getJobsManager().addExp(player, 2L);
            }
        }
    }

    // 3. FISHERMAN: Fishing Event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();
            PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
            if (data.getJobType() == JobType.FISHERMAN) {
                plugin.getJobsManager().addExp(player, 25L);
            }
        }
    }

    // 4. EXPLORER: Movement Event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        if (data.getJobType() != JobType.EXPLORER) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())) return;

        Location last = lastExplorerLocations.get(player.getUniqueId());
        if (last == null || !last.getWorld().equals(to.getWorld())) {
            lastExplorerLocations.put(player.getUniqueId(), to);
            return;
        }

        double dist = last.distance(to);
        if (dist > 0.1 && dist < 15.0) { // filter out teleports
            double current = distanceTraveled.getOrDefault(player.getUniqueId(), 0.0) + dist;
            if (current >= 50.0) {
                plugin.getJobsManager().addExp(player, 3L);
                distanceTraveled.put(player.getUniqueId(), 0.0);
            } else {
                distanceTraveled.put(player.getUniqueId(), current);
            }
            lastExplorerLocations.put(player.getUniqueId(), to);
        }
    }

    // 5. BREEDER: Animal Breeding Event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player player) {
            PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
            if (data.getJobType() == JobType.BREEDER) {
                plugin.getJobsManager().addExp(player, 20L);
            }
        }
    }

    // 6. BLACKSMITH: Smelting & Crafting
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        if (data.getJobType() == JobType.BLACKSMITH) {
            long exp = Math.max(1L, event.getItemAmount() * 2L);
            plugin.getJobsManager().addExp(player, exp);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
            if (data.getJobType() == JobType.BLACKSMITH) {
                ItemStack result = event.getRecipe().getResult();
                Material m = result.getType();
                if (m.name().endsWith("_SWORD") || m.name().endsWith("_AXE") || m.name().endsWith("_PICKAXE") ||
                        m.name().endsWith("_HELMET") || m.name().endsWith("_CHESTPLATE") ||
                        m.name().endsWith("_LEGGINGS") || m.name().endsWith("_BOOTS") || m.name().endsWith("_SHIELD")) {
                    plugin.getJobsManager().addExp(player, 15L);
                }
            }
        }
    }

    // 7. PVE DAMAGE BUFFS: Breeder (+25% / +10% PvE damage) & Blacksmith (+15% weapon damage)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (event.getEntity() instanceof Player) return; // Only PvE mobs

        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        int maxLvl = plugin.getConfigManager().getJobMaxLevel();
        double multiplier = 1.0;

        // Breeder Self Buff (+25%)
        if (data.getJobType() == JobType.BREEDER && data.getLevel() >= maxLvl) {
            multiplier += 0.25;
        } else if (isNearMaxJob(player, JobType.BREEDER)) {
            // Breeder Aura Buff (+10%)
            multiplier += 0.10;
        }

        // Blacksmith Self Buff (+15% weapon damage)
        if (data.getJobType() == JobType.BLACKSMITH && data.getLevel() >= maxLvl) {
            multiplier += 0.15;
        }

        if (multiplier > 1.0) {
            event.setDamage(event.getDamage() * multiplier);
        }
    }

    // Helper checks
    private boolean isCrop(Material mat) {
        return mat == Material.WHEAT || mat == Material.CARROTS || mat == Material.POTATOES ||
                mat == Material.BEETROOTS || mat == Material.NETHER_WART || mat == Material.COCOA ||
                mat == Material.PUMPKIN || mat == Material.MELON || mat == Material.SUGAR_CANE ||
                mat == Material.BAMBOO || mat == Material.CACTUS;
    }

    private boolean isFullyGrown(Block block) {
        if (block.getBlockData() instanceof Ageable ageable) {
            return ageable.getAge() >= ageable.getMaximumAge();
        }
        return true; // melons, pumpkins, sugar cane, bamboo, cactus
    }

    private boolean isOre(Material mat) {
        String name = mat.name();
        return name.endsWith("_ORE") || name.equals("ANCIENT_DEBRIS");
    }

    private long getOreExp(Material mat) {
        String name = mat.name();
        if (name.contains("ANCIENT_DEBRIS") || name.contains("NETHERITE")) return 40L;
        if (name.contains("DIAMOND") || name.contains("EMERALD")) return 25L;
        if (name.contains("GOLD") || name.contains("LAPIS") || name.contains("REDSTONE")) return 15L;
        if (name.contains("IRON") || name.contains("COPPER")) return 10L;
        return 5L; // Coal etc.
    }

    private boolean isLog(Material mat) {
        String name = mat.name();
        return name.endsWith("_LOG") || name.endsWith("_WOOD") || name.endsWith("_STEM") || name.endsWith("_HYPHAE");
    }

    private boolean isDiggable(Material mat) {
        return mat == Material.DIRT || mat == Material.GRASS_BLOCK || mat == Material.SAND ||
                mat == Material.RED_SAND || mat == Material.GRAVEL || mat == Material.CLAY ||
                mat == Material.MUD || mat == Material.SOUL_SAND || mat == Material.SOUL_SOIL;
    }

    private boolean isNearMaxJob(@NotNull Player player, @NotNull JobType job) {
        double radius = plugin.getConfigManager().getJobAuraRadius();
        int maxLvl = plugin.getConfigManager().getJobMaxLevel();
        for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius,
                ent -> ent instanceof Player && !ent.getUniqueId().equals(player.getUniqueId()))) {
            if (e instanceof Player nearbyPlayer) {
                PlayerJobData data = plugin.getJobsManager().getPlayerData(nearbyPlayer.getUniqueId());
                if (data.getJobType() == job && data.getLevel() >= maxLvl) {
                    return true;
                }
            }
        }
        return false;
    }
}
