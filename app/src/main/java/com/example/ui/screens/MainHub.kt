package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.FocusTask
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHub(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    // Current active screen tab index: 0 = Dashboard, 1 = Notes, 2 = AI Assistant, 3 = Calendar, 4 = Tasks
    var currentTab by remember { mutableStateOf(0) }

    val tasks by viewModel.tasks.collectAsState()
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val weeklyGoalFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0.65f

    // Dialog state
    var showQuickAddDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC7HK6qePmKXX0oFtrHbAVdanpzALwrgRkUEP1Rb8mdU3LjBysgaeB8QqeochEx2_b8GjEbiIM8arQf94DJ4oZjdf3olUugzKuQaayMyZmWubVY_ZUC3Iq1qa-mA04ZKpZgeYV1FF9WLqNBfLyGbbbaFiM1hlIM1okz5IPlFT2KA-atZ1DoqKtfU96JtDoAeZag9kRXOhkYVCBTjK9NE4wnuucivc7uLHEm3efLt3j4c5GuMZ9vd11T9xLvUYblcfsWw12BSVwRPqs"

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(SurfaceDarker)) {
        val isWideScreen = maxWidth >= 760.dp

        if (isWideScreen) {
            // Adaptive Grid: Persistent Sidebar layout + Content pane
            Row(modifier = Modifier.fillMaxSize()) {
                // Persistent Sidebar (Desktop view)
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .background(SurfaceDarker)
                        .border(width = 1.dp, color = OutlineDark, shape = RoundedCornerShape(0.dp))
                        .padding(top = 24.dp, bottom = 24.dp)
                ) {
                    // Title Header
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Redline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, Redline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.BubbleChart, contentDescription = null, tint = Redline, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "SNOW-X",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
                            color = OnDark
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Manager identity Profile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Alex Profile",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.dp, OutlineDark, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(text = "Alex Manager", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = OnDark)
                            Text(text = "Redline Plan", style = MaterialTheme.typography.labelSmall, color = Redline)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigation Panel items
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SidebarNavItem(title = "Dashboard", icon = Icons.Default.Dashboard, isActive = currentTab == 0) { currentTab = 0 }
                        SidebarNavItem(title = "Notes", icon = Icons.Outlined.Description, isActive = currentTab == 1) { currentTab = 1 }
                        SidebarNavItem(title = "AI Assistant", icon = Icons.Default.AutoAwesome, isActive = currentTab == 2) { currentTab = 2 }
                        SidebarNavItem(title = "Calendar", icon = Icons.Outlined.CalendarToday, isActive = currentTab == 3) { currentTab = 3 }
                        SidebarNavItem(title = "Checklist Tasks", icon = Icons.Default.CheckCircle, isActive = currentTab == 4) { currentTab = 4 }
                    }

                    // Progress Goal meter at bottom of Sidebar (mockup)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OutlineDark, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "Weekly Goal", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OnDark)
                                LinearProgressIndicator(
                                    progress = { weeklyGoalFraction },
                                    color = Redline,
                                    trackColor = OutlineDark,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                                Text(
                                    text = "$completedCount/$totalCount TASKS DONE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = OnDarkVariant
                                )
                            }
                        }
                    }
                }

                // Active Panel display
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 40.dp, vertical = 24.dp)
                ) {
                    Crossfade(targetState = currentTab, label = "desktop_panels") { tab ->
                        when (tab) {
                            0 -> DashboardTab(
                                viewModel = viewModel,
                                onNavigateToTasks = { currentTab = 4 },
                                onNavigateToCalendar = { currentTab = 3 }
                            )
                            1 -> NotesTab(viewModel = viewModel)
                            2 -> AssistantTab(viewModel = viewModel)
                            3 -> CalendarTab(viewModel = viewModel)
                            4 -> TasksTab(viewModel = viewModel)
                        }
                    }
                }
            }
        } else {
            // Responsive Mobile Layout (Scaffold with BottomNav and top-aligned Appbar)
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.BubbleChart, contentDescription = null, tint = Redline, modifier = Modifier.size(24.dp))
                                Text(
                                    text = "SNOW-X",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = OnDark
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts", tint = OnDarkVariant)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Alex Avatar",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, OutlineDark, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = SurfaceDarker,
                            titleContentColor = OnDark
                        ),
                        modifier = Modifier
                            .drawBehind {
                                val strokeWidthPx = 1.dp.toPx()
                                drawLine(
                                    color = OutlineDark,
                                    start = Offset(0f, size.height - strokeWidthPx),
                                    end = Offset(size.width, size.height - strokeWidthPx),
                                    strokeWidth = strokeWidthPx
                                )
                            }
                            .windowInsetsPadding(WindowInsets.statusBars)
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = SurfaceDark,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .border(width = 1.dp, color = OutlineDark, shape = RoundedCornerShape(0.dp))
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = { Icon(imageVector = if (currentTab == 0) Icons.Default.Home else Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SurfaceBlack,
                                selectedTextColor = Redline,
                                unselectedIconColor = OnDarkVariant,
                                unselectedTextColor = OnDarkVariant,
                                indicatorColor = Redline
                            ),
                            modifier = Modifier.testTag("nav_bottom_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = { Icon(imageVector = Icons.Outlined.EditNote, contentDescription = "Notes") },
                            label = { Text("Notes", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SurfaceBlack,
                                selectedTextColor = Redline,
                                unselectedIconColor = OnDarkVariant,
                                unselectedTextColor = OnDarkVariant,
                                indicatorColor = Redline
                            ),
                            modifier = Modifier.testTag("nav_bottom_notes")
                        )
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI") },
                            label = { Text("AI Helper", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SurfaceBlack,
                                selectedTextColor = Redline,
                                unselectedIconColor = OnDarkVariant,
                                unselectedTextColor = OnDarkVariant,
                                indicatorColor = Redline
                            ),
                            modifier = Modifier.testTag("nav_bottom_ai")
                        )
                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { currentTab = 3 },
                            icon = { Icon(imageVector = Icons.Default.Event, contentDescription = "Calendar") },
                            label = { Text("Calendar", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SurfaceBlack,
                                selectedTextColor = Redline,
                                unselectedIconColor = OnDarkVariant,
                                unselectedTextColor = OnDarkVariant,
                                indicatorColor = Redline
                            ),
                            modifier = Modifier.testTag("nav_bottom_calendar")
                        )
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { currentTab = 4 },
                            icon = { Icon(imageVector = Icons.Default.Assignment, contentDescription = "Tasks") },
                            label = { Text("Tasks", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SurfaceBlack,
                                selectedTextColor = Redline,
                                unselectedIconColor = OnDarkVariant,
                                unselectedTextColor = OnDarkVariant,
                                indicatorColor = Redline
                            ),
                            modifier = Modifier.testTag("nav_bottom_tasks")
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showQuickAddDialog = true },
                        containerColor = Redline,
                        contentColor = SurfaceBlack,
                        shape = CircleShape,
                        modifier = Modifier.padding(bottom = 16.dp).testTag("quick_add_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Quick Action", modifier = Modifier.size(24.dp))
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Crossfade(targetState = currentTab, label = "mobile_panels") { tab ->
                        when (tab) {
                            0 -> DashboardTab(
                                viewModel = viewModel,
                                onNavigateToTasks = { currentTab = 4 },
                                onNavigateToCalendar = { currentTab = 3 }
                            )
                            1 -> NotesTab(viewModel = viewModel)
                            2 -> AssistantTab(viewModel = viewModel)
                            3 -> CalendarTab(viewModel = viewModel)
                            4 -> TasksTab(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // Quick Add Modal Form dialog trigger
        if (showQuickAddDialog) {
            QuickAddSelectionDialog(
                onDismiss = { showQuickAddDialog = false },
                onSelectNote = {
                    currentTab = 1
                    showQuickAddDialog = false
                },
                onSelectTask = {
                    currentTab = 4
                    showQuickAddDialog = false
                },
                onSelectEvent = {
                    currentTab = 3
                    showQuickAddDialog = false
                }
            )
        }
    }
}

// Side Draw Navigation Item definition
@Composable
fun SidebarNavItem(
    title: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isActive) Redline.copy(alpha = 0.1f) else Color.Transparent)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Redline left indicator bar
        if (isActive) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(Redline, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(20.dp))
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Redline else OnDarkVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isActive) Redline else OnDarkVariant
        )
    }
}

