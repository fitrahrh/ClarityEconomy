package com.harukiyu.clarityeconomy.api.event;

import com.harukiyu.clarityeconomy.api.model.JobType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerJobChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final JobType oldJob;
    private final JobType newJob;
    private final double cost;
    private boolean cancelled = false;

    public PlayerJobChangeEvent(@NotNull Player player, @Nullable JobType oldJob, @Nullable JobType newJob, double cost) {
        this.player = player;
        this.oldJob = oldJob;
        this.newJob = newJob;
        this.cost = cost;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Nullable
    public JobType getOldJob() {
        return oldJob;
    }

    @Nullable
    public JobType getNewJob() {
        return newJob;
    }

    public double getCost() {
        return cost;
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
