package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.model.AiModelOption
import com.example.data.model.AiRole
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.prefs.AppPreferences
import com.example.data.tts.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
  val prefs = AppPreferences(application)
  val ttsManager = TextToSpeechManager(application)

  private val _messages = MutableStateFlow<List<ChatMessage>>(
    listOf(
      ChatMessage(
        text = "مرحباً بك في استوديو الذكاء الاصطناعي! 🚀\nأنا جاهز لمساعدتك في كل ما تحتاج: كتابة الأكواد، التفكير المعقد، صياغة المحتوى، وتحليل المهام. تفضل بسؤالي!",
        sender = MessageSender.AI,
        modelName = AiModelOption.GEMINI_3_8_FLASH.displayName
      )
    )
  )
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  private val _selectedModel = MutableStateFlow(AiModelOption.GEMINI_3_8_FLASH)
  val selectedModel: StateFlow<AiModelOption> = _selectedModel.asStateFlow()

  private val _selectedRole = MutableStateFlow(AiRole.presets.first())
  val selectedRole: StateFlow<AiRole> = _selectedRole.asStateFlow()

  private val _isHighThinkingEnabled = MutableStateFlow(false)
  val isHighThinkingEnabled: StateFlow<Boolean> = _isHighThinkingEnabled.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _attachedImageBase64 = MutableStateFlow<String?>(null)
  val attachedImageBase64: StateFlow<String?> = _attachedImageBase64.asStateFlow()

  private val _isAutoSpeakActive = MutableStateFlow(prefs.isAutoSpeakEnabled)
  val isAutoSpeakActive: StateFlow<Boolean> = _isAutoSpeakActive.asStateFlow()

  fun toggleAutoSpeak() {
    val newVal = !_isAutoSpeakActive.value
    _isAutoSpeakActive.value = newVal
    prefs.isAutoSpeakEnabled = newVal
  }

  fun selectModel(model: AiModelOption) {
    _selectedModel.value = model
    if (!model.supportsThinking) {
      _isHighThinkingEnabled.value = false
    }
  }

  fun toggleHighThinking() {
    val newState = !_isHighThinkingEnabled.value
    _isHighThinkingEnabled.value = newState
    if (newState) {
      // Must use gemini-4-argon for High Thinking
      _selectedModel.value = AiModelOption.GEMINI_4_ARGON
    }
  }

  fun selectRole(role: AiRole) {
    _selectedRole.value = role
  }

  fun attachImage(base64: String?) {
    _attachedImageBase64.value = base64
  }

  fun sendMessage(promptText: String) {
    val cleanPrompt = promptText.trim()
    if (cleanPrompt.isBlank() && _attachedImageBase64.value == null) return

    val currentAttachedImage = _attachedImageBase64.value
    val model = _selectedModel.value
    val highThinking = _isHighThinkingEnabled.value
    val role = _selectedRole.value

    // Clear attachment immediately
    _attachedImageBase64.value = null
    _errorMessage.value = null

    val userMessage = ChatMessage(
      text = cleanPrompt,
      sender = MessageSender.USER,
      imageBase64 = currentAttachedImage
    )

    _messages.value = _messages.value + userMessage
    _isLoading.value = true

    viewModelScope.launch {
      val apiKey = prefs.effectiveApiKey
      val result = GeminiApiClient.generateContent(
        apiKey = apiKey,
        modelName = model.id,
        prompt = cleanPrompt,
        systemInstruction = role.systemPrompt,
        history = _messages.value,
        enableHighThinking = highThinking,
        imageBase64 = currentAttachedImage
      )

      _isLoading.value = false

      result.onSuccess { replyText ->
        val aiMessage = ChatMessage(
          text = replyText,
          sender = MessageSender.AI,
          modelName = model.displayName,
          isThinking = highThinking
        )
        _messages.value = _messages.value + aiMessage

        if (_isAutoSpeakActive.value) {
          ttsManager.speak(replyText, aiMessage.id)
        }
      }.onFailure { ex ->
        _errorMessage.value = ex.localizedMessage ?: "حدث خطأ أثناء المحادثة."
      }
    }
  }

  fun speakText(text: String, messageId: String) {
    ttsManager.speak(text, messageId)
  }

  fun stopSpeaking() {
    ttsManager.stop()
  }

  fun clearChat() {
    _messages.value = listOf(
      ChatMessage(
        text = "تم بدء جلسة محادثة جديدة. كيف يمكنني مساعدتك الآن؟",
        sender = MessageSender.AI,
        modelName = _selectedModel.value.displayName
      )
    )
    _errorMessage.value = null
  }

  fun clearError() {
    _errorMessage.value = null
  }

  override fun onCleared() {
    super.onCleared()
    ttsManager.shutdown()
  }
}
