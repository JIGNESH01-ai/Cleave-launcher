/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Ported from mirai-launcher- (Zalith Launcher 2 fork):
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/theme/MiraiThemeManager.kt
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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.cleave.bridge.CleaveColorTheme
import net.kdt.pojavlaunch.cleave.bridge.CleavePreferences
import java.io.File
import android.graphics.Color as AndroidColor

data class CleaveWallpaperPreset(
    val id: String,
    val title: String,
    val biome: String,
    val assetPath: String,
    val themeName: String,
    val accentColor: Color
)

object CleaveThemeManager {
    const val WALLPAPER_NONE = "none"
    const val WALLPAPER_CUSTOM = "custom"
    /** Imported wallpapers are addressed as `clip:<file name>`, so several can coexist. */
    const val WALLPAPER_CLIP_PREFIX = "clip:"
    /** Footage that ships with the launcher, addressed as `clipasset:<id>`. */
    const val WALLPAPER_CLIP_ASSET_PREFIX = "clipasset:"
    private const val CLIP_DIR = "cleave_clips"
    private const val CUSTOM_WALLPAPER_FILE = "cleave_custom_wallpaper"

    /** Bumped every time the wallpaper changes so the background reloads its bitmap. */
    var wallpaperRevision by mutableIntStateOf(0)
        private set

