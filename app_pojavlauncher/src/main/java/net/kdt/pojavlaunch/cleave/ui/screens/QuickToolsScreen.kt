package net.kdt.pojavlaunch.cleave.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.CleaveInstallQueue
import net.kdt.pojavlaunch.cleave.bridge.CleaveInstance
import net.kdt.pojavlaunch.cleave.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavLaunchBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavNavigator
import net.kdt.pojavlaunch.cleave.bridge.PojavTaskBridge
import net.kdt.pojavlaunch.cleave.components.CleaveImageWell
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.ui.CleaveNav
import net.kdt.pojavlaunch.cleave.ui.LauncherSection
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension

/** Home's former quick-action, task and recent-instance cards, collected on one destination. */
@Composable
fun QuickToolsScreen(activity: FragmentActivity, navigate: (LauncherSection) -> Unit) {
    val accent = CleaveThemeManager.currentAccent()
    val instances = PojavInstanceBridge.instances
    val selected = PojavInstanceBridge.selected
    val taskCount = PojavTaskBridge.taskCount
    val jarPicker = rememberLauncherForActivityResult(OpenDocumentWithExtension("jar")) { uri ->
        if (uri != null) PojavNavigator.runJar(activity, uri)
    }

    CleavePage(title = "Quick Tools", subtitle = "Launcher shortcuts, recent instances and background work") {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(CleaveRadii.card),
                color = CleaveSurface.panelRaised,
                border = BorderStroke(CleaveSpacing.hairline, accent.copy(alpha = 0.34f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(CleaveSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                ) {
                    Icon(
                        painter = painterResource(if (CleaveInstallQueue.visible) R.drawable.cleave_ic_download else R.drawable.cleave_ic_schedule_outlined),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(25.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (taskCount == 0 && !CleaveInstallQueue.visible) "All caught up" else "Work in progress",
                            color = CleaveSurface.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            when {
                                CleaveInstallQueue.visible -> "An install is running; its progress panel stays available above the launcher."
                                taskCount > 0 -> "$taskCount background task(s) are active."
                                else -> "Downloads and installs continue while you move around the launcher."
                            },
                            color = CleaveSurface.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                    if (CleaveInstallQueue.visible) {
                        Text("OPEN", color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp,
                            modifier = Modifier.clickable { CleaveInstallQueue.openPanel() }.padding(6.dp))
                    }
                }
            }

            Text("QUICK ACTIONS", color = CleaveSurface.textSecondary, fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
            ToolRow("Instance library", "Manage, create and edit Minecraft instances", R.drawable.cleave_ic_dashboard_filled) {
                navigate(LauncherSection.LIBRARY)
            }
            ToolRow("Open game folder", "Browse the selected instance's files", R.drawable.cleave_ic_folder_outlined) {
                PojavInstanceBridge.openGameDirectory(activity)
            }
            ToolRow("File manager", "Browse launcher and Minecraft files", R.drawable.cleave_ic_folder_filled) {
                navigate(LauncherSection.FILES)
            }
            ToolRow("Run a JAR", "Choose a Java archive to launch", R.drawable.cleave_ic_java) {
                if (PojavNavigator.canRunJar(activity)) jarPicker.launch(null)
                else Toast.makeText(activity, "No active instance or Java runtime is available", Toast.LENGTH_LONG).show()
            }
            ToolRow("Control layout", "Edit touch controls and on-screen buttons", R.drawable.cleave_ic_keyboard_outlined) {
                PojavNavigator.openCustomControls(activity)
            }
            ToolRow("Gamepad mapper", "Configure controller buttons", R.drawable.cleave_ic_sports_esports_outlined) {
                PojavNavigator.openGamepadMapper(activity)
            }
            ToolRow("Logs", "Review launcher logs", R.drawable.cleave_ic_description_outlined) {
                navigate(LauncherSection.LOGS)
            }
            ToolRow("Share launcher logs", "Send a diagnostic log bundle", R.drawable.cleave_ic_share_filled) {
                PojavNavigator.shareLogs(activity)
            }
            ToolRow("CLEAVE account", "Sign in to your CLEAVE community account", R.drawable.cleave_ic_person_outlined) {
                navigate(LauncherSection.CLEAVE_ACCOUNT)
            }
            ToolRow("Downloads & discover", "Find modpacks, versions and content", R.drawable.cleave_ic_cloud_download) {
                navigate(LauncherSection.DISCOVER)
            }
            ToolRow("Settings", "Renderer, controls and launcher preferences", R.drawable.cleave_ic_settings_filled) {
                navigate(LauncherSection.SETTINGS)
            }
            ToolRow("Social profile", "Friends, messages, parties and your CLEAVE profile", R.drawable.cleave_ic_person_outlined) {
                SocialsNavigation.requestMyProfile()
                navigate(LauncherSection.SOCIALS)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = CleaveSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RECENT INSTANCES", color = CleaveSurface.textSecondary, fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                Text("VIEW ALL", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navigate(LauncherSection.INSTANCE_SELECTOR) }.padding(6.dp))
            }
            if (instances.isEmpty()) {
                Text("No instances yet. Create one from the library to get started.",
                    color = CleaveSurface.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
            } else {
                instances.asReversed().take(6).forEach { instance ->
                    RecentInstanceRow(
                        instance = instance,
                        selected = selected?.id == instance.id,
                        accent = accent,
                        onSelect = {
                            PojavInstanceBridge.select(instance)
                            CleaveNav.finishTo(LauncherSection.HOME)
                        },
                        onPlay = {
                            PojavInstanceBridge.select(instance)
                            PojavLaunchBridge.play(activity)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolRow(title: String, description: String, icon: Int, onClick: () -> Unit) {
    val accent = CleaveThemeManager.currentAccent()
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(CleaveRadii.cardSmall),
        color = CleaveSurface.panel.copy(alpha = 0.82f),
        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(CleaveSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(title, color = CleaveSurface.textPrimary, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium)
                Text(description, color = CleaveSurface.textSecondary, fontSize = 10.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(painterResource(R.drawable.cleave_ic_chevron_right), contentDescription = null,
                tint = CleaveSurface.textSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun RecentInstanceRow(
    instance: CleaveInstance,
    selected: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onSelect: () -> Unit,
    onPlay: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CleaveRadii.cardSmall),
        color = if (selected) accent.copy(alpha = 0.12f) else CleaveSurface.panel.copy(alpha = 0.72f),
        border = BorderStroke(CleaveSpacing.hairline, if (selected) accent.copy(alpha = 0.45f) else CleaveSurface.borderSoft)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = CleaveSpacing.sm, vertical = CleaveSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CleaveImageWell(instance.icon, R.drawable.cleave_ic_videogame_asset_outlined, 32.dp, radius = CleaveRadii.tiny)
            Spacer(Modifier.width(CleaveSpacing.sm))
            Column(Modifier.weight(1f).clickable(onClick = onSelect).padding(vertical = 4.dp)) {
                Text(instance.name, color = CleaveSurface.textPrimary, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(instance.versionId, color = CleaveSurface.textSecondary, fontSize = 10.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("PLAY", color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp,
                modifier = Modifier.clickable(onClick = onPlay).padding(8.dp))
        }
    }
}
