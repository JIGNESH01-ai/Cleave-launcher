/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Ported from mirai-launcher- (Zalith Launcher 2 fork):
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/main/MainScreen.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/LauncherScreen.kt
 * The Navigation3 back stack is replaced by a section state + AnimatedContent; nested Pojav screens
 * (installers, login, gamepad remapper) are pushed on the fragment back stack.
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

package net.kdt.pojavlaunch.cleave.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.cleave.bridge.CleavePreferences
import net.kdt.pojavlaunch.cleave.bridge.CleaveTransitionType
import net.kdt.pojavlaunch.cleave.bridge.CleaveWindowInsets
import net.kdt.pojavlaunch.cleave.bridge.PojavAccountBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavInstanceEditorBridge
import net.kdt.pojavlaunch.cleave.components.CleaveBackground
import net.kdt.pojavlaunch.cleave.discord.CleaveDiscordPresence
import net.kdt.pojavlaunch.cleave.social.SocialBackend
import net.kdt.pojavlaunch.cleave.festivals.FestivalEffectsHost
import net.kdt.pojavlaunch.cleave.festivals.FestivalTitleText
import net.kdt.pojavlaunch.cleave.festivals.rememberTodayFestivals
import net.kdt.pojavlaunch.cleave.guide.CleaveGuideOverlay
import net.kdt.pojavlaunch.cleave.guide.CleaveGuideState
import net.kdt.pojavlaunch.cleave.guide.GuideRegistry
import net.kdt.pojavlaunch.cleave.guide.LocalGuideRegistry
import net.kdt.pojavlaunch.cleave.guide.rememberGuideSteps
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.cleave.ui.screens.AboutScreen
import net.kdt.pojavlaunch.cleave.ui.screens.AccountsScreen
import net.kdt.pojavlaunch.cleave.ui.screens.CleaveAccountSetupScreen
import net.kdt.pojavlaunch.cleave.ui.screens.AddAccountScreen
import net.kdt.pojavlaunch.cleave.ui.screens.CleaveContribution
import net.kdt.pojavlaunch.cleave.ui.screens.ContributionDetailScreen
import net.kdt.pojavlaunch.cleave.ui.screens.ContributionScreen
import net.kdt.pojavlaunch.cleave.ui.screens.ClientHubScreen
import net.kdt.pojavlaunch.cleave.ui.screens.ClientOwnerPage
import net.kdt.pojavlaunch.cleave.ui.screens.CreateInstanceScreen
import net.kdt.pojavlaunch.cleave.ui.screens.DiscoverScreen
import net.kdt.pojavlaunch.cleave.ui.screens.FileManagerScreen
import net.kdt.pojavlaunch.cleave.ui.screens.HomeScreen
import net.kdt.pojavlaunch.cleave.ui.screens.InstanceEditorScreen
import net.kdt.pojavlaunch.cleave.ui.screens.InstanceSelectorScreen
import net.kdt.pojavlaunch.cleave.ui.screens.QuickToolsScreen
import net.kdt.pojavlaunch.cleave.ui.screens.LibraryScreen
import net.kdt.pojavlaunch.cleave.bridge.CleaveInstallQueue
import net.kdt.pojavlaunch.cleave.ui.screens.CleaveInstallPanel
import net.kdt.pojavlaunch.cleave.ui.screens.CleaveInstallPill
import net.kdt.pojavlaunch.cleave.ui.screens.LogScreen
import net.kdt.pojavlaunch.cleave.ui.screens.OfflineAccountScreen
import net.kdt.pojavlaunch.cleave.ui.screens.SettingsScreen
import net.kdt.pojavlaunch.cleave.ui.screens.ServersScreen
import net.kdt.pojavlaunch.cleave.ui.screens.SocialsScreen
import net.kdt.pojavlaunch.cleave.ui.screens.SocialsNavigation
import net.kdt.pojavlaunch.cleave.ui.screens.ModpacksScreen
import net.kdt.pojavlaunch.BuildConfig
import net.kdt.pojavlaunch.cleave.bridge.PojavNavigator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CleaveLauncherScreen(activity: FragmentActivity) {
    CleaveNav.init()
    net.kdt.pojavlaunch.cleave.ui.screens.FirstRunNotificationPopupHost(activity)
    net.kdt.pojavlaunch.cleave.ui.screens.LauncherUpdateNoticeHost(activity)
    val section = CleaveNav.section
    var clientOwnerPage by rememberSaveable { mutableStateOf(ClientOwnerPage.OWNERS) }
    val navigate: (LauncherSection) -> Unit = { target ->
        when (target) {
            LauncherSection.SETTINGS -> CleaveNav.navigate(LauncherSection.SETTINGS)
            LauncherSection.ABOUT -> {
                CleaveNav.settingsTab = CleaveNav.TAB_ABOUT
                CleaveNav.navigate(LauncherSection.SETTINGS)
            }
            // Opening the rail entry always lands on the team list, never on someone's page.
            LauncherSection.CONTRIBUTION -> {
                CleaveContribution.openedId = null
                CleaveNav.navigate(LauncherSection.CONTRIBUTION)
            }
            LauncherSection.CLEAVE_CLIENT -> {
                clientOwnerPage = ClientOwnerPage.OWNERS
                CleaveNav.navigate(LauncherSection.CLEAVE_CLIENT)
            }
            LauncherSection.DOWNLOADS -> CleaveNav.navigate(LauncherSection.DISCOVER)
            LauncherSection.CONTENT -> CleaveNav.navigate(LauncherSection.INSTANCE_EDITOR)
            else -> CleaveNav.navigate(target)
        }
    }
    val duration = CleavePreferences.transitionMillis()
    val transitionType = CleavePreferences.swapAnimateType
    val extent = CleavePreferences.extentScale()
    val festivals = rememberTodayFestivals()
    // The first-run tour: components register where they are, the overlay highlights them.
    // Wait until the first-time Notification ON/OFF popup has been answered so they never overlap.
    val guideRegistry = remember { GuideRegistry() }
    val guideSteps = rememberGuideSteps()
    val notificationPromptAnswered = CleavePreferences.notificationPromptShown
    LaunchedEffect(notificationPromptAnswered) {
        if (!notificationPromptAnswered) return@LaunchedEffect
        CleaveGuideState.startOnFirstRun()
        // The steps highlight things on the home page, so the tour brings you there rather than
        // starting on whichever section the launcher was left on.
        if (CleaveGuideState.running && CleaveNav.section != LauncherSection.HOME) {
            CleaveNav.navigate(LauncherSection.HOME)
        }
    }

    // Discord Rich Presence: the launcher is on screen, and it says which page you are on.
    // Lifecycle clear-after-1-minute on leaving the app is driven by LauncherActivity.onStart/onStop.
    LaunchedEffect(Unit) {
        CleaveDiscordPresence.attach(activity)
    }
    val activePartySnapshot = SocialBackend.parties
        .firstOrNull { !it.isExpired() }
        ?.let { "${it.id}:${it.memberCount}:${it.maxMembers}" }
    LaunchedEffect(section, activePartySnapshot) {
        CleaveDiscordPresence.inLauncher(section.name)
    }

    BackHandler(enabled = CleaveNav.canGoBack) { CleaveNav.back() }

    CompositionLocalProvider(LocalGuideRegistry provides guideRegistry) {
    CleaveBackground {
      FestivalEffectsHost(festivals = festivals, modifier = Modifier.fillMaxSize()) {
        // The wallpaper and the festival effects run to the edges of the display; the shell
        // itself stays clear of the camera cutout and of the system bars.
        Box(modifier = Modifier.fillMaxSize().padding(CleaveWindowInsets.padding())) {
            Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Socials has its own navigation; every other destination is reached from this
                // single left sidebar. There is no second rail - the right edge of the screen
                // belongs to page content only.
                if (section != LauncherSection.SOCIALS) {
                    CleaveNavigationRail(
                        selectedSection = section,
                        onNavigate = navigate,
                        onCreateInstance = { CleaveNav.navigate(LauncherSection.CREATE_INSTANCE) },
                        onOpenProfile = {
                            SocialsNavigation.requestMyProfile()
                            navigate(LauncherSection.SOCIALS)
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    AnimatedContent(
                        targetState = section,
                        transitionSpec = { sectionTransition(transitionType, duration, extent) },
                        label = "cleaveSection"
                    ) { target ->
                        when (target) {
                            LauncherSection.OTHER_RELEASES -> net.kdt.pojavlaunch.cleave.ui.screens.OtherReleasesScreen(activity, navigate)
                            LauncherSection.HOME -> HomeScreen(activity, navigate)
                            LauncherSection.QUICK_TOOLS -> QuickToolsScreen(activity, navigate)
                            LauncherSection.INSTANCE_SELECTOR -> InstanceSelectorScreen(activity, navigate)
                            LauncherSection.CLEAVE_CLIENT -> ClientHubScreen(activity, clientOwnerPage) { clientOwnerPage = it }
                            LauncherSection.LIBRARY -> LibraryScreen(activity)
                            LauncherSection.SERVERS -> ServersScreen(activity)
                            LauncherSection.MODPACKS -> ModpacksScreen(navigate)
                            LauncherSection.SOCIALS -> SocialsScreen(activity) { CleaveNav.finishTo(LauncherSection.HOME) }
                            LauncherSection.DISCOVER, LauncherSection.DOWNLOADS -> DiscoverScreen(activity, navigate)
                            LauncherSection.FILES -> FileManagerScreen(activity)
                            LauncherSection.LOGS -> LogScreen(activity)
                            LauncherSection.SETTINGS -> SettingsScreen(
                                activity = activity,
                                tabKey = CleaveNav.settingsTab,
                                onTabChanged = { CleaveNav.settingsTab = it }
                            )
                            LauncherSection.CONTRIBUTION -> ContributionScreen(onOpen = { member ->
                                CleaveContribution.openedId = member.id
                                CleaveNav.navigate(LauncherSection.CONTRIBUTION_DETAIL)
                            })
                            LauncherSection.CONTRIBUTION_DETAIL -> {
                                val member = CleaveContribution.byId(CleaveContribution.openedId)
                                if (member == null) {
                                    ContributionScreen(onOpen = { opened -> CleaveContribution.openedId = opened.id })
                                } else {
                                    ContributionDetailScreen(member = member, onBack = { CleaveNav.back() })
                                }
                            }
                            LauncherSection.ACCOUNTS -> AccountsScreen(activity)
                            LauncherSection.CLEAVE_ACCOUNT -> CleaveAccountSetupScreen(activity) { CleaveNav.back() }
                            LauncherSection.ABOUT -> AboutScreen(activity)
                            LauncherSection.CREATE_INSTANCE -> CreateInstanceScreen(activity) {
                                CleaveNav.finishTo(LauncherSection.LIBRARY)
                            }
                            LauncherSection.INSTANCE_EDITOR, LauncherSection.CONTENT -> InstanceEditorScreen(activity) {
                                PojavInstanceEditorBridge.close()
                                CleaveNav.finishTo(LauncherSection.LIBRARY)
                            }
                            LauncherSection.ADD_ACCOUNT -> AddAccountScreen(
                                activity = activity,
                                onClose = { CleaveNav.finishTo(LauncherSection.ACCOUNTS) },
                                onOffline = { CleaveNav.navigate(LauncherSection.OFFLINE_ACCOUNT) }
                            )
                            LauncherSection.OFFLINE_ACCOUNT -> OfflineAccountScreen(
                                activity = activity,
                                onClose = { CleaveNav.navigate(LauncherSection.ADD_ACCOUNT) },
                                onCreated = { CleaveNav.finishTo(LauncherSection.ACCOUNTS) }
                            )
                        }
                    }

                    // Login progress bar, the CLEAVE replacement for the bar drawn under Pojav's AccountSpinner.
                    val progress = PojavAccountBridge.loginProgress
                    if (progress < 1f) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(horizontal = CleaveSpacing.lg),
                            color = if (PojavAccountBridge.loginError) CleaveSurface.danger else CleaveThemeManager.currentAccent(),
                            trackColor = Color.Transparent
                        )
                    }
                }
            }
            CleaveFooterBar(activity)
            }

            FestivalTitleText(
                festivals = festivals,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = CleaveSpacing.xs, end = CleaveSpacing.lg)
            )

            // Live join toast ("Marie started 1.21.4 · Join?") from Supabase Realtime presence.
            net.kdt.pojavlaunch.cleave.social.SocialBackend.activeJoinToast?.let { toast ->
                net.kdt.pojavlaunch.cleave.ui.screens.SocialJoinToastBanner(
                    toast = toast,
                    onJoin = {
                        net.kdt.pojavlaunch.cleave.social.SocialBackend.dismissJoinToast()
                        val party = toast.partyId?.let { pid ->
                            net.kdt.pojavlaunch.cleave.social.SocialBackend.parties.firstOrNull { it.id == pid }
                        }
                        net.kdt.pojavlaunch.cleave.bridge.PojavLaunchBridge.joinSocialSession(
                            activity = activity,
                            mcVersion = party?.mcVersion ?: toast.mcVersion,
                            loaderName = party?.loader ?: toast.loader,
                            instanceHint = party?.instanceName,
                            modpackSource = party?.modpackSource,
                            modpackId = party?.modpackId,
                            modpackIconUrl = party?.modpackIconUrl,
                            serverAddress = party?.serverAddress ?: toast.serverAddress,
                            onNavigate = navigate
                        )
                    },
                    onDismiss = { net.kdt.pojavlaunch.cleave.social.SocialBackend.dismissJoinToast() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = CleaveSpacing.md)
                )
            }

            // Installs outlive the page that started them, so their two surfaces live here: the
            // panel with the progress bars, and the pill it collapses into when minimized.
            if (CleaveInstallQueue.visible) {
                if (CleaveInstallQueue.panelOpen) {
                    CleaveInstallPanel(onMinimize = { CleaveInstallQueue.minimizePanel() })
                } else {
                    CleaveInstallPill(
                        onExpand = { CleaveInstallQueue.openPanel() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(CleaveSpacing.lg)
                    )
                }
            }

            // Drawn last so it sits above the rail, the pages and the install panel. Only on the
            // home page: the steps highlight things that live there.
            if (CleaveGuideState.running && section == LauncherSection.HOME) {
                key(CleaveGuideState.runId) {
                    CleaveGuideOverlay(
                        steps = guideSteps,
                        onFinished = { CleaveGuideState.finish() }
                    )
                }
            }
        }
      }
    }
    }
}

