# Security Policy — GiftVault Pro

## Security Architecture & Philosophy

GiftVault Pro is built from the ground up on the principle of **Zero Trust & Zero Cloud Leakage**. 

### 1. 100% Offline-First
GiftVault Pro has no internet servers, no analytics beacons, and no tracking libraries. All data stays strictly local on your device.

### 2. Device Sandboxing
The application utilizes Android's Linux user-isolation sandbox. Data written to `/data/data/com.giftvault.app/` is owned solely by the application's unique Linux UID (`u0_a...`) with mode `0600` (read/write only by GiftVault Pro).

### 3. Hardware-Independent Portable Backups
Exported `.giftvault` backups are encrypted with a key derived from your personal password using:
- **KDF**: `PBKDF2WithHmacSHA256`
- **Iterations**: 65,536 rounds
- **Salt**: 16-byte cryptographically secure random salt
- **Cipher**: `AES-256-GCM` with a 12-byte random IV and 128-bit authentication tag

### 4. Dynamic Screen & Task Switcher Shielding (`FLAG_SECURE`)
When Privacy Mode is enabled or the application is locked, `FLAG_SECURE` is dynamically set on the window to prevent:
- Screen recording by third-party apps
- Unintended screenshot capture
- Thumbnail previews in the Android Recent Apps / Task Switcher

### Reporting a Vulnerability

If you discover any security vulnerabilities or privacy concerns, please open a private report or contact the developer directly at [asthikbandari51@gmail.com](mailto:asthikbandari51@gmail.com).
