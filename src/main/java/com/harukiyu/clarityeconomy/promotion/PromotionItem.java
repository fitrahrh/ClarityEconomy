package com.harukiyu.clarityeconomy.promotion;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PromotionItem {

    private final UUID playerUuid;
    private final String playerName;
    private final String message;
    private final long createdAt;
    private final long expiresAt;

    public PromotionItem(@NotNull UUID playerUuid,
                         @NotNull String playerName,
                         @NotNull String message,
                         long createdAt,
                         long expiresAt) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.message = message;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    @NotNull
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }
}
