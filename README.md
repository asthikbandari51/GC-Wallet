# 💳 GiftVault Pro (GC Wallet) — Ultimate Offline Android Gift Card & Voucher Wallet

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/asthikbandari51/GC-Wallet?style=for-the-badge&color=8B5CF6&logo=android)](https://github.com/asthikbandari51/GC-Wallet/releases)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?style=for-the-badge&logo=android)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Liquid%20Glass-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Privacy](https://img.shields.io/badge/Security-100%25%20Offline%20%7C%20Zero%20Cloud-059669?style=for-the-badge&logo=shield)](https://github.com/asthikbandari51/GC-Wallet)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

> **GiftVault Pro** (GC Wallet) is a high-performance, **100% offline-first Android gift card wallet, bank card manager, and voucher tracker** crafted with Jetpack Compose and an **iOS Liquid Glass design system**. It provides complete offline autonomy, zero cloud leakage, multi-voucher checkout stacking, live barcode symbology switching, IsoDep NFC contactless card tap reading, automatic PNB Imperial RuPay benefit tracking, and hardware-independent password-encrypted portable backups.

---

## ⚡ Direct Download

Download the latest ready-to-install Android APK directly from GitHub Releases:

👉 **[Download Latest GiftVault APK (v9.2.0)](https://github.com/asthikbandari51/GC-Wallet/releases/latest)**

---

## 🌟 Key Features

### 💎 1. iOS Liquid Glass Design System & Gyroscope Reflections
- **120 FPS Fluid Rendering**: Bespoke Jetpack Compose glass surfaces with frosted translucent specular highlights, tactile spring physics (`SpringTactile`), and adaptive border gradients.
- **Gyroscope-Driven Specular Reflections**: Hardware sensor (`TYPE_ROTATION_VECTOR`) adjusts specular border highlights in real time as you tilt your device, throttled at 30 FPS to conserve battery.
- **Direct Sunlight High-Contrast Mode**: Max-contrast deep borders and obsidian fills for effortless outdoor readability in direct sunlight.
- **Hardware-Accelerated Window Blur**: Implements `FLAG_BLUR_BEHIND` and custom radius blur on Android 12+ (API 31+) for authentic iOS modal and popup depth.

### 🛡️ 2. 100% Offline Architecture & Zero Cloud Leakage
- **Air-Gapped Private Storage**: All database records and preferences reside strictly within the Android Linux sandbox directory (`/data/data/com.giftvault.app/`).
- **No Third-Party SDKs or Analytics**: Zero tracking, zero telemetry, zero analytics, zero external API dependencies for wallet operation.
- **Dynamic Sandbox Shield (`FLAG_SECURE`)**: Automatically blocks Android task-switcher previews and prevents screenshots and screen recording when Privacy Mode or App Lock is active.
- **Duress / Decoy Vault Mode**: Secondary unlock PIN opening an isolated, decoy profile holding low-value or demo cards to protect your assets under duress. Progressive lockout timers safeguard against brute-force attacks.

### 💳 3. Multi-Voucher "Checkout Stacker" & Split Calculator
- **Automated Voucher Optimizer**: Enter your order total (e.g., ₹2,450), and GiftVault calculates the mathematically optimal combination of expiring or low-balance vouchers for that brand.
- **Split Payment Deck**: Displays the selected vouchers in a rapid swipeable deck, calculates the exact remainder to be paid via bank card, and supports 1-tap balance deductions.
- **On-the-Fly Barcode Symbology Switcher**: Switch live between **Code-128**, **QR Code**, **PDF-417**, and **Aztec** symbologies directly on checkout sheets to match whatever cashier scanner is present.
- **4-Character Chunk Tap-to-Copy**: Long voucher strings formatted into clean 4-character blocks (`ABCD - 1234 - EFGH`) with 1-tap copy per block or full string.

### 🏆 4. PNB Imperial RuPay Select Benefit Concierge
- **Automatic Benefit Mapping**: Automatically identifies and tracks all 6 quarterly benefits across your PNB Imperial 1, Imperial 2, and Imperial 3 cards:
  - 💇 **Lakme Salon / Spa**: ₹1,500 quarterly benefit
  - 🛍️ **Myntra / Reliance Shopping**: ₹500 quarterly benefit
  - 🏃 **Decathlon Sports**: ₹500 quarterly benefit
  - 🛒 **BigBasket Groceries**: ₹250 quarterly benefit
  - 🎬 **BookMyShow / OTT**: ₹200 quarterly benefit
  - 💊 **Apollo Pharmacy**: ₹250 quarterly benefit
- **Dual Visual Presentation**: Real-time summary widget on the **Home** dashboard plus a dedicated **RuPay Perks** action button on the **Vault** tab with an iOS Liquid Glass modal sheet supporting 1-tap voucher view and portal linking.

### 📲 5. Deep Android Hardware & System Integrations
- **Offline Contactless NFC Tap Reader (`IsoDep`)**: Tap your physical RuPay, Visa, or Mastercard to the back of your phone to read public EMV card metadata (network, card number, expiry date) offline.
- **Android System Share-Sheet Receiver**: Highlight a voucher in Gmail, SMS, or WhatsApp and tap **Share to GiftVault** to pre-fill voucher fields without switching apps.
- **Dynamic Launcher Shortcuts & App Pinning**: Long-press app icon to access top expiring vouchers or pin your favorite daily card directly to your launcher home screen.
- **Backup Staleness Safeguard**: Tracks mutations since your last `.giftvault` export and displays an unobtrusive warning badge when your backup needs updating.

### 🏦 6. Bank Cards & Spend Milestone Progress Rings
- **Circular Milestone Rings**: Animated sweep gradient progress rings illustrating progress toward quarterly spend milestones or annual fee waivers.
- **Hierarchical Two-Tier Filtering**: Filter cleanly by Card Type (`All`, `Credit`, `Debit`), with an interactive animated sub-tier for Card Networks (`All Networks`, `RuPay`, `Visa`, `Mastercard`, `Amex`).
- **Custom Finishes & Badges**: Obsidian Metallic, Sapphire Frost, Emerald Glass, Amethyst Specular, and Rose Gold textures with custom badge chips (`Salary`, `Corporate`, `Add-on`).

### 💰 7. Discount & Lifetime Savings Yield Tracker
- Optional "Amount Paid" field on vouchers surfaces lifetime savings metrics combining purchase discounts and claimed RuPay perks.

### 🔐 8. Hardware-Independent Portable Backups
- **Universal Portability**: Password-derived encrypted backups (`PBKDF2WithHmacSHA256` with 65,536 iterations + `AES-256-GCM`) restore seamlessly across devices and major Android OS updates (including Android 14, 15, 16, 17+).

### 🌐 6. Official Brand Balance Check Portals
- Integrated 1-tap launch into official online verification portals with zero manual searching:
  - **Myntra**: Official Gift Card Balance & Add Portal
  - **Reliance Digital**: Gyftr Digital Voucher Balance Checker
  - **Apollo Pharmacy**: ValueDesign Verification Portal
  - **Lakme Salon**: Woohoo Balance Inquiry
  - **Decathlon**: Decathlon Wallet & Gift Card Balance
  - **Kalyan Jewellers**: Official Public Voucher Check

### 📋 7. Smart Clipboard Voucher Detection
- **Passive Background Parser**: Automatically detects copied gift voucher codes, PINs, and monetary amounts from SMS, WhatsApp, or email notifications and prompts a 1-tap ingestion banner.

### 🖼️ 8. Steganographic Photo Vault Hide
- **Plausible Deniability**: Conceal your entire encrypted wallet payload inside an ordinary scenic image in your device's photo gallery (`Pictures/GiftVault`) using 3-bit ARGB LSB pixel encoding (imperceptible with PSNR > 51dB).

---

## 🏗️ Technical Architecture

GiftVault Pro follows modern Android architecture guidelines with strict separation of concerns and Clean Architecture:

```
app/src/main/java/com/giftvault/app/
├── core/
│   ├── backup/          # Portable PBKDF2 + AES-GCM BackupManager & RestoreManager
│   ├── database/        # Room Database v5, DAOs, and Entities
│   ├── security/        # CryptoManager, BiometricAuthManager, SteganographyManager
│   ├── ingestion/       # Smart clipboard ingestion & voucher parsing engine
│   ├── rupay/           # RuPay perk tracker & concierge helper
│   └── theme/           # ThemeManager (Dark, Light, AMOLED, Privacy mode)
├── data/
│   ├── repository/      # CardRepository, BankCardRepository implementations
│   └── mappers/         # Domain <-> Entity bidirectional mappers
├── domain/
│   ├── model/           # GiftCard, BankCard, CardCategory, CardNetwork, CardTier
│   └── usecase/         # Clean business logic use cases
└── ui/
    ├── activity/        # ActivityScreen & audit log
    ├── addcard/         # AddCardStepperScreen with barcode scanner
    ├── backup/          # BackupScreen & BackupViewModel (Liquid Glass)
    ├── bankcards/       # ManageBankCardsScreen & ManageBankCardsViewModel
    ├── carddetails/     # CardDetailsScreen with cashier mode & share
    ├── components/glass/# Custom Liquid Glass design system composables
    ├── home/            # HomeScreen, HomeViewModel & Wallet Insights
    ├── navigation/      # GiftVaultNavGraph & Liquid Glass BottomNavBar
    ├── settings/        # SettingsScreen with Brand Balance Portals & Recycle Bin
    └── vault/           # VaultScreen & LiquidGlassCardMenuPopup
```

### Tech Stack
- **Language**: Kotlin 2.0.0
- **UI Toolkit**: Jetpack Compose (100% declarative UI)
- **Architecture**: MVVM + Clean Architecture + StateFlow
- **Local Database**: Android Room v5 (SQLite) with AES-256 field-level encryption
- **Dependency Injection**: Dagger Hilt
- **Serialization**: Kotlinx Serialization (JSON)
- **Cryptography**: `javax.crypto` (PBKDF2WithHmacSHA256, AES/GCM/NoPadding, AndroidKeyStore)
- **Image & Barcode Processing**: Coil, Google ZXing (Barcode & QR generator/scanner)
- **Target OS**: Android 8.0 (API level 26) through Android 15 / 16 / 17 (API level 35+)

---

## 🚀 Building from Source

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17 or JDK 21
- Android SDK 35 (`targetSdk = 35`, `minSdk = 26`)

### Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/asthikbandari51/GC-Wallet.git
   cd GC-Wallet
   ```
2. Build the debug APK using the Gradle wrapper:
   ```bash
   ./gradlew assembleDebug
   ```
3. The compiled APK will be generated at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
4. Install directly to your connected device:
   ```bash
   ./gradlew installDebug
   ```

---

## 🔒 Security & Privacy Guarantee

- **Zero Cloud Footprint**: GiftVault Pro contains no server backend. No login required, no cloud accounts, and no remote synchronization.
- **Android Sandbox Isolation**: Protected by Android Linux Discretionary Access Control (DAC) and SELinux policies.
- **Exported Component Hardening**: All internal services, receivers, and file providers maintain `android:exported="false"`. `android:allowBackup="false"` is enforced to prevent ADB plain dumps.

---

## 👤 Author & Credits

- **Developer**: [Asthik Bandari](https://github.com/asthikbandari51)
- **Project**: GiftVault Pro (GC Wallet)

---

## 📄 License

This project is open-source and licensed under the [MIT License](LICENSE).
