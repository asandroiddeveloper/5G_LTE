# 5G LTE

A small, focused Android app that helps you check what your phone's radio is actually doing and
reach the hidden **Phone info** screen where the preferred network mode (5G / LTE / automatic)
is set.

Built with Kotlin and Jetpack Compose (Material 3). No accounts, no ads, no analytics.

- **Package:** `com.asdroid.jetpack_ui`
- **Min SDK:** 24 (Android 7.0)
- **Target / compile SDK:** 37
- **JDK toolchain:** 25 (see `gradle/gradle-daemon-jvm.properties`)

---

## What it does

### Status tab — what your connection is doing right now
* **Live connection card**: transport (Wi-Fi / Cellular / VPN / Ethernet), whether the internet is
  verified, metered or not, the OS bandwidth estimate, the interface and DNS servers.
  Powered by `ConnectivityManager.registerDefaultNetworkCallback()` — **no runtime permission**.
* **Mobile network card**: carrier name, and — if you opt in — whether you are on **5G Standalone,
  5G NSA, 4G LTE, 3G or 2G**, including the carrier display override that makes an LTE anchor
  report as 5G.
* **Copy diagnostics**: one tap puts device, network and menu-resolution details on the clipboard
  for a bug report.

### Setup tab — reach the hidden menu
* Three numbered steps and a button that opens the **Phone info** screen, probing a ladder of known
  entry points (AOSP, Samsung, Xiaomi, MediaTek), then falling back to documented settings screens.
* If nothing hidden is reachable, an honest dialog offers the dialer code `*#*#4636#*#*`
  (copy button + open-dialer button) instead of failing silently.
* The safety warning is now three readable paragraphs: locking to 4G/5G can break calls if your
  carrier has no VoLTE/VoNR in your area.

### Guide tab — which mode should I pick?
* The three real screenshots (automatic / 4G only / 5G only), each with a one-line "what to pick
  in the list" caption.
* A picker ("calls stay reliable" / "stable fast data" / "maximum speed") that scrolls straight to
  the matching card, page dots, and **tap-to-zoom** with pinch magnification.

### Everywhere
* **Quick Settings tile** ("Phone info"): one tap from the shade runs the same navigation ladder.
* **Tablet layout**: at ≥ 720dp wide the tabs become a navigation rail and content fills the pane.
* **Urdu translations** (`values-ur`) with RTL support, alongside English.

---

## Permissions and privacy

| Permission | Why | Prompt? |
|---|---|---|
| `ACCESS_NETWORK_STATE` | Read the active network's capabilities for the Status tab | No (normal permission) |
| `READ_PHONE_STATE` | Display the current network type (5G / 4G / 3G). **Optional** — every feature except that row works without it | Yes, only when you tap *Allow* |

Nothing is collected, stored or uploaded. The diagnostics report is only placed on your clipboard
when you ask for it.

> **Note:** no third-party app can change the radio mode itself — Android reserves that for the
> system and the dialer. This app exists to take you to the exact screen and explain the trade-offs.

---

## Project structure

```
app/src/main/java/com/asdroid/jetpack_ui/
├── MainActivity.kt                  # edge-to-edge, splash-screen install, tile requests
├── network/
│   ├── ConnectionStatus.kt          # ConnectivityManager monitor (no permission)
│   ├── TelephonyStatus.kt           # carrier + network generation, opt-in permission
│   ├── NetworkSettingsNavigator.kt  # the probe ladder for the hidden radio menu
│   └── Diagnostics.kt               # copyable report builder
├── qstile/RadioInfoTile.kt          # Quick Settings tile
├── ui/
│   ├── theme/                       # one brand theme: palette, type scale, gradient
│   ├── components/                  # cards, rows, pills, chips, nav rail, steps
│   └── screens/                     # HomeScreen (tabs) + Status / Setup / Guide
└── util/Clipboard.kt
```

Assets live in `res/drawable-nodpi/` as WebP: the three guide screenshots are 1080px wide
(~126 KB each) instead of full-resolution PNGs (~550 KB), because files in `res/drawable/`
are treated as mdpi and were being decoded at ~3× their size on a modern phone.

---

## Build

```bash
git clone https://github.com/asandroiddeveloper/5G_LTE.git
cd 5G_LTE
./gradlew assembleDebug          # debug APK in app/build/outputs/apk/debug/
./gradlew testDebugUnitTest      # unit tests
./gradlew lintDebug              # Android lint
```

Open in Android Studio (Ladybug or newer) and let it sync; the Gradle wrapper pins Gradle 9.5 and
AGP 9.3.1.

### Release signing (optional)

Create a git-ignored `keystore.properties` in the repository root:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=…
keyAlias=…
keyPassword=…
```

With that file present, `./gradlew assembleRelease` produces a signed, R8-optimised APK
(`optimization { enable = true }` in AGP 9.3 also shrinks unused resources). Without it the
release build still compiles, unsigned.

### Versioning from CI

`versionCode` / `versionName` default to `1` / `1.0` and can be overridden:

```bash
./gradlew assembleRelease -PversionCode=42 -PversionName=1.4.0
```

---

## CI/CD

`.github/workflows/build.yml`

* **On every push to `master` and every pull request**: lint, unit tests and a debug APK, uploaded
  as a build artifact.
* **On a `v*` tag**: builds a release APK and publishes a GitHub Release with generated notes.
  Add these repository secrets to get a **signed** release; otherwise the workflow publishes the
  debug APK so a release is never empty:

  | Secret | Contents |
  |---|---|
  | `KEYSTORE_BASE64` | `base64 -w0 release.jks` |
  | `KEYSTORE_PASSWORD` | keystore password |
  | `KEY_ALIAS` | key alias |
  | `KEY_PASSWORD` | key password |

`.github/dependabot.yml` keeps Gradle dependencies and Actions up to date weekly.

---

## Tests

* `NetworkGenerationMapperTest` — the pure mapping from network type codes (and 5G display
  overrides) to a generation label. Runs on the JVM.
* `ExampleInstrumentedTest` — launches `MainActivity` and checks the title renders.

---

## Known limitations

* The **OEM entry points** in `NetworkSettingsNavigator` are best-effort: manufacturers move these
  screens around between ROM versions. When nothing matches, the app says so and offers the dialer
  code — it never pretends to have changed a setting.
* **`READ_PHONE_STATE` is required** by Android to read the network type on modern versions; the
  app degrades to "Unknown" rather than crashing if you decline.
* Bandwidth figures come from the OS estimate, not a measurement — they are indicative only.

## Contributing / licence

No licence file is present yet; add one (Apache-2.0 or MIT are typical for this kind of app)
before accepting outside contributions.

Designed and developed by **AS** ([asdroid](https://github.com/asdroid)).
