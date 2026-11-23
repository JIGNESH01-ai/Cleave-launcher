/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Ported from mirai-launcher- (Zalith Launcher 2 fork):
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/theme/Theme.kt (cleaveDark scheme)
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/theme/Type.kt
 * Zalith Launcher 2 Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package net.kdt.pojavlaunch.cleave.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import net.kdt.pojavlaunch.cleave.bridge.CleaveColorTheme
import net.kdt.pojavlaunch.cleave.bridge.CleavePreferences

private val defaults = Typography()

private fun TextStyle.cleaveText(family: FontFamily, weight: FontWeight = fontWeight ?: FontWeight.Normal) = copy(
    fontFamily = family,
    fontWeight = weight
)

private fun cleaveTypography(family: FontFamily) = Typography(
    displayLarge = defaults.displayLarge.cleaveText(family, FontWeight.Bold),
    displayMedium = defaults.displayMedium.cleaveText(family, FontWeight.Bold),
    displaySmall = defaults.displaySmall.cleaveText(family, FontWeight.Bold),
    headlineLarge = defaults.headlineLarge.cleaveText(family, FontWeight.Bold),
    headlineMedium = defaults.headlineMedium.cleaveText(family, FontWeight.SemiBold),
    headlineSmall = defaults.headlineSmall.cleaveText(family, FontWeight.SemiBold),
    titleLarge = defaults.titleLarge.cleaveText(family, FontWeight.SemiBold),
    titleMedium = defaults.titleMedium.cleaveText(family, FontWeight.Medium),
    titleSmall = defaults.titleSmall.cleaveText(family, FontWeight.Medium),
    bodyLarge = defaults.bodyLarge.cleaveText(family),
    bodyMedium = defaults.bodyMedium.cleaveText(family),
    bodySmall = defaults.bodySmall.cleaveText(family),
    labelLarge = defaults.labelLarge.cleaveText(family, FontWeight.Medium),
    labelMedium = defaults.labelMedium.cleaveText(family, FontWeight.Medium),
    labelSmall = defaults.labelSmall.cleaveText(family, FontWeight.Medium),
)

/**
 * Built as a function, not a val: its surface roles come from `CleaveSurface.panel*`, which follow
 * the "Layout transparency" setting, so the scheme has to be assembled on every pass rather than
 * baked once into a top-level property.
 */
private fun cleaveDark(): ColorScheme = darkColorScheme(
    primary = CleaveSurface.accentDefault,
    onPrimary = CleaveSurface.onAccentDefault,
    primaryContainer = CleaveSurface.accentContainerDefault,
    onPrimaryContainer = CleaveSurface.onAccentContainer,
    secondary = CleaveSurface.accentSecondary,
    onSecondary = CleaveSurface.canvas,
    secondaryContainer = CleaveSurface.panelRaised,
    onSecondaryContainer = CleaveSurface.textPrimary,
    tertiary = CleaveSurface.success,
    onTertiary = CleaveSurface.canvas,
    tertiaryContainer = CleaveSurface.panelRaised,
    onTertiaryContainer = CleaveSurface.textPrimary,
    error = CleaveSurface.danger,
    onError = CleaveSurface.canvas,
    errorContainer = CleaveSurface.errorDarkContainer,
    onErrorContainer = CleaveSurface.textPrimary,
    background = CleaveSurface.canvas,
    onBackground = CleaveSurface.textPrimary,
    surface = CleaveSurface.panel,
    onSurface = CleaveSurface.textPrimary,
    surfaceVariant = CleaveSurface.panelRaised,
    onSurfaceVariant = CleaveSurface.textSecondary,
    outline = CleaveSurface.textMuted,
    outlineVariant = CleaveSurface.border,
    scrim = Color.Black,
    inverseSurface = CleaveSurface.lightTextPrimary,
    inverseOnSurface = CleaveSurface.lightPanel,
    inversePrimary = CleaveSurface.accentLight,
    surfaceDim = CleaveSurface.canvas,
    surfaceBright = CleaveSurface.panelRaised,
    surfaceContainerLowest = CleaveSurface.canvas,
    surfaceContainerLow = CleaveSurface.panelTrack,
    surfaceContainer = CleaveSurface.canvas,
    surfaceContainerHigh = CleaveSurface.panel,
    surfaceContainerHighest = CleaveSurface.panelRaised
)

/**
 * The CUSTOM theme in Zalith builds a full tonal palette through MaterialKolor. The port keeps the
 * CLEAVE glass surfaces and swaps only the accent roles, which is what the CLEAVE shell renders.
 */
private fun customAccentScheme(accent: Color): ColorScheme {
    val onAccent = if (accent.luminance() > 0.45f) CleaveSurface.onAccentDefault else Color.White
    return cleaveDark().copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accent.copy(alpha = 0.33f),
        onPrimaryContainer = CleaveSurface.textPrimary,
        inversePrimary = accent
    )
}

@Composable
fun CleaveLauncherTheme(content: @Composable () -> Unit) {
    val theme = CleavePreferences.colorTheme
    val custom = CleavePreferences.customColor
    // cleaveDark() reads CleavePreferences.layoutTransparency, so Material's surface roles follow the
    // "Layout transparency" slider the same way the CLEAVE cards do.
    val scheme = when (theme) {
        CleaveColorTheme.CLEAVE -> cleaveDark()
        CleaveColorTheme.CUSTOM -> customAccentScheme(Color(custom))
    }
    CleaveFonts.initialize(LocalContext.current)
    val fontId = CleaveFonts.selectedId
    val typography = remember(fontId) { cleaveTypography(CleaveFonts.selected().family()) }
    MaterialTheme(
        colorScheme = scheme,
        typography = typography,
        content = content
    )
}
