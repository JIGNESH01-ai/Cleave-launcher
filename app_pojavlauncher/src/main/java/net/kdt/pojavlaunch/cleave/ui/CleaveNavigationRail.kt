/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Ported from mirai-launcher- (Zalith Launcher 2 fork):
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/main/MiraiNavigationRail.kt
 *     (LauncherSection, MiraiNavigationRail, RailIconItem, AccountAvatarRailButton)
 * Account state comes from PojavAccountBridge instead of AccountsManager.currentAccountFlow.
 * Zalith Launcher 2 Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 * Copyright (C) 2026 CLEAVE Launcher contributors.
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

package net.kdt.pojavlaunch.cleave.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.CleaveAccount
import net.kdt.pojavlaunch.cleave.bridge.PojavAccountBridge
import net.kdt.pojavlaunch.cleave.components.CleaveImageWell
import net.kdt.pojavlaunch.cleave.components.bringFocusedItemIntoView
import net.kdt.pojavlaunch.cleave.components.liquidGlass
import net.kdt.pojavlaunch.cleave.theme.CLEAVE_COMPACT_HEIGHT_THRESHOLD_DP
import net.kdt.pojavlaunch.cleave.theme.CleaveMetrics
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.guide.CleaveGuideState
import net.kdt.pojavlaunch.cleave.guide.guideAnchor
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.ThemeMotion
import net.kdt.pojavlaunch.cleave.theme.accentGlow
import net.kdt.pojavlaunch.cleave.theme.accentRing
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager

/** Primary destinations kept visible while the CLEAVE launcher is open. */
enum class LauncherSection {
    HOME,
    /** Dedicated CLEAVE Client (Minecraft 1.16.5 Testing Edition · Liquid Crystal Clear). */
    CLEAVE_CLIENT,
    LIBRARY,
    SERVERS,
    /** Friends, messages and parties - the social launcher, opened from Home and the rail. */
    SOCIALS,
    DISCOVER,
    OTHER_RELEASES,
    DOWNLOADS,
    CONTENT,
    FILES,
    LOGS,
    SETTINGS,
    /** The team page, between Settings and the account entry. */
    CONTRIBUTION,
    /** Team CLEAVE modpacks, one press to install. Replaces the old Wallpapers rail entry. */
    MODPACKS,
    ACCOUNTS,
    ABOUT,
    /** Pages opened on top of a section (Mirai "Create New Instance", instance settings, account sign-in). */
    CREATE_INSTANCE,
    INSTANCE_EDITOR,
    /** Home utility destinations; Back returns to the dashboard. */
    QUICK_TOOLS,
    INSTANCE_SELECTOR,
    ADD_ACCOUNT,
    OFFLINE_ACCOUNT,
    CLEAVE_ACCOUNT,
    /** One team member's page, opened from Contribution. */
    CONTRIBUTION_DETAIL
}

