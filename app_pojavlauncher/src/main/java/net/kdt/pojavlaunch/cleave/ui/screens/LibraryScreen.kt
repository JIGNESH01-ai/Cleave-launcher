/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Ported from mirai-launcher- (Zalith Launcher 2 fork):
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/VersionsManageScreen.kt
 *     (VersionsManageScreen, LandscapeInstanceListItem, VersionManagementDetailPane,
 *      VersionManagementQuickAction)
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/VersionExportScreen.kt
 * Header ("Instance Management" / "Select an instance to manage", count circle pill, folder button,
 * "+ Install" button), search bar + sort button, "YOUR INSTANCES" horizontal LazyRow of LandscapeInstanceListItem cards, and the
 * VersionManagementDetailPane below (with Overview, Mods, Resource Packs, Worlds, Settings tabs,
 * favorite heart, overflow menu with Pin / Rename / Copy / Export Modpack / Open folder / Delete,
 * meta pills, quick actions and a full-width PLAY button that stays put).
 *
 * On a tall display that is the page. A display shorter than 500dp - a phone held sideways - cannot
 * stack chrome, a card strip and a detail card without leaving the detail card a couple of
 * centimetres, so there the instances become a vertical list beside the detail and both keep the
 * full height (InstanceListPane + InstanceListRow). The wide tiles of the old detail card are now
 * pills that wrap, and PLAY sits outside the scrolling part so it is never scrolled away.
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

package net.kdt.pojavlaunch.cleave.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.CleaveInstance
import net.kdt.pojavlaunch.cleave.bridge.instanceRenderKey
import net.kdt.pojavlaunch.cleave.bridge.CleavePreferences
import net.kdt.pojavlaunch.cleave.bridge.GameDownloadTarget
import net.kdt.pojavlaunch.cleave.bridge.ModpackExportFormat
import net.kdt.pojavlaunch.cleave.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavLaunchBridge
import net.kdt.pojavlaunch.cleave.components.CleaveImageWell
import net.kdt.pojavlaunch.cleave.components.ModrinthCompactSearchField
import net.kdt.pojavlaunch.cleave.components.ModrinthMetaPill
import net.kdt.pojavlaunch.cleave.components.liquidGlass
import net.kdt.pojavlaunch.cleave.theme.CleaveMetrics
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.cleave.ui.CleaveNav
import net.kdt.pojavlaunch.cleave.ui.LauncherSection

private enum class LibrarySortMode(val label: String) {
    PINNED_AND_ACTIVE("Sort: Pinned & Active"),
    NAME_ASC("Sort: Name (A\u2013Z)"),
    VERSION("Sort: Game Version")
}

/**
 * The version of the game an instance runs. In "fabric-loader-0.16.10-1.16.5" the first number is
 * the loader's version and only the second is Minecraft's, so it is picked by shape rather than by
 * being first: [GameDownloadTarget.minecraftVersionIn].
 */
private fun extractGameVersion(versionId: String): String =
    GameDownloadTarget.minecraftVersionIn(versionId) ?: versionId.ifBlank { "Latest" }

/** The loader alone ("Fabric"); the game's version is always shown beside it. */
private fun extractLoaderSummary(versionId: String, vanilla: String): String =
    loaderLabel(versionId, vanilla)

