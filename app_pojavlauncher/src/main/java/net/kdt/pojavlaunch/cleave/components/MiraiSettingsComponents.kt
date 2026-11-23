/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Ported from mirai-launcher- (Zalith Launcher 2 fork):
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/settings/layouts/_SettingsCard.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/settings/layouts/List.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/settings/layouts/Slider.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/settings/layouts/TextInput.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/settings/layouts/Enum.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/versions/layouts/VersionSettingsLayout.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/components/Sliders.kt
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

package net.kdt.pojavlaunch.cleave.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.PojavPluginBridge
import net.kdt.pojavlaunch.game.renderer.def.Renderers
import net.kdt.pojavlaunch.cleave.theme.CleaveMetrics
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.game.renderer.RendererCache
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import kotlin.math.roundToInt

/** Mirai CleavePillTabRow: liquid-glass pill bar used by Settings, Discover and Instance Management. */
@Composable
fun CleavePillTabRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = CleaveSpacing.xs, vertical = CleaveSpacing.xs),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .liquidGlass(
                shape = CircleShape,
                tint = CleaveSurface.glassTint,
                strength = 0.72f,
                elevation = CleaveMetrics.glassSelectedElevation
            )
            .selectableGroup()
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** Mirai CleavePillTab: one pill item inside [CleavePillTabRow]. */
@Composable
fun CleavePillTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val activeAccent = CleaveThemeManager.currentAccent()
    val outline by animateColorAsState(
        targetValue = if (selected) CleaveSurface.borderHighlight else Color.Transparent,
        animationSpec = tween(durationMillis = 160),
        label = "cleaveTabOutline"
    )
    val contentColor = if (selected) activeAccent else CleaveSurface.textSecondary
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = CleaveMetrics.pillTabHeight)
            .then(
                if (selected) {
                    Modifier.liquidGlass(
                        shape = CircleShape,
                        tint = activeAccent,
                        strength = 0.95f,
                        elevation = CleaveMetrics.glassSelectedElevation
                    )
                } else Modifier
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        color = Color.Transparent,
        contentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.48f),
        border = BorderStroke(CleaveSpacing.hairline, outline),
        shadowElevation = if (selected) CleaveSpacing.xs else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CleaveSpacing.md, vertical = CleaveSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/** Mirai ModrinthMetaPill: small badge used in Renderer Settings and Instance Management. */
@Composable
fun ModrinthMetaPill(
    text: String,
    highlighted: Boolean = false,
    backgroundColor: Color = if (highlighted) CleaveSurface.accentContainer else CleaveSurface.panelRaised,
    textColor: Color = if (highlighted) CleaveSurface.modrinthBrand else CleaveSurface.textSecondary,
    borderColor: Color? = if (highlighted) CleaveSurface.modrinthBrand.copy(alpha = 0.45f) else null
) {
    val shape = RoundedCornerShape(CleaveRadii.compact)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(backgroundColor)
            .then(if (borderColor != null) Modifier.border(CleaveSpacing.hairline, borderColor, shape) else Modifier)
            .padding(horizontal = CleaveSpacing.smTight, vertical = CleaveSpacing.xxs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1
        )
    }
}

/** Compact Modrinth-style search bar with leading search icon and optional clear button. */
@Composable
fun ModrinthCompactSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(CleaveRadii.controlSmall)
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(CleaveSurface.panel)
            .border(CleaveSpacing.hairline, CleaveSurface.borderSoft, shape)
            .padding(horizontal = CleaveSpacing.smPlus),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.smCompact)
    ) {
        Icon(
            painter = painterResource(R.drawable.cleave_ic_search),
            contentDescription = null,
            tint = CleaveSurface.textSecondary,
            modifier = Modifier.size(16.dp)
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = CleaveSurface.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                cursorBrush = SolidColor(CleaveThemeManager.currentAccent()),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                painter = painterResource(R.drawable.cleave_ic_close),
                contentDescription = null,
                tint = CleaveSurface.textSecondary,
                modifier = Modifier
                    .size(15.dp)
                    .clickable { onValueChange("") }
            )
        }
    }
}

