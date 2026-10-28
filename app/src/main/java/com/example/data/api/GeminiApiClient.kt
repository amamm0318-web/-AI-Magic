package com.example.data.api

import android.util.Log
import com.example.data.model.AspectRatioOption
import com.example.data.model.ChatMessage
import com.example.data.model.ImageResolutionOption
import com.example.data.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
  private const val TAG = "GeminiApiClient"
  private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"

  private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

  /**
   * Generates text content or multi-turn chat response with optional system instruction,
   * thinking mode (gemini-4-argon with thinkingLevel: HIGH), and multimodal image input.
   */
  suspend fun generateContent(
    apiKey: String,
    modelName: String,
    prompt: String,
    systemInstruction: String? = null,
    history: List<ChatMessage> = emptyList(),
    enableHighThinking: Boolean = false,
    imageBase64: String? = null
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext Result.failure(
          IllegalArgumentException("يرجى إدخال مفتاح Gemini API صالح في الإعدادات أو لوحة الأسرار للمتابعة.")
        )
      }

      val requestJson = JSONObject()

      // System instruction
      if (!systemInstruction.isNullOrBlank()) {
        val sysObj = JSONObject()
        val partsArr = JSONArray().put(JSONObject().put("text", systemInstruction))
        sysObj.put("parts", partsArr)
        requestJson.put("systemInstruction", sysObj)
      }

      // Contents array for multi-turn or single turn
      val contentsArr = JSONArray()

      // Add relevant history (up to last 10 turns)
      val recentHistory = history.takeLast(10)
      for (msg in recentHistory) {
        val roleStr = if (msg.sender == MessageSender.USER) "user" else "model"
        val parts = JSONArray()
        if (!msg.imageBase64.isNullOrBlank()) {
          val inlineData = JSONObject()
            .put("mimeType", "image/jpeg")
            .put("data", msg.imageBase64)
          parts.put(JSONObject().put("inlineData", inlineData))
        }
        if (msg.text.isNotBlank()) {
          parts.put(JSONObject().put("text", msg.text))
        }
        if (parts.length() > 0) {
          contentsArr.put(JSONObject().put("role", roleStr).put("parts", parts))
        }
      }

      // Add current user prompt
      val currentParts = JSONArray()
      if (!imageBase64.isNullOrBlank()) {
        val inlineData = JSONObject()
          .put("mimeType", "image/jpeg")
          .put("data", imageBase64)
        currentParts.put(JSONObject().put("inlineData", inlineData))
      }
      currentParts.put(JSONObject().put("text", prompt))
      contentsArr.put(JSONObject().put("role", "user").put("parts", currentParts))

      requestJson.put("contents", contentsArr)

      // Generation config & Thinking level
      val genConfig = JSONObject()
      genConfig.put("temperature", 0.7)

      if (enableHighThinking && modelName.contains("argon", ignoreCase = true)) {
        // High thinking requirement: gemini-4-argon with thinkingLevel: HIGH, no maxOutputTokens
        val thinkingConfig = JSONObject()
        thinkingConfig.put("thinkingLevel", "HIGH")
        genConfig.put("thinkingConfig", thinkingConfig)
      }

      requestJson.put("generationConfig", genConfig)

      val url = "$BASE_URL/models/$modelName:generateContent?key=$apiKey"
      val request = Request.Builder()
        .url(url)
        .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        Log.e(TAG, "API error: ${response.code} -> $responseBody")
        val errorMsg = parseErrorMessage(response.code, responseBody)
        return@withContext Result.failure(Exception(errorMsg))
      }

      val json = JSONObject(responseBody)
      val candidates = json.optJSONArray("candidates")
      if (candidates != null && candidates.length() > 0) {
        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        if (parts != null && parts.length() > 0) {
          val sb = StringBuilder()
          for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            sb.append(part.optString("text"))
          }
          val resultText = sb.toString().trim()
          if (resultText.isNotEmpty()) {
            return@withContext Result.success(resultText)
          }
        }
      }

      Result.failure(Exception("لم يرجع النموذج أي نص. يرجى تجربة صياغة أخرى."))
    } catch (e: Exception) {
      Log.e(TAG, "Request failed", e)
      Result.failure(Exception(e.localizedMessage ?: "فشل الاتصال بخدمة الذكاء الاصطناعي."))
    }
  }

  /**
   * Generates or edits images using Imagen 3 API or Gemini Multimodal endpoints.
   */
  suspend fun generateImage(
    apiKey: String,
    prompt: String,
    aspectRatio: AspectRatioOption,
    resolution: ImageResolutionOption,
    editImageBase64: String? = null
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext Result.failure(
          IllegalArgumentException("يرجى إدخال مفتاح Gemini API صالح في الإعدادات لتوليد الصور.")
        )
      }

      // Build Imagen 3 payload
      val requestJson = JSONObject()
      val instances = JSONArray()
      val instanceObj = JSONObject().put("prompt", prompt)
      if (!editImageBase64.isNullOrBlank()) {
        instanceObj.put("image", JSONObject().put("bytesBase64Encoded", editImageBase64))
      }
      instances.put(instanceObj)
      requestJson.put("instances", instances)

      val parameters = JSONObject()
      parameters.put("sampleCount", 1)
      parameters.put("aspectRatio", aspectRatio.apiValue)
      parameters.put("outputMimeType", "image/jpeg")
      requestJson.put("parameters", parameters)

      val url = "$BASE_URL/models/imagen-3.0-generate-002:predict?key=$apiKey"
      val request = Request.Builder()
        .url(url)
        .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        Log.e(TAG, "Imagen API error: ${response.code} -> $responseBody")
        val errorMsg = parseErrorMessage(response.code, responseBody)
        return@withContext Result.failure(Exception(errorMsg))
      }

      val json = JSONObject(responseBody)
      val predictions = json.optJSONArray("predictions")
      if (predictions != null && predictions.length() > 0) {
        val firstPred = predictions.getJSONObject(0)
        val b64 = firstPred.optString("bytesBase64Encoded")
        if (b64.isNotEmpty()) {
          return@withContext Result.success(b64)
        }
      }

      Result.failure(Exception("لم يرجع النموذج صورة صالحة. يرجى تجربة وصف آخر."))
    } catch (e: Exception) {
      Log.e(TAG, "Image generation error", e)
      Result.failure(Exception(e.localizedMessage ?: "فشل توليد الصورة."))
    }
  }

  private fun parseErrorMessage(statusCode: Int, responseBody: String): String {
    return try {
      val json = JSONObject(responseBody)
      val errorObj = json.optJSONObject("error")
      val message = errorObj?.optString("message") ?: ""

      when {
        statusCode == 400 && message.contains("API_KEY_INVALID", ignoreCase = true) ->
          "مفتاح Gemini API غير صالح. يرجى التحقق من المفتاح في الإعدادات."
        statusCode == 403 ->
          "تم رفض الوصول (403). يرجى التحقق من تفعيل واجهة برمجة تطبيقات Gemini ومفتاح API."
        statusCode == 429 || message.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ->
          "تم تجاوز الحد المسموح به من الطلبات مؤقتاً (Rate Limit). يرجى المحاولة بعد قليل."
        statusCode == 503 || statusCode == 500 ->
          "خادم الذكاء الاصطناعي مشغول حالياً. يرجى إعادة المحاولة."
        message.isNotEmpty() ->
          "خطأ من الخادم ($statusCode): $message"
        else ->
          "حدث خطأ برمز $statusCode أثناء الاتصال."
      }
    } catch (_: Exception) {
      "خطأ أثناء الاتصال بالخادم ($statusCode)."
    }
  }
}
