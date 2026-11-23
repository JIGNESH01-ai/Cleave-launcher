/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Adapter: the download dialog of mirai-launcher- (Zalith Launcher 2 fork): what a file needs on the
 * left, which instances to install it into on the right, Cancel and Install underneath; plus the
 * progress panel that the same install collapses into when it is minimized.
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/download/assets/download/DependenciesDialog.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/download/assets/download/DownloadAssetsScreen.kt
 * Zalith Launcher 2 Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package net.kdt.pojavlaunch.cleave.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.CleaveInstallQueue
import net.kdt.pojavlaunch.cleave.bridge.CleaveInstallStatus
import net.kdt.pojavlaunch.cleave.bridge.CleaveInstallTask
import net.kdt.pojavlaunch.cleave.bridge.AssetFile
import net.kdt.pojavlaunch.cleave.bridge.AssetTarget
import net.kdt.pojavlaunch.cleave.bridge.instanceRenderKey
import net.kdt.pojavlaunch.cleave.bridge.AssetType
import net.kdt.pojavlaunch.cleave.bridge.ResolvedDependency
import net.kdt.pojavlaunch.cleave.components.CleaveGhostButton
import net.kdt.pojavlaunch.cleave.components.CleaveImageWell
import net.kdt.pojavlaunch.cleave.components.CleavePrimaryButton
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager

/**
 * The dialog a download goes through, laid out as in Zalith Launcher 2: dependencies on one side,
 * the instances to install into on the other, Cancel and Install at the bottom. On a screen too
 * narrow for two panes the same content stacks, dependencies first.
 *
 * [onInstall] is handed the ticks the user left in place and nothing else, so the queue installs
 * exactly what was on screen when Install was pressed.
 */