/** Mirai DefaultSwitch: M3 Switch with the rotating check/close icon inside the thumb. */
@Composable
fun DefaultSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(
        checkedThumbColor = CleaveSurface.onAccent,
        checkedTrackColor = CleaveThemeManager.currentAccent(),
        uncheckedThumbColor = CleaveSurface.textSecondary,
        uncheckedTrackColor = CleaveSurface.panelTrack,
        uncheckedBorderColor = CleaveSurface.borderSoft
    )
) {
    val trackColor = if (checked) CleaveThemeManager.currentAccent() else CleaveSurface.panelTrack
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        thumbContent = {
            val rotation by animateFloatAsState(if (checked) 0f else -180f, label = "switchThumbRotation")
            Crossfade(modifier = Modifier.rotate(rotation), targetState = checked, label = "switchThumbIcon") { state ->
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(if (state) R.drawable.cleave_ic_check else R.drawable.cleave_ic_close),
                    contentDescription = null,
                    tint = trackColor
                )
            }
        },
        colors = colors
    )
}

/** Mirai CardPosition + rememberSettingsCardShape: grouped settings cards with 32dp outer corners and 4dp inner seams. */
enum class CardPosition { Top, Middle, Bottom, Single }

@Composable
fun rememberSettingsCardShape(
    position: CardPosition,
    outerShape: Dp = CleaveRadii.hero,
    innerShape: Dp = CleaveRadii.tiny
): Shape = remember(position, outerShape, innerShape) {
    when (position) {
        CardPosition.Top -> RoundedCornerShape(
            topStart = outerShape, topEnd = outerShape,
            bottomStart = innerShape, bottomEnd = innerShape
        )
        CardPosition.Middle -> RoundedCornerShape(innerShape)
        CardPosition.Bottom -> RoundedCornerShape(
            topStart = innerShape, topEnd = innerShape,
            bottomStart = outerShape, bottomEnd = outerShape
        )
        CardPosition.Single -> RoundedCornerShape(outerShape)
    }
}

@Composable
fun SettingsCardColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xxs),
        content = content
    )
}

/** Base Mirai SettingsCard surface (`_SettingsCard.kt`). */
@Composable
fun MiraiSettingsCard(
    position: CardPosition,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = rememberSettingsCardShape(position)
    Surface(
        modifier = modifier.liquidGlass(shape = shape, strength = 0.55f, elevation = CleaveSpacing.zero),
        shape = shape,
        color = CleaveSurface.panel,
        contentColor = Color.White,
        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft)
    ) {
        val clickMod = if (onClick != null) {
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(innerPadding)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        }
        Column(modifier = clickMod, content = content)
    }
}

@Composable
fun MiraiTitleAndSummary(
    title: String,
    summary: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White
        )
        if (!summary.isNullOrEmpty()) {
            Text(
                text = summary,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.70f)
            )
        }
    }
}

