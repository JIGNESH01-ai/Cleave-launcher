/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * "Create New Instance" page overhauled following mirai-launcher-:
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/download/game/SelectGameVersionScreen.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/download/game/DownloadGameWithAddonScreen.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/download/game/DownloadGameScreen.kt
 * Every install is executed by net.kdt.pojavlaunch.cleave.bridge.PojavCreateInstanceBridge, which calls the
 * PojavLauncher backend paths of the installer fragments.
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

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.CleaveIconCropper
import net.kdt.pojavlaunch.cleave.bridge.CleaveLoader
import net.kdt.pojavlaunch.cleave.bridge.CleaveLoaderVersion
import net.kdt.pojavlaunch.cleave.bridge.CleaveVersionType
import net.kdt.pojavlaunch.cleave.bridge.PojavCreateInstanceBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavInstaller
import net.kdt.pojavlaunch.cleave.bridge.PojavNavigator
import net.kdt.pojavlaunch.cleave.components.CleaveGhostButton
import net.kdt.pojavlaunch.cleave.components.CleaveImageWell
import net.kdt.pojavlaunch.cleave.components.CleavePrimaryButton
import net.kdt.pojavlaunch.cleave.components.ModrinthCompactSearchField
import net.kdt.pojavlaunch.cleave.components.ModrinthMetaPill
import net.kdt.pojavlaunch.cleave.components.QuietEmptyState
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.cleave.social.SocialActivity

/** Version tabs, the same grouping Pojav's VersionListAdapter builds. */
private val versionTabs = listOf(
    CleaveVersionType.INSTALLED to R.string.mcl_setting_veroption_installed,
    CleaveVersionType.RELEASE to R.string.mcl_setting_veroption_release,
    CleaveVersionType.SNAPSHOT to R.string.mcl_setting_veroption_snapshot,
    CleaveVersionType.OLD_BETA to R.string.mcl_setting_veroption_oldbeta,
    CleaveVersionType.OLD_ALPHA to R.string.mcl_setting_veroption_oldalpha
)

