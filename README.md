# 💳 GiftVault Pro (GC Wallet) — Ultimate Offline Android Gift Card & Voucher Wallet

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/asthikbandari51/GC-Wallet?style=for-the-badge&color=8B5CF6&logo=android)](https://github.com/asthikbandari51/GC-Wallet/releases)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?style=for-the-badge&logo=android)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Liquid%20Glass-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Privacy](https://img.shields.io/badge/Security-100%25%20Offline%20%7C%20Zero%20Cloud-059669?style=for-the-badge&logo=shield)](https://github.com/asthikbandari51/GC-Wallet)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

> **GiftVault Pro** (GC Wallet) is a high-performance, **100% offline-first Android gift card wallet, bank card manager, and voucher tracker** crafted with Jetpack Compose and an **iOS Liquid Glass design system**. It provides complete offline autonomy, zero cloud leakage, dynamic cashier barcode generation, RuPay lounge perk tracking, and hardware-independent password-encrypted portable backups.

---

## ⚡ Direct Download

Download the latest ready-to-install Android APK directly from GitHub Releases:

👉 **[Download Latest GiftVault APK (v9.1.0)](https://github.com/asthikbandari51/GC-Wallet/releases/latest)**

---

## 🌟 Key Features

### 💎 1. iOS Liquid Glass Design System
- **120 FPS Fluid Rendering**: Bespoke Jetpack Compose glass surfaces with frosted translucent specular highlights, tactile spring physics (`SpringTactile`), and adaptive border gradients.
- **Hardware-Accelerated Window Blur**: Implements `FLAG_BLUR_BEHIND` and custom radius blur on Android 12+ (API 31+) for authentic iOS modal and popup depth.
- **Adaptive Dark & Light Themes**: Seamlessly transitions between Obsidian Midnight glass and Frosted Crystal light mode.

### 🛡️ 2. 100% Offline Architecture & Zero Cloud Leakage
- **Air-Gapped Private Storage**: All database records and preferences reside strictly within the Android Linux sandbox directory (`/data/data/com.giftvault.app/`).
- **No Third-Party SDKs or Analytics**: Zero tracking, zero telemetry, zero analytics, zero external API dependencies for wallet operation.
- **Dynamic Sandbox Shield (`FLAG_SECURE`)**: Automatically blocks Android task-switcher previews and prevents screenshots and screen recording when Privacy Mode or App Lock is active.

### 🔐 3. Hardware-Independent Portable Backups
- **No Device Lockout**: Unlike standard hardware keystore approaches that break after hard resets or device transfers, GiftVault backups use **device-independent password derivation** (`PBKDF2WithHmacSHA256` with 65,536 iterations + `AES-256-GCM`).
- **Universal Portability**: Easily restore your entire wallet across hard resets, Android OS updates (including Android 14, 15, 16, and 17+), or when migrating to a new smartphone.
- **Interactive Restore Flow**: Choose your `.giftvault` file first, then enter your personal backup password in a secure Liquid Glass prompt.

### 💳 4. Universal Gift Card Vault & Cashier Deck
- **Instant High-Contrast Barcodes**: Automatically renders crisp Code128, QR, and Aztec codes with auto-brightness enhancement for rapid scanning at checkout registers.
- **Dynamic Brand Theming**: Built-in template presets for Myntra, Amazon Pay, Reliance Digital, Flipkart, Decathlon, Lakme Salon, Kalyan Jewellers, Uber, BookMyShow, BigBasket, and more.
- **Card Lifecycle Management**: Track balances, expiration alerts, partial redemptions, transaction history, notes, and archived vouchers with an integrated Recycle Bin.

### 🏦 5. Bank Cards & RuPay Concierge Engine
- **Hierarchical Two-Tier Filtering**: Filter cleanly by Card Type (`All`, `Credit`, `Debit`), with an interactive animated sub-tier for Card Networks (`All Networks`, `RuPay`, `Visa`, `Mastercard`, `Amex`).
- **RuPay Lounge & Perk Quotas**: Dedicated tracking for RuPay Select, Platinum, and Classic quarterly lounge visit quotas, wellness benefits, and golf sessions.
- **Quarterly Spend Progress**: Real-time progress bars and milestone indicators to ensure you never miss credit card fee waivers or milestone vouchers.

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
