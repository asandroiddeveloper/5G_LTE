# 5G LTE — App Improvement Plan

A prioritized review of the app as it stands today (branch `arena/01a10a35-5g-lte`, commit `ca09f7a`).

The whole app is ~640 lines of Kotlin across 4 source files, so almost every item below is
cheap to do. Items are ordered by **impact per unit of effort**, with concrete fixes.

---

## Implementation status

**Done in this branch** (see the sections referenced for the original reasoning):

| Table item | What changed |
|---|---|
| 1 — images | `best_for_*.png` → 1080px WebP in `res/drawable-nodpi/`. APK assets 1.65 MB → 396 KB; decoded size per card ~109 MB → ~10 MB |
| 2 — splash coroutine | `SplashActivity` deleted entirely; one seamless splash via `core-splashscreen` 1.2.0 (§3.1) |
| 3 — modifier order | Gradient moved to a root `Box`, transparent `Scaffold` above it; `SystemBarStyle.dark` fixes status-bar icon contrast (§1.3, §1.5) |
| 4 — `tryLTE()` | Replaced by `NetworkSettingsNavigator`: 9-rung ladder, per-rung `try/catch`, OEM entries, settings fallbacks, dialer code, and an honest `HIDDEN_MENU` vs `SETTINGS_FALLBACK` result (§1.1) |
| 5 — release build | `optimization { enable = true }` (AGP 9.3 R8 + resource shrinking), optional signing from `keystore.properties`, `.debug` application-id suffix, CI-driven versions (§6) |
| 6 — dead dependencies | Removed `appcompat`, `material`, `constraintlayout`, both navigation artifacts, `activity-ktx`, espresso and `viewBinding`; `material-icons-core` pinned explicitly to its final 1.7.8 (§5) |
| 7 — warning text | Rebuilt as three 14sp paragraphs plus a footer inside a card; no manual line breaks (§3.2) |
| 8 — strings | Extracted to `strings.xml`, plus new **Urdu translations** in `values-ur`; readable type scale; meaningful content descriptions (§4, §7.5, §10) |
| 9 — repo hygiene | `.idea/` untracked; dead dimens/themes/colors/drawables/strings removed; `.gitignore` hardened for keystores and build output (§8) |
| 10 — CI | Lint + unit tests on push and PR, debug artifact, tag-triggered release with optional signing, Dependabot (§6) |

**New features added** (beyond the original list):

* Three-tab UI (Status / Setup / Guide) with animated transitions, back handling, and a navigation
  rail at ≥ 720dp for tablets.
* Live connection card — transport, verification, metering, bandwidth estimate, interface, DNS.
* Network generation card — 5G SA / 5G NSA / 4G / 3G / 2G, opt-in `READ_PHONE_STATE`, graceful
  degradation, unit-tested mapper.
* Guided setup with device-specific hints and a manual fallback dialog (copy code / open dialer).
* Guide tab with per-variant "what to pick" captions, page dots and tap-to-zoom with pinch.
* Quick Settings tile, copy-diagnostics report, Urdu localization.
* `NetworkGenerationMapperTest` + a launch test for `MainActivity`.

**Still open** (Sprint 2/3 of §9): a `ViewModel` to hoist state out of the composables, a baseline
profile, screenshot tests, more locales, a `LICENSE` file, and Play listing assets.

---

## 0. The 60-second version

