package com.harukiyu.clarityeconomy.jobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.api.model.JobType;
import com.harukiyu.clarityeconomy.api.model.PlayerJobData;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@SuppressWarnings("null")
public class JobStorageManager {

    private final ClarityEconomy plugin;
    private final Map<UUID, PlayerJobData> cache = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File jobsDir;

    public JobStorageManager(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
        this.jobsDir = new File(plugin.getDataFolder(), "data/jobs");
        if (!jobsDir.exists()) {
            jobsDir.mkdirs();
        }
    }

    @NotNull
    public PlayerJobData getPlayerData(@NotNull UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadFromFile);
    }

    @Nullable
    public PlayerJobData getIfPresent(@NotNull UUID uuid) {
        return cache.get(uuid);
    }

    @NotNull
    public PlayerJobData loadFromFile(@NotNull UUID uuid) {
        File file = new File(jobsDir, uuid + ".json");
        if (!file.exists()) {
            return new PlayerJobData(uuid);
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JobDataJson json = gson.fromJson(reader, JobDataJson.class);
            if (json != null) {
                JobType job = null;
                if (json.jobType != null && !json.jobType.isBlank()) {
                    try {
                        job = JobType.valueOf(json.jobType.toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }
                return new PlayerJobData(uuid, job, json.level, json.currentExp);
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Failed to load job data for " + uuid + ": " + t.getMessage(), t);
        }

        return new PlayerJobData(uuid);
    }

    public void savePlayerData(@NotNull PlayerJobData data, boolean async) {
        Runnable task = () -> {
            File file = new File(jobsDir, data.getPlayerUuid() + ".json");
            JobDataJson json = new JobDataJson();
            JobType jobType = data.getJobType();
            json.jobType = jobType != null ? jobType.name() : null;
            json.level = data.getLevel();
            json.currentExp = data.getCurrentExp();

            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                gson.toJson(json, writer);
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Failed to save job data for " + data.getPlayerUuid() + ": " + t.getMessage(), t);
            }
        };

        if (async && plugin.isEnabled()) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            task.run();
        }
    }

    public void saveAll(boolean async) {
        for (PlayerJobData data : cache.values()) {
            savePlayerData(data, async);
        }
    }

    public void unloadPlayer(@NotNull UUID uuid) {
        PlayerJobData data = cache.remove(uuid);
        if (data != null) {
            savePlayerData(data, false);
        }
    }

    private static class JobDataJson {
        String jobType;
        int level;
        long currentExp;
    }
}
