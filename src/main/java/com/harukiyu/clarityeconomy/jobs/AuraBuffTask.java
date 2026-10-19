package com.harukiyu.clarityeconomy.jobs;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.api.model.JobType;
import com.harukiyu.clarityeconomy.api.model.PlayerJobData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AuraBuffTask extends BukkitRunnable {

    private final ClarityEconomy plugin;
    private static final int DURATION_TICKS = 100; // 5 seconds duration (refreshed every 3 seconds)

    public AuraBuffTask(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int maxLevel = plugin.getConfigManager().getJobMaxLevel();
        double radius = plugin.getConfigManager().getJobAuraRadius();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
            JobType job = data.getJobType();
            if (job == null || data.getLevel() < maxLevel) {
                continue;
            }

            // 1. Apply Self Buffs for Level 50 player
            applySelfBuffs(player, job);

            // 2. Apply Area Aura Buffs to nearby players within radius
            Collection<Entity> nearby = player.getWorld().getNearbyEntities(
                    player.getLocation(), radius, radius, radius,
                    entity -> entity instanceof Player && !entity.getUniqueId().equals(player.getUniqueId())
            );

            for (Entity entity : nearby) {
                if (entity instanceof Player target && target.isOnline()) {
                    applyAuraBuff(target, job);
                }
            }
        }
    }

    private void applySelfBuffs(@NotNull Player player, @NotNull JobType job) {
        switch (job) {
            case HUNTER -> addEffect(player, PotionEffectType.STRENGTH, 1); // Strength II
            case MINER -> {
                addEffect(player, PotionEffectType.HASTE, 1); // Haste II
                addEffect(player, PotionEffectType.NIGHT_VISION, 0);
            }
            case FISHERMAN -> addEffect(player, PotionEffectType.LUCK, 2); // Luck III
            case EXPLORER -> addEffect(player, PotionEffectType.SPEED, 1); // Speed II
            case BLACKSMITH -> addEffect(player, PotionEffectType.RESISTANCE, 0); // Resistance I
            case ALCHEMIST -> addEffect(player, PotionEffectType.REGENERATION, 0); // Regeneration I
            case LUMBERJACK -> {
                addEffect(player, PotionEffectType.HASTE, 0);
                addEffect(player, PotionEffectType.STRENGTH, 0);
            }
            case DIGGER -> {
                addEffect(player, PotionEffectType.HASTE, 0);
                addEffect(player, PotionEffectType.SPEED, 0);
            }
            default -> {}
        }
    }

    private void applyAuraBuff(@NotNull Player target, @NotNull JobType job) {
        switch (job) {
            case HUNTER -> addEffect(target, PotionEffectType.STRENGTH, 0); // Strength I
            case MINER -> {
                addEffect(target, PotionEffectType.HASTE, 0);
                addEffect(target, PotionEffectType.NIGHT_VISION, 0);
            }
            case FISHERMAN -> addEffect(target, PotionEffectType.LUCK, 0); // Luck I
            case EXPLORER -> addEffect(target, PotionEffectType.SPEED, 0); // Speed I
            case BLACKSMITH -> addEffect(target, PotionEffectType.RESISTANCE, 0);
            case ALCHEMIST -> addEffect(target, PotionEffectType.REGENERATION, 0);
            case LUMBERJACK, DIGGER -> addEffect(target, PotionEffectType.HASTE, 0);
            default -> {}
        }
    }

    private void addEffect(@NotNull Player p, @NotNull PotionEffectType type, int amplifier) {
        p.addPotionEffect(new PotionEffect(type, DURATION_TICKS, amplifier, true, false, true));
    }
}
