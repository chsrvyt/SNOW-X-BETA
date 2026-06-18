package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CalendarEvent
import com.example.data.model.FocusTask
import com.example.data.model.NoteItem
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // --- Database Flows ---
    val tasks: StateFlow<List<FocusTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val notes: StateFlow<List<NoteItem>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val events: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- Google Calendar Sync States ---
    private val _isCalendarSyncing = MutableStateFlow(false)
    val isCalendarSyncing: StateFlow<Boolean> = _isCalendarSyncing.asStateFlow()

    private val _calendarSyncError = MutableStateFlow<String?>(null)
    val calendarSyncError: StateFlow<String?> = _calendarSyncError.asStateFlow()

    private val _calendarSyncSuccess = MutableStateFlow(false)
    val calendarSyncSuccess: StateFlow<Boolean> = _calendarSyncSuccess.asStateFlow()

    // --- AI Insight States ---
    private val _studyInsights = MutableStateFlow("Tap 'Refresh Insights' to align tasks with Gemini AI...")
    val studyInsights: StateFlow<String> = _studyInsights.asStateFlow()

    private val _isInsightsLoading = MutableStateFlow(false)
    val isInsightsLoading: StateFlow<Boolean> = _isInsightsLoading.asStateFlow()

    // --- AI Chat Assistant States ---
    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Hello! I am your SNOW-X Study Advisor. How can I assist you with your classwork, calendar planning, or Pomodoro sessions today?" to false
        )
    )
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // --- Pomodoro States ---
    private val _pomodoroTimeLeft = MutableStateFlow(25 * 60)
    val pomodoroTimeLeft: StateFlow<Int> = _pomodoroTimeLeft.asStateFlow()

    private val _isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroRunning: StateFlow<Boolean> = _isPomodoroRunning.asStateFlow()

    private val _currentPomodoroSession = MutableStateFlow("01/04")
    val currentPomodoroSession: StateFlow<String> = _currentPomodoroSession.asStateFlow()

    private val _currentPomodoroType = MutableStateFlow("DEEP WORK")
    val currentPomodoroType: StateFlow<String> = _currentPomodoroType.asStateFlow()

    private var pomodoroJob: Job? = null

    init {
        // Prepopulate empty database with mock items on startup
        checkAndPrepopulateData()
        // Fetch AI insights automatically once some data is loaded
        triggerInsightGeneration()
    }

    private fun checkAndPrepopulateData() {
        viewModelScope.launch {
            // Check tasks
            tasks.first { true } // wait for first emission
            if (tasks.value.isEmpty()) {
                // Populate default mockup tasks
                repository.insertTask(FocusTask(title = "Finish Calculus HW", category = "Math", priority = "HIGH", subtext = "Due soon • Finance", timeText = "Started 09:30 AM", isInProgress = true, isCompleted = false))
                repository.insertTask(FocusTask(title = "Data Structures Lab", category = "Technical", priority = "URGENT", subtext = "Priority: High", timeText = "11:00 AM", isInProgress = false, isCompleted = false))
                repository.insertTask(FocusTask(title = "Group Project Sync", category = "Planning", priority = "MEDIUM", subtext = "3 Subtasks pending", timeText = "2:00 PM", isInProgress = false, isCompleted = false))
                repository.insertTask(FocusTask(title = "Review Weekly Metrics", category = "Analytics", priority = "LOW", subtext = "Analytics", timeText = "Done 08:45 AM", isInProgress = false, isCompleted = true))
            }
            // Check calendar events
            events.first { true }
            if (events.value.isEmpty()) {
                // Populate default classes
                repository.insertEvent(CalendarEvent(title = "Intro to Algorithms (Lec)", location = "Zoom", timeRange = "14:00 - 15:30", dateText = "Oct 24"))
                repository.insertEvent(CalendarEvent(title = "Discrete Math (Sem)", location = "Boardroom B", timeRange = "09:00 - 10:00", dateText = "Oct 25"))
            }
            // Check notes
            notes.first { true }
            if (notes.value.isEmpty()) {
                repository.insertNote(NoteItem(title = "Algorithms Cheat Sheet", content = "Merge Sort complexity: O(N log N) average/worst. Binary Search: O(log N) in sorted array. Dijkstra's Algorithm finds the shortest path: O((E+V) log V). Space complexity for quicksort is O(log N). Use dynamic programming when subproblems overlap.", summary = "• Focus on Dijkstra's complexity O((E+V) log V) and dynamic programming definitions.\n• Big-O review includes Merge Sort (O(N log N))."))
                repository.insertNote(NoteItem(title = "Calculus Derivatives", content = "Main derivative formulas:\nd/dx(sin x) = cos x\nd/dx(cos x) = -sin x\nd/dx(tan x) = sec^2 x\nd/dx(e^x) = e^x\nChain rule: [f(g(x))]' = f'(g(x)) * g'(x)\nProduct rule: [u*v]' = u'*v + u*v'. Need formulas for today's quiz.", summary = "• Main derivative formulas include trigonometric, exponential functions, and Chain rule f'(g(x)) * g'(x).\n• Review these specific equations before the upcoming weekly online exam."))
            }
        }
    }

    // --- Task Actions ---

    fun addTask(title: String, category: String, priority: String, subtext: String, timeText: String) {
        viewModelScope.launch {
            repository.insertTask(FocusTask(
                title = title,
                category = category,
                priority = priority,
                subtext = subtext,
                timeText = timeText,
                isInProgress = false,
                isCompleted = false
            ))
            triggerInsightGeneration()
        }
    }

    fun toggleTaskProgress(task: FocusTask) {
        viewModelScope.launch {
            val updated = when {
                !task.isInProgress && !task.isCompleted -> task.copy(isInProgress = true, isCompleted = false)
                task.isInProgress -> task.copy(isInProgress = false, isCompleted = true)
                else -> task.copy(isInProgress = false, isCompleted = false)
            }
            repository.updateTask(updated)
            triggerInsightGeneration()
        }
    }

    fun deleteTask(task: FocusTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
            triggerInsightGeneration()
        }
    }

    // --- Note Actions ---

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            repository.insertNote(NoteItem(title = title, content = content))
        }
    }

    fun deleteNote(note: NoteItem) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun summarizeNoteAsync(note: NoteItem) {
        viewModelScope.launch {
            val summary = repository.summarizeNote(note.title, note.content)
            repository.updateNote(note.copy(summary = summary))
        }
    }

    // --- Event Actions ---

    fun addEvent(title: String, location: String, timeRange: String, dateText: String) {
        viewModelScope.launch {
            repository.insertEvent(CalendarEvent(
                title = title,
                location = location,
                timeRange = timeRange,
                dateText = dateText
            ))
            triggerInsightGeneration()
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            triggerInsightGeneration()
        }
    }

    // --- Google Calendar Sync Actions ---

    fun syncGoogleCalendar(token: String) {
        viewModelScope.launch {
            _isCalendarSyncing.value = true
            _calendarSyncError.value = null
            _calendarSyncSuccess.value = false
            try {
                if (token.isBlank() || token == "simulation_token") {
                    // Simulates dynamic retrieval or sync of academic events in the workspace sandbox if token is empty
                    delay(1200)
                    repository.insertEvent(CalendarEvent(
                        title = "Google Core AI Workshop",
                        location = "AIDA Lab Suite",
                        timeRange = "10:00 - 11:30",
                        dateText = "Oct 24"
                    ))
                    repository.insertEvent(CalendarEvent(
                        title = "Advanced NLP Lecture",
                        location = "Tech Tower Room 42",
                        timeRange = "13:00 - 14:30",
                        dateText = "Oct 24"
                    ))
                    repository.insertEvent(CalendarEvent(
                        title = "SNOW-X Developer Sync",
                        location = "Google Meet",
                        timeRange = "16:00 - 17:00",
                        dateText = "Oct 25"
                    ))
                    _calendarSyncSuccess.value = true
                } else {
                    repository.syncGoogleCalendar(token)
                    _calendarSyncSuccess.value = true
                }
                triggerInsightGeneration()
            } catch (e: Exception) {
                _calendarSyncError.value = e.message ?: "Authentication failed or token invalid. Check Google account link."
            } finally {
                _isCalendarSyncing.value = false
            }
        }
    }

    fun resetCalendarSyncStatus() {
        _calendarSyncSuccess.value = false
        _calendarSyncError.value = null
    }

    // --- AI Insight Operations ---

    fun triggerInsightGeneration() {
        viewModelScope.launch {
            _isInsightsLoading.value = true
            // Grab current checklist and events snapshot
            val activeTasks = tasks.value
            val activeEvents = events.value
            val insight = repository.getStudyInsights(activeTasks, activeEvents)
            _studyInsights.value = insight
            _isInsightsLoading.value = false
        }
    }

    // --- AI Chat Companion ---

    fun sendChatMessage(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            // Add user message
            val currentHistory = _chatMessages.value.toMutableList()
            currentHistory.add(message to true)
            _chatMessages.value = currentHistory

            _isChatLoading.value = true
            val aiResponse = repository.getAssistantResponse(currentHistory, message)
            _isChatLoading.value = false

            // Add advisor message
            val updatedHistory = _chatMessages.value.toMutableList()
            updatedHistory.add(aiResponse to false)
            _chatMessages.value = updatedHistory
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            "Welcome back! I am your SNOW-X Study Advisor. Let me know how I can assist with tasks, course load, or mental focus coaching." to false
        )
    }

    // --- Pomodoro Operations ---

    fun togglePomodoro() {
        if (_isPomodoroRunning.value) {
            pausePomodoro()
        } else {
            startPomodoro()
        }
    }

    private fun startPomodoro() {
        _isPomodoroRunning.value = true
        pomodoroJob = viewModelScope.launch {
            while (_pomodoroTimeLeft.value > 0 && _isPomodoroRunning.value) {
                delay(1000)
                _pomodoroTimeLeft.value--
            }
            if (_pomodoroTimeLeft.value == 0) {
                // Completed! Switch modes or play beep
                if (_currentPomodoroType.value == "DEEP WORK") {
                    _currentPomodoroType.value = "SHORT BREAK"
                    _pomodoroTimeLeft.value = 5 * 60
                } else {
                    _currentPomodoroType.value = "DEEP WORK"
                    _pomodoroTimeLeft.value = 25 * 60
                    // loop sessions
                    val currentSessionNum = _currentPomodoroSession.value.substringBefore("/").toInt()
                    val totalSessionNum = _currentPomodoroSession.value.substringAfter("/").toInt()
                    val nextSessionNum = if (currentSessionNum < totalSessionNum) currentSessionNum + 1 else 1
                    _currentPomodoroSession.value = String.format("%02d/%02d", nextSessionNum, totalSessionNum)
                }
                _isPomodoroRunning.value = false
            }
        }
    }

    fun presetTaskPomodoro(taskTitle: String) {
        // Set deep work focus mode for a designated task
        _currentPomodoroType.value = "DEEP WORK"
        _pomodoroTimeLeft.value = 25 * 60
        startPomodoro()
    }

    fun pausePomodoro() {
        _isPomodoroRunning.value = false
        pomodoroJob?.cancel()
        pomodoroJob = null
    }

    fun resetPomodoro() {
        pausePomodoro()
        _currentPomodoroType.value = "DEEP WORK"
        _pomodoroTimeLeft.value = 25 * 60
    }
}

// --- ViewModel Factory ---

class AppViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = AppRepository(database.taskDao(), database.noteDao(), database.calendarDao())
            return AppViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
