package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.data.model.FocusTask
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@Composable
fun DashboardTab(
    viewModel: AppViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val events by viewModel.events.collectAsState()
    val insights by viewModel.studyInsights.collectAsState()
    val isInsightsLoading by viewModel.isInsightsLoading.collectAsState()

    val pomodoroTime by viewModel.pomodoroTimeLeft.collectAsState()
    val isPomodoroRunning by viewModel.isPomodoroRunning.collectAsState()
    val pomodoroSession by viewModel.currentPomodoroSession.collectAsState()
    val pomodoroType by viewModel.currentPomodoroType.collectAsState()

    // Calculate dynamic stats
    val pendingTasksCount = tasks.count { !it.isCompleted }
    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasksCount = tasks.size
    val completionRate = if (totalTasksCount > 0) {
        (completedTasksCount.toFloat() / totalTasksCount.toFloat() * 100).toInt()
    } else {
        0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_scroll_column"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Cinematic Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .border(1.dp, OutlineDark, RoundedCornerShape(24.dp))
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Background Image
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_hero_banner),
                        contentDescription = "SNOW-X Hero",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Dark linear overlay for perfect text contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        SurfaceBlack.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    
                    // Active Status Pillar Accent
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = Redline.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Redline),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "FOCUS SYSTEM SYNERGY ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = Redline,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Text(
                            text = "Aesthetic Peak Academic Command",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = OnDark
                        )
                    }
                }
            }
        }

        // Welcome Header
        item {
            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                            append("Morning, ")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Alex.")
                        }
                    },
                    style = MaterialTheme.typography.displayMedium.copy(
                        letterSpacing = (-0.02).sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                val classesToday = events.count { it.dateText.contains("Oct 24", ignoreCase = true) || it.dateText.contains("today", ignoreCase = true) }
                Text(
                    text = "You have $classesToday classes and $pendingTasksCount tasks remaining today • 3 sessions left in focus block.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnDarkVariant
                )
            }
        }

        // Firebase Cloud database control card
        item {
            FirebaseAuthSyncCard(viewModel = viewModel)
        }

        // Weekly Focus Breakdown Chart Card (Custom Compose alternative to Recharts)
        item {
            WeeklyCategoryFocusCard(tasks = tasks)
        }

        // Gemini Prioritized Academic Schedule Module
        item {
            GeminiPrioritizedScheduleCard(viewModel = viewModel)
        }

        // Bento Grid Row 1: AI Insight + Daily Progress Circle (Adaptive Row / Wrap)
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 680.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1.8f)) {
                            AiInsightCard(
                                insights = insights,
                                isLoading = isInsightsLoading,
                                onAcceptRecommendation = {
                                    viewModel.presetTaskPomodoro("Calculus HW Study")
                                },
                                onRefresh = { viewModel.triggerInsightGeneration() }
                            )
                        }
                        Box(modifier = Modifier.weight(1.2f)) {
                            ProgressStatsCard(completionRate = completionRate)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AiInsightCard(
                            insights = insights,
                            isLoading = isInsightsLoading,
                            onAcceptRecommendation = {
                                viewModel.presetTaskPomodoro("Calculus HW Study")
                            },
                            onRefresh = { viewModel.triggerInsightGeneration() }
                        )
                        ProgressStatsCard(completionRate = completionRate)
                    }
                }
            }
        }

        // Bento Grid Row 2: Unified Side-By-Side Workspace (Tasks, Timer, Calendar Events in 3 columns)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Unified Cohesive Workspace",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    
                    Surface(
                        color = Redline.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Redline.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Side-by-Side View",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Redline),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val width = maxWidth
                    if (width >= 960.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DashboardTasksCard(
                                    tasks = tasks,
                                    onToggleProgress = { viewModel.toggleTaskProgress(it) },
                                    onViewAll = onNavigateToTasks
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                PomodoroWidgetCard(
                                    pomodoroTime = pomodoroTime,
                                    isRunning = isPomodoroRunning,
                                    sessionIndex = pomodoroSession,
                                    focusType = pomodoroType,
                                    onToggle = { viewModel.togglePomodoro() },
                                    onReset = { viewModel.resetPomodoro() }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                TodayScheduleCard(
                                    events = events,
                                    onOpenCalendar = onNavigateToCalendar
                                )
                            }
                        }
                    } else {
                        // Horizontal Paging side-by-side workspace on narrow screens so mobile users get exact same side-by-side integration!
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(modifier = Modifier.width(310.dp)) {
                                DashboardTasksCard(
                                    tasks = tasks,
                                    onToggleProgress = { viewModel.toggleTaskProgress(it) },
                                    onViewAll = onNavigateToTasks
                                )
                            }
                            Box(modifier = Modifier.width(310.dp)) {
                                PomodoroWidgetCard(
                                    pomodoroTime = pomodoroTime,
                                    isRunning = isPomodoroRunning,
                                    sessionIndex = pomodoroSession,
                                    focusType = pomodoroType,
                                    onToggle = { viewModel.togglePomodoro() },
                                    onReset = { viewModel.resetPomodoro() }
                                )
                            }
                            Box(modifier = Modifier.width(310.dp)) {
                                TodayScheduleCard(
                                    events = events,
                                    onOpenCalendar = onNavigateToCalendar
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Dynamic Canvas-based Progress Ring ---

@Composable
fun ProgressStatsCard(completionRate: Int, modifier: Modifier = Modifier) {
    val progressAnim = animateFloatAsState(
        targetValue = completionRate.toFloat() / 100f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "percentage"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDark, RoundedCornerShape(24.dp))
            .testTag("progress_stats_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular Ring drawn on standard canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    // Draw outline
                    drawCircle(
                        color = OutlineDark,
                        style = Stroke(width = strokeWidth)
                    )
                    // Draw neon active progress
                    drawArc(
                        color = Redline,
                        startAngle = -90f,
                        sweepAngle = 360f * progressAnim.value,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completionRate%",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = OnDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Daily Efficiency",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (completionRate >= 75) "Keep it up, you're ahead of schedule!" else "Ready to expand your focus?",
                style = MaterialTheme.typography.labelSmall,
                color = OnDarkVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- AI Insights Bento Component ---

@Composable
fun AiInsightCard(
    insights: String,
    isLoading: Boolean,
    onAcceptRecommendation: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val borderGlow by infiniteTransition.animateColor(
        initialValue = OutlineDark,
        targetValue = Redline.copy(alpha = 0.4f),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                OutlineDark at 0
                Redline.copy(alpha = 0.6f) at 1500
                OutlineDark at 3000
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderGlow, RoundedCornerShape(24.dp))
            .testTag("ai_insights_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Redline.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 12.dp, y = (-12).dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = Redline,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI INTELLIGENCE INSIGHT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = Redline
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(24.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Redline,
                            strokeWidth = 1.5.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Insights",
                            tint = OnDarkVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Crossfade(targetState = insights, label = "insights") { insightText ->
                Text(
                    text = insightText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp
                    ),
                    color = OnDark,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAcceptRecommendation,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accept_recommendation_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Redline,
                    contentColor = SurfaceBlack
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Accept Recommendation",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        }
    }
}

// --- Checklist Bento Component ---

@Composable
fun DashboardTasksCard(
    tasks: List<FocusTask>,
    onToggleProgress: (FocusTask) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDark, RoundedCornerShape(24.dp))
            .testTag("dashboard_tasks_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Academic Checklist",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnDark
                )
                Text(
                    text = "View All",
                    modifier = Modifier
                        .clickable { onViewAll() }
                        .testTag("view_all_tasks_link"),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Redline,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No focus tasks yet.", color = OnDarkVariant)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Display up to 4 tasks in the dashboard card
                    tasks.take(4).forEach { task ->
                        DashboardTaskItemRow(task = task, onCheckedChange = { onToggleProgress(task) })
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardTaskItemRow(
    task: FocusTask,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDarker)
            .border(
                width = 1.dp,
                color = if (task.isInProgress) Redline.copy(alpha = 0.5f) else OutlineDark,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onCheckedChange() }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Neon pulse left accent strip
        if (task.isInProgress) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(Redline, RoundedCornerShape(2.dp))
            )
        } else if (task.priority == "URGENT") {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(RedlineDark, RoundedCornerShape(2.dp))
            )
        }
        // Styled Checkbox
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    when {
                        task.isCompleted -> Redline
                        task.isInProgress -> OutlineDark
                        else -> Color.Transparent
                    }
                )
                .border(
                    2.dp,
                    if (task.isCompleted || task.isInProgress) Redline else OnDarkVariant,
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                task.isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = SurfaceBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
                task.isInProgress -> {
                    Icon(
                        imageVector = Icons.Default.HorizontalRule,
                        contentDescription = "In Progress",
                        tint = Redline,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Text & Priorities
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) OnDarkVariant else OnDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.isCompleted) {
                    Text(
                        text = "COMPLETED",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = OnDarkVariant
                    )
                } else if (task.isInProgress) {
                    Box(
                        modifier = Modifier
                            .background(Redline.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(1.dp, Redline.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "IN PROGRESS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = Redline
                        )
                    }
                } else if (task.priority == "URGENT") {
                    Text(
                        text = "URGENT",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = Redline
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${task.priority} Priority • ${task.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnDarkVariant
                )

                if (task.timeText.isNotEmpty()) {
                    Text(
                        text = task.timeText,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = OnDarkVariant
                    )
                }
            }
        }
    }
}

// --- Pomodoro Dashboard Component ---

@Composable
fun PomodoroWidgetCard(
    pomodoroTime: Int,
    isRunning: Boolean,
    sessionIndex: String,
    focusType: String,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = pomodoroTime / 60
    val seconds = pomodoroTime % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    val waveRotation = rememberInfiniteTransition(label = "wave")
    val rotation by waveRotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isRunning) Redline.copy(alpha = 0.5f) else OutlineDark,
                RoundedCornerShape(24.dp)
            )
            .testTag("pomodoro_widget_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) AccentDarkRed else SurfaceDark
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Redline,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Focus Timer",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Redline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                // Interactive Concentric cyber rings drawn on canvas
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawCircle(
                        color = Redline.copy(alpha = 0.08f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    // Dotted rotated circle
                    drawCircle(
                        color = Redline.copy(alpha = 0.25f),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), rotation)
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-1).sp
                        ),
                        color = OnDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onToggle,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("start_timer_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Redline,
                        contentColor = SurfaceBlack
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isRunning) "Pause" else "Start",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_timer_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OnDark
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, OutlineDark)
                ) {
                    Text(text = "Reset", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = OutlineDark, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SESSION",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = OnDarkVariant
                    )
                    Text(
                        text = sessionIndex,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnDark
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TYPE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = OnDarkVariant
                    )
                    Text(
                        text = focusType,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Redline
                    )
                }
            }
        }
    }
}

