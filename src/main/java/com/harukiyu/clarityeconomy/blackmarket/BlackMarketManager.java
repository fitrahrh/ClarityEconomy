package com.harukiyu.clarityeconomy.blackmarket;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.config.ConfigManager;
import com.harukiyu.clarityeconomy.market.ShopItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class BlackMarketManager {

    private final ClarityEconomy plugin;
    private final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> purchaseCooldowns = new ConcurrentHashMap<>();

    private final Map<String, ShopItem> blackMarketItems = new LinkedHashMap<>();
    private static final ZoneId TIMEZONE_WIB = ZoneId.of("Asia/Jakarta");

    // Daily active window (evaluated dynamically)
    private int currentDayOfYear = -1;
    private int windowStartHour = 14;
    private int windowStartMinute = 0;

    public BlackMarketManager(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    public void load() {
        blackMarketItems.clear();
        File shopFolder = new File(plugin.getDataFolder(), "shops");
        File bmFile = new File(shopFolder, "black_market.yml");
        if (!bmFile.exists()) {
            try {
                plugin.saveResource("shops/black_market.yml", false);
            } catch (Throwable ignored) {}
        }

        if (bmFile.exists()) {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(bmFile);
                ConfigurationSection itemSection = yaml.getConfigurationSection("items");
                if (itemSection != null) {
                    for (String key : itemSection.getKeys(false)) {
                        ConfigurationSection is = itemSection.getConfigurationSection(key);
                        if (is != null) {
                            String rawMat = is.getString("material");
                            String matName = rawMat != null ? rawMat : key.toUpperCase(Locale.ROOT);
                            Material mat = Material.matchMaterial(matName);
                            if (mat == null) continue;

                            String customName = is.getString("name");
                            double buy = is.getDouble("buy_price", 0.0);
                            double sell = is.getDouble("sell_price", 0.0);
                            String desc = is.getString("description");

                            ShopItem shopItem = new ShopItem(key, mat, customName, buy, sell, desc);
                            blackMarketItems.put(key.toLowerCase(Locale.ROOT), shopItem);
                        }
                    }
                }
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Failed to load black_market.yml: " + t.getMessage(), t);
            }
        }

        // If black_market.yml was empty or missing items, add default illegal rare items
        if (blackMarketItems.isEmpty()) {
            populateDefaultBlackMarketItems();
        }

        updateDailySchedule();
    }

    private void populateDefaultBlackMarketItems() {
        blackMarketItems.put("netherite_ingot", new ShopItem("netherite_ingot", Material.NETHERITE_INGOT, "Netherite Ingot", 2500.0, 1000.0, "Rare illegal black market metal"));
        blackMarketItems.put("enchanted_golden_apple", new ShopItem("enchanted_golden_apple", Material.ENCHANTED_GOLDEN_APPLE, "Enchanted Golden Apple", 5000.0, 2000.0, "Forbidden relic apple"));
        blackMarketItems.put("totem_of_undying", new ShopItem("totem_of_undying", Material.TOTEM_OF_UNDYING, "Totem of Undying", 1500.0, 500.0, "Second chance at life"));
        blackMarketItems.put("elytra", new ShopItem("elytra", Material.ELYTRA, "Smuggled Elytra", 10000.0, 4000.0, "Smuggled wings from the End"));
        blackMarketItems.put("beacon", new ShopItem("beacon", Material.BEACON, "Illegal Beacon", 7500.0, 3000.0, "Contraband beacon core"));
        blackMarketItems.put("trident", new ShopItem("trident", Material.TRIDENT, "Ancient Trident", 4000.0, 1500.0, "Relic from the deep sea"));
        blackMarketItems.put("nether_star", new ShopItem("nether_star", Material.NETHER_STAR, "Nether Star", 6000.0, 2500.0, "Smuggled wither core"));
    }

    /**
     * Determines the operational schedule for today (between 06:00 and 21:00 WIB).
     */
    public void updateDailySchedule() {
        ZonedDateTime now = ZonedDateTime.now(TIMEZONE_WIB);
        int day = now.getDayOfYear();
        if (day != currentDayOfYear) {
            currentDayOfYear = day;
            ConfigManager cfg = plugin.getConfigManager();
            int startBound = cfg.getBlackMarketWindowStart(); // e.g. 6
            int endBound = cfg.getBlackMarketWindowEnd();     // e.g. 21
            int duration = cfg.getBlackMarketDurationHours();  // e.g. 2

            // Maximum start hour such that start + duration <= endBound
            int maxStartHour = Math.max(startBound, endBound - duration);
            int range = Math.max(1, (maxStartHour - startBound) + 1);

            // Deterministic pseudorandom for the day
            Random rng = new Random((long) now.getYear() * 1000L + day);
            this.windowStartHour = startBound + rng.nextInt(range);
            this.windowStartMinute = rng.nextInt(2) * 30; // 0 or 30 min
        }
    }

    public boolean isOpen() {
        updateDailySchedule();
        ZonedDateTime now = ZonedDateTime.now(TIMEZONE_WIB);
        ZonedDateTime start = now.withHour(windowStartHour).withMinute(windowStartMinute).withSecond(0).withNano(0);
        ZonedDateTime end = start.plusHours(plugin.getConfigManager().getBlackMarketDurationHours());

        return (now.isEqual(start) || now.isAfter(start)) && now.isBefore(end);
    }

    @NotNull
    public String getTimeRemainingFormatted() {
        updateDailySchedule();
        ZonedDateTime now = ZonedDateTime.now(TIMEZONE_WIB);
        ZonedDateTime start = now.withHour(windowStartHour).withMinute(windowStartMinute).withSecond(0).withNano(0);
        ZonedDateTime end = start.plusHours(plugin.getConfigManager().getBlackMarketDurationHours());

        if (isOpen()) {
            Duration rem = Duration.between(now, end);
            long h = rem.toHours();
            long m = rem.toMinutesPart();
            long s = rem.toSecondsPart();
            return String.format("%02d:%02d:%02d", h, m, s);
        } else {
            // Next opening calculation
            ZonedDateTime nextStart = start;
            if (now.isAfter(end) || now.isEqual(end)) {
                nextStart = start.plusDays(1);
            }
            Duration until = Duration.between(now, nextStart);
            long h = until.toHours();
            long m = until.toMinutesPart();
            return String.format("%dh %dm", h, m);
        }
    }

    @NotNull
    public String getScheduleDisplay() {
        return String.format("%02d:%02d - %02d:%02d WIB",
                windowStartHour, windowStartMinute,
                windowStartHour + plugin.getConfigManager().getBlackMarketDurationHours(), windowStartMinute);
    }

    @NotNull
    public Collection<ShopItem> getItems() {
        return blackMarketItems.values();
    }

    @Nullable
    public ShopItem getItem(@NotNull String id) {
        return blackMarketItems.get(id.toLowerCase(Locale.ROOT));
    }

    public double getDiscountedPrice(double originalPrice) {
        double mult = plugin.getConfigManager().getBlackMarketPriceDiscountMultiplier();
        return Math.max(1.0, Math.round(originalPrice * mult * 100.0) / 100.0);
    }

    public boolean isFrozen(@NotNull Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }

    public void setFrozen(@NotNull Player player, boolean freeze) {
        if (!plugin.getConfigManager().isBlackMarketFreezePlayer()) return;
        if (freeze) {
            frozenPlayers.add(player.getUniqueId());
        } else {
            frozenPlayers.remove(player.getUniqueId());
        }
    }

    public boolean checkCooldown(@NotNull Player player) {
        long cooldownMs = plugin.getConfigManager().getBlackMarketCooldownSeconds() * 1000L;
        Long lastTime = purchaseCooldowns.get(player.getUniqueId());
        if (lastTime == null) return true;

        long diff = System.currentTimeMillis() - lastTime;
        return diff >= cooldownMs;
    }

    public long getRemainingCooldownSeconds(@NotNull Player player) {
        long cooldownMs = plugin.getConfigManager().getBlackMarketCooldownSeconds() * 1000L;
        Long lastTime = purchaseCooldowns.get(player.getUniqueId());
        if (lastTime == null) return 0;
        long diff = System.currentTimeMillis() - lastTime;
        if (diff >= cooldownMs) return 0;
        return (cooldownMs - diff) / 1000L;
    }

    public void applyCooldown(@NotNull Player player) {
        purchaseCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void clearPlayerData(@NotNull UUID uuid) {
        frozenPlayers.remove(uuid);
        purchaseCooldowns.remove(uuid);
    }
}