@Composable
fun CleaveNavigationRail(
    selectedSection: LauncherSection,
    onNavigate: (LauncherSection) -> Unit,
    onCreateInstance: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account = PojavAccountBridge.current
    val scrollState = rememberScrollState()
    val activeAccent = CleaveThemeManager.currentAccent()
    val configuration = LocalConfiguration.current
    // Short screens (landscape phones): tighten the rail so every entry stays visible.
    val compactRail = configuration.screenHeightDp < CLEAVE_COMPACT_HEIGHT_THRESHOLD_DP
    val expandedRail = configuration.screenWidthDp >= 900 && !compactRail

    Row(modifier = modifier.fillMaxHeight()) {
        Surface(
            modifier = Modifier
                .width(if (expandedRail) CleaveMetrics.expandedNavigationRailWidth else CleaveMetrics.navigationRailWidth)
                .fillMaxHeight()
                // The pill sits against the left edge - only the end side keeps a gap, so the
                // pages own the rest of the width.
                .padding(start = 2.dp, end = CleaveSpacing.xs, top = CleaveSpacing.smPlus, bottom = CleaveSpacing.smPlus)
                .liquidGlass(shape = CircleShape, tint = CleaveSurface.glassTint, strength = 0.94f, elevation = CleaveMetrics.glassRailElevation),
            shape = CircleShape,
            color = Color.Transparent,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(vertical = if (compactRail) CleaveSpacing.xs else CleaveSpacing.smPlus),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compactRail) CleaveSpacing.xs else CleaveSpacing.sm)
            ) {
                // CLEAVE monogram and wordmark establish a persistent brand anchor.
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(CleaveRadii.panelSmall))
                        .bringFocusedItemIntoView()
                        .clickable(role = Role.Button) { onNavigate(LauncherSection.HOME) }
                        .semantics { contentDescription = "CLEAVE Home" }
                        .padding(vertical = CleaveSpacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xxs)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (compactRail) 32.dp else 38.dp)
                            .clip(RoundedCornerShape(CleaveRadii.control))
                            .background(activeAccent.copy(alpha = 0.12f))
                            .liquidGlass(
                                shape = RoundedCornerShape(CleaveRadii.control),
                                tint = activeAccent,
                                strength = 0.82f,
                                elevation = CleaveMetrics.glassSubtleElevation
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cleave_mark),
                            contentDescription = "CLEAVE",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(if (compactRail) 20.dp else 24.dp)
                        )
                    }
                    if (expandedRail) {
                        Text(text = "CLEAVE", fontSize = 8.sp, letterSpacing = 1.1.sp, fontWeight = FontWeight.ExtraBold, color = CleaveSurface.textSecondary)
                    }
                }

                Spacer(Modifier.height(if (compactRail) CleaveSpacing.zero else CleaveSpacing.xs))

                RailIconItem(R.drawable.cleave_ic_home_filled, stringResource(R.string.cleave_nav_home),
                    selectedSection == LauncherSection.HOME, activeAccent, expandedRail, compactRail) { onNavigate(LauncherSection.HOME) }
                RailIconItem(R.drawable.cleave_ic_add, stringResource(R.string.create_instance),
                    selectedSection == LauncherSection.CREATE_INSTANCE, activeAccent, expandedRail, compactRail,
                    onClick = onCreateInstance, guideKey = CleaveGuideState.CREATE)
                RailIconItem(R.drawable.cleave_ic_dashboard_filled, stringResource(R.string.cleave_nav_library),
                    librarySelected(selectedSection), activeAccent, expandedRail, compactRail,
                    onClick = { onNavigate(LauncherSection.LIBRARY) }, guideKey = CleaveGuideState.LIBRARY)
                RailIconItem(R.drawable.cleave_ic_client_crystal, "CLEAVE Client",
                    selectedSection == LauncherSection.CLEAVE_CLIENT, activeAccent, expandedRail, compactRail) { onNavigate(LauncherSection.CLEAVE_CLIENT) }
                RailIconItem(R.drawable.cleave_ic_archive_filled, "Our modpacks",
                    selectedSection == LauncherSection.MODPACKS, activeAccent, expandedRail, compactRail,
                    guideKey = CleaveGuideState.MODPACKS) { onNavigate(LauncherSection.MODPACKS) }
                RailIconItem(R.drawable.cleave_ic_sports_esports_outlined, "Servers",
                    selectedSection == LauncherSection.SERVERS, activeAccent, expandedRail, compactRail) { onNavigate(LauncherSection.SERVERS) }
                RailIconItem(R.drawable.cleave_ic_public, stringResource(R.string.cleave_nav_discover),
                    discoverSelected(selectedSection), activeAccent, expandedRail, compactRail) { onNavigate(LauncherSection.DISCOVER) }
                RailIconItem(R.drawable.cleave_ic_rocket_launch_filled, "Our other new releases",
                    selectedSection == LauncherSection.OTHER_RELEASES, activeAccent, expandedRail, compactRail) { onNavigate(LauncherSection.OTHER_RELEASES) }

                HorizontalDivider(modifier = Modifier.width(28.dp).padding(vertical = CleaveSpacing.xxs), color = CleaveSurface.borderSoft)

                // Every remaining destination lives in this one sidebar. The old right-hand
                // utility rail moved here, so no launcher option is ever shown twice and the
                // right edge of the screen stays free of navigation.
                AccountAvatarRailButton(
                    account = account,
                    selected = selectedSection == LauncherSection.ACCOUNTS || selectedSection == LauncherSection.ADD_ACCOUNT || selectedSection == LauncherSection.OFFLINE_ACCOUNT,
                    expanded = expandedRail,
                    compact = compactRail,
                    onClick = { onNavigate(LauncherSection.ACCOUNTS) }
                )
                RailIconItem(R.drawable.cleave_ic_person_outlined, "Profile",
                    selectedSection == LauncherSection.SOCIALS, activeAccent, expandedRail, compactRail) { onOpenProfile() }
                RailIconItem(R.drawable.cleave_ic_videogame_asset_outlined, "Instances",
                    selectedSection == LauncherSection.INSTANCE_SELECTOR, activeAccent, expandedRail, compactRail) { onNavigate(LauncherSection.INSTANCE_SELECTOR) }
                RailIconItem(R.drawable.cleave_ic_folder_outlined, "Files",
                    selectedSection == LauncherSection.FILES, activeAccent, expandedRail, compactRail) { onNavigate(LauncherSection.FILES) }
                RailIconItem(R.drawable.cleave_ic_build_outlined, "Quick Tools",
                    selectedSection == LauncherSection.QUICK_TOOLS, activeAccent, expandedRail, compactRail) { onNavigate(LauncherSection.QUICK_TOOLS) }
                RailIconItem(R.drawable.cleave_ic_description_outlined, "Logs",
                    selectedSection == LauncherSection.LOGS, activeAccent, expandedRail, compactRail) { onNavigate(LauncherSection.LOGS) }
                RailIconItem(R.drawable.cleave_ic_group_outlined, stringResource(R.string.cleave_nav_contribution),
                    selectedSection == LauncherSection.CONTRIBUTION || selectedSection == LauncherSection.CONTRIBUTION_DETAIL,
                    activeAccent, expandedRail, compactRail) { onNavigate(LauncherSection.CONTRIBUTION) }
                RailIconItem(R.drawable.cleave_ic_settings_filled, stringResource(R.string.cleave_nav_settings),
                    settingsSelected(selectedSection), activeAccent, expandedRail, compactRail,
                    onClick = { onNavigate(LauncherSection.SETTINGS) }, guideKey = CleaveGuideState.SETTINGS)
            }
        }
    }
}

