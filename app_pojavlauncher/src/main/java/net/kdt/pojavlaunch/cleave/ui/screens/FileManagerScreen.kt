/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Ported from mirai-launcher- (Zalith Launcher 2 fork):
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/filemanager/ui/FileManagerRootScreen.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/filemanager/ui/FmMainPage.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/filemanager/ui/FmEditorScreen.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/filemanager/ui/FmTrashScreen.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/filemanager/viewmodel/FmClipboard.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/filemanager/viewmodel/NavHistory.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/filemanager/viewmodel/SortConfig.kt
 * File operations go through PojavFileBridge; roots are Pojav's own folders.
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

import android.text.format.DateFormat
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.FileItem
import net.kdt.pojavlaunch.cleave.bridge.FileSort
import net.kdt.pojavlaunch.cleave.bridge.FileSortField
import net.kdt.pojavlaunch.cleave.bridge.PojavFileBridge
import net.kdt.pojavlaunch.cleave.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.cleave.bridge.TrashEntry
import net.kdt.pojavlaunch.cleave.components.CleaveGhostButton
import net.kdt.pojavlaunch.cleave.components.CleavePrimaryButton
import net.kdt.pojavlaunch.cleave.components.PillTabs
import net.kdt.pojavlaunch.cleave.components.QuietEmptyState
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import java.io.File
import java.util.Date

/** Survives section switches, like mirai's FmClipboard. */
private object FmClipboard {
    var files by mutableStateOf<List<File>>(emptyList())
    var move by mutableStateOf(false)
}

private class NameRequest(val titleRes: Int, val initial: String, val onConfirm: suspend (String) -> Boolean)

private fun fileIcon(item: File, directory: Boolean): Int {
    if (directory) return R.drawable.cleave_ic_folder_filled
    return when (item.extension.lowercase()) {
        "zip", "mrpack" -> R.drawable.cleave_ic_folder_zip_outlined
        "jar" -> R.drawable.cleave_ic_java
        "png", "jpg", "jpeg", "webp", "gif", "bmp" -> R.drawable.cleave_ic_photo_library_outlined
        "log" -> R.drawable.cleave_ic_terminal_outlined
        else -> if (PojavFileBridge.isText(item)) R.drawable.cleave_ic_description_outlined else R.drawable.cleave_ic_text_snippet_filled
    }
}

@Composable
private fun cleaveFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = CleaveSurface.textPrimary,
    unfocusedTextColor = CleaveSurface.textPrimary,
    focusedBorderColor = CleaveThemeManager.currentAccent(),
    unfocusedBorderColor = CleaveSurface.borderSoft,
    cursorColor = CleaveThemeManager.currentAccent()
)