| # | Fix | Why it matters | Effort |
|---|-----|----------------|--------|
| 1 | Move the 3 `best_for_*.png` (1198×2531, ~550 KB each) out of `res/drawable/` | On a 3× device these decode to ~3600×7600 px ≈ **100 MB each** → OOM/jank risk | 15 min |
| 2 | Replace `lifecycleScope.launch` inside `SplashUi` composition with `LaunchedEffect` | A new coroutine is started on **every recomposition** → repeated `startActivity` / flicker | 5 min |
| 3 | Fix modifier order in `LTE_UI`: `.background()` **before** `.padding(innerPadding)` | The gradient currently stops at the system bars, leaving white/black bands edge-to-edge | 2 min |
| 4 | Make `tryLTE()` probe real activities instead of 2 hardcoded class names | Silent failure on most non-AOSP/Pixel ROMs — the app's one job | 1 h |
| 5 | Enable R8 + resource shrinking for release, add signing config | Release APK is currently unminified and unsigned | 30 min |
| 6 | Delete dead deps (`appcompat`, `material`, `constraintlayout`, 2× navigation, `activity-ktx`) + `viewBinding` | Unused weight in every build and APK | 10 min |
| 7 | Move the 10 sp wall-of-text into readable paragraphs / a card | It is the app's main content and is nearly unreadable | 1 h |
| 8 | Delete the custom `SplashActivity`, use the Android 12+ SplashScreen API | Android 12+ shows **two** splash screens back-to-back | 30 min |
| 9 | Replace hardcoded strings/sizes with `stringResource` + `MaterialTheme.typography` | Localization + accessibility (font scaling) | 2 h |
| 10 | Add `lint` + unit tests + PR builds to CI | CI today only builds debug on `master` | 30 min |

---

## 1. Correctness & robustness

### 1.1 `tryLTE()` is fragile — this is the app's core feature
`app/src/main/java/com/asdroid/jetpack_ui/appui/home.kt`

```kotlin
val intent = Intent(Intent.ACTION_MAIN)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    intent.setClassName("com.android.phone", "com.android.phone.settings.RadioInfo")
} else {
    intent.setClassName("com.android.settings", "com.android.settings.RadioInfo")
}
context.startActivity(intent)
```

Problems:
- Hardcoded component names only exist on AOSP/Pixel-ish ROMs. Samsung, Xiaomi, Oppo, Vivo, Realme, Tecno, Infinix etc. use different packages → the `catch` fires and the user sees `java.lang.ActivityNotFoundException...` dumped into a Toast.
- `Intent.ACTION_MAIN` on an explicit component is meaningless and can interact badly with some launchers/ROMs.
- Only the two "radio info" screens are tried; there are several better, officially supported fallbacks.

Suggested replacement — probe, then fall back through a ladder:

```kotlin
private val radioInfoCandidates = listOf(
    "com.android.phone" to "com.android.phone.settings.RadioInfo",
    "com.android.settings" to "com.android.settings.RadioInfo",
    "com.android.phone" to "com.android.phone.settings.RadioInfoActivity",
    // OEMs — probe with resolveActivity, never assume
    "com.samsung.android.app.telephonyui" to
        "com.samsung.android.app.telephonyui.hiddennetworksetting.MainActivity",
    "com.android.phone" to "com.android.phone.MiuiMobileNetworkSettings",
)

fun openRadioInfo(activity: Activity): Boolean {
    val explicit = radioInfoCandidates.asSequence()
        .map { (pkg, cls) -> Intent().setClassName(pkg, cls) }
        .firstOrNull { it.resolveActivity(activity.packageManager) != null }
    if (explicit != null) {
        activity.startActivity(explicit)
        return true
    }
    // Officially supported fallbacks, in order of usefulness
    val fallbacks = listOf(
        Settings.ACTION_NETWORK_OPERATOR_SETTINGS,
        Settings.ACTION_WIRELESS_SETTINGS,
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode("*#*#4636#*#*"))),
    )
    for (action in fallbacks) {
        val i = if (action is Intent) action else Intent(action)
        if (i.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(i)
            return true
        }
    }
    return false
}
```

Also:
- Pass `Activity`, not `Context`, or add `Intent.FLAG_ACTIVITY_NEW_TASK`.
- On failure show a friendly message ("Couldn't open your device's network settings"), not `"${e}"`.
- Consider `Intent.ACTION_DIAL` with `*#*#4636#*#*` as a documented, permission-free path many OEMs honour.
- Log the failing package list (see §7.3) so you learn which OEMs need a new entry.