/** Mirai SwitchSettingsCard (`Switch.kt`). */
@Composable
fun MiraiSwitchSettingsCard(
    title: String,
    summary: String? = null,
    checked: Boolean,
    position: CardPosition = CardPosition.Single,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    MiraiSettingsCard(
        position = position,
        modifier = Modifier.fillMaxWidth(),
        onClick = if (enabled) ({ onCheckedChange(!checked) }) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = CleaveSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiraiTitleAndSummary(
                title = title,
                summary = summary,
                modifier = Modifier.weight(1f)
            )
            DefaultSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

/**
 * Mirai inline-expanding ListSettingsCard (`List.kt` — Photos 1, 2, 3, and 5).
 * Tapping the header expands an animated radio list directly inside the card rather than opening a popup dialog.
 */
@Composable
fun <E> MiraiListSettingsCard(
    items: List<E>,
    currentId: String,
    defaultId: String,
    title: String,
    position: CardPosition = CardPosition.Single,
    modifier: Modifier = Modifier,
    summary: String? = null,
    getItemText: @Composable (E) -> String,
    getItemId: (E) -> String,
    getItemSummary: (@Composable (E) -> Unit)? = null,
    enabled: Boolean = true,
    autoCollapse: Boolean = true,
    selectedIdText: (@Composable () -> Unit)? = null,
    middleLayout: (@Composable ColumnScope.() -> Unit)? = null,
    trailingIcon: (@Composable RowScope.() -> Unit)? = null,
    onValueChange: (E) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedItem = remember(items, currentId, defaultId) {
        items.firstOrNull { getItemId(it) == currentId }
            ?: items.firstOrNull { getItemId(it) == defaultId }
            ?: items.firstOrNull()
    }
    val selectedText = if (selectedItem != null) getItemText(selectedItem) else currentId

    MiraiSettingsCard(
        modifier = modifier.fillMaxWidth(),
        position = position
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { expanded = !expanded }
                    .padding(all = CleaveSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MiraiTitleAndSummary(
                        title = title,
                        summary = summary
                    )
                    middleLayout?.invoke(this)
                    if (selectedIdText != null) {
                        selectedIdText()
                    } else {
                        Text(
                            modifier = Modifier.alpha(0.7f),
                            text = "Selected: $selectedText",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    trailingIcon?.invoke(this)
                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) -180f else 0f,
                        animationSpec = tween(durationMillis = 220),
                        label = "listChevronRotation"
                    )
                    IconButton(
                        modifier = Modifier
                            .size(34.dp)
                            .rotate(rotation),
                        enabled = enabled,
                        onClick = { expanded = !expanded }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_arrow_drop_down_rounded),
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Color.White
                        )
                    }
                }
            }

            val optionListState = rememberLazyListState()
            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = expanded,
                enter = expandVertically(animationSpec = tween(durationMillis = 220)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 200)) + fadeOut()
            ) {
                LazyColumn(
                    state = optionListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .padding(
                            start = CleaveSpacing.sm,
                            end = CleaveSpacing.sm,
                            bottom = CleaveSpacing.sm
                        ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(items) { _, item ->
                        val itemId = getItemId(item)
                        val isSelected = itemId == currentId || (items.none { getItemId(it) == currentId } && item == selectedItem)
                        val itemName = getItemText(item)
                        MiraiSimpleListItem(
                            selected = isSelected,
                            itemName = itemName,
                            summary = getItemSummary?.let { summarySlot ->
                                { summarySlot.invoke(item) }
                            },
                            onClick = {
                                if (autoCollapse) expanded = false
                                onValueChange(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Radio item inside `MiraiListSettingsCard` (`SimpleListItem` from `Layouts.kt`). */
@Composable
fun MiraiSimpleListItem(
    selected: Boolean,
    itemName: String,
    modifier: Modifier = Modifier,
    summary: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val accent = CleaveThemeManager.currentAccent()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.large)
            .bringFocusedItemIntoView()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = CleaveSpacing.sm, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = accent,
                unselectedColor = Color.White.copy(alpha = 0.70f)
            )
        )
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = itemName,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
            summary?.invoke()
        }
    }
}

/**
 * Mirai SimpleTextSlider (`Sliders.kt` — Photo 4):
 * Thick pill slider on the left + cyan badge showing `<value><suffix> < >` with fine-tuning buttons and tap-to-input dialog.
 */
@Composable
fun MiraiSimpleTextSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: () -> Unit = {},
    toInt: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    suffix: String? = null,
    enabled: Boolean = true,
    fineTuningControl: Boolean = false,
    fineTuningStep: Float = 1f,
    appendContent: @Composable RowScope.() -> Unit = {}
) {
    val accent = CleaveThemeManager.currentAccent()
    var showEditDialog by remember { mutableStateOf(false) }
    var editInput by remember(value) {
        mutableStateOf(if (toInt) value.roundToInt().toString() else String.format("%.2f", value))
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = CleaveSurface.panelRaised,
            title = { Text("Enter Value", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = editInput,
                    onValueChange = { editInput = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = {
                        Text(
                            "Range: ${valueRange.start.roundToInt()} - ${valueRange.endInclusive.roundToInt()}${suffix ?: ""}",
                            color = CleaveSurface.textSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = accent,
                        unfocusedBorderColor = CleaveSurface.borderSoft
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsed = editInput.trim().toFloatOrNull()
                    if (parsed != null) {
                        val clamped = parsed.coerceIn(valueRange)
                        onValueChange(clamped)
                        onValueChangeFinished()
                    }
                    showEditDialog = false
                }) {
                    Text("OK", color = accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = CleaveSurface.textSecondary)
                }
            }
        )
    }

    fun changeValue(newVal: Float, finished: Boolean) {
        val next = (if (toInt) newVal.roundToInt().toFloat() else newVal).coerceIn(valueRange)
        onValueChange(next)
        if (finished) onValueChangeFinished()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Slider(
            value = value.coerceIn(valueRange),
            onValueChange = { changeValue(it, false) },
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = CleaveSurface.panelTrack,
                disabledThumbColor = accent.copy(alpha = 0.45f),
                disabledActiveTrackColor = accent.copy(alpha = 0.45f),
                disabledInactiveTrackColor = CleaveSurface.panelTrack.copy(alpha = 0.5f)
            ),
            modifier = Modifier.weight(1f)
        )
        Surface(
            modifier = Modifier
                .padding(start = 12.dp)
                .animateContentSize(),
            color = if (enabled) accent else accent.copy(alpha = 0.40f),
            contentColor = CleaveSurface.onAccent,
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(enabled = enabled) { showEditDialog = true }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val textVal = if (toInt) "${value.roundToInt()}" else String.format("%.2f", value)
                    Text(
                        text = textVal,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CleaveSurface.onAccent
                    )
                    if (suffix != null) {
                        Text(
                            text = suffix,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CleaveSurface.onAccent
                        )
                    }
                }
                if (fineTuningControl) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        enabled = enabled && value > valueRange.start,
                        onClick = { changeValue(value - fineTuningStep, true) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_arrow_back),
                            contentDescription = "Decrease",
                            tint = CleaveSurface.onAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        enabled = enabled && value < valueRange.endInclusive,
                        onClick = { changeValue(value + fineTuningStep, true) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cleave_ic_chevron_right),
                            contentDescription = "Increase",
                            tint = CleaveSurface.onAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        appendContent()
    }
}

/** Mirai IntSliderSettingsCard (`Slider.kt`). */
@Composable
fun MiraiIntSliderSettingsCard(
    value: Int,
    title: String,
    position: CardPosition = CardPosition.Single,
    modifier: Modifier = Modifier,
    summary: String? = null,
    valueRange: IntRange,
    steps: Int = 0,
    suffix: String? = null,
    enabled: Boolean = true,
    fineTuningControl: Boolean = true,
    fineTuningStep: Int = 1,
    previewText: (@Composable ColumnScope.() -> Unit)? = null,
    onValueChange: (Int) -> Unit = {},
    onValueFinished: (Int) -> Unit = {}
) {
    var currentVal by remember(value) { mutableIntStateOf(value) }
    MiraiSettingsCard(
        modifier = modifier.fillMaxWidth(),
        position = position
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = CleaveSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiraiTitleAndSummary(title = title, summary = summary)
            MiraiSimpleTextSlider(
                modifier = Modifier.fillMaxWidth(),
                value = currentVal.toFloat(),
                enabled = enabled,
                onValueChange = {
                    currentVal = it.roundToInt()
                    onValueChange(currentVal)
                },
                onValueChangeFinished = { onValueFinished(currentVal) },
                toInt = true,
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                steps = steps,
                suffix = suffix,
                fineTuningControl = fineTuningControl,
                fineTuningStep = fineTuningStep.toFloat()
            )
            previewText?.invoke(this)
        }
    }
}

