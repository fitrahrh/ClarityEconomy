package com.harukiyu.clarityeconomy.hook;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.Form;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.geyser.api.GeyserApi;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class BedrockFormHook {

    private BedrockFormHook() {}

    public static void init() {
        ClarityEconomy inst = ClarityEconomy.getInstance();
        if (inst != null) {
            boolean fg = isFloodgateAvailable();
            boolean gy = isGeyserAvailable();
            inst.getLogger().info(() -> "[BedrockFormHook] Initialized. Floodgate API: " + (fg ? "Available" : "Not Found")
                    + " | Geyser API: " + (gy ? "Available" : "Not Found"));
        }
    }

    public static boolean isFloodgateAvailable() {
        try {
            return FloodgateApi.getInstance() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isGeyserAvailable() {
        try {
            return GeyserApi.api() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isBedrockPlayer(@NotNull Player player) {
        UUID uuid = player.getUniqueId();

        // 1. Floodgate API
        try {
            FloodgateApi floodgate = FloodgateApi.getInstance();
            if (floodgate != null) {
                if (floodgate.isFloodgatePlayer(uuid)) return true;
                if (floodgate.getPlayer(uuid) != null) return true;
                if (floodgate.isFloodgateId(uuid)) return true;
            }
        } catch (Throwable ignored) {}

        // 2. Geyser API
        try {
            GeyserApi geyser = GeyserApi.api();
            if (geyser != null && geyser.isBedrockPlayer(uuid)) {
                return true;
            }
        } catch (Throwable ignored) {}

        // 3. Metadata check
        try {
            if (player.hasMetadata("floodgate:player") || player.hasMetadata("geyser:player")) {
                return true;
            }
        } catch (Throwable ignored) {}

        // 4. Name prefix check
        String name = player.getName();
        return name.startsWith(".") || name.startsWith("*");
    }

    public static boolean sendForm(@NotNull Player player, @NotNull Form form) {
        UUID uuid = player.getUniqueId();

        try {
            FloodgateApi floodgate = FloodgateApi.getInstance();
            if (floodgate != null && floodgate.isFloodgatePlayer(uuid)) {
                return floodgate.sendForm(uuid, form);
            }
        } catch (Throwable ignored) {}

        try {
            GeyserApi geyser = GeyserApi.api();
            if (geyser != null && geyser.isBedrockPlayer(uuid)) {
                return geyser.sendForm(uuid, form);
            }
        } catch (Throwable ignored) {}

        try {
            return FloodgateApi.getInstance().sendForm(uuid, form);
        } catch (Throwable t) {
            try {
                return GeyserApi.api().sendForm(uuid, form);
            } catch (Throwable ignored) {}
        }

        return false;
    }
}
