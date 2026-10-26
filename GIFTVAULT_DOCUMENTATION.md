# 🛡️ GiftVault Pro (Version 9.00.00) — Master Technical & Product Specification

**GiftVault Pro** is an air-gapped, zero-telemetry, bank-grade digital wallet, voucher orchestrator, and card perk management suite natively engineered for Android (Kotlin & Jetpack Compose, targeting API 35 / Android 15) and supported by a standalone desktop companion (`pc-web/`). 

The application is architected from the ground up for **100% offline data sovereignty**, **hardware-backed Keystore encryption**, and high-refresh-rate **120Hz display fluidity** through its **Liquid Glass** material design system.

---

## 🏗️ 1. Complete Architecture & System Topography

```
                    ┌──────────────────────────────────────────────┐
                    │               GiftVault Pro UI               │
                    │   (Jetpack Compose • Liquid Glass Engine)    │
                    └──────────────────────┬───────────────────────┘
                                           │
         ┌─────────────────────────────────┼─────────────────────────────────┐
         ▼                                 ▼                                 ▼
┌──────────────────┐             ┌──────────────────┐             ┌──────────────────┐
│  Home Dashboard  │             │  Voucher Vault   │             │   Bank Cards     │
│  (Hero Balance)  │             │  & Cashier Deck  │             │   & CVV Shield   │
└────────┬─────────┘             └────────┬─────────┘             └────────┬─────────┘
         │                                │                                │
         ▼                                ▼                                ▼
┌──────────────────┐             ┌──────────────────┐             ┌──────────────────┐
│ RuPay Concierge  │             │ ML Kit POS OCR   │             │ Steganography    │
│ & Quota Matrix   │             │ & Thermal Engine │             │ (Bit-Exact LSB)  │
└────────┬─────────┘             └────────┬─────────┘             └────────┬─────────┘
         │                                │                                │
         ▼                                ▼                                ▼
┌──────────────────┐             ┌──────────────────┐             ┌──────────────────┐
│ RuPay Perk Cycle │             │ Recycle Bin &    │             │ Rolling Backup   │
│ Reminder Worker  │             │ Soft-Archive     │             │ & Dedupe Notifs  │
└────────┬─────────┘             └────────┬─────────┘             └────────┬─────────┘
         │                                │                                │
         └────────────────────────────────┼────────────────────────────────┘
                                          │
                                          ▼
                       ┌─────────────────────────────────────┐
                       │      Room SQLite DB (Version 5)     │
                       │    (SQLCipher / AES-256 Keystore)   │
                       └─────────────────────────────────────┘
```

---

## 💎 2. Chronological Log of Updates & Additions

### 🔄 Phase 1: Core Vault & Architecture Modernization
1. **Cloud Sync Purge**: Stripped external dependencies (Google Drive, Outlook/Google Calendars) to establish an air-gapped, zero-leakage security posture.
2. **Native Compose Primitives**: Eliminated heavy third-party animation libraries, standardizing on native `AnimatedVisibility`, `Crossfade`, and `animateContentSize`.
3. **Room Database Foundations**: Built encrypted local persistence with automatic schema migrations.

### 💳 Phase 2: Bank Card Wallet & RuPay Integration
1. **Complete Card Profile Storage**: Secure storage of 16-digit card numbers, CVVs, and expiry dates backed by the Android Keystore (`AndroidKeyStore`).
2. **Network Variant Mapping**:
   - **RuPay**: Classic, Platinum, Select, EKAA
   - **Visa**: Classic, Gold, Platinum, Signature, Infinite
   - **Mastercard**: Classic, Platinum / Titanium, World, World Elite
3. **In-App RuPay Benefit Hub**: Integrated WebView portal for `https://rupayselect.truztee.com/` with a slide-out assistant drawer providing 1-tap credential copy.
4. **Desktop / Windowing Support**: Configured Samsung DeX and Android freeform multi-window modes.

### ⚡ Phase 3: Performance, Presentation & Theme Revamp
1. **120Hz Fluidity & Image Optimization**:
   - `hardwareAccelerated="true"` and `largeHeap="true"` in `AndroidManifest.xml`.
   - Replaced dynamic network image decoders with native resources and asynchronous caching to eliminate frame drops during fast scrolling.
   - Memoized graphic allocations to eliminate garbage collection (GC) pauses during Compose Canvas draws.
