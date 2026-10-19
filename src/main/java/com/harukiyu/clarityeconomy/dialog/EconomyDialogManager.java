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
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EconomyDialogManager {

    public static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
            .uses(-1)
            .lifetime(Duration.ofDays(365))
            .build();

    private final ClarityEconomy plugin;

    public EconomyDialogManager(@NotNull ClarityEconomy plugin) {
        this.plugin = plugin;
    }

    /* =========================================================================
     * A. LEGAL MARKET DIALOGS
     * ========================================================================= */

    public void openMarketMenu(@NotNull Player player) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openMarketMenu(player);
            return;
        }

        Component title = LanguageHook.getComponent("dialog.market.title", player);
        Component body = LanguageHook.getComponent("dialog.market.body", player,
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        List<ActionButton> buttons = new ArrayList<>();
        for (ShopCategory category : plugin.getMarketManager().getCategories()) {
            Component btnLabel = Component.text(category.getName());
            Component tooltip = Component.text(category.getDescription());

            buttons.add(ActionButton.builder(btnLabel)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            openCategoryMenu(p, category);
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) p.closeDialog();
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void openCategoryMenu(@NotNull Player player, @NotNull ShopCategory category) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openCategoryMenu(player, category);
            return;
        }

        Component title = LanguageHook.getComponent("dialog.category.title", player,
                Placeholder.unparsed("category", category.getName()));
        Component body = LanguageHook.getComponent("dialog.category.body", player,
                Placeholder.unparsed("category", category.getName()),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        List<ActionButton> buttons = new ArrayList<>();
        for (ShopItem item : category.getItems()) {
            String name = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
            String priceDesc = (item.canBuy() ? "B: $" + VaultHook.formatMoney(item.getBuyPrice()) : "") +
                    (item.canBuy() && item.canSell() ? " | " : "") +
                    (item.canSell() ? "S: $" + VaultHook.formatMoney(item.getSellPrice()) : "");

            Component btnLabel = Component.text(name);
            Component tooltip = Component.text(priceDesc + (item.getDescription() != null ? " - " + item.getDescription() : ""));

            buttons.add(ActionButton.builder(btnLabel)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            openItemActionMenu(p, category, item);
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openMarketMenu(p);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void openItemActionMenu(@NotNull Player player, @NotNull ShopCategory category, @NotNull ShopItem item) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openItemActionMenu(player, category, item);
            return;
        }

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

        List<ActionButton> buttons = new ArrayList<>();

        if (item.canBuy()) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("market.btn_buy", player))
                    .tooltip(Component.text("Purchase " + itemName))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            openBuyQuantityDialog(p, category, item, false, item.getBuyPrice());
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        if (item.canSell()) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("market.btn_sell", player))
                    .tooltip(Component.text("Sell " + itemName))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            openSellQuantityDialog(p, category, item, item.getSellPrice());
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openCategoryMenu(p, category);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void openBuyQuantityDialog(@NotNull Player player,
                                      @Nullable ShopCategory category,
                                      @NotNull ShopItem item,
                                      boolean isBlackMarket,
                                      double unitPrice) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openBuyQuantityDialog(player, category, item, isBlackMarket, unitPrice);
            return;
        }

        String itemName = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
        Component title = LanguageHook.getComponent("dialog.buy_input.title", player,
                Placeholder.unparsed("item", itemName));
        Component body = LanguageHook.getComponent("dialog.buy_input.body", player,
                Placeholder.unparsed("item", itemName),
                Placeholder.unparsed("unit_price", VaultHook.formatMoney(unitPrice)),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        TextDialogInput quantityInput = DialogInput.text("amount_value", LanguageHook.getComponent("dialog.input.quantity_label", player))
                .initial("1")
                .maxLength(4)
                .build();

        ActionButton confirmBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        String input = view.getText("amount_value");
                        int amount;
                        try {
                            amount = Integer.parseInt(input);
                            if (amount <= 0) throw new NumberFormatException();
                        } catch (NumberFormatException e) {
                            LanguageHook.sendMessage(p, "market.invalid_quantity");
                            return;
                        }
                        openBuyConfirmDialog(p, category, item, amount, isBlackMarket, unitPrice);
                    }
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton cancelBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_cancel", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        if (isBlackMarket) {
                            openBlackMarketMenu(p);
                        } else if (category != null) {
                            openItemActionMenu(p, category, item);
                        } else {
                            p.closeDialog();
                        }
                    }
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(quantityInput))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(dialog);
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

        ActionButton yesBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        if (isBlackMarket) {
                            BlackMarketManager bm = plugin.getBlackMarketManager();
                            bm.applyCooldown(p);
                            bm.setFrozen(p, false);
                        }
                        plugin.getMarketManager().processBuy(p, item, amount, isBlackMarket, unitPrice);
                    }
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton noBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_cancel", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        if (isBlackMarket) {
                            openBlackMarketMenu(p);
                        } else if (category != null) {
                            openItemActionMenu(p, category, item);
                        } else {
                            p.closeDialog();
                        }
                    }
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(yesBtn, noBtn))
        );

        player.showDialog(dialog);
    }

    public void openSellQuantityDialog(@NotNull Player player,
                                       @NotNull ShopCategory category,
                                       @NotNull ShopItem item,
                                       double unitPrice) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openSellQuantityDialog(player, category, item, unitPrice);
            return;
        }

        String itemName = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());
        int have = Utils.countItem(player, item.getMaterial());

        Component title = LanguageHook.getComponent("dialog.sell_input.title", player,
                Placeholder.unparsed("item", itemName));
        Component body = LanguageHook.getComponent("dialog.sell_input.body", player,
                Placeholder.unparsed("item", itemName),
                Placeholder.unparsed("unit_payout", VaultHook.formatMoney(unitPrice)),
                Placeholder.unparsed("inv_count", String.valueOf(have)));

        TextDialogInput quantityInput = DialogInput.text("amount_value", LanguageHook.getComponent("dialog.input.quantity_label", player))
                .initial(String.valueOf(Math.max(1, have)))
                .maxLength(4)
                .build();

        ActionButton confirmBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        String input = view.getText("amount_value");
                        int amount;
                        try {
                            amount = Integer.parseInt(input);
                            if (amount <= 0) throw new NumberFormatException();
                        } catch (NumberFormatException e) {
                            LanguageHook.sendMessage(p, "market.invalid_quantity");
                            return;
                        }
                        openSellConfirmDialog(p, category, item, amount, unitPrice);
                    }
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton cancelBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_cancel", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openItemActionMenu(p, category, item);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(quantityInput))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(dialog);
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

        ActionButton yesBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        plugin.getMarketManager().processSell(p, item, amount, unitPrice);
                    }
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton noBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_cancel", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openItemActionMenu(p, category, item);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(yesBtn, noBtn))
        );

        player.showDialog(dialog);
    }

    /* =========================================================================
     * B. BLACK MARKET DIALOGS
     * ========================================================================= */

    public void openBlackMarketMenu(@NotNull Player player) {
        BlackMarketManager bm = plugin.getBlackMarketManager();

        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openBlackMarketMenu(player);
            return;
        }

        if (!bm.isOpen()) {
            bm.setFrozen(player, false);
            Component title = LanguageHook.getComponent("dialog.blackmarket.closed_title", player);
            Component body = LanguageHook.getComponent("blackmarket.closed", player,
                    Placeholder.unparsed("schedule", bm.getScheduleDisplay()),
                    Placeholder.unparsed("remaining", bm.getTimeRemainingFormatted()));

            ActionButton okBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_close", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) p.closeDialog();
                    }, CALLBACK_OPTIONS))
                    .build();

            Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(title)
                            .body(List.of(DialogBody.plainMessage(body)))
                            .canCloseWithEscape(true)
                            .build())
                    .type(DialogType.notice(okBtn))
            );
            player.showDialog(dialog);
            return;
        }

        // Apply Freeze while Black Market dialog is active
        bm.setFrozen(player, true);

        Component title = LanguageHook.getComponent("dialog.blackmarket.title", player);
        Component body = LanguageHook.getComponent("dialog.blackmarket.body", player,
                Placeholder.unparsed("remaining", bm.getTimeRemainingFormatted()),
                Placeholder.unparsed("discount", "80% (5x)"),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        List<ActionButton> buttons = new ArrayList<>();
        for (ShopItem item : bm.getItems()) {
            double discounted = bm.getDiscountedPrice(item.getBuyPrice());
            String name = item.getName() != null ? item.getName() : Utils.formatMaterialName(item.getMaterial());

            Component btnLabel = Component.text(name + " ($" + VaultHook.formatMoney(discounted) + ")");
            Component tooltip = Component.text((item.getDescription() != null ? item.getDescription() + " - " : "") +
                    "Normal: $" + VaultHook.formatMoney(item.getBuyPrice()) + " -> Deal: $" + VaultHook.formatMoney(discounted));

            buttons.add(ActionButton.builder(btnLabel)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            if (!bm.checkCooldown(p)) {
                                long remSec = bm.getRemainingCooldownSeconds(p);
                                LanguageHook.sendMessage(p, "blackmarket.cooldown",
                                        Placeholder.unparsed("seconds", String.valueOf(remSec)));
                                return;
                            }
                            openBuyQuantityDialog(p, null, item, true, discounted);
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        bm.setFrozen(p, false);
                        p.closeDialog();
                    }
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
        );

        player.showDialog(dialog);
    }

    /* =========================================================================
     * C. JOBS DASHBOARD DIALOGS
     * ========================================================================= */

    public void openJobsMenu(@NotNull Player player) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openJobsMenu(player);
            return;
        }

        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        JobType currentJob = data.getJobType();

        // SCENARIO A: Player has NOT chosen any profession yet
        if (currentJob == null) {
            Component title = LanguageHook.getComponent("dialog.jobs.no_job_title", player);
            Component body = LanguageHook.getComponent("dialog.jobs.no_job_body", player);

            List<ActionButton> buttons = new ArrayList<>();

            // 1. Choose Job Now Button
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.jobs.btn_choose_now", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openJobSelectionMenu(p);
                    }, CALLBACK_OPTIONS))
                    .build());

            // 2. Guide / Tips Button
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.jobs.btn_guide", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openJobsGuideMenu(p);
                    }, CALLBACK_OPTIONS))
                    .build());

            // 3. Back to Main Menu Button
            if (plugin.getConfigManager().isEnableBackToMainMenu()) {
                buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.jobs.btn_main_menu", player))
                        .action(DialogAction.customClick((view, audience) -> {
                            if (audience instanceof Player p) returnToMainMenu(p);
                        }, CALLBACK_OPTIONS))
                        .build());
            }

            // Close Button
            ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_close", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) Utils.forceCloseScreen(p);
                    }, CALLBACK_OPTIONS))
                    .build();

            Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(title)
                            .body(List.of(DialogBody.plainMessage(body)))
                            .canCloseWithEscape(true)
                            .pause(false)
                            .afterAction(DialogBase.DialogAfterAction.NONE)
                            .build())
                    .type(DialogType.multiAction(buttons, exitBtn, 1))
            );

            player.showDialog(dialog);
            return;
        }

        // SCENARIO B: Player ALREADY has a profession
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

        List<ActionButton> buttons = new ArrayList<>();

        // 1. Switch / Change Job Button
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.jobs.btn_change_job", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openJobSelectionMenu(p);
                }, CALLBACK_OPTIONS))
                .build());

        // 2. Guide / Tips Button
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.jobs.btn_guide", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openJobsGuideMenu(p);
                }, CALLBACK_OPTIONS))
                .build());

        // 3. Back to Main Menu Button
        if (plugin.getConfigManager().isEnableBackToMainMenu()) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.jobs.btn_main_menu", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) returnToMainMenu(p);
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        // Close Button
        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) Utils.forceCloseScreen(p);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 1))
        );

        player.showDialog(dialog);
    }

    public void openJobSelectionMenu(@NotNull Player player) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openJobSelectionMenu(player);
            return;
        }

        PlayerJobData data = plugin.getJobsManager().getPlayerData(player.getUniqueId());
        JobType currentJob = data.getJobType();

        Component title = LanguageHook.getComponent("dialog.jobs.select_title", player);
        Component body = LanguageHook.getComponent("dialog.jobs.select_body", player);

        List<ActionButton> buttons = new ArrayList<>();
        for (JobType job : JobType.values()) {
            boolean isCurrent = (currentJob == job);
            Component btnLabel = Component.text((isCurrent ? "★ " : "") + job.getDisplayName());
            Component tooltip = Component.text(job.getSelfBuffDesc() + " | Aura: " + job.getAuraBuffDesc());

            buttons.add(ActionButton.builder(btnLabel)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            openJobDetailsDialog(p, job);
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openJobsMenu(p);
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) Utils.forceCloseScreen(p);
                }, CALLBACK_OPTIONS))
                .build();

        buttons.add(backBtn);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void openJobDetailsDialog(@NotNull Player player, @NotNull JobType job) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openJobDetailsDialog(player, job);
            return;
        }

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
            ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_back", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openJobSelectionMenu(p);
                    }, CALLBACK_OPTIONS))
                    .build();

            Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(title)
                            .body(List.of(DialogBody.plainMessage(body)))
                            .canCloseWithEscape(true)
                            .pause(false)
                            .afterAction(DialogBase.DialogAfterAction.NONE)
                            .build())
                    .type(DialogType.notice(backBtn))
            );
            player.showDialog(dialog);
            return;
        }

        ActionButton selectBtn = ActionButton.builder(LanguageHook.getComponent("jobs.btn_select_job", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        openJobChangeConfirmDialog(p, job, resetCost);
                    }
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openJobSelectionMenu(p);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .build())
                .type(DialogType.confirmation(selectBtn, backBtn))
        );

        player.showDialog(dialog);
    }

    public void openJobChangeConfirmDialog(@NotNull Player player, @NotNull JobType job, double cost) {
        Component title = LanguageHook.getComponent("dialog.job_confirm.title", player);
        Component body = LanguageHook.getComponent("dialog.job_confirm.body", player,
                Placeholder.unparsed("job", job.getDisplayName()),
                Placeholder.unparsed("cost", VaultHook.formatMoney(cost)),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        ActionButton yesBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        Utils.forceCloseScreen(p);
                        plugin.getJobsManager().changeJob(p, job);
                    }
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton noBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_cancel", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openJobDetailsDialog(p, job);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .build())
                .type(DialogType.confirmation(yesBtn, noBtn))
        );

        player.showDialog(dialog);
    }

    public void openJobsGuideMenu(@NotNull Player player) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openJobsGuideMenu(player);
            return;
        }

        Component title = LanguageHook.getComponent("dialog.jobs_guide.title", player);
        Component body = LanguageHook.getComponent("dialog.jobs_guide.body", player,
                Placeholder.unparsed("reset_cost", VaultHook.formatMoney(plugin.getConfigManager().getJobResetCost())));

        List<ActionButton> buttons = new ArrayList<>();

        // View All 10 Jobs & Buffs Button
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.jobs_guide.btn_all_jobs", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openJobsGuideListDialog(p);
                }, CALLBACK_OPTIONS))
                .build());

        // Back Button
        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openJobsMenu(p);
                }, CALLBACK_OPTIONS))
                .build();
        buttons.add(backBtn);

        // Close Button
        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) Utils.forceCloseScreen(p);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void openJobsGuideListDialog(@NotNull Player player) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openJobsGuideListDialog(player);
            return;
        }

        Component title = LanguageHook.getComponent("dialog.jobs_guide.list_title", player);
        Component body = LanguageHook.getComponent("dialog.jobs_guide.list_body", player);

        List<ActionButton> buttons = new ArrayList<>();
        for (JobType job : JobType.values()) {
            Component btnLabel = Component.text(job.getDisplayName());
            Component tooltip = Component.text(job.getSelfBuffDesc() + " | Aura: " + job.getAuraBuffDesc());

            buttons.add(ActionButton.builder(btnLabel)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openJobDetailsDialog(p, job);
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openJobsGuideMenu(p);
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) Utils.forceCloseScreen(p);
                }, CALLBACK_OPTIONS))
                .build();

        buttons.add(backBtn);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void returnToMainMenu(@NotNull Player player) {
        // 1. Try to open ClarityEssentials dashboard directly via reflection
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

        // 2. Fallback: execute configured menu command (e.g. /menu)
        String cmd = plugin.getConfigManager().getMainMenuCommand();
        if (cmd != null && !cmd.isBlank()) {
            String cleanCmd = cmd.startsWith("/") ? cmd.substring(1) : cmd;
            Utils.forceCloseScreen(player);
            Utils.executeSafeCommand(player, cleanCmd);
        } else {
            Utils.forceCloseScreen(player);
        }
    }

    /* =========================================================================
     * D. PROMOTION RENTAL DIALOG
     * ========================================================================= */

    public void openPromotionDialog(@NotNull Player player) {
        if (BedrockFormHook.isBedrockPlayer(player)) {
            plugin.getBedrockMenuManager().openPromotionDialog(player);
            return;
        }

        Component title = LanguageHook.getComponent("dialog.promosi.title", player);
        Component body = LanguageHook.getComponent("dialog.promosi.body", player,
                Placeholder.unparsed("cost", VaultHook.formatMoney(plugin.getConfigManager().getPromotionCost())),
                Placeholder.unparsed("duration", String.valueOf(plugin.getConfigManager().getPromotionDurationMinutes())),
                Placeholder.unparsed("active_count", String.valueOf(plugin.getPromotionManager().getActivePromotionsCount())),
                Placeholder.unparsed("max_slots", String.valueOf(plugin.getConfigManager().getPromotionMaxSlots())),
                Placeholder.unparsed("balance", VaultHook.formatMoney(VaultHook.getBalance(player))));

        TextDialogInput msgInput = DialogInput.text("promo_text", LanguageHook.getComponent("dialog.input.promo_label", player))
                .initial("")
                .maxLength(120)
                .build();

        ActionButton confirmBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        String text = view.getText("promo_text");
                        if (text == null || text.trim().isEmpty()) {
                            LanguageHook.sendMessage(p, "promosi.empty_message");
                            return;
                        }
                        p.closeDialog();
                        plugin.getPromotionManager().submitPromotion(p, text.trim());
                    }
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton cancelBtn = ActionButton.builder(LanguageHook.getComponent("general.btn_cancel", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) p.closeDialog();
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(msgInput))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(dialog);
    }
}
