package com.harukiyu.clarityeconomy.config;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigManager {

    private final ClarityEconomy plugin;

    private String currencySymbol = "$";

    // Jobs
    private int jobMaxLevel = 50;
    private double jobResetCost = 50000.0;
    private double jobAuraRadius = 10.0;
    private long jobBaseExp = 100L;
    private double jobExpMultiplier = 1.35;

    // Black Market
    private int blackMarketWindowStart = 6;
    private int blackMarketWindowEnd = 21;
    private int blackMarketDurationHours = 2;
    private int blackMarketCooldownSeconds = 60;
    private boolean blackMarketFreezePlayer = true;
    private double blackMarketPriceDiscountMultiplier = 0.2;

    // Auction
    private int auctionDailySessions = 2;
    private int auctionSessionDurationHours = 4;
    private Location auctionArenaLocation = null;

    // Promotion
    private int promotionMaxSlots = 10;
    private int promotionCooldownHours = 3;
    private int promotionDurationMinutes = 360;
    private double promotionCost = 10000.0;
    private int promotionBroadcastIntervalMinutes = 5;

    // Main Menu
    private boolean enableBackToMainMenu = true;
    private String mainMenuCommand = "menu";

    public ConfigManager(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.currencySymbol = config.getString("settings.currency-symbol", "$");

        // Jobs
        this.jobMaxLevel = config.getInt("jobs.max-level", 50);
        this.jobResetCost = config.getDouble("jobs.reset-cost", 50000.0);
        this.jobAuraRadius = config.getDouble("jobs.aura-radius", 10.0);
        this.jobBaseExp = config.getLong("jobs.base-exp", 100L);
        this.jobExpMultiplier = config.getDouble("jobs.exp-multiplier", 1.35);

        // Black Market
        this.blackMarketWindowStart = config.getInt("black-market.open-window-start", 6);
        this.blackMarketWindowEnd = config.getInt("black-market.open-window-end", 21);
        this.blackMarketDurationHours = config.getInt("black-market.duration-hours", 2);
        this.blackMarketCooldownSeconds = config.getInt("black-market.cooldown-seconds", 60);
        this.blackMarketFreezePlayer = config.getBoolean("black-market.freeze-player", true);
        this.blackMarketPriceDiscountMultiplier = config.getDouble("black-market.price-discount-multiplier", 0.2);

        // Auction
        this.auctionDailySessions = config.getInt("auction.daily-sessions", 2);
        this.auctionSessionDurationHours = config.getInt("auction.session-duration-hours", 4);
        loadAuctionLocation(config);

        // Promotion
        this.promotionMaxSlots = config.getInt("promotion.max-slots", 10);
        this.promotionCooldownHours = config.getInt("promotion.cooldown-hours", 3);
        this.promotionDurationMinutes = config.getInt("promotion.duration-minutes", 360);
        this.promotionCost = config.getDouble("promotion.cost", 10000.0);
        this.promotionBroadcastIntervalMinutes = config.getInt("promotion.broadcast-interval-minutes", 5);

        // Main Menu
        this.enableBackToMainMenu = config.getBoolean("main_menu.enable_back_to_main_menu", true);
        this.mainMenuCommand = config.getString("main_menu.command", "menu");
    }

    private void loadAuctionLocation(FileConfiguration config) {
        if (!config.contains("auction.arena-location.world")) {
            this.auctionArenaLocation = null;
            return;
        }
        String worldName = config.getString("auction.arena-location.world", "world");
        World world = worldName != null ? Bukkit.getWorld(worldName) : null;
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().getFirst();
        }
        if (world != null) {
            double x = config.getDouble("auction.arena-location.x", 0.0);
            double y = config.getDouble("auction.arena-location.y", 64.0);
            double z = config.getDouble("auction.arena-location.z", 0.0);
            float yaw = (float) config.getDouble("auction.arena-location.yaw", 0.0);
            float pitch = (float) config.getDouble("auction.arena-location.pitch", 0.0);
            this.auctionArenaLocation = new Location(world, x, y, z, yaw, pitch);
        }
    }

    public void setAuctionLocation(@NotNull Location loc) {
        this.auctionArenaLocation = loc.clone();
        FileConfiguration config = plugin.getConfig();
        config.set("auction.arena-location.world", loc.getWorld().getName());
        config.set("auction.arena-location.x", loc.getX());
        config.set("auction.arena-location.y", loc.getY());
        config.set("auction.arena-location.z", loc.getZ());
        config.set("auction.arena-location.yaw", loc.getYaw());
        config.set("auction.arena-location.pitch", loc.getPitch());
        plugin.saveConfig();
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public int getJobMaxLevel() {
        return jobMaxLevel;
    }

    public double getJobResetCost() {
        return jobResetCost;
    }

    public double getJobAuraRadius() {
        return jobAuraRadius;
    }

    public long getJobBaseExp() {
        return jobBaseExp;
    }

    public double getJobExpMultiplier() {
        return jobExpMultiplier;
    }

    public int getBlackMarketWindowStart() {
        return blackMarketWindowStart;
    }

    public int getBlackMarketWindowEnd() {
        return blackMarketWindowEnd;
    }

    public int getBlackMarketDurationHours() {
        return blackMarketDurationHours;
    }

    public int getBlackMarketCooldownSeconds() {
        return blackMarketCooldownSeconds;
    }

    public boolean isBlackMarketFreezePlayer() {
        return blackMarketFreezePlayer;
    }

    public double getBlackMarketPriceDiscountMultiplier() {
        return blackMarketPriceDiscountMultiplier;
    }

    public int getAuctionDailySessions() {
        return auctionDailySessions;
    }

    public int getAuctionSessionDurationHours() {
        return auctionSessionDurationHours;
    }

    @Nullable
    public Location getAuctionArenaLocation() {
        return auctionArenaLocation;
    }

    public int getPromotionMaxSlots() {
        return promotionMaxSlots;
    }

    public int getPromotionCooldownHours() {
        return promotionCooldownHours;
    }

    public int getPromotionDurationMinutes() {
        return promotionDurationMinutes;
    }

    public double getPromotionCost() {
        return promotionCost;
    }

    public int getPromotionBroadcastIntervalMinutes() {
        return promotionBroadcastIntervalMinutes;
    }

    public boolean isEnableBackToMainMenu() {
        return enableBackToMainMenu;
    }

    public String getMainMenuCommand() {
        return mainMenuCommand;
    }
}