2. **Cashier Presentation Redesign**:
   - High-contrast Apple/Google Wallet pass style with vector Code 128 monochrome barcodes.
   - Elevated bottom action buttons by `36.dp` (~1.2 cm) to prevent interference with Android navigation gesture bars.
3. **Card Type & Spend Targets**:
   - Added explicit `CREDIT` vs `DEBIT` selector and visual badges.
   - Quarterly spend target made optional (leaving blank or `0` hides the milestone tracker from the home page).
4. **Visual Card Share Correction**:
   - Removed the semi-transparent grey overlay that previously darkened the bottom half of shared cards.
   - Added user attribution watermark on the card graphic: `"Gift card voucher for '[Brand]' shared by [User Name]"`.
5. **Spendable Balance Rule**: Excluded non-monetary OTT and streaming subscriptions (Netflix, Prime, Hotstar, Gaana) from total balance calculations.
6. **Brand Expansion**: Added brand templates (`googleplay`, `district`, `uber`, updated `bookmyshow1`) along with category fallbacks (`electronics_other`, `shopping_other`, `food_other`, `travel_other`).

### 🚀 Phase 4: Advanced Cryptographic & Offline Persistence
1. **Steganographic Gallery Hide (Plausible Deniability Cold Storage)**:
   - Encrypts vault backups into ordinary 32-bit `ARGB_8888` lossless PNG gallery photos.
   - Uses 3-bit LSB replacement (Red, Green, Blue channels; Alpha left at `0xFF`).
   - 32-byte header with `GVST` magic marker, payload size, and salt ($MSE < 0.5$, $PSNR > 51\text{ dB}$).
2. **RuPay Concierge & Quota Matrix**:
   - `PerkQuotaEntity` and `PerkQuotaDao` tracking quarterly and annual entitlements (Lounge, Spa, Golf, Gym, Health Checkups, OTT).
   - Automated boundary calculator for Indian fiscal/calendar quarters ($Q_1$ to $Q_4$).
   - Direct phone intent to RuPay Dedicated Concierge Desk (`18002667077`) and pre-formatted SMS booking generator.
3. **POS Thermal Receipt OCR Scraper**:
   - Thermal slip pre-processing pipeline: Grayscale ($Y = 0.299R + 0.587G + 0.114B$) and adaptive binarization to clean paper grain and thermal fading.
   - Google ML Kit on-device text recognition with regex extraction for `TOTAL_BILL`, `VOUCHER_DEBIT`, and `REMAINING_BALANCE`.
4. **On-Device Structured Ingestion Engine**: Air-gapped regex parser that extracts raw SMS, WhatsApp, and banking notifications into structured vouchers.
5. **Multi-Card Stacking & Kalyan Gold Goal**:
   - Multi-voucher checkout deck with `HorizontalPager`, display auto-boost (`BRIGHTNESS_OVERRIDE_FULL`), and "Redeemed & Next" auto-advance.
   - Kalyan Gold Stacking Target Calculator with Showroom Stacking Cap Guard (alerts when stacking $> 10$ vouchers per billing invoice limit).
6. **Rolling Auto-Backup**: Periodic WorkManager worker executing every 7 days during device idle/charging states, with FIFO pruning retaining the 3 most recent backups.

### 🌟 Phase 5: Polish, Privacy & Soft-Archive
1. **Expiring Cards Typography Scaling**: Scaled down typography in `GiftCardVisual.kt` when rendered in compact mode (`compactMode = true`) to prevent boundary overflows in Home carousels.
2. **ZK Proof Purge**: Completely removed `ZkProofManager.kt` and its UI dialog/buttons to streamline execution and eliminate unused cryptographic overhead.
3. **Voucher Information 1-Tap Copy Actions**: Dedicated copy buttons in `CardDetailsScreen.kt` for both Card Number and PIN/CVV with instant haptic feedback.
4. **Share GV Refinement**: Purged `SAFE_MASKED` share mode; removed redundant plain text message under visual PNG card image shares.
5. **Privacy Mode Balance Masking**: Full privacy masking across Home Dashboard (`₹ ••••••`), Vault screen (`INR ••••••`), and Bank Cards (`₹•••• / ₹••••`).
6. **Comprehensive Bank Card & Perk Quota Backup/Restore**: Serialized all bank cards, claims, and perk quotas in `BackupManager.kt` and restored them transactionally in `RestoreManager.kt`.
7. **Settings Recycle Bin / Archive**: Soft-archiving support (`status = "archived"`) for vouchers with restore and permanent delete actions.
8. **Settings Bank Card Security**: Toggles for "Mask Bank Card CVV by Default" and "Require Biometrics / PIN for CVV".
9. **Target SDK 35 (Android 15 Compatibility)**: Updated to `targetSdk = 35` and `compileSdk = 35`.

