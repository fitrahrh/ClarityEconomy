package com.harukiyu.clarityeconomy.listeners;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerFreezeListener implements Listener {

    private final ClarityEconomy plugin;

    public PlayerFreezeListener(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getBlackMarketManager().isFrozen(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        // Cancel positional translation, but allow head rotation (yaw/pitch)
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location locked = from.clone();
            locked.setYaw(to.getYaw());
            locked.setPitch(to.getPitch());
            event.setTo(locked);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (plugin.getBlackMarketManager().isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (plugin.getBlackMarketManager().isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
