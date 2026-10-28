package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.model.AspectRatioOption
import com.example.data.model.GeneratedImageItem
import com.example.data.model.ImageResolutionOption
import com.example.data.prefs.AppPreferences
import com.example.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageStudioViewModel(application: Application) : AndroidViewModel(application) {
  val prefs = AppPreferences(application)

  private val _prompt = MutableStateFlow("")
  val prompt: StateFlow<String> = _prompt.asStateFlow()

  private val _selectedAspectRatio = MutableStateFlow(AspectRatioOption.SQUARE_1_1)
  val selectedAspectRatio: StateFlow<AspectRatioOption> = _selectedAspectRatio.asStateFlow()

  private val _selectedResolution = MutableStateFlow(ImageResolutionOption.SIZE_1K)
  val selectedResolution: StateFlow<ImageResolutionOption> = _selectedResolution.asStateFlow()

  private val _editImageBase64 = MutableStateFlow<String?>(null)
  val editImageBase64: StateFlow<String?> = _editImageBase64.asStateFlow()

  private val _currentGeneratedImage = MutableStateFlow<GeneratedImageItem?>(null)
  val currentGeneratedImage: StateFlow<GeneratedImageItem?> = _currentGeneratedImage.asStateFlow()

  private val _gallery = MutableStateFlow<List<GeneratedImageItem>>(emptyList())
  val gallery: StateFlow<List<GeneratedImageItem>> = _gallery.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _isEnhancingPrompt = MutableStateFlow(false)
  val isEnhancingPrompt: StateFlow<Boolean> = _isEnhancingPrompt.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  fun setPrompt(text: String) {
    _prompt.value = text
  }

  fun setAspectRatio(ratio: AspectRatioOption) {
    _selectedAspectRatio.value = ratio
  }

  fun setResolution(res: ImageResolutionOption) {
    _selectedResolution.value = res
  }

  fun setEditImage(base64: String?) {
    _editImageBase64.value = base64
  }

  fun enhancePromptWithGemini() {
    val current = _prompt.value.trim()
    if (current.isBlank()) return

    _isEnhancingPrompt.value = true
    viewModelScope.launch {
      val apiKey = prefs.effectiveApiKey
      val enhanceRequest = "أعد صياغة هذا الوصف البسيط لتحويله إلى وصف فني دقيق وغني بالتفاصيل والإضاءة والأسلوب لتوليد صورة سينمائية مذهلة بالذكاء الاصطناعي (أعطني فقط الوصف الفني النهائي بدون أي مقدمات أو شروحات): $current"
      val result = GeminiApiClient.generateContent(
        apiKey = apiKey,
        modelName = "gemini-3.8-flash",
        prompt = enhanceRequest
      )
      _isEnhancingPrompt.value = false
      result.onSuccess { enhancedText ->
        _prompt.value = enhancedText.trim()
      }
    }
  }

  fun generateImage() {
    val promptText = _prompt.value.trim()
    if (promptText.isBlank()) {
      _errorMessage.value = "يرجى كتابة وصف الصورة المطلوب توليدها."
      return
    }

    _isLoading.value = true
    _errorMessage.value = null

    viewModelScope.launch {
      val apiKey = prefs.effectiveApiKey
      val result = GeminiApiClient.generateImage(
        apiKey = apiKey,
        prompt = promptText,
        aspectRatio = _selectedAspectRatio.value,
        resolution = _selectedResolution.value,
        editImageBase64 = _editImageBase64.value
      )

      _isLoading.value = false

      result.onSuccess { base64 ->
        val cachedFile = ImageUtils.saveBase64ToCacheFile(
          getApplication(),
          base64,
          "gemini_gen_${System.currentTimeMillis()}.jpg"
        )
        val item = GeneratedImageItem(
          prompt = promptText,
          base64Data = base64,
          aspectRatio = _selectedAspectRatio.value,
          filePath = cachedFile?.absolutePath
        )
        _currentGeneratedImage.value = item
        _gallery.value = listOf(item) + _gallery.value
      }.onFailure { ex ->
        _errorMessage.value = ex.localizedMessage ?: "فشل في توليد الصورة."
      }
    }
  }

  fun clearError() {
    _errorMessage.value = null
  }
}