### ⚡ Phase 6: V9.00.00 — Ultimate Performance & Auto Bot Purge
1. **Total Auto Bot System Purge**: Completely deleted `RupayAutoClaimAccessibilityService.kt`, accessibility XML configuration, and permissions.
2. **120 FPS Silk Engine**: Asynchronous cached decoding via `coil.compose.AsyncImage`, in-memory Keystore secret key caching, $O(1)$ decrypted string caching, and off-thread Room entity-to-domain flow decryptions (`.flowOn(Dispatchers.Default)`).
3. **Spendable Balance & Excluded Categories**: Excluded balance categories strictly govern both Home Dashboard and Voucher Vault totals.
4. **Lossless Bit-Exact Steganography**: Hardened decoding options (`inPremultiplied = false`, `inScaled = false`, `Bitmap.Config.ARGB_8888`) guaranteeing bit-exact LSB payload extraction.
5. **RuPay Perk Reminders**: `RuPayPerkReminderWorker.kt` dispatches reminders 14 days, 7 days, and 1 day prior to quota resets.

### 💎 Phase 7: Complete Offers Purge & Liquid Glass Material System (Current Plan Execution)
1. **Complete Offers Purge**:
   - Completely deleted `OffersScreen.kt` and `ui/offers/` directory.
   - Removed `Screen.OFFERS` destination, route, and navigation mapping from `NavGraph.kt`.
   - Removed Offers button from `HomeScreen.kt` Quick Actions.
   - Purged all dead code, unused parameters, and obsolete navigation links across the entire project.
2. **Unified Liquid Glass Material Design System**:
   - Built a comprehensive, iOS-inspired Liquid Glass material component suite in `app/src/main/java/com/giftvault/app/ui/components/glass/`:
     - `LiquidGlassTokens.kt`: Design tokens for Light and Dark modes with translucent surface layers, active border gradients, specular reflection brushes, and physical spring motion specs (`SpringTactile`).
     - `LiquidGlassSurface.kt`: Base translucent material surface with layered vertical gradients, luminous border stroke, and diagonal specular highlight sheen.
     - `LiquidGlassCard.kt`: Interactive card surface featuring physical iOS-style spring compression (`scale = 0.985f`) upon touch interaction.
     - `LiquidGlassButton.kt` & `LiquidGlassIconButton.kt`: Tactile glass buttons with Primary, Secondary, Subtle, and Accent Gold styles with spring physics.
     - `LiquidGlassChip.kt`: Translucent filter and category selector pills with glowing borders when active.
     - `LiquidGlassSearchBar.kt`: Translucent search container with focus border glow, single-line placeholder, and instant clear button.
     - `LiquidGlassSortPopup.kt`: Anchored popover anchored right beside the Sort button.
     - `LiquidGlassDialog.kt`: Modal glass alert and input container.
   - **Zero Live Blur Shaders in LazyLists**: Designed explicitly with multi-stop linear/vertical gradients and 1px luminous edge borders to preserve locked 120 FPS without dropping frames on high-refresh-rate devices.
   - Set `ThemeMode.LIQUID_GLASS` as the default theme mode across the application.
3. **Floating 4-Tab Bottom Navigation Bar**:
   - Redesigned `BottomNavBar.kt` into a floating translucent Liquid Glass capsule (`28.dp` radius) elevated above content.
   - Pill selection indicator that animates smoothly across the 4 primary tabs: **Home**, **Vault**, **Cards**, and **Settings**.
