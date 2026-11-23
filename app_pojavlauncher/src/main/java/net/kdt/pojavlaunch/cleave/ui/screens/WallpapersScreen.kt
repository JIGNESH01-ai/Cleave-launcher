/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Wallpapers page, a line by line port of mirai-launcher-
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/main/WallpaperPage.kt
 * Layout, order and behaviour are the ones of that file: the floating control bar (accent swatch,
 * "N Minecraft HD Wallpapers & Dynamic Themes", "Active: X • Theme: Y" line, "Dim n%" slider,
 * "Solid Dark" button, accent "+ Custom" button) and the three column grid of preset cards whose
 * image carries the theme chip and whose footer carries the title and the biome • shader line.
 * The AlertDialog-free section of the page uses CleaveThemeManager and CleavePreferences, the adapters
 * that replace MiraiThemeManager and AllSettings.
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

package net.kdt.pojavlaunch.cleave.ui.screens

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.CleavePreferences
import net.kdt.pojavlaunch.cleave.components.liquidGlass
import net.kdt.pojavlaunch.cleave.theme.CleaveMetrics
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.cleave.theme.CleaveWallpaperPreset
import net.kdt.pojavlaunch.cleave.components.CleaveAnimations
import net.kdt.pojavlaunch.cleave.components.paintScenePreview
import net.kdt.pojavlaunch.cleave.components.CleaveHdClips
import net.kdt.pojavlaunch.cleave.components.HdClip
import net.kdt.pojavlaunch.cleave.components.rememberClipThumbnail
import net.kdt.pojavlaunch.cleave.components.MEDIA_WALLPAPER_MIME
import net.kdt.pojavlaunch.cleave.components.BundledClip
import net.kdt.pojavlaunch.cleave.components.BundledClips
import net.kdt.pojavlaunch.cleave.components.CleaveAnimation
import net.kdt.pojavlaunch.cleave.components.isAnimationId

/**
 * Which half of the wallpapers page is showing.
 *
 * It lives here rather than in the composable because applying a clip changes the theme, which
 * rebuilds the settings screen around this page; a local remember slot can then come back reset and
 * the page appears to jump to Wallpapers by itself after every tap.
 */
private object WallpaperSection {
    var showAnimations by mutableStateOf(false)
    var initialised = false

    fun initialise(selectedId: String) {
        if (initialised) return
        initialised = true
        showAnimations = CleaveThemeManager.isBundledClipId(selectedId) ||
            CleaveThemeManager.isClipId(selectedId) ||
            isAnimationId(selectedId)
    }
}

