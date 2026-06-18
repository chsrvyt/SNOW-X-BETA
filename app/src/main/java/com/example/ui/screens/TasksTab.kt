package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusTask
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@Composable
fun TasksTab(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()

    var isAddingTask by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskCategory by remember { mutableStateOf("Math") }
    var taskPriority by remember { mutableStateOf("HIGH") } // URGENT, HIGH, MEDIUM, LOW
    var taskSubtext by remember { mutableStateOf("Due soon") }
    var taskTimeText by remember { mutableStateOf("") }

    // Filtering tabs: 0 = ALL, 1 = FOCUS / IN PROGRESS, 2 = URGENT, 3 = COMPLETED
    var selectedFilterIndex by remember { mutableStateOf(0) }

    val filteredTasks = remember(tasks, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> tasks.filter { it.isInProgress || (!it.isCompleted && (it.priority == "URGENT" || it.priority == "HIGH")) }
            2 -> tasks.filter { it.priority == "URGENT" && !it.isCompleted }
            3 -> tasks.filter { it.isCompleted }
            else -> tasks
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("tasks_tab_container"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Tab Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Academic Checklist",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnDark
                )
                Text(
                    text = "Organize courses and check completed tasks to boost daily efficiency.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnDarkVariant
                )
            }

            if (!isAddingTask) {
                Button(
                    onClick = { isAddingTask = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Redline, contentColor = SurfaceBlack),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("add_task_header_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(text = "Add Task", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        Divider(color = OutlineDark, thickness = 1.dp)

        AnimatedVisibility(
            isAddingTask,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Redline.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .testTag("add_task_form_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create Academic Task",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnDark
                    )

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title (e.g. Homework 2)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input"),
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = taskCategory,
                            onValueChange = { taskCategory = it },
                            label = { Text("Category (e.g. Math, Tech)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("task_category_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Redline,
                                unfocusedBorderColor = OutlineDark,
                                focusedTextColor = OnDark,
                                unfocusedTextColor = OnDarkVariant
                            ),
                            singleLine = true
                        )

                        // Priority Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Priority: $taskPriority",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Redline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("URGENT", "HIGH", "MEDIUM", "LOW").forEach { prio ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (taskPriority == prio) Redline.copy(alpha = 0.15f) else SurfaceDarker,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (taskPriority == prio) Redline else OutlineDark,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable { taskPriority = prio }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = prio.take(3),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (taskPriority == prio) OnDark else OnDarkVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = taskSubtext,
                            onValueChange = { taskSubtext = it },
                            label = { Text("Details (e.g. Due soon)") },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("task_subtext_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Redline,
                                unfocusedBorderColor = OutlineDark,
                                focusedTextColor = OnDark,
                                unfocusedTextColor = OnDarkVariant
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = taskTimeText,
                            onValueChange = { taskTimeText = it },
                            label = { Text("Time (e.g. 11:00 AM)") },
                            modifier = Modifier
                                .weight(0.7f)
                                .testTag("task_timetext_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Redline,
                                unfocusedBorderColor = OutlineDark,
                                focusedTextColor = OnDark,
                                unfocusedTextColor = OnDarkVariant
                            ),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (taskTitle.isNotBlank()) {
                                    viewModel.addTask(
                                        title = taskTitle,
                                        category = taskCategory,
                                        priority = taskPriority,
                                        subtext = taskSubtext.ifEmpty { "Academic" },
                                        timeText = taskTimeText
                                    )
                                    // Reset and Close
                                    taskTitle = ""
                                    taskCategory = "Math"
                                    taskPriority = "HIGH"
                                    taskSubtext = "Due soon"
                                    taskTimeText = ""
                                    isAddingTask = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_task_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Redline, contentColor = SurfaceBlack),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Save Task Checklist", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        OutlinedButton(
                            onClick = { isAddingTask = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnDark),
                            border = BorderStroke(1.dp, OutlineDark),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }

        // Segments Filter row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filtersList = listOf("ALL", "FOCUS", "URGENT", "DONE")
            filtersList.forEachIndexed { idx, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selectedFilterIndex == idx) Redline.copy(alpha = 0.12f) else SurfaceDark,
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            if (selectedFilterIndex == idx) Redline else OutlineDark,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedFilterIndex = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = if (selectedFilterIndex == idx) OnDark else OnDarkVariant
                    )
                }
            }
        }

        // List tasks
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = OnDarkVariant, modifier = Modifier.size(48.dp))
                    Text(text = "No checklist items under this filter.", color = OnDarkVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredTasks) { task ->
                    TaskFullRowItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskProgress(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskFullRowItem(
    task: FocusTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(
                1.dp,
                if (task.isInProgress) Redline.copy(alpha = 0.5f) else OutlineDark,
                RoundedCornerShape(16.dp)
            )
            .clickable { onToggle() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Redline left accent strip
        if (task.isInProgress) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .background(Redline, RoundedCornerShape(2.dp))
            )
        } else if (task.priority == "URGENT") {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .background(RedlineDark, RoundedCornerShape(2.dp))
            )
        }
        // Styled Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(5.dp))
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
                    RoundedCornerShape(5.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                task.isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = SurfaceBlack,
                        modifier = Modifier.size(18.dp)
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

        // Task Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
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
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
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
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Redline
                        )
                    }
                } else if (task.priority == "URGENT") {
                    Text(
                        text = "URGENT",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Redline
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category: ${task.category} • priority: ${task.priority.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnDarkVariant
                )

                if (task.subtext.isNotEmpty()) {
                    Text(
                        text = task.subtext + (if (task.timeText.isNotEmpty()) " • ${task.timeText}" else ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnDarkVariant
                    )
                }
            }
        }

        IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = OnDarkVariant)
        }
    }
}
