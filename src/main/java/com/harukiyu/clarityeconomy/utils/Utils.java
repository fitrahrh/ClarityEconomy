package com.harukiyu.clarityeconomy.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("null")
public final class Utils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private Utils() {}

    /**
     * Forcefully closes any open Paper Dialog and any open Inventory.
     */
    public static void forceCloseScreen(@NotNull Player player) {
        try {
            player.closeDialog();
        } catch (Throwable ignored) {}
        try {
            player.closeInventory();
        } catch (Throwable ignored) {}
    }

    /**
     * Safely executes a command on behalf of the player.
     */
    public static void executeSafeCommand(@NotNull Player player, @NotNull String command) {
        try {
            player.performCommand(command);
        } catch (CommandException | SecurityException ignored) {}
    }

    /**
     * Deserializes MiniMessage or legacy color codes into Adventure Component.
     */
    @NotNull
    public static Component parseComponent(@NotNull String text) {
        if (text.contains("&") || text.contains("§")) {
            return LEGACY.deserialize(text);
        }
        return MINI_MESSAGE.deserialize(text);
    }

    /**
     * Strips color codes and formatting for plain text.
     */
    @NotNull
    public static String toPlainText(@NotNull Component component) {
        return PLAIN.serialize(component);
    }

    /**
     * Formats item name in a human readable style (e.g. DIAMOND_SWORD -> Diamond Sword).
     */
    @NotNull
    public static String formatMaterialName(@NotNull Material material) {
        String name = material.name().replace('_', ' ').toLowerCase();
        StringBuilder sb = new StringBuilder();
        boolean capitalize = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                capitalize = true;
                sb.append(' ');
            } else if (capitalize) {
                sb.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Gets a display name for an item stack, falling back to material name.
     */
    @NotNull
    public static String getItemDisplayName(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "Air";
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            Component displayName = meta.displayName();
            if (displayName != null) {
                return PLAIN.serialize(displayName);
            }
        }
        return formatMaterialName(item.getType());
    }

    /**
     * Calculates the total quantity of a specific material in the player's inventory.
     */
    public static int countItem(@NotNull Player player, @NotNull Material material) {
        int count = 0;
        ItemStack[] contents = player.getInventory().getStorageContents();
        if (contents == null) {
            return 0;
        }
        for (ItemStack is : contents) {
            if (is != null && is.getType() == material) {
                count += is.getAmount();
            }
        }
        return count;
    }

    /**
     * Removes a specified quantity of a material from player's inventory.
     * Returns true if the full amount was removed, false otherwise.
     */
    public static boolean removeItem(@NotNull Player player, @NotNull Material material, int amount) {
        if (countItem(player, material) < amount) {
            return false;
        }
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        if (contents == null) {
            return false;
        }
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is != null && is.getType() == material) {
                int stackAmount = is.getAmount();
                if (stackAmount <= remaining) {
                    player.getInventory().setItem(i, null);
                    remaining -= stackAmount;
                } else {
                    is.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
        player.updateInventory();
        return true;
    }


    /**
     * Adds items to player inventory, dropping any overflow at the player's feet.
     */
    public static void giveItemOrDrop(@NotNull Player player, @NotNull ItemStack item) {
        var leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                if (drop != null && !drop.getType().isAir()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }
    }

    /**
     * Resolves an aesthetic icon or symbol for a given Material for dialog menu buttons.
     */
    @NotNull
    public static String getMaterialIcon(@NotNull Material material) {
        String name = material.name();
        if (name.contains("PICKAXE")) return "⛏️";
        if (name.contains("AXE")) return "🪓";
        if (name.contains("SWORD")) return "🗡️";
        if (name.contains("SHOVEL")) return "⛏️";
        if (name.contains("HOE")) return "🌾";
        if (name.contains("BOW") || name.contains("ARROW")) return "🏹";
        if (name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") || name.contains("BOOTS") || name.contains("SHIELD")) return "🛡️";
        if (name.contains("DIAMOND")) return "💎";
        if (name.contains("EMERALD")) return "❇️";
        if (name.contains("GOLD") || name.contains("RAW_GOLD")) return "🪙";
        if (name.contains("IRON") || name.contains("RAW_IRON")) return "⚙️";
        if (name.contains("COPPER")) return "🧱";
        if (name.contains("COAL") || name.contains("CHARCOAL")) return "⬛";
        if (name.contains("NETHERITE") || name.contains("ANCIENT_DEBRIS")) return "🛡️";
        if (name.contains("REDSTONE")) return "🔴";
        if (name.contains("LAPIS")) return "🔷";
        if (name.contains("AMETHYST") || name.contains("QUARTZ")) return "🔮";
        if (name.contains("WHEAT") || name.contains("SEED") || name.contains("CARROT") || name.contains("POTATO") || name.contains("BEETROOT")) return "🌾";
        if (name.contains("APPLE")) return name.contains("ENCHANTED") || name.contains("GOLDEN") ? "🍏" : "🍎";
        if (name.contains("BREAD") || name.contains("COOKIE") || name.contains("CAKE") || name.contains("PIE")) return "🍞";
        if (name.contains("BEEF") || name.contains("PORKCHOP") || name.contains("MUTTON") || name.contains("CHICKEN") || name.contains("MEAT")) return "🥩";
        if (name.contains("FISH") || name.contains("SALMON") || name.contains("COD")) return "🐟";
        if (name.contains("LOG") || name.contains("WOOD") || name.contains("PLANKS")) return "🪵";
        if (name.contains("BRICK") || name.contains("STONE") || name.contains("DEEPSLATE") || name.contains("COBBLE")) return "🧱";
        if (name.contains("GLASS")) return "🪟";
        if (name.contains("SAND") || name.contains("GRAVEL") || name.contains("DIRT")) return "📦";
        if (name.contains("BONE")) return "🦴";
        if (name.contains("GUNPOWDER") || name.contains("TNT")) return "🧨";
        if (name.contains("STRING") || name.contains("COBWEB")) return "🕸️";
        if (name.contains("ENDER") || name.contains("EYE")) return "🔮";
        if (name.contains("ROTTEN_FLESH") || name.contains("SKULL") || name.contains("HEAD")) return "💀";
        if (name.contains("BLAZE")) return "🔥";
        if (name.contains("POTION") || name.contains("BREWING") || name.contains("BOTTLE")) return "🧪";
        if (name.contains("BOOK") || name.contains("PAPER")) return "📖";
        if (name.contains("TOTEM")) return "🗿";
        if (name.contains("BEACON") || name.contains("STAR")) return "🌟";
        if (name.contains("TRIDENT")) return "🔱";
        if (name.contains("ELYTRA") || name.contains("FEATHER")) return "🪶";
        if (name.contains("CHEST") || name.contains("SHULKER")) return "📦";
        return "📦";
    }
}
