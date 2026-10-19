package com.harukiyu.clarityeconomy.api.event;

import com.harukiyu.clarityeconomy.api.model.JobType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerJobLevelUpEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final JobType jobType;
    private final int oldLevel;
    private final int newLevel;

    public PlayerJobLevelUpEvent(@NotNull Player player, @NotNull JobType jobType, int oldLevel, int newLevel) {
        this.player = player;
        this.jobType = jobType;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public JobType getJobType() {
        return jobType;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