4. **Complete Home Dashboard Revamp**:
   - **Spendable Balance Hero Card**: Prominent visual anchor displaying Total Spendable Balance with a 1-tap privacy eye toggle to instantly mask/unmask monetary figures (`₹ ••••••`).
   - **Header**: Spacious, uncluttered greeting header (`"Hey, Asthik! 👋"`) with glowing avatar halo.
   - **Tactile Quick Actions**: Floating Liquid Glass buttons for **RuPay**, **+ Voucher**, and **Vault**.
   - **Mini Bank Cards Carousel**: Horizontal carousel of linked bank card glass profiles with card type badges.
   - **Compact Urgent Expiring Section**: Compact, perfectly-proportioned voucher cards highlighting vouchers nearing expiry without viewport clipping.
   - **Milestone Tracker & Insight Cards**: Progress indicators for spend targets and quick tips.
5. **Bank Cards Tab Redesign**:
   - Floating `LiquidGlassSearchBar` with single-line translucent placeholder.
   - Horizontal `LiquidGlassChip` filter pills (`All`, `Credit`, `Debit`, `RuPay`, `Visa`, `Mastercard`).
   - Physical card visual component with tap-to-reveal CVV protected by biometric / device PIN authentication.
   - Redesigned grouped Add/Edit modal dialog with frosted fields and validation.
6. **Anchored Sort Popover in Vault**:
   - Replaced the cumbersome `ModalBottomSheet` with `LiquidGlassSortPopup`, anchored directly adjacent to the `[ Sort ↕ ]` button.
   - Quick selection across 6 sorting orders with checkmark indicators and 1-tap dismissal.
7. **Overhauled Notification Engine**:
   - Dedicated notification channels in `NotificationHelper.kt`:
     - `giftvault_expiry_alerts`: Voucher Expiry Reminders
     - `giftvault_voucher_ingestion`: New Voucher Detected
     - `giftvault_perk_cycle`: RuPay Perk Cycle Resets
     - `giftvault_system_backup`: Auto-Backup Status
   - Thread-safe deduplication engine (`ConcurrentHashMap`) preventing duplicate alerts for identical vouchers within 5 minutes (and 24 hours for expiry alerts).
   - Actionable notification buttons ("Add to Vault", "View Voucher", "View Perks").
   - Privacy Mode awareness: Automatically masks financial values and merchant names in notifications when Privacy Mode is active.
8. **Settings Screen Upgrade**:
   - All settings groups wrapped in `LiquidGlassCard`.
   - Settings cards reflect the active glass theme tokens with glowing icons and translucent switch tracks.

### 💎 Phase 7: Liquid Glass Polish Pass (V9.1)
1. **Compact Smart Ingestion**: Converted the clipboard banner into a search-bar-sized 46.dp Liquid Glass pill (`✨ Coupon detected  Amazon • ₹500  +  ✕`), strictly filtered to recognizable vouchers/coupons, with zero reserved vertical space when inactive.
2. **Complete 3-Button Home Quick Actions**: Unified all three quick actions (`[ RuPay ]  [ + Voucher ]  [ Vault ]`) with identical structural widths, heights, and tactile spring animations.
3. **Restored Vault Search & Compact Sort**: Pinned search on left (`weight(1f)`) and compact `[ ↕ Sort ]` pill on right. Replaced bottom sheet with right-anchored, compact `LiquidGlassSortPopup` preserving all 6 sorting modes.
4. **Eliminated Grey Legacy Padding**: Purged all legacy Material `ElevatedCard` containers from `CardDetailsScreen.kt`, establishing a pure 3-layer glass hierarchy (`Screen Background -> Liquid Glass Section -> Content`).
5. **Full System Dark Theme Propagation**: Connected `Theme.kt` to `isSystemInDarkTheme()` dynamically applying `LiquidGlassTokens.dark()` and `DarkColorScheme`, eliminating all unreadable dark-on-dark text across all screens.
6. **Unified Add Flows**:
   - `AddCardStepperScreen.kt`: Compact auto-fill action pill row (`[ Camera ] [ Gallery ] [ Paste ]`), live preview, glass chips for categories/cards, and keyboard-safe sleek Save action.
   - `ManageBankCardsScreen.kt`: Real-time updating physical glass card preview with grouped identity, details, and milestone fields.
7. **Unified Glass Add Button**: Introduced shared `LiquidGlassAddButton` across Home, Bank Cards, and Truztee Claims.
8. **Settings & Modal Modernization**: Migrated all Settings cards, rows, and modal dialogs (Edit Display Name, Expiry Alert Window, Excluded Categories, Recycle Bin) to `LiquidGlassDialog`. Preserved "Asthik Bandari" cursive digital signature seal in About.

