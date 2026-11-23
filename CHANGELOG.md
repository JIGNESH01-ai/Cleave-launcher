# Changelog

## CLEAVE Launcher 2.4.4 — 2026-11-23 (rebrand release)

CLEAVE Launcher debuts on the codebase previously branded Aerix Launcher (itself a PojavLauncher/MojoLauncher descendant). This release is a **full rebrand and launch-readiness pass**, not a feature-equal fork claim.

### Branding
- New official **CL monogram** everywhere: launcher icon (legacy + adaptive + monochrome), splash screen (`black → CLEAVE LAUNCHER → Minecraft Java, Anywhere.`), About page, brand assets
- Launcher name set to **CLEAVE Launcher**; tagline **Minecraft Java, Anywhere.**; creator credit **Made by JIGNESHGAMING** on splash, About (Settings → About), and the new slim launcher footer
- Footer strip added shell-wide: `CLEAVE Launcher · vX.Y.Z … Made by JIGNESHGAMING · YouTube • Instagram • Discord` (YouTube/Instagram/Discord open the official links)
- Accent/theme system unchanged (black, graphite, electric blue; AMOLED mode)

### Identity & packaging
- Package/applicationId changed: `net.al.aerixlauncher` → **`com.cleave.launcher`** (debug suffix `.debug`, pub suffix `.pub`); JNI namespace kept as `net.kdt.pojavlaunch`
- `storageProviderAuthorities`, deep link scheme `cleave-social://`, file-provider authorities, all consistent with new applicationId
- In-app updater now targets **this repository's** GitHub Releases (`JIGNESH01-ai/Cleave-launcher`), with release-asset trust checks updated to match
- Discord invite updated to the official CLEAVE server (`discord.gg/nyn75Un9MR`); YouTube/Instagram official links wired into About and the footer (`Tools.URL_HOME` now points at the repo)

### Functionality
- About screen (both the navigation entry and Settings → About) rewritten: CLEAVE logo, tagline, installed version, **Check for Updates** (real GitHub-releases check with download/install flow), Project Link, **YouTube / Instagram / Discord** buttons, Credits & Licenses with upstream attribution, device/GPU report panel
- `CLEAVE FooterBar` added (shell-wide, 26dp, muted)
- Dead `AboutRowCard` removed; unused upstream avatar asset removed
- Version remains **2.4.4** (deliberate — CI's release gate and updater version-policy pin it; CLEAVE ships side-by-side under a new package id)

### Engineering
- GitHub Actions: `cleave-debug.yml` builds `Cleave-Nightly.apk` on push to `v3_openjdk`/`arena/**`, verifies signature/package, publishes the `debug-latest` prerelease; nightly release anchoring fixed to `GITHUB_SHA`
- `settings.gradle` rootProject.name → `CleaveLauncher`; module `aerix_client_mod` → `cleave_client_mod`
- ~500 files renamed/rebranded (`aerix` → `cleave` across source, resources, tests, CI, docs)
- New assets: adaptive icon set (all densities), monochrome glyph, 512px brand mark, splash bitmap

### Not in this release
- No device-verified game launch yet (see KNOWN_ISSUES.md)
- Signed public APK requires maintainer keystore secrets (`CLEAVE_KEYSTORE_FILE`/`CLEAVE_KEYSTORE_PASSWORD`)
- `docs/` upstream site and stale inherited screenshots remain pending refresh
