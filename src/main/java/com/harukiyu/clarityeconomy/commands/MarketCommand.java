package com.harukiyu.clarityeconomy.commands;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MarketCommand implements BasicCommand {

    private final ClarityEconomy plugin;

    public MarketCommand(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) {
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can open the Market menu.");
            return;
        }

        if (!player.hasPermission("clarityeconomy.market")) {
            player.sendMessage("You do not have permission to open the market.");
            return;
        }

        plugin.getDialogManager().openMarketMenu(player);
    }
}