---

## 📱 3. Deep Dive: Screen-by-Screen Breakdown

### 🏠 1. Home Dashboard (`HomeScreen.kt`)
- **Header**: Spacious greeting header with avatar halo and privacy mode eye toggle.
- **Spendable Balance Hero Card**: Visual focal point of the app showcasing the total spendable balance (excluding user-configured categories like OTT). Supports 1-tap instant privacy masking.
- **Quick Actions Bar**: Tactile glass action buttons for RuPay Rewards Assistant, + Add Voucher, and Browse Vault.
- **Bank Cards Stack**: Horizontal glass card profiles displaying bank badge, card nickname, and network tier.
- **Milestone Tracker**: Appears dynamically when active bank cards have spend milestones set, tracking quarterly progress.
- **Expiring Soon Carousel**: Displays vouchers expiring within the user-configured alert window with scaled typography for zero template overflow.
- **Vault Insights**: Liquid Glass cards providing quick metrics and privacy tips.

---

### 🗄️ 2. Voucher Vault Screen (`VaultScreen.kt`)
- **Liquid Glass Search & Sort Bar**: Single-line translucent search input paired with an anchored `[ Sort ↕ ]` button.
- **Anchored Sort Popover (`LiquidGlassSortPopup`)**: Directly pops up beside the Sort button with:
  - *Recently Added* (Default)
  - *Value: High to Low*
  - *Value: Low to High*
  - *Expiring Soonest*
  - *Expiring Latest*
  - *Brand Name (A → Z)*
- **Horizontal Category Pills**: Smooth scrolling filter chips (`All`, `Shopping`, `Food`, `Travel`, `Entertainment`, `OTT`, `Archived`).
- **Spendable Balance Banner**: Shows active spendable balance with automatic category exclusion.
- **Kalyan Gold Stacking Target Calculator**:
  - Automatically activates when Kalyan Jewellers vouchers are in the vault.
  - Computes total accumulated value against a ₹50,000 gold goal.
  - Showroom Stacking Cap Guard alerts users if the pool exceeds the 10-voucher showroom invoice limit.
- **Voucher Cards**: `LiquidGlassCard` items featuring 1-tap code copy, brand template artwork, balance, expiry pill, and 3-dot context menu.

---

### 💳 3. Bank Cards Tab (`ManageBankCardsScreen.kt`)
- **Search & Filter Bar**: Translucent search bar with single-line placeholder and category chips.
- **Physical Glass Bank Card Component**:
  - Bank badge and card nickname.
  - 16-digit card number with 1-tap copy.
  - **Tap-to-Reveal CVV**: Masked by default (`•••`). Tapping prompts biometric/PIN authentication when enabled, revealing the CVV for 10 seconds before auto-re-masking.
  - Cardholder name, expiry date, and network badge (RuPay, Visa, Mastercard).
- **Grouped Add / Edit Modal**: Frosted glass dialog with clean, translucent fields and validation.

---

### 🔍 4. Voucher Details Screen (`CardDetailsScreen.kt`)
- **Hero Card Visual**: Large hardware-accelerated visual rendering the brand artwork, balance, masked card code, and expiry.
- **Direct 1-Tap Copy Actions**: Dedicated copy buttons next to Card Number and PIN / CVV with haptic feedback.
- **Action Buttons**:
  - **Cashier**: Launches full-screen cashier pass with Code 128 barcode and elevated buttons.
  - **Reveal / Hide**: Toggles card number and PIN visibility (authenticated via biometrics/PIN if enabled).
  - **Share**: Opens visual PNG card share with user attribution watermark (no redundant text).
  - **Archive / Restore**: Move voucher to Recycle Bin or restore to active vault.
  - **Find Store / Redeem App**: Launches Google Maps for physical retailers or merchant apps/websites for online brands.
- **Balance Management**: Log partial debits or balance top-ups with notes.
- **Receipts Section**: Attach, inspect, and delete physical thermal receipt photos.
- **Transaction History**: Chronological log of all redemption and reload activities.

---

### 🛍️ 5. Cashier Presentation Modes
- **Single Voucher Presentation (`CashierPresentationDialog.kt`)**:
  - Deep obsidian container with rounded white pass.
  - Vector-drawn Code 128 monochrome barcode.
  - Large monospace code, separate PIN pill, and available balance badge.
  - Action buttons elevated by `36.dp` above bottom bezel to prevent gesture bar collisions.
