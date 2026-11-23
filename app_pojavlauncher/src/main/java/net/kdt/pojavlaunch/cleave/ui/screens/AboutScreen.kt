/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * About screen: brand, creator and official links for CLEAVE Launcher.
 * GPL-3.0
 */

package net.kdt.pojavlaunch.cleave.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.BuildConfig
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.DeviceReport
import net.kdt.pojavlaunch.cleave.bridge.PojavDeviceBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavNavigator
import net.kdt.pojavlaunch.cleave.components.MetaPill
import net.kdt.pojavlaunch.cleave.guide.CleaveGuideState
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.cleave.components.CleaveGhostButton
import net.kdt.pojavlaunch.cleave.components.GlassPanel
import net.kdt.pojavlaunch.cleave.components.SectionLabel
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface

private const val SOURCE_URL = "https://github.com/JIGNESH01-ai/Cleave-launcher"
private const val POJAV_URL = "https://github.com/PojavLauncherTeam/PojavLauncher"
private const val LICENSE_URL = "https://www.gnu.org/licenses/gpl-3.0.html"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(activity: FragmentActivity) {
    var showUpdateDialog by remember { mutableStateOf(false) }
    if (showUpdateDialog) LauncherUpdateDialog(activity) { showUpdateDialog = false }

    CleavePage(
        title = stringResource(R.string.cleave_about_title),
        subtitle = stringResource(R.string.cleave_about_subtitle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
        ) {
            // Brand: official CL logo on black, tagline, version.
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.lg)) {
                    Image(
                        painter = painterResource(R.drawable.cleave_logo),
                        contentDescription = "CLEAVE Launcher logo",
                        modifier = Modifier.size(96.dp)
                    )
                    Column {
                        Text(
                            text = "CLEAVE Launcher",
                            color = CleaveSurface.textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Minecraft Java, Anywhere.",
                            color = CleaveThemeManager.currentAccent(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.cleave_about_version, BuildConfig.VERSION_NAME),
                            color = CleaveSurface.textSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Minecraft: Java Edition for Android · CLEAVE is free software (GPL-3.0)",
                            color = CleaveSurface.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            // Creator + official links.
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                SectionLabel(title = "Made by JIGNESHGAMING")
                Text(
                    text = "Creator and maintainer of CLEAVE Launcher. Find the official channel below.",
                    color = CleaveSurface.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                ) {
                    CleaveGhostButton("YouTube", R.drawable.cleave_ic_play_arrow_filled, { PojavNavigator.openYouTube(activity) })
                    CleaveGhostButton("Instagram", R.drawable.cleave_ic_person_outlined, { PojavNavigator.openInstagram(activity) })
                    CleaveGhostButton("Discord", R.drawable.ic_discord, { PojavNavigator.openDiscord(activity) })
                    CleaveGhostButton("Project Link", R.drawable.cleave_ic_github, { PojavNavigator.openUrl(activity, SOURCE_URL) })
                    CleaveGhostButton("Check for Updates", R.drawable.cleave_ic_update, { showUpdateDialog = true })
                }
            }
            // The tour only runs once by itself, so it has to be reachable afterwards.
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                SectionLabel(title = stringResource(R.string.cleave_guide_replay))
                Text(
                    text = stringResource(R.string.cleave_guide_replay_body),
                    color = CleaveSurface.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                CleaveGhostButton(
                    stringResource(R.string.cleave_guide_replay),
                    R.drawable.cleave_ic_info_outlined,
                    { CleaveGuideState.replay() }
                )
            }
            DeviceReportPanel(activity)
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                SectionLabel(title = stringResource(R.string.cleave_about_license))
                Text(
                    text = "CLEAVE Launcher is free software under GPL-3.0. " +
                        "Launcher core from PojavLauncher / MojoLauncher (PojavLauncherTeam and contributors). " +
                        "Interface ported from Zalith Launcher 2 (MovTery and contributors). " +
                        "Minecraft is a trademark of Mojang Studios. CLEAVE is not affiliated with Mojang or Microsoft.",
                    color = CleaveSurface.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                ) {
                    CleaveGhostButton("GPL-3.0", R.drawable.cleave_ic_copyright_outlined, { PojavNavigator.openUrl(activity, LICENSE_URL) })
                    CleaveGhostButton("PojavLauncher", R.drawable.cleave_ic_terminal_outlined, { PojavNavigator.openUrl(activity, POJAV_URL) })
                    CleaveGhostButton("Source Code", R.drawable.cleave_ic_github, { PojavNavigator.openUrl(activity, SOURCE_URL) })
                }
            }
        }
    }
}

/**
 * Device and graphics compatibility: SoC, ABIs, RAM, GLES driver, Vulkan version/level and the
 * renderers CLEAVE allows on this device.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeviceReportPanel(activity: FragmentActivity) {
    val report by produceState<DeviceReport?>(initialValue = null) { value = PojavDeviceBridge.report(activity) }
    val accent = CleaveThemeManager.currentAccent()
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(title = stringResource(R.string.cleave_device_title))
        val r = report
        if (r == null) {
            Text(stringResource(R.string.cleave_device_probing), color = CleaveSurface.textSecondary, fontSize = 12.sp)
            return@GlassPanel
        }
        DeviceLine(stringResource(R.string.cleave_device_model), "${r.manufacturer} ${r.model}")
        DeviceLine(stringResource(R.string.cleave_device_soc), r.soc)
        DeviceLine(stringResource(R.string.cleave_device_android), "${r.androidRelease} (API ${r.sdk})")
        DeviceLine(
            stringResource(R.string.cleave_device_abi),
            r.abis.joinToString(", ") + " · " + r.architecture +
                if (r.is32BitOnly) " · " + stringResource(R.string.cleave_device_32bit) else ""
        )
        DeviceLine(stringResource(R.string.cleave_device_ram), stringResource(R.string.cleave_device_ram_value, r.totalRamMb, r.freeRamMb))
        DeviceLine(stringResource(R.string.cleave_device_gles), "${r.glVendor} ${r.glRenderer} · GLES ${r.glesMajor}")
        val vk = r.vulkan
        if (vk == null) {
            DeviceLine(stringResource(R.string.cleave_device_vulkan), stringResource(R.string.cleave_device_vulkan_none), CleaveSurface.warning)
        } else {
            val levelText = if (vk.level >= 0) " · " + stringResource(R.string.cleave_device_vulkan_level, vk.level) else ""
            val deqpText = vk.deqpDate?.let { " · dEQP $it" } ?: ""
            DeviceLine(stringResource(R.string.cleave_device_vulkan), vk.versionString + levelText + deqpText)
            DeviceLine(
                stringResource(R.string.cleave_device_vulkan_baseline),
                stringResource(if (vk.meetsBaseline) R.string.cleave_device_vulkan_ok else R.string.cleave_device_vulkan_low),
                if (vk.meetsBaseline) CleaveSurface.success else CleaveSurface.warning
            )
        }
        DeviceLine(stringResource(R.string.cleave_device_current_renderer), r.currentRenderer, accent)
        Text(stringResource(R.string.cleave_device_renderers), color = CleaveSurface.textSecondary, fontSize = 12.sp)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
        ) {
            r.compatibleRenderers.forEach { MetaPill(it) }
        }
    }
}

@Composable
private fun DeviceLine(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = CleaveSurface.textPrimary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md)) {
        Text(label, color = CleaveSurface.textMuted, fontSize = 12.sp, modifier = Modifier.weight(0.35f))
        Text(value.ifBlank { "—" }, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.65f))
    }
}
