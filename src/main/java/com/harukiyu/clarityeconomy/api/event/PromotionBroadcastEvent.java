package com.harukiyu.clarityeconomy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PromotionBroadcastEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private String message;
    private final boolean isAutomated;
    private boolean cancelled = false;

    public PromotionBroadcastEvent(@Nullable Player player, @NotNull String message, boolean isAutomated) {
        this.player = player;
        this.message = message;
        this.isAutomated = isAutomated;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public void setMessage(@NotNull String message) {
        this.message = message;
    }

    public boolean isAutomated() {
        return isAutomated;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
