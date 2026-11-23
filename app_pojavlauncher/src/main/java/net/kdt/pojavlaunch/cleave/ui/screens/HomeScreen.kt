/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Ported from mirai-launcher- (Zalith Launcher 2 fork):
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/LauncherScreen.kt
 *     (LauncherScreen, ActionMenuCardContent, VersionsContent)
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/home/MiraiHomeDashboard.kt
 *     (MiraiHomeDashboard, WideHomeDashboard, CompactHomeDashboard, LiquidHomeHero, ActiveWorldPanel,
 *      TaskSummaryPanel, RecentWorldsPanel, RecentWorldRow, QuickToolsPanel, HomeToolAction)
 * Data comes from PojavInstanceBridge / PojavAccountBridge / PojavTaskBridge; PLAY goes through PojavLaunchBridge.
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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import net.kdt.pojavlaunch.cleave.bridge.CleavePreferences
import net.kdt.pojavlaunch.cleave.bridge.CleaveLauncherUiStyle
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.CleaveInstance
import net.kdt.pojavlaunch.cleave.bridge.GameDownloadTarget
import net.kdt.pojavlaunch.cleave.bridge.PojavAccountBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavLaunchBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavNavigator
import net.kdt.pojavlaunch.cleave.bridge.PojavTaskBridge
import net.kdt.pojavlaunch.cleave.components.CleaveImageWell
import net.kdt.pojavlaunch.cleave.components.CleaveSkinPreview3D
import net.kdt.pojavlaunch.cleave.components.MetaPill
import net.kdt.pojavlaunch.cleave.components.QuickActionTile
import net.kdt.pojavlaunch.cleave.components.QuietEmptyState
import net.kdt.pojavlaunch.cleave.components.liquidGlass
import net.kdt.pojavlaunch.cleave.theme.CleaveMetrics
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.guide.CleaveGuideState
import net.kdt.pojavlaunch.cleave.guide.guideAnchor
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.cleave.ui.CleaveNav
import net.kdt.pojavlaunch.cleave.ui.LauncherSection
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

private const val ContentWeight = 7.4f
private const val ActionMenuWeight = 2.6f
private val ActionMenuOuterPadding = 12.dp

/** Degrees of doll rotation per pixel of horizontal drag (mirai LauncherScreen). */
private const val DOLL_DRAG_DEG_PER_PX = 0.45f
private const val DOLL_FLING_FRICTION = 5f
private const val DOLL_FLING_MIN_VELOCITY = 60f
private const val DOLL_FLING_STOP_VELOCITY = 20f

/**
 * "Fabric 1.16.5" - the loader and the version of the game it runs, never the loader's own
 * version. A loader id ("fabric-loader-0.16.10-1.16.5") carries both numbers, and the first one is
 * the loader's, so the Minecraft version has to be picked by shape: see
 * [GameDownloadTarget.minecraftVersionIn].
 */
private fun instanceVersionLabel(versionId: String, vanilla: String): String {
    val mcVersion = GameDownloadTarget.minecraftVersionIn(versionId) ?: versionId
    val loader = loaderLabel(versionId, vanilla)
    return if (loader == vanilla) mcVersion else "$loader $mcVersion"
}

private class HomeActions(
    val play: () -> Unit,
    val playInstance: (CleaveInstance) -> Unit,
    val select: (CleaveInstance) -> Unit,
    val edit: (CleaveInstance) -> Unit,
    val create: () -> Unit,
    val socials: () -> Unit,
    val explore: () -> Unit,
    val library: () -> Unit,
    val files: () -> Unit,
    val controls: () -> Unit,
    val runJar: () -> Unit,
    val shareLogs: () -> Unit,
    val gamepad: () -> Unit,
    val content: () -> Unit,
    val downloads: () -> Unit,
    val fileManager: () -> Unit,
    val logs: () -> Unit,
    val accounts: () -> Unit,
    val cleaveAccount: () -> Unit,
    val quickTools: () -> Unit,
    val instanceSelector: () -> Unit
)