/**
 * Section transition for the CLEAVE "launcherSwapAnimateType" setting
 * (mirai utils/animation/TransitionAnimationType.kt, ui/screens/_Navigation.kt):
 * CLOSE snaps, JELLY_BOUNCE scales in on a low-stiffness spring, BOUNCE rises on a bouncy spring,
 * SLICE_IN slides in from the side. "launcherAnimateExtent" scales the travel distance.
 */
private fun AnimatedContentTransitionScope<LauncherSection>.sectionTransition(
    type: CleaveTransitionType,
    duration: Int,
    extent: Float
): ContentTransform {
    if (duration == 0 || type == CleaveTransitionType.CLOSE) {
        return fadeIn(snap()) togetherWith fadeOut(snap())
    }
    val exit = fadeOut(tween((duration * 0.75f).toInt().coerceAtLeast(80), easing = FastOutSlowInEasing))
    val forward = targetState.ordinal >= initialState.ordinal
    return when (type) {
        CleaveTransitionType.JELLY_BOUNCE -> (fadeIn(tween(duration)) +
            scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                initialScale = 1f - 0.08f * extent)) togetherWith exit
        CleaveTransitionType.BOUNCE -> (fadeIn(tween(duration)) +
            slideInVertically(spring<IntOffset>(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow)) {
                (it / 10 * extent).toInt()
            }) togetherWith exit
        CleaveTransitionType.SLICE_IN -> (fadeIn(tween(duration)) +
            slideInHorizontally(tween(duration, easing = FastOutSlowInEasing)) {
                val distance = (it / 6 * extent).toInt()
                if (forward) distance else -distance
            }) togetherWith exit
        CleaveTransitionType.CLOSE -> fadeIn(snap()) togetherWith fadeOut(snap())
    }
}

/**
 * The launcher-wide footer strip: a quiet brand line and the official links.
 * Deliberately small (one 24dp line, muted colors) so it never competes with the dashboard;
 * the About screen carries the full creator and credits presentation.
 */
@Composable
private fun CleaveFooterBar(activity: FragmentActivity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(Color(0xFF0A0A0C))
            .padding(horizontal = CleaveSpacing.mdPlus),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "CLEAVE Launcher · v${BuildConfig.VERSION_NAME}",
            color = CleaveSurface.textMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Made by JIGNESHGAMING",
            color = CleaveSurface.textMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(CleaveSpacing.md))
        FooterLink("YouTube") { PojavNavigator.openYouTube(activity) }
        Text("•", color = CleaveSurface.textMuted, fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = CleaveSpacing.xs))
        FooterLink("Instagram") { PojavNavigator.openInstagram(activity) }
        Text("•", color = CleaveSurface.textMuted, fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = CleaveSpacing.xs))
        FooterLink("Discord") { PojavNavigator.openDiscord(activity) }
    }
}

@Composable
private fun FooterLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = CleaveThemeManager.currentAccent(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable(onClickLabel = label, onClick = onClick)
    )
}
