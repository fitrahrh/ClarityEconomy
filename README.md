# 💰 ClarityEconomy

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Paper-26.3+-red.svg?style=for-the-badge)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-25-orange.svg?style=for-the-badge)](https://openjdk.org)

**ClarityEconomy** is an all-in-one, modern economy ecosystem custom-engineered for **Clarity Network** (Minecraft 1.21.4+ / Paper 26.3+). Purpose-built to replace outdated chest GUI shops and fragmented economy add-ons, it leverages the **Paper Native Dialog API** (with full **Geyser/Floodgate Bedrock Forms** support) and **Adventure MiniMessage** to deliver a sleek, zero-inventory-glitch user experience.

---

## ✨ Features

### 🖥️ 1. 100% Native Dialogs & Bedrock Forms (Zero Chest GUI)
* **Zero Inventory Hacks**: All interfaces—from shops and quantity pickers to job menus and confirmation screens—render directly using Paper's native client dialogs (`Dialog.create`, `ActionButton`, `DialogInput.text`).
* **Bedrock Cross-Play Compatible**: Seamlessly translates menus into native Floodgate/Geyser `SimpleForm` and `CustomForm` for mobile, console, and Windows Bedrock players.
* **Unified Navigation**: Every dialog includes a clean `<white>` title, intuitive action layout, **Kembali (Back)**, **Tutup (Close)**, and **Kembali ke Menu Utama (Return to Main Menu)** which dynamically links into the **ClarityEssentials** central dashboard.

---

### 🏪 2. Legal Market (`/market`)
* Centralized server commerce center with multi-category browsing:
  * 🧱 **Blocks**: Building materials, stones, woods, and terracotta.
  * 💎 **Minerals**: Raw ores, ingots, diamonds, emeralds, and lapis.
  * 🌾 **Farming & Agriculture**: Crops, seeds, meats, and cooking supplies.
  * 🏹 **Mob Drops**: Bones, gunpowder, string, blaze rods, and rare materials.
  * 🔮 **Miscellaneous**: Enchantment ingredients, utility blocks, and redstone components.
* **Smart Buying & Selling**: Instant calculations based on Vault currency balance and player inventory storage.
* **Flexible Quantity Selector**:
  * Quick preset buttons: `x1`, `x16`, `x32`, `x64`.
  * **Custom Amount Input**: Enter custom quantities directly through a native text prompt dialog (`DialogInput.text`).
* **Protection against overflow**: Automatically calculates remaining inventory space before completing transactions.

---

### 🕶️ 3. Underground Black Market (`/blackmarket`)
* Secret NPC black-market dealer offering restricted high-tier items:
  * Rare and illegal artifacts: Netherite Upgrade Smiths, Totems of Undying, Enchanted Golden Apples, Beacons, Ancient Debris, and customized gear.
* **Dynamic Time Windows & Limits**:
  * Configurable daily operating windows (e.g., 06:00 - 21:00 WIB).
  * Timed cooldowns and per-player daily stock purchase quotas.
* **Police Raid & Freeze Mechanics (`PlayerFreezeListener`)**:
  * Configurable percentage chance to trigger an undercover police raid upon black market purchases.
  * Suspenseful sound effects (sirens, heartbeats), blindness effect, and temporary positional movement freeze.
  * Head rotation (yaw/pitch) remains free for maximum immersion while preventing physical escape.

---

### 🏛️ 4. Daily Auction Arena (`/auction`, `/lelang`)
* Automated daily live bidding arena at scheduled server hours.
* **Arena Teleportation**: Admin-configured arena location (`/ce auction set`) with automated countdown teleportation.
* **Live Arena Bidding**:
  * Players in the arena receive real-time MiniMessage chat announcements for each incoming bid.
  * **Anti-Snipe Protection**: Automatic timer extension if a bid is placed in the final seconds of an auction.
  * Automated delivery of won items directly into the winner's inventory (or safe offline storage).

---

### 💼 5. Dynamic 10-Jobs Profession System (`/jobs`)
ClarityEconomy features an extensive leveling-based jobs progression engine with 10 distinct professions:

| Job | Action & EXP Source | Level 50 Self Buff | Level 50 Aura Buff (Radius: 10m) |
| :--- | :--- | :--- | :--- |
| **⛏️ Miner** | Mining stone, deepslate, and valuable ores | Haste II, Night Vision | Haste I |
| **🪓 Lumberjack** | Chopping down trees and stripping wood logs | Strength I, Haste I | Speed I |
| **🌾 Farmer** | Harvesting fully-grown crops & replanting | Saturation I, Speed I | Saturation I |
| **🏹 Hunter** | Slaying hostile monsters and dungeon bosses | Strength II, Speed I | Strength I |
| **🎣 Fisher** | Catching fish, junk, and rare water treasures | Dolphin's Grace, Water Breathing, Luck I | Luck I, Conduit Power |
| **🧱 Builder** | Placing decorative, construction, and building blocks | Jump Boost II, Saturation I | Jump Boost I |
| **🛠️ Crafter** | Crafting weapons, armor, mechanisms, and tools | Resistance I, Speed I | Resistance I |
| **✨ Enchanter** | Applying enchantments & using grindstones | Regeneration I, Fire Resistance | Regeneration I |
| **🧪 Brewer** | Brewing potions and combining alchemy reagents | Night Vision, Speed II | Speed I |
| **🔥 Smelter** | Smelting ores and cooking food in blast furnaces | Fire Resistance, Resistance I | Fire Resistance |

#### Jobs Progression & Auras:
* **Exponential Progression**: Configurable base EXP and multiplier curve up to **Level 50**.
* **Area-of-Effect Auras (`AuraBuffTask`)**: Level 50 masters passively grant continuous aura buffs to all nearby allies within a 10-block radius.
* **Jobs Dashboard (`/jobs`)**:
  * Displays active job, level, EXP bar, progress percentage, and earnings history.
  * Informative onboarding dialog for players without a job.
  * Job resignation/reset system with configurable Vault coin fee.
  * **Comprehensive Jobs Guide Modal**: Detailed in-game reference dialog explaining mechanics, payouts, and buffs for all 10 jobs.

---

### 📢 6. Promotional Advertising & Quick Commerce (`/promosi`, `/wtb`, `/wts`, `/wta`)
* **Ad Slot Rental System (`/promosi`)**:
  * Players can rent recurring broadcast advertising slots using in-game currency.
  * Automated queue rotation with customizable broadcast intervals (e.g., every 5 minutes).
  * Prominent broadcast notifications accompanied by custom chime audio cues.
* **Instant Quick-Trade Broadcasts**:
  * `/wtb [item/note]` - **Want To Buy**: Solicit sellers for needed materials.
  * `/wts [item/note]` - **Want To Sell**: Advertise goods for sale.
  * `/wta [item/note]` - **Want To Ask / Auction**: Solicit price checks or run quick auctions.
  * **Smart Item Detection**: If a player holds an item in their main hand, the command automatically captures the item's display name and quantity into the broadcast.

---

### 🌐 7. Multi-Language Support (ClarityLangCore Integration)
* Automatically hooks into **ClarityLangCore** for per-player dynamic language switching (`/lang`).
* Bundled language localizations:
  * 🇺🇸 **English** (`en.json`)
  * 🇮🇩 **Indonesian** (`id.json`)
* Clean typographic design using `<white>` un-bolded title text and high-contrast, readable body descriptions.
* Full self-contained fallback for standalone server setups without ClarityLangCore.

---

## 🧩 PlaceholderAPI Expansion

ClarityEconomy registers the `%clarityeconomy_<placeholder>%` expansion identifier:

| Placeholder | Description | Example Output |
| :--- | :--- | :--- |
| `%clarityeconomy_job%` | Technical ID of player's active job | `MINER` |
| `%clarityeconomy_job_display%` | Formatted display name of active job | `Miner` |
| `%clarityeconomy_job_level%` | Current job level (1-50) | `24` |
| `%clarityeconomy_job_exp%` | Current EXP in the current level | `1850` |
| `%clarityeconomy_job_req_exp%` | EXP required to level up | `3200` |
| `%clarityeconomy_job_progress_percent%` | Level progress percentage | `57.8` |
| `%clarityeconomy_balance%` | Raw numeric Vault balance | `125000.0` |
| `%clarityeconomy_balance_formatted%` | Currency-formatted Vault balance | `$125,000` |

---

## 📜 Commands & Permissions

| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/market` | - | Open the Legal Market dialog | `clarityeconomy.market` |
| `/blackmarket` | - | Open the underground Black Market dialog | `clarityeconomy.blackmarket` |
| `/auction` | `/lelang` | Teleport to the daily auction arena | `clarityeconomy.lelang` |
| `/jobs` | - | Open the Jobs & Professions dashboard | `clarityeconomy.jobs` |
| `/promosi` | - | Rent a broadcast advertisement slot | `clarityeconomy.promosi` |
| `/wtb [note]` | - | Quick broadcast: Want To Buy | `clarityeconomy.wtb` |
| `/wts [note]` | - | Quick broadcast: Want To Sell | `clarityeconomy.wtb` |
| `/wta [note]` | - | Quick broadcast: Want To Ask / Auction | `clarityeconomy.wtb` |
| `/ce reload` | - | Reload configuration, shops, and language files | `clarityeconomy.admin` |
| `/ce auction set` | - | Set the daily auction arena spawn location | `clarityeconomy.admin` |
| `/ce job set <p> <j> [lvl]` | - | Set target player's job and level | `clarityeconomy.admin` |
| `/ce job reset <p>` | - | Reset target player's job progression | `clarityeconomy.admin` |

---

## ⚙️ Configuration Files

* **`config.yml`**: General settings, currency formatting, job limits, EXP multipliers, auction timers, and main menu fallback command.
* **`market.yml`**: All categories, items, materials, buy prices, sell prices, and stock limits for the legal market.
* **`blackmarket.yml`**: Secret merchant inventory, high-tier pricing, daily purchase limits, and raid probability.
* **`auction.yml`**: Daily auction schedule, minimum starting bids, bid increments, and reward pool.
* **`lang/`**: Language definition files (`en.json`, `id.json`).

---

## 🔌 Developer API & Custom Events

External plugins can interact directly via Bukkit's standard event bus:

* `JobLevelUpEvent`: Fired when a player advances to a new job level.
* `JobExpGainEvent`: Cancellable event fired when a player earns job experience.
* `BlackMarketBuyEvent`: Fired when a player purchases an item from the black market.
* `PromotionBroadcastEvent`: Cancellable event fired prior to an advertisement broadcast.
* `AuctionWinEvent`: Fired when the winning bidder claims an auction prize.

---

## 📦 Requirements & Installation

1. **Server Platform**: [Paper](https://papermc.io) 26.3+ (Minecraft 1.21.4+).
2. **Java Version**: Java 25.
3. **Core Dependencies**:
   * [Vault](https://www.spigotmc.org/resources/vault.34315/) + Any Vault Economy Provider (e.g. EssentialsX).
4. **Optional Integrations**:
   * **ClarityEssentials**: Integrates `/menu` cross-navigation and ESC pause screen button.
   * **ClarityLangCore**: Dynamically syncs multi-language selections per player.
   * **PlaceholderAPI**: Provides placeholders for scoreboards, tablists, and chat.
   * **Floodgate / Geyser**: Enables native Bedrock UI forms for mobile and console players.

Place `ClarityEconomy-1.0.0.jar` into your server's `plugins/` directory and restart the server.

---

## 📄 License

This project is licensed under the terms of the [GNU General Public License v3.0](LICENSE).
