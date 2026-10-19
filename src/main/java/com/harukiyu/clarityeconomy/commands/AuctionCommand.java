package com.harukiyu.clarityeconomy.commands;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class AuctionCommand implements BasicCommand {

    private final ClarityEconomy plugin;

    public AuctionCommand(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) {
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /auction.");
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("set")) {
            if (!player.hasPermission("clarityeconomy.admin")) {
                player.sendMessage("You do not have permission to set the auction arena location.");
                return;
            }
            plugin.getAuctionManager().setArenaLocation(player);
            return;
        }

        if (!player.hasPermission("clarityeconomy.lelang")) {
            player.sendMessage("You do not have permission to access the auction arena.");
            return;
        }

        plugin.getAuctionManager().teleportToArena(player);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length <= 1 && stack.getSender().hasPermission("clarityeconomy.admin")) {
            return List.of("set");
        }
        return List.of();
    }
}