@Composable
fun FileManagerScreen(activity: FragmentActivity) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accent = CleaveThemeManager.currentAccent()
    val selectedInstance = PojavInstanceBridge.selected
    val roots = remember(selectedInstance?.source) { PojavFileBridge.roots() }
    var rootIndex by rememberSaveable { mutableIntStateOf(0) }
    val root = roots[rootIndex.coerceIn(0, roots.size - 1)]
    var currentPath by rememberSaveable(root.dir.absolutePath) { mutableStateOf(root.dir.absolutePath) }
    val current = File(currentPath)
    var showTrash by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<File?>(null) }

    var sortField by rememberSaveable { mutableStateOf(FileSortField.NAME) }
    var descending by rememberSaveable { mutableStateOf(false) }
    var foldersFirst by rememberSaveable { mutableStateOf(true) }
    var showHidden by rememberSaveable { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }

    var items by remember { mutableStateOf<List<FileItem>?>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var revision by remember { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FileItem>?>(null) }
    var selection by remember { mutableStateOf<Set<File>>(emptySet()) }
    var nameRequest by remember { mutableStateOf<NameRequest?>(null) }
    var deleteRequest by remember { mutableStateOf<List<File>?>(null) }
    var archivePrompt by remember { mutableStateOf<File?>(null) }
    var busy by remember { mutableStateOf(false) }

    val failedText = stringResource(R.string.cleave_files_failed)
    val doneText = stringResource(R.string.cleave_files_done)

    fun runOp(block: suspend () -> Boolean) {
        scope.launch {
            busy = true
            val ok = block()
            busy = false
            Toast.makeText(context, if (ok) doneText else failedText, Toast.LENGTH_SHORT).show()
            selection = emptySet()
            revision++
        }
    }

    LaunchedEffect(currentPath, sortField, descending, foldersFirst, showHidden, revision) {
        loading = true
        current.mkdirs()
        items = PojavFileBridge.list(current, FileSort(sortField, descending, foldersFirst), showHidden)
        loading = false
    }
    LaunchedEffect(query, currentPath, showHidden, revision) {
        if (query.isBlank()) {
            searchResults = null
        } else {
            delay(300)
            searchResults = PojavFileBridge.search(current, query.trim(), showHidden)
        }
    }
    LaunchedEffect(currentPath) { selection = emptySet() }

    val atRoot = current.absolutePath == root.dir.absolutePath
    BackHandler(enabled = editing == null && (showTrash || selection.isNotEmpty() || query.isNotEmpty() || !atRoot)) {
        when {
            showTrash -> showTrash = false
            selection.isNotEmpty() -> selection = emptySet()
            query.isNotEmpty() -> query = ""
            else -> current.parentFile?.let { currentPath = it.absolutePath }
        }
    }

    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) runOp { PojavFileBridge.import(context, uris, current) == uris.size }
    }

    editing?.let { file ->
        FileEditorPane(file = file, onClose = {
            editing = null
            revision++
        })
        return
    }

    nameRequest?.let { request ->
        NameDialog(request = request, onDismiss = { nameRequest = null }, onConfirm = { name ->
            nameRequest = null
            runOp { request.onConfirm(name) }
        })
    }
    deleteRequest?.let { files ->
        DeleteDialog(count = files.size, onDismiss = { deleteRequest = null }, onConfirm = { permanent ->
            deleteRequest = null
            runOp {
                val done = if (permanent) PojavFileBridge.deletePermanently(files) else PojavFileBridge.trash(files)
                done == files.size
            }
        })
    }
    archivePrompt?.let { archive ->
        AlertDialog(
            onDismissRequest = { archivePrompt = null },
            containerColor = CleaveSurface.panelRaised,
            title = { Text(archive.name, color = CleaveSurface.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = { Text(stringResource(R.string.cleave_files_archive_prompt), color = CleaveSurface.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    archivePrompt = null
                    runOp { PojavFileBridge.extract(archive) != null }
                }) { Text(stringResource(R.string.cleave_files_extract), color = accent) }
            },
            dismissButton = {
                TextButton(onClick = {
                    archivePrompt = null
                    PojavFileBridge.open(activity, archive, false)
                }) { Text(stringResource(R.string.cleave_files_open_with), color = CleaveSurface.textSecondary) }
            }
        )
    }

    val compressDefault = stringResource(R.string.cleave_files_archive_default)

    CleavePage(
        title = stringResource(if (showTrash) R.string.cleave_files_trash else R.string.cleave_files_title),
        subtitle = if (showTrash) stringResource(R.string.cleave_files_trash_sub) else current.absolutePath,
        trailing = {
            Row(horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)) {
                CleaveGhostButton(
                    stringResource(if (showTrash) R.string.cleave_files_title else R.string.cleave_files_trash),
                    if (showTrash) R.drawable.cleave_ic_folder_outlined else R.drawable.cleave_ic_delete_outlined,
                    { showTrash = !showTrash }
                )
                if (!showTrash) {
                    CleavePrimaryButton(stringResource(R.string.cleave_files_import), R.drawable.cleave_ic_upload, {
                        importer.launch(arrayOf("*" + "/" + "*"))
                    })
                }
            }
        }
    ) {
        if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = accent, trackColor = CleaveSurface.panelTrack)
        if (showTrash) {
            TrashPane(onChanged = { revision++ })
            return@CleavePage
        }

        if (roots.size > 1) {
            PillTabs(
                tabs = roots.map { stringResource(it.titleRes) },
                selected = rootIndex.coerceIn(0, roots.size - 1),
                onSelect = {
                    rootIndex = it
                    query = ""
                },
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }

        // Toolbar: up, breadcrumb, sort and creation actions.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
        ) {
            IconButton(onClick = { current.parentFile?.let { currentPath = it.absolutePath } }, enabled = !atRoot) {
                Icon(painterResource(R.drawable.cleave_ic_arrow_back), stringResource(R.string.cleave_files_up),
                    tint = if (atRoot) CleaveSurface.textMuted else CleaveSurface.textPrimary)
            }
            Breadcrumb(root = root.dir, current = current, rootLabel = stringResource(root.titleRes), modifier = Modifier.weight(1f)) {
                currentPath = it.absolutePath
            }
            Box {
                IconButton(onClick = { sortMenu = true }) {
                    Icon(painterResource(R.drawable.cleave_ic_sort), stringResource(R.string.cleave_files_sort), tint = CleaveSurface.textPrimary)
                }
                DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }, containerColor = CleaveSurface.panelRaised) {
                    listOf(
                        FileSortField.NAME to R.string.cleave_files_sort_name,
                        FileSortField.SIZE to R.string.cleave_files_sort_size,
                        FileSortField.DATE to R.string.cleave_files_sort_date,
                        FileSortField.TYPE to R.string.cleave_files_sort_type
                    ).forEach { (field, label) ->
                        DropdownMenuItem(
                            text = { Text(stringResource(label), color = if (sortField == field) accent else CleaveSurface.textPrimary) },
                            onClick = { sortField = field }
                        )
                    }
                    HorizontalDivider(color = CleaveSurface.borderSoft)
                    MenuCheck(stringResource(R.string.cleave_files_sort_desc), descending) { descending = it }
                    MenuCheck(stringResource(R.string.cleave_files_folders_first), foldersFirst) { foldersFirst = it }
                    MenuCheck(stringResource(R.string.cleave_files_show_hidden), showHidden) { showHidden = it }
                }
            }
            IconButton(onClick = {
                nameRequest = NameRequest(R.string.cleave_files_new_folder, "") { PojavFileBridge.createFolder(current, it) }
            }) {
                Icon(painterResource(R.drawable.cleave_ic_create_new_folder_filled), stringResource(R.string.cleave_files_new_folder), tint = CleaveSurface.textPrimary)
            }
            IconButton(onClick = {
                nameRequest = NameRequest(R.string.cleave_files_new_file, "") { PojavFileBridge.createFile(current, it) }
            }) {
                Icon(painterResource(R.drawable.cleave_ic_add), stringResource(R.string.cleave_files_new_file), tint = CleaveSurface.textPrimary)
            }
            IconButton(onClick = { revision++ }) {
                Icon(painterResource(R.drawable.cleave_ic_refresh), stringResource(R.string.cleave_files_refresh), tint = CleaveSurface.textPrimary)
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text(stringResource(R.string.cleave_files_search), color = CleaveSurface.textMuted) },
            leadingIcon = { Icon(painterResource(R.drawable.cleave_ic_search), null, tint = CleaveSurface.textSecondary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(painterResource(R.drawable.cleave_ic_close), null, tint = CleaveSurface.textSecondary)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CleaveRadii.control),
            colors = cleaveFieldColors()
        )

        val clip = FmClipboard.files
        if (selection.isNotEmpty()) {
            ActionBar {
                Text(
                    stringResource(R.string.cleave_files_selected, selection.size),
                    color = CleaveSurface.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = CleaveSpacing.sm)
                )
                BarIcon(R.drawable.cleave_ic_select_all, R.string.cleave_files_select_all) {
                    selection = (searchResults ?: items).orEmpty().map { it.file }.toSet()
                }
                BarIcon(R.drawable.cleave_ic_deselect, R.string.cleave_files_clear_selection) { selection = emptySet() }
                BarIcon(R.drawable.cleave_ic_content_copy_filled, R.string.cleave_files_copy) {
                    FmClipboard.files = selection.toList()
                    FmClipboard.move = false
                    selection = emptySet()
                }
                BarIcon(R.drawable.cleave_ic_content_cut, R.string.cleave_files_cut) {
                    FmClipboard.files = selection.toList()
                    FmClipboard.move = true
                    selection = emptySet()
                }
                if (selection.size == 1) {
                    val single = selection.first()
                    BarIcon(R.drawable.cleave_ic_edit_outlined, R.string.cleave_files_rename) {
                        nameRequest = NameRequest(R.string.cleave_files_rename, single.name) { PojavFileBridge.rename(single, it) }
                    }
                    if (single.isFile) {
                        BarIcon(R.drawable.cleave_ic_share_filled, R.string.cleave_files_share) {
                            PojavFileBridge.open(activity, single, true)
                            selection = emptySet()
                        }
                    }
                    if (single.isFile && PojavFileBridge.isArchive(single)) {
                        BarIcon(R.drawable.cleave_ic_unarchive_outlined, R.string.cleave_files_extract) {
                            runOp { PojavFileBridge.extract(single) != null }
                        }
                    }
                }
                BarIcon(R.drawable.cleave_ic_archive_filled, R.string.cleave_files_compress) {
                    val files = selection.toList()
                    val initial = if (files.size == 1) files[0].nameWithoutExtension else compressDefault
                    nameRequest = NameRequest(R.string.cleave_files_compress, initial) { PojavFileBridge.compress(files, current, it) != null }
                }
                BarIcon(R.drawable.cleave_ic_delete_outlined, R.string.cleave_files_delete, CleaveSurface.danger) {
                    deleteRequest = selection.toList()
                }
            }
        } else if (clip.isNotEmpty()) {
            ActionBar {
                Text(
                    stringResource(if (FmClipboard.move) R.string.cleave_files_clip_move else R.string.cleave_files_clip_copy, clip.size),
                    color = CleaveSurface.textPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = CleaveSpacing.sm)
                )
                CleaveGhostButton(stringResource(R.string.cleave_files_paste), R.drawable.cleave_ic_content_paste, {
                    val files = clip
                    val move = FmClipboard.move
                    FmClipboard.files = emptyList()
                    runOp { PojavFileBridge.paste(files, current, move) == files.size }
                })
                BarIcon(R.drawable.cleave_ic_close, R.string.cleave_settings_cancel) { FmClipboard.files = emptyList() }
            }
        }

        val shown = searchResults ?: items
        when {
            shown == null -> QuietEmptyState(R.drawable.cleave_ic_warning_outlined, stringResource(R.string.cleave_files_unreadable))
            loading && shown.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accent)
            }
            shown.isEmpty() -> QuietEmptyState(R.drawable.cleave_ic_folder_outlined, stringResource(
                if (searchResults != null) R.string.cleave_files_no_match else R.string.cleave_files_empty
            ))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = CleaveSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
            ) {
                items(shown, key = { it.file.absolutePath }) { item ->
                    FileRow(
                        item = item,
                        selected = item.file in selection,
                        onClick = {
                            when {
                                selection.isNotEmpty() -> selection = if (item.file in selection) selection - item.file else selection + item.file
                                item.isDirectory -> {
                                    query = ""
                                    currentPath = item.file.absolutePath
                                }
                                PojavFileBridge.isArchive(item.file) -> archivePrompt = item.file
                                PojavFileBridge.isText(item.file) && item.size <= PojavFileBridge.MAX_EDIT_BYTES -> editing = item.file
                                else -> PojavFileBridge.open(activity, item.file, false)
                            }
                        },
                        onLongClick = {
                            selection = if (item.file in selection) selection - item.file else selection + item.file
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuCheck(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val accent = CleaveThemeManager.currentAccent()
    DropdownMenuItem(
        text = { Text(label, color = CleaveSurface.textPrimary) },
        trailingIcon = {
            Checkbox(
                checked = checked,
                onCheckedChange = onChange,
                colors = CheckboxDefaults.colors(checkedColor = accent, uncheckedColor = CleaveSurface.textSecondary, checkmarkColor = CleaveSurface.onAccent)
            )
        },
        onClick = { onChange(!checked) }
    )
}

@Composable
private fun ActionBar(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(CleaveRadii.panelSmall)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CleaveSurface.panelRaised.copy(alpha = 0.35f), shape)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = CleaveSpacing.xs, vertical = CleaveSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xxs)
    ) { content() }
}

@Composable
private fun BarIcon(iconRes: Int, labelRes: Int, tint: Color = CleaveSurface.textPrimary, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(painterResource(iconRes), stringResource(labelRes), tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Breadcrumb(root: File, current: File, rootLabel: String, modifier: Modifier, onNavigate: (File) -> Unit) {
    val accent = CleaveThemeManager.currentAccent()
    val chain = remember(root, current) {
        val list = ArrayList<File>()
        var cursor: File? = current
        val rootPath = root.absolutePath
        while (cursor != null) {
            list.add(0, cursor)
            if (cursor.absolutePath == rootPath) break
            cursor = cursor.parentFile
        }
        list
    }
    val scroll = rememberScrollState()
    LaunchedEffect(chain.size) { scroll.animateScrollTo(scroll.maxValue) }
    Row(
        modifier = modifier.horizontalScroll(scroll),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chain.forEachIndexed { index, dir ->
            if (index > 0) Text("/", color = CleaveSurface.textMuted, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 2.dp))
            val last = index == chain.size - 1
            Text(
                text = if (index == 0) rootLabel else dir.name,
                color = if (last) accent else CleaveSurface.textSecondary,
                fontSize = 13.sp,
                fontWeight = if (last) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !last) { onNavigate(dir) }
                    .padding(horizontal = CleaveSpacing.xs, vertical = CleaveSpacing.xxs)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(item: FileItem, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val context = LocalContext.current
    val accent = CleaveThemeManager.currentAccent()
    val shape = RoundedCornerShape(CleaveRadii.panelSmall)
    val date = remember(item.lastModified) {
        val d = Date(item.lastModified)
        DateFormat.getMediumDateFormat(context).format(d) + " " + DateFormat.getTimeFormat(context).format(d)
    }
    val meta = if (item.isDirectory) {
        if (item.childCount >= 0) context.resources.getQuantityString(R.plurals.cleave_files_items, item.childCount, item.childCount) + " · " + date
        else date
    } else {
        Formatter.formatShortFileSize(context, item.size) + " · " + date
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.22f) else CleaveSurface.panelRaised.copy(alpha = 0.21f), shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
    ) {
        Icon(
            painter = painterResource(if (selected) R.drawable.cleave_ic_check else fileIcon(item.file, item.isDirectory)),
            contentDescription = null,
            tint = if (selected || item.isDirectory) accent else CleaveSurface.textSecondary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, color = CleaveSurface.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(meta, color = CleaveSurface.textMuted, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun NameDialog(request: NameRequest, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val accent = CleaveThemeManager.currentAccent()
    var text by remember(request) { mutableStateOf(request.initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CleaveSurface.panelRaised,
        title = { Text(stringResource(request.titleRes), color = CleaveSurface.textPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text(stringResource(R.string.cleave_files_name), color = CleaveSurface.textSecondary) },
                colors = cleaveFieldColors()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank()) {
                Text(stringResource(R.string.cleave_files_ok), color = accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cleave_settings_cancel), color = CleaveSurface.textSecondary) }
        }
    )
}

@Composable
private fun DeleteDialog(count: Int, onDismiss: () -> Unit, onConfirm: (Boolean) -> Unit) {
    val accent = CleaveThemeManager.currentAccent()
    var permanent by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CleaveSurface.panelRaised,
        title = { Text(stringResource(R.string.cleave_files_delete), color = CleaveSurface.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)) {
                Text(stringResource(R.string.cleave_files_delete_confirm, count), color = CleaveSurface.textSecondary)
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { permanent = !permanent },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = permanent,
                        onCheckedChange = { permanent = it },
                        colors = CheckboxDefaults.colors(checkedColor = accent, uncheckedColor = CleaveSurface.textSecondary, checkmarkColor = CleaveSurface.onAccent)
                    )
                    Text(stringResource(R.string.cleave_files_delete_permanent), color = CleaveSurface.textPrimary, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(permanent) }) { Text(stringResource(R.string.cleave_files_delete), color = CleaveSurface.danger) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cleave_settings_cancel), color = CleaveSurface.textSecondary) }
        }
    )
}

@Composable
private fun ColumnScope.TrashPane(onChanged: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accent = CleaveThemeManager.currentAccent()
    var entries by remember { mutableStateOf<List<TrashEntry>?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    var confirmEmpty by remember { mutableStateOf(false) }
    val failedText = stringResource(R.string.cleave_files_failed)

    LaunchedEffect(revision) { entries = PojavFileBridge.trashEntries() }

    fun op(block: suspend () -> Boolean) {
        scope.launch {
            if (!block()) Toast.makeText(context, failedText, Toast.LENGTH_SHORT).show()
            revision++
            onChanged()
        }
    }

    if (confirmEmpty) {
        AlertDialog(
            onDismissRequest = { confirmEmpty = false },
            containerColor = CleaveSurface.panelRaised,
            title = { Text(stringResource(R.string.cleave_files_empty_trash), color = CleaveSurface.textPrimary) },
            text = { Text(stringResource(R.string.cleave_files_empty_trash_confirm), color = CleaveSurface.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmEmpty = false
                    op { PojavFileBridge.emptyTrash() }
                }) { Text(stringResource(R.string.cleave_files_empty_trash), color = CleaveSurface.danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmEmpty = false }) { Text(stringResource(R.string.cleave_settings_cancel), color = CleaveSurface.textSecondary) }
            }
        )
    }

    val list = entries
    when {
        list == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accent) }
        list.isEmpty() -> QuietEmptyState(R.drawable.cleave_ic_delete_outlined, stringResource(R.string.cleave_files_trash_empty))
        else -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                CleaveGhostButton(stringResource(R.string.cleave_files_empty_trash), R.drawable.cleave_ic_delete_outlined, { confirmEmpty = true })
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = CleaveSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
            ) {
                items(list, key = { it.file.absolutePath }) { entry ->
                    val shape = RoundedCornerShape(CleaveRadii.panelSmall)
                    val date = remember(entry.deletedAt) {
                        val d = Date(entry.deletedAt)
                        DateFormat.getMediumDateFormat(context).format(d) + " " + DateFormat.getTimeFormat(context).format(d)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(CleaveSurface.panelRaised.copy(alpha = 0.21f), shape)
                            .padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
                    ) {
                        Icon(painterResource(fileIcon(entry.file, entry.isDirectory)), null, tint = CleaveSurface.textSecondary, modifier = Modifier.size(22.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.file.name, color = CleaveSurface.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(entry.originalPath, color = CleaveSurface.textMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(Formatter.formatShortFileSize(context, entry.size) + " · " + date, color = CleaveSurface.textMuted, fontSize = 11.sp, maxLines = 1)
                        }
                        IconButton(onClick = { op { PojavFileBridge.restore(entry) } }) {
                            Icon(painterResource(R.drawable.cleave_ic_restore_from_trash_filled), stringResource(R.string.cleave_files_restore), tint = accent)
                        }
                        IconButton(onClick = { op { PojavFileBridge.purge(entry) } }) {
                            Icon(painterResource(R.drawable.cleave_ic_delete_outlined), stringResource(R.string.cleave_files_delete_permanent), tint = CleaveSurface.danger)
                        }
                    }
                }
            }
        }
    }
}