@Composable
fun WallpapersScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeAccent = CleaveThemeManager.currentAccent()
    val failedText = stringResource(R.string.cleave_wallpaper_failed)

    val selectedId = CleavePreferences.selectedWallpaper
    // Kept outside the composable: applying a clip switches the theme, which rebuilds this screen,
    // and a rememberSaveable slot could come back initialised to Wallpapers - the page would jump
    // out from under the tap.
    WallpaperSection.initialise(selectedId)
    var showAnimations by remember { mutableStateOf(WallpaperSection.showAnimations) }
    val selectedPreset = remember(selectedId) { CleaveThemeManager.wallpapers.firstOrNull { it.id == selectedId } }
    val selectedAnimation = remember(selectedId) { CleaveAnimations.firstOrNull { it.id == selectedId } }
    val opacitySetting = CleavePreferences.backgroundOpacity

    // Any clip or animated image on the device: they are copied into app storage, so the launcher
    // can show real footage without shipping any of it.
    val clipPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val name = context.contentResolver.query(
                        uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
                    withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: error("The file could not be read")
                        CleaveThemeManager.importClip(context, name, input)
                    }
                }.onFailure { Toast.makeText(context, failedText, Toast.LENGTH_LONG).show() }
            }
        }
    }

    // Reading the folder touches disk, so it happens off the composition. A new import or a delete
    // bumps the theme revision, which re-reads the list.
    val clipRevision = CleaveThemeManager.wallpaperRevision
    var clips by remember(clipRevision) { mutableStateOf<List<java.io.File>>(emptyList()) }
    LaunchedEffect(clipRevision) {
        clips = withContext(Dispatchers.IO) { CleaveThemeManager.clips(context) }
    }
    // The high-resolution pack is saved into the same folder as the player's own imports, so it is
    // taken back out of that list here: those five are drawn by their own cards, which know whether
    // they have been downloaded yet.
    val hdFileNames = remember { CleaveHdClips.clips.map { it.file }.toSet() }
    val importedClips = clips.filterNot { it.name in hdFileNames }
    val gridState = rememberLazyGridState()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching { CleaveThemeManager.applyCustomUri(context, uri) }
                    .onFailure { Toast.makeText(context, failedText, Toast.LENGTH_LONG).show() }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
    ) {
        // ---- header and controls bar ----------------------------------------------------------
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(CleaveRadii.cardSmall),
                    tint = CleaveSurface.glassTint,
                    strength = 0.9f,
                    elevation = CleaveMetrics.glassFloatingElevation
                ),
            shape = RoundedCornerShape(CleaveRadii.cardSmall),
            color = CleaveSurface.panelRaised.copy(alpha = 0.33f),
            border = BorderStroke(CleaveSpacing.hairline, activeAccent.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
            ) {
                // Left side of the bar: the one switch for everything that moves, and the blur
                // meter that governs every wallpaper rather than a single one.
                Surface(
                    shape = RoundedCornerShape(CleaveRadii.compact),
                    color = if (CleavePreferences.themeMotion) activeAccent.copy(alpha = 0.22f) else CleaveSurface.panel,
                    border = BorderStroke(
                        CleaveSpacing.hairline,
                        if (CleavePreferences.themeMotion) activeAccent else CleaveSurface.borderSoft
                    ),
                    onClick = { CleavePreferences.saveThemeMotion(!CleavePreferences.themeMotion) }
                ) {
                    Text(
                        text = stringResource(
                            if (CleavePreferences.themeMotion) R.string.cleave_wallpaper_motion_on
                            else R.string.cleave_wallpaper_motion_off
                        ),
                        color = if (CleavePreferences.themeMotion) activeAccent else CleaveSurface.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = CleaveSpacing.smPlus, vertical = CleaveSpacing.smCompact)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.cleave_wallpaper_blur, CleavePreferences.backgroundBlur),
                        color = CleaveSurface.textSecondary,
                        fontSize = 10.sp
                    )
                    Slider(
                        value = CleavePreferences.backgroundBlur.toFloat(),
                        onValueChange = { CleavePreferences.saveBackgroundBlur(it.toInt()) },
                        valueRange = 0f..20f,
                        modifier = Modifier
                            .width(104.dp)
                            .height(22.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = activeAccent,
                            activeTrackColor = activeAccent,
                            inactiveTrackColor = CleaveSurface.canvas
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (showAnimations) {
                            stringResource(
                                R.string.cleave_wallpaper_header,
                                BundledClips.size + CleaveHdClips.clips.size + importedClips.size
                            )
                        } else {
                            stringResource(R.string.cleave_wallpaper_header, CleaveThemeManager.wallpapers.size)
                        },
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            selectedAnimation != null -> "${selectedAnimation.title} \u00B7 ${selectedAnimation.biome}"
                            selectedPreset != null -> stringResource(
                                R.string.cleave_wallpaper_active_line,
                                selectedPreset.title,
                                selectedPreset.themeName
                            )
                            selectedId == CleaveThemeManager.WALLPAPER_CUSTOM -> stringResource(R.string.cleave_wallpaper_active_custom)
                            else -> stringResource(R.string.cleave_wallpaper_active_dark)
                        },
                        color = activeAccent,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Dim slider, compact like the Mirai page
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
                ) {
                    Text(
                        text = stringResource(R.string.cleave_wallpaper_dim, opacitySetting),
                        color = CleaveSurface.textSecondary,
                        fontSize = 11.sp
                    )
                    Slider(
                        value = opacitySetting.toFloat(),
                        onValueChange = { CleavePreferences.saveBackgroundOpacity(it.toInt()) },
                        valueRange = 35f..95f,
                        modifier = Modifier
                            .width(96.dp)
                            .height(24.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = activeAccent,
                            activeTrackColor = activeAccent,
                            inactiveTrackColor = CleaveSurface.canvas
                        )
                    )
                }

                // Solid dark
                Surface(
                    shape = RoundedCornerShape(CleaveRadii.compact),
                    color = if (selectedId == CleaveThemeManager.WALLPAPER_NONE) activeAccent.copy(alpha = 0.2f) else CleaveSurface.panel,
                    border = BorderStroke(
                        CleaveSpacing.hairline,
                        if (selectedId == CleaveThemeManager.WALLPAPER_NONE) activeAccent else CleaveSurface.borderSoft
                    ),
                    onClick = { CleaveThemeManager.clearWallpaper(context) }
                ) {
                    Text(
                        text = stringResource(R.string.cleave_wallpaper_solid_dark),
                        color = if (selectedId == CleaveThemeManager.WALLPAPER_NONE) activeAccent else CleaveSurface.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = CleaveSpacing.smPlus, vertical = CleaveSpacing.smCompact)
                    )
                }

                // Import a custom image
                Surface(
                    shape = RoundedCornerShape(CleaveRadii.compact),
                    color = activeAccent,
                    contentColor = CleaveSurface.onAccent,
                    onClick = { picker.launch("image/*") }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = CleaveSpacing.smPlus, vertical = CleaveSpacing.smCompact),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = stringResource(R.string.cleave_wallpaper_custom_button),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // ---- two grids: the 25 photographs, and the 25 animated scenes -----------------------
        // Both live on this page; the buttons switch which one is showing.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                stringResource(R.string.cleave_wallpaper_tab_wallpaper) to false,
                stringResource(R.string.cleave_wallpaper_tab_animations) to true
            ).forEach { (label, animations) ->
                val active = showAnimations == animations
                Surface(
                    shape = RoundedCornerShape(CleaveRadii.compact),
                    color = if (active) activeAccent.copy(alpha = 0.22f) else CleaveSurface.panel,
                    border = BorderStroke(
                        CleaveSpacing.hairline,
                        if (active) activeAccent else CleaveSurface.borderSoft
                    ),
                    onClick = {
                        showAnimations = animations
                        WallpaperSection.showAnimations = animations
                        if (!animations) scope.launch { gridState.scrollToItem(0) }
                    }
                ) {
                    Text(
                        text = label,
                        color = if (active) activeAccent else CleaveSurface.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.xsPlus)
                    )
                }
            }
            // Adding your own clip sits with the section buttons, so nothing pushes the grid down.
            Surface(
                shape = RoundedCornerShape(CleaveRadii.compact),
                color = if (showAnimations) activeAccent else CleaveSurface.panel,
                contentColor = if (showAnimations) CleaveSurface.onAccent else CleaveSurface.textPrimary,
                border = BorderStroke(
                    CleaveSpacing.hairline,
                    if (showAnimations) activeAccent else CleaveSurface.borderSoft
                ),
                onClick = { clipPicker.launch(MEDIA_WALLPAPER_MIME) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.xsPlus),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = stringResource(R.string.cleave_wallpaper_video_button),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = CleaveSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus),
            verticalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
        ) {
            if (showAnimations) {
                // Real recorded footage first: it is what the section is for.
                items(items = BundledClips, key = { it.id }) { clip ->
                    BundledClipCard(
                        clip = clip,
                        isSelected = selectedId == CleaveThemeManager.bundledClipId(clip),
                        onClick = { CleaveThemeManager.applyBundledClip(clip) }
                    )
                }
                items(items = CleaveHdClips.clips, key = { "hd:" + it.id }) { hd ->
                    val saved = CleaveHdClips.file(context, hd)
                    val downloaded = CleaveHdClips.isDownloaded(context, hd)
                    HdClipCard(
                        clip = hd,
                        downloaded = downloaded,
                        downloading = CleaveHdClips.downloadingId == hd.id,
                        progress = CleaveHdClips.progress,
                        isSelected = downloaded && selectedId == CleaveThemeManager.clipId(saved),
                        onClick = {
                            if (downloaded) {
                                CleaveThemeManager.applyClip(saved)
                            } else if (CleaveHdClips.downloadingId == null) {
                                scope.launch {
                                    val file = CleaveHdClips.download(context, hd)
                                    if (file != null) {
                                        CleaveThemeManager.applyClip(file)
                                    } else {
                                        val message = CleaveHdClips.failure ?: "The download failed"
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        CleaveHdClips.clearFailure()
                                    }
                                }
                            }
                        }
                    )
                }
                items(items = importedClips, key = { "clip:" + it.absolutePath }) { clip ->
                    ClipCard(
                        file = clip,
                        isSelected = selectedId == CleaveThemeManager.clipId(clip),
                        onClick = { CleaveThemeManager.applyClip(clip) },
                        onDelete = { CleaveThemeManager.deleteClip(context, clip) }
                    )
                }
            } else {
                items(
                    items = CleaveThemeManager.wallpapers,
                    key = { it.id }
                ) { preset ->
                    WallpaperPresetCard(
                        preset = preset,
                        isSelected = selectedId == preset.id,
                        onClick = { CleaveThemeManager.applyPreset(preset) }
                    )
                }
            }
        }
    }
}