@Composable
internal fun AssetInstallDialog(
    file: AssetFile,
    type: AssetType,
    targets: List<AssetTarget>,
    dependencies: List<ResolvedDependency>,
    loading: Boolean,
    onDismiss: () -> Unit,
    onInstall: (List<AssetTarget>, List<ResolvedDependency>) -> Unit
) {
    val accent = CleaveThemeManager.currentAccent()
    // Ticked dependencies are remembered per project id, so reopening the dialog keeps the choices.
    val dependencyTicks = remember(dependencies) {
        mutableStateMapOf<String, Boolean>().apply {
            dependencies.forEach { put(it.project.id + it.project.platform, it.required) }
        }
    }
    // The one instance this file belongs in: the one being played when the file fits it, otherwise
    // the closest match. The rest stay unticked, so a download can never land in a world that
    // cannot load it without the player saying so.
    val autoTarget = remember(targets, file, type) { defaultInstallTarget(file, type, targets) }
    val targetTicks = remember(targets, file, type) {
        mutableStateMapOf<String, Boolean>().apply {
            targets.forEach { put(it.instanceId, it.instanceId == autoTarget?.instanceId) }
        }
    }

    val isModpack = type == AssetType.MODPACK
    val pickedTargets = targets.filter { target -> targetTicks[target.instanceId] == true }
    val pickedDependencies = dependencies.filter { dependencyTicks[it.project.id + it.project.platform] == true }
    // A modpack brings its own instance. Anything else needs somewhere to go: with no instance
    // at all, or with every row unticked, there is nothing to install into.
    val canInstall = !loading && (isModpack || pickedTargets.isNotEmpty())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CleaveSurface.canvas.copy(alpha = 0.72f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .widthIn(min = 300.dp, max = 720.dp)
                    .padding(CleaveSpacing.md)
            ) {
                val wide = maxWidth >= 560.dp
                // The panel is never taller than the screen it is on. The header and the two buttons
                // are measured first and the panes take whatever is left, scrolling inside
                // themselves: that is what keeps Cancel and Install on screen when the display is
                // short, which a fixed height cannot promise.
                val panelMaxHeight = minOf(maxHeight, 640.dp)
                val tight = maxHeight < 480.dp
                val gap = if (tight) CleaveSpacing.sm else CleaveSpacing.md
                val paneMaxHeight = if (tight) 190.dp else 360.dp

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = panelMaxHeight)
                        .clip(RoundedCornerShape(CleaveRadii.dialog))
                        .background(CleaveSurface.panelRaised)
                        .border(CleaveSpacing.hairline, CleaveSurface.borderSoft, RoundedCornerShape(CleaveRadii.dialog))
                        // Swallows the taps meant for the panel instead of the scrim behind it.
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                        .padding(if (tight) CleaveSpacing.md else CleaveSpacing.lg)
                ) {
                    InstallHeader(file = file, onDismiss = onDismiss)

                    if (isModpack) {
                        Text(
                            text = stringResource(R.string.cleave_install_modpack_note),
                            color = CleaveSurface.textSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = gap)
                        )
                    } else if (wide) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                // fill = false, so the dialog stays as short as its content and the
                                // row only ever takes the room the header and the buttons left over.
                                .weight(1f, fill = false)
                                .padding(top = gap),
                            horizontalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            DependencyPane(
                                dependencies = dependencies,
                                ticks = dependencyTicks,
                                loading = loading,
                                accent = accent,
                                listMaxHeight = paneMaxHeight,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                            TargetPane(
                                loading = loading,
                                targets = targets,
                                file = file,
                                loadersMatter = loaderMatters(type),
                                ticks = targetTicks,
                                autoTarget = autoTarget,
                                ticksChanged = { key, checked -> targetTicks[key] = checked },
                                accent = accent,
                                listMaxHeight = paneMaxHeight,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                                .padding(top = gap),
                            verticalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            DependencyPane(
                                dependencies = dependencies,
                                ticks = dependencyTicks,
                                loading = loading,
                                accent = accent,
                                listMaxHeight = if (tight) 150.dp else 190.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            TargetPane(
                                loading = loading,
                                targets = targets,
                                file = file,
                                loadersMatter = loaderMatters(type),
                                ticks = targetTicks,
                                autoTarget = autoTarget,
                                ticksChanged = { key, checked -> targetTicks[key] = checked },
                                accent = accent,
                                listMaxHeight = if (tight) 150.dp else 190.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = gap),
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        CleaveGhostButton(
                            text = stringResource(R.string.cleave_install_cancel),
                            iconRes = null,
                            onClick = onDismiss
                        )
                        CleavePrimaryButton(
                            text = if (pickedTargets.size > 1 && !isModpack) {
                                stringResource(R.string.cleave_install_confirm_to, pickedTargets.size)
                            } else {
                                stringResource(R.string.cleave_install_confirm)
                            },
                            iconRes = R.drawable.cleave_ic_download,
                            enabled = canInstall,
                            onClick = { onInstall(pickedTargets, pickedDependencies) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallHeader(file: AssetFile, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.cleave_install_title, file.name),
                color = CleaveSurface.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = file.versionNumber,
                color = CleaveSurface.textMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            contentColor = CleaveSurface.textSecondary,
            onClick = onDismiss
        ) {
            Icon(
                painter = painterResource(R.drawable.cleave_ic_close),
                contentDescription = stringResource(R.string.cleave_install_cancel),
                modifier = Modifier.padding(CleaveSpacing.xs).size(18.dp)
            )
        }
    }
}

/** Left pane of the dialog: what the file needs besides itself, one tick box per project. */
@Composable
private fun DependencyPane(
    dependencies: List<ResolvedDependency>,
    ticks: MutableMap<String, Boolean>,
    loading: Boolean,
    accent: Color,
    listMaxHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        PaneTitle(text = stringResource(R.string.cleave_install_dependencies), accent = accent)
        when {
            loading && dependencies.isEmpty() -> PaneNote(
                text = stringResource(R.string.cleave_install_dependencies_loading),
                loading = true
            )
            dependencies.isEmpty() -> PaneNote(
                text = stringResource(R.string.cleave_install_dependencies_none),
                loading = false
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = listMaxHeight),
                contentPadding = PaddingValues(vertical = CleaveSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
            ) {
                items(dependencies, key = { it.project.platform.name + it.project.id }) { dependency ->
                    DependencyRow(
                        dependency = dependency,
                        checked = ticks[dependency.project.id + dependency.project.platform] == true,
                        accent = accent,
                        onCheckedChange = { checked ->
                            ticks[dependency.project.id + dependency.project.platform] = checked
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DependencyRow(
    dependency: ResolvedDependency,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    val icon = rememberRemoteBitmap(dependency.project.iconUrl, targetMinEdgePx = 48)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CleaveRadii.controlSmall))
            .background(if (checked) accent.copy(alpha = 0.10f) else Color.Transparent)
            .clickable { onCheckedChange(!checked) }
            .padding(top = CleaveSpacing.xxs, end = CleaveSpacing.sm, bottom = CleaveSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = accent,
                checkmarkColor = CleaveSurface.onAccent,
                uncheckedColor = CleaveSurface.textMuted
            )
        )
        CleaveImageWell(
            image = icon,
            fallback = R.drawable.cleave_ic_extension_outlined,
            size = 28.dp,
            radius = CleaveRadii.micro
        )
        Spacer(modifier = Modifier.width(CleaveSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dependency.project.title,
                color = CleaveSurface.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val dependencyFile = dependency.file
            Text(
                text = dependencyFile?.fileName
                    ?: stringResource(R.string.cleave_install_dependency_missing),
                color = if (dependencyFile == null) CleaveSurface.warning else CleaveSurface.textMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(CleaveSpacing.xs))
        DependencyChip(required = dependency.required, accent = accent)
    }
}

@Composable
private fun DependencyChip(required: Boolean, accent: Color) {
    val tint = if (required) accent else CleaveSurface.textMuted
    Surface(
        shape = RoundedCornerShape(CleaveRadii.pill),
        color = tint.copy(alpha = 0.14f),
        border = BorderStroke(CleaveSpacing.hairline, tint.copy(alpha = 0.5f))
    ) {
        Text(
            text = stringResource(
                if (required) R.string.cleave_install_dependency_required
                else R.string.cleave_install_dependency_optional
            ),
            color = tint,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = CleaveSpacing.smCompact, vertical = 1.dp)
        )
    }
}

/**
 * Right pane of the dialog: the instances to install into, several at once. The pane opens with the
 * one instance the file belongs in ticked, and says which one and why at the top.
 */
@Composable
private fun TargetPane(
    loading: Boolean,
    targets: List<AssetTarget>,
    /** The file being installed: what it was built for is what the rows are checked against. */
    file: AssetFile,
    loadersMatter: Boolean,
    ticks: MutableMap<String, Boolean>,
    /** The instance ticked on open, or null when the file fits none of them. */
    autoTarget: AssetTarget?,
    ticksChanged: (String, Boolean) -> Unit,
    accent: Color,
    listMaxHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        PaneTitle(text = stringResource(R.string.cleave_install_targets), accent = accent)
        val autoLabel = autoTarget?.let { target ->
            listOfNotNull(
                target.name.takeIf { it.isNotBlank() },
                target.mcVersion ?: target.versionId.takeIf { it.isNotBlank() },
                target.loader?.replaceFirstChar { it.uppercase() }
            ).joinToString(" · ")
        }
        if (!autoLabel.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.cleave_install_targets_detected, autoLabel),
                color = CleaveSurface.textMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = CleaveSpacing.xs, vertical = 1.dp)
            )
        }
        if (targets.isEmpty()) {
            PaneNote(
                text = stringResource(if (loading) R.string.cleave_install_targets_loading else R.string.cleave_install_targets_none),
                loading = loading
            )
            return@Column
        }
        val allTicked = targets.all { ticks[it.instanceId] == true }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CleaveSpacing.xs, vertical = CleaveSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(CleaveRadii.pill),
                color = accent.copy(alpha = 0.12f),
                onClick = { targets.forEach { ticksChanged(it.instanceId, !allTicked) } }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = CleaveSpacing.smPlus, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
                ) {
                    Icon(
                        painter = painterResource(
                            if (allTicked) R.drawable.cleave_ic_deselect else R.drawable.cleave_ic_select_all
                        ),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(
                            if (allTicked) R.string.cleave_install_deselect_all
                            else R.string.cleave_install_select_all
                        ),
                        color = accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = listMaxHeight),
            contentPadding = PaddingValues(vertical = CleaveSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
        ) {
            itemsIndexed(targets, key = { index, target -> instanceRenderKey(index, target.instanceId) }) { _, target ->
                TargetRow(
                    target = target,
                    check = checkTarget(file, target, loadersMatter),
                    checked = ticks[target.instanceId] == true,
                    accent = accent,
                    onCheckedChange = { checked -> ticksChanged(target.instanceId, checked) }
                )
            }
        }
        if (targets.none { ticks[it.instanceId] == true }) {
            Text(
                text = stringResource(
                    if (autoTarget == null) R.string.cleave_install_targets_no_match
                    else R.string.cleave_install_targets_none_ticked
                ),
                color = CleaveSurface.warning,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = CleaveSpacing.xs, vertical = CleaveSpacing.xxs)
            )
        }
    }
}

