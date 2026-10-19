package com.harukiyu.clarityeconomy.listeners;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerConnectionListener implements Listener {

    private final ClarityEconomy plugin;

    public PlayerConnectionListener(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getJobStorageManager().getPlayerData(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getBlackMarketManager().clearPlayerData(player.getUniqueId());
        plugin.getJobStorageManager().unloadPlayer(player.getUniqueId());
    }
}
