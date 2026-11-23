/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Ported from mirai-launcher- (Zalith Launcher 2 fork):
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/AccountManageScreen.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/elements/AccountItem.kt
 * Plus the Account Details & Skin Wardrobe view (assets/mockups/account_skin_wardrobe_mockup.png):
 *   - Selected Account card (2D pixel avatar, username, account type badge, copyable UUID,
 *     Set Active / Delete actions)
 *   - Saved Skins & Presets 4-column grid (<DIR_GAME_HOME>/skins) + "+" import & Open Skins Folder
 *   - Interactive 360° 3D skinview3d character stage with Wide (Classic 4px) / Slim (Alex 3px)
 *     toggle, Equip Skin from Internal Storage (.png), Equip Cape (.png), and Reset to Default.
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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.cleave.bridge.CleaveAccount
import net.kdt.pojavlaunch.cleave.bridge.CleaveSkinPreset
import net.kdt.pojavlaunch.cleave.bridge.PojavAccountBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavAuthBridge
import net.kdt.pojavlaunch.cleave.components.CleaveImageWell
import net.kdt.pojavlaunch.cleave.components.CleavePrimaryButton
import net.kdt.pojavlaunch.cleave.components.CleaveSkinPreview3D
import net.kdt.pojavlaunch.cleave.components.MetaPill
import net.kdt.pojavlaunch.cleave.components.QuietEmptyState
import net.kdt.pojavlaunch.cleave.components.liquidGlass
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.cleave.ui.CleaveNav
import net.kdt.pojavlaunch.cleave.ui.LauncherSection
import net.kdt.pojavlaunch.authenticator.AuthType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountsScreen(activity: FragmentActivity) {
    val accent = CleaveThemeManager.currentAccent()
    val accounts = PojavAccountBridge.accounts
    val current = PojavAccountBridge.current
    val focusedKey = PojavAccountBridge.focusedAccountKey
    val focusedAccount = remember(accounts, focusedKey) {
        if (focusedKey == null) null else accounts.firstOrNull { it.key == focusedKey }
    }
    var pendingDelete by remember { mutableStateOf<CleaveAccount?>(null) }
    var showOfflineDialog by remember { mutableStateOf(false) }
    var offlineUsername by remember { mutableStateOf("") }
    var skinPickerAccount by remember { mutableStateOf<CleaveAccount?>(null) }
    var pendingSkinUri by remember { mutableStateOf<Pair<CleaveAccount, Uri>?>(null) }

    val skinFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val target = skinPickerAccount ?: current ?: accounts.firstOrNull()
        if (uri != null && target != null) {
            pendingSkinUri = target to uri
        }
    }

    val capeFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val target = current ?: accounts.firstOrNull()
        if (uri != null && target != null) {
            PojavAccountBridge.equipCapeFromUri(activity, target, uri)
        }
    }

    if (showOfflineDialog) {
        val validOfflineName = PojavAuthBridge.isValidOfflineName(offlineUsername.trim())
        AlertDialog(
            onDismissRequest = { showOfflineDialog = false },
            containerColor = CleaveSurface.panelRaised,
            title = { Text("Add Offline Account", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = offlineUsername,
                    onValueChange = { offlineUsername = it },
                    singleLine = true,
                    isError = offlineUsername.isNotEmpty() && !validOfflineName,
                    supportingText = { Text(stringResource(R.string.cleave_account_name_rule)) },
                    label = { Text("Username", color = CleaveSurface.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = accent,
                        unfocusedBorderColor = CleaveSurface.borderSoft,
                        cursorColor = accent
                    )
                )
            },
            confirmButton = {
                TextButton(
                    enabled = validOfflineName,
                    onClick = {
                        if (PojavAccountBridge.createOfflineAccount(offlineUsername)) {
                            offlineUsername = ""
                            showOfflineDialog = false
                        }
                    }
                ) {
                    Text("Create", color = if (validOfflineName) accent else CleaveSurface.textSecondary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfflineDialog = false }) {
                    Text(stringResource(R.string.cleave_settings_cancel), color = CleaveSurface.textSecondary)
                }
            }
        )
    }

    pendingSkinUri?.let { (targetAccount, uri) ->
        AlertDialog(
            onDismissRequest = { pendingSkinUri = null },
            containerColor = CleaveSurface.panelRaised,
            title = { Text("Select Skin Model", color = Color.White) },
            text = {
                Text(
                    "Choose the arm model for ${targetAccount.username}'s custom skin:",
                    color = CleaveSurface.textSecondary
                )
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            PojavAccountBridge.setSlimModel(activity, targetAccount, false)
                            PojavAccountBridge.equipSkinFromUri(activity, targetAccount, uri)
                            pendingSkinUri = null
                        }
                    ) {
                        Text("Classic (4px)", color = accent, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = {
                            PojavAccountBridge.setSlimModel(activity, targetAccount, true)
                            PojavAccountBridge.equipSkinFromUri(activity, targetAccount, uri)
                            pendingSkinUri = null
                        }
                    ) {
                        Text("Slim (3px)", color = accent, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingSkinUri = null }) {
                    Text(stringResource(R.string.cleave_settings_cancel), color = CleaveSurface.textSecondary)
                }
            }
        )
    }

    pendingDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = CleaveSurface.panelRaised,
            title = { Text(stringResource(R.string.cleave_accounts_remove), color = CleaveSurface.textPrimary) },
            text = { Text(stringResource(R.string.cleave_accounts_remove_confirm, account.username), color = CleaveSurface.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    PojavAccountBridge.delete(account)
                    pendingDelete = null
                }) { Text(stringResource(R.string.cleave_accounts_remove), color = CleaveSurface.danger) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.cleave_settings_cancel), color = CleaveSurface.textSecondary)
                }
            }
        )
    }

    if (focusedAccount != null) {
        BackHandler {
            PojavAccountBridge.closeDetail()
        }
        AccountSkinWardrobeView(
            activity = activity,
            account = focusedAccount,
            isSelected = focusedAccount == current,
            onBackToAccounts = { PojavAccountBridge.closeDetail() },
            onRequestDelete = { pendingDelete = focusedAccount }
        )
        return
    }

    // Mirai AccountManageScreen + SkinLibraryPanel layout (Photo 6)
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 12.dp)
            .liquidGlass(shape = RoundedCornerShape(22.dp), strength = 0.7f),
        shape = RoundedCornerShape(22.dp),
        color = CleaveSurface.panel,
        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(all = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Top row of pill buttons: + Microsoft, + Offline, + Auth Server, and + Equip Cape (from internal storage)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiraiTopAccountPill(
                    text = "Microsoft",
                    onClick = { CleaveNav.navigate(LauncherSection.ADD_ACCOUNT) }
                )
                MiraiTopAccountPill(
                    text = "CLEAVE",
                    onClick = {
                        if (net.kdt.pojavlaunch.cleave.social.SocialBackend.signedIn) {
                            AuthType.CLEAVE.createAuth().createAccount(PojavAccountBridge, "")
                        } else {
                            Toast.makeText(activity, "Sign in to CLEAVE Social and choose a game name first", Toast.LENGTH_LONG).show()
                            CleaveNav.navigate(LauncherSection.SOCIALS)
                        }
                    }
                )
                MiraiTopAccountPill(
                    text = "Offline",
                    onClick = { showOfflineDialog = true }
                )
                MiraiTopAccountPill(
                    text = "Auth Server",
                    onClick = { CleaveNav.navigate(LauncherSection.ADD_ACCOUNT) }
                )
                MiraiTopAccountPill(
                    text = "Equip Cape",
                    highlighted = true,
                    onClick = {
                        val target = current ?: accounts.firstOrNull()
                        if (target == null) {
                            Toast.makeText(activity, "Add or select an account first", Toast.LENGTH_SHORT).show()
                        } else {
                            runCatching {
                                capeFilePicker.launch(arrayOf("image/png", "image/*"))
                            }.onFailure {
                                Toast.makeText(activity, "Unable to open file picker", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            // 2. Accounts list (Mirai AccountItem cards — Photo 6)
            if (accounts.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = CleaveSurface.panelRaised.copy(alpha = 0.35f),
                    border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.cleave_accounts_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CleaveSurface.textSecondary
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        val isCurrent = account == current
                        MiraiAccountItemRow(
                            account = account,
                            isCurrent = isCurrent,
                            onSelect = { PojavAccountBridge.select(account) },
                            onOpenWardrobe = { PojavAccountBridge.openDetail(account) },
                            onChangeSkin = {
                                skinPickerAccount = account
                                runCatching {
                                    skinFilePicker.launch(arrayOf("image/png", "image/*"))
                                }.onFailure {
                                    Toast.makeText(activity, "Unable to open file picker", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onRefresh = {
                                if (account.authType.requiresLogin()) {
                                    PojavAccountBridge.forceRefresh(account)
                                } else {
                                    PojavAccountBridge.reload()
                                    Toast.makeText(activity, "Refreshed ${account.username}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCopyUuid = {
                                val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Minecraft UUID", account.displayUuid))
                                Toast.makeText(activity, "Copied UUID: ${account.displayUuid}", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { pendingDelete = account }
                        )
                    }
                }
            }

            // 3. SkinLibraryPanel (Search skins from MC Skin... + 3-column skin grid — Photo 6)
            MiraiSkinLibraryPanel(
                activity = activity,
                currentAccount = current ?: accounts.firstOrNull()
            )
        }
    }
}

@Composable
private fun MiraiTopAccountPill(
    text: String,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    val accent = CleaveThemeManager.currentAccent()
    Surface(
        shape = CircleShape,
        color = if (highlighted) accent.copy(alpha = 0.22f) else CleaveSurface.panelRaised,
        border = BorderStroke(
            width = 1.dp,
            color = if (highlighted) accent.copy(alpha = 0.65f) else CleaveSurface.borderSoft
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.cleave_ic_add),
                contentDescription = text,
                tint = if (highlighted) accent else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun MiraiAccountItemRow(
    account: CleaveAccount,
    isCurrent: Boolean,
    onSelect: () -> Unit,
    onOpenWardrobe: () -> Unit,
    onChangeSkin: () -> Unit,
    onRefresh: () -> Unit,
    onCopyUuid: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = CleaveThemeManager.currentAccent()
    val typeLabel = when (account.authType) {
        AuthType.MICROSOFT -> "Microsoft"
        AuthType.ELY_BY -> "Auth Server"
        AuthType.CLEAVE -> "CLEAVE Auth"
        AuthType.LOCAL -> "Offline"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CleaveSurface.panelRaised.copy(alpha = 0.42f),
        border = BorderStroke(
            width = if (isCurrent) 1.dp else CleaveSpacing.hairline,
            color = if (isCurrent) accent.copy(alpha = 0.45f) else CleaveSurface.borderSoft
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isCurrent,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accent,
                    unselectedColor = Color.White.copy(alpha = 0.70f)
                )
            )
            CleaveImageWell(
                image = account.face,
                fallback = R.drawable.cleave_ic_person_outlined,
                size = 36.dp,
                radius = 6.dp,
                modifier = Modifier.clickable(onClick = onOpenWardrobe)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.username,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = CleaveSurface.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onChangeSkin) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_styler_outlined),
                        contentDescription = "Equip Skin from Storage",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_refresh),
                        contentDescription = "Refresh Account",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onCopyUuid) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_content_copy_filled),
                        contentDescription = "Copy UUID",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_delete_outlined),
                        contentDescription = "Delete Account",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private data class MiraiSkinLibraryEntry(
    val id: String,
    val name: String,
    val modelType: String,
    val isSlim: Boolean,
    val avatarUrl: String?,
    val skinUrls: List<String>,
    val localPreset: CleaveSkinPreset? = null
)

private val defaultMcSkinEntries = listOf(
    MiraiSkinLibraryEntry(
        id = "notch",
        name = "Notch",
        modelType = "Classic",
        isSlim = false,
        avatarUrl = "https://mc-heads.net/avatar/069a79f444e94726a5befca90e38aaf5/64",
        skinUrls = listOf(
            "https://mc-heads.net/skin/069a79f444e94726a5befca90e38aaf5",
            "https://minotar.net/skin/Notch",
            "https://crafatar.com/skins/069a79f444e94726a5befca90e38aaf5"
        )
    ),
    MiraiSkinLibraryEntry(
        id = "jeb_",
        name = "jeb_",
        modelType = "Classic",
        isSlim = false,
        avatarUrl = "https://mc-heads.net/avatar/853c80ef3c3749fdaa49938b674adae6/64",
        skinUrls = listOf(
            "https://mc-heads.net/skin/853c80ef3c3749fdaa49938b674adae6",
            "https://minotar.net/skin/jeb_",
            "https://crafatar.com/skins/853c80ef3c3749fdaa49938b674adae6"
        )
    ),
    MiraiSkinLibraryEntry(
        id = "dinnerbone",
        name = "Dinnerbone",
        modelType = "Classic",
        isSlim = false,
        avatarUrl = "https://mc-heads.net/avatar/61699b2ed3274a019f1e0ea8c3f06bc6/64",
        skinUrls = listOf(
            "https://mc-heads.net/skin/61699b2ed3274a019f1e0ea8c3f06bc6",
            "https://minotar.net/skin/Dinnerbone",
            "https://crafatar.com/skins/61699b2ed3274a019f1e0ea8c3f06bc6"
        )
    ),
    MiraiSkinLibraryEntry(
        id = "technoblade",
        name = "Technoblade",
        modelType = "Classic",
        isSlim = false,
        avatarUrl = "https://mc-heads.net/avatar/b876ec32e396476ba1158438d83c67d4/64",
        skinUrls = listOf(
            "https://mc-heads.net/skin/b876ec32e396476ba1158438d83c67d4",
            "https://minotar.net/skin/Technoblade"
        )
    ),
    MiraiSkinLibraryEntry(
        id = "dream",
        name = "Dream",
        modelType = "Classic",
        isSlim = false,
        avatarUrl = "https://mc-heads.net/avatar/ec70bcaf702f4bb8b48d276fa52a780c/64",
        skinUrls = listOf(
            "https://mc-heads.net/skin/ec70bcaf702f4bb8b48d276fa52a780c",
            "https://minotar.net/skin/Dream"
        )
    ),
    MiraiSkinLibraryEntry(
        id = "ph1lza",
        name = "Ph1LzA",
        modelType = "Classic",
        isSlim = false,
        avatarUrl = "https://mc-heads.net/avatar/334b65f7055a4b1f97409f635116220b/64",
        skinUrls = listOf(
            "https://mc-heads.net/skin/334b65f7055a4b1f97409f635116220b",
            "https://minotar.net/skin/Ph1LzA"
        )
    ),
    MiraiSkinLibraryEntry(
        id = "grian",
        name = "Grian",
        modelType = "Classic",
        isSlim = false,
        avatarUrl = "https://mc-heads.net/avatar/5f8eb73b25be4c5aa50fd27d65e30ca0/64",
        skinUrls = listOf(
            "https://mc-heads.net/skin/5f8eb73b25be4c5aa50fd27d65e30ca0",
            "https://minotar.net/skin/Grian"
        )
    ),
    MiraiSkinLibraryEntry(
        id = "mumbo",
        name = "Mumbo",
        modelType = "Classic",
        isSlim = false,
        avatarUrl = "https://mc-heads.net/avatar/c7da90d56a054217b94a7d427cbbcad8/64",
        skinUrls = listOf(
            "https://mc-heads.net/skin/c7da90d56a054217b94a7d427cbbcad8",
            "https://minotar.net/skin/Mumbo"
        )
    ),
    MiraiSkinLibraryEntry(
        id = "alex",
        name = "Alex",
        modelType = "Slim",
        isSlim = true,
        avatarUrl = "https://mc-heads.net/avatar/ec561538f3fd461daff5086b22154bce/64",
        skinUrls = listOf(
            "https://mc-heads.net/skin/ec561538f3fd461daff5086b22154bce",
            "https://minotar.net/skin/MHF_Alex"
        )
    )
)

@Composable
private fun MiraiSkinLibraryPanel(
    activity: FragmentActivity,
    currentAccount: CleaveAccount?
) {
    val accent = CleaveThemeManager.currentAccent()
    val scope = rememberCoroutineScope()
    val revision = PojavAccountBridge.wardrobeRevision
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var localPresets by remember { mutableStateOf<List<CleaveSkinPreset>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<MiraiSkinLibraryEntry>?>(null) }

    LaunchedEffect(revision) {
        localPresets = withContext(Dispatchers.IO) {
            PojavAccountBridge.loadSavedSkinPresets(activity)
        }
    }

    val displayedEntries = remember(searchResults, localPresets) {
        searchResults ?: buildList {
            addAll(defaultMcSkinEntries)
            localPresets.filter { it.file != null }.forEach { preset ->
                add(
                    MiraiSkinLibraryEntry(
                        id = "local_${preset.id}",
                        name = preset.name,
                        modelType = "Classic",
                        isSlim = false,
                        avatarUrl = null,
                        skinUrls = emptyList(),
                        localPreset = preset
                    )
                )
            }
        }
    }

    fun triggerSearch() {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            searchResults = null
            return
        }
        isSearching = true
        scope.launch {
            val found = withContext(Dispatchers.IO) {
                searchMcSkinsOnline(trimmed)
            }
            searchResults = found
            isSearching = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    if (it.isBlank()) searchResults = null
                },
                placeholder = {
                    Text(
                        text = "Search skins from MC Skin...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = accent,
                    unfocusedBorderColor = CleaveSurface.borderSoft,
                    cursorColor = accent
                ),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { triggerSearch() },
                enabled = !isSearching,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = CleaveSurface.onAccent
                ),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp)
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = CleaveSurface.onAccent
                    )
                } else {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            displayedEntries.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { entry ->
                        MiraiSkinLibraryCard(
                            entry = entry,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (currentAccount == null) {
                                    Toast.makeText(activity, "Add or select an account first", Toast.LENGTH_SHORT).show()
                                } else if (entry.localPreset != null) {
                                    PojavAccountBridge.equipSkinPreset(activity, currentAccount, entry.localPreset)
                                } else {
                                    PojavAccountBridge.equipOnlineSkin(
                                        context = activity,
                                        account = currentAccount,
                                        skinName = entry.name,
                                        candidateUrls = entry.skinUrls,
                                        isSlim = entry.isSlim
                                    )
                                }
                            }
                        )
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // A skin picked here is handed to Minecraft by the launcher when the game starts, so it
        // shows up the next time the instance is launched, not on the menu this is drawn on.
        Text(
            text = "Equipped skins, capes and the Wide/Slim model reach the game on your next launch.",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun MiraiSkinLibraryCard(
    entry: MiraiSkinLibraryEntry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var faceBitmap by remember(entry.id) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(entry.id) {
        faceBitmap = withContext(Dispatchers.IO) {
            PojavAccountBridge.loadSkinFacePreview(
                cacheKey = entry.id,
                localFile = entry.localPreset?.file,
                avatarUrl = entry.avatarUrl ?: entry.skinUrls.firstOrNull()
            )
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CleaveSurface.panelRaised.copy(alpha = 0.42f),
        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CleaveImageWell(
                image = faceBitmap,
                fallback = R.drawable.cleave_ic_person_outlined,
                size = 36.dp,
                radius = 6.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.modelType,
                    style = MaterialTheme.typography.labelSmall,
                    color = CleaveSurface.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

private fun searchMcSkinsOnline(query: String): List<MiraiSkinLibraryEntry> {
    val results = ArrayList<MiraiSkinLibraryEntry>()
    val cleanQuery = query.trim()

    // 1. Direct player name match via MC-Heads / Minotar
    if (cleanQuery.matches(Regex("^[A-Za-z0-9_]{2,16}$"))) {
        results.add(
            MiraiSkinLibraryEntry(
                id = "player_${cleanQuery.lowercase()}",
                name = cleanQuery,
                modelType = "Classic",
                isSlim = false,
                avatarUrl = "https://mc-heads.net/avatar/$cleanQuery/64",
                skinUrls = listOf(
                    "https://mc-heads.net/skin/$cleanQuery",
                    "https://minotar.net/skin/$cleanQuery"
                )
            )
        )
    }

    // 2. LittleSkin public skin library keyword search (same backend as Mirai SkinLibrary.kt)
    runCatching {
        val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
        val url = "https://littleskin.cn/skinlib/list?filter=skin&sort=likes&page=1&keyword=$encoded"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 6000
            readTimeout = 6000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "CleaveLauncher/1.6")
        }
        val body = try {
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else null
        } finally {
            conn.disconnect()
        }
        if (!body.isNullOrBlank()) {
            val root = JsonParser.parseString(body).asJsonObject
            val data = root.getAsJsonArray("data")
            if (data != null) {
                for (i in 0 until minOf(data.size(), 12)) {
                    val item = data[i].asJsonObject
                    val tid = item.get("tid")?.asInt ?: continue
                    val name = item.get("name")?.asString ?: "Skin #$tid"
                    val type = item.get("type")?.asString ?: "steve"
                    val isSlim = type.equals("alex", ignoreCase = true)
                    val hash = runCatching {
                        val infoConn = (URL("https://littleskin.cn/skinlib/info/$tid").openConnection() as HttpURLConnection).apply {
                            connectTimeout = 4000
                            readTimeout = 4000
                            setRequestProperty("Accept", "application/json")
                        }
                        try {
                            if (infoConn.responseCode in 200..299) {
                                val infoText = infoConn.inputStream.bufferedReader().use { it.readText() }
                                JsonParser.parseString(infoText).asJsonObject.get("hash")?.asString
                            } else null
                        } finally {
                            infoConn.disconnect()
                        }
                    }.getOrNull()
                    if (!hash.isNullOrBlank()) {
                        val texUrl = "https://littleskin.cn/textures/$hash"
                        results.add(
                            MiraiSkinLibraryEntry(
                                id = "ls_$tid",
                                name = name,
                                modelType = if (isSlim) "Slim" else "Classic",
                                isSlim = isSlim,
                                avatarUrl = texUrl,
                                skinUrls = listOf(texUrl)
                            )
                        )
                    }
                }
            }
        }
    }

    if (results.isEmpty()) {
        results.addAll(
            defaultMcSkinEntries.filter { it.name.contains(cleanQuery, ignoreCase = true) }
        )
    }
    return results
}

@Composable
private fun AccountSkinWardrobeView(
    activity: FragmentActivity,
    account: CleaveAccount,
    isSelected: Boolean,
    onBackToAccounts: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val accent = CleaveThemeManager.currentAccent()
    val revision = PojavAccountBridge.wardrobeRevision
    var isSlim by remember(account.key, revision) {
        mutableStateOf(PojavAccountBridge.isSlimModel(account))
    }
    var activePresetId by remember(account.key, revision) {
        mutableStateOf(PojavAccountBridge.activePresetId(activity, account))
    }
    var presets by remember { mutableStateOf<List<CleaveSkinPreset>>(emptyList()) }
    var compactGrid by remember { mutableStateOf(false) }

    LaunchedEffect(account.key, revision) {
        isSlim = PojavAccountBridge.isSlimModel(account)
        activePresetId = PojavAccountBridge.activePresetId(activity, account)
        presets = withContext(Dispatchers.IO) {
            PojavAccountBridge.loadSavedSkinPresets(activity)
        }
    }

    val skinDocumentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            PojavAccountBridge.equipSkinFromUri(activity, account, uri)
        }
    }

    val capeDocumentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            PojavAccountBridge.equipCapeFromUri(activity, account, uri)
        }
    }

    fun launchSkinPicker() {
        runCatching {
            skinDocumentPicker.launch(arrayOf("image/png", "image/*"))
        }.onFailure {
            Toast.makeText(activity, "Unable to open file picker", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCapePicker() {
        runCatching {
            capeDocumentPicker.launch(arrayOf("image/png", "image/*"))
        }.onFailure {
            Toast.makeText(activity, "Unable to open file picker", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF151924),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onBackToAccounts)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_arrow_back),
                            contentDescription = "Back to accounts",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "ACCOUNT DETAILS & SKIN WARDROBE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Manage your accounts, skins, capes, and presets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.62f)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF151924),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { CleaveNav.navigate(LauncherSection.ADD_ACCOUNT) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_add),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.cleave_accounts_add),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Two-Column Wardrobe Content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // LEFT COLUMN: Selected Account Card + Saved Skins & Presets Grid
            Column(
                modifier = Modifier
                    .weight(0.56f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Card: SELECTED ACCOUNT
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF121620).copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "SELECTED ACCOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.9.sp
                            ),
                            color = Color.White.copy(alpha = 0.55f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 2D Pixel Face Avatar with cyan frame + active dot
                            Box(modifier = Modifier.size(68.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF1A2030))
                                        .border(2.dp, accent, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (account.face != null) {
                                        Image(
                                            bitmap = account.face,
                                            contentDescription = account.username,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop,
                                            filterQuality = FilterQuality.None
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(R.drawable.cleave_ic_person_outlined),
                                            contentDescription = null,
                                            tint = accent,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF22C55E) else Color(0xFF64748B))
                                        .border(2.dp, Color(0xFF121620), CircleShape)
                                )
                            }

                            // Username + Auth Type badge + Copyable UUID
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = account.username,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = accent.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(accent)
                                        )
                                        Text(
                                            text = authTypeDisplayLabel(account.authType),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = accent
                                        )
                                    }
                                }

                                // Copyable UUID pill
                                val uuidText = account.displayUuid
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0B0E15),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            cm?.setPrimaryClip(ClipData.newPlainText("UUID", uuidText))
                                            Toast.makeText(activity, "UUID copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "UUID: $uuidText",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp
                                            ),
                                            color = Color.White.copy(alpha = 0.68f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            painter = painterResource(R.drawable.cleave_ic_content_copy_filled),
                                            contentDescription = "Copy UUID",
                                            tint = Color.White.copy(alpha = 0.55f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }

                            // Right action buttons: Set Active + Delete Account
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Button(
                                    onClick = { PojavAccountBridge.select(account) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) accent.copy(alpha = 0.22f) else accent,
                                        contentColor = if (isSelected) accent else Color(0xFF061018)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.cleave_ic_check),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isSelected) "Active" else "Set Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = onRequestDelete,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, CleaveSurface.danger.copy(alpha = 0.55f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = CleaveSurface.danger.copy(alpha = 0.10f),
                                        contentColor = CleaveSurface.danger
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.cleave_ic_delete_outlined),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Delete Account",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Card: SAVED SKINS & PRESETS
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF121620).copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SAVED SKINS & PRESETS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.9.sp
                                ),
                                color = Color.White.copy(alpha = 0.55f)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1B2230),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { launchSkinPicker() }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(R.drawable.cleave_ic_add),
                                            contentDescription = "Import .png Skin",
                                            tint = accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1B2230),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { compactGrid = !compactGrid }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(R.drawable.cleave_ic_dashboard_outlined),
                                            contentDescription = "Toggle Grid Density",
                                            tint = Color.White.copy(alpha = 0.75f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(if (compactGrid) 5 else 4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(presets, key = { it.id }) { preset ->
                                val equipped = preset.id.equals(activePresetId, ignoreCase = true)
                                SkinPresetGridCard(
                                    preset = preset,
                                    equipped = equipped,
                                    accent = accent,
                                    onClick = {
                                        PojavAccountBridge.equipSkinPreset(activity, account, preset) {
                                            activePresetId = preset.id
                                        }
                                    }
                                )
                            }
                        }

                        // Open Skins Folder footer bar
                        val skinsDir = remember { PojavAccountBridge.skinsDirectory() }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0B0E15),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    runCatching { Tools.openPath(activity, skinsDir, false) }
                                        .onFailure { CleaveNav.navigate(LauncherSection.FILES) }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.cleave_ic_folder_outlined),
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Open Skins Folder",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = skinsDir.absolutePath,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.45f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // RIGHT COLUMN: 3D Skin Wardrobe Stage + Wide/Slim Toggle + Equip Buttons
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF121620).copy(alpha = 0.94f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .weight(0.44f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Wide (Classic 4px) / Slim (Alex 3px) segmented pill toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0B0E15),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ModelTypePill(
                                modifier = Modifier.weight(1f),
                                label = "Wide (Classic 4px)",
                                selected = !isSlim,
                                accent = accent,
                                onClick = {
                                    isSlim = false
                                    PojavAccountBridge.setSlimModel(activity, account, false)
                                }
                            )
                            ModelTypePill(
                                modifier = Modifier.weight(1f),
                                label = "Slim (Alex 3px)",
                                selected = isSlim,
                                accent = accent,
                                onClick = {
                                    isSlim = true
                                    PojavAccountBridge.setSlimModel(activity, account, true)
                                }
                            )
                        }
                    }

                    // Center 360° 3D Character Stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Subtle radial stage backdrop
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            accent.copy(alpha = 0.18f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        // Grass-block style stage pedestal at bottom of 3D character
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1A2432),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.30f)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 22.dp)
                                .width(136.dp)
                                .height(22.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF22C55E).copy(alpha = 0.32f),
                                                Color(0xFF78350F).copy(alpha = 0.38f)
                                            )
                                        )
                                    )
                            )
                        }

                        CleaveSkinPreview3D(
                            account = account,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(bottom = 14.dp)
                        )

                        // Left/Right rotation hint pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1B2230).copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.cleave_ic_arrow_left_rounded),
                                        contentDescription = "Drag left to rotate",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1B2230).copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.cleave_ic_arrow_right_rounded),
                                        contentDescription = "Drag right to rotate",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Drag to rotate • Scroll to zoom",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.48f),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }

                    // Bottom Action Buttons: Equip Skin from Internal Storage (.png), Equip Cape (.png), Reset to Default
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { launchSkinPicker() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = Color(0xFF061018)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.cleave_ic_upload),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Equip Skin from Internal Storage (.png)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { launchCapePicker() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFF181E2C),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.cleave_ic_style_outlined),
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Equip Cape (.png)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    PojavAccountBridge.resetSkinAndCape(activity, account)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFF181E2C),
                                    contentColor = Color.White.copy(alpha = 0.85f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.cleave_ic_refresh),
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reset to Default",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelTypePill(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = if (selected) accent else Color.Transparent,
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (selected) Color(0xFF061018) else Color.White.copy(alpha = 0.68f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SkinPresetGridCard(
    preset: CleaveSkinPreset,
    equipped: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (equipped) Color(0xFF182436) else Color(0xFF171C28),
        border = BorderStroke(
            width = if (equipped) 1.5.dp else 1.dp,
            color = if (equipped) accent else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            if (equipped) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_check),
                        contentDescription = "Equipped",
                        tint = Color(0xFF061018),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (preset.preview != null) {
                        Image(
                            bitmap = preset.preview,
                            contentDescription = preset.name,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 2.dp),
                            contentScale = ContentScale.Fit,
                            filterQuality = FilterQuality.None
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_person_outlined),
                            contentDescription = preset.name,
                            tint = accent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (equipped) FontWeight.Bold else FontWeight.Medium,
                    color = if (equipped) accent else Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun authTypeDisplayLabel(authType: AuthType): String = when (authType) {
    AuthType.MICROSOFT -> "Microsoft Account"
    AuthType.ELY_BY -> "Ely.by Account"
    AuthType.CLEAVE -> "CLEAVE Account"
    AuthType.LOCAL -> "Local / Offline Account"
}

@Composable
private fun AccountCard(
    account: CleaveAccount,
    isCurrent: Boolean,
    onOpenWardrobe: () -> Unit,
    onUse: () -> Unit,
    onRefresh: () -> Unit,
    onRemove: () -> Unit
) {
    val accent = CleaveThemeManager.currentAccent()
    val shape = RoundedCornerShape(CleaveRadii.cardLarge)
    val typeLabel = when (account.authType) {
        AuthType.MICROSOFT -> stringResource(R.string.cleave_accounts_type_microsoft)
        AuthType.ELY_BY -> stringResource(R.string.cleave_accounts_type_elyby)
        AuthType.CLEAVE -> "CLEAVE Auth"
        AuthType.LOCAL -> stringResource(R.string.cleave_accounts_type_local)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(shape = shape, tint = if (isCurrent) accent else CleaveSurface.glassTint, strength = if (isCurrent) 0.9f else 0.6f)
            .background(if (isCurrent) accent.copy(alpha = 0.12f) else CleaveSurface.panel, shape)
            .border(BorderStroke(if (isCurrent) 1.5.dp else CleaveSpacing.hairline, if (isCurrent) accent else CleaveSurface.borderSoft), shape)
            .clickable(role = Role.Button, onClick = onOpenWardrobe)
            .padding(CleaveSpacing.md),
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smPlus)) {
            CleaveImageWell(image = account.face, fallback = R.drawable.cleave_ic_person_outlined, size = 48.dp, radius = CleaveRadii.control)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = account.username, color = CleaveSurface.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = if (account.expired) stringResource(R.string.cleave_accounts_expired) else "$typeLabel • Tap to equip skin",
                    color = if (account.expired) CleaveSurface.warning else CleaveSurface.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isCurrent) MetaPill(text = stringResource(R.string.cleave_accounts_current), iconRes = R.drawable.cleave_ic_check)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)) {
            MetaPill(text = typeLabel, iconRes = R.drawable.cleave_ic_link)
            Box(Modifier.weight(1f))
            CardTextAction("Skin Wardrobe", accent, onOpenWardrobe)
            if (account.authType.requiresLogin()) {
                CardTextAction(stringResource(R.string.cleave_accounts_refresh), CleaveSurface.textSecondary, onRefresh)
            }
            CardTextAction(stringResource(R.string.cleave_accounts_remove), CleaveSurface.danger, onRemove)
            if (!isCurrent) {
                Text(
                    text = stringResource(R.string.cleave_accounts_use),
                    color = CleaveSurface.onAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accent)
                        .clickable(onClick = onUse)
                        .padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.xs)
                )
            }
        }
    }
}

@Composable
private fun CardTextAction(text: String, color: Color, onClick: () -> Unit) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = CleaveSpacing.smPlus, vertical = CleaveSpacing.xs)
    )
}