// --- Schedule Bento Component ---

@Composable
fun TodayScheduleCard(
    events: List<CalendarEvent>,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDark, RoundedCornerShape(24.dp))
            .testTag("today_schedule_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Today's Schedule",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No study schedule events for today.", color = OnDarkVariant)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    events.take(3).forEach { event ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Fancy Calendar box icon
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 48.dp)
                                    .background(SurfaceDarker, RoundedCornerShape(6.dp))
                                    .border(1.dp, Redline.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val dateParts = event.dateText.split(" ")
                                    val month = dateParts.getOrNull(0) ?: "Oct"
                                    val day = dateParts.getOrNull(1) ?: "01"
                                    Text(
                                        text = month.take(3).uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                        color = Redline
                                    )
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 12.sp),
                                        color = OnDark
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${event.timeRange} • ${event.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnDarkVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onOpenCalendar,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_calendar_dashboard_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OnDark
                ),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, OutlineDark)
            ) {
                Text(text = "Open Calendar", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun FirebaseAuthSyncCard(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val authStatus by viewModel.authStatusMessage.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.dp,
                    if (currentUser != null) Color.Green.copy(alpha = 0.4f) else Redline.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("firebase_auth_sync_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentUser != null) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                        contentDescription = "Sync",
                        tint = if (currentUser != null) Color.Green else Redline,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Cloud Database Sync",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnDark
                        )
                        Text(
                            text = if (currentUser != null) "Realtime Firestore active" else "Running in local cache mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnDarkVariant
                        )
                    }
                }

                Button(
                    onClick = { isExpanded = !isExpanded },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentUser != null) Color.Green.copy(alpha = 0.15f) else Redline.copy(alpha = 0.1f),
                        contentColor = if (currentUser != null) Color.Green else Redline
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (currentUser != null) "Linked" else "Connect Account",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (authStatus != null) {
                Surface(
                    color = if (currentUser != null) Color.Green.copy(alpha = 0.08f) else Color.Red.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (currentUser != null) Color.Green.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = authStatus ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (currentUser != null) Color.Green else Color.Red,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearAuthStatusMessage() },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = OnDarkVariant, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentUser == null) {
                        Text(
                            text = "Link your study tracker with Google Firebase Firestore to sync your tasks instantly across devices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnDarkVariant
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("e.g. sarveshchonde@gmail.com") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Redline,
                                unfocusedBorderColor = OutlineDark,
                                focusedTextColor = OnDark,
                                unfocusedTextColor = OnDarkVariant
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Secure Password") },
                            placeholder = { Text("••••••••") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Redline,
                                unfocusedBorderColor = OutlineDark,
                                focusedTextColor = OnDark,
                                unfocusedTextColor = OnDarkVariant
                            ),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.signInWithEmail(email, password)
                                },
                                enabled = !isAuthLoading && email.isNotBlank() && password.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Redline, contentColor = SurfaceBlack),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SurfaceBlack, strokeWidth = 2.dp)
                                } else {
                                    Text("Log In / Sign Up", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            OutlinedButton(
                                onClick = { viewModel.signInAnonymously() },
                                enabled = !isAuthLoading,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OnDark),
                                border = BorderStroke(1.dp, OutlineDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.8f)
                            ) {
                                Text("Guest Sync")
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Logged in as:",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnDarkVariant
                            )
                            Text(
                                text = currentUser?.email ?: "Guest Account",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnDark
                            )
                            Text(
                                text = "Your academic checklist tasks are synchronized with Google Cloud Firestore database in real-time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnDarkVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.startFirestoreSync() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.2f), contentColor = Color.Green),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Force Sync", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }

                                OutlinedButton(
                                    onClick = { viewModel.signOut() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Disconnect")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Custom Category Focus Time Breakdown Chart (Recharts Native Compose Equivalent) ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyCategoryFocusCard(tasks: List<FocusTask>, modifier: Modifier = Modifier) {
    // Helper to parse duration text to minutes
    fun parseTimeToMinutesRaw(timeText: String): Int {
        val clean = timeText.lowercase().trim()
        if (clean.isEmpty()) return 25
        val numeric = clean.filter { it.isDigit() }.toIntOrNull() ?: 25
        return if (clean.contains("hour") || clean.contains("hr")) {
            numeric * 60
        } else {
            numeric
        }
    }

    // Grouping & aggregate focus times (both completed and pending tasks to show accumulated week efforts!)
    val categorySumMap = remember(tasks) {
        tasks.groupBy { it.category.trim().ifEmpty { "General" } }
            .mapValues { (_, taskList) ->
                taskList.sumOf { parseTimeToMinutesRaw(it.timeText) }
            }.filter { it.value > 0 }
    }

    val totalTimeMinutes = remember(categorySumMap) {
        categorySumMap.values.sum()
    }

    // Modern cyber academic color palette
    val categoryColors = remember {
        mapOf(
            "Calculus" to Color(0xFF00ADB5),
            "Math" to Color(0xFF00ADB5),
            "Algorithms" to Color(0xFFFF2E93),
            "CS" to Color(0xFFFF2E93),
            "Physics" to Color(0xFFFFC045),
            "Science" to Color(0xFFFFC045),
            "Humanities" to Color(0xFF8A99AD),
            "General" to Color(0xFFBB86FC)
        )
    }

    fun getColorForCategory(cat: String): Color {
        return categoryColors[cat] ?: categoryColors.entries.firstOrNull { cat.contains(it.key, ignoreCase = true) }?.value ?: Color(0xFF8A2BE2)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDark, RoundedCornerShape(24.dp))
            .testTag("weekly_focus_breakdown_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Redline
                    )
                    Text(
                        text = "Subject Study Breakdown",
                        fontWeight = FontWeight.Bold,
                        color = OnDark
                    )
                }

                Text(
                    text = "${totalTimeMinutes / 60}h ${totalTimeMinutes % 60}m Total",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Redline
                )
            }

            Text(
                text = "Visualization representing total study minutes allocated per subject category throughout the current academic cycle.",
                style = MaterialTheme.typography.bodySmall,
                color = OnDarkVariant
            )

            if (totalTimeMinutes == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(SurfaceDarker, RoundedCornerShape(12.dp))
                        .border(1.dp, OutlineDark, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recorded focus task categories. Create checklists to begin.",
                        color = OnDarkVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                // Stacked custom progress bar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(OutlineDark)
                    ) {
                        categorySumMap.forEach { (cat, minutes) ->
                            val weight = minutes.toWeightValue()
                            if (weight > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(weight)
                                        .background(getColorForCategory(cat))
                                )
                            }
                        }
                    }

                    // Interactive Legends Flow row mapping
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorySumMap.forEach { (cat, minutes) ->
                            val percentage = (minutes.toFloat() / totalTimeMinutes.toFloat() * 100).toInt()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(getColorForCategory(cat))
                                )
                                Text(
                                    text = "$cat: ${minutes}m ($percentage%)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = OnDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Ext helper for Weight mapping safely
private fun Int.toWeightValue(): Float {
    return this.toFloat().coerceAtLeast(1f)
}

// --- Gemini Prioritized Study Schedule Card Module ---

@Composable
fun GeminiPrioritizedScheduleCard(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val schedule by viewModel.prioritizedSchedule.collectAsState()
    val isLoading by viewModel.isPrioritizedScheduleLoading.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "prioritizer_glow")
    val glowBorderColor by infiniteTransition.animateColor(
        initialValue = OutlineDark,
        targetValue = Redline.copy(alpha = 0.5f),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                OutlineDark at 0
                Redline.copy(alpha = 0.50f) at 1750
                OutlineDark at 3500
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "scheduler_border"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, glowBorderColor, RoundedCornerShape(24.dp))
            .testTag("gemini_prioritizer_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Redline
                    )
                    Text(
                        text = "Gemini Focus Scheduler",
                        fontWeight = FontWeight.ExtraBold,
                        color = OnDark
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        color = Redline,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Surface(
                        color = Redline.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AI AGENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Redline,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarker, RoundedCornerShape(16.dp))
                    .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (schedule == null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Analyze workload difficulties and generate optimal blocks.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnDark
                        )
                        Text(
                            text = "Click 'Analyze Workload' below. Gemini will evaluate the relative cognitive difficulty of your active check tasks and suggest an optimal, event-prioritized schedule around your synced classes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnDarkVariant
                        )
                    }
                } else {
                    SelectionContainer {
                        MarkdownScheduleRenderer(text = schedule ?: "")
                    }
                }
            }

            Button(
                onClick = { viewModel.generatePrioritizedSchedule() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Redline,
                    contentColor = SurfaceBlack
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isLoading) "Running Analysis..." else "Analyze Workload & Prioritize",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun MarkdownScheduleRenderer(text: String, modifier: Modifier = Modifier) {
    val lines = remember(text) { text.split("\n") }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        lines.forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) return@forEach

            // Check if it is a Section Header, containing e.g. "Difficulty Matrix", "Block Plan", "Synergy"
            val isHeader = trimmedLine.contains("Difficulty Matrix", ignoreCase = true) ||
                    trimmedLine.contains("Block Plan", ignoreCase = true) ||
                    trimmedLine.contains("Synergy", ignoreCase = true) ||
                    (trimmedLine.firstOrNull()?.isDigit() == true && trimmedLine.contains("**"))

            if (isHeader) {
                // Clear out asterisks, numbers at the beginning
                val cleanTitle = trimmedLine
                    .replace(Regex("^\\d+\\.\\s*"), "")
                    .replace("**", "")
                    .trim()

                val (icon, tint) = when {
                    cleanTitle.contains("Difficulty", ignoreCase = true) -> Icons.Default.BarChart to Redline
                    cleanTitle.contains("Plan", ignoreCase = true) -> Icons.Default.EditCalendar to Color(0xFF00ADB5)
                    else -> Icons.Default.Psychology to Color(0xFFFFC045)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = cleanTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            } else if (trimmedLine.startsWith("-") || trimmedLine.startsWith("*")) {
                // List bullet item
                val content = trimmedLine.removePrefix("-").removePrefix("*").trim()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(5.dp)
                            .background(Redline, CircleShape)
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        // Check for tags like [HARD], [MEDIUM], [EASY]
                        val hasHard = content.contains("[HARD]", ignoreCase = true)
                        val hasMedium = content.contains("[MEDIUM]", ignoreCase = true)
                        val hasEasy = content.contains("[EASY]", ignoreCase = true)

                        val cleanContent = content
                            .replace("[HARD]", "", ignoreCase = true)
                            .replace("[MEDIUM]", "", ignoreCase = true)
                            .replace("[EASY]", "", ignoreCase = true)
                            .trim()

                        if (hasHard || hasMedium || hasEasy) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                val (tagText, tagColor) = when {
                                    hasHard -> "HARD COGNITIVE WORK" to Redline
                                    hasMedium -> "MEDIUM COMPLEXITY" to Color(0xFFFFC045)
                                    else -> "ROUTINE STUDY / EASY" to Color(0xFF00ADB5)
                                }
                                Surface(
                                    color = tagColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, tagColor.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = tagText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = tagColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Style bold elements in the content text (e.g. **Task Name**)
                        val annotatedText = buildAnnotatedString {
                            var currentIndex = 0
                            val regex = Regex("\\*\\*(.*?)\\*\\*")
                            val matches = regex.findAll(cleanContent)
                            
                            for (match in matches) {
                                val start = match.range.first
                                val end = match.range.last + 1
                                
                                // Append preceding text
                                if (start > currentIndex) {
                                    append(cleanContent.substring(currentIndex, start))
                                }
                                
                                // Append bold match
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                                    append(match.groupValues[1])
                                }
                                
                                currentIndex = end
                            }
                            
                            if (currentIndex < cleanContent.length) {
                                append(cleanContent.substring(currentIndex))
                            }
                        }

                        Text(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = OnDark
                        )
                    }
                }
            } else {
                // Regular paragraph
                val annotatedParagraph = buildAnnotatedString {
                    var currentIndex = 0
                    val regex = Regex("\\*\\*(.*?)\\*\\*")
                    val matches = regex.findAll(trimmedLine)
                    
                    for (match in matches) {
                        val start = match.range.first
                        val end = match.range.last + 1
                        
                        if (start > currentIndex) {
                            append(trimmedLine.substring(currentIndex, start))
                        }
                        
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                            append(match.groupValues[1])
                        }
                        
                        currentIndex = end
                    }
                    
                    if (currentIndex < trimmedLine.length) {
                        append(trimmedLine.substring(currentIndex))
                    }
                }

                Text(
                    text = annotatedParagraph,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = OnDarkVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