@Composable
fun HomeScreen(activity: FragmentActivity, navigate: (LauncherSection) -> Unit) {
    LaunchedEffect(Unit) {
        PojavAccountBridge.ensureLoaded()
    }
    val instances = PojavInstanceBridge.instances
    val selected = PojavInstanceBridge.selected
    val jarLauncher = rememberLauncherForActivityResult(OpenDocumentWithExtension("jar")) { uri ->
        if (uri != null) PojavNavigator.runJar(activity, uri)
    }
    val actions = HomeActions(
        play = { PojavLaunchBridge.play(activity) },
        playInstance = { instance ->
            PojavInstanceBridge.select(instance)
            PojavLaunchBridge.play(activity)
        },
        select = { PojavInstanceBridge.select(it) },
        edit = { PojavInstanceBridge.edit(activity, it) },
        create = { CleaveNav.navigate(LauncherSection.CREATE_INSTANCE) },
        socials = { navigate(LauncherSection.SOCIALS) },
        explore = { navigate(LauncherSection.DISCOVER) },
        library = { navigate(LauncherSection.LIBRARY) },
        files = { PojavInstanceBridge.openGameDirectory(activity) },
        controls = { PojavNavigator.openCustomControls(activity) },
        runJar = { if (PojavNavigator.canRunJar(activity)) jarLauncher.launch(null) },
        shareLogs = { PojavNavigator.shareLogs(activity) },
        gamepad = { PojavNavigator.openGamepadMapper(activity) },
        content = {
            selected?.let { PojavInstanceBridge.edit(activity, it) } ?: navigate(LauncherSection.LIBRARY)
        },
        downloads = { navigate(LauncherSection.DISCOVER) },
        fileManager = { navigate(LauncherSection.FILES) },
        logs = { navigate(LauncherSection.LOGS) },
        accounts = { navigate(LauncherSection.ACCOUNTS) },
        cleaveAccount = { CleaveNav.navigate(LauncherSection.CLEAVE_ACCOUNT) },
        quickTools = { navigate(LauncherSection.QUICK_TOOLS) },
        instanceSelector = { navigate(LauncherSection.INSTANCE_SELECTOR) }
    )

    // "The old UI" is this dashboard, and it is a different composition rather than the same one
    // with different padding: the utility tiles under YOUR SPACE, the active-world card, tasks and
    // recent worlds below them, and the docked 3D skin card with its own PLAY on the right. The
    // character stage that replaced it is the Clean skin. Reading the preference as Compose state is
    // what makes the switch repaint the home page on the spot, with no restart.
    if (CleavePreferences.uiStyle == CleaveLauncherUiStyle.PREMIUM) {
        val accent = CleaveThemeManager.currentAccent()
        val taskCount = PojavTaskBridge.taskCount
        val recent = remember(instances, selected) {
            instances.filterNot { it == selected }.take(8).ifEmpty { instances.take(8) }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            // The switch stays on the page in both skins, so choosing the old look is never a trap.
            CleaveUiStyleSwitch(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = CleaveSpacing.mdPlus, end = CleaveSpacing.mdPlus, top = CleaveSpacing.xs)
            )
            ClassicHomeDashboard(
                selected = selected,
                instances = instances,
                recent = recent,
                count = instances.size,
                accent = accent,
                taskCount = taskCount,
                actions = actions
            )
        }
    } else {
        HomeCharacterStage(
            selected = selected,
            actions = actions,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = CleaveSpacing.mdPlus, end = CleaveSpacing.xs, top = CleaveSpacing.smPlus, bottom = CleaveSpacing.smPlus)
        )
    }
}

/**
 * Home as it was before the character stage became the hero: Mirai's LauncherScreen split, the
 * content dashboard on the left (weight 7.4f) and the docked 3D skin [ActionMenuCard] on the right
 * (weight 2.6f) whenever the window is 500.dp wide or more. Inside the dashboard, landscape and at
 * least 720.dp wide gets [WideHomeDashboard]; anything smaller scrolls as [CompactHomeDashboard].
 */
@Composable
private fun ClassicHomeDashboard(
    selected: CleaveInstance?,
    instances: List<CleaveInstance>,
    recent: List<CleaveInstance>,
    count: Int,
    accent: Color,
    taskCount: Int,
    actions: HomeActions
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val showActionMenu = maxWidth >= 500.dp
        Row(modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(if (showActionMenu) ContentWeight else 1f)
                    .fillMaxHeight()
                    .padding(start = CleaveSpacing.mdPlus, end = CleaveSpacing.smCompact, top = CleaveSpacing.smPlus)
            ) {
                if (maxWidth >= 720.dp && maxWidth > maxHeight) {
                    WideHomeDashboard(selected, recent, count, accent, taskCount, actions)
                } else {
                    CompactHomeDashboard(selected, recent, count, accent, taskCount, actions)
                }
            }
            if (showActionMenu) {
                ActionMenuCard(
                    selected = selected,
                    instances = instances,
                    actions = actions,
                    modifier = Modifier
                        .guideAnchor(CleaveGuideState.PLAY)
                        .weight(ActionMenuWeight)
                        .fillMaxHeight()
                        .padding(
                            top = ActionMenuOuterPadding,
                            bottom = ActionMenuOuterPadding,
                            end = ActionMenuOuterPadding
                        )
                )
            }
        }
    }
}

/** The Clean skin's home: the 360° character is the hero, with instance selection and Play
 * directly below it. Everything else lives in the utility rails and their destinations. The Premium
 * skin renders [ClassicHomeDashboard] instead - see [HomeScreen]. */
