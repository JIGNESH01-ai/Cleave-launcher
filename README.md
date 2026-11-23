<p align="center">
  <img src="assets/cleave-launcher-logo.png" alt="CLEAVE Launcher — CL monogram" width="240" />
</p>

<h1 align="center">CLEAVE LAUNCHER</h1>
<p align="center"><strong>Minecraft Java, Anywhere.</strong></p>
<p align="center"><em>A real, landscape, PC-style Minecraft: Java Edition launcher for Android — built for low-end phones, made by JIGNESHGAMING.</em></p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/Platform-Android%209%2B-3DDC84?style=flat-square&logo=android&logoColor=white" />
  <img alt="Licence" src="https://img.shields.io/badge/Licence-GPL--3.0-blue?style=flat-square" />
  <img alt="Orientation" src="https://img.shields.io/badge/Orientation-Landscape%20only-111827?style=flat-square" />
  <img alt="Language" src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" />
</p>

---

## What CLEAVE Launcher is

CLEAVE Launcher is a **native Android application** that runs **Minecraft: Java Edition** on Android. It is a full launcher — not a UI mockup — built on the open-source PojavLauncher / MojoLauncher technology stack (GPL-3.0) and the Zalith Launcher 2 interface lineage, re-branded, re-engineered and maintained for CLEAVE by **JIGNESHGAMING**.

- **Application IDs:** `com.cleave.launcher` (public) · `com.cleave.launcher.debug` (testing) · `com.cleave.launcher.pub` (nightly)
- **Source namespace:** `net.kdt.pojavlaunch` (kept stable for native/JNI compatibility; applicationId is what your phone installs)
- **Target SDK 36 · minSdk 23 · Android 9+ · ARM64-first (ARMv7 supported) · landscape only**
- **Design:** black / graphite / electric-blue, horizontal PC-style interface, forced landscape everywhere

## Official links

| Channel | Link |
|---|---|
| YouTube | <https://www.youtube.com/@JIGNESH_GAMING-k7j> |
| Instagram | <https://www.instagram.com/_jignesh_734> |
| Discord (official CLEAVE server) | <https://discord.gg/nyn75Un9MR> |
| Source & releases | <https://github.com/JIGNESH01-ai/Cleave-launcher> |

**Made by JIGNESHGAMING.** CREDITS and licence attribution live in-app under **Settings → About**.

## Feature overview

Everything below exists in the codebase and is exercised by the CI smoke/unit suite where a device is not required. Launching a real game still needs a real phone — see **Honest status**.

- **Home dashboard** — brand, selected account/version/loader, RAM and performance preset, PLAY button, tasks, recent worlds
- **Accounts** — Microsoft (OAuth, Java profile + entitlement check) and clearly-marked **unofficial offline profiles** (custom username, Steve/Alex/custom skins, import/export backup). Offline accounts are never presented as authenticated Microsoft accounts
- **Version manager** — releases, snapshots, alphas/betas *where compatible*, install/repair/remove, favourites, per-version RAM
- **Mod loaders** — Vanilla, Fabric, Forge, NeoForge, Quilt, OptiFine (where compatible), one-tap loader install
- **Mod manager** — import/enable/disable, loader identification, conflict hints, mods folder shortcut, backups
- **Modpacks** — CurseForge/Modrinth-style discovery, ZIP import/export, separate instances, progress reporting
- **Skin Studio** — 3D Steve/Alex preview, rotate/zoom, import/export skins, skin library
- **Worlds** — list, duplicate/rename/delete, backups, exports
- **Servers** — list with real ping support, favourites, quick join. No server is shown as "online" unless its status was actually checked
- **Downloads centre** — versions, libraries, assets, Java runtimes, loaders, mods, packs — queue, pause/resume/retry, real progress
- **Performance centre** — Potato → Balanced presets, RAM slider with safe limits (never allocates all device RAM), render distance, particles, vsync, FPS cap, resolution scaling, battery-saver aware. No guaranteed FPS numbers — real settings only
- **Java runtimes** — OpenJDK 8/17/21 selection per Minecraft version, automatic selection, repair/reinstall
- **Controls** — fully customisable touch layouts, keyboard & mouse, gamepad mapping, import/export, opacity/size/position editing
- **Settings** — AMOLED/compact modes, UI scaling, custom background, themes, accent colour, landscape locked (cannot be switched to portrait)
- **Tools** — latest-log and crash-report viewers, storage analysis, launch/JVM arguments editor, config backup/restore
- **About** — logo, tagline, installed version, official social buttons, update check against this repository's GitHub Releases, credits

## Honest status (read this)

This project is a **rebrand and continuation of a real, working launcher codebase**. In particular:

