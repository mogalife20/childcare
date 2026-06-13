package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ActivityLog
import com.example.data.AppControl
import com.example.data.ChildProfile
import com.example.data.ChoreTask
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChildMonitorDashboard(
    viewModel: ChildMonitorViewModel,
    modifier: Modifier = Modifier
) {
    val children by viewModel.children.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val child by viewModel.selectedChild.collectAsState()
    val chores by viewModel.chores.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val apps by viewModel.apps.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Parent Console, 1 = Simulated Kid Phone

    var showAddProfileDialog by remember { mutableStateOf(false) }
    var showAddChoreDialog by remember { mutableStateOf(false) }

    // Background gradients to matching modern dark care/security aesthetic vibe
    val topGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App branding header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(topGradient)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Child Care Monitor",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Unified Guardianship Hub",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Child Selection Row & Slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FAMILY PROFILES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                TextButton(
                    onClick = { showAddProfileDialog = true },
                    modifier = Modifier.testTag("add_profile_trigger")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Kid Profile", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Child", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                items(children) { c ->
                    val isSelected = selectedChildId == c.id
                    val borderStroke = if (isSelected) {
                        BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    val itemBg = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(itemBg)
                            .border(borderStroke, RoundedCornerShape(24.dp))
                            .clickable { viewModel.selectChild(c.id) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("profile_tab_${c.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(c.avatarEmoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = c.name,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (c.isDeviceLocked) "🔒 Locked" else "📱 Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (c.isDeviceLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Navigation - Switch between Parent Mode and Simulated Kid View
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("tab_parent"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️ Parent Console", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("tab_child"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📱 Kid's Device", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        if (child != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                child!!.avatarEmoji,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            )
        }

        // Active layout panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (children.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("👪", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No family profiles configured",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "To start controlling limits and viewing activity, click standard Add Child above to configure a profile.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else if (child == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading device configurations...", fontSize = 14.sp)
                }
            } else {
                val currentChild = child!!
                if (activeTab == 0) {
                    ParentView(
                        child = currentChild,
                        chores = chores,
                        logs = logs,
                        apps = apps,
                        viewModel = viewModel,
                        onAddChoreClick = { showAddChoreDialog = true }
                    )
                } else {
                    ChildSimulatorView(
                        child = currentChild,
                        chores = chores,
                        apps = apps,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Modal dialogs
    if (showAddProfileDialog) {
        DialogAddChild(
            onDismiss = { showAddProfileDialog = false },
            onConfirm = { name, avatar, limit ->
                viewModel.createChildProfile(name, avatar, limit)
                showAddProfileDialog = false
            }
        )
    }

    if (showAddChoreDialog && selectedChildId != null) {
        DialogAddChore(
            childName = child?.name ?: "Child",
            onDismiss = { showAddChoreDialog = false },
            onConfirm = { title, points ->
                viewModel.addNewChore(selectedChildId!!, title, points)
                showAddChoreDialog = false
            }
        )
    }
}

// ==========================================
// PARENT CONSOLE SCREEN LAYOUT
// ==========================================
@Composable
fun ParentView(
    child: ChildProfile,
    chores: List<ChoreTask>,
    logs: List<ActivityLog>,
    apps: List<AppControl>,
    viewModel: ChildMonitorViewModel,
    onAddChoreClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. INSTANT REMOTE COLD LOCK BAR
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (child.isDeviceLocked) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    }
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (child.isDeviceLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lock_control_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (child.isDeviceLocked) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (child.isDeviceLocked) "🔒" else "🔓", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Emergency Super Lock",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (child.isDeviceLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (child.isDeviceLocked) {
                                    "${child.name}'s phone is fully FROZEN"
                                } else {
                                    "${child.name}'s phone uses normal limit policies"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.toggleDeviceLock(child) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (child.isDeviceLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("instant_lock_button")
                    ) {
                        Text(
                            text = if (child.isDeviceLocked) "UNLOCK" else "LOCK NOW",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 2. SCREEN TIME CONTROL PANEL
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "⏳ Screen Time Allowance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Daily limit setup and simulated usage",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = "${child.screenTimeUsedMinutes}m / ${child.screenTimeLimitMinutes}m",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (child.screenTimeUsedMinutes >= child.screenTimeLimitMinutes) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress visual
                    val progressRatio = if (child.screenTimeLimitMinutes > 0) {
                        (child.screenTimeUsedMinutes.toFloat() / child.screenTimeLimitMinutes.toFloat()).coerceIn(0f, 1f)
                    } else {
                        1f
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressRatio)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (progressRatio >= 0.9f) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive slider to tweak Daily Limit (minutes)
                    Text(
                        text = "Set daily limit: ${child.screenTimeLimitMinutes} mins",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Slider(
                        value = child.screenTimeLimitMinutes.toFloat(),
                        onValueChange = { newVal ->
                            viewModel.setScreenTimeLimit(child, newVal.toInt())
                        },
                        valueRange = 15f..240f,
                        steps = 14,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("limit_slider")
                    )

                    // Simulated Screen Time updates for tester convenience
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Simulate Usage:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { viewModel.adjustScreenTimeUsed(child, -15) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("use_minus_15")
                        ) {
                            Text("-15m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.adjustScreenTimeUsed(child, 15) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("use_plus_15")
                        ) {
                            Text("+15m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. TRACKING RADAR & GEOFENCES
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, "GeoLocation", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Safe Zones & Radar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "Current child position and geofence tracking",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.simulateLocationMovement(child) },
                            modifier = Modifier
                                .testTag("simulate_movement_button")
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, "Trigger movement simulation", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated ASCII Map Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "STATUS:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = child.currentStatus.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (child.currentStatus.contains("🚨")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom radar visualization
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                LocationBar(name = "Home 🏠", isActive = child.currentStatus.contains("Home"))
                                LocationBar(name = "School 🏫", isActive = child.currentStatus.contains("School"))
                                LocationBar(name = "Library 📚", isActive = child.currentStatus.contains("Library"))
                                LocationBar(name = "Transit / Mall 🛍️", isActive = child.currentStatus.contains("Transit") || child.currentStatus.contains("Mall"))
                                LocationBar(name = "Park 🌳", isActive = child.currentStatus.contains("Park"))
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "Telemetry GPS: %.4f, %.4f", child.latitude, child.longitude),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. INSTANT APP BLOCK SELECTOR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🛡️ App Access Security",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Block unwanted platforms instantly. Tap a block switch below:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (apps.isEmpty()) {
                        Text(
                            "No apps registered for security tracking.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            apps.forEach { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .border(
                                            1.dp,
                                            if (app.isBlocked) MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(app.iconEmoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                app.appName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (app.isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                app.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (app.isBlocked) "BLOCKED" else "ALLOWED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (app.isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Switch(
                                            checked = app.isBlocked,
                                            onCheckedChange = { viewModel.toggleAppBlock(app) },
                                            modifier = Modifier.testTag("app_block_toggle_${app.appName}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. CHORES & ALLOWANCES
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🏆 Chores & Allowance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                // 1 point = $0.50
                                text = "Current Ledger: $${String.format(Locale.getDefault(), "%.2f", child.pocketMoneyBalance)} pocket money",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = onAddChoreClick,
                            modifier = Modifier
                                .testTag("add_chore_trigger")
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, "Assign new chore", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (chores.isEmpty()) {
                        Text(
                            "No current Chores configured for this child profile.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            chores.forEach { chore ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            chore.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "${chore.points} pts ($${String.format(Locale.getDefault(), "%.2f", chore.points * 0.5)})",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "•",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (chore.isApprovedByParent) {
                                                    "✅ Approved & Awarded"
                                                } else if (chore.isCompletedByChild) {
                                                    "⏳ Done (Needs Approval)"
                                                } else {
                                                    "❌ Unfinished"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (chore.isApprovedByParent) {
                                                    MaterialTheme.colorScheme.primary
                                                } else if (chore.isCompletedByChild) {
                                                    Color(0xFFE2B13C)
                                                } else {
                                                    MaterialTheme.colorScheme.error
                                                }
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (chore.isCompletedByChild && !chore.isApprovedByParent) {
                                            IconButton(
                                                onClick = { viewModel.approveChore(child.id, chore, child) },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                    .testTag("approve_chore_${chore.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Approve",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteChore(chore) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Chore",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. REAL-TIME GUARDIAN ACTIVITY LOGS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📜 Security Activity Feed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(
                            onClick = { viewModel.clearLogs(child.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Clear logs",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (logs.isEmpty()) {
                        Text(
                            "Logs empty. Active logs record updates instantly.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 14.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())
                        ) {
                            val formater = SimpleDateFormat("HH:mm", Locale.getDefault())
                            logs.forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (log.severity) {
                                                "ALERT" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                                                "WARNING" -> Color(0xFFE2B13C).copy(alpha = 0.1f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            }
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = formater.format(Date(log.timestamp)),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.width(42.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.message,
                                        fontSize = 11.sp,
                                        color = when (log.severity) {
                                            "ALERT" -> MaterialTheme.colorScheme.error
                                            "WARNING" -> Color(0xFFE2B13C)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. PROFILE ADMIN DELETION CORNER
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Delete ${child.name}'s profile?",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        onClick = { viewModel.deleteChildProfile(child) }
                    ) {
                        Text("Delete Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LocationBar(name: String, isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            name,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        if (isActive) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "🟢 Active Target",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ==========================================
// SIMULATED KID'S DEVICE PREVIEW HANDSET
// ==========================================
@Composable
fun ChildSimulatorView(
    child: ChildProfile,
    chores: List<ChoreTask>,
    apps: List<AppControl>,
    viewModel: ChildMonitorViewModel
) {
    var simulatorLaunchedAppMsg by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Physical mock phone layout representation
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(6.dp, Color(0xFF37474F), RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF121212)) // Dark phone UI
                .padding(16.dp)
        ) {
            // Simulated phone status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "09:41 📡",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 10.dp)
                )
                // Phone speaker notch
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 14.dp)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(Color(0xFF37474F))
                )
                Text(
                    "98% 🔋",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }

            Text(
                "Simulated Kid Handset: ${child.name}",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // LOCKED SCREEN OVERLAY IF COLD LOCKED OR EXCEEDED TIMES
            if (child.isDeviceLocked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E0E12))
                        .border(1.dp, Color(0xFFE57373), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🔒", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "DEVICE DISABLED BY PARENTS",
                        color = Color(0xFFEF9A9A),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (child.screenTimeUsedMinutes >= child.screenTimeLimitMinutes) {
                            "You have exceeded your daily screen time budget: ${child.screenTimeLimitMinutes} mins."
                        } else {
                            "Admin locked this device directly for study / sleep hours."
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                        onClick = {
                            viewModel.adjustScreenTimeUsed(child, -15) // quick debug bypass inside emulator
                        },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("REQUEST +15M BYPASS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Chores to execute unlock
                    Text(
                        "Finish your Chores to earn pocket money:",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Left
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(chores) { chore ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2C1E20))
                                    .clickable { viewModel.completeChore(child.id, chore) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = chore.isCompletedByChild,
                                    onCheckedChange = { viewModel.completeChore(child.id, chore) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        chore.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (chore.isApprovedByParent) {
                                            "Approved ($${chore.points * 0.50})"
                                        } else if (chore.isCompletedByChild) {
                                            "Done! Waiting for Parent to Approve..."
                                        } else {
                                            "Not done • Reward: $${chore.points * 0.50}"
                                        },
                                        color = if (chore.isApprovedByParent) Color(0xFF81C784) else Color(0xFFFFB74D),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ACTIVE UNLOCKED CHILD MODE SCREEN
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131A1C))
                        .border(1.dp, Color(0xFF80CBC4), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    // Header Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🟢 ONLINE ACTIVE",
                            color = Color(0xFF4DB6AC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val remaining = (child.screenTimeLimitMinutes - child.screenTimeUsedMinutes).coerceAtLeast(0)
                        Text(
                            text = "⏳ ${remaining}m left Today",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // APP SHELF (GRID LAYOUT RECREATED)
                    Text(
                        text = "App Locker Simulator",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        apps.forEach { app ->
                            Column(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (app.isBlocked) Color(0xFF2C1C1D) else Color(0xFF212C2D)
                                    )
                                    .clickable {
                                        viewModel.simulateAppOpenAttempt(child.id, app)
                                        simulatorLaunchedAppMsg = if (app.isBlocked) {
                                            "⚠️ Blocked: ${app.appName} blocked by Guardian security!"
                                        } else {
                                            "🟢 Welcome! Launched ${app.appName} successfully"
                                        }
                                    }
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(app.iconEmoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    app.appName,
                                    color = if (app.isBlocked) Color(0xFFEF9A9A) else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (app.isBlocked) "🚫" else "✓",
                                    fontSize = 8.sp,
                                    color = if (app.isBlocked) Color.Red else Color.Green
                                )
                            }
                        }
                    }

                    // Ticker prompt matching block warning
                    AnimatedVisibility(
                        visible = simulatorLaunchedAppMsg.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (simulatorLaunchedAppMsg.contains("Blocked")) Color(0xFF4C0E14) else Color(0xFF0F3E32)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = simulatorLaunchedAppMsg,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { simulatorLaunchedAppMsg = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Clear, "Close", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chores List inside active handset
                    Text(
                        "Your Assigned Daily Tasks:",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(chores) { chore ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF22282B))
                                    .clickable { viewModel.completeChore(child.id, chore) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = chore.isCompletedByChild,
                                    onCheckedChange = { viewModel.completeChore(child.id, chore) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        chore.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (chore.isApprovedByParent) {
                                            "Done! Reward paid: $${chore.points * 0.50}"
                                        } else if (chore.isCompletedByChild) {
                                            "Submitted to parent..."
                                        } else {
                                            "Incomplete • Earn $${chore.points * 0.50}"
                                        },
                                        color = if (chore.isApprovedByParent) Color(0xFF81C784) else Color(0xFFFFB74D),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Simulated Home Swipe gestural spacer bar
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.5f))
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

// ==========================================
// MODAL DIALOGS IMPLEMENTATION
// ==========================================

@Composable
fun DialogAddChild(
    onDismiss: () -> Unit,
    onConfirm: (name: String, avatar: String, dailyLimit: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dailyLimitMinutes by remember { mutableStateOf(120) }
    var selectedAvatarIndex by remember { mutableStateOf(0) }
    val avatars = listOf("🧒", "👧", "👶", "🧑", "👱‍♀️")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add Child Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("child_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    "Select Character Avatar:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    avatars.forEachIndexed { index, avatar ->
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedAvatarIndex == index) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                )
                                .border(
                                    2.dp,
                                    if (selectedAvatarIndex == index) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedAvatarIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatar, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Standard Daily Screen Time Limit: ${dailyLimitMinutes}m",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Slider(
                    value = dailyLimitMinutes.toFloat(),
                    onValueChange = { dailyLimitMinutes = it.toInt() },
                    valueRange = 30f..240f,
                    steps = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name, avatars[selectedAvatarIndex], dailyLimitMinutes)
                            }
                        },
                        modifier = Modifier.testTag("add_profile_submit")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun DialogAddChore(
    childName: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, points: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var points by remember { mutableStateOf(5) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Assign Chore to $childName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task description (e.g. Wash dishes)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chore_title_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Allowance points value: ${points} XP ($${String.format(Locale.getDefault(), "%.2f", points * 0.50)})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Slider(
                    value = points.toFloat(),
                    onValueChange = { points = it.toInt() },
                    valueRange = 2f..20f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, points)
                            }
                        },
                        modifier = Modifier.testTag("add_chore_submit")
                    ) {
                        Text("Assign Chore")
                    }
                }
            }
        }
    }
}
