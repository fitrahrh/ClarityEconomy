package com.harukiyu.clarityeconomy.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

@SuppressWarnings("null")
public final class VaultHook {

    private static Economy economy = null;
    private static boolean isVaultPresent = false;
    private static final DecimalFormat COMPACT_FORMAT = new DecimalFormat("#,##0.#");
    private static final DecimalFormat STANDARD_FORMAT = new DecimalFormat("#,##0.00");

    private VaultHook() {}

    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                isVaultPresent = (economy != null);
            }
        }
    }

    public static boolean hasEconomy() {
        return isVaultPresent && economy != null;
    }

    @Nullable
    public static Economy getEconomy() {
        return economy;
    }

    public static double getBalance(@NotNull OfflinePlayer player) {
        if (!hasEconomy()) return 0.0;
        try {
            return economy.getBalance(player);
        } catch (Throwable t) {
            return 0.0;
        }
    }

    public static boolean hasEnough(@NotNull OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    public static boolean withdraw(@NotNull OfflinePlayer player, double amount) {
        if (!hasEconomy() || amount <= 0) return false;
        try {
            return economy.withdrawPlayer(player, amount).transactionSuccess();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean deposit(@NotNull OfflinePlayer player, double amount) {
        if (!hasEconomy() || amount <= 0) return false;
        try {
            return economy.depositPlayer(player, amount).transactionSuccess();
        } catch (Throwable t) {
            return false;
        }
    }

    public static String formatMoney(double amount) {
        if (amount >= 1_000_000_000_000.0) {
            return COMPACT_FORMAT.format(amount / 1_000_000_000_000.0) + "T";
        }
        if (amount >= 1_000_000_000.0) {
            return COMPACT_FORMAT.format(amount / 1_000_000_000.0) + "B";
        }
        if (amount >= 1_000_000.0) {
            return COMPACT_FORMAT.format(amount / 1_000_000.0) + "M";
        }
        if (amount >= 1_000.0) {
            return COMPACT_FORMAT.format(amount / 1_000.0) + "K";
        }
        return STANDARD_FORMAT.format(amount);
    }
}