private fun librarySelected(section: LauncherSection): Boolean =
    section == LauncherSection.LIBRARY || section == LauncherSection.INSTANCE_EDITOR || section == LauncherSection.CONTENT

private fun discoverSelected(section: LauncherSection): Boolean =
    section == LauncherSection.DISCOVER || section == LauncherSection.DOWNLOADS

/**
 * Settings is one destination with several tabs, so its rail entry stays highlighted on every tab.
 * The former Wallpapers entry had to share the highlight with it; that entry is gone, so the
 * pairing is now simply "Settings is selected whenever Settings is open".
 */
private fun settingsSelected(section: LauncherSection): Boolean =
    section == LauncherSection.SETTINGS || section == LauncherSection.ABOUT || section == LauncherSection.LOGS

@Composable
private fun RailIconItem(
    iconRes: Int,
    label: String,
    selected: Boolean,
    accentColor: Color,
    expanded: Boolean,
    compact: Boolean,
    /** Names this item for the first-run guide, so the tour can point at it. */
    guideKey: String? = null,
    onClick: () -> Unit
) {
    // The selected item uses the current theme accent; icon, fill and border update together.
    val selectedContentColor = MaterialTheme.colorScheme.onPrimary
    // With the animated theme on, the selected item's accent breathes and carries a glow. Off, it
    // is the identical flat accent as before - and no animation is started for unselected items.
    val motionOn = ThemeMotion.rememberEnabled()
    val liveAccent = ThemeMotion.rememberPulse(accentColor, selected, motionOn)
    val containerColor by animateColorAsState(
        targetValue = if (selected) liveAccent else CleaveSurface.panel,
        animationSpec = tween(140),
        label = "railContainerColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) selectedContentColor else CleaveSurface.textSecondary,
        animationSpec = tween(140),
        label = "railIconTint"
    )
    val itemWidth = when {
        expanded -> 64.dp
        compact -> 36.dp
        else -> 44.dp
    }
    val itemHeight = when {
        expanded -> 54.dp
        compact -> 36.dp
        else -> 42.dp
    }
    val itemShape = RoundedCornerShape(CleaveRadii.control)
    // Compact rails trade the always-on caption for a long-press tooltip, so every entry can
    // still be identified without widening the sidebar.
    var showTip by remember { mutableStateOf(false) }
    LaunchedEffect(showTip) {
        if (showTip) {
            delay(1400)
            showTip = false
        }
    }
    Box(
        modifier = Modifier
            .then(if (guideKey != null) Modifier.guideAnchor(guideKey) else Modifier)
            .size(width = itemWidth, height = itemHeight)
            .clip(itemShape)
            .background(containerColor)
            .accentGlow(liveAccent, selected, motionOn)
            .accentRing(liveAccent, itemShape, selected, motionOn, CleaveSpacing.hairline)
            .border(BorderStroke(CleaveSpacing.hairline, if (selected) liveAccent else CleaveSurface.borderSoft), itemShape)
            .bringFocusedItemIntoView()
            .combinedClickable(role = Role.Tab, onClick = onClick, onLongClick = { showTip = true })
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        if (showTip && !expanded) {
            Popup(alignment = Alignment.CenterEnd, offset = IntOffset(14, 0), onDismissRequest = { showTip = false }) {
                Surface(
                    shape = RoundedCornerShape(CleaveRadii.controlSmall),
                    color = CleaveSurface.panelRaised,
                    border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
                ) {
                    Text(
                        text = label,
                        color = CleaveSurface.textPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = CleaveSpacing.sm, vertical = CleaveSpacing.xxs)
                    )
                }
            }
        }
        if (expanded) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(painter = painterResource(iconRes), contentDescription = label, tint = iconTint, modifier = Modifier.size(19.dp))
                Text(
                    text = label,
                    color = if (selected) iconTint else CleaveSurface.textSecondary,
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        } else {
            Icon(painter = painterResource(iconRes), contentDescription = label, tint = iconTint, modifier = Modifier.size(if (compact) 18.dp else 21.dp))
        }
    }
}