### 1.2 Splash coroutine is started during composition
`splash/SplashActivity.kt` — the class already carries `@SuppressLint("CoroutineCreationDuringComposition")`, which is the smell:

```kotlin
setContent {
    JetPackUITheme {
        lifecycleScope.launch { delay(2000L.milliseconds); startActivity(...); finish() }
        SplashUi()
    }
}
```

Every recomposition launches another coroutine → duplicate navigations. Fix:

```kotlin
var navigated by rememberSaveable { mutableStateOf(false) }
LaunchedEffect(Unit) {
    delay(2.seconds)
    if (!navigated) { navigated = true; startActivity(...); finish() }
}
```

Even better: delete this activity entirely (§3.1).

### 1.3 Edge-to-edge gradient doesn't reach the system bars
`appui/home.kt`, `LTE_UI`:
```kotlin
Modifier.fillMaxSize()
    .verticalScroll(rememberScrollState())
    .padding(innerPadding)   // ← shrinks the box first
    .background(brush = ...) // ← so the gradient only paints inside it
```
Modifier order matters: background paints *inside* the padding. Result is visible white/black
bands at the top and bottom on `enableEdgeToEdge()` devices. Use:

```kotlin
Box(Modifier.fillMaxSize().background(gradient)) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ...
    )
}
```

### 1.4 Ignored Scaffold padding
`MainActivity.kt` suppresses `UnusedMaterial3ScaffoldPaddingParameter` and then `@SuppressLint`s it.
Either consume `innerPadding` properly (preferred, see above) or drop the `Scaffold` — there is no
top bar, FAB, or snackbar host, so `Surface { ... }` is enough and avoids the whole issue.

### 1.5 Status-bar icon contrast
The UI is always a dark gradient, but the XML theme is `Theme.Material.Light.NoActionBar`
(+ DayNight variant) and `enableEdgeToEdge()` derives icon colour from the *system* theme.
On a device in light mode you get **dark status-bar icons on a dark blue/green gradient**.
Fix by forcing light icons:

```kotlin
enableEdgeToEdge(
    statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
)
```
…and do the same in the splash, which currently calls no `enableEdgeToEdge()` at all.

### 1.6 `LazyRow` with a single item
`RenderSuggestions()` puts all three cards in one `item { }`. Nothing is lazy; layout is
computed in one pass and you get no recycling benefit. Either emit three `item { }` blocks or
replace it with a plain `Row` + `horizontalScroll` (simpler, same result, less overhead).

### 1.7 `@Preview` on the wrong composable
`RenderSuggestions()` is annotated `@Preview(showSystemUi = true)` but it uses
`painterResource` of 1200×2500 images and is a private building block of the screen. Put the
preview on a `LTE_UI`-level wrapper with fake data, and keep `LTE_UI` stateless so previews
don't need a `Context`.

---

## 2. Performance & memory (highest real-world risk)

### 2.1 Full-resolution screenshots as drawables
```
app/src/main/res/drawable/best_for_all.png   1198 × 2531   552 KB   ← no density qualifier
app/src/main/res/drawable/best_for_4g.png    1198 × 2531   546 KB
app/src/main/res/drawable/best_for_5g.png    1198 × 2531   547 KB
app/src/main/res/drawable/ltelogo.png         600 ×  327    84 KB
```

Files in `res/drawable/` are treated as **mdpi**. On an xxhdpi (3×) phone, Android decodes
`best_for_all.png` as ~3594 × 7593 px = **~109 MB** of ARGB_8888 for one image, and you have
three of them plus the logo. Even on mdpi that's 12 MB each. This is a textbook
`OutOfMemoryError` / frame-drop generator on low-end devices, which is exactly the target
audience for this app.