@Composable
private fun HomeCharacterStage(
    selected: CleaveInstance?,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    val account = PojavAccountBridge.current
    val accent = CleaveThemeManager.currentAccent()
    val vanilla = stringResource(R.string.cleave_home_vanilla)
    var dollAzimuth by remember(account?.username) { mutableFloatStateOf(28f) }
    val flingScope = rememberCoroutineScope()
    val velocityTracker = remember(account?.username) { VelocityTracker() }
    var flingJob by remember(account?.username) { mutableStateOf<Job?>(null) }
    val versionLabel = remember(selected, vanilla) {
        selected?.let { instanceVersionLabel(it.versionId, vanilla) } ?: "Choose a Minecraft instance"
    }
    // Premium UI is the older, heavier skin - wide glass stage, pill buttons, generous padding.
    // Clean UI is the newer flat one: the same controls, tighter rows, smaller stage. Reading the
    // preference as Compose state is what makes the switch take effect on the spot.
    val clean = CleavePreferences.uiStyle == CleaveLauncherUiStyle.CLEAN
    val stageGap by animateDpAsState(if (clean) CleaveSpacing.xs else CleaveSpacing.sm, label = "stageGap")
    val stagePadding by animateDpAsState(if (clean) CleaveSpacing.xs else CleaveSpacing.smPlus, label = "stagePadding")
    val buttonHeight by animateDpAsState(if (clean) 40.dp else 46.dp, label = "playHeight")
    val buttonRadius by animateDpAsState(if (clean) 14.dp else 999.dp, label = "playRadius")
    val panelAlpha by animateFloatAsState(if (clean) 0.55f else 0.8f, label = "panelAlpha")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(stageGap)
    ) {
        CleaveUiStyleSwitch(
            modifier = Modifier.fillMaxWidth().padding(horizontal = stagePadding, vertical = CleaveSpacing.xxs)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = stagePadding, vertical = CleaveSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(if (clean) "CHARACTER" else "YOUR CHARACTER", color = CleaveSurface.textSecondary,
                    fontSize = if (clean) 9.sp else 10.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                Text(account?.username ?: "CLEAVE", color = CleaveSurface.textPrimary,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Surface(
                onClick = actions.accounts,
                shape = RoundedCornerShape(999.dp),
                color = if (account == null) accent.copy(alpha = 0.16f) else CleaveSurface.panel.copy(alpha = 0.8f),
                border = BorderStroke(CleaveSpacing.hairline, if (account == null) accent.copy(alpha = 0.7f) else CleaveSurface.borderSoft)
            ) {
                Text(
                    if (account == null) "ADD MINECRAFT ACCOUNT" else "DRAG TO ROTATE",
                    color = if (account == null) accent else CleaveSurface.textSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = CleaveSpacing.smPlus)
                .clipToBounds()
                .drawWithCache {
                    val glow = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.58f),
                        radius = size.minDimension * 0.54f
                    )
                    onDrawBehind { drawRect(glow) }
                }
                .pointerInput(account?.username) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            flingJob?.cancel()
                            flingJob = null
                            velocityTracker.resetTracking()
                        },
                        onDragEnd = {
                            val releaseVelocity = -velocityTracker.calculateVelocity().x * DOLL_DRAG_DEG_PER_PX
                            flingJob?.cancel()
                            if (abs(releaseVelocity) < DOLL_FLING_MIN_VELOCITY) {
                                flingJob = null
                                return@detectHorizontalDragGestures
                            }
                            flingJob = flingScope.launch {
                                var velocity = releaseVelocity
                                var lastFrame = withFrameNanos { it }
                                while (abs(velocity) > DOLL_FLING_STOP_VELOCITY) {
                                    ensureActive()
                                    val now = withFrameNanos { it }
                                    val dt = ((now - lastFrame) / 1_000_000_000f).coerceIn(0f, 0.05f)
                                    lastFrame = now
                                    dollAzimuth = (dollAzimuth + velocity * dt).mod(360f)
                                    velocity *= exp(-DOLL_FLING_FRICTION * dt)
                                }
                                flingJob = null
                            }
                        },
                        onDragCancel = { flingJob?.cancel(); flingJob = null },
                        onHorizontalDrag = { change, dragAmount ->
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            dollAzimuth = (dollAzimuth - dragAmount * DOLL_DRAG_DEG_PER_PX).mod(360f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            CleaveSkinPreview3D(
                account = account,
                azimuth = dollAzimuth.roundToInt(),
                interactionEnabled = false,
                useClientCape = true,
                showOfficialCape = true,
                animationName = "NewIdle",
                cameraDistance = 48,
                modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp)
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = stagePadding),
            shape = RoundedCornerShape(CleaveRadii.cardSmall),
            color = CleaveSurface.panel.copy(alpha = panelAlpha),
            border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft),
            onClick = actions.instanceSelector
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = CleaveSpacing.smPlus, vertical = CleaveSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(painterResource(R.drawable.cleave_ic_videogame_asset_outlined), contentDescription = null,
                    tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(CleaveSpacing.xs))
                Text(selected?.name ?: "Select instance", color = CleaveSurface.textPrimary,
                    fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(CleaveSpacing.xs))
                Text(versionLabel, color = accent, fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = stagePadding).height(buttonHeight)
                .guideAnchor(CleaveGuideState.PLAY),
            shape = RoundedCornerShape(buttonRadius),
            color = accent,
            contentColor = CleaveSurface.onAccent,
            onClick = if (selected != null) actions.play else actions.instanceSelector
        ) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Text("PLAY", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold,
                    color = CleaveSurface.onAccent)
                Spacer(Modifier.width(CleaveSpacing.xs))
                Icon(painterResource(R.drawable.cleave_ic_play_arrow_filled), contentDescription = "Play",
                    tint = CleaveSurface.onAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/**
 * The docked right-hand card of [ClassicHomeDashboard]: the 3D rotatable skin stage, an instance
 * menu and the PLAY button. [HomeCharacterStage] is the other skin's take on the same controls.
 */
@Composable
private fun ActionMenuCard(
    selected: CleaveInstance?,
    instances: List<CleaveInstance>,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    val account = PojavAccountBridge.current
    val accent = CleaveThemeManager.currentAccent()
    val vanilla = stringResource(R.string.cleave_home_vanilla)
    var dollAzimuth by remember(account?.username) { mutableFloatStateOf(28f) }
    val flingScope = rememberCoroutineScope()
    val dollVelocityTracker = remember(account?.username) { VelocityTracker() }
    var dollFlingJob by remember(account?.username) { mutableStateOf<Job?>(null) }
    var showInstanceMenu by remember { mutableStateOf(false) }

    // The loader and the version of the game: "Fabric 1.16.5". The loader's own version
    // ("0.16.10") is not what the player picked and does not belong on the card.
    val versionSubtitle = remember(selected, vanilla) {
        when {
            selected == null -> "Select Instance"
            loaderLabel(selected.versionId, vanilla) == vanilla ->
                "Minecraft ${GameDownloadTarget.minecraftVersionIn(selected.versionId) ?: selected.versionId}"
            else -> instanceVersionLabel(selected.versionId, vanilla)
        }
    }

    Surface(
        modifier = modifier.liquidGlass(shape = RoundedCornerShape(CleaveRadii.card), strength = 0.75f),
        shape = RoundedCornerShape(CleaveRadii.card),
        color = CleaveSurface.panel,
        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 3D Skin & Cape Stage on Dark Pedestal (Mirai LauncherScreen ActionMenuCardContent)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = CleaveSpacing.smCompact, start = CleaveSpacing.sm, end = CleaveSpacing.sm)
                    .clip(RoundedCornerShape(CleaveRadii.control))
                    .pointerInput(actions) {
                        detectTapGestures(onTap = { actions.accounts() })
                    }
                    .pointerInput(account?.username) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dollFlingJob?.cancel()
                                dollFlingJob = null
                                dollVelocityTracker.resetTracking()
                            },
                            onDragEnd = {
                                val releaseVelocity = -dollVelocityTracker.calculateVelocity().x * DOLL_DRAG_DEG_PER_PX
                                dollFlingJob?.cancel()
                                if (abs(releaseVelocity) < DOLL_FLING_MIN_VELOCITY) {
                                    dollFlingJob = null
                                    return@detectHorizontalDragGestures
                                }
                                dollFlingJob = flingScope.launch {
                                    var velocity = releaseVelocity
                                    var lastFrame = withFrameNanos { it }
                                    while (abs(velocity) > DOLL_FLING_STOP_VELOCITY) {
                                        ensureActive()
                                        val now = withFrameNanos { it }
                                        val dt = ((now - lastFrame) / 1_000_000_000f).coerceIn(0f, 0.05f)
                                        lastFrame = now
                                        dollAzimuth = (dollAzimuth + velocity * dt).mod(360f)
                                        velocity *= exp(-DOLL_FLING_FRICTION * dt)
                                    }
                                    dollFlingJob = null
                                }
                            },
                            onDragCancel = {
                                dollFlingJob?.cancel()
                                dollFlingJob = null
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                dollVelocityTracker.addPosition(change.uptimeMillis, change.position)
                                dollAzimuth = (dollAzimuth - dragAmount * DOLL_DRAG_DEG_PER_PX).mod(360f)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                CleaveSkinPreview3D(
                    account = account,
                    azimuth = dollAzimuth.roundToInt(),
                    interactionEnabled = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = CleaveSpacing.mdPlus)
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-4).dp),
                    shape = RoundedCornerShape(CleaveRadii.compact),
                    color = CleaveSurface.canvas,
                    border = BorderStroke(CleaveSpacing.hairline, accent.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = account?.username ?: "+ Add Account",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (account != null) Color.White else accent,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = CleaveSpacing.smPlus, vertical = CleaveSpacing.tiny)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
            ) {
                // Compact 2-line Instance Selector Pill (Mirai VersionsContent)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CleaveSpacing.smPlus)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(CleaveRadii.cardSmall),
                        color = CleaveSurface.panel,
                        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft),
                        onClick = {
                            if (instances.isNotEmpty()) showInstanceMenu = true else actions.library()
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CleaveSpacing.smPlus, vertical = CleaveSpacing.smTight),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xxs)
                        ) {
                            Text(
                                text = selected?.name ?: stringResource(R.string.cleave_home_no_active_world),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = versionSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = accent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showInstanceMenu,
                        onDismissRequest = { showInstanceMenu = false }
                    ) {
                        instances.forEach { instance ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                                    ) {
                                        CleaveImageWell(
                                            image = instance.icon,
                                            fallback = R.drawable.cleave_ic_videogame_asset_outlined,
                                            size = 24.dp,
                                            radius = CleaveRadii.tiny
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(instance.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text(instance.versionId, style = MaterialTheme.typography.labelSmall, color = CleaveSurface.textSecondary, maxLines = 1)
                                        }
                                    }
                                },
                                onClick = {
                                    actions.select(instance)
                                    showInstanceMenu = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Manage All Instances...",
                                    color = accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            onClick = {
                                showInstanceMenu = false
                                actions.library()
                            }
                        )
                    }
                }

                // Vibrant Pill PLAY Button (Mirai VersionsContent)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CleaveSpacing.smPlus)
                        .padding(bottom = CleaveSpacing.smPlus)
                        .height(42.dp),
                    shape = RoundedCornerShape(CleaveRadii.panel),
                    color = accent,
                    contentColor = CleaveSurface.onAccent,
                    onClick = if (selected != null) actions.play else actions.create
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "PLAY",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = CleaveSurface.onAccent
                        )
                        Spacer(Modifier.width(CleaveSpacing.xs))
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_play_arrow_filled),
                            contentDescription = stringResource(R.string.cleave_home_play),
                            tint = CleaveSurface.onAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WideHomeDashboard(
    selected: CleaveInstance?,
    recent: List<CleaveInstance>,
    count: Int,
    accent: Color,
    taskCount: Int,
    actions: HomeActions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = CleaveSpacing.smPlus),
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
    ) {
        SameVersionNudgeStrip(selected = selected, accent = accent, onOpenSocials = actions.socials)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.57f),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
        ) {
            // The hero banner is gone: the active world now opens the page, and everything that
            // used to sit below it moves up into the space it took.
            ActiveWorldPanel(selected, accent, actions, Modifier.weight(1.55f).fillMaxHeight())
            Column(
                modifier = Modifier
                    .weight(0.85f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
            ) {
                TaskSummaryPanel(taskCount, accent, Modifier.height(74.dp))
                QuickToolsPanel(accent, actions, Modifier.weight(1f), scrollable = true)
            }
        }
        QuickTiles(count, accent, actions, Modifier.fillMaxWidth().height(78.dp), scrollable = false)
        CleaveAccountHomeCard(accent, actions.cleaveAccount)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.43f),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
        ) {
            RecentWorldsPanel(recent, selected, accent, actions, Modifier.weight(1f).fillMaxHeight(), lazy = true)
        }
    }
}

