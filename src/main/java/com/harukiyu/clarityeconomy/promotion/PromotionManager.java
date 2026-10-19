package com.harukiyu.clarityeconomy.promotion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.api.event.PromotionBroadcastEvent;
import com.harukiyu.clarityeconomy.config.ConfigManager;
import com.harukiyu.clarityeconomy.hook.LanguageHook;
import com.harukiyu.clarityeconomy.hook.VaultHook;
import com.harukiyu.clarityeconomy.utils.Utils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PromotionManager {

    public enum QuickType {
        WTB("WTB", "looking for", "wtb.broadcast"),
        WTS("WTS", "selling", "wts.broadcast"),
        WTA("WTA", "asking about", "wta.broadcast");

        private final String tag;
        private final String action;
        private final String key;

        QuickType(String tag, String action, String key) {
            this.tag = tag;
            this.action = action;
            this.key = key;
        }

        public String getTag() {
            return tag;
        }

        public String getAction() {
            return action;
        }

        public String getKey() {
            return key;
        }
    }

    private final ClarityEconomy plugin;
    private final List<PromotionItem> activePromotions = new CopyOnWriteArrayList<>();
    private final Map<UUID, Long> playerCooldowns = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private int broadcastIndex = 0;
    private ScheduledTask broadcastTask;

    public PromotionManager(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    public void start() {
        loadData();
        startBroadcastTask();
    }

    public void stop() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
        saveData();
    }

    public void reload() {
        stop();
        start();
    }

    private void startBroadcastTask() {
        int intervalMinutes = Math.max(1, plugin.getConfigManager().getPromotionBroadcastIntervalMinutes());
        this.broadcastTask = Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                task -> broadcastNextPromotion(),
                intervalMinutes,
                intervalMinutes,
                TimeUnit.MINUTES
        );
    }

    public int getActivePromotionsCount() {
        cleanExpired();
        return activePromotions.size();
    }

    public boolean canSubmitPromotion(@NotNull Player player) {
        cleanExpired();
        if (activePromotions.size() >= plugin.getConfigManager().getPromotionMaxSlots()) {
            return false;
        }
        Long cd = playerCooldowns.get(player.getUniqueId());
        if (cd == null) return true;
        return System.currentTimeMillis() >= cd;
    }

    public long getRemainingCooldownSeconds(@NotNull Player player) {
        Long cd = playerCooldowns.get(player.getUniqueId());
        if (cd == null) return 0;
        long diff = cd - System.currentTimeMillis();
        return diff > 0 ? diff / 1000L : 0;
    }

    public boolean submitPromotion(@NotNull Player player, @NotNull String message) {
        cleanExpired();
        ConfigManager cfg = plugin.getConfigManager();

        if (activePromotions.size() >= cfg.getPromotionMaxSlots()) {
            LanguageHook.sendMessage(player, "promosi.queue_full",
                    Placeholder.unparsed("max", String.valueOf(cfg.getPromotionMaxSlots())));
            return false;
        }

        if (!canSubmitPromotion(player)) {
            long remSec = getRemainingCooldownSeconds(player);
            long h = remSec / 3600;
            long m = (remSec % 3600) / 60;
            LanguageHook.sendMessage(player, "promosi.cooldown",
                    Placeholder.unparsed("time", String.format("%dh %dm", h, m)));
            return false;
        }

        double cost = cfg.getPromotionCost();
        if (!VaultHook.hasEnough(player, cost)) {
            LanguageHook.sendMessage(player, "promosi.not_enough_money",
                    Placeholder.unparsed("cost", VaultHook.formatMoney(cost)),
                    Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));
            return false;
        }

        PromotionBroadcastEvent event = new PromotionBroadcastEvent(player, message, true);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        if (!VaultHook.withdraw(player, cost)) {
            LanguageHook.sendMessage(player, "market.transaction_failed");
            return false;
        }

        long now = System.currentTimeMillis();
        long durationMs = cfg.getPromotionDurationMinutes() * 60_000L;
        long cooldownMs = cfg.getPromotionCooldownHours() * 3600_000L;

        PromotionItem item = new PromotionItem(player.getUniqueId(), player.getName(), event.getMessage(), now, now + durationMs);
        activePromotions.add(item);
        playerCooldowns.put(player.getUniqueId(), now + cooldownMs);

        saveData();

        LanguageHook.sendMessage(player, "promosi.success",
                Placeholder.unparsed("cost", VaultHook.formatMoney(cost)),
                Placeholder.unparsed("duration", String.valueOf(cfg.getPromotionDurationMinutes())),
                Placeholder.unparsed("cooldown", String.valueOf(cfg.getPromotionCooldownHours())));

        return true;
    }

    private void cleanExpired() {
        activePromotions.removeIf(PromotionItem::isExpired);
    }

    private void broadcastNextPromotion() {
        cleanExpired();
        if (activePromotions.isEmpty()) return;

        if (broadcastIndex >= activePromotions.size()) {
            broadcastIndex = 0;
        }

        PromotionItem promo = activePromotions.get(broadcastIndex);
        broadcastIndex = (broadcastIndex + 1) % activePromotions.size();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == null || !p.isOnline()) continue;
            LanguageHook.sendMessage(p, "promosi.broadcast_format",
                    Placeholder.unparsed("player", promo.getPlayerName()),
                    Placeholder.unparsed("message", promo.getMessage()));
            try {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.7f, 1.2f);
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Quick broadcast for /wtb, /wts, /wta commands.
     */
    public void broadcastQuick(@NotNull Player player, @NotNull QuickType type, @Nullable String note) {
        ItemStack held = player.getInventory().getItemInMainHand();
        String itemInfo;
        if (!held.getType().isAir()) {
            itemInfo = Utils.getItemDisplayName(held) + " (x" + held.getAmount() + ")";
        } else {
            itemInfo = note != null && !note.isBlank() ? note : "Unspecified Item";
        }

        if (note != null && !note.isBlank() && !held.getType().isAir()) {
            itemInfo += " - " + note;
        }

        PromotionBroadcastEvent event = new PromotionBroadcastEvent(player, itemInfo, false);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == null || !p.isOnline()) continue;
            LanguageHook.sendMessage(p, type.getKey(),
                    Placeholder.unparsed("player", player.getName()),
                    Placeholder.unparsed("tag", type.getTag()),
                    Placeholder.unparsed("action", type.getAction()),
                    Placeholder.unparsed("item", event.getMessage()));
            try {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.5f);
            } catch (Throwable ignored) {}
        }
    }

    private void loadData() {
        activePromotions.clear();
        playerCooldowns.clear();
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File file = new File(dataFolder, "promotions.json");
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<PromotionDataHolder>() {}.getType();
            PromotionDataHolder holder = gson.fromJson(reader, type);
            if (holder != null) {
                if (holder.promotions != null) {
                    for (PromotionItem item : holder.promotions) {
                        if (!item.isExpired()) {
                            activePromotions.add(item);
                        }
                    }
                }
                if (holder.cooldowns != null) {
                    playerCooldowns.putAll(holder.cooldowns);
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Failed to load promotions.json: " + t.getMessage(), t);
        }
    }

    private void saveData() {
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File file = new File(dataFolder, "promotions.json");
        PromotionDataHolder holder = new PromotionDataHolder();
        holder.promotions = new ArrayList<>(activePromotions);
        holder.cooldowns = new HashMap<>(playerCooldowns);

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(holder, writer);
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Failed to save promotions.json: " + t.getMessage(), t);
        }
    }

    private static class PromotionDataHolder {
        List<PromotionItem> promotions;
        Map<UUID, Long> cooldowns;
    }
}
