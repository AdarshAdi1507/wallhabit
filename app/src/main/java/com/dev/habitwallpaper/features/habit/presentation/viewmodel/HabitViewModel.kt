package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.core.notification.AlarmScheduler
import com.dev.habitwallpaper.core.wallpaper.WallpaperManager
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.model.HabitCategory
import com.dev.habitwallpaper.domain.model.TrackingType
import com.dev.habitwallpaper.domain.usecase.CreateHabitUseCase
import com.dev.habitwallpaper.features.habit.presentation.state.HabitUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HabitViewModel @Inject constructor(
    private val createHabitUseCase: CreateHabitUseCase,
    private val alarmScheduler: AlarmScheduler,
    private val wallpaperManager: WallpaperManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUIState())
    val uiState: StateFlow<HabitUIState> = _uiState.asStateFlow()

    private val _customDuration = MutableStateFlow("")
    val customDuration: StateFlow<String> = _customDuration.asStateFlow()

    init {
        observeNameChanges()
    }

    private fun observeNameChanges() {
        viewModelScope.launch {
            _uiState
                .map { it.habitName }
                .distinctUntilChanged()
                .debounce(600L)
                .collect { name ->
                    if (name.isNotBlank()) {
                        detectAndSetCategory(name)
                    }
                }
        }
    }

    private fun detectAndSetCategory(name: String) {
        val lowercaseName = name.lowercase()
        val detectedCategory = when {
            // FITNESS: Sports, gym, outdoor activities, exercises
            listOf(
                "gym", "workout", "run", "fitness", "exercise", "training", "sport", "yoga", "walk", "swim", "cycling",
                "bike", "pilates", "cardio", "strength", "lift", "weights", "crossfit", "hiit", "stretching", "plank",
                "pushup", "situp", "squat", "marathon", "triathlon", "hike", "climb", "boxing", "martial arts", "karate",
                "dance", "zumba", "aerobics", "football", "soccer", "basketball", "tennis", "badminton", "rowing", "surf",
                "skate", "ski", "jog", "athlete", "track", "field", "volleyball", "golf", "rugby", "hockey", "baseball",
                "wrestling", "mma", "taekwondo", "judo", "fencing", "archery", "bowling", "cricket", "tabata", "burpee"
            ).any { lowercaseName.contains(it) } -> HabitCategory.FITNESS
            
            // HEALTH: Physical health, diet, medical, body maintenance
            listOf(
                "meditation", "sleep", "water", "diet", "medicine", "health", "fruit", "doctor", "hydration", "vitamin",
                "calorie", "keto", "vegan", "vegetarian", "nutrition", "protein", "breakfast", "lunch", "dinner", "snack",
                "salad", "veggie", "organic", "fasting", "detox", "dentist", "checkup", "posture", "skin", "hair", "teeth",
                "brush", "floss", "sunscreen", "nap", "rest", "recovery", "therapy", "counsel", "supplement", "mineral",
                "sugar", "salt", "fiber", "carb", "fat", "weight", "bmi", "blood", "pressure", "heart", "lung", "brain"
            ).any { lowercaseName.contains(it) } -> HabitCategory.HEALTH
            
            // LEARNING: Academic, skills, instruments, arts
            listOf(
                "coding", "read", "study", "book", "course", "learn", "language", "guitar", "piano", "skill", "lesson",
                "program", "python", "java", "kotlin", "swift", "javascript", "math", "science", "history", "philosophy",
                "write", "poem", "novel", "library", "podcast", "documentary", "exam", "test", "quiz", "homework",
                "flashcard", "workshop", "seminar", "tutorial", "lecture", "instrument", "violin", "drum", "sing", "art",
                "draw", "paint", "sketch", "sculpt", "craft", "knit", "sew", "photography", "video", "edit", "language",
                "spanish", "french", "german", "chinese", "japanese", "korean", "russian", "arabic", "sign language"
            ).any { lowercaseName.contains(it) } -> HabitCategory.LEARNING
            
            // PRODUCTIVITY: Professional, financial, organizational
            listOf(
                "work", "focus", "email", "task", "project", "plan", "productivity", "writing", "meeting", "deep work",
                "pomodoro", "checklist", "todo", "calendar", "schedule", "deadline", "organize", "clean", "inbox",
                "document", "spreadsheet", "report", "presentation", "client", "business", "career", "money", "budget",
                "save", "invest", "stock", "finance", "tax", "banking", "invoice", "meeting", "call", "office", "resume",
                "job", "interview", "delegate", "prioritize", "efficiency", "workflow"
            ).any { lowercaseName.contains(it) } -> HabitCategory.PRODUCTIVITY
            
            // MINDFULNESS: Mental well-being, spiritual, relaxation
            listOf(
                "mindful", "breath", "calm", "journal", "relax", "zen", "grateful", "gratitude", "prayer", "worship",
                "church", "mosque", "temple", "silence", "solitude", "reflect", "intention", "manifest", "affirm",
                "presence", "peace", "stillness", "tai chi", "qigong", "spa", "massage", "sauna", "bath", "nature",
                "forest", "spiritual", "soul", "kindness", "compassion", "empathy", "forgive", "tarot", "manifestation"
            ).any { lowercaseName.contains(it) } -> HabitCategory.MINDFULNESS
            
            // LIFESTYLE: Home, social, hobbies, chores
            listOf(
                "lifestyle", "hobby", "clean", "cook", "garden", "social", "family", "friend", "pet", "dog", "cat",
                "call", "talk", "visit", "travel", "trip", "vacation", "nature", "park", "outdoor", "shop", "grocery",
                "meal prep", "laundry", "dish", "vacuum", "sweep", "recycle", "community", "volunteer", "charity",
                "event", "party", "concert", "movie", "game", "hobby", "collection", "diy", "home", "decor", "fashion",
                "style", "clothes", "makeup", "grooming", "plant", "water plants"
            ).any { lowercaseName.contains(it) } -> HabitCategory.LIFESTYLE
            
            // PERSONAL DEVELOPMENT: Routines, soft skills, character
            listOf(
                "development", "growth", "routine", "discipline", "goal", "habit", "morning", "evening", "night",
                "wakeup", "early", "bedtime", "wake", "mirror", "confidence", "speech", "public speaking", "leadership",
                "mentor", "coach", "feedback", "review", "change", "improve", "transform", "mindset", "motivation",
                "inspiration", "productivity", "time management", "self-care", "self-love", "boundaries", "assertive"
            ).any { lowercaseName.contains(it) } -> HabitCategory.PERSONAL_DEVELOPMENT
            
            else -> null
        }

        detectedCategory?.let { category ->
            if (_uiState.value.category != category) {
                _uiState.update { it.copy(category = category) }
            }
        }
    }

    fun onHabitNameChange(newName: String) {
        _uiState.update { it.copy(habitName = newName, error = null) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onCategoryChange(newCategory: HabitCategory) {
        _uiState.update { it.copy(category = newCategory) }
    }

    fun onDurationChange(days: Int) {
        _uiState.update { it.copy(durationDays = days) }
    }

    fun onCustomDurationChange(daysString: String) {
        val filtered = daysString.filter { it.isDigit() }
        _customDuration.value = filtered
        val days = filtered.toIntOrNull()
        if (days != null) {
            val clampedDays = days.coerceIn(1, 365)
            _uiState.update { it.copy(durationDays = clampedDays) }
        }
    }

    fun onStartDateChange(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun onReminderEnabledChange(enabled: Boolean) {
        _uiState.update { 
            it.copy(
                isReminderEnabled = enabled,
                reminderTime = if (enabled) it.reminderTime ?: LocalTime.of(9, 0) else null,
                reminderDays = if (enabled && it.reminderDays.isEmpty()) DayOfWeek.entries.toList() else it.reminderDays
            ) 
        }
    }

    fun onReminderModeChange(isDaily: Boolean) {
        _uiState.update { 
            it.copy(
                isDaily = isDaily,
                reminderDays = if (isDaily) DayOfWeek.entries.toList() else emptyList()
            ) 
        }
    }

    fun toggleReminderDay(day: DayOfWeek) {
        _uiState.update { state ->
            val currentDays = state.reminderDays.toMutableList()
            if (currentDays.contains(day)) {
                currentDays.remove(day)
            } else {
                currentDays.add(day)
            }
            state.copy(reminderDays = currentDays, isDaily = currentDays.size == 7)
        }
    }

    fun onReminderTimeChange(time: LocalTime) {
        _uiState.update { it.copy(reminderTime = time) }
    }

    fun onTrackingTypeChange(type: TrackingType) {
        _uiState.update { it.copy(trackingType = type) }
    }

    fun onGoalValueChange(value: Float) {
        _uiState.update { it.copy(goalValue = value) }
    }

    fun onColorChange(color: Int?) {
        _uiState.update { it.copy(color = color) }
    }

    fun onIconChange(icon: String?) {
        _uiState.update { it.copy(icon = icon) }
    }

    fun onWallpaperSelectedChange(selected: Boolean) {
        _uiState.update { it.copy(isWallpaperSelected = selected) }
    }

    fun saveHabit() {
        val currentState = _uiState.value
        if (currentState.habitName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a habit name") }
            return
        }
        
        if (currentState.durationDays <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid duration") }
            return
        }

        if (currentState.isReminderEnabled && currentState.reminderDays.isEmpty()) {
            _uiState.update { it.copy(error = "Please select at least one day for reminders") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val habit = Habit(
                    name = currentState.habitName,
                    description = currentState.description,
                    category = currentState.category,
                    durationDays = currentState.durationDays,
                    startDate = currentState.startDate,
                    reminderTime = if (currentState.isReminderEnabled) currentState.reminderTime ?: LocalTime.of(9, 0) else null,
                    reminderDays = if (currentState.isReminderEnabled) currentState.reminderDays else emptyList(),
                    trackingType = currentState.trackingType,
                    goalValue = currentState.goalValue,
                    color = currentState.color,
                    icon = currentState.icon,
                    isWallpaperSelected = currentState.isWallpaperSelected
                )
                val id = createHabitUseCase(habit)
                
                if (habit.reminderTime != null) {
                    alarmScheduler.scheduleHabitReminders(habit.copy(id = id))
                }

                if (habit.isWallpaperSelected) {
                    wallpaperManager.triggerUpdate()
                }

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save habit") }
            }
        }
    }
}
