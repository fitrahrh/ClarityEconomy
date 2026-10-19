package com.harukiyu.clarityeconomy;

import com.harukiyu.clarityeconomy.api.ClarityEconomyAPI;
import com.harukiyu.clarityeconomy.auction.AuctionManager;
import com.harukiyu.clarityeconomy.blackmarket.BlackMarketManager;
import com.harukiyu.clarityeconomy.commands.*;
import com.harukiyu.clarityeconomy.config.ConfigManager;
import com.harukiyu.clarityeconomy.dialog.BedrockEconomyMenuManager;
import com.harukiyu.clarityeconomy.dialog.EconomyDialogManager;
import com.harukiyu.clarityeconomy.hook.BedrockFormHook;
import com.harukiyu.clarityeconomy.hook.LanguageHook;
import com.harukiyu.clarityeconomy.hook.PlaceholderHook;
import com.harukiyu.clarityeconomy.hook.VaultHook;
import com.harukiyu.clarityeconomy.jobs.AuraBuffTask;
import com.harukiyu.clarityeconomy.jobs.JobStorageManager;
import com.harukiyu.clarityeconomy.jobs.JobsManager;
import com.harukiyu.clarityeconomy.listeners.JobActivityListener;
import com.harukiyu.clarityeconomy.listeners.PlayerConnectionListener;
import com.harukiyu.clarityeconomy.listeners.PlayerFreezeListener;
import com.harukiyu.clarityeconomy.market.MarketManager;
import com.harukiyu.clarityeconomy.promotion.PromotionManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ClarityEconomy extends JavaPlugin {

    private static ClarityEconomy instance;

    private ConfigManager configManager;
    private MarketManager marketManager;
    private BlackMarketManager blackMarketManager;
    private AuctionManager auctionManager;
    private PromotionManager promotionManager;
    private JobStorageManager jobStorageManager;
    private JobsManager jobsManager;
    private EconomyDialogManager dialogManager;
    private BedrockEconomyMenuManager bedrockMenuManager;
    private AuraBuffTask auraBuffTask;
    private ScheduledTask autoSaveTask;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // 1. Config & Hooks Initialization
        this.configManager = new ConfigManager(this);
        this.configManager.reload();
        LanguageHook.init(this, "clarityeconomy");
        VaultHook.init();
        BedrockFormHook.init();

        // 2. Core Managers Initialization
        this.jobStorageManager = new JobStorageManager(this);
        this.jobsManager = new JobsManager(this, jobStorageManager);
        this.marketManager = new MarketManager(this);
        this.marketManager.loadShops();
        this.blackMarketManager = new BlackMarketManager(this);
        this.blackMarketManager.load();
        this.auctionManager = new AuctionManager(this);
        this.promotionManager = new PromotionManager(this);
        this.promotionManager.start();

        // 3. Dialog Managers
        this.dialogManager = new EconomyDialogManager(this);
        this.bedrockMenuManager = new BedrockEconomyMenuManager(this);

        // 4. API Initialization
        ClarityEconomyAPI.init(this);

        // 5. Register Event Listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerConnectionListener(this), this);
        pm.registerEvents(new PlayerFreezeListener(this), this);
        pm.registerEvents(new JobActivityListener(this), this);

        // 6. Register Commands (Paper 26.3 Native BasicCommand)
        registerCommand("market", "Open legal market item store", List.of("shop", "toko"), new MarketCommand(this));
        registerCommand("blackmarket", "Open clandestine black market", List.of("bm", "pasargelap"), new BlackMarketCommand(this));
        registerCommand("lelang", "Teleport to daily auction arena", List.of("auction"), new AuctionCommand(this));
        registerCommand("promosi", "Rent automated advertisement broadcast slot", List.of("promo", "iklan"), new PromosiCommand(this));
        registerCommand("wtb", "Quick buy broadcast", List.of(), new QuickBroadcastCommand(this, PromotionManager.QuickType.WTB));
        registerCommand("wts", "Quick sell broadcast", List.of(), new QuickBroadcastCommand(this, PromotionManager.QuickType.WTS));
        registerCommand("wta", "Quick ask broadcast", List.of(), new QuickBroadcastCommand(this, PromotionManager.QuickType.WTA));
        registerCommand("jobs", "Open jobs progression dashboard", List.of("job", "pekerjaan"), new JobsCommand(this));
        registerCommand("ceconomy", "Admin command for ClarityEconomy", List.of("ce", "clarityeconomy"), new ClarityEconomyCommand(this));

        // 7. Register PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            getLogger().info("Successfully hooked into PlaceholderAPI!");
        }

        // 8. Start Recurring Tasks
        this.auraBuffTask = new AuraBuffTask(this);
        this.auraBuffTask.runTaskTimer(this, 60L, 60L); // Every 3 seconds

        this.autoSaveTask = Bukkit.getAsyncScheduler().runAtFixedRate(
                this,
                task -> {
                    jobStorageManager.saveAll(false);
                    promotionManager.stop();
                    promotionManager.start();
                },
                5,
                5,
                TimeUnit.MINUTES
        );

        getLogger().log(Level.INFO, "{0} v{1} has been enabled!", new Object[]{getName(), getPluginMeta().getVersion()});
    }

    @Override
    public void onDisable() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        if (auraBuffTask != null) {
            auraBuffTask.cancel();
        }

        if (promotionManager != null) {
            promotionManager.stop();
        }

        if (jobStorageManager != null) {
            jobStorageManager.saveAll(false);
        }

        getLogger().log(Level.INFO, "{0} has been disabled!", getName());
    }

    public void reloadPlugin() {
        configManager.reload();
        LanguageHook.init(this, "clarityeconomy");
        VaultHook.init();
        marketManager.loadShops();
        blackMarketManager.load();
        promotionManager.reload();
    }

    public static ClarityEconomy getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    public BlackMarketManager getBlackMarketManager() {
        return blackMarketManager;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public PromotionManager getPromotionManager() {
        return promotionManager;
    }

    public JobStorageManager getJobStorageManager() {
        return jobStorageManager;
    }

    public JobsManager getJobsManager() {
        return jobsManager;
    }

    public EconomyDialogManager getDialogManager() {
        return dialogManager;
    }

    public BedrockEconomyMenuManager getBedrockMenuManager() {
        return bedrockMenuManager;
    }
}
