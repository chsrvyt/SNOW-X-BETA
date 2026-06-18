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

        // Bento Grid Row 2: Tasks + Pomodoro Widget / Events (Adaptive layout)
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 680.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1.6f)) {
                            DashboardTasksCard(
                                tasks = tasks,
                                onToggleProgress = { viewModel.toggleTaskProgress(it) },
                                onViewAll = onNavigateToTasks
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1.4f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PomodoroWidgetCard(
                                pomodoroTime = pomodoroTime,
                                isRunning = isPomodoroRunning,
                                sessionIndex = pomodoroSession,
                                focusType = pomodoroType,
                                onToggle = { viewModel.togglePomodoro() },
                                onReset = { viewModel.resetPomodoro() }
                            )
                            TodayScheduleCard(
                                events = events,
                                onOpenCalendar = onNavigateToCalendar
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PomodoroWidgetCard(
                            pomodoroTime = pomodoroTime,
                            isRunning = isPomodoroRunning,
                            sessionIndex = pomodoroSession,
                            focusType = pomodoroType,
                            onToggle = { viewModel.togglePomodoro() },
                            onReset = { viewModel.resetPomodoro() }
                        )
                        DashboardTasksCard(
                            tasks = tasks,
                            onToggleProgress = { viewModel.toggleTaskProgress(it) },
                            onViewAll = onNavigateToTasks
                        )
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
