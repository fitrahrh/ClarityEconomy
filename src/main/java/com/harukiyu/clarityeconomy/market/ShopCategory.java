package com.harukiyu.clarityeconomy.market;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShopCategory {

    private final String id;
    private final String name;
    private final Material icon;
    private final String description;
    private final Map<String, ShopItem> items;
    private final String iconString;

    public ShopCategory(@NotNull String id,
                        @NotNull String name,
                        @NotNull Material icon,
                        @Nullable String description,
                        @NotNull Map<String, ShopItem> items) {
        this(id, name, icon, description, items, null);
    }

    public ShopCategory(@NotNull String id,
                        @NotNull String name,
                        @NotNull Material icon,
                        @Nullable String description,
                        @NotNull Map<String, ShopItem> items,
                        @Nullable String customIcon) {
        this.id = id.toLowerCase(Locale.ROOT);
        this.name = name;
        this.icon = icon;
        this.description = description != null ? description : "";
        this.items = new LinkedHashMap<>(items);
        this.iconString = (customIcon != null && !customIcon.isBlank()) ? customIcon : com.harukiyu.clarityeconomy.utils.Utils.getMaterialIcon(icon);
    }

    @NotNull
    public String getIconString() {
        return iconString;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Material getIcon() {
        return icon;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public Collection<ShopItem> getItems() {
        return items.values();
    }

    @Nullable
    public ShopItem getItem(@NotNull String itemId) {
        return items.get(itemId.toLowerCase(Locale.ROOT));
    }

    public boolean hasItem(@NotNull String itemId) {
        return items.containsKey(itemId.toLowerCase(Locale.ROOT));
    }
}
