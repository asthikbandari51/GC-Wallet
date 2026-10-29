# Contributing to GiftVault Pro

Thank you for your interest in contributing to GiftVault Pro (GC Wallet)!

## Code of Conduct
We are committed to providing a welcoming, inclusive, and harassment-free environment for everyone.

## How to Contribute
1. **Fork the Repository**: Create your own feature branch (`git checkout -b feature/amazing-feature`).
2. **Follow Code Conventions**:
   - Write clean, idiomatic Kotlin code following official Kotlin coding standards.
   - Use Jetpack Compose declarative patterns for all UI components.
   - Adhere to the Liquid Glass design guidelines (frosted surfaces, tactile animations, adaptive gradients).
   - Maintain 100% offline isolation: do not introduce external tracking, cloud dependencies, or unencrypted storage.
3. **Test Your Changes**: Verify compilation and runtime integrity:
   ```bash
   ./gradlew test
   ./gradlew assembleDebug
   ```
4. **Submit a Pull Request**: Provide a clear description of the problem solved and changes introduced.

## Security
If you discover a security issue or potential privacy risk, please review our [Security Policy](SECURITY.md) and report it responsibly.