    val wallpapers: List<CleaveWallpaperPreset> = listOf(
        CleaveWallpaperPreset("wp_01_lush_caves", "Lush Glowberry Cave", "Lush Caves • Complementary Shaders", "wallpapers/wp_01_lush_caves.webp", "Lush Teal", CleaveSurface.accentDefault),
        CleaveWallpaperPreset("wp_02_cherry_blossom", "Cherry Blossom Grove", "Cherry Grove • BSL Shaders", "wallpapers/wp_02_cherry_blossom.webp", "Sakura Pink", Color(0xFFF472B6)),
        CleaveWallpaperPreset("wp_03_sunset_river", "Golden Sunset River", "River Valley • SEUS Renewed", "wallpapers/wp_03_sunset_river.webp", "Sunset Amber", Color(0xFFF59E0B)),
        CleaveWallpaperPreset("wp_04_crystal_river", "Crystal River Forest", "Birch & Oak River • Complementary", "wallpapers/wp_04_crystal_river.webp", "River Cyan", Color(0xFF06B6D4)),
        CleaveWallpaperPreset("wp_05_nether_fortress", "Nether Lava Cavern", "Nether Wastes • RTX Shaders", "wallpapers/wp_05_nether_fortress.webp", "Magma Orange", Color(0xFFF97316)),
        CleaveWallpaperPreset("wp_06_the_end", "The End Obsidian Pillars", "The End • Sildur's Vibrant", "wallpapers/wp_06_the_end.webp", "Ender Amethyst", Color(0xFFA855F7)),
        CleaveWallpaperPreset("wp_07_ocean_arch", "Sunlight Ocean Arch", "Warm Ocean • Complementary", "wallpapers/wp_07_ocean_arch.webp", "Ocean Azure", Color(0xFF38BDF8)),
        CleaveWallpaperPreset("wp_08_moonlit_lake", "Moonlit Birch Lake", "Night Forest • Nostalgia Shaders", "wallpapers/wp_08_moonlit_lake.webp", "Moonlight Indigo", Color(0xFF818CF8)),
        CleaveWallpaperPreset("wp_09_flower_meadow", "Sunrise Flower Meadow", "Flower Forest • BSL Shaders", "wallpapers/wp_09_flower_meadow.webp", "Rose Meadow", Color(0xFFEC4899)),
        CleaveWallpaperPreset("wp_10_spruce_mist", "Spruce Lake Morning Mist", "Old Growth Taiga • Bliss Shaders", "wallpapers/wp_10_spruce_mist.webp", "Taiga Lime", Color(0xFF84CC16)),
        CleaveWallpaperPreset("wp_11_cherry_bee", "Honeybee Cherry Canopy", "Cherry Canopy • Complementary", "wallpapers/wp_11_cherry_bee.webp", "Honey Gold", Color(0xFFFBBF24)),
        CleaveWallpaperPreset("wp_12_snowy_valley", "Snowy Peaks & Cherry Valley", "Jagged Peaks • Distant Horizons", "wallpapers/wp_12_snowy_valley.webp", "Alpine Sky", Color(0xFF60A5FA)),
        CleaveWallpaperPreset("wp_13_frozen_glacier", "Frozen Glacier Ridge", "Frozen Peaks • Complementary", "wallpapers/wp_13_frozen_glacier.webp", "Glacier Frost", Color(0xFF22D3EE)),
        CleaveWallpaperPreset("wp_14_cozy_village", "Cozy Plains Village", "Plains Village • SEUS PTGI", "wallpapers/wp_14_cozy_village.webp", "Harvest Emerald", Color(0xFF10B981)),
        CleaveWallpaperPreset("wp_15_end_portal", "End Portal Starfield", "Stronghold Portal • Void Shaders", "wallpapers/wp_15_end_portal.webp", "Portal Teal", Color(0xFF14B8A6)),
        CleaveWallpaperPreset("wp_16_ocean_monument", "Deep Ocean Monument", "Deep Ocean • Prismarine Shaders", "wallpapers/wp_16_ocean_monument.webp", "Prismarine Blue", Color(0xFF3B82F6)),
        CleaveWallpaperPreset("wp_17_night_clouds", "Starry Night River", "Plains Night • Solas Shaders", "wallpapers/wp_17_night_clouds.webp", "Starlight Violet", Color(0xFF8B5CF6)),
        CleaveWallpaperPreset("wp_18_crater_harbor", "Cherry Crater Village", "Cherry Basin • Vanilla+ Shaders", "wallpapers/wp_18_crater_harbor.webp", "Coral Blossom", Color(0xFFFB7185)),
        CleaveWallpaperPreset("wp_19_sinkhole_falls", "Lush Sinkhole Waterfall", "Dripstone & Lush • Iris Shaders", "wallpapers/wp_19_sinkhole_falls.webp", "Waterfall Mint", Color(0xFF2DD4BF)),
        CleaveWallpaperPreset("wp_20_sakura_sunbeams", "Sakura Sunbeams", "Cherry Glade • Complementary", "wallpapers/wp_20_sakura_sunbeams.webp", "Orchid Sunbeam", Color(0xFFE879F9)),
        CleaveWallpaperPreset("wp_21_crimson_forest", "Crimson Forest Embers", "Nether Crimson Forest • Nether Shaders", "wallpapers/wp_21_crimson_forest.webp", "Crimson Red", Color(0xFFE11D48)),
        CleaveWallpaperPreset("wp_22_amethyst_geode", "Amethyst Geode Lake", "Amethyst Geode • Crystal Shaders", "wallpapers/wp_22_amethyst_geode.webp", "Amethyst Bloom", Color(0xFFA855F7)),
        CleaveWallpaperPreset("wp_23_bamboo_dawn", "Bamboo Sunrise", "Bamboo Jungle • Sunrise Shaders", "wallpapers/wp_23_bamboo_dawn.webp", "Bamboo Dawn", Color(0xFF84CC16)),
        CleaveWallpaperPreset("wp_24_badlands_canyon", "Badlands Canyon River", "Badlands Mesa • Canyon Shaders", "wallpapers/wp_24_badlands_canyon.webp", "Terracotta Sunset", Color(0xFFF97316)),
        CleaveWallpaperPreset("wp_25_deep_dark", "Deep Dark Sculk City", "Deep Dark • Sculk Shaders", "wallpapers/wp_25_deep_dark.webp", "Sculk Abyss", Color(0xFF06B6D4)),
        CleaveWallpaperPreset("cleave_twilight", "CLEAVE Twilight", "A cozy voxel village at dusk", "wallpapers/cleave_twilight.jpg", "Mint", CleaveSurface.accentDefault),
    )

