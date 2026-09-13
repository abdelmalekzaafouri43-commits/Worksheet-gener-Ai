package com.example.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessage
import com.example.data.Worksheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppThemeStyle {
    DEFAULT,
    CYBER_VOID,
    ALPINE_FROST,
    SAGE_GARDEN,
    HYPER_BERRY
}

class AppViewModel(context: Context) : ViewModel() {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _isPremium = MutableStateFlow(prefs.getBoolean("is_premium", false))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _appTheme = MutableStateFlow(AppThemeStyle.DEFAULT)
    val appTheme: StateFlow<AppThemeStyle> = _appTheme.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setAppTheme(theme: AppThemeStyle) {
        _appTheme.value = theme
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    private val _tokens = MutableStateFlow(prefs.getInt("tokens", 3))
    val tokens: StateFlow<Int> = _tokens.asStateFlow()

    private val _recentWorksheets = MutableStateFlow<List<Worksheet>>(emptyList())
    val recentWorksheets: StateFlow<List<Worksheet>> = _recentWorksheets.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage(text = "Hello! I'm your AI English Tutor. How can I help you today?", isUser = false))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isTutorTyping = MutableStateFlow(false)
    val isTutorTyping: StateFlow<Boolean> = _isTutorTyping.asStateFlow()

    private val _currentWorksheet = MutableStateFlow<Worksheet?>(null)
    val currentWorksheet: StateFlow<Worksheet?> = _currentWorksheet.asStateFlow()

    fun setPremium(status: Boolean) {
        _isPremium.value = status
        prefs.edit().putBoolean("is_premium", status).apply()
        if (status) {
            _tokens.value = 999
            prefs.edit().putInt("tokens", 999).apply()
        }
    }

    fun generateWorksheet(difficulty: String, topic: String, type: String) {
        if (!isPremium.value && tokens.value <= 0) return
        
        viewModelScope.launch {
            _isGenerating.value = true
            delay(2000) // Mock 2-second generation
            
            val worksheet = when (difficulty) {
                "Elementary" -> Worksheet(
                    title = "Basic Grammar: $topic",
                    description = "Elementary level worksheet focusing on basic grammar.",
                    content = "1. The cat ___ (is/are) sleeping.\n2. She ___ (has/have) a red apple.\n3. They ___ (play/plays) outside.\n\nDraw a line to the correct word:\nApple        Blue\nSky          Red",
                    answerKey = "1. is\n2. has\n3. play\nApple -> Red, Sky -> Blue",
                    difficulty = difficulty,
                    illustrationRes = com.example.R.drawable.img_worksheet_elementary
                )
                "Middle School" -> Worksheet(
                    title = "Vocabulary Builder: $topic",
                    description = "Expand your middle school vocabulary.",
                    content = "Match the synonym:\n1. Huge     a. Tiny\n2. Happy    b. Enormous\n3. Small    c. Joyful\n\nWrite a sentence using the word 'Enormous':\n________________________________________________",
                    answerKey = "1-b, 2-c, 3-a\n(Sentence answers will vary)",
                    difficulty = difficulty,
                    illustrationRes = com.example.R.drawable.img_worksheet_middle
                )
                else -> Worksheet(
                    title = "Advanced Comprehension: $topic",
                    description = "High school reading comprehension.",
                    content = "Read the following passage:\n'The advent of technology has profoundly altered human communication, shifting it from deeply personal handwritten notes to instantaneous, brief digital messages.'\n\n1. What does the word 'advent' mean in this context?\n2. Discuss the author's tone regarding the shift in communication.",
                    answerKey = "1. The arrival or invention of something notable.\n2. The tone is objective but slightly nostalgic for 'deeply personal' communication.",
                    difficulty = difficulty,
                    illustrationRes = com.example.R.drawable.img_worksheet_advanced
                )
            }
            
            _currentWorksheet.value = worksheet
            _recentWorksheets.update { listOf(worksheet) + it }
            
            if (!isPremium.value) {
                _tokens.update { it - 1 }
                prefs.edit().putInt("tokens", _tokens.value).apply()
            }
            _isGenerating.value = false
        }
    }

    fun viewWorksheet(worksheet: Worksheet) {
        _currentWorksheet.value = worksheet
    }
    
    fun clearCurrentWorksheet() {
        _currentWorksheet.value = null
    }

    fun sendChatMessage(message: String) {
        if (message.isBlank()) return
        _chatMessages.update { it + ChatMessage(text = message, isUser = true) }
        
        viewModelScope.launch {
            _isTutorTyping.value = true
            try {
                // Add a temporary typing indicator if needed, or just wait for the response
                val history = _chatMessages.value
                    .filter { it.text != "Hello! I'm your AI English Tutor. How can I help you today?" && !it.text.startsWith("Error:") }
                    .map {
                        com.example.api.Content(
                            parts = listOf(com.example.api.Part(text = it.text)),
                            role = if (it.isUser) "user" else "model"
                        )
                    }
                
                val request = com.example.api.GenerateContentRequest(
                    contents = history,
                    systemInstruction = com.example.api.Content(
                        parts = listOf(com.example.api.Part(text = "You are a helpful and knowledgeable AI English Tutor. You help users practice English grammar, vocabulary, and conversation."))
                    )
                )
                
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                val response = com.example.api.RetrofitClient.service.generateContent(
                    model = "gemini-1.5-flash",
                    apiKey = apiKey, 
                    request = request
                )
                val aiText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I'm sorry, I couldn't understand that."
                
                _chatMessages.update { it + ChatMessage(text = aiText, isUser = false) }
            } catch (e: Exception) {
                _chatMessages.update { it + ChatMessage(text = "Error: ${e.message}", isUser = false) }
            } finally {
                _isTutorTyping.value = false
            }
        }
    }
}

class AppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
