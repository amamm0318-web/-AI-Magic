package com.example.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(context: Context) {
  private val TAG = "TTSManager"
  private var tts: TextToSpeech? = null
  private var isInitialized = false

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _currentUtteranceId = MutableStateFlow<String?>(null)
  val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

  private val _currentlySpeakingText = MutableStateFlow<String?>(null)
  val currentlySpeakingText: StateFlow<String?> = _currentlySpeakingText.asStateFlow()

  var speechRate: Float = 1.0f
    set(value) {
      field = value
      tts?.setSpeechRate(value)
    }

  var pitch: Float = 1.0f
    set(value) {
      field = value
      tts?.setPitch(value)
    }

  init {
    tts = TextToSpeech(context.applicationContext) { status ->
      if (status == TextToSpeech.SUCCESS) {
        isInitialized = true
        val arLocale = Locale.forLanguageTag("ar")
        val arResult = tts?.setLanguage(arLocale)
        if (arResult == TextToSpeech.LANG_MISSING_DATA || arResult == TextToSpeech.LANG_NOT_SUPPORTED) {
          Log.w(TAG, "Arabic TTS not fully supported directly, defaulting to US English")
          tts?.setLanguage(Locale.US)
        }
        tts?.setSpeechRate(speechRate)
        tts?.setPitch(pitch)

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(utteranceId: String?) {
            _isPlaying.value = true
            _currentUtteranceId.value = utteranceId
          }

          override fun onDone(utteranceId: String?) {
            _isPlaying.value = false
            _currentUtteranceId.value = null
            _currentlySpeakingText.value = null
          }

          @Deprecated("Deprecated in Java")
          override fun onError(utteranceId: String?) {
            _isPlaying.value = false
            _currentUtteranceId.value = null
            _currentlySpeakingText.value = null
          }
        })
      } else {
        Log.e(TAG, "TTS Initialization failed with code $status")
      }
    }
  }

  /**
   * Cleans Markdown formatting, code fences, and symbols returned by Gemini
   * so the spoken output is completely natural and fluent.
   */
  fun cleanTextForSpeech(raw: String): String {
    return raw
      // Strip code blocks ```code```
      .replace(Regex("```[\\s\\S]*?```"), "كود برمجي")
      // Strip inline code `code`
      .replace(Regex("`([^`]+)`"), "$1")
      // Strip markdown links [label](url) -> label
      .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")
      // Strip bold & italic markers
      .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
      .replace(Regex("\\*([^*]+)\\*"), "$1")
      .replace(Regex("__([^_]+)__"), "$1")
      .replace(Regex("_([^_]+)_"), "$1")
      // Strip markdown headers #, ##, ###
      .replace(Regex("(?m)^#{1,6}\\s+"), "")
      // Strip blockquotes >
      .replace(Regex("(?m)^>\\s+"), "")
      // Strip bullet asterisks or dashes at start of lines
      .replace(Regex("(?m)^[\\*\\-\\+]\\s+"), "")
      // Normalize multiple newlines/spaces
      .replace(Regex("\\n{2,}"), "\n")
      .trim()
  }

  /**
   * Converts Gemini text to speech using Android TextToSpeech.
   * Splits into sentence chunks if text is long to prevent utterance overflow.
   */
  fun speak(text: String, id: String = System.currentTimeMillis().toString()) {
    if (!isInitialized || tts == null) return

    val cleanText = cleanTextForSpeech(text)
    if (cleanText.isBlank()) return

    // Auto-detect language
    val hasArabic = cleanText.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }
    if (hasArabic) {
      tts?.setLanguage(Locale.forLanguageTag("ar"))
    } else {
      tts?.setLanguage(Locale.US)
    }

    tts?.setSpeechRate(speechRate)
    tts?.setPitch(pitch)

    // Stop current speech before starting new
    stop()
    _currentlySpeakingText.value = cleanText

    // If cleanText is within single utterance limit (< 3000 chars)
    if (cleanText.length <= 3000) {
      tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, id)
    } else {
      // Chunk long Gemini answers into smaller pieces
      val chunks = cleanText.chunked(2500)
      for ((index, chunk) in chunks.withIndex()) {
        val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(chunk, queueMode, null, "${id}_$index")
      }
    }
  }

  fun stop() {
    tts?.stop()
    _isPlaying.value = false
    _currentUtteranceId.value = null
    _currentlySpeakingText.value = null
  }

  fun shutdown() {
    stop()
    tts?.shutdown()
    tts = null
  }
}
