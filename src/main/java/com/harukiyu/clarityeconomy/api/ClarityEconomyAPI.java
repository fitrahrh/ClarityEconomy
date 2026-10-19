package com.harukiyu.clarityeconomy.api;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.api.model.JobType;
import com.harukiyu.clarityeconomy.api.model.PlayerJobData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ClarityEconomyAPI {

    private static ClarityEconomy plugin;

    private ClarityEconomyAPI() {}

    public static void init(@NotNull ClarityEconomy inst) {
        plugin = inst;
    }

    @Nullable
    public static PlayerJobData getPlayerJobData(@NotNull UUID uuid) {
        return plugin != null ? plugin.getJobsManager().getPlayerData(uuid) : null;
    }

    @Nullable
    public static PlayerJobData getPlayerJobData(@NotNull OfflinePlayer player) {
        return getPlayerJobData(player.getUniqueId());
    }

    @Nullable
    public static JobType getPlayerJob(@NotNull UUID uuid) {
        PlayerJobData data = getPlayerJobData(uuid);
        return data != null ? data.getJobType() : null;
    }

    public static int getPlayerJobLevel(@NotNull UUID uuid) {
        PlayerJobData data = getPlayerJobData(uuid);
        return data != null ? data.getLevel() : 1;
    }

    public static boolean isBlackMarketOpen() {
        return plugin != null && plugin.getBlackMarketManager().isOpen();
    }

    public static boolean isAuctionSessionActive() {
        return plugin != null && plugin.getAuctionManager().isSessionActive();
    }

    public static boolean isPlayerFrozen(@NotNull Player player) {
        return plugin != null && plugin.getBlackMarketManager().isFrozen(player);
    }
}
