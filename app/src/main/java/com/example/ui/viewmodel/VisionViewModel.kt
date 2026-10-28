package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.model.AiModelOption
import com.example.data.model.VisionAnalysisMode
import com.example.data.prefs.AppPreferences
import com.example.data.tts.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VisionViewModel(application: Application) : AndroidViewModel(application) {
  val prefs = AppPreferences(application)
  val ttsManager = TextToSpeechManager(application)

  private val _selectedImageBase64 = MutableStateFlow<String?>(null)
  val selectedImageBase64: StateFlow<String?> = _selectedImageBase64.asStateFlow()

  private val _selectedMode = MutableStateFlow(VisionAnalysisMode.GENERAL)
  val selectedMode: StateFlow<VisionAnalysisMode> = _selectedMode.asStateFlow()

  private val _customPrompt = MutableStateFlow("")
  val customPrompt: StateFlow<String> = _customPrompt.asStateFlow()

  private val _useHighThinking = MutableStateFlow(true)
  val useHighThinking: StateFlow<Boolean> = _useHighThinking.asStateFlow()

  private val _analysisResult = MutableStateFlow<String?>(null)
  val analysisResult: StateFlow<String?> = _analysisResult.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  fun setImage(base64: String?) {
    _selectedImageBase64.value = base64
    _analysisResult.value = null
    _errorMessage.value = null
  }

  fun setMode(mode: VisionAnalysisMode) {
    _selectedMode.value = mode
  }

  fun setCustomPrompt(prompt: String) {
    _customPrompt.value = prompt
  }

  fun toggleHighThinking() {
    _useHighThinking.value = !_useHighThinking.value
  }

  fun analyzeImage() {
    val imageBase64 = _selectedImageBase64.value
    if (imageBase64.isNullOrBlank()) {
      _errorMessage.value = "يرجى اختيار صورة أولاً للتحليل."
      return
    }

    val mode = _selectedMode.value
    val prompt = if (mode == VisionAnalysisMode.CUSTOM) {
      if (_customPrompt.value.isBlank()) "صف هذه الصورة وما تشاهده فيها بالتفصيل." else _customPrompt.value.trim()
    } else {
      mode.promptInstruction
    }

    _isLoading.value = true
    _errorMessage.value = null
    _analysisResult.value = null

    viewModelScope.launch {
      val apiKey = prefs.effectiveApiKey
      val modelName = if (_useHighThinking.value) {
        AiModelOption.GEMINI_4_ARGON.id
      } else {
        AiModelOption.GEMINI_3_8_FLASH.id
      }

      val result = GeminiApiClient.generateContent(
        apiKey = apiKey,
        modelName = modelName,
        prompt = prompt,
        enableHighThinking = _useHighThinking.value,
        imageBase64 = imageBase64
      )

      _isLoading.value = false

      result.onSuccess { text ->
        _analysisResult.value = text
      }.onFailure { ex ->
        _errorMessage.value = ex.localizedMessage ?: "فشل تحليل الصورة."
      }
    }
  }

  fun speakResult() {
    val text = _analysisResult.value
    if (!text.isNullOrBlank()) {
      ttsManager.speak(text)
    }
  }

  fun stopSpeaking() {
    ttsManager.stop()
  }

  fun clearError() {
    _errorMessage.value = null
  }

  override fun onCleared() {
    super.onCleared()
    ttsManager.shutdown()
  }
}
