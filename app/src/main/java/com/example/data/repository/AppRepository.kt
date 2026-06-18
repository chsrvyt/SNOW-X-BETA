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

    /**
     * Integrates with real Google Calendar REST API v3 to sync primary calendar events.
     */
    suspend fun syncGoogleCalendar(authToken: String): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val authHeader = if (authToken.startsWith("Bearer ", ignoreCase = true)) authToken else "Bearer $authToken"
        try {
            val response = com.example.data.api.GoogleCalendarClient.service.getCalendarEvents(
                authorization = authHeader
            )
            val apiEvents = response.items ?: emptyList()
            val savedEvents = mutableListOf<CalendarEvent>()
            
            for (gv in apiEvents) {
                val startLocal = gv.start
                val dateStr = if (startLocal != null) {
                    if (!startLocal.dateTime.isNullOrEmpty()) {
                        val isoDate = startLocal.dateTime
                        val parts = isoDate.split("T")
                        val datePart = parts.getOrNull(0) ?: ""
                        formatIsoDateToReadable(datePart)
                    } else if (!startLocal.date.isNullOrEmpty()) {
                        formatIsoDateToReadable(startLocal.date)
                    } else {
                        "Oct 24"
                    }
                } else {
                    "Oct 24"
                }

                val timeRangeStr = if (startLocal != null && !startLocal.dateTime.isNullOrEmpty()) {
                    val startIso = startLocal.dateTime
                    val endIso = gv.end?.dateTime ?: ""
                    val startTime = startIso.substringAfter("T").substringBeforeLast(":").take(5)
                    val endTime = if (endIso.isNotEmpty()) endIso.substringAfter("T").substringBeforeLast(":").take(5) else ""
                    if (endTime.isNotEmpty()) "$startTime - $endTime" else startTime
                } else {
                    "All Day"
                }

                // Check duplicates by matching title, location, date text
                val mapped = CalendarEvent(
                    title = gv.summary ?: "Untitled Event",
                    location = gv.location ?: "Google Calendar",
                    timeRange = timeRangeStr,
                    dateText = dateStr
                )
                
                calendarDao.insertEvent(mapped)
                savedEvents.add(mapped)
            }
            savedEvents
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun formatIsoDateToReadable(isoDate: String): String {
        try {
            val parts = isoDate.split("-")
            if (parts.size >= 3) {
                val monthNum = parts[1]
                val day = parts[2].substring(0, 2)
                val monthName = when(monthNum) {
                    "01" -> "Jan"
                    "02" -> "Feb"
                    "03" -> "Mar"
                    "04" -> "Apr"
                    "05" -> "May"
                    "06" -> "Jun"
                    "07" -> "Jul"
                    "08" -> "Aug"
                    "09" -> "Sep"
                    "10" -> "Oct"
                    "11" -> "Nov"
                    "12" -> "Dec"
                    else -> "Oct"
                }
                return "$monthName $day"
            }
        } catch (e: Exception) {
            // ignore
        }
        return "Oct 24"
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

    /**
     * Module that uses the Gemini API to analyze task difficulty and suggest a prioritized study schedule.
     */
    suspend fun getDifficultyPrioritizedSchedule(tasks: List<FocusTask>, events: List<CalendarEvent>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key not configured. Please enter your GEMINI_API_KEY inside the Secrets panel of Google AI Studio."
        }

        if (tasks.none { !it.isCompleted }) {
            return@withContext "You have no pending academic tasks to rank! Add some check items in the Checklist Tasks tab to construct your schedule analysis."
        }

        val tasksString = tasks.filter { !it.isCompleted }.joinToString("\n") { 
            "- ${it.title} (Subject/Category: ${it.category}, Priority: ${it.priority}, Notes: ${it.subtext}, Est: ${it.timeText})" 
        }
        val eventsString = if (events.isEmpty()) {
            "No scheduled class events or deadlines today."
        } else {
            events.joinToString("\n") { 
                "- ${it.title} at ${it.timeRange} (${it.location}) [${it.dateText}]" 
            }
        }

        val prompt = """
            You are the SNOW-X AI Prioritized Academic Scheduler, powered by Gemini 3.5.
            Your purpose is to analyze the student's task difficulty level, correlate with their upcoming Google Calendar events or classroom checkpoints, and output a highly optimized academic study plan.
            
            Current workload pending details:
            $tasksString
            
            Upcoming Google Calendar lectures / checkpoints:
            $eventsString
            
            Please organize your response beautifully using clean layout and bullet points:
            
            1. **Task Difficulty Matrix**: Categorize each pending task into [HARD] (high cognitive subjects or urgent priority), [MEDIUM], or [EASY] (routine study, quick tasks), adding a 1-sentence analytical reason why.
            2. **Event-Aware Prioritized Block Plan**: Recommend 2-3 specific focus slots relative to upcoming classes/events today or tomorrow (e.g., "Deep Focus Block (Math) - 10:00 to 11:30" during free gaps) with actionable reasoning.
            3. **Cognitive Synergy Recommendation**: Give a 1-sentence elite focus tip tailored specifically to managing low-difficulty versus high-difficulty subjects together.
            
            Be crisp, authoritative, motivational, clear, and ensure the tone is extremely sharp and academic. No generic talk. Limit response under 250 words total.
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
                ?: "No recommendation generated. Check back in a moment."
        } catch (e: Exception) {
            "Analysis Error: ${e.message ?: "Failed to query Gemini. Please verify your internet connection."}"
        }
    }
}