@Composable
private fun TargetRow(
    target: AssetTarget,
    /** What the file asks for against this instance: the badges, and whether it fits at all. */
    check: TargetCheck,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(CleaveRadii.controlSmall),
        color = if (checked) accent.copy(alpha = 0.12f) else CleaveSurface.panel,
        border = BorderStroke(
            CleaveSpacing.hairline,
            if (checked) accent.copy(alpha = 0.55f) else CleaveSurface.borderSoft
        ),
        onClick = { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = CleaveSpacing.sm, top = CleaveSpacing.xxs, bottom = CleaveSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = accent,
                    checkmarkColor = CleaveSurface.onAccent,
                    uncheckedColor = CleaveSurface.textMuted
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = target.name,
                    color = CleaveSurface.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = target.subtitle,
                    color = CleaveSurface.textSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // What is wrong with the row, named after what the file needs rather than after the
            // instance: "not for 1.12.2" read as if the file were for 1.12.2.
            check.wantedVersion?.let { wanted ->
                Spacer(modifier = Modifier.width(CleaveSpacing.xs))
                TargetBadge(
                    text = stringResource(R.string.cleave_install_target_needs_version, wanted),
                    tint = CleaveSurface.warning
                )
            }
            check.wantedLoader?.let { wanted ->
                Spacer(modifier = Modifier.width(CleaveSpacing.xs))
                TargetBadge(
                    text = stringResource(R.string.cleave_install_target_needs_loader, wanted),
                    tint = CleaveSurface.warning
                )
            }
            if (target.detected && check.fits) {
                Spacer(modifier = Modifier.width(CleaveSpacing.xs))
                TargetBadge(text = stringResource(R.string.cleave_install_target_detected), tint = accent)
            }
        }
    }
}

