package com.harukiyu.clarityeconomy.api.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerJobData {

    private final UUID playerUuid;
    private JobType jobType;
    private int level;
    private long currentExp;

    public PlayerJobData(@NotNull UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.jobType = null;
        this.level = 1;
        this.currentExp = 0;
    }

    public PlayerJobData(@NotNull UUID playerUuid, @Nullable JobType jobType, int level, long currentExp) {
        this.playerUuid = playerUuid;
        this.jobType = jobType;
        this.level = Math.max(1, level);
        this.currentExp = Math.max(0, currentExp);
    }

    @NotNull
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    @Nullable
    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(@Nullable JobType jobType) {
        this.jobType = jobType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public long getCurrentExp() {
        return currentExp;
    }

    public void setCurrentExp(long currentExp) {
        this.currentExp = Math.max(0, currentExp);
    }

    public void addExp(long exp) {
        if (exp > 0) {
            this.currentExp += exp;
        }
    }

    public void reset() {
        this.level = 1;
        this.currentExp = 0;
    }
}