Fixes, in order of preference:
1. **Resize the source files** to what actually gets displayed — the cards render at
   `Modifier.height(350.dp)`, so ~1080 px wide is plenty. Downscale to ≈1080 × 2280 and convert
   to WebP (`cwebp -q 80`), which typically cuts them to 60–90 KB each.
2. Put them in `res/drawable-nodpi/` so the ROM stops density-upscaling them, and set an
   explicit size at the call site: `Modifier.fillMaxWidth(0.85f).aspectRatio(1198f/2531f)`.
3. For scrolling content like this, consider Coil (`coil-compose`) with a memory/disk cache —
   it downsamples to the target size at decode time, which is the proper fix for large imagery.

Also: `res/drawable/logo.webp` (72×72) appears unreferenced — dead file.

### 2.2 Hardcoded 400 dp / 290 dp on the splash
`SplashUi()` uses `Spacer(height = 290.dp)` + `Image(size = 400.dp)`. On a 320 dp-wide small
phone, in landscape, or at a large display-font setting, the layout clips or overflows.
Replace the magic numbers with proportional sizing and centralise the content:

```kotlin
Column(
    Modifier.fillMaxSize().padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Image(painterResource(R.drawable.ltelogo), null, Modifier.fillMaxWidth(0.7f))
    Spacer(Modifier.height(24.dp))
    ...
}
```

### 2.3 Wrap the gradient
`Brush.linearGradient(...)` is re-created on every recomposition in both screens. Hoist it:
`val gradient = remember { Brush.linearGradient(listOf(...)) }`, or better derive it from
`MaterialTheme.colorScheme` so it follows the theme (§4.1).

---

## 3. UX

### 3.1 You currently show two splash screens on Android 12+
Since Android 12 the system always draws a splash (app icon on `windowSplashScreenBackground`)
before your first activity. Your `SplashActivity` then adds a second, custom one. Users see a
flash of the launcher icon, then a 2-second gradient. Replace both with the official API:

```kotlin
// app/build.gradle.kts
implementation("androidx.core:core-splashscreen:1.2.0")
```
```kotlin
// MainActivity
val splash = installSplashScreen().apply {
    setKeepOnScreenCondition { viewModel.isLoading }
}
```
```xml
<!-- values-v31/themes.xml -->
<style name="Theme.JetPackUI" parent="Base.Theme.JetPackUI">
    <item name="android:windowSplashScreenBackground">@color/black</item>
    <item name="android:windowSplashScreenAnimatedIcon">@drawable/ic_splash</item>
</style>
```
Benefits: one seamless screen, no waiting, no extra activity, no `CustomSplashScreen`
`@SuppressLint`. Keep the gradient as the *branding* on the home screen if you like the look.

### 3.2 The warning text is the product — make it readable
Today: one `Text` with `fontSize = 10.sp`, 8 hardcoded `\n` line breaks, mixed emoji, and
sentences that wrap unpredictably at different widths/font scales. Replace with:

- A `Card` / `Surface` with proper padding, `MaterialTheme.typography.bodyMedium` (14 sp),
  and real paragraphs (no manual `\n`).
- A short headline ("Keep **LTE/GSM/WCDMA (Auto)** for reliable calls") plus an expandable
  "Why?" section — the detail is valuable but shouldn't be the first thing users see.
- `TextAlign.Start` and `Modifier.fillMaxWidth()` so text respects the reading direction.
- A distinct colour for the warning (amber) instead of `Color.LightGray` on green.

### 3.3 Honest expectation setting
The README correctly notes a third-party app can't change the radio mode without
`MODIFY_PHONE_STATE`. The button labelled **"Set 5G LTE"** implies it does. Rename it to
**"Open network settings"** / **"Open radio info"**, and add one line of copy explaining that
Android requires you to confirm the change yourself. This turns a "the app didn't work" review
into a "the app told me exactly what to do" review.

