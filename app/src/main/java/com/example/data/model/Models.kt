package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tasks")
data class FocusTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String = "General",
    val priority: String = "MEDIUM", // URGENT, HIGH, MEDIUM, LOW
    val subtext: String = "",
    val timeText: String = "",
    val isInProgress: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val summary: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val location: String = "Zoom",
    val timeRange: String = "", // e.g. "14:00 - 15:30"
    val dateText: String = "", // e.g. "Oct 24"
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
