package com.harukiyu.clarityeconomy.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Resolves vanilla Minecraft item and block textures directly to Adventure Sprite ObjectComponents.
 * This natively renders real vanilla 16x16 Minecraft item/block textures in text components
 * and Paper Dialog button labels without needing external custom resource pack setups.
 */
public final class SpriteResolver {

    private SpriteResolver() {}

    private static final Map<Material, Component> SPRITE_CACHE = new EnumMap<>(Material.class);

    private static final Key ATLAS_BLOCKS = Key.key("minecraft", "blocks");
    private static final Key ATLAS_ITEMS = Key.key("minecraft", "items");

    public record TargetSprite(@NotNull Key atlas, @NotNull Key sprite) {}

    private static final Map<String, TargetSprite> SPRITE_OVERRIDES = Map.ofEntries(
            Map.entry("SNOW_BLOCK", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/snow"))),
            Map.entry("SNOW", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/snow"))),
            Map.entry("SMOOTH_SANDSTONE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/sandstone_top"))),
            Map.entry("SMOOTH_RED_SANDSTONE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/red_sandstone_top"))),
            Map.entry("SMOOTH_BASALT", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/smooth_basalt_side"))),
            Map.entry("SMOOTH_STONE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/smooth_stone"))),
            Map.entry("SMOOTH_QUARTZ", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/smooth_quartz"))),
            Map.entry("QUARTZ_BLOCK", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/quartz_block_side"))),
            Map.entry("CHISELED_QUARTZ_BLOCK", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/chiseled_quartz_block"))),
            Map.entry("QUARTZ_PILLAR", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/quartz_pillar"))),
            Map.entry("QUARTZ_BRICKS", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/quartz_bricks"))),
            Map.entry("GLASS_PANE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/glass"))),
            Map.entry("WHEAT", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/wheat"))),
            Map.entry("WHEAT_SEEDS", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/wheat_seeds"))),
            Map.entry("STONECUTTER", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/stonecutter_side"))),
            Map.entry("POINTED_DRIPSTONE", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/pointed_dripstone"))),
            Map.entry("SUNFLOWER", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/sunflower"))),
            Map.entry("PITCHER_POD", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/pitcher_pod"))),
            Map.entry("PITCHER_PLANT", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/pitcher_plant"))),
            Map.entry("RESIN_CLUMP", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/resin_clump"))),
            Map.entry("RESIN_BRICKS", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/resin_bricks"))),
            Map.entry("RESIN_BRICK_WALL", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/resin_bricks"))),
            Map.entry("LEVER", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/lever"))),
            Map.entry("ENCHANTED_GOLDEN_APPLE", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/enchanted_golden_apple"))),
            Map.entry("GOLDEN_APPLE", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/golden_apple"))),
            Map.entry("LIGHTNING_ROD", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/lightning_rod"))),
            Map.entry("LECTERN", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/lectern_top"))),
            Map.entry("NETHER_WART", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/nether_wart"))),
            Map.entry("NETHER_WART_BLOCK", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/nether_wart_block"))),
            Map.entry("CHEST", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/chest_minecart"))),
            Map.entry("TRAPPED_CHEST", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/chest_minecart"))),
            Map.entry("ENDER_CHEST", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/obsidian"))),
            Map.entry("RECOVERY_COMPASS", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/recovery_compass_00"))),
            Map.entry("CLOCK", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/clock_00"))),
            Map.entry("COMPASS", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/compass_00"))),
            Map.entry("BARREL", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/barrel_side"))),
            Map.entry("COMPOSTER", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/composter_side"))),
            Map.entry("SMITHING_TABLE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/smithing_table_front"))),
            Map.entry("FLETCHING_TABLE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/fletching_table_front"))),
            Map.entry("BARRIER", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/barrier"))),
            Map.entry("SPYGLASS", new TargetSprite(ATLAS_ITEMS, Key.key("minecraft", "item/spyglass"))),
            Map.entry("COBWEB", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/cobweb"))),
            Map.entry("GRASS_BLOCK", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/grass_block_side"))),
            Map.entry("ANCIENT_DEBRIS", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/ancient_debris_side"))),
            Map.entry("AZALEA", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/azalea_side"))),
            Map.entry("FLOWERING_AZALEA", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/flowering_azalea_side"))),
            Map.entry("BASALT", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/basalt_side"))),
            Map.entry("POLISHED_BASALT", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/polished_basalt_side"))),
            Map.entry("BONE_BLOCK", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/bone_block_side"))),
            Map.entry("DRIED_KELP_BLOCK", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/dried_kelp_side"))),
            Map.entry("MYCELIUM", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/mycelium_side"))),
            Map.entry("PODZOL", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/podzol_side"))),
            Map.entry("DIRT_PATH", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/dirt_path_side"))),
            Map.entry("FURNACE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/furnace_front"))),
            Map.entry("CRAFTING_TABLE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/crafting_table_front"))),
            Map.entry("TNT", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/tnt_side"))),
            Map.entry("CACTUS", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/cactus_side"))),
            Map.entry("PUMPKIN", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/pumpkin_side"))),
            Map.entry("JACK_O_LANTERN", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/jack_o_lantern"))),
            Map.entry("BEACON", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/beacon"))),
            Map.entry("DISPENSER", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/dispenser_front"))),
            Map.entry("DROPPER", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/dropper_front"))),
            Map.entry("OBSERVER", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/observer_front"))),
            Map.entry("PISTON", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/piston_top"))),
            Map.entry("STICKY_PISTON", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/piston_top_sticky"))),
            Map.entry("SMOKER", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/smoker_front"))),
            Map.entry("BLAST_FURNACE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/blast_furnace_front"))),
            Map.entry("ENCHANTING_TABLE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/enchanting_table_top"))),
            Map.entry("BOOKSHELF", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/bookshelf"))),
            Map.entry("CHISELED_BOOKSHELF", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/chiseled_bookshelf_empty"))),
            Map.entry("JUKEBOX", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/jukebox_top"))),
            Map.entry("NOTE_BLOCK", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/note_block"))),
            Map.entry("DAYLIGHT_DETECTOR", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/daylight_detector_top"))),
            Map.entry("TARGET", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/target_side"))),
            Map.entry("HAY_BLOCK", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/hay_block_side"))),
            Map.entry("BEE_NEST", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/bee_nest_side"))),
            Map.entry("BEEHIVE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/beehive_side"))),
            Map.entry("LODESTONE", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/lodestone_side"))),
            Map.entry("RESPAWN_ANCHOR", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/respawn_anchor_top"))),
            Map.entry("ANVIL", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/anvil"))),
            Map.entry("CHIPPED_ANVIL", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/chipped_anvil_top"))),
            Map.entry("DAMAGED_ANVIL", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/damaged_anvil_top"))),
            Map.entry("RAIL", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/rail"))),
            Map.entry("POWERED_RAIL", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/powered_rail"))),
            Map.entry("DETECTOR_RAIL", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/detector_rail"))),
            Map.entry("ACTIVATOR_RAIL", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/activator_rail"))),
            Map.entry("CRAFTER", new TargetSprite(ATLAS_BLOCKS, Key.key("minecraft", "block/crafter_top")))
    );

    private static final Set<String> ITEM_ATLAS_ITEMS = Set.of(
            "REDSTONE", "REPEATER", "COMPARATOR", "LEVER", "STRING", "HOPPER",
            "CAULDRON", "BREWING_STAND", "ITEM_FRAME", "GLOW_ITEM_FRAME", "PAINTING",
            "ARMOR_STAND", "CAMPFIRE", "SOUL_CAMPFIRE", "LANTERN", "SOUL_LANTERN",
            "CHAIN", "END_CRYSTAL", "FLOWER_POT", "SUGAR_CANE", "KELP", "BAMBOO",
            "BELL", "BARRIER", "SPYGLASS", "LIGHTNING_ROD", "BRUSH", "BUNDLE",
            "MACE", "WIND_CHARGE", "BREEZE_ROD", "TRIAL_KEY", "OMINOUS_TRIAL_KEY",
            "OMINOUS_BOTTLE", "WHEAT", "WHEAT_SEEDS", "POINTED_DRIPSTONE", "SUNFLOWER",
            "PITCHER_POD", "PITCHER_PLANT", "RESIN_CLUMP"
    );

    @NotNull
    public static Component renderItemSprite(@Nullable Material material) {
        if (material == null || material.isAir()) {
            return Component.empty();
        }
        synchronized (SPRITE_CACHE) {
            Component cached = SPRITE_CACHE.get(material);
            if (cached != null) {
                return cached;
            }
            Component rendered = renderItemSpriteUncached(material);
            SPRITE_CACHE.put(material, rendered);
            return rendered;
        }
    }

    @NotNull
    private static Component renderItemSpriteUncached(@NotNull Material material) {
        TargetSprite target = resolveTargetSprite(material);
        return Component.object(ObjectContents.sprite(target.atlas(), target.sprite()));
    }

    @NotNull
    public static TargetSprite resolveTargetSprite(@NotNull Material material) {
        String name = material.name();
        String matKey = material.getKey().getKey();
        String namespace = material.getKey().getNamespace();

        if (SPRITE_OVERRIDES.containsKey(name)) {
            return SPRITE_OVERRIDES.get(name);
        }

        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") ||
                name.endsWith("_BOOTS") || name.endsWith("_SWORD") || name.endsWith("_PICKAXE") ||
                name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE") ||
                name.equals("BOW") || name.equals("TRIDENT") || name.equals("MACE") ||
                name.equals("FISHING_ROD") || name.equals("SHEARS") || name.equals("FLINT_AND_STEEL") ||
                name.endsWith("_SMITHING_TEMPLATE") || name.endsWith("_DISC") ||
                name.endsWith("_DOOR") || name.endsWith("_SIGN") || name.endsWith("_BOAT") ||
                name.endsWith("_RAFT") || name.endsWith("_MINECART") || ITEM_ATLAS_ITEMS.contains(name)) {
            return new TargetSprite(ATLAS_ITEMS, Key.key(namespace, "item/" + matKey));
        }

        if (name.endsWith("_WOOD")) {
            return new TargetSprite(ATLAS_BLOCKS, Key.key(namespace, "block/" + matKey.replace("_wood", "_log")));
        }

        if (name.endsWith("_HYPHAE")) {
            return new TargetSprite(ATLAS_BLOCKS, Key.key(namespace, "block/" + matKey.replace("_hyphae", "_stem")));
        }

        if (name.endsWith("_BUTTON") || name.endsWith("_PRESSURE_PLATE") || name.endsWith("_FENCE") ||
                name.endsWith("_FENCE_GATE") || name.endsWith("_WALL")) {
            String parent = getParentBlockTexture(name);
            if (parent != null) {
                return new TargetSprite(ATLAS_BLOCKS, Key.key(namespace, "block/" + parent));
            }
        }

        if (material.isBlock()) {
            return new TargetSprite(ATLAS_BLOCKS, Key.key(namespace, "block/" + matKey));
        }

        return new TargetSprite(ATLAS_ITEMS, Key.key(namespace, "item/" + matKey));
    }

    @Nullable
    private static String getParentBlockTexture(@NotNull String name) {
        if (name.startsWith("ACACIA_")) return "acacia_planks";
        if (name.startsWith("OAK_")) return "oak_planks";
        if (name.startsWith("BIRCH_")) return "birch_planks";
        if (name.startsWith("SPRUCE_")) return "spruce_planks";
        if (name.startsWith("JUNGLE_")) return "jungle_planks";
        if (name.startsWith("DARK_OAK_")) return "dark_oak_planks";
        if (name.startsWith("MANGROVE_")) return "mangrove_planks";
        if (name.startsWith("CHERRY_")) return "cherry_planks";
        if (name.startsWith("BAMBOO_")) return "bamboo_planks";
        if (name.startsWith("CRIMSON_")) return "crimson_planks";
        if (name.startsWith("WARPED_")) return "warped_planks";
        if (name.startsWith("PALE_OAK_")) return "pale_oak_planks";
        if (name.startsWith("COBBLESTONE_") || name.equals("COBBLESTONE_WALL")) return "cobblestone";
        if (name.startsWith("MOSSY_COBBLESTONE_")) return "mossy_cobblestone";
        if (name.startsWith("STONE_BRICK_")) return "stone_bricks";
        if (name.startsWith("MOSSY_STONE_BRICK_")) return "mossy_stone_bricks";
        if (name.startsWith("STONE_")) return "stone";
        if (name.startsWith("SMOOTH_STONE_")) return "smooth_stone";
        if (name.startsWith("BRICK_") || name.equals("BRICK_WALL")) return "bricks";
        if (name.startsWith("NETHER_BRICK_") || name.equals("NETHER_BRICK_WALL")) return "nether_bricks";
        if (name.startsWith("RED_NETHER_BRICK_") || name.equals("RED_NETHER_BRICK_WALL")) return "red_nether_bricks";
        if (name.startsWith("SANDSTONE_") || name.equals("SANDSTONE_WALL")) return "sandstone";
        if (name.startsWith("RED_SANDSTONE_") || name.equals("RED_SANDSTONE_WALL")) return "red_sandstone";
        if (name.startsWith("PRISMARINE_BRICK_")) return "prismarine_bricks";
        if (name.startsWith("DARK_PRISMARINE_")) return "dark_prismarine";
        if (name.startsWith("PRISMARINE_") || name.equals("PRISMARINE_WALL")) return "prismarine";
        if (name.startsWith("PURPUR_")) return "purpur_block";
        if (name.startsWith("QUARTZ_")) return "quartz_block_side";
        if (name.startsWith("SMOOTH_QUARTZ_")) return "smooth_quartz";
        if (name.startsWith("DEEPSLATE_BRICK_") || name.equals("DEEPSLATE_BRICK_WALL")) return "deepslate_bricks";
        if (name.startsWith("DEEPSLATE_TILE_") || name.equals("DEEPSLATE_TILE_WALL")) return "deepslate_tiles";
        if (name.startsWith("COBBLED_DEEPSLATE_") || name.equals("COBBLED_DEEPSLATE_WALL")) return "cobbled_deepslate";
        if (name.startsWith("POLISHED_DEEPSLATE_") || name.equals("POLISHED_DEEPSLATE_WALL")) return "polished_deepslate";
        if (name.startsWith("POLISHED_BLACKSTONE_BRICK_") || name.equals("POLISHED_BLACKSTONE_BRICK_WALL")) return "polished_blackstone_bricks";
        if (name.startsWith("POLISHED_BLACKSTONE_") || name.equals("POLISHED_BLACKSTONE_WALL")) return "polished_blackstone";
        if (name.startsWith("BLACKSTONE_") || name.equals("BLACKSTONE_WALL")) return "blackstone";
        if (name.startsWith("POLISHED_ANDESITE_")) return "polished_andesite";
        if (name.startsWith("ANDESITE_") || name.equals("ANDESITE_WALL")) return "andesite";
        if (name.startsWith("POLISHED_DIORITE_")) return "polished_diorite";
        if (name.startsWith("DIORITE_") || name.equals("DIORITE_WALL")) return "diorite";
        if (name.startsWith("POLISHED_GRANITE_")) return "polished_granite";
        if (name.startsWith("GRANITE_") || name.equals("GRANITE_WALL")) return "granite";
        if (name.startsWith("POLISHED_TUFF_") || name.equals("POLISHED_TUFF_WALL")) return "polished_tuff";
        if (name.startsWith("TUFF_BRICK_") || name.equals("TUFF_BRICK_WALL")) return "tuff_bricks";
        if (name.startsWith("TUFF_") || name.equals("TUFF_WALL")) return "tuff";
        if (name.startsWith("MUD_BRICK_") || name.equals("MUD_BRICK_WALL")) return "mud_bricks";
        if (name.startsWith("END_STONE_BRICK_") || name.equals("END_STONE_BRICK_WALL")) return "end_stone_bricks";
        if (name.startsWith("RESIN_BRICK_") || name.equals("RESIN_BRICK_WALL")) return "resin_bricks";
        return null;
    }

    /**
     * Combines an item sprite and text into a single button label Component.
     */
    @NotNull
    public static Component createButtonLabel(@Nullable Material material, @NotNull Component text) {
        if (material == null || material.isAir()) {
            return text;
        }
        return renderItemSprite(material).append(Component.space()).append(text);
    }

    /**
     * Combines an item sprite and text string into a single button label Component.
     */
    @NotNull
    public static Component createButtonLabel(@Nullable Material material, @NotNull String text) {
        return createButtonLabel(material, Component.text(text));
    }

    /**
     * Combines an item/custom icon and text into a single button label Component.
     * Supports custom Material names or MiniMessage/unicode icons.
     */
    @NotNull
    public static Component createButtonLabel(@Nullable Material fallbackMaterial,
                                              @Nullable String customIcon,
                                              @NotNull Component text) {
        if (customIcon != null && !customIcon.isBlank()) {
            Material match = Material.matchMaterial(customIcon.trim().toUpperCase(Locale.ROOT));
            if (match != null && !match.isAir()) {
                return renderItemSprite(match).append(Component.space()).append(text);
            }
            if (customIcon.startsWith("<") || customIcon.contains(":")) {
                Component customComp = MiniMessage.miniMessage().deserialize(customIcon);
                return customComp.append(Component.space()).append(text);
            }
            return Component.text(customIcon + " ").append(text);
        }
        return createButtonLabel(fallbackMaterial, text);
    }
}
