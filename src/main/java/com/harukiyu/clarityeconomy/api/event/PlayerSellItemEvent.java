package com.harukiyu.clarityeconomy.api.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerSellItemEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Material material;
    private int amount;
    private double totalPayout;
    private boolean cancelled = false;

    public PlayerSellItemEvent(@NotNull Player player, @NotNull Material material, int amount, double totalPayout) {
        this.player = player;
        this.material = material;
        this.amount = amount;
        this.totalPayout = totalPayout;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getTotalPayout() {
        return totalPayout;
    }

    public void setTotalPayout(double totalPayout) {
        this.totalPayout = totalPayout;
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
