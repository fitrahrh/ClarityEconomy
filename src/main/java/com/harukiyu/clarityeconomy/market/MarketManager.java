package com.harukiyu.clarityeconomy.market;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.api.event.PlayerBuyItemEvent;
import com.harukiyu.clarityeconomy.api.event.PlayerSellItemEvent;
import com.harukiyu.clarityeconomy.hook.LanguageHook;
import com.harukiyu.clarityeconomy.hook.VaultHook;
import com.harukiyu.clarityeconomy.utils.Utils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class MarketManager {

    private final ClarityEconomy plugin;
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();

    private static final String[] DEFAULT_SHOPS = {
            "farming.yml", "mining.yml", "building.yml", "combat.yml", "mob_drops.yml", "_template.yml"
    };

    public MarketManager(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    public void loadShops() {
        categories.clear();
        File shopFolder = new File(plugin.getDataFolder(), "shops");
        if (!shopFolder.exists()) {
            shopFolder.mkdirs();
        }

        // Save default resources if not present
        for (String shopFile : DEFAULT_SHOPS) {
            File target = new File(shopFolder, shopFile);
            if (!target.exists()) {
                try {
                    plugin.saveResource("shops/" + shopFile, false);
                } catch (Throwable ignored) {}
            }
        }

        File[] files = shopFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.startsWith("_"));
        if (files == null) return;

        for (File file : files) {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                String id = file.getName().substring(0, file.getName().length() - 4).toLowerCase(Locale.ROOT);
                String name = yaml.getString("name", Utils.formatMaterialName(Material.CHEST));
                String iconName = yaml.getString("icon", "CHEST");
                Material icon = Material.matchMaterial(iconName != null ? iconName : "CHEST");
                if (icon == null) icon = Material.CHEST;
                String description = yaml.getString("description", "");
                String customCatIcon = yaml.getString("custom_icon");

                Map<String, ShopItem> items = new LinkedHashMap<>();
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
                            String customItemIcon = is.getString("icon");

                            ShopItem shopItem = new ShopItem(key, mat, customName, buy, sell, desc, customItemIcon);
                            items.put(key.toLowerCase(Locale.ROOT), shopItem);
                        }
                    }
                }

                ShopCategory category = new ShopCategory(id, name, icon, description, items, customCatIcon);
                categories.put(id, category);
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Failed to load shop category from " + file.getName() + ": " + t.getMessage(), t);
            }
        }

        plugin.getLogger().log(Level.INFO, "Loaded {0} market shop categories.", categories.size());
    }


    @NotNull
    public Collection<ShopCategory> getCategories() {
        return categories.values();
    }

    @Nullable
    public ShopCategory getCategory(@NotNull String id) {
        return categories.get(id.toLowerCase(Locale.ROOT));
    }

    /**
     * Executes the purchase of items.
     */
    public boolean processBuy(@NotNull Player player, @NotNull ShopItem item, int amount, boolean isBlackMarket, double unitPrice) {
        if (amount <= 0 || unitPrice <= 0) {
            LanguageHook.sendMessage(player, "market.invalid_quantity");
            return false;
        }

        double totalCost = unitPrice * amount;

        if (!VaultHook.hasEconomy()) {
            LanguageHook.sendMessage(player, "market.economy_unavailable");
            return false;
        }

        if (!VaultHook.hasEnough(player, totalCost)) {
            LanguageHook.sendMessage(player, "market.not_enough_money",
                    Placeholder.unparsed("cost", VaultHook.formatMoney(totalCost)),
                    Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));
            return false;
        }

        PlayerBuyItemEvent event = new PlayerBuyItemEvent(player, item.getMaterial(), amount, totalCost, isBlackMarket);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        if (!VaultHook.withdraw(player, event.getTotalPrice())) {
            LanguageHook.sendMessage(player, "market.transaction_failed");
            return false;
        }

        ItemStack stack = new ItemStack(item.getMaterial(), event.getAmount());
        Utils.giveItemOrDrop(player, stack);

        String itemName = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
        LanguageHook.sendMessage(player, "market.buy_success",
                Placeholder.unparsed("amount", String.valueOf(event.getAmount())),
                Placeholder.unparsed("item", itemName),
                Placeholder.unparsed("cost", VaultHook.formatMoney(event.getTotalPrice())),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        return true;
    }

    /**
     * Executes the sale of items from the player's inventory.
     */
    public boolean processSell(@NotNull Player player, @NotNull ShopItem item, int amount, double unitPrice) {
        if (amount <= 0 || unitPrice <= 0) {
            LanguageHook.sendMessage(player, "market.invalid_quantity");
            return false;
        }

        int inventoryCount = Utils.countItem(player, item.getMaterial());
        if (inventoryCount < amount) {
            LanguageHook.sendMessage(player, "market.not_enough_items",
                    Placeholder.unparsed("required", String.valueOf(amount)),
                    Placeholder.unparsed("have", String.valueOf(inventoryCount)));
            return false;
        }

        double totalPayout = unitPrice * amount;

        if (!VaultHook.hasEconomy()) {
            LanguageHook.sendMessage(player, "market.economy_unavailable");
            return false;
        }

        PlayerSellItemEvent event = new PlayerSellItemEvent(player, item.getMaterial(), amount, totalPayout);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        if (!Utils.removeItem(player, item.getMaterial(), event.getAmount())) {
            LanguageHook.sendMessage(player, "market.transaction_failed");
            return false;
        }

        VaultHook.deposit(player, event.getTotalPayout());

        String itemName = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
        LanguageHook.sendMessage(player, "market.sell_success",
                Placeholder.unparsed("amount", String.valueOf(event.getAmount())),
                Placeholder.unparsed("item", itemName),
                Placeholder.unparsed("payout", VaultHook.formatMoney(event.getTotalPayout())),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        return true;
    }
}