### 3.4 Add a "which one should I pick?" decision aid
You already ship the three `best_for_*` cards, but they are just images that require pinch-zoom
to read. Add above them a 3-option segmented control (`Auto` / `4G only` / `5G only`) that
swaps in the matching card, plus a one-line recommendation based on what you can measure
(§7.1). Fewer swipes, more useful.

### 3.5 Give feedback on the tap
No loading state, no confirmation. If opening the settings screen takes a moment (or fails),
the user gets nothing. Add a `Snackbar` ("Opening…", "Your device doesn't expose this screen")
and disable/`CircularProgressIndicator` the button while resolving.

---

## 4. Theming & visual design

### 4.1 Two competing theme packages, neither actually used
- `ui/theme/` and `splash/ui/theme/` both define `JetPackUITheme`, `Typography` and the
  Material purple palette (copy-pasted).
- Both enable `dynamicColor = true`, so on Android 12+ the app's Material colours are
  wallpaper-derived — but every screen hardcodes `ElectricBlue`/`ExtraGreen`/`Black` inline,
  so the dynamic scheme only affects the components you didn't style. That inconsistency is why
  a `Button` container colour, a ripple, or a `Card` can look off-brand.

Do this instead:
1. Delete `splash/ui/theme/`, keep **one** `ui/theme/` package.
2. Define a real brand scheme rather than Material purple:

```kotlin
private val BrandDark = darkColorScheme(
    primary = ElectricBlue,
    secondary = ExtraGreen,
    background = Black,
    surface = Color(0xFF0B1220),
    onPrimary = Color.White,
    onBackground = Color(0xFFE6E6E6),
)
```
3. Expose the gradient as part of the theme (`CompositionLocal`) so splash + home share it.
4. Decide on dynamic colour deliberately — for a dark, branded app the usual choice is
   `dynamicColor = false`.

### 4.2 XML themes
- `values/themes.xml` defines `Theme.JetPackUI` as framework `android:Theme.Material.Light.NoActionBar`,
  while `values-v23/themes.xml` redefines the same name via `Base.Theme.JetPackUI`
  (`Theme.Material3.DayNight.NoActionBar`). With `minSdk = 24` the v23 file always wins, so the
  base definition is dead-but-confusing. Keep one definition and let `Base.Theme.JetPackUI` be
  the real source of truth.
- Add a `values-night/themes.xml` `windowBackground` matching the gradient so the window
  background doesn't flash white before first frame (a real "first paint" flash today).
- `Theme.Transparent` uses `android:windowDisablePreview` — good for the custom splash, but it
  makes cold start feel *slower*. The SplashScreen API (§3.1) removes the need entirely.

### 4.3 Adaptive icon
`mipmap-anydpi-v26/ic_launcher.xml` sets `<monochrome>` to `@mipmap/ic_launcher_foreground`,
which is a full-colour WebP. Monochrome layers must be a single-colour vector, otherwise
themed icons (Android 13+) render as a grey blob. Point `<monochrome>` at a dedicated
`@drawable/ic_launcher_monochrome` vector, and note that
`drawable/ic_launcher_background.xml` / `ic_launcher_foreground.xml` are currently unreferenced.

### 4.4 Motion
The README advertises an "animated linear-gradient background" — there is no animation in
`SplashActivity.kt`. Either implement it (a slow `infiniteTransition` rotating gradient stops
looks great and costs little) or fix the README. Add a subtle entrance transition
(`AnimatedVisibility` / `animateFloatAsState` for logo alpha + scale) so the splash doesn't
feel like a static poster for 2 seconds.

---

## 5. Architecture & code quality

Current shape: two activities, one 266-line composable file, UI logic mixed with
`Context`/`Intent` side effects, no state holder, no ViewModel.

