package com.harukiyu.clarityeconomy.api.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerBuyItemEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Material material;
    private int amount;
    private double totalPrice;
    private final boolean isBlackMarket;
    private boolean cancelled = false;

    public PlayerBuyItemEvent(@NotNull Player player, @NotNull Material material, int amount, double totalPrice, boolean isBlackMarket) {
        this.player = player;
        this.material = material;
        this.amount = amount;
        this.totalPrice = totalPrice;
        this.isBlackMarket = isBlackMarket;
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

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isBlackMarket() {
        return isBlackMarket;
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