// Dialog helper
@Composable
fun QuickAddSelectionDialog(
    onDismiss: () -> Unit,
    onSelectNote: () -> Unit,
    onSelectTask: () -> Unit,
    onSelectEvent: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Concept / Task / Event",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnDark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(text = "Choose which academic module you'd like to update:", color = OnDarkVariant, fontSize = 14.sp)
                Button(
                    onClick = onSelectTask,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = OnDark),
                    modifier = Modifier.fillMaxWidth().border(1.dp, OutlineDark, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Redline, modifier = Modifier.size(16.dp))
                        Text(text = "Add Checklist Task", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Button(
                    onClick = onSelectNote,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = OnDark),
                    modifier = Modifier.fillMaxWidth().border(1.dp, OutlineDark, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Redline, modifier = Modifier.size(16.dp))
                        Text(text = "Capture Study Note", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Button(
                    onClick = onSelectEvent,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = OnDark),
                    modifier = Modifier.fillMaxWidth().border(1.dp, OutlineDark, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = Redline, modifier = Modifier.size(16.dp))
                        Text(text = "Schedule Class Event", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Redline)) {
                Text("Dismiss")
            }
        },
        containerColor = SurfaceDarker,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.border(1.dp, OutlineDark, RoundedCornerShape(14.dp)).testTag("quick_add_selection_dialog")
    )
}
