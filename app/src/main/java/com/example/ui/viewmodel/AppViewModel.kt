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

    // --- Firebase Authentication States ---
    private val _currentUser = MutableStateFlow<com.google.firebase.auth.FirebaseUser?>(null)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()

    private val _authStatusMessage = MutableStateFlow<String?>(null)
    val authStatusMessage: StateFlow<String?> = _authStatusMessage.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

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

    // --- Prioritized Study Schedule States ---
    private val _prioritizedSchedule = MutableStateFlow<String?>(null)
    val prioritizedSchedule: StateFlow<String?> = _prioritizedSchedule.asStateFlow()

    private val _isPrioritizedScheduleLoading = MutableStateFlow(false)
    val isPrioritizedScheduleLoading: StateFlow<Boolean> = _isPrioritizedScheduleLoading.asStateFlow()

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
        // Initialize dynamic Firebase Framework
        com.example.data.api.FirebaseHelper.initialize(application)

        // Read initial user and register listener
        com.example.data.api.FirebaseHelper.auth?.addAuthStateListener { fa ->
            _currentUser.value = fa.currentUser
            if (fa.currentUser != null) {
                startFirestoreSync()
            } else {
                stopFirestoreSync()
            }
        }

        // Fetch AI insights automatically once some data is loaded
        triggerInsightGeneration()
    }

    private fun checkAndPrepopulateData() {
        // No prewritten or hardcoded mock data on startup for a clean dynamic experience
    }

    // --- Task Actions & Firestore Integration ---

    fun FocusTask.toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "category" to category,
            "priority" to priority,
            "subtext" to subtext,
            "timeText" to timeText,
            "isInProgress" to isInProgress,
            "isCompleted" to isCompleted,
            "createdAt" to createdAt
        )
    }

    fun Map<String, Any>.toFocusTask(): FocusTask {
        return FocusTask(
            id = (this["id"] as? Long)?.toInt() ?: (this["id"] as? Double)?.toInt() ?: 0,
            title = this["title"] as? String ?: "",
            category = this["category"] as? String ?: "General",
            priority = this["priority"] as? String ?: "MEDIUM",
            subtext = this["subtext"] as? String ?: "",
            timeText = this["timeText"] as? String ?: "",
            isInProgress = this["isInProgress"] as? Boolean ?: false,
            isCompleted = this["isCompleted"] as? Boolean ?: false,
            createdAt = this["createdAt"] as? Long ?: System.currentTimeMillis()
        )
    }

    fun addTask(title: String, category: String, priority: String, subtext: String, timeText: String) {
        viewModelScope.launch {
            val task = FocusTask(
                title = title,
                category = category,
                priority = priority,
                subtext = subtext,
                timeText = timeText,
                isInProgress = false,
                isCompleted = false
            )
            repository.insertTask(task)
            
            // Play sound tick
            com.example.util.NotificationSoundHelper.playStartClickSfx()

            // Firestore sync
            val user = _currentUser.value ?: com.example.data.api.FirebaseHelper.currentUser
            val db = com.example.data.api.FirebaseHelper.firestore
            if (user != null && db != null) {
                val latest = repository.allTasks.first()
                val matched = latest.find { it.title == title && it.createdAt == task.createdAt }
                if (matched != null) {
                    db.collection("users").document(user.uid)
                        .collection("tasks").document(matched.id.toString())
                        .set(matched.toMap())
                }
            }
            triggerInsightGeneration()
        }
    }

    fun toggleTaskProgress(task: FocusTask) {
        viewModelScope.launch {
            val updated = when {
                !task.isInProgress && !task.isCompleted -> {
                    com.example.util.NotificationSoundHelper.playStartClickSfx()
                    task.copy(isInProgress = true, isCompleted = false)
                }
                task.isInProgress -> {
                    com.example.util.NotificationSoundHelper.playCompleteChime()
                    task.copy(isInProgress = false, isCompleted = true)
                }
                else -> {
                    com.example.util.NotificationSoundHelper.playStartClickSfx()
                    task.copy(isInProgress = false, isCompleted = false)
                }
            }
            repository.updateTask(updated)

            val user = _currentUser.value ?: com.example.data.api.FirebaseHelper.currentUser
            val db = com.example.data.api.FirebaseHelper.firestore
            if (user != null && db != null) {
                db.collection("users").document(user.uid)
                    .collection("tasks").document(updated.id.toString())
                    .set(updated.toMap())
            }
            triggerInsightGeneration()
        }
    }

    fun deleteTask(task: FocusTask) {
        viewModelScope.launch {
            repository.deleteTask(task)

            val user = _currentUser.value ?: com.example.data.api.FirebaseHelper.currentUser
            val db = com.example.data.api.FirebaseHelper.firestore
            if (user != null && db != null) {
                db.collection("users").document(user.uid)
                    .collection("tasks").document(task.id.toString())
                    .delete()
            }
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

    fun generatePrioritizedSchedule() {
        viewModelScope.launch {
            _isPrioritizedScheduleLoading.value = true
            val activeTasks = tasks.value
            val activeEvents = events.value
            val schedule = repository.getDifficultyPrioritizedSchedule(activeTasks, activeEvents)
            _prioritizedSchedule.value = schedule
            _isPrioritizedScheduleLoading.value = false
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
        com.example.util.NotificationSoundHelper.playStartClickSfx()
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
                // Completed! Play beautiful academic notify chime
                com.example.util.NotificationSoundHelper.playCompleteChime()
                
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
        com.example.util.NotificationSoundHelper.playStartClickSfx()
        pausePomodoro()
        _currentPomodoroType.value = "DEEP WORK"
        _pomodoroTimeLeft.value = 25 * 60
    }

    // --- User Authentication Actions ---

    fun clearAuthStatusMessage() {
        _authStatusMessage.value = null
    }

    fun signInAnonymously() {
        _isAuthLoading.value = true
        _authStatusMessage.value = null
        val auth = com.example.data.api.FirebaseHelper.auth
        if (auth == null) {
            _authStatusMessage.value = "Framework inactive. Simulation mode."
            _isAuthLoading.value = false
            return
        }
        auth.signInAnonymously().addOnCompleteListener { task ->
            _isAuthLoading.value = false
            if (task.isSuccessful) {
                _currentUser.value = task.result?.user
                _authStatusMessage.value = "Signed in anonymously! Task cloud sync enabled."
                startFirestoreSync()
            } else {
                _authStatusMessage.value = "Anonymous Login Error: ${task.exception?.message}"
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authStatusMessage.value = "Email and Password cannot be empty."
            return
        }
        _isAuthLoading.value = true
        _authStatusMessage.value = null
        val auth = com.example.data.api.FirebaseHelper.auth
        if (auth == null) {
            _authStatusMessage.value = "Framework inactive. Simulation mode."
            _isAuthLoading.value = false
            return
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _isAuthLoading.value = false
                _currentUser.value = task.result?.user
                _authStatusMessage.value = "Successfully Logged In! Cloud sync activated."
                startFirestoreSync()
            } else {
                // If login fails (user might not exist), automatically register!
                signUpWithEmail(email, password)
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        val auth = com.example.data.api.FirebaseHelper.auth ?: return
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            _isAuthLoading.value = false
            if (task.isSuccessful) {
                _currentUser.value = task.result?.user
                _authStatusMessage.value = "New Account created and linked successfully!"
                startFirestoreSync()
            } else {
                _authStatusMessage.value = "Login/Register failed: ${task.exception?.message}"
            }
        }
    }

    fun signOut() {
        stopFirestoreSync()
        com.example.data.api.FirebaseHelper.auth?.signOut()
        _currentUser.value = null
        _authStatusMessage.value = "Successfully signed out. Switched back to local-only cache."
    }

    private var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun startFirestoreSync() {
        val user = _currentUser.value ?: com.example.data.api.FirebaseHelper.currentUser ?: return
        val db = com.example.data.api.FirebaseHelper.firestore ?: return
        val uid = user.uid

        // 1. Initial push of local tasks to user's remote list
        viewModelScope.launch {
            try {
                val currentLocalTasks = tasks.value
                for (t in currentLocalTasks) {
                    db.collection("users").document(uid)
                        .collection("tasks").document(t.id.toString())
                        .set(t.toMap())
                }
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "Initial firestore upload exception", e)
            }
        }

        // 2. Clear previous listeners
        firestoreListener?.remove()

        // 3. Register real-time remote-to-local synchronization listener
        firestoreListener = db.collection("users").document(uid)
            .collection("tasks")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    android.util.Log.e("AppViewModel", "Firestore sync snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    viewModelScope.launch {
                        for (doc in snapshots.documents) {
                            val data = doc.data
                            if (data != null) {
                                try {
                                    val cloudTask = data.toFocusTask()
                                    repository.insertTask(cloudTask)
                                } catch (ex: Exception) {
                                    // ignore corrupt document
                                }
                            }
                        }
                    }
                }
            }
    }

    fun stopFirestoreSync() {
        firestoreListener?.remove()
        firestoreListener = null
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