@Composable
fun LibraryScreen(activity: FragmentActivity) {
    val instances = PojavInstanceBridge.instances
    val activeInstance = PojavInstanceBridge.selected
    val pinnedNames = remember(CleavePreferences.pinnedJson) { PojavInstanceBridge.pinnedSet() }
    val vanilla = stringResource(R.string.cleave_home_vanilla)
    val accent = CleaveThemeManager.currentAccent()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortMode by rememberSaveable { mutableStateOf(LibrarySortMode.PINNED_AND_ACTIVE) }

    var renameTarget by remember { mutableStateOf<CleaveInstance?>(null) }
    var copyTarget by remember { mutableStateOf<CleaveInstance?>(null) }
    var deleteTarget by remember { mutableStateOf<CleaveInstance?>(null) }
    var exportTarget by remember { mutableStateOf<CleaveInstance?>(null) }

    val displayedInstances = remember(instances, activeInstance, pinnedNames, searchQuery, sortMode) {
        val filtered = instances.filter { inst ->
            val matchesQuery = searchQuery.isBlank() ||
                inst.name.contains(searchQuery, ignoreCase = true) ||
                inst.versionId.contains(searchQuery, ignoreCase = true)
            matchesQuery
        }
        when (sortMode) {
            LibrarySortMode.PINNED_AND_ACTIVE -> filtered.sortedWith(
                compareByDescending<CleaveInstance> { it == activeInstance }
                    .thenByDescending { it.name in pinnedNames }
                    .thenBy { it.name.lowercase() }
            )
            LibrarySortMode.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            LibrarySortMode.VERSION -> filtered.sortedByDescending { extractGameVersion(it.versionId) }
        }
    }

    val selectedInstance = activeInstance ?: displayedInstances.firstOrNull()

    // Dialogs: Rename / Copy / Delete / Export Modpack
    renameTarget?.let { target ->
        SingleLineInputDialog(
            title = "Rename Instance",
            initial = target.name,
            confirmLabel = "Rename",
            onDismiss = { renameTarget = null },
            onConfirm = { newName ->
                renameTarget = null
                PojavInstanceBridge.rename(target, newName)
            }
        )
    }

    copyTarget?.let { target ->
        SingleLineInputDialog(
            title = "Copy Instance",
            initial = "${target.name} Copy",
            confirmLabel = "Duplicate",
            onDismiss = { copyTarget = null },
            onConfirm = { copyName ->
                copyTarget = null
                PojavInstanceBridge.copy(activity, target, copyName)
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = CleaveSurface.panelRaised,
            title = { Text(stringResource(R.string.cleave_editor_delete_title), color = CleaveSurface.textPrimary) },
            text = {
                Text(
                    stringResource(R.string.cleave_editor_delete_message, target.name),
                    color = CleaveSurface.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    deleteTarget = null
                    PojavInstanceBridge.deleteInstance(target)
                }) {
                    Text(stringResource(R.string.cleave_editor_delete_confirm), color = CleaveSurface.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.cleave_editor_delete_cancel), color = CleaveSurface.textSecondary)
                }
            }
        )
    }

    exportTarget?.let { target ->
        ModpackExportDialog(
            activity = activity,
            instance = target,
            onDismiss = { exportTarget = null }
        )
    }

    // The page reads differently on a short display (a phone held sideways) than on a tall one.
    // Stacked, five rows of chrome and a strip of cards left the detail card a couple of
    // centimetres of height, so everything inside it was cut off at the bottom edge. Side by side
    // on a short display, the list and the detail each get the whole height instead.
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val sideBySide = maxHeight < 500.dp
        // Read here, in the scope that owns it: inside the Row below, the receiver is the row and
        // maxWidth would not resolve.
        val listPaneWidth = (maxWidth * 0.34f).coerceIn(196.dp, 288.dp)

        // Everything the detail card can ask for, gathered once: both arrangements hand it over the
        // same way, so neither can drift from the other.
        val actions = LibraryActions(
            onInstall = { CleaveNav.navigate(LauncherSection.CREATE_INSTANCE) },
            onPlay = { inst ->
                PojavInstanceBridge.select(inst)
                PojavLaunchBridge.play(activity)
            },
            onOpenSubTab = { inst, tab ->
                PojavInstanceBridge.edit(activity, inst, tab)
            },
            onTogglePin = { PojavInstanceBridge.togglePinned(it) },
            onRename = { renameTarget = it },
            onCopy = { copyTarget = it },
            onExport = { exportTarget = it },
            onOpenFolder = {
                PojavInstanceBridge.select(it)
                PojavInstanceBridge.openGameDirectory(activity)
            },
            onDelete = { deleteTarget = it }
        )
        val selectedIsActive = selectedInstance != null && selectedInstance == activeInstance
        val selectedIsPinned = selectedInstance != null && selectedInstance.name in pinnedNames

        if (sideBySide) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CleaveSpacing.mdPlus, vertical = CleaveSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
            ) {
                LibraryHeaderRow(
                    shownCount = displayedInstances.size,
                    onOpenFolder = { PojavInstanceBridge.openGameDirectory(activity) },
                    onInstall = { CleaveNav.navigate(LauncherSection.CREATE_INSTANCE) }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
                ) {
                    InstanceListPane(
                        modifier = Modifier
                            .width(listPaneWidth)
                            .fillMaxHeight(),
                        instances = displayedInstances,
                        installedCount = instances.size,
                        selected = selectedInstance,
                        active = activeInstance,
                        pinned = pinnedNames,
                        vanilla = vanilla,
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        sortMode = sortMode,
                        onSortChange = { sortMode = it },
                        onSelect = { PojavInstanceBridge.select(it) }
                    )
                    InstanceDetailCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        instance = selectedInstance,
                        selectedIsActive = selectedIsActive,
                        selectedIsPinned = selectedIsPinned,
                        vanilla = vanilla,
                        actions = actions
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CleaveSpacing.mdPlus, vertical = CleaveSpacing.smPlus),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
            ) {
                LibraryHeaderRow(
                    shownCount = displayedInstances.size,
                    onOpenFolder = { PojavInstanceBridge.openGameDirectory(activity) },
                    onInstall = { CleaveNav.navigate(LauncherSection.CREATE_INSTANCE) }
                )

                LibrarySearchRow(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    sortMode = sortMode,
                    onSortChange = { sortMode = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "YOUR INSTANCES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleaveSurface.textSecondary
                    )
                    Text(
                        text = "${instances.size} installed",
                        style = MaterialTheme.typography.labelSmall,
                        color = CleaveSurface.textMuted
                    )
                }

                InstanceStrip(
                    instances = displayedInstances,
                    totalInstalled = instances.size,
                    selected = selectedInstance,
                    active = activeInstance,
                    pinned = pinnedNames,
                    vanilla = vanilla,
                    onSelect = { PojavInstanceBridge.select(it) }
                )

                InstanceDetailCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    instance = selectedInstance,
                    selectedIsActive = selectedIsActive,
                    selectedIsPinned = selectedIsPinned,
                    vanilla = vanilla,
                    actions = actions
                )
            }
        }
    }
}

