package com.harukiyu.clarityeconomy.market;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShopItem {

    private final String id;
    private final Material material;
    private final String name;
    private final double buyPrice;
    private final double sellPrice;
    private final String description;
    private final String customIcon;

    public ShopItem(@NotNull String id,
                    @NotNull Material material,
                    @Nullable String name,
                    double buyPrice,
                    double sellPrice,
                    @Nullable String description) {
        this(id, material, name, buyPrice, sellPrice, description, null);
    }

    public ShopItem(@NotNull String id,
                    @NotNull Material material,
                    @Nullable String name,
                    double buyPrice,
                    double sellPrice,
                    @Nullable String description,
                    @Nullable String customIcon) {
        this.id = id;
        this.material = material;
        this.name = name;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.description = description;
        this.customIcon = (customIcon != null && !customIcon.isBlank()) ? customIcon : com.harukiyu.clarityeconomy.utils.Utils.getMaterialIcon(material);
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public boolean canBuy() {
        return buyPrice > 0;
    }

    public boolean canSell() {
        return sellPrice > 0;
    }

    @NotNull
    public String getIcon() {
        return customIcon;
    }
}