- **Multi-Card Deck (`MultiCardPresentationDialog.kt`)**:
  - `HorizontalPager` swipe deck for multi-voucher checkout.
  - Automatically sets window brightness to maximum (`BRIGHTNESS_OVERRIDE_FULL`), restoring previous brightness upon dismissal.
  - "Redeemed & Next" button to debit the current voucher and slide to the next in one tap.

---

### 🌐 6. RuPay Benefit Portal (`RuPayRedeemScreen.kt`)
- Integrated WebView for `https://rupayselect.truztee.com/`.
- Assistant drawer providing 1-tap copy of primary card number, expiry, and CVV.
- Dedicated Concierge Desk button (`tel:18002667077`).
- Zero background accessibility services or intrusive prompts—safe, direct, and private.

---

### ⚙️ 7. Settings Screen (`SettingsScreen.kt`)
- **Profile & Personalization**: Customize user display name shown across greetings, shares, and visual card watermarks.
- **Appearance**: Toggle between standard Light Mode and iOS Liquid Glass theme.
- **Security & Privacy**:
  - Privacy Mode (Mask Balances).
  - App Lock (Biometrics / PIN).
  - Mask Bank Card CVV by Default.
  - Require Biometrics / PIN to Reveal CVV.
- **Balance & Expiry Rules**:
  - Excluded Balance Categories selector (multi-select dialog). Governs both Home and Vault spendable totals.
  - Expiring Soon Alert Window selector (7, 15, 30, 60 days).
- **Data & Backups**:
  - Password-protected `.giftvault` export and import.
  - Steganography Gallery Hide & Restore (lossless 32-bit ARGB_8888 bit-exact LSB).
  - **Recycle Bin / Archived Vouchers**: View archived vouchers, restore, or permanently delete.
- **About Section**:
  - Version identifier (`GiftVault Pro V9.00.00`).
  - Cryptographic verification badge.
  - Asthik Bandari's centered cursive digital signature and precision credit.

---

## 🔒 4. Security & Cryptographic Architecture

| Layer | Implementation Details |
| :--- | :--- |
| **Data-at-Rest** | SQLite Room Database (`gift_vault_database`, version 5) with encrypted columns. |
| **Keystore Key Derivation** | Hardware-backed Master Key in `AndroidKeyStore` (`AES/GCM/NoPadding`, 256-bit). |
| **Backup Archive Encryption** | AES-256-GCM authenticated encryption with PBKDF2WithHmacSHA256 (65,536 iterations), 16-byte random salt, and 12-byte IV. |
| **Steganographic Cold Storage** | Lossless 32-bit ARGB_8888 PNG with 3-bit LSB injection (R, G, B channels) and 32-byte binary header (`GVST`). |
| **Memory Sanitation** | Sensitive card numbers and CVVs decrypted in memory are stored in volatile character arrays and cleared via `Arrays.fill(..., '0')` immediately after use. |
| **CVV Biometric Gate** | BiometricPrompt authentication barrier guarding CVV decryption and copy operations. |
| **OS Isolation** | `android:allowBackup="false"` and strict `android:exported="false"` on FileProvider to eliminate IPC and backup leaks. |

---

## 🖥️ 5. Companion Web Suite (`pc-web/`)

Located in `c:\Users\asthi\OneDrive\Desktop\GC Wallet\pc-web\`:
- Zero-server desktop web application built with HTML5, Tailwind CSS, and Lucide icons.
- Parity with Android bank cards, voucher vault, and spending metrics.
- SMS and email transactional text parser for quick voucher extraction.
- Truztee and RuPay dispute escalation email generator.
- Encrypted local backup export and import compatible with the Android `.giftvault` format.

---

## 📦 6. Verification & Artifacts
- **Current Version Code**: `90000`
- **Current Version Name**: `"9.00.00"`
- **Target Android Version**: API 35 (Android 15)
- **Minimum Android Version**: API 26 (Android 8.0 Oreo)
- **Compiled Debug APK**: `app/build/outputs/apk/debug/app-debug.apk` (Size: ~116 MB)
- **Theme**: Liquid Glass (Default) & Clean Light
- **Room DB Version**: `5`
