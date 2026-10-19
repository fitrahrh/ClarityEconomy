package com.harukiyu.clarityeconomy.commands;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("null")
public class PromosiCommand implements BasicCommand {

    private final ClarityEconomy plugin;

    public PromosiCommand(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /promosi.");
            return;
        }

        if (!player.hasPermission("clarityeconomy.promosi")) {
            player.sendMessage("You do not have permission to rent promotion slots.");
            return;
        }

        plugin.getDialogManager().openPromotionDialog(player);
    }
}
