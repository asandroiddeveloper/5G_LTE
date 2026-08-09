# 5G LTE — JetPackUI

A Jetpack Compose Android app exploring modern UI patterns, gradient theming, and network-related system settings.

## Overview

**5G LTE** is a Kotlin + Jetpack Compose project featuring a custom splash screen, gradient-based UI theming, and a network mode settings screen (LTE/NR preference controls with safety warnings for voice call reliability).

- **Package name:** `com.asdroid.jetpack_ui`
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Min SDK / Target SDK:** _(fill in from `build.gradle`)_

## Features

- **Splash Screen** — animated linear-gradient background with logo and branding, edge-to-edge layout with a custom bottom bar
- **Custom Color Palette** — centralized theme colors (`ElectricBlue`, `ExtraGreen`, `Black`, etc.) defined in `Color.kt`
- **Network Settings Screen** — lets users choose preferred network mode (Auto / LTE Only / NR Only) with an in-app warning about potential call reliability issues when VoLTE/VoNR isn't supported
- **Scrollable layouts** — long-form screens using `Column` + `verticalScroll` for content that exceeds screen height
- **Material Icons (Extended)** — icon support via `material-icons-extended`

## Project Structure

```
app/src/main/java/com/asdroid/jetpack_ui/
├── MainActivity.kt
├── splash/
│   ├── SplashActivity.kt        # Splash screen entry point
│   └── ui/theme/
│       ├── Color.kt              # App color palette
│       └── JetPackUITheme        # Theme wrapper
├── appui/
│   └── home.kt                   # Home / network settings UI
└── ...
```

## Theming

Colors are centralized in `Color.kt` and referenced across composables rather than hardcoded inline:

```kotlin
val ElectricBlue = Color(0xFF080FC3)
val ExtraGreen = Color(0xFF...)
val Black = Color(0xFF...)
```

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/asandroiddeveloper/5G_LTE.git
   ```
2. Open in **Android Studio** (Giraffe or later recommended).
3. Sync Gradle (ensure `androidx.appcompat:appcompat` and other dependencies are pinned to valid published versions — avoid placeholder/invalid version numbers).
4. Run on an emulator or physical device (min API level per `build.gradle`).

## Key Dependencies

```kotlin
implementation("androidx.compose.material:material-icons-extended:1.7.5")
```

## Notes on Network Settings

Changing the preferred network mode to **LTE Only** or **NR Only** may prevent calls if the carrier doesn't support VoLTE/VoNR. The default/recommended mode is **Auto (LTE/WCDMA/GSM)** to ensure fallback for voice calls. Programmatically changing system network mode requires `MODIFY_PHONE_STATE` or carrier-privileged access and is not available to standard third-party apps without elevated permissions.

## CI/CD

GitHub Actions workflow builds a debug APK and publishes it as a GitHub Release on every push to the `master` branch.

## Author

Designed and developed by **AS** ([asdroid](https://github.com/asdroid))

## Branding

- **Developer:** AS
- **Developer tag:** asdroid
- **Package namespace:** `com.asdroid.jetpack_ui`

## Tags

`android` `kotlin` `jetpack-compose` `material3` `5g` `lte` `network-settings` `android-ui` `mobile-app` `compose-ui` `android-development` `asdroid` `as`
