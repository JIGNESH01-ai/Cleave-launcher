/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * "Add Account" page: Microsoft, Ely.by and Offline, plus the installed accounts strip.
 * Follows mirai-launcher-
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/account/LoginMethodScreen.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/account/AccountManagerScreen.kt
 * Sign-in runs through the PojavLauncher login fragments (see PojavAuthBridge).
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
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package net.kdt.pojavlaunch.cleave.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.CleaveAccount
import net.kdt.pojavlaunch.cleave.bridge.PojavAccountBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavAuth
import net.kdt.pojavlaunch.cleave.bridge.PojavAuthBridge
import net.kdt.pojavlaunch.cleave.components.CleaveGhostButton
import net.kdt.pojavlaunch.cleave.components.CleaveImageWell
import net.kdt.pojavlaunch.cleave.components.CleavePrimaryButton
import net.kdt.pojavlaunch.cleave.components.GlassPanel
import net.kdt.pojavlaunch.cleave.theme.CleaveLayoutDefaults
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager

@Composable
fun AddAccountScreen(activity: FragmentActivity, onClose: () -> Unit, onOffline: () -> Unit) {
    val accounts = PojavAccountBridge.accounts
    val current = PojavAccountBridge.current
    val accent = CleaveThemeManager.currentAccent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CleaveLayoutDefaults.pagePadding()),
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)) {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.cleave_ic_arrow_back),
                    contentDescription = stringResource(R.string.cleave_create_close),
                    tint = CleaveSurface.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.cleave_account_add_title),
                    color = CleaveSurface.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(R.string.cleave_account_add_subtitle),
                    color = CleaveSurface.textSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
        ) {
            AuthCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                iconRes = R.drawable.ic_auth_ms,
                title = "Microsoft",
                subtitle = stringResource(R.string.cleave_account_microsoft_sub),
                action = stringResource(R.string.cleave_account_sign_in),
                primary = true,
                onClick = { PojavAuthBridge.openLogin(activity, PojavAuth.MICROSOFT) }
            )
            AuthCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                iconRes = R.drawable.ic_auth_elyby,
                title = "Ely.by",
                subtitle = stringResource(R.string.cleave_account_elyby_sub),
                action = stringResource(R.string.cleave_account_sign_in),
                primary = false,
                onClick = { PojavAuthBridge.openLogin(activity, PojavAuth.ELYBY) }
            )
            AuthCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                iconRes = R.drawable.cleave_head_steve,
                title = "Offline",
                subtitle = stringResource(R.string.cleave_account_offline_sub),
                action = stringResource(R.string.cleave_account_create),
                primary = false,
                onClick = onOffline
            )
        }

        // ---- installed accounts ---------------------------------------------------------------
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            radius = CleaveRadii.panelSmall,
            contentPadding = CleaveSpacing.md
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)) {
                Text(
                    text = stringResource(R.string.cleave_account_your_accounts),
                    color = CleaveSurface.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (accounts.isEmpty()) {
                    Text(
                        text = stringResource(R.string.cleave_account_none),
                        color = CleaveSurface.textMuted,
                        fontSize = 12.sp
                    )
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
                    ) {
                        accounts.forEach { account ->
                            AccountChip(
                                account = account,
                                selected = account == current,
                                accent = accent,
                                onClick = { PojavAccountBridge.select(account) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthCard(
    modifier: Modifier,
    iconRes: Int,
    title: String,
    subtitle: String,
    action: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    val accent = CleaveThemeManager.currentAccent()
    val shape = RoundedCornerShape(CleaveRadii.cardSmall)
    GlassPanel(modifier = modifier, radius = CleaveRadii.cardSmall, contentPadding = CleaveSpacing.md) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(46.dp)
            )
        }
        Text(
            text = title,
            color = CleaveSurface.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = subtitle,
            color = CleaveSurface.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(CleaveSpacing.xs))
        if (primary) {
            CleavePrimaryButton(
                text = action,
                iconRes = null,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                accent = accent
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(CircleShape)
                    .background(CleaveSurface.panelRaised.copy(alpha = 0.2f), CircleShape)
                    .border(BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft), CircleShape)
                    .clickable(role = Role.Button, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Text(text = action, color = CleaveSurface.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AccountChip(account: CleaveAccount, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val shape = CircleShape
    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.22f) else CleaveSurface.panelRaised.copy(alpha = 0.17f), shape)
            .border(BorderStroke(CleaveSpacing.hairline, if (selected) accent else CleaveSurface.borderSoft), shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = CleaveSpacing.sm, vertical = CleaveSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
    ) {
        CleaveImageWell(
            image = account.face,
            fallback = R.drawable.cleave_ic_person_outlined,
            size = 22.dp,
            radius = 11.dp
        )
        Text(
            text = account.username + " · " + labelFor(account),
            color = if (selected) accent else CleaveSurface.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun labelFor(account: CleaveAccount): String = when (account.authType.name.lowercase()) {
    "microsoft" -> "Microsoft"
    "ely_by", "elyby" -> "Ely.by"
    "cleave" -> "CLEAVE Auth"
    else -> "Offline"
}
