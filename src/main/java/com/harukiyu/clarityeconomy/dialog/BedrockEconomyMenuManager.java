package com.harukiyu.clarityeconomy.dialog;

import com.harukiyu.clarityeconomy.ClarityEconomy;
import com.harukiyu.clarityeconomy.api.model.JobType;
import com.harukiyu.clarityeconomy.api.model.PlayerJobData;
import com.harukiyu.clarityeconomy.blackmarket.BlackMarketManager;
import com.harukiyu.clarityeconomy.hook.BedrockFormHook;
import com.harukiyu.clarityeconomy.hook.LanguageHook;
import com.harukiyu.clarityeconomy.hook.VaultHook;
import com.harukiyu.clarityeconomy.market.ShopCategory;
import com.harukiyu.clarityeconomy.market.ShopItem;
import com.harukiyu.clarityeconomy.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("null")
public class BedrockEconomyMenuManager {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private final ClarityEconomy plugin;

    public BedrockEconomyMenuManager(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    private void runSync(Runnable task) {
        if (plugin.isEnabled()) {
            Bukkit.getScheduler().runTask(plugin, task);
        } else {
            task.run();
        }
    }

    private String text(@NotNull Component component) {
        return PLAIN.serialize(component);
    }

    /* =========================================================================
     * A. LEGAL MARKET (BEDROCK CUMULUS)
     * ========================================================================= */

    public void openMarketMenu(@NotNull Player player) {
        Component title = LanguageHook.getComponent("dialog.market.title", player);
        Component body = LanguageHook.getComponent("dialog.market.body", player,
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(text(title))
                .content(text(body));

        List<ShopCategory> categories = new ArrayList<>(plugin.getMarketManager().getCategories());
        for (ShopCategory cat : categories) {
            builder.button("§0" + cat.getIconString() + " " + cat.getName() + "\n§7" + cat.getDescription());
        }

        builder.validResultHandler(response -> {
            int idx = response.clickedButtonId();
            if (idx >= 0 && idx < categories.size()) {
                ShopCategory selected = categories.get(idx);
                runSync(() -> openCategoryMenu(player, selected));
            }
        });

        BedrockFormHook.sendForm(player, builder.build());
    }

    public void openCategoryMenu(@NotNull Player player, @NotNull ShopCategory category) {
        Component title = LanguageHook.getComponent("dialog.category.title", player,
                Placeholder.unparsed("category", category.getName()));
        Component body = LanguageHook.getComponent("dialog.category.body", player,
                Placeholder.unparsed("category", category.getName()),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(text(title))
                .content(text(body));

        List<ShopItem> items = new ArrayList<>(category.getItems());
        for (ShopItem item : items) {
            String name = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
            String price = (item.canBuy() ? "B: $" + VaultHook.formatMoney(item.getBuyPrice()) : "") +
                    (item.canBuy() && item.canSell() ? " | " : "") +
                    (item.canSell() ? "S: $" + VaultHook.formatMoney(item.getSellPrice()) : "");
            builder.button("§0" + item.getIcon() + " " + name + "\n§8" + price);
        }

        builder.button("§c« " + text(LanguageHook.getComponent("general.btn_back", player)));

        builder.validResultHandler(response -> {
            int idx = response.clickedButtonId();
            if (idx >= 0 && idx < items.size()) {
                ShopItem item = items.get(idx);
                runSync(() -> openItemActionMenu(player, category, item));
            } else if (idx == items.size()) {
                runSync(() -> openMarketMenu(player));
            }
        });

        BedrockFormHook.sendForm(player, builder.build());
    }

    public void openItemActionMenu(@NotNull Player player, @NotNull ShopCategory category, @NotNull ShopItem item) {
        String itemName = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
        Component title = LanguageHook.getComponent("dialog.item_action.title", player,
                Placeholder.unparsed("item", itemName));

        int invCount = Utils.countItem(player, item.getMaterial());
        Component body = LanguageHook.getComponent("dialog.item_action.body", player,
                Placeholder.unparsed("item", itemName),
                Placeholder.unparsed("buy_price", item.canBuy() ? VaultHook.formatMoney(item.getBuyPrice()) : "N/A"),
                Placeholder.unparsed("sell_price", item.canSell() ? VaultHook.formatMoney(item.getSellPrice()) : "N/A"),
                Placeholder.unparsed("inv_count", String.valueOf(invCount)),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(text(title))
                .content(text(body));

        List<String> actions = new ArrayList<>();
        if (item.canBuy()) {
            builder.button("§2" + text(LanguageHook.getComponent("market.btn_buy", player)) + " ($" + VaultHook.formatMoney(item.getBuyPrice()) + ")");
            actions.add("BUY");
        }
        if (item.canSell()) {
            builder.button("§6" + text(LanguageHook.getComponent("market.btn_sell", player)) + " ($" + VaultHook.formatMoney(item.getSellPrice()) + ")");
            actions.add("SELL");
        }

        builder.button("§c« " + text(LanguageHook.getComponent("general.btn_back", player)));
        actions.add("BACK");

        builder.validResultHandler(response -> {
            int idx = response.clickedButtonId();
            if (idx >= 0 && idx < actions.size()) {
                String act = actions.get(idx);
                switch (act) {
                    case "BUY" -> runSync(() -> openBuyQuantityDialog(player, category, item, false, item.getBuyPrice()));
                    case "SELL" -> runSync(() -> openSellQuantityDialog(player, category, item, item.getSellPrice()));
                    case "BACK" -> runSync(() -> openCategoryMenu(player, category));
                }
            }
        });

        BedrockFormHook.sendForm(player, builder.build());
    }

    public void openBuyQuantityDialog(@NotNull Player player,
                                      @Nullable ShopCategory category,
                                      @NotNull ShopItem item,
                                      boolean isBlackMarket,
                                      double unitPrice) {
        String itemName = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
        Component title = LanguageHook.getComponent("dialog.buy_input.title", player,
                Placeholder.unparsed("item", itemName));

        CustomForm form = CustomForm.builder()
                .title(text(title))
                .input("Quantity (Unit Price: $" + VaultHook.formatMoney(unitPrice) + "):", "1", "1")
                .validResultHandler(response -> {
                    String input = response.asInput(0);
                    int amount;
                    try {
                        amount = Integer.parseInt(input);
                        if (amount <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        LanguageHook.sendMessage(player, "market.invalid_quantity");
                        return;
                    }
                    runSync(() -> openBuyConfirmDialog(player, category, item, amount, isBlackMarket, unitPrice));
                })
                .closedOrInvalidResultHandler(response -> {
                    if (isBlackMarket) {
                        plugin.getBlackMarketManager().setFrozen(player, false);
                    }
                })
                .build();

        BedrockFormHook.sendForm(player, form);
    }

    public void openBuyConfirmDialog(@NotNull Player player,
                                     @Nullable ShopCategory category,
                                     @NotNull ShopItem item,
                                     int amount,
                                     boolean isBlackMarket,
                                     double unitPrice) {
        String itemName = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
        double totalCost = unitPrice * amount;

        Component title = LanguageHook.getComponent("dialog.buy_confirm.title", player);
        Component body = LanguageHook.getComponent("dialog.buy_confirm.body", player,
                Placeholder.unparsed("amount", String.valueOf(amount)),
                Placeholder.unparsed("item", itemName),
                Placeholder.unparsed("cost", VaultHook.formatMoney(totalCost)),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        ModalForm form = ModalForm.builder()
                .title(text(title))
                .content(text(body))
                .button1("§2" + text(LanguageHook.getComponent("general.btn_confirm", player)))
                .button2("§c" + text(LanguageHook.getComponent("general.btn_cancel", player)))
                .validResultHandler(response -> {
                    if (response.clickedFirst()) {
                        runSync(() -> {
                            if (isBlackMarket) {
                                BlackMarketManager bm = plugin.getBlackMarketManager();
                                bm.applyCooldown(player);
                                bm.setFrozen(player, false);
                            }
                            plugin.getMarketManager().processBuy(player, item, amount, isBlackMarket, unitPrice);
                        });
                    } else {
                        if (isBlackMarket) {
                            plugin.getBlackMarketManager().setFrozen(player, false);
                            runSync(() -> openBlackMarketMenu(player));
                        } else if (category != null) {
                            runSync(() -> openItemActionMenu(player, category, item));
                        }
                    }
                })
                .build();

        BedrockFormHook.sendForm(player, form);
    }

    public void openSellQuantityDialog(@NotNull Player player,
                                       @NotNull ShopCategory category,
                                       @NotNull ShopItem item,
                                       double unitPrice) {
        String itemName = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
        Component title = LanguageHook.getComponent("dialog.sell_input.title", player,
                Placeholder.unparsed("item", itemName));

        int have = Utils.countItem(player, item.getMaterial());

        CustomForm form = CustomForm.builder()
                .title(text(title))
                .input("Amount to sell (You have: " + have + " | Unit payout: $" + VaultHook.formatMoney(unitPrice) + "):", String.valueOf(Math.max(1, have)), String.valueOf(Math.max(1, have)))
                .toggle("Sell All (" + have + ")", false)
                .validResultHandler(response -> {
                    boolean sellAll = response.asToggle(1);
                    int amount;
                    if (sellAll) {
                        amount = have;
                    } else {
                        String input = response.asInput(0);
                        try {
                            amount = Integer.parseInt(input);
                            if (amount <= 0) throw new NumberFormatException();
                        } catch (NumberFormatException e) {
                            LanguageHook.sendMessage(player, "market.invalid_quantity");
                            return;
                        }
                    }
                    if (amount <= 0) {
                        LanguageHook.sendMessage(player, "market.not_enough_items",
                                Placeholder.unparsed("required", "1"),
                                Placeholder.unparsed("have", "0"));
                        return;
                    }
                    final int finalAmount = amount;
                    runSync(() -> openSellConfirmDialog(player, category, item, finalAmount, unitPrice));
                })
                .build();

        BedrockFormHook.sendForm(player, form);
    }

    public void openSellConfirmDialog(@NotNull Player player,
                                      @NotNull ShopCategory category,
                                      @NotNull ShopItem item,
                                      int amount,
                                      double unitPrice) {
        String itemName = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
        double totalPayout = unitPrice * amount;

        Component title = LanguageHook.getComponent("dialog.sell_confirm.title", player);
        Component body = LanguageHook.getComponent("dialog.sell_confirm.body", player,
                Placeholder.unparsed("amount", String.valueOf(amount)),
                Placeholder.unparsed("item", itemName),
                Placeholder.unparsed("payout", VaultHook.formatMoney(totalPayout)),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        ModalForm form = ModalForm.builder()
                .title(text(title))
                .content(text(body))
                .button1("§2" + text(LanguageHook.getComponent("general.btn_confirm", player)))
                .button2("§c" + text(LanguageHook.getComponent("general.btn_cancel", player)))
                .validResultHandler(response -> {
                    if (response.clickedFirst()) {
                        runSync(() -> plugin.getMarketManager().processSell(player, item, amount, unitPrice));
                    } else {
                        runSync(() -> openItemActionMenu(player, category, item));
                    }
                })
                .build();

        BedrockFormHook.sendForm(player, form);
    }

    /* =========================================================================
     * B. BLACK MARKET (BEDROCK CUMULUS)
     * ========================================================================= */

    public void openBlackMarketMenu(@NotNull Player player) {
        BlackMarketManager bm = plugin.getBlackMarketManager();

        if (!bm.isOpen()) {
            bm.setFrozen(player, false);
            Component title = LanguageHook.getComponent("dialog.blackmarket.closed_title", player);
            Component body = LanguageHook.getComponent("blackmarket.closed", player,
                    Placeholder.unparsed("schedule", bm.getScheduleDisplay()),
                    Placeholder.unparsed("remaining", bm.getTimeRemainingFormatted()));

            SimpleForm form = SimpleForm.builder()
                    .title(text(title))
                    .content(text(body))
                    .button("§c" + text(LanguageHook.getComponent("general.btn_close", player)))
                    .build();

            BedrockFormHook.sendForm(player, form);
            return;
        }

        // Freeze player
        bm.setFrozen(player, true);

        Component title = LanguageHook.getComponent("dialog.blackmarket.title", player);
        Component body = LanguageHook.getComponent("dialog.blackmarket.body", player,
                Placeholder.unparsed("remaining", bm.getTimeRemainingFormatted()),
                Placeholder.unparsed("discount", "80% (5x)"),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(text(title))
                .content(text(body));

        List<ShopItem> items = new ArrayList<>(bm.getItems());
        for (ShopItem item : items) {
            double discounted = bm.getDiscountedPrice(item.getBuyPrice());
            String name = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
            builder.button("§0" + item.getIcon() + " " + name + "\n§2$" + VaultHook.formatMoney(discounted) + " §8(Orig: $" + VaultHook.formatMoney(item.getBuyPrice()) + ")");
        }

        builder.button("§c" + text(LanguageHook.getComponent("general.btn_close", player)));

        builder.validResultHandler(response -> {
            int idx = response.clickedButtonId();
            if (idx >= 0 && idx < items.size()) {
                ShopItem item = items.get(idx);
                double discounted = bm.getDiscountedPrice(item.getBuyPrice());
                runSync(() -> {
                    if (!bm.checkCooldown(player)) {
                        long remSec = bm.getRemainingCooldownSeconds(player);
                        LanguageHook.sendMessage(player, "blackmarket.cooldown",
                                Placeholder.unparsed("seconds", String.valueOf(remSec)));
                        return;
                    }
                    openBuyQuantityDialog(player, null, item, true, discounted);
                });
            } else {
                bm.setFrozen(player, false);
            }
        });

        builder.closedOrInvalidResultHandler(response -> bm.setFrozen(player, false));

        BedrockFormHook.sendForm(player, builder.build());
    }

    /* =========================================================================
     * C. JOBS (BEDROCK CUMULUS)
     * ========================================================================= */

    public void openJobsMenu(@NotNull Player player) {
        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        JobType currentJob = data.getJobType();

        // SCENARIO A: No job yet
        if (currentJob == null) {
            Component title = LanguageHook.getComponent("dialog.jobs.no_job_title", player);
            Component body = LanguageHook.getComponent("dialog.jobs.no_job_body", player);

            SimpleForm.Builder builder = SimpleForm.builder()
                    .title(text(title))
                    .content(text(body));

            builder.button(text(LanguageHook.getComponent("dialog.jobs.btn_choose_now", player)));
            builder.button(text(LanguageHook.getComponent("dialog.jobs.btn_guide", player)));

            boolean hasMenu = plugin.getConfigManager().isEnableBackToMainMenu();
            if (hasMenu) {
                builder.button(text(LanguageHook.getComponent("dialog.jobs.btn_main_menu", player)));
            }

            builder.button(text(LanguageHook.getComponent("general.btn_close", player)));

            builder.validResultHandler(response -> {
                int idx = response.clickedButtonId();
                if (idx == 0) {
                    runSync(() -> openJobSelectionMenu(player));
                } else if (idx == 1) {
                    runSync(() -> openJobsGuideMenu(player));
                } else if (hasMenu && idx == 2) {
                    runSync(() -> returnToMainMenu(player));
                }
            });

            BedrockFormHook.sendForm(player, builder.build());
            return;
        }

        // SCENARIO B: Has job
        int maxLvl = plugin.getConfigManager().getJobMaxLevel();
        long reqExp = plugin.getJobsManager().calculateRequiredExp(data.getLevel());
        double expPercent = reqExp > 0 ? Math.min(100.0, ((double) data.getCurrentExp() / reqExp) * 100.0) : 100.0;

        Component title = LanguageHook.getComponent("dialog.jobs.title", player);
        Component body = LanguageHook.getComponent("dialog.jobs.body", player,
                Placeholder.unparsed("job", currentJob.getDisplayName()),
                Placeholder.unparsed("level", String.valueOf(data.getLevel())),
                Placeholder.unparsed("max_level", String.valueOf(maxLvl)),
                Placeholder.unparsed("exp", String.valueOf(data.getCurrentExp())),
                Placeholder.unparsed("req_exp", data.getLevel() >= maxLvl ? "MAX" : String.valueOf(reqExp)),
                Placeholder.unparsed("exp_percent", String.format("%.1f", expPercent)),
                Placeholder.unparsed("self_buff", currentJob.getSelfBuffDesc()),
                Placeholder.unparsed("aura_buff", currentJob.getAuraBuffDesc()),
                Placeholder.unparsed("reset_cost", VaultHook.formatMoney(plugin.getConfigManager().getJobResetCost())));

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(text(title))
                .content(text(body));

        builder.button(text(LanguageHook.getComponent("dialog.jobs.btn_details", player)));
        builder.button(text(LanguageHook.getComponent("dialog.jobs.btn_guide", player)));

        boolean hasMenu = plugin.getConfigManager().isEnableBackToMainMenu();
        if (hasMenu) {
            builder.button(text(LanguageHook.getComponent("dialog.jobs.btn_main_menu", player)));
        }

        builder.button(text(LanguageHook.getComponent("general.btn_close", player)));

        builder.validResultHandler(response -> {
            int idx = response.clickedButtonId();
            if (idx == 0) {
                runSync(() -> openActiveJobDialog(player));
            } else if (idx == 1) {
                runSync(() -> openJobsGuideMenu(player));
            } else if (hasMenu && idx == 2) {
                runSync(() -> returnToMainMenu(player));
            }
        });

        BedrockFormHook.sendForm(player, builder.build());
    }

    public void openJobSelectionMenu(@NotNull Player player) {
        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        JobType currentJob = data.getJobType();

        Component title = LanguageHook.getComponent("dialog.jobs.select_title", player);
        Component body = LanguageHook.getComponent("dialog.jobs.select_body", player);

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(text(title))
                .content(text(body));

        JobType[] jobs = JobType.values();
        for (JobType job : jobs) {
            boolean isCurrent = (currentJob == job);
            builder.button((isCurrent ? "★ " : "") + job.getDisplayName() + "\n§8" + job.getSelfBuffDesc());
        }

        builder.button(text(LanguageHook.getComponent("general.btn_back", player)));

        builder.validResultHandler(response -> {
            int idx = response.clickedButtonId();
            if (idx >= 0 && idx < jobs.length) {
                JobType selected = jobs[idx];
                runSync(() -> openJobDetailsDialog(player, selected));
            } else {
                runSync(() -> openJobsMenu(player));
            }
        });

        BedrockFormHook.sendForm(player, builder.build());
    }

    public void openJobDetailsDialog(@NotNull Player player, @NotNull JobType job) {
        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        boolean isCurrent = (data.getJobType() == job);
        double resetCost = (data.getJobType() != null) ? plugin.getConfigManager().getJobResetCost() : 0.0;

        Component title = LanguageHook.getComponent("dialog.job_details.title", player,
                Placeholder.unparsed("job", job.getDisplayName()));
        Component body = LanguageHook.getComponent("dialog.job_details.body", player,
                Placeholder.unparsed("job", job.getDisplayName()),
                Placeholder.unparsed("description", LanguageHook.getRawString(job.getDescriptionKey(), LanguageHook.getEffectiveLanguageCode(player))),
                Placeholder.unparsed("self_buff", job.getSelfBuffDesc()),
                Placeholder.unparsed("aura_buff", job.getAuraBuffDesc()),
                Placeholder.unparsed("cost", VaultHook.formatMoney(resetCost)),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        if (isCurrent) {
            SimpleForm form = SimpleForm.builder()
                    .title(text(title))
                    .content(text(body))
                    .button(text(LanguageHook.getComponent("general.btn_back", player)))
                    .validResultHandler(response -> runSync(() -> openJobSelectionMenu(player)))
                    .build();
            BedrockFormHook.sendForm(player, form);
            return;
        }

        ModalForm form = ModalForm.builder()
                .title(text(title))
                .content(text(body))
                .button1(text(LanguageHook.getComponent("jobs.btn_select_job", player)))
                .button2(text(LanguageHook.getComponent("general.btn_back", player)))
                .validResultHandler(response -> {
                    if (response.clickedFirst()) {
                        runSync(() -> plugin.getJobsManager().changeJob(player, job));
                    } else {
                        runSync(() -> openJobSelectionMenu(player));
                    }
                })
                .build();

        BedrockFormHook.sendForm(player, form);
    }

    public void openActiveJobDialog(@NotNull Player player) {
        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        JobType job = data.getJobType();
        if (job == null) {
            openJobsMenu(player);
            return;
        }

        int maxLvl = plugin.getConfigManager().getJobMaxLevel();
        long reqExp = plugin.getJobsManager().calculateRequiredExp(data.getLevel());
        double expPercent = reqExp > 0 ? Math.min(100.0, ((double) data.getCurrentExp() / reqExp) * 100.0) : 100.0;
        double resetCost = plugin.getConfigManager().getJobResetCost();

        Component title = LanguageHook.getComponent("dialog.jobs.active_title", player,
                Placeholder.unparsed("job", job.getDisplayName()));
        Component body = LanguageHook.getComponent("dialog.jobs.active_body", player,
                Placeholder.unparsed("job", job.getDisplayName()),
                Placeholder.unparsed("level", String.valueOf(data.getLevel())),
                Placeholder.unparsed("max_level", String.valueOf(maxLvl)),
                Placeholder.unparsed("exp", String.valueOf(data.getCurrentExp())),
                Placeholder.unparsed("req_exp", data.getLevel() >= maxLvl ? "MAX" : String.valueOf(reqExp)),
                Placeholder.unparsed("exp_percent", String.format("%.1f", expPercent)),
                Placeholder.unparsed("description", LanguageHook.getRawString(job.getDescriptionKey(), LanguageHook.getEffectiveLanguageCode(player))),
                Placeholder.unparsed("self_buff", job.getSelfBuffDesc()),
                Placeholder.unparsed("aura_buff", job.getAuraBuffDesc()),
                Placeholder.unparsed("cost", VaultHook.formatMoney(resetCost)),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        SimpleForm form = SimpleForm.builder()
                .title(text(title))
                .content(text(body))
                .button(text(LanguageHook.getComponent("dialog.jobs.btn_quests", player)))
                .button(text(LanguageHook.getComponent("dialog.jobs.btn_resign", player)))
                .button(text(LanguageHook.getComponent("general.btn_back", player)))
                .button(text(LanguageHook.getComponent("general.btn_close", player)))
                .validResultHandler(response -> {
                    switch (response.clickedButtonId()) {
                        case 0 -> runSync(() -> openJobQuestsDialog(player, job));
                        case 1 -> runSync(() -> openJobResignConfirmDialog(player, job));
                        case 2 -> runSync(() -> openJobsMenu(player));
                    }
                })
                .build();

        BedrockFormHook.sendForm(player, form);
    }

    public void openJobQuestsDialog(@NotNull Player player, @NotNull JobType job) {
        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        Component title = LanguageHook.getComponent("dialog.jobs.quests_title", player,
                Placeholder.unparsed("job", job.getDisplayName()));
        Component body = LanguageHook.getComponent("dialog.jobs.quests_body", player,
                Placeholder.unparsed("job", job.getDisplayName()),
                Placeholder.unparsed("level", String.valueOf(data.getLevel())));

        SimpleForm form = SimpleForm.builder()
                .title(text(title))
                .content(text(body))
                .button(text(LanguageHook.getComponent("dialog.jobs.btn_quest_daily", player)))
                .button(text(LanguageHook.getComponent("dialog.jobs.btn_quest_specialist", player)))
                .button(text(LanguageHook.getComponent("dialog.jobs.btn_quest_milestone", player)))
                .button(text(LanguageHook.getComponent("general.btn_back", player)))
                .button(text(LanguageHook.getComponent("general.btn_close", player)))
                .validResultHandler(response -> {
                    String lang = LanguageHook.getEffectiveLanguageCode(player);
                    switch (response.clickedButtonId()) {
                        case 0 -> {
                            String cat = LanguageHook.getRawString("dialog.jobs.btn_quest_daily", lang);
                            runSync(() -> openQuestDetailNotice(player, job, cat != null ? cat : "Misi Harian"));
                        }
                        case 1 -> {
                            String cat = LanguageHook.getRawString("dialog.jobs.btn_quest_specialist", lang);
                            runSync(() -> openQuestDetailNotice(player, job, cat != null ? cat : "Tantangan Spesialis"));
                        }
                        case 2 -> {
                            String cat = LanguageHook.getRawString("dialog.jobs.btn_quest_milestone", lang);
                            runSync(() -> openQuestDetailNotice(player, job, cat != null ? cat : "Milestone Tingkat"));
                        }
                        case 3 -> runSync(() -> openActiveJobDialog(player));
                    }
                })
                .build();

        BedrockFormHook.sendForm(player, form);
    }

    public void openQuestDetailNotice(@NotNull Player player, @NotNull JobType job, @NotNull String categoryName) {
        Component title = LanguageHook.getComponent("dialog.jobs.quest_detail_title", player,
                Placeholder.unparsed("category", categoryName));
        Component body = LanguageHook.getComponent("dialog.jobs.quest_detail_body", player,
                Placeholder.unparsed("category", categoryName),
                Placeholder.unparsed("job", job.getDisplayName()));

        SimpleForm form = SimpleForm.builder()
                .title(text(title))
                .content(text(body))
                .button(text(LanguageHook.getComponent("general.btn_back", player)))
                .validResultHandler(response -> runSync(() -> openJobQuestsDialog(player, job)))
                .build();

        BedrockFormHook.sendForm(player, form);
    }

    public void openJobResignConfirmDialog(@NotNull Player player, @NotNull JobType job) {
        double cost = plugin.getConfigManager().getJobResetCost();
        Component title = LanguageHook.getComponent("dialog.jobs.resign_title", player);
        Component body = LanguageHook.getComponent("dialog.jobs.resign_body", player,
                Placeholder.unparsed("job", job.getDisplayName()),
                Placeholder.unparsed("cost", VaultHook.formatMoney(cost)),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        ModalForm form = ModalForm.builder()
                .title(text(title))
                .content(text(body))
                .button1(text(LanguageHook.getComponent("dialog.jobs.btn_confirm_resign", player)))
                .button2(text(LanguageHook.getComponent("general.btn_cancel", player)))
                .validResultHandler(response -> {
                    if (response.clickedFirst()) {
                        runSync(() -> {
                            boolean ok = plugin.getJobsManager().resignJob(player);
                            if (ok) {
                                openJobsMenu(player);
                            } else {
                                openActiveJobDialog(player);
                            }
                        });
                    } else {
                        runSync(() -> openActiveJobDialog(player));
                    }
                })
                .build();

        BedrockFormHook.sendForm(player, form);
    }

    public void openJobsGuideMenu(@NotNull Player player) {
        Component title = LanguageHook.getComponent("dialog.jobs_guide.title", player);
        Component body = LanguageHook.getComponent("dialog.jobs_guide.body", player,
                Placeholder.unparsed("reset_cost", VaultHook.formatMoney(plugin.getConfigManager().getJobResetCost())));

        SimpleForm form = SimpleForm.builder()
                .title(text(title))
                .content(text(body))
                .button(text(LanguageHook.getComponent("dialog.jobs_guide.btn_all_jobs", player)))
                .button(text(LanguageHook.getComponent("general.btn_back", player)))
                .validResultHandler(response -> {
                    if (response.clickedButtonId() == 0) {
                        runSync(() -> openJobsGuideListDialog(player));
                    } else {
                        runSync(() -> openJobsMenu(player));
                    }
                })
                .build();

        BedrockFormHook.sendForm(player, form);
    }

    public void openJobsGuideListDialog(@NotNull Player player) {
        Component title = LanguageHook.getComponent("dialog.jobs_guide.list_title", player);
        Component body = LanguageHook.getComponent("dialog.jobs_guide.list_body", player);

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(text(title))
                .content(text(body));

        JobType[] jobs = JobType.values();
        for (JobType job : jobs) {
            builder.button(job.getDisplayName() + "\n§8" + job.getSelfBuffDesc());
        }

        builder.button(text(LanguageHook.getComponent("general.btn_back", player)));

        builder.validResultHandler(response -> {
            int idx = response.clickedButtonId();
            if (idx >= 0 && idx < jobs.length) {
                JobType selected = jobs[idx];
                runSync(() -> openJobDetailsDialog(player, selected));
            } else {
                runSync(() -> openJobsGuideMenu(player));
            }
        });

        BedrockFormHook.sendForm(player, builder.build());
    }

    public void returnToMainMenu(@NotNull Player player) {
        // 1. Try ClarityEssentials reflection
        try {
            org.bukkit.plugin.Plugin essentials = org.bukkit.Bukkit.getPluginManager().getPlugin("ClarityEssentials");
            if (essentials != null && essentials.isEnabled()) {
                java.lang.reflect.Method getInstance = essentials.getClass().getMethod("getInstance");
                Object inst = getInstance.invoke(null);
                if (inst != null) {
                    java.lang.reflect.Method getMenuMgr = inst.getClass().getMethod("getMenuDialogManager");
                    Object menuMgr = getMenuMgr.invoke(inst);
                    if (menuMgr != null) {
                        java.lang.reflect.Method openDashboard = menuMgr.getClass().getMethod("openDashboard", Player.class, boolean.class);
                        openDashboard.invoke(menuMgr, player, false);
                        return;
                    }
                }
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {}

        // 2. Fallback command
        String cmd = plugin.getConfigManager().getMainMenuCommand();
        if (cmd != null && !cmd.isBlank()) {
            String cleanCmd = cmd.startsWith("/") ? cmd.substring(1) : cmd;
            try {
                player.performCommand(cleanCmd);
            } catch (Exception ignored) {}
        }
    }

    /* =========================================================================
     * D. PROMOTION RENTAL (BEDROCK CUMULUS)
     * ========================================================================= */

    public void openPromotionDialog(@NotNull Player player) {
        Component title = LanguageHook.getComponent("dialog.promosi.title", player);

        CustomForm form = CustomForm.builder()
                .title(text(title))
                .input("Ad Message (Cost: $" + VaultHook.formatMoney(plugin.getConfigManager().getPromotionCost()) + "):", "Selling items...")
                .validResultHandler(response -> {
                    String msg = response.asInput(0);
                    if (msg == null || msg.trim().isEmpty()) {
                        LanguageHook.sendMessage(player, "promosi.empty_message");
                        return;
                    }
                    runSync(() -> plugin.getPromotionManager().submitPromotion(player, msg.trim()));
                })
                .build();

        BedrockFormHook.sendForm(player, form);
    }
}