@Composable
fun CreateInstanceScreen(activity: FragmentActivity, onClose: () -> Unit) {
    val accent = CleaveThemeManager.currentAccent()
    val revision = PojavCreateInstanceBridge.versionTableRevision
    val versions = remember(revision) { PojavCreateInstanceBridge.gameVersions() }

    var tab by rememberSaveable { mutableIntStateOf(1) }
    var filter by rememberSaveable { mutableStateOf("") }
    var selectedVersion by remember { mutableStateOf<String?>(null) }
    var loader by remember { mutableStateOf(CleaveLoader.VANILLA) }
    var onlyStable by remember { mutableStateOf(true) }
    var name by rememberSaveable { mutableStateOf("") }
    net.kdt.pojavlaunch.cleave.ui.cleaveLauncherActivity("Building ${name.ifBlank { "a new instance" }.take(36)}")
    var nameEdited by rememberSaveable { mutableStateOf(false) }
    var icon by remember { mutableStateOf<Bitmap?>(null) }
    var loaderVersions by remember { mutableStateOf<List<CleaveLoaderVersion>?>(null) }
    var loaderBuild by remember { mutableStateOf<CleaveLoaderVersion?>(null) }
    var loadingBuilds by remember { mutableStateOf(false) }
    var buildsFailed by remember { mutableStateOf(false) }

    LaunchedEffect(versions) {
        if (selectedVersion == null) {
            selectedVersion = versions.firstOrNull { it.type == CleaveVersionType.RELEASE }?.id
                ?: versions.firstOrNull()?.id
        }
    }

    LaunchedEffect(loader, selectedVersion) {
        loaderBuild = null
        buildsFailed = false
        if (!loader.usesGameVersion) {
            loadingBuilds = true
            loaderVersions = withContext(Dispatchers.IO) {
                runCatching { PojavCreateInstanceBridge.loadLoaderVersions(loader, null) }
                    .onFailure { buildsFailed = true }
                    .getOrDefault(emptyList())
            }
            loadingBuilds = false
        } else if (selectedVersion != null) {
            loadingBuilds = true
            loaderVersions = withContext(Dispatchers.IO) {
                runCatching { PojavCreateInstanceBridge.loadLoaderVersions(loader, selectedVersion) }
                    .onFailure { buildsFailed = true }
                    .getOrDefault(emptyList())
            }
            loadingBuilds = false
        } else {
            loaderVersions = null
        }
    }

    val visibleBuilds = remember(loaderVersions, onlyStable) {
        loaderVersions?.filter { !onlyStable || it.stable } ?: emptyList()
    }
    LaunchedEffect(visibleBuilds) {
        loaderBuild = visibleBuilds.firstOrNull()
    }

    val suggestedName = remember(loader, selectedVersion, loaderBuild) {
        PojavCreateInstanceBridge.suggestedName(loader, selectedVersion, loaderBuild)
    }
    LaunchedEffect(suggestedName) {
        if (!nameEdited) name = suggestedName
    }

    // Vanilla has no builds to choose, so it is ready as soon as a Minecraft version is picked;
    // every other loader also needs the build of the loader itself.
    val canInstall = !PojavCreateInstanceBridge.installing &&
        selectedVersion != null &&
        (!loader.usesGameVersion || !loader.hasBuilds || loaderBuild != null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
    ) {
        // ====================================================================================
        // 1. Compact Single-Line Top Bar
        // ====================================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(CleaveRadii.control),
                    color = CleaveSurface.panel,
                    border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft),
                    onClick = onClose
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_arrow_back),
                            contentDescription = stringResource(R.string.cleave_create_close),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.cleave_create_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
                ModrinthMetaPill(
                    text = buildString {
                        append("MC ")
                        append(selectedVersion ?: "...")
                        append(" \u2022 ")
                        append(loader.displayName)
                        if (loader.usesGameVersion && loaderBuild != null) {
                            append(" ")
                            append(loaderBuild?.label ?: "")
                        }
                    },
                    highlighted = true
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
            ) {
                CleaveGhostButton(
                    text = stringResource(R.string.cleave_create_import_modpack),
                    iconRes = R.drawable.cleave_ic_import_modpack,
                    onClick = {
                        PojavNavigator.openInstaller(activity, PojavInstaller.MODPACK)
                    }
                )
                CleavePrimaryButton(
                    text = stringResource(
                        if (PojavCreateInstanceBridge.installing) R.string.cleave_create_installing
                        else R.string.cleave_create_install
                    ),
                    iconRes = R.drawable.cleave_ic_download,
                    enabled = canInstall,
                    onClick = {
                        SocialActivity.building(name.ifBlank { loader.displayName })
                        PojavCreateInstanceBridge.install(
                            context = activity,
                            loader = loader,
                            gameVersion = selectedVersion,
                            loaderVersion = loaderBuild,
                            name = name,
                            icon = icon
                        ) { created ->
                            if (created) onClose() else PojavInstanceBridge.reload()
                        }
                    }
                )
            }
        }

        // ====================================================================================
        // 2. Full-Height Two-Pane Landscape Content
        //    Left Pane: 100% Dedicated to Minecraft Version Selection (Mirai SelectGameVersionScreen)
        //    Right Pane: Instance Name & Icon + Mod Loader Grid + Loader Version Picker
        // ====================================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
        ) {
            // --------------------------------------------------------------------------------
            // LEFT PANE: Full-Height Minecraft Version List
            // --------------------------------------------------------------------------------
            Surface(
                modifier = Modifier
                    .weight(1.12f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(CleaveRadii.panel),
                color = CleaveSurface.panel,
                border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(CleaveSpacing.smPlus),
                    verticalArrangement = Arrangement.spacedBy(CleaveSpacing.smCompact)
                ) {
                    // Category filter pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        versionTabs.forEachIndexed { index, (_, labelRes) ->
                            val active = index == tab
                            Surface(
                                shape = CircleShape,
                                color = if (active) accent.copy(alpha = 0.22f) else CleaveSurface.panelRaised,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (active) accent else CleaveSurface.borderSoft
                                ),
                                onClick = { tab = index }
                            ) {
                                Text(
                                    text = stringResource(labelRes),
                                    color = if (active) accent else CleaveSurface.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Compact 36dp search field
                    ModrinthCompactSearchField(
                        value = filter,
                        onValueChange = { filter = it },
                        placeholder = stringResource(R.string.cleave_create_filter_hint),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val tabType = versionTabs[tab].first
                    val rows = remember(versions, tabType, filter) {
                        versions.filter { it.type == tabType }
                            .filter { filter.isBlank() || it.id.contains(filter.trim(), ignoreCase = true) }
                            .map { it.id }
                    }

                    if (rows.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            QuietEmptyState(
                                iconRes = R.drawable.cleave_ic_package_2_outlined,
                                text = stringResource(
                                    if (filter.isBlank() && versions.isEmpty()) R.string.cleave_create_versions_empty
                                    else R.string.cleave_create_filter_empty
                                )
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(rows, key = { it }) { id ->
                                VersionRow(
                                    versionId = id,
                                    label = if (tabType == CleaveVersionType.INSTALLED) id
                                    else stringResource(R.string.cleave_create_loader_version, "Minecraft", id),
                                    selected = id == selectedVersion,
                                    accent = accent,
                                    onClick = { selectedVersion = id }
                                )
                            }
                        }
                    }
                }
            }

            // --------------------------------------------------------------------------------
            // RIGHT PANE: Instance Name & Icon + Mod Loader Grid + Loader Version Picker
            // --------------------------------------------------------------------------------
            Surface(
                modifier = Modifier
                    .weight(0.88f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(CleaveRadii.panel),
                color = CleaveSurface.panel,
                border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(CleaveSpacing.smPlus),
                    verticalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
                ) {
                    // 1. Instance Name & Icon
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.cleave_create_instance_info).uppercase(),
                            color = accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                        ) {
                            CleaveImageWell(
                                image = icon?.asImageBitmap(),
                                fallback = R.drawable.cleave_loader_vanilla,
                                size = 42.dp,
                                modifier = Modifier.clickable(role = Role.Button) {
                                    CleaveIconCropper.start(activity) { icon = it }
                                }
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    nameEdited = it.isNotEmpty()
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.cleave_create_name_hint),
                                        color = CleaveSurface.textMuted,
                                        fontSize = 13.sp
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = CleaveSurface.textPrimary,
                                    unfocusedTextColor = CleaveSurface.textPrimary,
                                    focusedBorderColor = accent,
                                    unfocusedBorderColor = CleaveSurface.borderSoft,
                                    cursorColor = accent
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = CleaveSurface.borderSoft)

                    // 2. Mod Loader Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.cleave_create_loader_heading).uppercase(),
                            color = accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        val tiles = CleaveLoader.entries.toList()
                        tiles.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { entry ->
                                    LoaderTile(
                                        loader = entry,
                                        selected = entry == loader,
                                        accent = accent,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            loader = entry
                                            onlyStable = true
                                        }
                                    )
                                }
                                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }

                        // Vanilla has nothing to choose, so the pane says so instead of leaving
                        // the empty space where every other loader lists its builds.
                        if (loader.usesGameVersion && !loader.hasBuilds) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.cleave_create_vanilla_hint),
                                color = CleaveSurface.textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // 3. Inline Loader Version List when a mod loader is selected. Vanilla has
                        // nothing to choose - there is one Minecraft version and nothing on top of
                        // it - so the list, its switch and its loading line are left out.
                        if (loader.usesGameVersion && loader.hasBuilds) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                            ) {
                                Text(
                                    text = if (loaderBuild != null) {
                                        stringResource(
                                            R.string.cleave_create_loader_version,
                                            loader.displayName,
                                            loaderBuild?.label ?: ""
                                        )
                                    } else {
                                        stringResource(R.string.cleave_create_loader_loading)
                                    },
                                    color = if (loaderBuild != null) CleaveSurface.textPrimary else CleaveSurface.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.cleave_create_only_stable),
                                    color = CleaveSurface.textSecondary,
                                    fontSize = 11.sp
                                )
                                Switch(
                                    checked = onlyStable,
                                    onCheckedChange = { onlyStable = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CleaveSurface.onAccent,
                                        checkedTrackColor = accent,
                                        uncheckedThumbColor = CleaveSurface.textSecondary,
                                        uncheckedTrackColor = CleaveSurface.panelTrack
                                    )
                                )
                            }

                            when {
                                loadingBuilds -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = accent,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.cleave_create_loader_loading),
                                            color = CleaveSurface.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                visibleBuilds.isEmpty() -> {
                                    Text(
                                        text = if (buildsFailed) stringResource(R.string.cleave_create_failed)
                                        else stringResource(
                                            R.string.cleave_create_loader_empty,
                                            loader.displayName,
                                            selectedVersion ?: stringResource(R.string.cleave_create_versions_empty)
                                        ),
                                        color = CleaveSurface.warning,
                                        fontSize = 11.sp
                                    )
                                }
                                else -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 160.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CleaveSurface.panelRaised.copy(alpha = 0.3f))
                                            .border(
                                                CleaveSpacing.hairline,
                                                CleaveSurface.borderSoft,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .verticalScroll(rememberScrollState())
                                            .padding(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        visibleBuilds.forEach { build ->
                                            val isSelected = build.id == loaderBuild?.id
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (isSelected) accent.copy(alpha = 0.16f)
                                                        else Color.Transparent
                                                    )
                                                    .clickable { loaderBuild = build }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { loaderBuild = build },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = accent,
                                                        unselectedColor = CleaveSurface.textSecondary
                                                    ),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = build.label,
                                                    color = if (isSelected) Color.White else CleaveSurface.textPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (build.stable) {
                                                    ModrinthMetaPill(text = "Stable", highlighted = isSelected)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionRow(
    versionId: String,
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = if (selected) accent.copy(alpha = 0.16f) else CleaveSurface.panelRaised.copy(alpha = 0.36f),
        border = BorderStroke(
            width = if (selected) 1.5.dp else CleaveSpacing.hairline,
            color = if (selected) accent else CleaveSurface.borderSoft
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accent,
                    unselectedColor = CleaveSurface.textSecondary
                ),
                modifier = Modifier.size(20.dp)
            )
            Image(
                painter = painterResource(R.drawable.cleave_loader_vanilla),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            ModrinthMetaPill(
                text = versionId,
                highlighted = selected
            )
        }
    }
}

@Composable
private fun LoaderTile(
    loader: CleaveLoader,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (selected) accent.copy(alpha = 0.18f) else CleaveSurface.panelRaised.copy(alpha = 0.38f),
        border = BorderStroke(
            width = if (selected) 1.5.dp else CleaveSpacing.hairline,
            color = if (selected) accent else CleaveSurface.borderSoft
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(loader.iconRes),
                contentDescription = loader.displayName,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = loader.displayName,
                    color = if (selected) Color.White else CleaveSurface.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(loader.taglineRes),
                    color = CleaveSurface.textSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