/**
 * Mirai ToggleableIntSliderSettingsCard (`VersionSettingsLayout.kt` — Photo 4):
 * Used for per-instance Memory Allocation with a slider + `<value>MB < >` pill + enable Checkbox on the right.
 */
@Composable
fun MiraiToggleableIntSliderSettingsCard(
    currentValue: Int,
    defaultValue: Int,
    title: String,
    position: CardPosition = CardPosition.Middle,
    modifier: Modifier = Modifier,
    summary: String? = null,
    valueRange: IntRange,
    suffix: String? = null,
    enabled: Boolean = true,
    fineTuningControl: Boolean = true,
    onValueChange: (Int) -> Unit = {},
    onValueFinished: (Int) -> Unit = {}
) {
    val accent = CleaveThemeManager.currentAccent()
    var checked by remember(currentValue) { mutableStateOf(currentValue >= valueRange.first) }
    var value by remember(currentValue, defaultValue) {
        mutableIntStateOf(currentValue.takeIf { it >= valueRange.first } ?: defaultValue)
    }

    MiraiSettingsCard(
        modifier = modifier.fillMaxWidth(),
        position = position,
        innerPadding = PaddingValues(
            start = CleaveSpacing.lg,
            end = CleaveSpacing.sm,
            top = CleaveSpacing.lg,
            bottom = CleaveSpacing.sm
        )
    ) {
        MiraiTitleAndSummary(
            modifier = Modifier.padding(end = CleaveSpacing.sm),
            title = title,
            summary = summary
        )
        MiraiSimpleTextSlider(
            modifier = Modifier.fillMaxWidth(),
            value = value.toFloat(),
            enabled = enabled && checked,
            onValueChange = {
                value = it.roundToInt()
                onValueChange(value)
            },
            onValueChangeFinished = { onValueFinished(value) },
            toInt = true,
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            suffix = suffix,
            fineTuningControl = fineTuningControl,
            appendContent = {
                Checkbox(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = { isChecked ->
                        checked = isChecked
                        if (isChecked) {
                            value = defaultValue
                            onValueFinished(value)
                        } else {
                            onValueFinished(-1)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = accent,
                        checkmarkColor = CleaveSurface.onAccent,
                        uncheckedColor = Color.White.copy(alpha = 0.70f)
                    )
                )
            }
        )
    }
}

/** Mirai Three-State setting (`Follow Global`, `Enable`, `Disable`) used in `VersionConfigScreen.kt` (Photo 3). */
enum class MiraiFollowGlobalState(val label: String) {
    FOLLOW_GLOBAL("Follow Global"),
    ENABLE("Enable"),
    DISABLE("Disable")
}

/**
 * Mirai StatefulDropdownMenuFollowGlobal (`VersionSettingsLayout.kt` — Photo 3):
 * Shows `Title` + `Summary` on the left, and `<StateLabel> [GearIcon]` on the right with a dropdown menu.
 */
@Composable
fun MiraiStatefulDropdownMenuFollowGlobal(
    currentState: MiraiFollowGlobalState,
    title: String,
    position: CardPosition = CardPosition.Middle,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    onValueChange: (MiraiFollowGlobalState) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val accent = CleaveThemeManager.currentAccent()

    MiraiSettingsCard(
        modifier = modifier.fillMaxWidth(),
        position = position,
        innerPadding = PaddingValues(all = CleaveSpacing.lg),
        onClick = if (enabled) ({ expanded = !expanded }) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
        ) {
            MiraiTitleAndSummary(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = CleaveSpacing.md),
                title = title,
                summary = summary
            )
            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = currentState.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                IconButton(
                    modifier = Modifier.size(34.dp),
                    enabled = enabled,
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cleave_ic_settings_filled),
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = MaterialTheme.shapes.large,
                    containerColor = CleaveSurface.panelRaised
                ) {
                    MiraiFollowGlobalState.entries.forEach { state ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = state.label,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = {
                                RadioButton(
                                    selected = currentState == state,
                                    onClick = {
                                        onValueChange(state)
                                        expanded = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = accent,
                                        unselectedColor = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                            },
                            onClick = {
                                onValueChange(state)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Mirai TextInputSettingsCard (`TextInput.kt` — Photo 4). */
@Composable
fun MiraiTextInputSettingsCard(
    value: String,
    title: String,
    position: CardPosition = CardPosition.Middle,
    modifier: Modifier = Modifier,
    summary: String? = null,
    label: String = "Follow global if left blank",
    onValueChange: (String) -> Unit = {},
    singleLine: Boolean = true
) {
    val accent = CleaveThemeManager.currentAccent()
    MiraiSettingsCard(
        modifier = modifier.fillMaxWidth(),
        position = position,
        innerPadding = PaddingValues(
            start = CleaveSpacing.lg,
            end = CleaveSpacing.lg,
            top = CleaveSpacing.lg,
            bottom = CleaveSpacing.md
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiraiTitleAndSummary(title = title, summary = summary)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                textStyle = MaterialTheme.typography.labelMedium.copy(color = Color.White),
                onValueChange = { raw ->
                    val cleaned = if (singleLine) raw.replace("\n", "").replace("\r", "") else raw
                    onValueChange(cleaned)
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                },
                singleLine = singleLine,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = accent,
                    unfocusedBorderColor = CleaveSurface.borderSoft,
                    cursorColor = accent
                )
            )
        }
    }
}

/** Mirai EnumSettingsCard (`Enum.kt`). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <E> MiraiEnumSettingsCard(
    items: List<E>,
    currentValue: E,
    title: String,
    position: CardPosition = CardPosition.Single,
    modifier: Modifier = Modifier,
    summary: String? = null,
    getItemText: @Composable (E) -> String,
    onValueChange: (E) -> Unit = {}
) {
    val accent = CleaveThemeManager.currentAccent()
    MiraiSettingsCard(
        modifier = modifier.fillMaxWidth(),
        position = position,
        innerPadding = PaddingValues(all = CleaveSpacing.lg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiraiTitleAndSummary(title = title, summary = summary)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onValueChange(item) }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentValue == item,
                            onClick = { onValueChange(item) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = accent,
                                unselectedColor = Color.White.copy(alpha = 0.70f)
                            )
                        )
                        Text(
                            text = getItemText(item),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * A renderer the launcher can run.
 *
 * The entries are built from the launcher's own renderer list, so every option maps to a
 * backend implemented in net.kdt.pojavlaunch.game.renderer and is only offered when the
 * current device supports it. The name of each renderer is the one the launcher itself
 * uses, the supported Minecraft versions included.
 */
data class MiraiRendererEntry(
    val uniqueId: String,
    val displayName: String,
    val backendRendererId: String?,
    val summary: String? = null,
    val minVer: String? = null,
    val maxVer: String? = null
)

private const val PREF_MIRAI_GLOBAL_RENDERER_ID = "cleave_mirai_global_renderer_id"
private const val PREF_MIRAI_INSTANCE_RENDERER_PREFIX = "cleave_mirai_instance_renderer_"

/**
 * Every renderer the launcher implements, plus the external renderer plugins that are installed.
 *
 * @param includeFollowGlobal add the "Follow Global" entry used by the per instance list
 */
fun miraiRendererEntries(context: Context, includeFollowGlobal: Boolean): List<MiraiRendererEntry> {
    val list = ArrayList<MiraiRendererEntry>()
    if (includeFollowGlobal) {
        list.add(
            MiraiRendererEntry(
                uniqueId = "",
                displayName = "Follow Global",
                backendRendererId = null
            )
        )
    }
    val plugins = runCatching { PojavPluginBridge.loadPlugins(context) }.getOrDefault(emptyList())
    val pluginTags = plugins.map { it.pluginTag }.toSet()
    val cache = RendererCache.getCompatibleRenderers(context)
    cache.rendererIds.forEachIndexed { index, rendererId ->
        if (rendererId in pluginTags) return@forEachIndexed
        list.add(
            MiraiRendererEntry(
                uniqueId = rendererId,
                displayName = cache.rendererDisplayNames.getOrElse(index) { rendererId },
                backendRendererId = rendererId,
                // A renderer with a device restriction says so here, next to its name; a bundled
                // renderer also describes what it is.
                summary = when (rendererId) {
                    Renderers.PROTON_RENDERER -> "32-bit devices only"
                    Renderers.EMESIS_RENDERER -> context.getString(R.string.mcl_setting_renderer_emesis_description)
                    else -> null
                }
            )
        )
    }
    plugins.forEach { plugin ->
        if (list.none { it.uniqueId == plugin.pluginTag }) {
            list.add(
                MiraiRendererEntry(
                    uniqueId = plugin.pluginTag,
                    displayName = plugin.displayName,
                    backendRendererId = plugin.pluginTag,
                    summary = plugin.summary.ifBlank { "External Renderer Plugin (${plugin.packageName})" },
                    minVer = plugin.minMCVer,
                    maxVer = plugin.maxMCVer
                )
            )
        }
    }
    return list
}

fun getMiraiGlobalRendererId(context: Context): String {
    val entries = miraiRendererEntries(context, includeFollowGlobal = false)
    val saved = LauncherPreferences.DEFAULT_PREF.getString(PREF_MIRAI_GLOBAL_RENDERER_ID, null)
    if (!saved.isNullOrEmpty() && entries.any { it.uniqueId == saved }) {
        return saved
    }
    val backend = LauncherPreferences.PREF_RENDERER
    return entries.firstOrNull { it.backendRendererId == backend }?.uniqueId
        ?: entries.firstOrNull()?.uniqueId
        ?: backend
}

fun setMiraiGlobalRenderer(entry: MiraiRendererEntry) {
    val backend = entry.backendRendererId ?: LauncherPreferences.PREF_RENDERER
    LauncherPreferences.PREF_RENDERER = backend
    LauncherPreferences.DEFAULT_PREF.edit()
        .putString("renderer", backend)
        .putString(PREF_MIRAI_GLOBAL_RENDERER_ID, entry.uniqueId)
        .apply()
}

fun getMiraiInstanceRendererId(context: Context, instanceKey: String, backendRendererId: String?): String {
    if (backendRendererId.isNullOrEmpty()) return ""
    val entries = miraiRendererEntries(context, includeFollowGlobal = true)
    val saved = LauncherPreferences.DEFAULT_PREF.getString(PREF_MIRAI_INSTANCE_RENDERER_PREFIX + instanceKey, null)
    if (!saved.isNullOrEmpty()) {
        val matched = entries.firstOrNull { it.uniqueId == saved }
        if (matched != null && matched.backendRendererId == backendRendererId) {
            return matched.uniqueId
        }
    }
    return entries.firstOrNull { it.backendRendererId == backendRendererId }?.uniqueId ?: backendRendererId
}

fun saveMiraiInstanceRendererId(instanceKey: String, entry: MiraiRendererEntry) {
    LauncherPreferences.DEFAULT_PREF.edit()
        .putString(PREF_MIRAI_INSTANCE_RENDERER_PREFIX + instanceKey, entry.uniqueId)
        .apply()
}

/**
 * Mirai RendererSummaryLayout (`RendererSettingsScreen.kt` — Photos 1, 2, and 5):
 * Renders optional renderer summary + `MC Version Support: >= minVer <= maxVer`.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MiraiRendererSummaryLayout(renderer: MiraiRendererEntry) {
    FlowRow(
        modifier = Modifier.alpha(0.7f),
        horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.md)
    ) {
        renderer.summary?.let { summaryText ->
            Text(
                text = summaryText,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        val minVer = renderer.minVer
        val maxVer = renderer.maxVer
        if (minVer != null || maxVer != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "MC Version Support:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
                if (minVer != null) {
                    Text(
                        text = ">= $minVer",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
                if (maxVer != null) {
                    Text(
                        text = "<= $maxVer",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}