@Composable
private fun CompactHomeDashboard(
    selected: CleaveInstance?,
    recent: List<CleaveInstance>,
    count: Int,
    accent: Color,
    taskCount: Int,
    actions: HomeActions
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = CleaveSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)) {
                SameVersionNudgeStrip(selected = selected, accent = accent, onOpenSocials = actions.socials)
                HomeSectionLabel(stringResource(R.string.cleave_home_your_space), null)
                QuickTiles(count, accent, actions, Modifier.fillMaxWidth(), scrollable = true)
            }
        }
        // On a phone the side action panel is collapsed, so the play step points at the world card here.
        item {
            ActiveWorldPanel(
                selected, accent, actions,
                Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .guideAnchor(CleaveGuideState.PLAY)
            )
        }
        item { TaskSummaryPanel(taskCount, accent, Modifier.fillMaxWidth().height(74.dp)) }
        item { RecentWorldsPanel(recent, selected, accent, actions, Modifier.fillMaxWidth(), lazy = false) }
        item { QuickToolsPanel(accent, actions, Modifier.fillMaxWidth(), scrollable = false) }
        item { CleaveAccountHomeCard(accent, actions.cleaveAccount) }
    }
}

@Composable
private fun CleaveAccountHomeCard(accent: Color, onClick: () -> Unit) {
    val signedIn = net.kdt.pojavlaunch.cleave.social.SocialBackend.signedIn
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(CleaveRadii.card),
        color = CleaveSurface.panelRaised,
        border = BorderStroke(CleaveSpacing.hairline, accent.copy(alpha = 0.5f))
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(painterResource(R.drawable.cleave_ic_person_outlined), contentDescription = null,
                tint = accent, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(if (signedIn) "CLEAVE account" else "Sign in to CLEAVE account",
                    color = CleaveSurface.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Minecraft name, skin & cape", color = CleaveSurface.textSecondary, fontSize = 10.sp)
            }
            Text("›", color = accent, fontSize = 22.sp)
        }
    }
}