| Suggestion | Detail |
|---|---|
| **Feature packages** | `ui/home`, `ui/splash`, `ui/theme`, `data/network`, `util`. Drops the `appui`/`splash` asymmetry. |
| **Introduce a `ViewModel`** | `HomeViewModel` exposing `StateFlow<HomeUiState>`; keeps `Intent`/`Context` work out of composables, survives rotation, and makes the screen previewable and testable. |
| **Stateless composables** | `LTE_UI(state, onOpenSettings)` — then `@Preview` works without a device and tests don't need an activity. |
| **Extract the intent ladder** | `NetworkSettingsNavigator` (a pure class) → unit-testable per OEM, see §6. |
| **Design tokens** | `Spacing.kt` / `Dimens` instead of scattered `6.dp`, `16.dp`, `30.dp`, `290.dp`. |
| **Remove inline `\n`** | Text layout belongs to the layout engine, not to string literals. |
| **Dead code / resources** | `strings.xml` still carries ~5 KB of `lorem_ipsum` plus `first_fragment_label`, `second_fragment_label`, `next`, `previous`; `values/dimens.xml` + `values-land`, `values-w600dp`, `values-w1240dp` exist only for the unused `fab_margin`; `colors.xml` has unused purple/teal; keepRules is empty. Delete them (saves a res merge pass and confusion). |
| **Alignment of Java/Kotlin targets** | `compileOptions` is Java 11 with no `kotlin { compilerOptions { jvmTarget } }`; AGP 9 will warn or fail on inconsistent JVM targets. Set both to 17 explicitly. |
| **Static analysis** | Add `ktlint`/`detekt` with an `.editorconfig`, and `lint { abortOnError = true }`. |
| **Version catalog hygiene** | `implementation("androidx.compose.material:material-icons-core")` is versionless and only resolves because the BOM still pins it — and it resolves to **1.7.8** while the rest of Compose is 1.11.4 (verified in the `compose-bom:2026.06.01` POM). Add it as a catalog alias with an explicit version, or better, replace it with a small local vector asset (`Settings` is in `icons-core`; if you never need the extended set, one SVG removes the dependency entirely). |

---

## 6. Build, release & CI

