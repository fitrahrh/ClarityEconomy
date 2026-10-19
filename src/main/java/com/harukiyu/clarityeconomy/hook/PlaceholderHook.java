package com.harukiyu.clarityeconomy.hook;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.api.model.JobType;
import com.harukiyu.clarityeconomy.api.model.PlayerJobData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {

    private final ClarityEconomy plugin;

    public PlaceholderHook(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clarityeconomy";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Harukiyu";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (params.equalsIgnoreCase("blackmarket_status")) {
            return plugin.getBlackMarketManager().isOpen() ? "OPEN" : "CLOSED";
        }
        if (params.equalsIgnoreCase("blackmarket_remaining")) {
            return plugin.getBlackMarketManager().getTimeRemainingFormatted();
        }
        if (params.equalsIgnoreCase("auction_status")) {
            return plugin.getAuctionManager().isSessionActive() ? "OPEN" : "CLOSED";
        }
        if (params.equalsIgnoreCase("auction_remaining")) {
            return plugin.getAuctionManager().getTimeRemainingFormatted();
        }
        if (params.equalsIgnoreCase("promotion_active_count")) {
            return String.valueOf(plugin.getPromotionManager().getActivePromotionsCount());
        }

        if (offlinePlayer == null) return "";

        if (params.equalsIgnoreCase("balance")) {
            return String.valueOf(VaultHook.getBalance(offlinePlayer));
        }
        if (params.equalsIgnoreCase("balance_formatted")) {
            return VaultHook.formatMoney(VaultHook.getBalance(offlinePlayer));
        }

        PlayerJobData jobData = plugin.getJobsManager().getPlayerData(offlinePlayer.getUniqueId());
        JobType job = jobData.getJobType();
        long reqExp = plugin.getJobsManager().calculateRequiredExp(jobData.getLevel());

        if (params.equalsIgnoreCase("job")) {
            return job != null ? job.name() : "NONE";
        }
        if (params.equalsIgnoreCase("job_display")) {
            return job != null ? job.getDisplayName() : "None";
        }
        if (params.equalsIgnoreCase("job_level")) {
            return String.valueOf(jobData.getLevel());
        }
        if (params.equalsIgnoreCase("job_exp")) {
            return String.valueOf(jobData.getCurrentExp());
        }
        if (params.equalsIgnoreCase("job_req_exp")) {
            return String.valueOf(reqExp);
        }
        if (params.equalsIgnoreCase("job_progress_percent")) {
            if (jobData.getLevel() >= plugin.getConfigManager().getJobMaxLevel()) return "100%";
            double pct = reqExp > 0 ? ((double) jobData.getCurrentExp() / reqExp) * 100.0 : 0.0;
            return String.format("%.1f%%", Math.min(100.0, pct));
        }

        return null;
    }
}
