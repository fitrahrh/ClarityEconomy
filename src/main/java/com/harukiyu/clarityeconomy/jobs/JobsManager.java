package com.harukiyu.clarityeconomy.jobs;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.api.event.PlayerJobChangeEvent;
import com.harukiyu.clarityeconomy.api.event.PlayerJobLevelUpEvent;
import com.harukiyu.clarityeconomy.api.model.JobType;
import com.harukiyu.clarityeconomy.api.model.PlayerJobData;
import com.harukiyu.clarityeconomy.config.ConfigManager;
import com.harukiyu.clarityeconomy.hook.LanguageHook;
import com.harukiyu.clarityeconomy.hook.VaultHook;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("null")
public class JobsManager {

    private final ClarityEconomy plugin;
    private final JobStorageManager storageManager;

    public JobsManager(@NotNull ClarityEconomy plugin, @NotNull JobStorageManager storageManager) {
        this.plugin = plugin;
        this.storageManager = storageManager;
    }

    @NotNull
    public PlayerJobData getPlayerData(@NotNull UUID uuid) {
        return storageManager.getPlayerData(uuid);
    }

    public long calculateRequiredExp(int level) {
        ConfigManager cfg = plugin.getConfigManager();
        if (level >= cfg.getJobMaxLevel()) return Long.MAX_VALUE;
        long base = cfg.getJobBaseExp();
        double mult = cfg.getJobExpMultiplier();
        return Math.max(10L, (long) (base * Math.pow(level, mult)));
    }

    public void addExp(@NotNull Player player, long exp) {
        if (exp <= 0) return;
        PlayerJobData data = getPlayerData(player.getUniqueId());
        JobType job = data.getJobType();
        if (job == null) return;

        ConfigManager cfg = plugin.getConfigManager();
        if (data.getLevel() >= cfg.getJobMaxLevel()) return;

        data.addExp(exp);

        boolean leveledUp = false;
        int oldLevel = data.getLevel();

        while (data.getLevel() < cfg.getJobMaxLevel()) {
            long req = calculateRequiredExp(data.getLevel());
            if (data.getCurrentExp() >= req) {
                data.setCurrentExp(data.getCurrentExp() - req);
                data.setLevel(data.getLevel() + 1);
                leveledUp = true;
            } else {
                break;
            }
        }

        if (leveledUp) {
            PlayerJobLevelUpEvent event = new PlayerJobLevelUpEvent(player, job, oldLevel, data.getLevel());
            Bukkit.getPluginManager().callEvent(event);

            try {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
            } catch (Throwable ignored) {}

            LanguageHook.sendMessage(player, "jobs.level_up",
                    Placeholder.unparsed("job", job.getDisplayName()),
                    Placeholder.unparsed("level", String.valueOf(data.getLevel())));

            if (data.getLevel() >= cfg.getJobMaxLevel()) {
                LanguageHook.sendMessage(player, "jobs.max_level_reached",
                        Placeholder.unparsed("job", job.getDisplayName()),
                        Placeholder.unparsed("buff", job.getSelfBuffDesc()));
            }
        }

        storageManager.savePlayerData(data, true);
    }

    public boolean changeJob(@NotNull Player player, @NotNull JobType newJob) {
        PlayerJobData data = getPlayerData(player.getUniqueId());
        if (data.getJobType() == newJob) {
            LanguageHook.sendMessage(player, "jobs.already_has_job",
                    Placeholder.unparsed("job", newJob.getDisplayName()));
            return false;
        }

        ConfigManager cfg = plugin.getConfigManager();
        double cost = (data.getJobType() != null) ? cfg.getJobResetCost() : 0.0;

        if (cost > 0) {
            if (!VaultHook.hasEnough(player, cost)) {
                LanguageHook.sendMessage(player, "jobs.not_enough_money",
                        Placeholder.unparsed("cost", VaultHook.formatMoney(cost)),
                        Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));
                return false;
            }
        }

        PlayerJobChangeEvent event = new PlayerJobChangeEvent(player, data.getJobType(), newJob, cost);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        if (event.getCost() > 0) {
            if (!VaultHook.withdraw(player, event.getCost())) {
                LanguageHook.sendMessage(player, "market.transaction_failed");
                return false;
            }
        }

        data.setJobType(newJob);
        data.reset(); // Level resets to 1 and Exp to 0 per specification!

        storageManager.savePlayerData(data, true);

        try {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } catch (Throwable ignored) {}

        LanguageHook.sendMessage(player, "jobs.job_changed",
                Placeholder.unparsed("job", newJob.getDisplayName()),
                Placeholder.unparsed("cost", VaultHook.formatMoney(event.getCost())));

        return true;
    }

    public boolean resignJob(@NotNull Player player) {
        PlayerJobData data = getPlayerData(player.getUniqueId());
        JobType oldJob = data.getJobType();
        if (oldJob == null) {
            return false;
        }

        ConfigManager cfg = plugin.getConfigManager();
        double cost = cfg.getJobResetCost();

        if (cost > 0) {
            if (!VaultHook.hasEnough(player, cost)) {
                LanguageHook.sendMessage(player, "jobs.not_enough_money",
                        Placeholder.unparsed("cost", VaultHook.formatMoney(cost)),
                        Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));
                return false;
            }
        }

        PlayerJobChangeEvent event = new PlayerJobChangeEvent(player, oldJob, null, cost);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        if (event.getCost() > 0) {
            if (!VaultHook.withdraw(player, event.getCost())) {
                LanguageHook.sendMessage(player, "market.transaction_failed");
                return false;
            }
        }

        data.setJobType(null);
        data.reset();
        storageManager.savePlayerData(data, true);

        try {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1.2f);
        } catch (Throwable ignored) {}

        LanguageHook.sendMessage(player, "jobs.job_resigned",
                Placeholder.unparsed("job", oldJob.getDisplayName()),
                Placeholder.unparsed("cost", VaultHook.formatMoney(event.getCost())));

        return true;
    }
}
