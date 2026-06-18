package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesTab(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsState()

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var isCreatingNote by remember { mutableStateOf(false) }
    var selectedNoteForDetail by remember { mutableStateOf<NoteItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("notes_tab_container"),
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
                    text = "Study Notes Guide",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnDark
                )
                Text(
                    text = "Review lecture points and summarize concepts with AI.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnDarkVariant
                )
            }

            if (!isCreatingNote && selectedNoteForDetail == null) {
                Button(
                    onClick = { isCreatingNote = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Redline, contentColor = SurfaceBlack),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("create_note_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(text = "New Note", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        Divider(color = OutlineDark, thickness = 1.dp)

        AnimatedVisibility(
            visible = isCreatingNote,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Redline.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .testTag("new_note_form_card"),
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
                        text = "Capture New Study Concept",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnDark
                    )

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Note Title (e.g. Algorithms Lecture)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Redline,
                            unfocusedBorderColor = OutlineDark,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDarkVariant
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Core Study Concepts & Equations...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("note_content_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Redline,
                            unfocusedBorderColor = OutlineDark,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDarkVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                                    viewModel.addNote(noteTitle, noteContent)
                                    noteTitle = ""
                                    noteContent = ""
                                    isCreatingNote = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_note_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Redline, contentColor = SurfaceBlack),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Save Study Note", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        OutlinedButton(
                            onClick = {
                                noteTitle = ""
                                noteContent = ""
                                isCreatingNote = false
                            },
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

        // Selected Note Detail Overlay View
        if (selectedNoteForDetail != null) {
            val currNote = selectedNoteForDetail!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineDark, RoundedCornerShape(24.dp))
                    .testTag("note_detail_view_card"),
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
                        Text(
                            text = currNote.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnDark
                        )

                        IconButton(
                            onClick = { selectedNoteForDetail = null },
                            modifier = Modifier.testTag("close_note_detail_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = OnDarkVariant)
                        }
                    }

                    Text(
                        text = "Captured: " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(currNote.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnDarkVariant
                    )

                    Divider(color = OutlineDark)

                    Text(
                        text = currNote.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnDark,
                        lineHeight = 24.sp
                    )

                    if (currNote.summary.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Redline.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .border(1.dp, Redline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Redline, modifier = Modifier.size(16.dp))
                                    Text(text = "AI FOCUS SUMMARY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Redline)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = currNote.summary, style = MaterialTheme.typography.bodyMedium, color = OnDark)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.summarizeNoteAsync(currNote)
                                // Close and reopen or just set local state to trigger rerender
                                selectedNoteForDetail = currNote.copy(summary = "Connecting to Gemini...")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarker, contentColor = Redline),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Redline.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().testTag("summarize_action_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(text = "Summarize Key Concepts with Gemini AI", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.deleteNote(currNote)
                                selectedNoteForDetail = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Redline)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Delete Note")
                        }
                    }
                }
            }
        } else {
            // Notes Grid
            if (notes.isEmpty()) {
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
                        Icon(imageVector = Icons.Outlined.ContentPaste, contentDescription = null, tint = OnDarkVariant, modifier = Modifier.size(48.dp))
                        Text(text = "No study notes written yet.", color = OnDarkVariant)
                        Text(text = "Click 'New Note' to save concepts or formulae.", style = MaterialTheme.typography.bodySmall, color = OnDarkVariant)
                    }
                }
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val colsCount = if (maxWidth >= 600.dp) 2 else 1
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(colsCount),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(notes) { note ->
                            NoteCardItem(
                                note = note,
                                onClick = { selectedNoteForDetail = note },
                                onSummarize = { viewModel.summarizeNoteAsync(note) },
                                onDelete = { viewModel.deleteNote(note) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCardItem(
    note: NoteItem,
    onClick: () -> Unit,
    onSummarize: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("note_card_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnDarkVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = OnDarkVariant, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = OnDarkVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (note.summary.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Redline.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                        .border(1.dp, Redline.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Redline, modifier = Modifier.size(12.dp))
                            Text(text = "GEMINI RECALL SUMMARY", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = Redline)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = note.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { onSummarize() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Redline),
                    border = BorderStroke(1.dp, OutlineDark)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                        Text(text = "Auto-Summarize Key Highlights", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
