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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@Composable
fun CalendarTab(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.events.collectAsState()
    val isSyncing by viewModel.isCalendarSyncing.collectAsState()
    val syncError by viewModel.calendarSyncError.collectAsState()
    val syncSuccess by viewModel.calendarSyncSuccess.collectAsState()

    var isAddingEvent by remember { mutableStateOf(false) }
    var isShowingSyncPanel by remember { mutableStateOf(false) }
    var tokenInput by remember { mutableStateOf("") }

    var eventTitle by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("Zoom") }
    var eventDate by remember { mutableStateOf("Oct 24") }
    var eventTime by remember { mutableStateOf("14:00 - 15:30") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_tab_container"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Tab Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Academic Calendar",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnDark
                )
                Text(
                    text = "Track class lectures, lab schedules, and exam deadlines.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnDarkVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { isShowingSyncPanel = !isShowingSyncPanel },
                    modifier = Modifier.testTag("toggle_sync_panel_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync Panel",
                        tint = if (syncSuccess) Color.Green else if (syncError != null) Color.Red else Redline
                    )
                }

                if (!isAddingEvent) {
                    Button(
                        onClick = { isAddingEvent = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Redline, contentColor = SurfaceBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_event_header_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(text = "Add Event", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // Google Calendar Connection / Sync Status Banner Card
        AnimatedVisibility(
            visible = isShowingSyncPanel,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (syncError != null) Color.Red.copy(alpha = 0.3f) else Redline.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .testTag("google_calendar_sync_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
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
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Redline,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Google Calendar Sync Center",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnDark
                            )
                        }

                        // Connected indicator pill
                        Surface(
                            color = if (syncSuccess) Color.Green.copy(alpha = 0.15f) else Color.Yellow.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (syncSuccess) Color.Green.copy(alpha = 0.5f) else Color.Yellow.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (syncSuccess) Color.Green else Color.Yellow)
                                )
                                Text(
                                    text = if (syncSuccess) "Synced" else "OAuth Linked",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (syncSuccess) Color.Green else Color.Yellow
                                )
                            }
                        }
                    }

                    Text(
                        text = "Synchronize lectures and deadlines directly from your Google Calendar account. Click 'Sync Now' below to perform a live retrieval.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnDarkVariant
                    )

                    // Optional access token input overrides
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Developer Access Token Override (Optional)") },
                        placeholder = { Text("simulation_token") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("oauth_token_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Redline,
                            unfocusedBorderColor = OutlineDark,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDarkVariant
                        ),
                        singleLine = true
                    )

                    // Error feedback banner
                    if (syncError != null) {
                        Surface(
                            color = Color.Red.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Sync Error: $syncError",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Success announcement
                    if (syncSuccess) {
                        Surface(
                            color = Color.Green.copy(alpha = 0.10f),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Green.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Google Calendar event log successfully synced! Study advisor recommendations are updated.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Green,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Synchronize and close tools
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.syncGoogleCalendar(tokenInput.ifBlank { "simulation_token" })
                            },
                            enabled = !isSyncing,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_google_sync_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Redline, contentColor = SurfaceBlack),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = SurfaceBlack,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing...", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            } else {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Now", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.resetCalendarSyncStatus()
                                isShowingSyncPanel = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnDark),
                            border = BorderStroke(1.dp, OutlineDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }

        Divider(color = OutlineDark, thickness = 1.dp)

        AnimatedVisibility(
            visible = isAddingEvent,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Redline.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .testTag("add_event_form_card"),
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
                        text = "Schedule Class / Exam Event",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnDark
                    )

                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("Event / Lecture Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_title_input"),
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
                            value = eventLocation,
                            onValueChange = { eventLocation = it },
                            label = { Text("Location (e.g. Zoom, Science B)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("event_location_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Redline,
                                unfocusedBorderColor = OutlineDark,
                                focusedTextColor = OnDark,
                                unfocusedTextColor = OnDarkVariant
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            label = { Text("Date (e.g. Oct 24)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("event_date_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Redline,
                                unfocusedBorderColor = OutlineDark,
                                focusedTextColor = OnDark,
                                unfocusedTextColor = OnDarkVariant
                            ),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = eventTime,
                        onValueChange = { eventTime = it },
                        label = { Text("Time range (e.g. 14:00 - 15:30)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_time_input"),
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
                        Button(
                            onClick = {
                                if (eventTitle.isNotBlank() && eventTime.isNotBlank()) {
                                    viewModel.addEvent(
                                        title = eventTitle,
                                        location = eventLocation,
                                        timeRange = eventTime,
                                        dateText = eventDate
                                    )
                                    // Reset and Close
                                    eventTitle = ""
                                    eventLocation = "Zoom"
                                    eventDate = "Oct 24"
                                    eventTime = "14:00 - 15:30"
                                    isAddingEvent = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_event_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Redline, contentColor = SurfaceBlack),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Save Schedule Event", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        OutlinedButton(
                            onClick = { isAddingEvent = false },
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

        // List Events
        if (events.isEmpty()) {
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
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = OnDarkVariant, modifier = Modifier.size(48.dp))
                    Text(text = "Schedule timeline is completely empty.", color = OnDarkVariant)
                    Text(text = "Add class calendars to align your morning focus details.", style = MaterialTheme.typography.bodySmall, color = OnDarkVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(events) { event ->
                    CalendarEventRowItem(
                        event = event,
                        onDelete = { viewModel.deleteEvent(event) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarEventRowItem(
    event: CalendarEvent,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Redline structured Date block
        Box(
            modifier = Modifier
                .size(height = 56.dp, width = 50.dp)
                .background(SurfaceDarker, RoundedCornerShape(6.dp))
                .border(1.dp, Redline.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val dateParts = event.dateText.split(" ")
                val month = dateParts.getOrNull(0) ?: "Oct"
                val day = dateParts.getOrNull(1) ?: "24"
                Text(
                    text = month.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = Redline
                )
                Text(
                    text = day,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 16.sp),
                    color = OnDark
                )
            }
        }

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = OnDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = OnDarkVariant, modifier = Modifier.size(12.dp))
                Text(text = event.timeRange, style = MaterialTheme.typography.bodySmall, color = OnDarkVariant)
                Text(text = "•", color = OnDarkVariant)
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = OnDarkVariant, modifier = Modifier.size(12.dp))
                Text(text = event.location, style = MaterialTheme.typography.bodySmall, color = OnDarkVariant)
            }
        }

        IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = OnDarkVariant)
        }
    }
}