1. **Game launching has not been executed on a physical phone as part of this change set.** The launch path is the battle-tested PojavLauncher/MojoLauncher one, and the CI compiles the APK and runs ~100 smoke/unit tests, but *you* should treat the first run on your device as the acceptance test.
2. **APK artefacts** are produced by GitHub Actions (`.github/workflows/cleave-debug.yml` builds `Cleave-Nightly.apk` and attaches it to the `debug-latest` prerelease; `release.yml` builds signed public releases — only with the release keystore secrets configured). **Signed public APKs cannot be produced without `CLEAVE_KEYSTORE_FILE` / `CLEAVE_KEYSTORE_PASSWORD` secrets.**
3. **Update checking** targets this repository's GitHub Releases API (`releases/latest`). It works, but only after a public release exists.
4. **In-game "Socials" and "Cleave account" sign-in** need the CLEAVE social backend: the complete, deployable definition ships in `supabase/` (SQL schema + the `functions/cleave-auth` edge function). **Until it is deployed to a Supabase project of your own and `SocialBackend.PROJECT_URL`/`PUBLISHABLE_KEY` (and `AuthType.java`) point at it, the Cleave-account buttons will report a backend error.** The stock configuration still points at the original upstream project, which serves the old, pre-rebrand function name — so CI's live probes for it skip with a visible warning instead of pretending to pass. Microsoft and offline accounts are unaffected.
5. **CurseForge content** needs `CURSEFORGE_API_KEY` (or a `curseforge_key.txt`) at build time; without it the CurseForge source is disabled gracefully. Modrinth is keyless.
6. Release numbering continues from the upstream **2.4.4** lineage — CLEAVE did not reset versioning, so the in-app updater and CI gates stay consistent. Package identity changed (`com.cleave.launcher`), so CLEAVE installs side-by-side with the old launcher and does not migrate its data automatically.
7. The `docs/` folder is the upstream marketing site, carried over with brand substitutions; it is **not** the current CLEAVE website and gets its full refresh separately.
8. Screenshots under `assets/screenshots/` are inherited from the 2.4.4 lineage UI and show the identical layout; fresh CLEAVE-branded captures require a device/emulator run.

See [KNOWN_ISSUES.md](KNOWN_ISSUES.md) for the full list and [CHANGELOG.md](CHANGELOG.md) for what changed in this release.

## Requirements

- Android 9 (API 23) or newer · ARM64 recommended (ARMv7 builds are produced for the testing channel) · 2 GB+ RAM (3 GB+ recommended for modded)
- A Mojang/Microsoft account for official play, **or** an unofficial offline profile (single-player only)
- For building: JDK 17+, Android SDK (compileSdk 36), Android NDK **29.0.14206865**, CMake, and git submodules

## Install

1. Grab the APK from this repository's **[Releases](https://github.com/JIGNESH01-ai/Cleave-launcher/releases)** — `Cleave-Nightly.apk` (testing, prerelease) or a public `v*` release when published.
2. Sideload it (allow "install unknown apps" for your browser/file manager).
3. First run: CLEAVE will offer to import game files from a previous launcher if present; otherwise create a profile (Microsoft sign-in or offline) and press **PLAY**. Minecraft Java is downloaded from Mojang's servers on first launch.

## Build from source

```bash
git clone --recursive https://github.com/JIGNESH01-ai/Cleave-launcher.git
cd Cleave-launcher
git submodule update --init --recursive   # glfw, mojoexec, SDL

# Android SDK: platform 36, build-tools 36.x, ndk;29.0.14206865, cmake
./gradlew :app_pojavlauncher:assembleFullDebug        # full debug APK
./gradlew :app_pojavlauncher:assembleNoruntimeNightly # testing APK (this is what CI builds)
./gradlew :cleave_client_mod:jar                      # in-game client mod JAR
```

Debug APK output: `app_pojavlauncher/build/outputs/apk/full/debug/app_pojavlauncher-full-debug.apk`.

CI (`.github/workflows/`):
- `cleave-debug.yml` — on every push to `v3_openjdk` / `arena/**`: runs the smoke/unit suite, builds `Cleave-Nightly.apk`, verifies it (zipalign, apksigner, package name), uploads the artifact and publishes the `debug-latest` prerelease
- `android.yml` / `abi-splits.yml` — debug APK(s) and ABI splits
- `release.yml` — manual, confirmation-gated public release (requires `CLEAVE_KEYSTORE_*` secrets; refuses to publish unsigned builds)
- `cleave-release.yml` — release channel definition

## Project layout (high level)

```
app_pojavlauncher/   # the Android app (Java + Kotlin + Compose)
  src/main/java/net/kdt/pojavlaunch/           # PojavLauncher-derived core
  src/main/java/net/kdt/pojavlaunch/cleave/    # CLEAVE UI layer, bridges, theme
cleave_client_mod/   # in-game client mod (Fabric, runs inside the game JVM)
forge_installer/     # Forge/NeoForge installer shim
glfw/ mojoexec/ sdl/ # native windowing stacks (submodules)
tests/               # smoke & unit tests run by CI
docs/                # upstream website (carried over; see KNOWN_ISSUES.md)
```

## Credits

- **CLEAVE Launcher** is made by **JIGNESHGAMING**.
- Core technology: [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher) / [MojoLauncher](https://github.com/MojoLauncher) (PojavLauncherTeam and contributors), GPL-3.0.
- Interface lineage: [Zalith Launcher 2](https://github.com/MovTery/ZalithLauncher2) by MovTery and contributors; mirai-launcher- fork lineage, GPL-3.0.
- Rendering: MobileGlues, ANGLE, Mesa (licenses under `app_pojavlauncher/src/main/assets/licenses/`).
- Auth: Microsoft OAuth + [authlib-injector](https://github.com/yushijinhun/authlib-injector) (where configured).

**Minecraft is a trademark of Mojang Studios. CLEAVE Launcher is not affiliated with, endorsed by or sold by Mojang Studios or Microsoft.**

## Licence

GPL-3.0 — see [LICENSE](LICENSE). All third-party components remain under their own licences; attribution files ship in-app and under `app_pojavlauncher/src/main/assets/licenses/`.
