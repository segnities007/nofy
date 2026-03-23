# Nofy

**Local-first, encrypted notes for Android.**  
Password-protected vault, SQLCipher-backed Room, field-level AES-GCM, optional strong biometric unlock, and on-device security hardening.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)

---

## Why Nofy

- **Privacy**: Data stays on the device by default. `INTERNET` is declared for **local Wi‑Fi vault transfer** (TCP on your LAN); the app does not use cloud sync.
- **Encryption**: SQLCipher for the database file; note bodies encrypted with a password-bound session key; sensitive prefs wrapped with Keystore-backed AES-GCM.
- **Modern stack**: Jetpack Compose, Navigation 3, Coroutines / Flow, Room + KSP, Koin.

---

## Requirements

| Item | Version |
|------|---------|
| JDK | 11+ (project targets Java 11 bytecode) |
| Android SDK | compileSdk **36** |
| Min device | **API 26** |

---

## Quick start

```bash
git clone <repository-url>
cd nofy
./gradlew :app:assembleDebug
```

Install the generated APK from `app/build/outputs/apk/debug/`.

Run unit tests:

```bash
./gradlew testDebugUnitTest
```

---

## Repository layout

Gradle module **names** match **on-disk paths** for `platform/` and `shared/`:

| Gradle module | Source path |
|---------------|-------------|
| `:app` | `app/` |
| `:feature:*:api` / `:feature:*:impl` | `feature/<name>/api`, `feature/<name>/impl` |
| `:platform:*` | `platform/<name>/` (e.g. `:platform:storage` → `platform/storage/`) |
| `:shared:*` | `shared/<name>/` |

See **[`platform/README.md`](platform/README.md)** and **[`shared/README.md`](shared/README.md)**.  
`:platform` / `:shared` / `:feature:<name>` の親はそれぞれ `platform/` / `shared/` / `feature/<name>/` 直下（集約用 `build.gradle.kts` のみ）。実装モジュールは `feature/<name>/impl/`。

---

## Documentation

| Doc | Description |
|-----|-------------|
| [`docs/tech.md`](docs/tech.md) | Technical choices and architecture notes (Japanese). |
| [`docs/release-signing-play.md`](docs/release-signing-play.md) | Play signing, upload key, CI secrets, key rotation (Japanese). |
| [`AGENTS.md`](AGENTS.md) | AI / contributor guidelines (Compose, design system, security). |
| [`app/README.md`](app/README.md) | Application shell, DI entry, security gate. |
| [`feature/README.md`](feature/README.md) | Feature modules (`login`, `note`, `setting`). |
| [`platform/README.md`](platform/README.md) | `:platform:*` libraries (navigation, DB, crypto, design system, …). |
| [`shared/README.md`](shared/README.md) | `:shared:*` libraries (`auth`, `settings`). |
| [`build-logic/README.md`](build-logic/README.md) | Convention plugins. |

---

## Tech stack

- **Language**: Kotlin  
- **UI**: Jetpack Compose, Material 3 (only inside `:platform:designsystem`)  
- **DI**: [Koin](https://insert-koin.io/)  
- **DB**: Room + SQLCipher ([Zetetic](https://www.zetetic.net/sqlcipher/))  
- **Crypto**: Android Keystore, Argon2 (Argon2Kt), AES-GCM  
- **Auth UI**: AndroidX Biometric (`BiometricPrompt` + `CryptoObject` where required)  

Versions are centralized in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

---

## Architecture (short)

- **Clean / layered / modular**: features own `presentation` → `domain` → `data`; dependencies point inward.
- **UDF-style UI**: `UiState`, `Intent`, and effects (see feature READMEs).
- **Design system**: `platform/designsystem` exposes atoms/molecules; feature modules must not depend on Compose Material 3 directly (enforced by convention plugin).

---

## Security highlights

- `allowBackup="false"`, cleartext traffic disabled, `FLAG_SECURE` on the main window.
- Release builds combine **risky-environment detection** with a **snapshot-based** `SensitiveOperationGuard` so UI and data-layer checks stay aligned.
- **Dependency visibility**: Dependabot (Gradle + GitHub Actions), [dependency graph submission](.github/workflows/dependency-submission.yml) on the default branch, and [dependency review](.github/workflows/ci.yml) on pull requests.
- See `docs/tech.md` and `AGENTS.md` for the threat model and coding rules. Release signing and upload-key rotation: `docs/release-signing-play.md`.

---

## Contributing

1. Follow [`AGENTS.md`](AGENTS.md) and Kotlin / Android official style guides.
2. Keep feature UI on the design system; do not add `implementation(libs.androidx.compose.material3)` outside `platform/designsystem`.
3. Prefer small, focused PRs; update module READMEs when you change public responsibilities.

---

## License

Copyright (C) 2026 segnities007  
This project is licensed under the **GNU General Public License v3.0 only** — see [`LICENSE`](LICENSE).