/**
 * Everything the detail card can ask for, in one object, so the two arrangements of the page pass
 * it along the same way and neither can drift from the other.
 */
private class LibraryActions(
    val onInstall: () -> Unit,
    val onPlay: (CleaveInstance) -> Unit,
    val onOpenSubTab: (CleaveInstance, String) -> Unit,
    val onTogglePin: (CleaveInstance) -> Unit,
    val onRename: (CleaveInstance) -> Unit,
    val onCopy: (CleaveInstance) -> Unit,
    val onExport: (CleaveInstance) -> Unit,
    val onOpenFolder: (CleaveInstance) -> Unit,
    val onDelete: (CleaveInstance) -> Unit
)

/** The detail card for the selected instance, wired to the page's actions. */
@Composable
private fun InstanceDetailCard(
    modifier: Modifier = Modifier,
    instance: CleaveInstance?,
    selectedIsActive: Boolean,
    selectedIsPinned: Boolean,
    vanilla: String,
    actions: LibraryActions
) {
    VersionManagementDetailPane(
        modifier = modifier,
        instance = instance,
        isCurrent = selectedIsActive,
        isPinned = selectedIsPinned,
        vanilla = vanilla,
        onInstall = actions.onInstall,
        onPlay = actions.onPlay,
        onOpenSubTab = actions.onOpenSubTab,
        onTogglePin = actions.onTogglePin,
        onRename = actions.onRename,
        onCopy = actions.onCopy,
        onExport = actions.onExport,
        onOpenFolder = actions.onOpenFolder,
        onDelete = actions.onDelete
    )
}

/**
 * The title, how many instances are showing, the folder button and Install. One row on every
 * display, so the height it costs is the same everywhere.
 */