/** Text editor for configs and logs (mirai FmEditorScreen: save, word wrap, find, replace, go to line). */
@Composable
private fun FileEditorPane(file: File, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accent = CleaveThemeManager.currentAccent()
    var original by remember(file) { mutableStateOf<String?>(null) }
    var value by remember(file) { mutableStateOf(TextFieldValue("")) }
    var loadFailed by remember(file) { mutableStateOf(false) }
    var wrap by rememberSaveable { mutableStateOf(true) }
    var findOpen by remember { mutableStateOf(false) }
    var find by remember { mutableStateOf("") }
    var replace by remember { mutableStateOf("") }
    var gotoOpen by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val savedText = stringResource(R.string.cleave_files_saved)
    val failedText = stringResource(R.string.cleave_files_failed)
    val notFoundText = stringResource(R.string.cleave_files_find_none)

    LaunchedEffect(file) {
        val text = PojavFileBridge.readText(file)
        if (text == null) loadFailed = true else {
            original = text
            value = TextFieldValue(text)
        }
    }
    val dirty = original != null && value.text != original
    val lines = remember(value.text) { value.text.count { it == '\n' } + 1 }
    val matches = remember(value.text, find) {
        if (find.isEmpty()) 0 else Regex(Regex.escape(find), RegexOption.IGNORE_CASE).findAll(value.text).count()
    }

    fun save() {
        scope.launch {
            saving = true
            val ok = PojavFileBridge.writeText(file, value.text)
            saving = false
            if (ok) original = value.text
            Toast.makeText(context, if (ok) savedText else failedText, Toast.LENGTH_SHORT).show()
        }
    }

    fun findNext() {
        if (find.isEmpty()) return
        val text = value.text
        var index = text.indexOf(find, value.selection.max, ignoreCase = true)
        if (index < 0) index = text.indexOf(find, 0, ignoreCase = true)
        if (index < 0) {
            Toast.makeText(context, notFoundText, Toast.LENGTH_SHORT).show()
        } else {
            value = value.copy(selection = TextRange(index, index + find.length))
        }
    }

    fun replaceCurrent() {
        val sel = value.selection
        val text = value.text
        if (find.isNotEmpty() && !sel.collapsed && text.substring(sel.min, sel.max).equals(find, ignoreCase = true)) {
            val updated = text.substring(0, sel.min) + replace + text.substring(sel.max)
            value = TextFieldValue(updated, TextRange(sel.min + replace.length))
        }
        findNext()
    }

    fun replaceAll() {
        if (find.isEmpty()) return
        val updated = value.text.replace(find, replace, ignoreCase = true)
        value = TextFieldValue(updated, TextRange(0))
    }

    BackHandler { if (dirty) confirmDiscard = true else onClose() }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            containerColor = CleaveSurface.panelRaised,
            title = { Text(stringResource(R.string.cleave_files_unsaved), color = CleaveSurface.textPrimary) },
            text = { Text(stringResource(R.string.cleave_files_unsaved_confirm), color = CleaveSurface.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    onClose()
                }) { Text(stringResource(R.string.cleave_files_discard), color = CleaveSurface.danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text(stringResource(R.string.cleave_settings_cancel), color = CleaveSurface.textSecondary) }
            }
        )
    }
    if (gotoOpen) {
        var line by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { gotoOpen = false },
            containerColor = CleaveSurface.panelRaised,
            title = { Text(stringResource(R.string.cleave_files_goto_line), color = CleaveSurface.textPrimary) },
            text = {
                OutlinedTextField(
                    value = line,
                    onValueChange = { line = it.filter(Char::isDigit) },
                    singleLine = true,
                    label = { Text("1 – $lines", color = CleaveSurface.textSecondary) },
                    colors = cleaveFieldColors()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    gotoOpen = false
                    val target = (line.toIntOrNull() ?: 1).coerceIn(1, lines)
                    var offset = 0
                    repeat(target - 1) {
                        val next = value.text.indexOf('\n', offset)
                        if (next >= 0) offset = next + 1
                    }
                    value = value.copy(selection = TextRange(offset))
                }) { Text(stringResource(R.string.cleave_files_ok), color = accent) }
            },
            dismissButton = {
                TextButton(onClick = { gotoOpen = false }) { Text(stringResource(R.string.cleave_settings_cancel), color = CleaveSurface.textSecondary) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(net.kdt.pojavlaunch.cleave.theme.CleaveLayoutDefaults.pagePadding()),
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)) {
            IconButton(onClick = { if (dirty) confirmDiscard = true else onClose() }) {
                Icon(painterResource(R.drawable.cleave_ic_arrow_back), stringResource(R.string.cleave_files_close), tint = CleaveSurface.textPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    (if (dirty) "• " else "") + file.name,
                    color = CleaveSurface.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    context.resources.getQuantityString(R.plurals.cleave_files_lines, lines, lines) + " · " + file.parent.orEmpty(),
                    color = CleaveSurface.textMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = { findOpen = !findOpen }) {
                Icon(painterResource(R.drawable.cleave_ic_search), stringResource(R.string.cleave_files_find), tint = if (findOpen) accent else CleaveSurface.textPrimary)
            }
            IconButton(onClick = { gotoOpen = true }) {
                Icon(painterResource(R.drawable.cleave_ic_more_vert), stringResource(R.string.cleave_files_goto_line), tint = CleaveSurface.textPrimary)
            }
            IconButton(onClick = { wrap = !wrap }) {
                Icon(painterResource(R.drawable.cleave_ic_text_snippet_filled), stringResource(R.string.cleave_files_wrap), tint = if (wrap) accent else CleaveSurface.textSecondary)
            }
            CleavePrimaryButton(stringResource(R.string.cleave_files_save), R.drawable.cleave_ic_check, { save() }, enabled = dirty && !saving)
        }
        if (findOpen) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)) {
                OutlinedTextField(
                    value = find,
                    onValueChange = { find = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.cleave_files_find), color = CleaveSurface.textMuted) },
                    modifier = Modifier.weight(1f),
                    colors = cleaveFieldColors()
                )
                OutlinedTextField(
                    value = replace,
                    onValueChange = { replace = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.cleave_files_replace), color = CleaveSurface.textMuted) },
                    modifier = Modifier.weight(1f),
                    colors = cleaveFieldColors()
                )
                Text(matches.toString(), color = CleaveSurface.textMuted, fontSize = 12.sp)
                TextButton(onClick = { findNext() }) { Text(stringResource(R.string.cleave_files_find_next), color = accent) }
                TextButton(onClick = { replaceCurrent() }) { Text(stringResource(R.string.cleave_files_replace), color = accent) }
                TextButton(onClick = { replaceAll() }) { Text(stringResource(R.string.cleave_files_replace_all), color = accent) }
            }
        }
        val shape = RoundedCornerShape(CleaveRadii.panelSmall)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(CleaveSurface.canvas.copy(alpha = 0.72f), shape)
                .padding(CleaveSpacing.sm)
        ) {
            when {
                loadFailed -> QuietEmptyState(R.drawable.cleave_ic_warning_outlined, stringResource(R.string.cleave_files_unreadable))
                original == null -> CircularProgressIndicator(color = accent, modifier = Modifier.align(Alignment.Center))
                else -> {
                    val verticalScroll = rememberScrollState()
                    val horizontalScroll = rememberScrollState()
                    val scrollModifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                        .let { if (wrap) it else it.horizontalScroll(horizontalScroll) }
                    Box(modifier = scrollModifier) {
                        BasicTextField(
                            value = value,
                            onValueChange = { value = it },
                            textStyle = TextStyle(color = CleaveSurface.textPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                            cursorBrush = SolidColor(accent),
                            modifier = if (wrap) Modifier.fillMaxWidth() else Modifier
                        )
                    }
                }
            }
        }
    }
}