@Composable
private fun QuickTiles(count: Int, accent: Color, actions: HomeActions, modifier: Modifier, scrollable: Boolean) {
    val rowModifier = if (scrollable) modifier.horizontalScroll(rememberScrollState()) else modifier
    // The Socials tile tells you who is around without opening anything.
    val socialContext = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        net.kdt.pojavlaunch.cleave.social.SocialBackend.attach(socialContext)
        if (net.kdt.pojavlaunch.cleave.social.SocialBackend.signedIn) {
            net.kdt.pojavlaunch.cleave.social.SocialBackend.refreshFriends()
            net.kdt.pojavlaunch.cleave.social.SocialBackend.refreshParties()
            net.kdt.pojavlaunch.cleave.social.SocialBackend.refreshPartyInvites()
            net.kdt.pojavlaunch.cleave.social.SocialBackend.unreadCount()
        }
    }
    val playableNow = net.kdt.pojavlaunch.cleave.social.SocialBackend.friends.count { it.online && !it.pendingIncoming && !it.pendingOutgoing }
    val waiting = net.kdt.pojavlaunch.cleave.social.SocialBackend.totalActionableBadge
    val socialSubtitle = when {
        playableNow > 0 && waiting > 0 -> "$playableNow online · $waiting waiting"
        playableNow > 0 -> "$playableNow online now"
        waiting > 0 -> "$waiting unread / invite${if (waiting == 1) "" else "s"}"
        else -> stringResource(R.string.cleave_home_quick_socials_sub)
    }
    Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)) {
        val tiles = listOf(
            // The card that used to open the instance creator now opens Socials: creating an
            // instance lives on the rail's + button and in the library, where it is not competing
            // with the one thing the launcher can do that no other launcher can.
            Triple(R.drawable.cleave_ic_people, stringResource(R.string.cleave_home_quick_socials) to socialSubtitle, accent to actions.socials),
            Triple(R.drawable.cleave_ic_search, stringResource(R.string.cleave_home_quick_explore) to stringResource(R.string.cleave_home_quick_explore_sub), CleaveSurface.glassBlue to actions.explore),
            Triple(R.drawable.cleave_ic_dashboard_filled, stringResource(R.string.cleave_home_quick_library) to stringResource(R.string.cleave_home_quick_library_sub, count), CleaveSurface.glassViolet to actions.library),
            Triple(R.drawable.cleave_ic_folder_outlined, stringResource(R.string.cleave_home_quick_files) to stringResource(R.string.cleave_home_quick_files_sub), CleaveSurface.glassRose to actions.fileManager)
        )
        tiles.forEach { (icon, texts, style) ->
            QuickActionTile(
                iconRes = icon,
                title = texts.first,
                subtitle = texts.second,
                accent = style.first,
                onClick = style.second,
                modifier = if (scrollable) Modifier.width(198.dp).height(78.dp) else Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun HomeSectionLabel(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = title.uppercase(), color = CleaveSurface.textPrimary, fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.ExtraBold)
        if (subtitle != null) {
            Text(text = subtitle, color = CleaveSurface.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ActiveWorldPanel(selected: CleaveInstance?, accent: Color, actions: HomeActions, modifier: Modifier) {
    val shape = RoundedCornerShape(CleaveRadii.panel)
    val vanilla = stringResource(R.string.cleave_home_vanilla)
    Surface(
        modifier = modifier.liquidGlass(shape = shape, tint = accent, strength = 0.82f, elevation = CleaveMetrics.glassSubtleElevation),
        shape = shape,
        color = Color.Transparent,
        contentColor = CleaveSurface.textPrimary,
        border = BorderStroke(1.dp, CleaveSurface.borderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CleaveSpacing.mdPlus),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HomeSectionLabel(stringResource(R.string.cleave_home_active_world), stringResource(R.string.cleave_home_active_sub))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
            ) {
                CleaveImageWell(image = selected?.icon, fallback = R.drawable.cleave_ic_videogame_asset_outlined, size = 58.dp, radius = CleaveRadii.card)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)) {
                    Text(
                        text = selected?.name ?: stringResource(R.string.cleave_home_no_active_world),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = selected?.let {
                            val mcVersion = GameDownloadTarget.minecraftVersionIn(it.versionId) ?: it.versionId
                            "${loaderLabel(it.versionId, vanilla)}  ·  ${stringResource(R.string.cleave_home_minecraft, mcVersion)}"
                        }
                            ?: stringResource(R.string.cleave_home_create_first),
                        color = CleaveSurface.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .liquidGlass(shape = CircleShape, tint = accent, strength = 0.9f, elevation = CleaveMetrics.glassSubtleElevation),
                    shape = CircleShape,
                    color = accent,
                    contentColor = CleaveSurface.onAccent,
                    onClick = if (selected != null) actions.play else actions.create
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = CleaveSpacing.md),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(if (selected != null) R.drawable.cleave_ic_play_arrow_filled else R.drawable.cleave_ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(CleaveSpacing.xs))
                        Text(
                            text = stringResource(if (selected != null) R.string.cleave_home_play else R.string.cleave_home_create),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(
                    onClick = { selected?.let { actions.edit(it) } },
                    enabled = selected != null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CleaveSurface.panelTrack)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_settings_filled),
                        contentDescription = stringResource(R.string.cleave_home_instance_settings),
                        tint = if (selected != null) CleaveSurface.textPrimary else CleaveSurface.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskSummaryPanel(taskCount: Int, accent: Color, modifier: Modifier) {
    val shape = RoundedCornerShape(CleaveRadii.panelSmall)
    Surface(
        modifier = modifier.liquidGlass(
            shape = shape,
            tint = if (taskCount > 0) accent else CleaveSurface.glassBlue,
            strength = 0.5f,
            elevation = CleaveMetrics.glassSubtleElevation
        ),
        shape = shape,
        color = Color.Transparent,
        contentColor = CleaveSurface.textPrimary,
        border = BorderStroke(1.dp, CleaveSurface.borderSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CleaveSpacing.mdPlus),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(CleaveRadii.control))
                    .background((if (taskCount > 0) accent else CleaveSurface.glassBlue).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.cleave_ic_schedule_outlined),
                    contentDescription = null,
                    tint = if (taskCount > 0) accent else CleaveSurface.glassBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.cleave_home_tasks), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CleaveSurface.textPrimary)
                Text(
                    text = if (taskCount > 0) stringResource(R.string.cleave_home_tasks_running, taskCount) else stringResource(R.string.cleave_home_tasks_idle),
                    fontSize = 11.sp,
                    color = CleaveSurface.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RecentWorldsPanel(
    recent: List<CleaveInstance>,
    selected: CleaveInstance?,
    accent: Color,
    actions: HomeActions,
    modifier: Modifier,
    lazy: Boolean
) {
    val shape = RoundedCornerShape(CleaveRadii.panel)
    Surface(
        modifier = modifier.liquidGlass(shape = shape, strength = 0.6f, elevation = CleaveMetrics.glassSubtleElevation),
        shape = shape,
        color = CleaveSurface.panel,
        contentColor = CleaveSurface.textPrimary,
        border = BorderStroke(1.dp, CleaveSurface.borderSoft)
    ) {
        Column(modifier = Modifier.padding(CleaveSpacing.mdPlus), verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HomeSectionLabel(stringResource(R.string.cleave_home_recent), stringResource(R.string.cleave_home_recent_sub), Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.cleave_home_view_all).uppercase(),
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = actions.library)
                        .padding(horizontal = CleaveSpacing.sm, vertical = CleaveSpacing.xs)
                )
            }
            if (recent.isEmpty()) {
                QuietEmptyState(R.drawable.cleave_ic_videogame_asset_outlined, stringResource(R.string.cleave_library_empty), Modifier.fillMaxWidth())
            } else if (lazy) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)) {
                    items(recent) { instance ->
                        RecentWorldRow(instance, instance == selected, accent, actions)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)) {
                    recent.forEach { instance -> RecentWorldRow(instance, instance == selected, accent, actions) }
                }
            }
        }
    }
}

