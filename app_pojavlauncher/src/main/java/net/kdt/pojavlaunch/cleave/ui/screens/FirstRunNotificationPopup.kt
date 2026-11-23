package net.kdt.pojavlaunch.cleave.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.CleavePreferences
import net.kdt.pojavlaunch.cleave.components.liquidGlass
import net.kdt.pojavlaunch.cleave.social.SocialBackend
import net.kdt.pojavlaunch.cleave.theme.CleaveMetrics
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.cleave.theme.ThemeMotion
import net.kdt.pojavlaunch.cleave.theme.accentGlow
import net.kdt.pojavlaunch.cleave.theme.accentRing
import androidx.compose.ui.res.painterResource

/**
 * First-time app open Notification ON/OFF popup window.
 *
 * Shown once the very first time a user opens CLEAVE Launcher so they can immediately choose
 * whether background message & launcher notifications should be ON or OFF.
 */
@Composable
internal fun FirstRunNotificationPopupHost(activity: FragmentActivity) {
    if (CleavePreferences.notificationPromptShown) return

    val accent = CleaveThemeManager.currentAccent()
    val motion = ThemeMotion.rememberEnabled()
    val liveAccent = ThemeMotion.rememberPulse(accent, active = true, enabled = motion)
    var selectedOn by remember { mutableStateOf(true) }

    val applyChoice: (Boolean) -> Unit = { enableNotifications ->
        SocialBackend.attach(activity.applicationContext)
        SocialBackend.toggleBackgroundNotifications(enableNotifications)
        CleavePreferences.saveNotificationPromptShown(true)
        if (activity is LauncherActivity) {
            if (enableNotifications) {
                activity.markNotificationPromptHandled()
                if (Build.VERSION.SDK_INT >= 33 &&
                    !activity.checkForPermission(33, Manifest.permission.POST_NOTIFICATIONS)
                ) {
                    activity.askForPermission(33, Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                activity.markNotificationPromptHandled()
            }
        }
    }

    val panelShape = RoundedCornerShape(CleaveRadii.panel)
    Dialog(
        onDismissRequest = { applyChoice(selectedOn) },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = panelShape,
            color = CleaveSurface.panel,
            contentColor = CleaveSurface.textPrimary,
            border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft),
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.90f)
                .liquidGlass(
                    shape = panelShape,
                    tint = CleaveSurface.panelGlassTint,
                    strength = 1f,
                    elevation = CleaveMetrics.glassFloatingElevation
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header row with glowing bell icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.18f))
                            .border(BorderStroke(CleaveSpacing.hairline, accent.copy(alpha = 0.55f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_bell),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Notifications?",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CleaveSurface.textPrimary
                        )
                        Text(
                            text = "Choose how CLEAVE Launcher notifies you",
                            fontSize = 12.sp,
                            color = CleaveSurface.textSecondary
                        )
                    }
                }

                Text(
                    text = "Turn notifications ON to receive instant alerts with inline Direct Reply when friends or party members message you while the app is closed, plus background download status.",
                    fontSize = 12.5.sp,
                    lineHeight = 17.5.sp,
                    color = CleaveSurface.textSecondary
                )

                // Interactive ON / OFF toggle card
                val cardShape = RoundedCornerShape(CleaveRadii.cardSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(if (selectedOn) accent.copy(alpha = 0.13f) else CleaveSurface.panelTrack)
                        .border(
                            BorderStroke(
                                CleaveSpacing.hairline,
                                if (selectedOn) accent.copy(alpha = 0.65f) else CleaveSurface.borderSoft
                            ),
                            cardShape
                        )
                        .clickable(role = Role.Switch) { selectedOn = !selectedOn }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedOn) "Notifications: ON" else "Notifications: OFF",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedOn) accent else CleaveSurface.textPrimary
                        )
                        Text(
                            text = if (selectedOn) {
                                "Direct & party message alerts + inline reply enabled"
                            } else {
                                "Silent mode — no background message alerts"
                            },
                            fontSize = 11.5.sp,
                            color = CleaveSurface.textMuted
                        )
                    }
                    Switch(
                        checked = selectedOn,
                        onCheckedChange = { selectedOn = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CleaveSurface.canvas,
                            checkedTrackColor = accent,
                            uncheckedThumbColor = CleaveSurface.textMuted,
                            uncheckedTrackColor = CleaveSurface.panelRaised
                        )
                    )
                }

                Text(
                    text = "You can change this anytime in Socials → Change Status.",
                    fontSize = 11.sp,
                    color = CleaveSurface.textMuted
                )

                Spacer(Modifier.height(2.dp))

                // Direct 1-tap ON / OFF action buttons + Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val offBtnShape = RoundedCornerShape(CleaveRadii.pill)
                    Box(
                        modifier = Modifier
                            .clip(offBtnShape)
                            .background(CleaveSurface.panelTrack)
                            .border(BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft), offBtnShape)
                            .clickable(role = Role.Button) { applyChoice(false) }
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Turn OFF",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleaveSurface.textSecondary
                        )
                    }

                    val onBtnShape = RoundedCornerShape(CleaveRadii.pill)
                    Box(
                        modifier = Modifier
                            .clip(onBtnShape)
                            .background(liveAccent)
                            .accentGlow(liveAccent, active = true, enabled = motion)
                            .accentRing(liveAccent, onBtnShape, active = true, enabled = motion, width = CleaveSpacing.hairline)
                            .clickable(role = Role.Button) { applyChoice(selectedOn) }
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(if (selectedOn) R.drawable.cleave_ic_bell else R.drawable.cleave_ic_check),
                                contentDescription = null,
                                tint = CleaveSurface.canvas,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(1.dp))
                            Text(
                                text = if (selectedOn) "Turn ON" else "Save (OFF)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CleaveSurface.canvas
                            )
                        }
                    }
                }
            }
        }
    }
}
