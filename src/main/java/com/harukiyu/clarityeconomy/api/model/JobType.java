package com.harukiyu.clarityeconomy.api.model;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public enum JobType {
    HUNTER(
            "Hunter",
            Material.BOW,
            "job.hunter.name",
            "job.hunter.description",
            "Strength II",
            "Strength I"
    ),
    FARMER(
            "Farmer",
            Material.GOLDEN_HOE,
            "job.farmer.name",
            "job.farmer.description",
            "Double Crop Drops",
            "30% Extra Crop Chance"
    ),
    MINER(
            "Miner",
            Material.DIAMOND_PICKAXE,
            "job.miner.name",
            "job.miner.description",
            "Haste II & Night Vision",
            "Haste I & Night Vision"
    ),
    FISHERMAN(
            "Fisherman",
            Material.FISHING_ROD,
            "job.fisherman.name",
            "job.fisherman.description",
            "Luck III",
            "Luck I"
    ),
    EXPLORER(
            "Explorer",
            Material.COMPASS,
            "job.explorer.name",
            "job.explorer.description",
            "Speed II",
            "Speed I"
    ),
    BREEDER(
            "Breeder",
            Material.LEAD,
            "job.breeder.name",
            "job.breeder.description",
            "+25% PvE Damage",
            "+10% PvE Damage"
    ),
    BLACKSMITH(
            "Blacksmith",
            Material.ANVIL,
            "job.blacksmith.name",
            "job.blacksmith.description",
            "+15% Weapon Damage & Resistance I",
            "Resistance I"
    ),
    ALCHEMIST(
            "Alchemist",
            Material.BREWING_STAND,
            "job.alchemist.name",
            "job.alchemist.description",
            "Regeneration I",
            "Minor Regeneration I"
    ),
    LUMBERJACK(
            "Lumberjack",
            Material.DIAMOND_AXE,
            "job.lumberjack.name",
            "job.lumberjack.description",
            "Haste I & Strength I",
            "Haste I"
    ),
    DIGGER(
            "Digger",
            Material.DIAMOND_SHOVEL,
            "job.digger.name",
            "job.digger.description",
            "Haste I & Speed I",
            "Haste I"
    );

    private final String displayName;
    private final Material icon;
    private final String nameKey;
    private final String descriptionKey;
    private final String selfBuffDesc;
    private final String auraBuffDesc;

    JobType(@NotNull String displayName,
            @NotNull Material icon,
            @NotNull String nameKey,
            @NotNull String descriptionKey,
            @NotNull String selfBuffDesc,
            @NotNull String auraBuffDesc) {
        this.displayName = displayName;
        this.icon = icon;
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.selfBuffDesc = selfBuffDesc;
        this.auraBuffDesc = auraBuffDesc;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public Material getIcon() {
        return icon;
    }

    @NotNull
    public String getNameKey() {
        return nameKey;
    }

    @NotNull
    public String getDescriptionKey() {
        return descriptionKey;
    }

    @NotNull
    public String getSelfBuffDesc() {
        return selfBuffDesc;
    }

    @NotNull
    public String getAuraBuffDesc() {
        return auraBuffDesc;
    }
}