@Composable
private fun LibraryHeaderRow(
    shownCount: Int,
    onOpenFolder: () -> Unit,
    onInstall: () -> Unit
) {
    val accent = CleaveThemeManager.currentAccent()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Instance Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Select an instance to manage",
                style = MaterialTheme.typography.labelSmall,
                color = CleaveSurface.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            modifier = Modifier.size(30.dp),
            shape = RoundedCornerShape(CleaveRadii.pill),
            color = CleaveSurface.panelRaised,
            border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$shownCount",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CleaveSurface.textPrimary
                )
            }
        }
        Surface(
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(CleaveRadii.controlSmall),
            color = CleaveSurface.panel,
            border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft),
            onClick = onOpenFolder
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.cleave_ic_folder_outlined),
                    contentDescription = "Directories",
                    tint = CleaveSurface.textSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Surface(
            modifier = Modifier
                .height(34.dp)
                .widthIn(min = 34.dp),
            shape = RoundedCornerShape(CleaveRadii.controlSmall),
            color = accent,
            contentColor = CleaveSurface.onAccent,
            onClick = onInstall
        ) {
            Row(
                modifier = Modifier.padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
            ) {
                Icon(
                    painter = painterResource(R.drawable.cleave_ic_add),
                    contentDescription = "Install instance",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Install",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Search, and the sort menu behind one button. */
@Composable
private fun LibrarySearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    sortMode: LibrarySortMode,
    onSortChange: (LibrarySortMode) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
    ) {
        ModrinthCompactSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search instances...",
            modifier = Modifier.weight(1f)
        )
        Box {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(CleaveRadii.controlSmall),
                color = CleaveSurface.panel,
                border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft),
                onClick = { showSortMenu = true }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_sort),
                        contentDescription = "Sort instances",
                        tint = CleaveSurface.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                LibrarySortMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(if (sortMode == mode) "${mode.label}\u2003\u2713" else mode.label) },
                        onClick = {
                            onSortChange(mode)
                            showSortMenu = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * The instances as a list, for the side-by-side layout: search, sort, and a scrolling
 * column of rows that never has to be squeezed the way a card strip is.
 */
@Composable
private fun InstanceListPane(
    modifier: Modifier = Modifier,
    instances: List<CleaveInstance>,
    installedCount: Int,
    selected: CleaveInstance?,
    active: CleaveInstance?,
    pinned: Set<String>,
    vanilla: String,
    query: String,
    onQueryChange: (String) -> Unit,
    sortMode: LibrarySortMode,
    onSortChange: (LibrarySortMode) -> Unit,
    onSelect: (CleaveInstance) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(CleaveRadii.card)

    Surface(
        modifier = modifier.liquidGlass(shape = shape, strength = 0.7f),
        shape = shape,
        color = CleaveSurface.panel,
        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CleaveSpacing.smPlus),
            verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
        ) {
            ModrinthCompactSearchField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Search instances...",
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$installedCount installed",
                    style = MaterialTheme.typography.labelSmall,
                    color = CleaveSurface.textMuted
                )
                Box {
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = RoundedCornerShape(CleaveRadii.controlSmall),
                        color = CleaveSurface.panelRaised,
                        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft),
                        onClick = { showSortMenu = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.cleave_ic_sort),
                                contentDescription = "Sort instances",
                                tint = CleaveSurface.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        LibrarySortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(if (sortMode == mode) "${mode.label}\u2003\u2713" else mode.label) },
                                onClick = {
                                    onSortChange(mode)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs),
                contentPadding = PaddingValues(bottom = CleaveSpacing.xs)
            ) {
                if (instances.isEmpty()) {
                    item {
                        Text(
                            text = if (installedCount == 0) {
                                "No instances yet \u2014 install one to get started."
                            } else {
                                "No instances match this search."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = CleaveSurface.textSecondary,
                            modifier = Modifier.padding(CleaveSpacing.xs)
                        )
                    }
                }
                itemsIndexed(
                    items = instances,
                    key = { index, instance -> instanceRenderKey(index, instance.id) }
                ) { _, instance ->
                    InstanceListRow(
                        instance = instance,
                        selected = instance == selected,
                        active = instance == active,
                        pinned = instance.name in pinned,
                        vanilla = vanilla,
                        onClick = { onSelect(instance) }
                    )
                }
            }
        }
    }
}

