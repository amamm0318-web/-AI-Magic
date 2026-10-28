package com.example.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class AppPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("ai_studio_prefs", Context.MODE_PRIVATE)

  var customApiKey: String
    get() = prefs.getString("custom_api_key", "").orEmpty()
    set(value) = prefs.edit().putString("custom_api_key", value.trim()).apply()

  val effectiveApiKey: String
    get() {
      val custom = customApiKey
      if (custom.isNotBlank()) return custom
      val buildKey = BuildConfig.GEMINI_API_KEY
      if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
        return buildKey
      }
      return ""
    }

  val hasValidApiKey: Boolean
    get() = effectiveApiKey.isNotBlank()

  var speechRate: Float
    get() = prefs.getFloat("tts_speech_rate", 1.0f)
    set(value) = prefs.edit().putFloat("tts_speech_rate", value).apply()

  var speechPitch: Float
    get() = prefs.getFloat("tts_speech_pitch", 1.0f)
    set(value) = prefs.edit().putFloat("tts_speech_pitch", value).apply()

  var isAutoSpeakEnabled: Boolean
    get() = prefs.getBoolean("tts_auto_speak", false)
    set(value) = prefs.edit().putBoolean("tts_auto_speak", value).apply()
}