/**
 * One of the 25 animated scenes in the grid.
 *
 * The preview is painted from the scene's own sky and accent rather than run live: twenty five
 * running scenes at once would cost far more than it shows, and tapping the card puts the real
 * thing full screen immediately.
 */
@Composable
private fun AnimationCard(animation: CleaveAnimation, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(CleaveRadii.control)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = CleaveSurface.panelRaised,
        border = BorderStroke(
            width = if (isSelected) 2.dp else CleaveSpacing.hairline,
            color = if (isSelected) animation.accent else CleaveSurface.panelRaised
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                // The preview is the scene itself, painted at a fixed moment: sky, its own light,
                // the hills and the water, exactly as it will look behind the launcher. A still of
                // the real thing reads far better than any gradient stand-in.
                Canvas(Modifier.fillMaxSize()) {
                    paintScenePreview(animation)
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(CleaveSpacing.xsPlus),
                    shape = RoundedCornerShape(CleaveRadii.compact),
                    color = CleaveSurface.canvas.copy(alpha = 0.55f)
                ) {
                    Text(
                        text = stringResource(R.string.cleave_animation_live),
                        color = animation.accent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(CleaveSpacing.xsPlus)) {
                Text(
                    text = animation.title,
                    color = CleaveSurface.textPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = animation.biome,
                    color = CleaveSurface.textSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Footage that ships with the launcher: a recorded Minecraft scene, previewed with a real frame
 * from the clip rather than a gradient.
 */
@Composable
private fun BundledClipCard(clip: BundledClip, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(CleaveRadii.control)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = CleaveSurface.panelRaised,
        border = BorderStroke(
            width = if (isSelected) 2.dp else CleaveSpacing.hairline,
            color = if (isSelected) clip.accent else CleaveSurface.panelRaised
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(CleaveSurface.panelTrack)
            ) {
                // A real frame of this exact clip, shipped at 640x360 so the grid shows the scene
                // immediately instead of spinning up a video decoder per card.
                Image(
                    painter = painterResource(clip.poster),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(CleaveSpacing.xsPlus),
                    shape = RoundedCornerShape(CleaveRadii.compact),
                    color = CleaveSurface.canvas.copy(alpha = 0.62f)
                ) {
                    Text(
                        text = stringResource(R.string.cleave_clip_real),
                        color = clip.accent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(CleaveSpacing.xsPlus)) {
                Text(
                    text = clip.title,
                    color = CleaveSurface.textPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = clip.subtitle,
                    color = CleaveSurface.textSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * One clip of the high-resolution pack.
 *
 * Until it is downloaded the card is a placeholder that says what it is, how big and how sharp, and
 * downloads it on tap - the pack is far too large to put in everybody's APK. Once downloaded it
 * behaves exactly like the player's own imported clip, because that is what it is: the original
 * file, byte for byte, in the same folder.
 */
@Composable
private fun HdClipCard(
    clip: HdClip,
    downloaded: Boolean,
    downloading: Boolean,
    progress: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(CleaveRadii.control)
    val context = LocalContext.current
    val thumbnail = if (downloaded) rememberClipThumbnail(java.io.File(CleaveThemeManager.clipDir(context), clip.file)) else null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = CleaveSurface.panelRaised,
        border = BorderStroke(
            width = if (isSelected) 2.dp else CleaveSpacing.hairline,
            color = if (isSelected) clip.accent else CleaveSurface.panelRaised
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(CleaveSurface.panelTrack),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Not downloaded yet. Without a frame the card used to be a coloured square with
                    // a cloud on it, which said nothing about what the animation looks like; the
                    // poster shipped with the pack is a real frame of this exact clip.
                    Image(
                        painter = painterResource(clip.poster),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(34.dp)
                            .background(CleaveSurface.canvas.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_cloud_download),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(CleaveSpacing.xsPlus),
                    shape = RoundedCornerShape(CleaveRadii.compact),
                    color = CleaveSurface.canvas.copy(alpha = 0.62f)
                ) {
                    Text(
                        text = "HD",
                        color = clip.accent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                if (downloading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CleaveSurface.canvas.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = clip.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(clip.accent.copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(clip.accent)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(CleaveSpacing.xsPlus)) {
                Text(
                    text = clip.title,
                    color = CleaveSurface.textPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (downloaded) "${clip.subtitle} · ${clip.seconds}" else "${clip.resolution} · ${clip.seconds} · ${clip.megabytes}",
                    color = CleaveSurface.textSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * A wallpaper the user brought in: real footage or a real looping animation.
 *
 * The card shows a frame from the clip itself, so the grid mixes their own clips with the drawn
 * scenes and everything looks like what it will be.
 */
@Composable
private fun ClipCard(
    file: java.io.File,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(CleaveRadii.control)
    val accent = CleaveThemeManager.currentAccent()
    val thumbnail = rememberClipThumbnail(file)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = CleaveSurface.panelRaised,
        border = BorderStroke(
            width = if (isSelected) 2.dp else CleaveSpacing.hairline,
            color = if (isSelected) accent else CleaveSurface.panelRaised
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(CleaveSurface.panelTrack)
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // The frame is read out of the player's own file the first time it is shown;
                    // until then the card is a wash of the theme accent rather than an empty box.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(accent.copy(alpha = 0.34f), accent.copy(alpha = 0.10f))
                                )
                            )
                    )
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_play_arrow_filled),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(22.dp)
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(CleaveSpacing.xsPlus),
                    shape = RoundedCornerShape(CleaveRadii.compact),
                    color = CleaveSurface.canvas.copy(alpha = 0.62f)
                ) {
                    Text(
                        text = stringResource(R.string.cleave_clip_badge),
                        color = accent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                // Removing a clip lives on the corner of its own card, never in a menu.
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(CleaveSpacing.xsPlus)
                        .size(18.dp)
                        .clickable(onClick = onDelete),
                    shape = RoundedCornerShape(CleaveRadii.compact),
                    color = CleaveSurface.canvas.copy(alpha = 0.62f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "\u00D7",
                            color = CleaveSurface.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(CleaveSpacing.xsPlus)) {
                Text(
                    text = file.nameWithoutExtension,
                    color = CleaveSurface.textPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.cleave_clip_kind),
                    color = CleaveSurface.textSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WallpaperPresetCard(preset: CleaveWallpaperPreset, isSelected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    // Same thumbnail policy as the Mirai page: decoded from the bundled asset at card size.
    val thumbnail by produceState<ImageBitmap?>(initialValue = null, preset.assetPath) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open(preset.assetPath).use { stream ->
                    BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = 8 })
                        ?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CleaveRadii.control))
            .liquidGlass(
                shape = RoundedCornerShape(CleaveRadii.control),
                tint = if (isSelected) preset.accentColor else CleaveSurface.glassTint,
                strength = if (isSelected) 0.95f else 0.7f,
                elevation = if (isSelected) CleaveMetrics.glassSelectedElevation else CleaveMetrics.glassSubtleElevation
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CleaveRadii.control),
        color = CleaveSurface.panelRaised.copy(alpha = 0.34f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else CleaveSpacing.hairline,
            color = if (isSelected) preset.accentColor else CleaveSurface.panelRaised
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                val image = thumbnail
                if (image != null) {
                    Image(
                        bitmap = image,
                        contentDescription = preset.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(CleaveSurface.panelRaised))
                }

                // Bottom gradient so the theme chip stays readable
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, CleaveSurface.canvas.copy(alpha = 0.8f))
                            )
                        )
                )

                // Theme colour pill, bottom left of the image
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(CleaveSpacing.smCompact),
                    shape = RoundedCornerShape(CleaveRadii.micro),
                    color = CleaveSurface.panelGlassTint.copy(alpha = 0.66f),
                    border = BorderStroke(CleaveSpacing.hairline, preset.accentColor.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = CleaveSpacing.smCompact, vertical = CleaveSpacing.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(preset.accentColor)
                        )
                        Text(
                            text = preset.themeName,
                            color = preset.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Active badge, top right of the image
                if (isSelected) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(CleaveSpacing.smCompact),
                        shape = RoundedCornerShape(CleaveRadii.micro),
                        color = preset.accentColor,
                        contentColor = CleaveSurface.onAccent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = CleaveSpacing.smCompact, vertical = CleaveSpacing.xxs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.tiny)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.cleave_ic_check),
                                contentDescription = stringResource(R.string.cleave_wallpaper_active),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = stringResource(R.string.cleave_wallpaper_active),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CleaveSpacing.smNarrow, vertical = CleaveSpacing.smCompact),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.hairline)
            ) {
                Text(
                    text = preset.title,
                    color = if (isSelected) preset.accentColor else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = preset.biome,
                    color = CleaveSurface.textSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
