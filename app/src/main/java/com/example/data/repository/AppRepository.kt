package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.local.CalendarDao
import com.example.data.local.NoteDao
import com.example.data.local.TaskDao
import com.example.data.model.CalendarEvent
import com.example.data.model.FocusTask
import com.example.data.model.NoteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(
    private val taskDao: TaskDao,
    private val noteDao: NoteDao,
    private val calendarDao: CalendarDao
) {
    // --- Room Database Access ---

    val allTasks: Flow<List<FocusTask>> = taskDao.getAllTasks()
    val allNotes: Flow<List<NoteItem>> = noteDao.getAllNotes()
    val allEvents: Flow<List<CalendarEvent>> = calendarDao.getAllEvents()

    suspend fun insertTask(task: FocusTask) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: FocusTask) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: FocusTask) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteBranchTaskById(id: Int) = withContext(Dispatchers.IO) {
        taskDao.deleteById(id)
    }

    suspend fun insertNote(note: NoteItem) = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: NoteItem) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: NoteItem) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    suspend fun insertEvent(event: CalendarEvent) = withContext(Dispatchers.IO) {
        calendarDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: CalendarEvent) = withContext(Dispatchers.IO) {
        calendarDao.deleteEvent(event)
    }

    // --- Gemini API Integrations ---

    /**
     * Generates study insights based on current pending tasks and events.
     */
    suspend fun getStudyInsights(pendingTasks: List<FocusTask>, events: List<CalendarEvent>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key not configured. Enter GEMINI_API_KEY in the Secrets panel."
        }

        val tasksString = pendingTasks.filter { !it.isCompleted }.joinToString("\n") { 
            "- ${it.title} (${it.category}, Priority: ${it.priority})" 
        }
        val eventsString = events.joinToString("\n") { 
            "- ${it.title} at ${it.timeRange} (${it.location})" 
        }

        val prompt = """
            You are SNOW-X Study Advisor, a strict, cool, and highly effective academic mentor.
            Here is the student's context for today:
            
            PENDING TASKS:
            $tasksString
            
            TODAY'S SCHEDULE:
            $eventsString
            
            Synthesize this and provide one powerful, highly specific, action-focused recommendation (e.g., "Schedule a 25-min Deep Focus session for your Calculus HW now before the Intro to Algorithms lecture at 14:00"). Keep it clean, direct, under 2 sentences, and make it sound extremely professional and motivating. DO NOT use fancy titles or markdown prefixes.
        """.trimIndent()

        try {
            val response = RetrofitClient.service.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    )
                )
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "Finish your highest priority task first today for peak performance!"
        } catch (e: Exception) {
            "Efficiency recommendation: Complete your high-priority items before afternoon lectures to maintain momentum."
        }
    }

    /**
     * Chat response of the companion AI Study Assistant.
     */
    suspend fun getAssistantResponse(chatHistory: List<Pair<String, Boolean>>, currentPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key not configured. Enter GEMINI_API_KEY in the Secrets panel."
        }

        // build contextual chat prompt including recent history
        val historySnippet = chatHistory.takeLast(6).joinToString("\n") { (msg, isUser) ->
            if (isUser) "Student: $msg" else "Advisor: $msg"
        }

        val prompt = """
            You are the SNOW-X Study Advisor, a helpful academic advisor. You help students plan, explain notes, formulate logic, or solve academic problems.
            Keep your answer short, concise, and direct (max 3 sentences) to maintain student attention, but be highly useful, encouraging and precise.
            
            CONVERSATION HISTORY:
            $historySnippet
            
            Student: $currentPrompt
            Advisor:
        """.trimIndent()

        try {
            val response = RetrofitClient.service.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    )
                )
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "I apologize, but I had trouble processing that request. Please try asking again."
        } catch (e: Exception) {
            "Error: ${e.message ?: "Network error. Please check your connection."}"
        }
    }

    /**
     * Elegant note summarizer.
     */
    suspend fun summarizeNote(title: String, content: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key not configured."
        }

        val prompt = """
            You are an expert summarizer. Summarize this academic/study note content in exactly 1 or 2 clear, concise bullets for quick review. Keep it under 30 words.
            
            NOTE TITLE: $title
            CONTENT:
            $content
        """.trimIndent()

        try {
            val response = RetrofitClient.service.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    )
                )
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "Summary unavailable."
        } catch (e: Exception) {
            "Could not connect to summarize."
        }
    }
}