    val selectedWallpaperId: String
        get() = CleavePreferences.selectedWallpaper

    fun customWallpaperFile(context: Context): File = File(context.filesDir, CUSTOM_WALLPAPER_FILE)

    /** Where imported wallpapers live. They survive restarts and cost the APK nothing. */
    fun clipDir(context: Context): File = File(context.filesDir, CLIP_DIR)

    fun clips(context: Context): List<File> =
        clipDir(context).listFiles()?.filter { it.isFile }?.sortedBy { it.name.lowercase() } ?: emptyList()

    fun isClipId(id: String): Boolean = id.startsWith(WALLPAPER_CLIP_PREFIX)

    fun clipFile(context: Context, id: String): File? {
        if (!isClipId(id)) return null
        val name = id.removePrefix(WALLPAPER_CLIP_PREFIX)
        return clips(context).firstOrNull { it.name == name }
    }

    fun clipId(file: File): String = WALLPAPER_CLIP_PREFIX + file.name

    fun isBundledClipId(id: String): Boolean = id.startsWith(WALLPAPER_CLIP_ASSET_PREFIX)

    fun bundledClipId(clip: net.kdt.pojavlaunch.cleave.components.BundledClip): String =
        WALLPAPER_CLIP_ASSET_PREFIX + clip.id

    /** Applies footage that ships with the launcher. Its own palette becomes the accent. */
    fun applyBundledClip(clip: net.kdt.pojavlaunch.cleave.components.BundledClip) {
        CleavePreferences.saveSelectedWallpaper(bundledClipId(clip))
        CleavePreferences.saveCustomColor(clip.accent.toArgb())
        CleavePreferences.saveColorTheme(CleaveColorTheme.CUSTOM)
        if (CleavePreferences.backgroundOpacity >= 95) CleavePreferences.saveBackgroundOpacity(72)
        wallpaperRevision++
    }

    @Composable
    fun currentAccent(): Color = if (CleavePreferences.colorTheme == CleaveColorTheme.CUSTOM) {
        Color(CleavePreferences.customColor)
    } else {
        MaterialTheme.colorScheme.primary
    }

    /** Canvas tint drawn over the wallpaper; opacity follows the CLEAVE background opacity slider. */
    fun screenScrim(): Color {
        if (selectedWallpaperId == WALLPAPER_NONE) return CleaveSurface.canvas
        val opacity = CleavePreferences.backgroundOpacity
        return CleaveSurface.canvas.copy(alpha = (100 - opacity).coerceIn(8, 80) / 100f)
    }

    fun applyPreset(preset: CleaveWallpaperPreset) {
        CleavePreferences.saveSelectedWallpaper(preset.id)
        CleavePreferences.saveCustomColor(preset.accentColor.toArgb())
        CleavePreferences.saveColorTheme(CleaveColorTheme.CUSTOM)
        if (CleavePreferences.backgroundOpacity >= 95) CleavePreferences.saveBackgroundOpacity(72)
        wallpaperRevision++
    }

    /**
     * Imports a clip or animated image and makes it the wallpaper.
     *
     * Real footage and real looped animation, brought by the user: the launcher itself ships none,
     * so nothing is added to the download, and any number of them can be kept.
     */
    fun importClip(context: Context, displayName: String?, source: java.io.InputStream): File {
        val file = net.kdt.pojavlaunch.cleave.components.importMediaWallpaper(
            clipDir(context), displayName, source
        )
        applyClip(file)
        return file
    }

    fun applyClip(file: File) {
        CleavePreferences.saveSelectedWallpaper(clipId(file))
        wallpaperRevision++
    }