@Composable
private fun RecentWorldRow(instance: CleaveInstance, isSelected: Boolean, accent: Color, actions: HomeActions) {
    val shape = RoundedCornerShape(CleaveRadii.control)
    val vanilla = stringResource(R.string.cleave_home_vanilla)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) accent.copy(alpha = 0.14f) else CleaveSurface.panelRaised.copy(alpha = 0.17f))
            .clickable(role = Role.Button) { actions.select(instance) }
            .padding(horizontal = CleaveSpacing.sm, vertical = CleaveSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
    ) {
        CleaveImageWell(image = instance.icon, fallback = R.drawable.cleave_ic_videogame_asset_outlined, size = 36.dp, radius = CleaveRadii.controlSmall)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = instance.name, color = CleaveSurface.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = instanceVersionLabel(instance.versionId, vanilla),
                color = CleaveSurface.textSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = { actions.edit(instance) }, modifier = Modifier.size(32.dp)) {
            Icon(
                painter = painterResource(R.drawable.cleave_ic_edit_outlined),
                contentDescription = stringResource(R.string.cleave_home_instance_settings),
                tint = CleaveSurface.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        IconButton(onClick = { actions.playInstance(instance) }, modifier = Modifier.size(32.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f))) {
            Icon(
                painter = painterResource(R.drawable.cleave_ic_play_arrow_filled),
                contentDescription = stringResource(R.string.cleave_home_play),
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuickToolsPanel(accent: Color, actions: HomeActions, modifier: Modifier, scrollable: Boolean) {
    val shape = RoundedCornerShape(CleaveRadii.panel)
    Surface(
        modifier = modifier.liquidGlass(shape = shape, strength = 0.6f, elevation = CleaveMetrics.glassSubtleElevation),
        shape = shape,
        color = CleaveSurface.panel,
        contentColor = CleaveSurface.textPrimary,
        border = BorderStroke(1.dp, CleaveSurface.borderSoft)
    ) {
        val padded = Modifier.padding(CleaveSpacing.mdPlus)
        Column(
            modifier = if (scrollable) padded.verticalScroll(rememberScrollState()) else padded,
            verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
        ) {
            HomeSectionLabel(stringResource(R.string.cleave_home_quick_tools), null)
            HomeToolAction(stringResource(R.string.cleave_download_title), R.drawable.cleave_ic_download, CleaveSurface.modrinthBrand, actions.downloads)
            HomeToolAction(stringResource(R.string.cleave_content_title), R.drawable.cleave_ic_extension_outlined, CleaveSurface.success, actions.content)
            HomeToolAction(stringResource(R.string.mcl_option_customcontrol), R.drawable.cleave_ic_touch_app_outlined, accent, actions.controls)
            HomeToolAction(stringResource(R.string.main_install_jar_file), R.drawable.cleave_ic_java, CleaveSurface.glassBlue, actions.runJar)
            HomeToolAction(stringResource(R.string.cleave_logs_title), R.drawable.cleave_ic_terminal_outlined, CleaveSurface.warning, actions.logs)
            HomeToolAction(stringResource(R.string.main_share_logs), R.drawable.cleave_ic_share_filled, CleaveSurface.glassViolet, actions.shareLogs)
            HomeToolAction(stringResource(R.string.cleave_files_title), R.drawable.cleave_ic_folder_filled, CleaveSurface.glassBlue, actions.fileManager)
            HomeToolAction(stringResource(R.string.mcl_button_open_directory), R.drawable.cleave_ic_folder_outlined, CleaveSurface.glassRose, actions.files)
            HomeToolAction(stringResource(R.string.preference_remap_controller_title), R.drawable.cleave_ic_sports_esports_outlined, CleaveSurface.auroraCyan, actions.gamepad)
        }
    }
}

@Composable
private fun HomeToolAction(label: String, iconRes: Int, accent: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CleaveRadii.control))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = CleaveSpacing.xs, vertical = CleaveSpacing.smCompact),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(CleaveRadii.compact))
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        }
        Text(text = label, color = CleaveSurface.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SameVersionNudgeStrip(
    selected: CleaveInstance?,
    accent: Color,
    onOpenSocials: () -> Unit
) {
    val myVer = remember(selected?.versionId) {
        net.kdt.pojavlaunch.cleave.social.SocialBackend.extractMcVersion(selected?.versionId)
    } ?: return
    val matching = net.kdt.pojavlaunch.cleave.social.SocialBackend.friends.filter { f ->
        !f.pendingIncoming && !f.pendingOutgoing && f.online && f.mcVersion.equals(myVer, ignoreCase = true)
    }
    if (matching.isEmpty()) return
    val names = matching.take(2).joinToString(", ") { it.effectiveName }
    val shape = RoundedCornerShape(CleaveRadii.control)
    Surface(
        shape = shape,
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(CleaveSpacing.hairline, accent.copy(alpha = 0.48f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpenSocials)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.xsPlus),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
        ) {
            Icon(
                painter = painterResource(R.drawable.cleave_ic_people),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "$names ${if (matching.size == 1) "is" else "are"} also on $myVer — tap to party up or join!",
                color = CleaveSurface.textPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(R.drawable.cleave_ic_chevron_right),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}


/**
 * The dashboard's own switch between the two launcher skins.
 *
 * It sits at the very top because that is where a player looks for "make this look like the other
 * one", and it is a pair of pills with a sliding highlight rather than a checkbox: pressing a name
 * and watching the page move into its new position is the whole point of the choice, and a
 * confirmation dialog or a restart would take that away.
 */
@Composable
private fun CleaveUiStyleSwitch(modifier: Modifier = Modifier) {
    val selected = CleavePreferences.uiStyle
    val accent = CleaveThemeManager.currentAccent()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(CleaveSurface.panel.copy(alpha = 0.45f))
            .border(CleaveSpacing.hairline, CleaveSurface.borderSoft, RoundedCornerShape(999.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CleaveLauncherUiStyle.values().forEach { style ->
            val active = style == selected
            // Animated on alpha rather than on the color itself: this Compose build has no colour
            // animator here, and a fading fill reads the same while sliding into place.
            val pillAlpha by animateFloatAsState(
                if (active) 1f else 0f,
                animationSpec = tween(260, easing = FastOutSlowInEasing),
                label = "uiStylePill"
            )
            Text(
                text = style.label,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent.copy(alpha = 0.28f * pillAlpha))
                    .clickable { CleavePreferences.saveUiStyle(style) }
                    .padding(vertical = 7.dp),
                color = if (active) CleaveSurface.textPrimary else CleaveSurface.textSecondary,
                fontSize = 11.sp,
                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
