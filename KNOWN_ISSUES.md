# Known Issues & Limitations — CLEAVE Launcher

This list is deliberately uncomfortable and complete. If it is not listed here, it is not known.

## Verification gaps
1. **No device-tested game launch as part of the rebrand.** CI compiles the APK, runs ~100 smoke/unit tests and verifies the package, but an actual Minecraft session was not executed. First run on your phone is the real test.
2. **Installers (Fabric/Forge/NeoForge/Quilt/OptiFine)** are code-path-complete but only as reliable as the upstream installer code + network. Loader compatibility with a *specific* Minecraft build is not guaranteed for every combination.
3. **Screenshots** in `assets/screenshots/` and the `docs/` site show the 2.4.4 lineage UI (same layout, old paint). Regenerate on a device for CLEAVE-gold material.
4. **Translated READMEs** (`README_BN.md`, `README_RU.md`) are placeholder pointers to `README.md` — translations are pending.

## Backend dependencies (upstream)
5. **Socials (friends/parties/presence/chat) and "Cleave account" sign-in** use the Supabase project hardcoded in `app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/cleave/social/SocialBackend.kt` (`PROJECT_URL`, `PUBLISHABLE_KEY`) and `AuthType.java`. The **CLEAVE backend contract** (tables, `rpc/cleave_*` functions, the `functions/cleave-auth` edge function) is fully defined in `supabase/` and is what the launcher calls. **The stock `PROJECT_URL` points at the original upstream project, which has not deployed that contract — so Cleave-account sign-in and social features fail there with a clear error until a maintainer deploys `supabase/` to their own Supabase project and re-points the constants.** CI's live probes detect the undeployed function and skip with a `::warning::` instead of failing or pretending. The publishable key is public by design; row-level security is the access control. No telemetry goes anywhere else.
6. **Discord Rich Presence** is opt-in and talks to Discord's IPC from the launcher; it is absent on devices without the Discord app.
7. **CurseForge** content needs `CURSEFORGE_API_KEY` at build time (or `curseforge_key.txt` in the repo root, gitignored). Without it, CurseForge sources report a clear "not configured" state; Modrinth needs no key.

## Packaging & signing
8. **No signed public APK can be produced in the build environment without `CLEAVE_KEYSTORE_FILE` + `CLEAVE_KEYSTORE_PASSWORD`** (+ optional `CLEAVE_KEYSTORE_ALIAS`). Unsigned `release` builds compile but are not publishable; the release workflow enforces this. Signing CI secrets live with the maintainer.
9. **`versionName` stays 2.4.4** for this release (CI gate + updater policy pin). A future 2.5.0/3.0.0 will bump it deliberately.
10. **Side-by-side install**: CLEAVE (`com.cleave.launcher`) does not import data from the legacy `net.al.aerixlauncher` install automatically; use the in-app import flow / Settings import on Android 10+.

## Behavioural notes
11. **Landscape is mandatory by design** (`screenOrientation="sensorLandscape"`): portrait is unsupported everywhere, per project requirement.
12. **Offline accounts are unofficial** and clearly marked; they cannot authenticate to Microsoft services.
13. **Servers online status** is only shown when a Server List Ping actually succeeded; otherwise CLEAVE shows the server as unknown, not online.
14. **Low-end devices**: shaders are never forced; dynamic resolution and performance presets are real option writers, but FPS improvements depend on hardware and pack choices — no guarantees are made.
15. **ARM64** is the supported target. ARMv7 nightly exists but Java 17+ runtimes need 64-bit; some newest versions will not run on 32-bit-only devices.
16. **`docs/`** marketing site is upstream material with brand substitutions only; it is not an official CLEAVE site yet.
17. **In-game CLEAVE client mod** (`cleave_client_mod`) is built by CI as a test artifact; its in-game features (adaptive render distance, visual overrides) depend on the Fabric runtime matching your Minecraft version.
