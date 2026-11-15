## 💳 GiftVault Pro v9.2.0 — Hardware & RuPay Edition

### 🌟 What's New in v9.2.0

#### 1. Automatic PNB Imperial RuPay Benefit Concierge & Dual UI
- **Full 6-Category Quarterly Benefit Tracking**: Automated real-time tracking for Lakme Salon/Spa (₹1,500), Myntra/Reliance Shopping (₹500), Decathlon Sports (₹500), BigBasket Groceries (₹250), BookMyShow/OTT (₹200), and Apollo Pharmacy (₹250).
- **Imperial Card Switcher**: Seamlessly toggle between PNB Imperial 1, Imperial 2, and Imperial 3 cards.
- **Dual Visual Surface**:
  - Interactive **Home Screen** summary widget displaying claimed vs. unclaimed quarterly counts and monetary yield.
  - Dedicated **RuPay Benefits** action button on top of the **Vault (Gift Vouchers)** tab opening an authentic iOS Liquid Glass modal sheet with 1-tap voucher view and portal claim links.

#### 2. Multi-Voucher "Checkout Stacker" & Split Remainder Calculator
- **Automated Voucher Optimizer**: Enter your cart total, and GiftVault calculates the best combination of expiring or low-balance vouchers for that brand.
- **Split Payment Deck**: Displays the selected vouchers in a swipeable checkout deck and calculates the exact remainder to be paid via bank card.
- **1-Tap Redemption Deduction**: Automatically updates voucher balances or archives redeemed cards directly from checkout.

#### 3. On-the-Fly Barcode Symbology Switcher & 4-Char Chunk Copy
- **Symbology Switcher**: Switch live between **Code-128**, **QR Code**, **PDF-417**, and **Aztec** symbologies directly on checkout sheets to match whatever cashier scanner is present.
- **4-Character Chunk Blocks**: Long voucher codes formatted into monospace 4-character blocks (`ABCD - 1234 - EFGH`) with 1-tap copy per block or full string.

#### 4. Deep Android Hardware & System Integrations
- **Offline Contactless NFC Tap Reader (`IsoDep`)**: Tap any physical contactless card to the back of your phone to read public EMV card metadata (network, card number, expiry date) offline.
- **Android System Share-Sheet Receiver**: Highlight a voucher code in SMS, WhatsApp, or Gmail and tap **Share to GiftVault** to pre-fill voucher fields instantly.
- **Dynamic Launcher Shortcuts & App Pinning**: Long-press app icon to access top expiring vouchers or pin cards directly to your home screen.
- **Gyroscope-Driven Specular Reflections**: Hardware sensor (`TYPE_ROTATION_VECTOR`) adjusts specular border highlights in real time as you tilt your device, throttled at 30 FPS.
- **Direct Sunlight High-Contrast Override**: Deep contrast borders and obsidian fills for effortless outdoor readability in direct sunlight.

#### 5. Bank Cards & Spend Milestone Progress Rings
- **Circular Milestone Rings**: Animated sweep gradient progress rings illustrating progress toward quarterly spend milestones or annual fee waivers.
- **Custom Finishes & Badges**: Obsidian Metallic, Sapphire Frost, Emerald Glass, Amethyst Specular, and Rose Gold textures with custom badge chips (`Salary`, `Corporate`, `Add-on`).

#### 6. Power-User Settings & Duress Decoy Mode
- **Duress / Decoy Vault Mode**: Secondary unlock PIN opening an isolated, decoy profile holding low-value or demo cards to protect your assets under duress. Progressive lockout timers safeguard against brute-force attacks.
- **Backup Staleness Safeguard**: Tracks mutations since your last `.giftvault` export and displays an unobtrusive warning badge in Settings when your backup needs updating.
- **Per-Field Biometric Step-Up**: Require fingerprint/biometric before copying card numbers or revealing PINs.
- **Notification Quiet Hours**: Scheduled suppression of non-critical expiration alerts during rest hours.

---
**Architected & Designed with Precision by Asthik Bandari**
