package net.kdt.pojavlaunch.cleave.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.cleave.bridge.CleaveInstance
import net.kdt.pojavlaunch.cleave.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.cleave.components.CleaveImageWell
import net.kdt.pojavlaunch.cleave.theme.CleaveRadii
import net.kdt.pojavlaunch.cleave.theme.CleaveSpacing
import net.kdt.pojavlaunch.cleave.theme.CleaveSurface
import net.kdt.pojavlaunch.cleave.theme.CleaveThemeManager
import net.kdt.pojavlaunch.cleave.ui.CleaveNav
import net.kdt.pojavlaunch.cleave.ui.LauncherSection

/** Dedicated Home-side instance picker. Selecting one returns directly to the character dashboard. */
@Composable
fun InstanceSelectorScreen(activity: FragmentActivity, navigate: (LauncherSection) -> Unit) {
    val instances = PojavInstanceBridge.instances
    val selectedId = PojavInstanceBridge.selected?.id
    val accent = CleaveThemeManager.currentAccent()

    LaunchedEffect(Unit) { PojavInstanceBridge.reload() }

    CleavePage(title = "Choose Instance", subtitle = "Pick the Minecraft version and loader to launch") {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CleaveSpacing.sm)
            ) {
                InstanceAction("Create instance", R.drawable.cleave_ic_add, Modifier.weight(1f)) {
                    navigate(LauncherSection.CREATE_INSTANCE)
                }
                InstanceAction("Manage library", R.drawable.cleave_ic_dashboard_filled, Modifier.weight(1f)) {
                    navigate(LauncherSection.LIBRARY)
                }
            }

            if (PojavInstanceBridge.loading && instances.isEmpty()) {
                Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = accent)
                    Text("Loading instances…", color = CleaveSurface.textSecondary,
                        modifier = Modifier.padding(top = CleaveSpacing.sm))
                }
            } else if (instances.isEmpty()) {
                Text("No instances yet. Create one to get started.", color = CleaveSurface.textSecondary,
                    modifier = Modifier.padding(CleaveSpacing.md))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CleaveSpacing.xs)
                ) {
                    items(instances, key = { it.id }) { instance ->
                        InstanceSelectorRow(
                            instance = instance,
                            selected = instance.id == selectedId,
                            accent = accent,
                            onSelect = {
                                PojavInstanceBridge.select(instance)
                                CleaveNav.finishTo(LauncherSection.HOME)
                            },
                            onEdit = { PojavInstanceBridge.edit(activity, instance) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstanceAction(label: String, icon: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val accent = CleaveThemeManager.currentAccent()
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CleaveRadii.cardSmall),
        color = CleaveSurface.panelRaised,
        border = BorderStroke(CleaveSpacing.hairline, CleaveSurface.borderSoft),
        onClick = onClick
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = CleaveSpacing.sm, vertical = CleaveSpacing.sm),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(painterResource(icon), contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(CleaveSpacing.xs))
            Text(label, color = CleaveSurface.textPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
private fun InstanceSelectorRow(
    instance: CleaveInstance,
    selected: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onSelect: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CleaveRadii.cardSmall),
        color = if (selected) accent.copy(alpha = 0.14f) else CleaveSurface.panel.copy(alpha = 0.82f),
        border = BorderStroke(CleaveSpacing.hairline, if (selected) accent.copy(alpha = 0.58f) else CleaveSurface.borderSoft)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = CleaveSpacing.sm, vertical = CleaveSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CleaveImageWell(instance.icon, R.drawable.cleave_ic_videogame_asset_outlined, 38.dp, radius = CleaveRadii.tiny)
            Spacer(Modifier.width(CleaveSpacing.sm))
            Column(Modifier.weight(1f).clickable(onClick = onSelect).padding(vertical = CleaveSpacing.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(instance.name, color = CleaveSurface.textPrimary, fontWeight = FontWeight.Bold,
                        fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (selected) Text("ACTIVE", color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 8.sp)
                }
                Text(instance.versionId, color = CleaveSurface.textSecondary, fontSize = 10.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("EDIT", color = accent, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable(onClick = onEdit).padding(8.dp))
        }
    }
}