/** One instance in the list pane: icon, name, loader and version, and its active or pinned mark. */
@Composable
private fun InstanceListRow(
    instance: CleaveInstance,
    selected: Boolean,
    active: Boolean,
    pinned: Boolean,
    vanilla: String,
    onClick: () -> Unit
) {
    val accent = CleaveThemeManager.currentAccent()
    val loaderSummary = remember(instance.versionId, vanilla) {
        extractLoaderSummary(instance.versionId, vanilla)
    }
    val gameVersion = remember(instance.versionId) { extractGameVersion(instance.versionId) }
    val shape = RoundedCornerShape(CleaveRadii.controlSmall)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = if (selected) CleaveSurface.panelRaised else CleaveSurface.panel.copy(alpha = 0.31f),
        border = BorderStroke(
            if (selected) 1.5.dp else CleaveSpacing.hairline,
            if (selected) accent.copy(alpha = 0.86f) else CleaveSurface.borderSoft
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CleaveSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
        ) {
            CleaveImageWell(
                image = instance.icon,
                fallback = R.drawable.cleave_loader_vanilla,
                size = 34.dp,
                radius = CleaveRadii.controlSmall
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xxs)
            ) {
                Text(
                    text = instance.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$loaderSummary \u2022 $gameVersion",
                    style = MaterialTheme.typography.labelSmall,
                    color = CleaveSurface.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (active) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = RoundedCornerShape(CleaveRadii.pill),
                    color = CleaveSurface.success
                ) {}
            } else if (pinned) {
                Icon(
                    painter = painterResource(R.drawable.cleave_ic_favorite_filled),
                    contentDescription = "Pinned",
                    tint = accent,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/** The strip of instance cards, for the tall layout. */
@Composable
private fun InstanceStrip(
    instances: List<CleaveInstance>,
    totalInstalled: Int,
    selected: CleaveInstance?,
    active: CleaveInstance?,
    pinned: Set<String>,
    vanilla: String,
    onSelect: (CleaveInstance) -> Unit
) {
    if (instances.isEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(CleaveRadii.cardSmall),
            color = CleaveSurface.panel,
            border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = CleaveSpacing.md),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (totalInstalled == 0) {
                        "No instances yet \u2014 install one to get started."
                    } else {
                        "No instances match this search."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = CleaveSurface.textSecondary
                )
            }
        }
    } else {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = CleaveSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
        ) {
            itemsIndexed(
                items = instances,
                key = { index, instance -> instanceRenderKey(index, instance.id) }
            ) { _, instance ->
                LandscapeInstanceListItem(
                    instance = instance,
                    selected = instance == selected,
                    active = instance == active,
                    pinned = instance.name in pinned,
                    vanilla = vanilla,
                    onClick = { onSelect(instance) },
                    modifier = Modifier.width(220.dp)
                )
            }
        }
    }
}

@Composable
private fun LandscapeInstanceListItem(
    instance: CleaveInstance,
    selected: Boolean,
    active: Boolean,
    pinned: Boolean,
    vanilla: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loaderSummary = remember(instance.versionId, vanilla) {
        extractLoaderSummary(instance.versionId, vanilla)
    }
    val gameVersion = remember(instance.versionId) { extractGameVersion(instance.versionId) }
    val shape = RoundedCornerShape(CleaveRadii.cardSmall)
    val accent = CleaveThemeManager.currentAccent()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = shape,
                tint = if (selected) accent else CleaveSurface.glassBlue,
                strength = if (selected) 0.88f else 0.58f,
                elevation = CleaveMetrics.glassSubtleElevation
            ),
        shape = shape,
        color = if (selected) CleaveSurface.panelRaised else CleaveSurface.panel.copy(alpha = 0.41f),
        border = BorderStroke(
            if (selected) 1.5.dp else CleaveSpacing.hairline,
            if (selected) accent.copy(alpha = 0.86f) else CleaveSurface.borderSoft
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CleaveSpacing.smPlus),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
        ) {
            CleaveImageWell(
                image = instance.icon,
                fallback = R.drawable.cleave_loader_vanilla,
                size = 46.dp,
                radius = CleaveRadii.controlSmall
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xxs)
            ) {
                Text(
                    text = instance.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$loaderSummary \u2022 $gameVersion",
                    style = MaterialTheme.typography.labelSmall,
                    color = CleaveSurface.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (active || pinned) {
                    Text(
                        text = if (active) "Active" else "Pinned",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (active) CleaveSurface.success else accent
                    )
                }
            }
            Surface(
                modifier = Modifier.size(8.dp),
                shape = RoundedCornerShape(CleaveRadii.pill),
                color = CleaveSurface.success
            ) {}
        }
    }
}

