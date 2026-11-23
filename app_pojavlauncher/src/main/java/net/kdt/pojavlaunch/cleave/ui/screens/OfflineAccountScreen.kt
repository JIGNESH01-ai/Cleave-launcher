/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * "Offline account" page, the CLEAVE form of net.kdt.pojavlaunch.fragments.LocalLoginFragment:
 * the same name rule (3..16 characters, A-Z, 0-9 and _), the same license reminder for first-time
 * users, and the same ExtraConstants.MOJANG_LOGIN_TODO request.
 * Layout follows mirai-launcher-
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/account/LoginMethodScreen.kt (offline card)
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.PojavAuth
import net.kdt.pojavlaunch.cleave.bridge.PojavAuthBridge
import net.kdt.pojavlaunch.cleave.components.CleavePrimaryButton
import net.kdt.pojavlaunch.cleave.components.GlassPanel
import net.kdt.pojavlaunch.cleave.theme.CleaveLayoutDefaults
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import kotlin.random.Random

@Composable
fun OfflineAccountScreen(activity: FragmentActivity, onClose: () -> Unit, onCreated: () -> Unit) {
    val accent = CleaveThemeManager.currentAccent()
    var name by rememberSaveable { mutableStateOf("Steve") }
    val valid = PojavAuthBridge.isValidOfflineName(name)
    val suggestions = remember { listOf("Steve", "Alex") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CleaveLayoutDefaults.pagePadding()),
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
    ) {
        // ---- method chips (Microsoft / Ely.by / Offline) --------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.cleave_ic_arrow_back),
                    contentDescription = stringResource(R.string.cleave_create_close),
                    tint = CleaveSurface.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs), modifier = Modifier.weight(1f)) {
                MethodChip(text = "+ Microsoft", selected = false, accent = accent) {
                    PojavAuthBridge.openLogin(activity, PojavAuth.MICROSOFT)
                }
                MethodChip(text = "+ Ely.by", selected = false, accent = accent) {
                    PojavAuthBridge.openLogin(activity, PojavAuth.ELYBY)
                }
                MethodChip(text = "+ Offline", selected = true, accent = accent) { }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)) {
            Icon(
                painter = painterResource(R.drawable.cleave_ic_chevron_right),
                contentDescription = null,
                tint = CleaveSurface.textSecondary,
                modifier = Modifier.size(14.dp)
            )
            Column {
                Text(
                    text = stringResource(R.string.cleave_account_offline_title),
                    color = CleaveSurface.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(R.string.cleave_account_offline_subtitle),
                    color = CleaveSurface.textSecondary,
                    fontSize = 12.sp
                )
            }
        }

        GlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = CleaveSpacing.md
        ) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md)) {
                // ---- skin preview ----------------------------------------------------------
                Column(
                    modifier = Modifier.width(160.dp).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(CleaveRadii.cardSmall))
                            .background(CleaveSurface.panelRaised.copy(alpha = 0.23f))
                            .border(BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft), RoundedCornerShape(CleaveRadii.cardSmall)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cleave_head_steve),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.cleave_account_default_skin),
                        color = CleaveSurface.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = CleaveSpacing.sm)
                    )
                }

                // ---- name form -----------------------------------------------------------------
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                ) {
                    Text(
                        text = stringResource(R.string.cleave_account_player_name),
                        color = CleaveSurface.textSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CleaveSurface.textPrimary,
                            unfocusedTextColor = CleaveSurface.textPrimary,
                            focusedBorderColor = accent,
                            unfocusedBorderColor = CleaveSurface.borderSoft,
                            cursorColor = accent
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (valid) accent else CleaveSurface.danger),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(if (valid) R.drawable.cleave_ic_check else R.drawable.cleave_ic_warning_outlined),
                                contentDescription = null,
                                tint = if (valid) CleaveSurface.onAccent else Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Text(
                            text = if (valid) stringResource(R.string.cleave_account_name_rule)
                            else stringResource(R.string.cleave_account_name_invalid),
                            color = if (valid) CleaveSurface.textSecondary else CleaveSurface.danger,
                            fontSize = 11.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)) {
                        MethodChip(text = stringResource(R.string.cleave_account_random_name), selected = false, accent = accent) {
                            name = "Cleave_" + Random.nextInt(1000, 9999)
                        }
                        suggestions.forEach { suggestion ->
                            MethodChip(text = suggestion, selected = name == suggestion, accent = accent) { name = suggestion }
                        }
                    }
                    Box(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
                    ) {
                        Spacer(Modifier.weight(1f))
                        MethodChip(text = stringResource(R.string.cleave_account_cancel), selected = false, accent = accent) { onClose() }
                        CleavePrimaryButton(
                            text = stringResource(R.string.cleave_account_create_account),
                            iconRes = R.drawable.cleave_ic_check,
                            onClick = {
                                if (PojavAuthBridge.createOfflineAccount(activity, name)) onCreated()
                            },
                            enabled = valid
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodChip(text: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val shape = CircleShape
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) accent else CleaveSurface.panelRaised.copy(alpha = 0.2f), shape)
            .border(BorderStroke(CleaveSpacing.hairline, if (selected) accent else CleaveSurface.borderSoft), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = CleaveSpacing.smPlus, vertical = CleaveSpacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) CleaveSurface.onAccent else CleaveSurface.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
