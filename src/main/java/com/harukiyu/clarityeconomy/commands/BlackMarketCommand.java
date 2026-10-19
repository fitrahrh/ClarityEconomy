package com.harukiyu.clarityeconomy.commands;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BlackMarketCommand implements BasicCommand {

    private final ClarityEconomy plugin;

    public BlackMarketCommand(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can open the Black Market menu.");
            return;
        }

        if (!player.hasPermission("clarityeconomy.blackmarket")) {
            player.sendMessage("You do not have permission to access the black market.");
            return;
        }

        plugin.getDialogManager().openBlackMarketMenu(player);
    }
}