private data class DetailQuickTab(val label: String, val iconRes: Int, val subTab: String?)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VersionManagementDetailPane(
    modifier: Modifier = Modifier,
    instance: CleaveInstance?,
    isCurrent: Boolean,
    isPinned: Boolean,
    vanilla: String,
    onInstall: () -> Unit,
    onPlay: (CleaveInstance) -> Unit,
    onOpenSubTab: (CleaveInstance, String) -> Unit,
    onTogglePin: (CleaveInstance) -> Unit,
    onRename: (CleaveInstance) -> Unit,
    onCopy: (CleaveInstance) -> Unit,
    onExport: (CleaveInstance) -> Unit,
    onOpenFolder: (CleaveInstance) -> Unit,
    onDelete: (CleaveInstance) -> Unit
) {
    val accent = CleaveThemeManager.currentAccent()
    val shape = RoundedCornerShape(CleaveRadii.card)

    Surface(
        modifier = modifier.liquidGlass(shape = shape, strength = 0.7f),
        shape = shape,
        color = CleaveSurface.panel,
        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
    ) {
        if (instance == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CleaveSpacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select an instance to manage it",
                    style = MaterialTheme.typography.titleMedium,
                    color = CleaveSurface.textSecondary
                )
            }
        } else {
            val loaderSummary = remember(instance.versionId, vanilla) {
                extractLoaderSummary(instance.versionId, vanilla)
            }
            val gameVersion = remember(instance.versionId) { extractGameVersion(instance.versionId) }
            val tabs = listOf(
                DetailQuickTab("Overview", R.drawable.cleave_ic_dashboard_outlined, null),
                DetailQuickTab("Mods", R.drawable.cleave_ic_extension_outlined, "mods"),
                DetailQuickTab("Resource Packs", R.drawable.cleave_ic_format_paint_outlined, "packs"),
                DetailQuickTab("Worlds", R.drawable.cleave_ic_public, "worlds"),
                DetailQuickTab("Settings", R.drawable.cleave_ic_build_outlined, "settings")
            )
            var showActions by remember(instance.name) { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CleaveSpacing.mdPlus),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
            ) {
                // Header row inside detail card (Screenshot 7 bottom card)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
                ) {
                    CleaveImageWell(
                        image = instance.icon,
                        fallback = R.drawable.cleave_loader_vanilla,
                        size = 42.dp,
                        radius = CleaveRadii.controlSmall,
                        modifier = Modifier.clickable { onOpenSubTab(instance, "mods") }
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenSubTab(instance, "mods") },
                        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xxs)
                    ) {
                        Text(
                            text = instance.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
                        ) {
                            ModrinthMetaPill(
                                text = loaderSummary,
                                backgroundColor = CleaveSurface.panelRaised,
                                textColor = CleaveSurface.textPrimary
                            )
                            Text(
                                text = "Minecraft $gameVersion",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleaveSurface.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = { onTogglePin(instance) }) {
                        Icon(
                            painter = painterResource(
                                if (isPinned) R.drawable.cleave_ic_favorite_filled
                                else R.drawable.cleave_ic_favorite_outlined
                            ),
                            contentDescription = "Favorite",
                            tint = if (isPinned) accent else CleaveSurface.textSecondary
                        )
                    }
                    Box {
                        IconButton(onClick = { showActions = true }) {
                            Icon(
                                painter = painterResource(R.drawable.cleave_ic_more_horiz),
                                contentDescription = "More actions",
                                tint = CleaveSurface.textPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showActions,
                            onDismissRequest = { showActions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Manage Mods & Content") },
                                onClick = {
                                    showActions = false
                                    onOpenSubTab(instance, "mods")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isPinned) "Unpin" else "Pin") },
                                onClick = {
                                    showActions = false
                                    onTogglePin(instance)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Rename") },
                                onClick = {
                                    showActions = false
                                    onRename(instance)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = {
                                    showActions = false
                                    onCopy(instance)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Modpack") },
                                onClick = {
                                    showActions = false
                                    onExport(instance)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Open instance folder") },
                                onClick = {
                                    showActions = false
                                    onOpenFolder(instance)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = CleaveSurface.danger) },
                                onClick = {
                                    showActions = false
                                    onDelete(instance)
                                }
                            )
                        }
                    }
                }

                // Scrollable body: subscreen tab pills + meta pills + quick actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
                    ) {
                        tabs.forEach { tab ->
                            val selected = tab.subTab == null
                            Surface(
                                shape = RoundedCornerShape(CleaveRadii.pill),
                                color = if (selected) accent.copy(alpha = 0.22f) else CleaveSurface.panelRaised,
                                border = BorderStroke(
                                    CleaveSpacing.hairline,
                                    if (selected) accent else CleaveSurface.borderSoft
                                ),
                                onClick = {
                                    onOpenSubTab(instance, tab.subTab ?: "overview")
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.xs),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(tab.iconRes),
                                        contentDescription = null,
                                        tint = if (selected) accent else CleaveSurface.textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(CleaveSpacing.xs))
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) accent else CleaveSurface.textPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // What the four wide tiles used to say, as pills: they wrap onto a second line
                    // when the pane is narrow instead of being crushed to a sliver each, and they
                    // cost a third of the height.
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs),
                        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
                    ) {
                        ModrinthMetaPill(
                            text = "Healthy",
                            backgroundColor = CleaveSurface.success.copy(alpha = 0.16f),
                            textColor = CleaveSurface.success,
                            borderColor = CleaveSurface.success.copy(alpha = 0.42f)
                        )
                        ModrinthMetaPill(text = loaderSummary)
                        ModrinthMetaPill(text = "Minecraft $gameVersion")
                        ModrinthMetaPill(text = instance.name)
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                    ) {
                        VersionManagementQuickAction("Mods", R.drawable.cleave_ic_extension_outlined) {
                            onOpenSubTab(instance, "mods")
                        }
                        VersionManagementQuickAction("Resource Packs", R.drawable.cleave_ic_format_paint_outlined) {
                            onOpenSubTab(instance, "packs")
                        }
                        VersionManagementQuickAction("Shaders", R.drawable.cleave_ic_lightbulb) {
                            onOpenSubTab(instance, "shaders")
                        }
                        VersionManagementQuickAction("Worlds", R.drawable.cleave_ic_public) {
                            onOpenSubTab(instance, "worlds")
                        }
                        VersionManagementQuickAction("Settings", R.drawable.cleave_ic_build_outlined) {
                            onOpenSubTab(instance, "settings")
                        }
                        VersionManagementQuickAction("Export Modpack", R.drawable.cleave_ic_upload) {
                            onExport(instance)
                        }
                    }
                }

                // PLAY sits under the scrolling part, not inside it, so it is on screen whatever
                // height the card was given.
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(CleaveRadii.cardSmall),
                    color = accent,
                    contentColor = CleaveSurface.onAccent,
                    onClick = { onPlay(instance) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_play_arrow_filled),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(CleaveSpacing.sm))
                        Text(
                            text = "PLAY",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                if (isCurrent) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = "Selected as the current launch instance",
                        style = MaterialTheme.typography.labelSmall,
                        color = CleaveSurface.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionManagementQuickAction(
    label: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.height(36.dp),
        shape = RoundedCornerShape(CleaveRadii.control),
        color = CleaveSurface.panelRaised,
        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft),
        contentColor = CleaveSurface.textPrimary,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CleaveSpacing.smPlus),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(15.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun SingleLineInputDialog(
    title: String,
    initial: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember(initial) { mutableStateOf(initial) }
    val accent = CleaveThemeManager.currentAccent()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CleaveSurface.panelRaised,
        title = { Text(title, color = CleaveSurface.textPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    unfocusedBorderColor = CleaveSurface.borderSoft,
                    focusedTextColor = CleaveSurface.textPrimary,
                    unfocusedTextColor = CleaveSurface.textPrimary,
                    cursorColor = accent
                )
            )
        },
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank(),
                onClick = { onConfirm(text.trim()) }
            ) {
                Text(confirmLabel, color = accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cleave_settings_cancel), color = CleaveSurface.textSecondary)
            }
        }
    )
}