`app/build.gradle.kts`:
```kotlin
buildTypes {
    release {
        optimization { enable = false }   // ← R8 is OFF
    }
}
buildFeatures { compose = true; viewBinding = true }  // ← viewBinding unused (0 XML layouts)
```
- Enable R8 + resource shrinking for release; keep `keepRules/rules.keep` (already wired, it's empty).
- Add a `signingConfigs.release` reading from `keystore.properties`/env vars (never commit the keystore — and add `*.jks`, `*.keystore`, `keystore.properties` to `.gitignore`).
- Add `debug { applicationIdSuffix = ".debug"; versionNameSuffix = "-debug" }` so debug and release can coexist on a device.
- Remove unused dependencies (verified 0 references in `app/src`):
  `androidx.appcompat`, `com.google.android.material`, `androidx.constraintlayout`,
  `androidx.navigation.fragment.ktx`, `androidx.navigation.ui.ktx`, `androidx.activity.ktx`.
  That's 6 of your 20 dependencies.
- Derive `versionCode`/`versionName` from CI (`-PversionCode=$GITHUB_RUN_NUMBER`) instead of hardcoded `1` / `"1.0"`.

`.github/workflows/build.yml` today: builds `assembleDebug` on push to `master`, then always
publishes a GitHub Release tagged `v${{ github.run_number }}`. Improvements:

```yaml
on:
  push: { branches: [ master ], tags: [ 'v*' ] }
  pull_request:
  workflow_dispatch:

steps:
  - uses: actions/checkout@v4
  - uses: actions/setup-java@v4
    with: { distribution: temurin, java-version: '21' }
  - uses: gradle/actions/setup-gradle@v4      # caching + build scans
  - run: ./gradlew lint testDebugUnitTest assembleDebug --no-daemon
  - uses: actions/upload-artifact@v4
    with: { name: debug-apk, path: app/build/outputs/apk/debug/*.apk }
  # release only on tags, with generated notes:
  - if: startsWith(github.ref, 'refs/tags/v')
    uses: softprops/action-gh-release@v2
    with: { generate_release_notes: true, files: app/build/outputs/apk/release/*.apk }
```
Also worth adding: `.github/dependabot.yml` (gradle + github-actions, weekly) so the
compose/AGP versions in `libs.versions.toml` don't drift 6 months behind.

---

## 7. Features that would genuinely improve the app

### 7.1 Live connection status card (no permissions needed)
Right now the app tells the user what to do but doesn't show them anything about *their*
connection. Using `ConnectivityManager.registerDefaultNetworkCallback()` you can display, with
zero runtime permissions:
- transport (Wi-Fi / cellular / VPN), `NET_CAPABILITY_VALIDATED`, metered or not,
- estimated downstream/upstream bandwidth (`linkDownstreamBandwidthKbps`),
- link properties (interface name, DNS, MTU).

On API 31+ add `TelephonyManager.registerTelephonyCallback` with a
`TelephonyCallback.DisplayInfoListener` to distinguish **5G (NSA/SA)** from **4G LTE** — that's
what makes the app feel like it does something. (Signal strength needs `READ_PHONE_STATE`, so
gate it behind an explicit permission request with a rationale, and degrade gracefully.)

Even without permission, `TelephonyManager.networkOperatorName`, `simOperatorName` and
`isDataEnabled` + `isRoaming` give a useful "Operator / SIM / roaming" row.

### 7.2 Guided, OEM-aware setup
Combine §1.1 with a small step list: *"1. Tap Open network settings → 2. Choose
'LTE/GSM/WCDMA (Auto)' → 3. Come back here."* Persist a "did it work?" check
(`DataStore`) and offer a "still stuck?" branch with the dialer code and OEM-specific paths.
This is the difference between a demo and a tool people keep installed.

### 7.3 Quick Settings tile / widget
A `TileService` (API 24+) or a 1×1 widget that deep-links straight to the radio settings screen
is a genuinely useful shortcut for the app's single purpose — and it's ~40 lines.

### 7.4 Diagnostics & privacy
- Crash reporting (Play Vitals is free, or Crashlytics) — you're currently blind to the
  `ActivityNotFoundException` rate per OEM, which is exactly the data you need for §1.1.
- A tiny in-app "Diagnostics" screen (device model, ROM, Android version, resolved component,
  last error) with a **Copy** button — turns support chats into one paste.
- Privacy: the app collects nothing today, which is a selling point. Add a one-paragraph
  privacy policy (Play requires a URL anyway) and keep the app permission-free if you can.

### 7.5 Localization
The app is English-only with hardcoded strings. Given the store listing and target audience,
adding `values-ur/strings.xml` (RTL) plus extracting all strings would roughly double the
addressable users — and Compose handles RTL automatically once the text is in resources.
(Remember `Modifier.padding(start/end)` instead of `left/right`.)

### 7.6 Store & packaging polish
- Add a `LICENSE` file — the repo currently has none, which leaves users and Play reviewers
  guessing. Apache-2.0 or MIT fits this project.
- Add `androidx.profileinstaller` + a baseline profile: your cold start is dominated by a
  2-second artificial splash; a baseline profile is the single biggest measurable startup win.
- Screenshots / feature graphic / short description for Play, plus the existing
  `ic_launcher-playstore.png` (512×512) — keep it.
- Adopt `WindowSizeClass` for tablet/landscape: the single-column 1200 px-wide layout looks
  broken on a tablet.

### 7.7 Testing
You have only the two generated placeholder tests. Add:
- **Unit**: `NetworkSettingsNavigator` probe order given a fake `PackageManager` (Robolectric),
  version-code parsing, state mapping.
- **Compose UI**: `createAndroidComposeRule<MainActivity>()` asserting the button exists,
  tapping it hits the injected navigator, and the warning text is present at font scale 2.0.
- **Screenshot tests** (Roborazzi/Paparazzi) so the gradient screens can't silently regress.

---

## 8. Housekeeping

| Item | Action |
|---|---|
| `.idea/*.xml` is committed (9 files) | Remove from VCS, add `.idea/` (except nothing) to `.gitignore` — IDE state is per-developer and causes merge noise. |
| `googlebd9be255d66afd34.html` at repo root | Search-console verification — fine to keep; consider moving under `docs/` so the root stays clean. |
| No `LICENSE` | Add one. |
| No `CONTRIBUTING.md` / issue & PR templates | Add minimal ones if you want outside contributions. |
| No `dependabot.yml` | Add it (§6). |
| `.gitignore` gaps | Add `*.apk`, `*.aab`, `*.jks`, `*.keystore`, `keystore.properties`, `.kotlin/`, `**/build/`. |
| `README.md` accuracy | It advertises "animated" gradient, "Material Icons (Extended)", and a "MODIFY_PHONE_STATE" approach; none match the code. It also says min/target are "_(fill in from `build.gradle`)_" — they're `24` / `37`. Fix or it undermines trust in the repo. |
| App label | `android:label="5G LTE"` is hardcoded on both activities while the application uses `@string/app_name` — use the string everywhere. |

---

## 9. Suggested roadmap

**Sprint 1 — stop the bleeding (½ day)**
1. Resize/re-encode the images + `drawable-nodpi` (§2.1)
2. Fix modifier order and status-bar icons (§1.3, §1.5)
3. Replace the splash coroutine with `LaunchedEffect` or drop the activity (§1.2, §3.1)
4. Robust `openRadioInfo()` with fallbacks + friendly error (§1.1)
5. Delete dead deps, dead resources, dead `.idea` files (§6, §8)

**Sprint 2 — make it feel like a real app (2–3 days)**
6. `ViewModel` + stateless composables + one consolidated theme (§4.1, §5)
7. Readable warning card + segmented "which mode" picker (§3.2, §3.4)
8. Live connection status card (§7.1)
9. Strings → resources, `typography` scale, content descriptions (§7.5, §10)

**Sprint 3 — ship-quality (2–3 days)**
10. R8 + signing + versioning + PR CI with lint/tests (§6)
11. Unit + Compose UI tests (§7.7)
12. Quick Settings tile, diagnostics screen, LICENSE, README rewrite (§7.3, §7.4, §8)

---

## 10. Accessibility quick list

- Body text at 10 sp fails comfortably-readable sizing; use ≥14 sp for content, 12 sp for
  captions, and verify at font scale 200 % (the current hardcoded `\n` layout will shatter).
- `contentDescription = "Logo"`, `"Best_for_all"`, `"Setting"` are not meaningful. Decorative
  images → `contentDescription = null`; the button icon → `null` since the label describes it.
- `Color.LightGray` on `ExtraGreen` (#0A6B2F) is ~4.4:1 — borderline for small text;
  `#cde018` on blue/green varies. Pick fixed, contrast-checked text colours per surface.
- Ensure tap targets ≥48 dp (the 70 dp button is fine; the icons inside are not individually
  tappable — good).
- Test with TalkBack and with "Remove animations" enabled.

---

### Notes on verification
- `material-icons-core` resolves: the `compose-bom:2026.06.01` POM still pins
  `material-icons-core`/`extended` at **1.7.8** (the final release of those artifacts,
  Feb 2025), while `compose-ui`/`foundation` are 1.11.4 and `material3` is 1.4.0 — so the
  versionless declaration builds, but mixes two Compose trains. Worth making explicit.
- Unused-dependency claims were checked by grepping `app/src` for each artifact's API surface
  (all returned 0 references).
- The image memory estimate assumes ARGB_8888 at device density 3× and a `mdpi` baseline for
  files in `res/drawable/`, which is how Android's resource system scales them.