@Composable
private fun TargetBadge(text: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(CleaveRadii.pill),
        color = tint.copy(alpha = 0.16f)
    ) {
        Text(
            text = text,
            color = tint,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = CleaveSpacing.smCompact, vertical = 1.dp)
        )
    }
}

@Composable
private fun PaneTitle(text: String, accent: Color) {
    Text(
        text = text.uppercase(),
        color = accent,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(horizontal = CleaveSpacing.xs)
    )
}

@Composable
private fun PaneNote(text: String, loading: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CleaveSpacing.xs, vertical = CleaveSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = CleaveSurface.textMuted
            )
        }
        Text(text = text, color = CleaveSurface.textMuted, fontSize = 11.sp)
    }
}

// ============================================================================================
// The running install: the panel the dialog becomes, and the pill it collapses into
// ============================================================================================

/**
 * Progress of everything the queue is doing, with a Minimize button. This is what the plan dialog
 * turns into once Install is pressed, and it is also what the pill reopens, so the same install can
 * be watched from any section of the launcher.
 */
@Composable
internal fun CleaveInstallPanel(onMinimize: () -> Unit) {
    val accent = CleaveThemeManager.currentAccent()
    val tasks = CleaveInstallQueue.tasks
    val anyRunning = CleaveInstallQueue.running

    Dialog(
        onDismissRequest = onMinimize,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CleaveSurface.canvas.copy(alpha = 0.72f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onMinimize() },
            contentAlignment = Alignment.Center
        ) {
            // The same rule as the plan dialog: the panel is never taller than the screen, and the
            // list is what gives up room, so the button under it stays on screen.
            BoxWithConstraints(
                modifier = Modifier
                    .widthIn(min = 300.dp, max = 520.dp)
                    .padding(CleaveSpacing.md)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = minOf(maxHeight, 640.dp))
                        .clip(RoundedCornerShape(CleaveRadii.dialog))
                        .background(CleaveSurface.panelRaised)
                        .border(CleaveSpacing.hairline, CleaveSurface.borderSoft, RoundedCornerShape(CleaveRadii.dialog))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                        .padding(CleaveSpacing.lg)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                    ) {
                        Text(
                            text = stringResource(R.string.cleave_install_tasks).uppercase(),
                            color = accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (tasks.any { it.finished }) {
                            Surface(
                                shape = RoundedCornerShape(CleaveRadii.pill),
                                color = CleaveSurface.panel,
                                onClick = { CleaveInstallQueue.clearFinished() }
                            ) {
                                Text(
                                    text = stringResource(R.string.cleave_install_clear_finished),
                                    color = CleaveSurface.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = CleaveSpacing.smPlus, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(top = CleaveSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            InstallTaskCard(task = task, accent = accent)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = CleaveSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        CleaveGhostButton(
                            text = stringResource(
                                if (anyRunning) R.string.cleave_install_minimize
                                else R.string.cleave_install_dismiss
                            ),
                            iconRes = if (anyRunning) R.drawable.cleave_ic_arrow_drop_down_rounded else null,
                            // With nothing running the button also clears the finished cards, so the
                            // pill does not outlive the install it was reporting.
                            onClick = {
                                if (!anyRunning) CleaveInstallQueue.clearFinished()
                                onMinimize()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallTaskCard(task: CleaveInstallTask, accent: Color) {
    val statusColor = when (task.status) {
        CleaveInstallStatus.DONE -> CleaveSurface.success
        CleaveInstallStatus.FAILED -> CleaveSurface.danger
        CleaveInstallStatus.CANCELLED -> CleaveSurface.textMuted
        else -> accent
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CleaveRadii.cardSmall))
            .background(CleaveSurface.panel)
            .border(CleaveSpacing.hairline, CleaveSurface.borderSoft, RoundedCornerShape(CleaveRadii.cardSmall))
            .padding(CleaveSpacing.md),
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.projectTitle,
                    color = CleaveSurface.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = task.title,
                    color = CleaveSurface.textMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (task.finished) {
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    contentColor = CleaveSurface.textMuted,
                    onClick = { CleaveInstallQueue.dismiss(task) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_close),
                        contentDescription = stringResource(R.string.cleave_install_dismiss),
                        modifier = Modifier.padding(CleaveSpacing.xs).size(14.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(CleaveRadii.pill),
                    color = CleaveSurface.danger.copy(alpha = 0.14f),
                    onClick = { CleaveInstallQueue.cancel(task) }
                ) {
                    Text(
                        text = stringResource(R.string.cleave_install_cancel_task),
                        color = CleaveSurface.danger,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = CleaveSpacing.smPlus, vertical = 4.dp)
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { if (task.finished) 1f else task.progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(CleaveRadii.pill)),
            color = statusColor,
            trackColor = CleaveSurface.panelTrack
        )

        val line = when (task.status) {
            CleaveInstallStatus.QUEUED -> stringResource(R.string.cleave_install_queued)
            CleaveInstallStatus.RUNNING -> if (task.totalSteps == 1) {
                stringResource(R.string.cleave_install_started, task.title)
            } else {
                stringResource(
                    R.string.cleave_install_progress_summary,
                    task.completedSteps,
                    task.totalSteps
                ) + " · " + task.currentStep
            }
            CleaveInstallStatus.DONE -> if (task.dependencySteps > 0) {
                stringResource(R.string.cleave_install_done_deps, task.title, task.dependencySteps)
            } else {
                stringResource(R.string.cleave_install_done, task.title)
            }
            CleaveInstallStatus.CANCELLED -> stringResource(R.string.cleave_install_cancelled)
            CleaveInstallStatus.FAILED -> task.failure.ifBlank { stringResource(R.string.cleave_install_failed) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
        ) {
            Text(
                text = line,
                color = if (task.finished && task.status != CleaveInstallStatus.DONE) statusColor
                else CleaveSurface.textSecondary,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (task.targetSummary.isNotBlank()) {
                Text(
                    text = stringResource(R.string.cleave_install_into, task.targetSummary),
                    color = CleaveSurface.textMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * The minimized install: a pill that sits over whatever page is open. Tapping it brings the panel
 * back; the download itself never stopped.
 */
@Composable
internal fun CleaveInstallPill(onExpand: () -> Unit, modifier: Modifier = Modifier) {
    val accent = CleaveThemeManager.currentAccent()
    val task = CleaveInstallQueue.activeTasks.firstOrNull() ?: CleaveInstallQueue.tasks.firstOrNull() ?: return
    val running = CleaveInstallQueue.running
    val tint = when (task.status) {
        CleaveInstallStatus.DONE -> CleaveSurface.success
        CleaveInstallStatus.FAILED, CleaveInstallStatus.CANCELLED -> CleaveSurface.danger
        else -> accent
    }
    val expandLabel = stringResource(R.string.cleave_install_expand)
    val label = when {
        running -> stringResource(
            R.string.cleave_install_progress_summary,
            task.completedSteps,
            task.totalSteps
        )
        task.status == CleaveInstallStatus.DONE -> stringResource(R.string.cleave_install_done, task.title)
        else -> stringResource(R.string.cleave_install_failed)
    }
    Surface(
        modifier = modifier
            .widthIn(max = 260.dp)
            .semantics { contentDescription = expandLabel },
        shape = RoundedCornerShape(CleaveRadii.pill),
        color = CleaveSurface.panelRaised,
        border = BorderStroke(CleaveSpacing.hairline, tint.copy(alpha = 0.6f)),
        onClick = onExpand
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
        ) {
            if (running) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = tint
                )
            } else {
                Icon(
                    painter = painterResource(
                        if (task.status == CleaveInstallStatus.DONE) R.drawable.cleave_ic_check
                        else R.drawable.cleave_ic_warning_filled
                    ),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp)
                )
            }
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = task.projectTitle,
                    color = CleaveSurface.textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    color = tint,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