@Composable
internal fun ModpackExportDialog(
    activity: FragmentActivity,
    instance: CleaveInstance,
    onDismiss: () -> Unit
) {
    val accent = CleaveThemeManager.currentAccent()
    var format by remember { mutableStateOf(ModpackExportFormat.MODRINTH) }
    var packName by remember(instance.name) { mutableStateOf(instance.name) }
    var packVersion by remember { mutableStateOf("1.0.0") }
    var packAuthor by remember { mutableStateOf("CLEAVE") }
    var exporting by remember { mutableStateOf(false) }

    val availableFolders = listOf(
        "mods" to "Mods (mods/)",
        "config" to "Configs (config/)",
        "resourcepacks" to "Resource Packs (resourcepacks/)",
        "shaderpacks" to "Shaders (shaderpacks/)",
        "saves" to "Worlds (saves/)",
        "options.txt" to "Game Options (options.txt)"
    )
    var selectedFolders by remember {
        mutableStateOf(setOf("mods", "config", "resourcepacks", "shaderpacks", "options.txt"))
    }

    AlertDialog(
        onDismissRequest = { if (!exporting) onDismiss() },
        containerColor = CleaveSurface.panelRaised,
        title = { Text("Export Modpack", color = CleaveSurface.textPrimary, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
            ) {
                Text("Export Format", color = CleaveSurface.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                ModpackExportFormat.entries.forEach { f ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { format = f }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = format == f,
                            onClick = { format = f },
                            colors = RadioButtonDefaults.colors(selectedColor = accent)
                        )
                        Column {
                            Text(f.label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(f.description, color = CleaveSurface.textSecondary, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = packName,
                    onValueChange = { packName = it },
                    singleLine = true,
                    label = { Text("Modpack Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)) {
                    OutlinedTextField(
                        value = packVersion,
                        onValueChange = { packVersion = it },
                        singleLine = true,
                        label = { Text("Version") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = packAuthor,
                        onValueChange = { packAuthor = it },
                        singleLine = true,
                        label = { Text("Author") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Include Content", color = CleaveSurface.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                availableFolders.forEach { (key, label) ->
                    val checked = key in selectedFolders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFolders = if (checked) selectedFolders - key else selectedFolders + key
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                selectedFolders = if (it) selectedFolders + key else selectedFolders - key
                            },
                            colors = CheckboxDefaults.colors(checkedColor = accent)
                        )
                        Text(label, color = CleaveSurface.textPrimary, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !exporting && packName.isNotBlank() && selectedFolders.isNotEmpty(),
                onClick = {
                    exporting = true
                    PojavInstanceBridge.exportModpack(
                        context = activity,
                        instance = instance,
                        format = format,
                        packName = packName,
                        packVersion = packVersion,
                        packAuthor = packAuthor,
                        includeFolders = selectedFolders
                    ) {
                        exporting = false
                        onDismiss()
                    }
                }
            ) {
                Text(if (exporting) "Exporting..." else "Export", color = accent, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(enabled = !exporting, onClick = onDismiss) {
                Text(stringResource(R.string.cleave_settings_cancel), color = CleaveSurface.textSecondary)
            }
        }
    )
}
