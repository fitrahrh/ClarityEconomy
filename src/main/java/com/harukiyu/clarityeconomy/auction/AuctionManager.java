package com.harukiyu.clarityeconomy.auction;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.hook.LanguageHook;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class AuctionManager {

    private final ClarityEconomy plugin;
    private static final ZoneId TIMEZONE_WIB = ZoneId.of("Asia/Jakarta");

    // Two 4-hour sessions per 24h:
    // Session 1: 07:00 - 11:00 WIB
    // Session 2: 17:00 - 21:00 WIB
    private final int session1StartHour = 7;
    private final int session2StartHour = 17;

    public AuctionManager(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    public boolean isSessionActive() {
        ZonedDateTime now = ZonedDateTime.now(TIMEZONE_WIB);
        int duration = plugin.getConfigManager().getAuctionSessionDurationHours();

        // Check Session 1
        ZonedDateTime s1Start = now.withHour(session1StartHour).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime s1End = s1Start.plusHours(duration);
        if ((now.isEqual(s1Start) || now.isAfter(s1Start)) && now.isBefore(s1End)) {
            return true;
        }

        // Check Session 2
        ZonedDateTime s2Start = now.withHour(session2StartHour).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime s2End = s2Start.plusHours(duration);
        return (now.isEqual(s2Start) || now.isAfter(s2Start)) && now.isBefore(s2End);
    }

    @NotNull
    public String getTimeRemainingFormatted() {
        ZonedDateTime now = ZonedDateTime.now(TIMEZONE_WIB);
        int duration = plugin.getConfigManager().getAuctionSessionDurationHours();

        ZonedDateTime s1Start = now.withHour(session1StartHour).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime s1End = s1Start.plusHours(duration);
        ZonedDateTime s2Start = now.withHour(session2StartHour).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime s2End = s2Start.plusHours(duration);

        if ((now.isEqual(s1Start) || now.isAfter(s1Start)) && now.isBefore(s1End)) {
            Duration rem = Duration.between(now, s1End);
            return formatDuration(rem);
        }
        if ((now.isEqual(s2Start) || now.isAfter(s2Start)) && now.isBefore(s2End)) {
            Duration rem = Duration.between(now, s2End);
            return formatDuration(rem);
        }

        // Closed: calculate next session
        ZonedDateTime nextSession;
        if (now.isBefore(s1Start)) {
            nextSession = s1Start;
        } else if (now.isBefore(s2Start)) {
            nextSession = s2Start;
        } else {
            nextSession = s1Start.plusDays(1);
        }

        Duration until = Duration.between(now, nextSession);
        long h = until.toHours();
        long m = until.toMinutesPart();
        return String.format("%dh %dm", h, m);
    }

    private String formatDuration(Duration d) {
        long h = d.toHours();
        long m = d.toMinutesPart();
        long s = d.toSecondsPart();
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @NotNull
    public String getScheduleDisplay() {
        int duration = plugin.getConfigManager().getAuctionSessionDurationHours();
        return String.format("Session 1: %02d:00 - %02d:00 WIB | Session 2: %02d:00 - %02d:00 WIB",
                session1StartHour, session1StartHour + duration,
                session2StartHour, session2StartHour + duration);
    }

    public void teleportToArena(@NotNull Player player) {
        Location loc = plugin.getConfigManager().getAuctionArenaLocation();
        if (loc == null) {
            LanguageHook.sendMessage(player, "auction.location_not_set");
            return;
        }

        if (!isSessionActive()) {
            LanguageHook.sendMessage(player, "auction.closed",
                    Placeholder.unparsed("schedule", getScheduleDisplay()),
                    Placeholder.unparsed("remaining", getTimeRemainingFormatted()));
            return;
        }

        player.teleportAsync(loc).thenAccept(success -> {
            if (success) {
                LanguageHook.sendMessage(player, "auction.teleport_success");
            } else {
                LanguageHook.sendMessage(player, "auction.teleport_failed");
            }
        });
    }

    public void setArenaLocation(@NotNull Player player) {
        Location loc = player.getLocation();
        plugin.getConfigManager().setAuctionLocation(loc);
        LanguageHook.sendMessage(player, "auction.location_saved",
                Placeholder.unparsed("world", loc.getWorld().getName()),
                Placeholder.unparsed("x", String.format("%.1f", loc.getX())),
                Placeholder.unparsed("y", String.format("%.1f", loc.getY())),
                Placeholder.unparsed("z", String.format("%.1f", loc.getZ())));
    }
}
