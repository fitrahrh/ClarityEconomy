package com.harukiyu.clarityeconomy.commands;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.promotion.PromotionManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuickBroadcastCommand implements BasicCommand {

    private final ClarityEconomy plugin;
    private final PromotionManager.QuickType type;

    public QuickBroadcastCommand(@NotNull ClarityEconomy plugin, @NotNull PromotionManager.QuickType type) {
        this.plugin = plugin;
        this.type = type;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use quick broadcast commands.");
            return;
        }

        if (!player.hasPermission("clarityeconomy.wtb")) {
            player.sendMessage("You do not have permission to use quick broadcast commands.");
            return;
        }

        String note = args.length > 0 ? String.join(" ", args) : null;
        plugin.getPromotionManager().broadcastQuick(player, type, note);
    }
}