    fun deleteClip(context: Context, file: File) {
        val wasActive = CleavePreferences.selectedWallpaper == clipId(file)
        file.delete()
        if (wasActive) {
            CleavePreferences.saveSelectedWallpaper(wallpapers.first().id)
            wallpaperRevision++
        }
    }

    /** Applies one of the 25 animated wallpapers: same accent behaviour as a photographic preset. */
    fun applyAnimation(animation: net.kdt.pojavlaunch.cleave.components.CleaveAnimation) {
        CleavePreferences.saveSelectedWallpaper(animation.id)
        CleavePreferences.saveCustomColor(animation.accent.toArgb())
        CleavePreferences.saveColorTheme(CleaveColorTheme.CUSTOM)
        if (CleavePreferences.backgroundOpacity >= 95) CleavePreferences.saveBackgroundOpacity(72)
        wallpaperRevision++
    }

    fun clearWallpaper(context: Context) {
        customWallpaperFile(context).delete()
        CleavePreferences.saveSelectedWallpaper(WALLPAPER_NONE)
        CleavePreferences.saveCustomColor(CleaveSurface.accentDefault.toArgb())
        CleavePreferences.saveColorTheme(CleaveColorTheme.CLEAVE)
        wallpaperRevision++
    }

    suspend fun applyCustomUri(context: Context, uri: Uri) {
        val accent = withContext(Dispatchers.IO) {
            val target = customWallpaperFile(context)
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            runCatching {
                val options = BitmapFactory.Options().apply { inSampleSize = 8 }
                val bmp = BitmapFactory.decodeFile(target.absolutePath, options)
                if (bmp != null) {
                    val color = extractVibrantAccent(bmp)
                    bmp.recycle()
                    color
                } else CleaveSurface.accentDefault
            }.getOrDefault(CleaveSurface.accentDefault)
        }
        withContext(Dispatchers.Main) {
            CleavePreferences.saveSelectedWallpaper(WALLPAPER_CUSTOM)
            CleavePreferences.saveCustomColor(accent.toArgb())
            CleavePreferences.saveColorTheme(CleaveColorTheme.CUSTOM)
            if (CleavePreferences.backgroundOpacity >= 95) CleavePreferences.saveBackgroundOpacity(72)
            wallpaperRevision++
        }
    }

    private fun extractVibrantAccent(bitmap: Bitmap): Color {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return CleaveSurface.accentDefault

        val buckets = FloatArray(12)
        val hueSums = FloatArray(12)
        val satSums = FloatArray(12)
        val hsv = FloatArray(3)
        val stepX = (width / 24).coerceAtLeast(1)
        val stepY = (height / 24).coerceAtLeast(1)

        var y = 0
        while (y < height) {
            var x = 0
            while (x < width) {
                AndroidColor.colorToHSV(bitmap.getPixel(x, y), hsv)
                val h = hsv[0]
                val s = hsv[1]
                val v = hsv[2]
                if (s >= 0.25f && v >= 0.20f) {
                    val bucket = (h / 30f).toInt().coerceIn(0, 11)
                    val weight = s * s * v
                    buckets[bucket] += weight
                    hueSums[bucket] += h * weight
                    satSums[bucket] += s * weight
                }
                x += stepX
            }
            y += stepY
        }

        var bestBucket = -1
        var bestScore = 0f
        for (i in 0 until 12) {
            if (buckets[i] > bestScore) {
                bestScore = buckets[i]
                bestBucket = i
            }
        }
        if (bestBucket == -1 || bestScore <= 0.01f) return CleaveSurface.accentDefault

        val avgHue = (hueSums[bestBucket] / bestScore).coerceIn(0f, 360f)
        val avgSat = (satSums[bestBucket] / bestScore).coerceIn(0.65f, 0.90f)
        return Color(AndroidColor.HSVToColor(floatArrayOf(avgHue, avgSat, 0.92f)))
    }
}