@Composable
private fun AccountAvatarRailButton(
    account: CleaveAccount?,
    selected: Boolean,
    expanded: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    val activeAccent = CleaveThemeManager.currentAccent()
    val ringColor by animateColorAsState(
        targetValue = if (selected) activeAccent else CleaveSurface.borderSoft,
        animationSpec = tween(150),
        label = "accountAvatarRing"
    )
    val accountsLabel = stringResource(R.string.cleave_nav_accounts)
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(CleaveRadii.panelSmall))
            .bringFocusedItemIntoView()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = account?.username ?: accountsLabel }
            .padding(CleaveSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 32.dp else 36.dp)
                .clip(CircleShape)
                .background(CleaveSurface.panel)
                .liquidGlass(shape = CircleShape, tint = activeAccent, strength = if (selected) 0.82f else 0.34f, elevation = CleaveMetrics.glassSelectedElevation)
                .border(width = if (selected) 2.dp else CleaveSpacing.hairline, color = ringColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CleaveImageWell(
                image = account?.face,
                fallback = R.drawable.cleave_head_steve,
                size = if (compact) 26.dp else 30.dp,
                radius = 15.dp
            )
        }
        if (expanded) {
            Text(
                text = accountsLabel,
                color = if (selected) activeAccent else CleaveSurface.textSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}
