package com.harukiyu.clarityeconomy.commands;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.api.model.JobType;
import com.harukiyu.clarityeconomy.api.model.PlayerJobData;
import com.harukiyu.clarityeconomy.hook.LanguageHook;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("null")
public class ClarityEconomyCommand implements BasicCommand {

    private final ClarityEconomy plugin;

    public ClarityEconomyCommand(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!sender.hasPermission("clarityeconomy.admin")) {
            sender.sendMessage("You do not have permission to execute ClarityEconomy admin commands.");
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            LanguageHook.sendMessage(sender, "command.admin.reloaded");
            return;
        }

        if (args[0].equalsIgnoreCase("auction") && args.length > 1 && args[1].equalsIgnoreCase("set")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can set the auction arena location.");
                return;
            }
            plugin.getAuctionManager().setArenaLocation(player);
            return;
        }

        if (args[0].equalsIgnoreCase("job")) {
            if (args.length >= 4 && args[1].equalsIgnoreCase("set")) {
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage("Player not found: " + args[2]);
                    return;
                }
                try {
                    JobType job = JobType.valueOf(args[3].toUpperCase(Locale.ROOT));
                    int level = 1;
                    if (args.length >= 5) {
                        level = Integer.parseInt(args[4]);
                    }
                    PlayerJobData data = plugin.getJobsManager().getPlayerData(target.getUniqueId());
                    data.setJobType(job);
                    data.setLevel(level);
                    data.setCurrentExp(0);
                    plugin.getJobStorageManager().savePlayerData(data, true);

                    sender.sendMessage("Set " + target.getName() + "'s job to " + job.getDisplayName() + " (Level " + level + ").");
                    LanguageHook.sendMessage(target, "jobs.job_changed",
                            Placeholder.unparsed("job", job.getDisplayName()),
                            Placeholder.unparsed("cost", "0"));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("Invalid job name. Available: " + getAvailableJobsString());
                }
                return;
            }

            if (args.length >= 3 && args[1].equalsIgnoreCase("reset")) {
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage("Player not found: " + args[2]);
                    return;
                }
                PlayerJobData data = plugin.getJobsManager().getPlayerData(target.getUniqueId());
                data.setJobType(null);
                data.reset();
                plugin.getJobStorageManager().savePlayerData(data, true);
                sender.sendMessage("Reset " + target.getName() + "'s job.");
                return;
            }
        }

        sendHelp(sender);
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage("§e--- ClarityEconomy Commands ---");
        sender.sendMessage("§f/ce reload §7- Reload configuration and shops");
        sender.sendMessage("§f/ce auction set §7- Set daily auction arena location");
        sender.sendMessage("§f/ce job set <player> <job> [level] §7- Set a player's job");
        sender.sendMessage("§f/ce job reset <player> §7- Reset a player's job");
        sender.sendMessage("§f/market §7- Open legal market");
        sender.sendMessage("§f/blackmarket §7- Open black market");
        sender.sendMessage("§f/lelang §7- Teleport to auction arena");
        sender.sendMessage("§f/promosi §7- Rent broadcast advertisement slot");
        sender.sendMessage("§f/jobs §7- Open jobs dashboard");
    }

    private String getAvailableJobsString() {
        List<String> list = new ArrayList<>();
        for (JobType j : JobType.values()) {
            list.add(j.name());
        }
        return String.join(", ", list);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload", "auction", "job", "help");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("auction")) {
            return List.of("set");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("job")) {
            return List.of("set", "reset");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("job")) {
            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p != null) {
                    players.add(p.getName());
                }
            }
            return players;
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("job") && args[1].equalsIgnoreCase("set")) {
            List<String> jobs = new ArrayList<>();
            for (JobType j : JobType.values()) {
                jobs.add(j.name());
            }
            return jobs;
        }
        return List.of();
    }
}
