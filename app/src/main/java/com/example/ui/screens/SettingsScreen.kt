package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.prefs.AppPreferences
import com.example.data.tts.TextToSpeechManager
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.ThinkingAmber

@Composable
fun SettingsScreen(
  prefs: AppPreferences,
  ttsManager: TextToSpeechManager,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var inputKey by remember { mutableStateOf(prefs.customApiKey) }
  var isKeyVisible by remember { mutableStateOf(false) }

  var speed by remember { mutableFloatStateOf(prefs.speechRate) }
  var pitch by remember { mutableFloatStateOf(prefs.speechPitch) }
  var isAutoSpeak by remember { mutableStateOf(prefs.isAutoSpeakEnabled) }
  var customTtsText by remember { mutableStateOf("") }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(scrollState)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(PrimaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = SecondaryCyan
          )
        }
        Column {
          Text(
            text = "إعدادات التطبيق والمحركات",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "إدارة مفاتيح Gemini API وضبط سرعة ونبرة النطق الصوتي",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // API Key Section
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = PrimaryIndigo)
          Text(
            text = "مفتاح Gemini API",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        // Status pill
        val isKeyConfigured = prefs.hasValidApiKey
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
              if (isKeyConfigured) SuccessGreen.copy(alpha = 0.15f) else ThinkingAmber.copy(alpha = 0.15f)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = if (isKeyConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isKeyConfigured) SuccessGreen else ThinkingAmber,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = if (isKeyConfigured) "المفتاح جاهز ونشط" else "المفتاح غير معين حالياً",
            style = MaterialTheme.typography.labelMedium,
            color = if (isKeyConfigured) SuccessGreen else ThinkingAmber,
            fontWeight = FontWeight.Bold
          )
        }

        Text(
          text = "يتم قراءة المفتاح تلقائياً من لوحة أسرار AI Studio (BuildConfig)، أو يمكنك لصق مفتاحك المخصص هنا:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = inputKey,
          onValueChange = { inputKey = it },
          placeholder = { Text("AIzaSy...") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("api_key_input_field"),
          shape = RoundedCornerShape(12.dp),
          visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
              Icon(
                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "إظهار/إخفاء المفتاح"
              )
            }
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryIndigo,
            unfocusedBorderColor = SurfaceCardBorder,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background
          ),
          singleLine = true
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Button(
            onClick = {
              prefs.customApiKey = inputKey
              Toast.makeText(context, "تم حفظ المفتاح بنجاح!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("save_api_key_button")
          ) {
            Text("حفظ المفتاح")
          }
        }
      }
    }

    // TTS Voice Configuration
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.RecordVoiceOver,
            contentDescription = null,
            tint = SecondaryCyan
          )
          Text(
            text = "النطق الصوتي (Text to Speech)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        // Speed Slider
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "سرعة الصوت:", style = MaterialTheme.typography.bodyMedium)
            Text(text = "%.1fx".format(speed), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
          }
          Slider(
            value = speed,
            onValueChange = {
              speed = it
              prefs.speechRate = it
              ttsManager.speechRate = it
            },
            valueRange = 0.5f..2.0f,
            steps = 6,
            colors = SliderDefaults.colors(
              thumbColor = SecondaryCyan,
              activeTrackColor = SecondaryCyan
            ),
            modifier = Modifier.testTag("tts_speed_slider")
          )
        }

        // Pitch Slider
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "نبرة الصوت (Pitch):", style = MaterialTheme.typography.bodyMedium)
            Text(text = "%.1fx".format(pitch), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
          }
          Slider(
            value = pitch,
            onValueChange = {
              pitch = it
              prefs.speechPitch = it
              ttsManager.pitch = it
            },
            valueRange = 0.5f..2.0f,
            steps = 6,
            colors = SliderDefaults.colors(
              thumbColor = PrimaryIndigo,
              activeTrackColor = PrimaryIndigo
            ),
            modifier = Modifier.testTag("tts_pitch_slider")
          )
        }

        // Auto-Speak Switch Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "قراءة الردود تلقائياً (Auto-Speak)",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "يبدأ نطق ردود Gemini فور اكتمال توليدها بالصوت المسموع",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Switch(
            checked = isAutoSpeak,
            onCheckedChange = {
              isAutoSpeak = it
              prefs.isAutoSpeakEnabled = it
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = SecondaryCyan,
              checkedTrackColor = PrimaryContainer
            ),
            modifier = Modifier.testTag("auto_speak_switch")
          )
        }

        // Custom TTS Test Text Field
        OutlinedTextField(
          value = customTtsText,
          onValueChange = { customTtsText = it },
          placeholder = { Text("اكتب أو الصق أي نص أو رد هنا للاستماع إليه فوراً...") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_tts_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SecondaryCyan,
            unfocusedBorderColor = SurfaceCardBorder,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background
          ),
          minLines = 2,
          maxLines = 4
        )

        // Test Voice Button
        Button(
          onClick = {
            val textToSpeak = if (customTtsText.isNotBlank()) {
              customTtsText.trim()
            } else {
              "مرحباً بك! هذه ميزة تحويل النصوص الناتجة من نموذج Gemini إلى صوت مسموع باستخدام مكتبة TextToSpeech المدمجة في أندرويد."
            }
            ttsManager.speak(textToSpeak)
          },
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("test_tts_button")
        ) {
          Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (customTtsText.isNotBlank()) "قراءة النص المكتوب بصوت مسموع" else "تجربة النطق الصوتي الآن")
        }
      }
    }

    // Models info Card
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SecondaryCyan)
          Text(
            text = "نماذج الذكاء الاصطناعي المتوفرة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        Text(
          text = "• Gemini 3.8 Flash: متوازن، سريع وذكي ومثالي للاستخدامات اليومية والمحادثات المتقدمة.",
          style = MaterialTheme.typography.bodySmall
        )
        Text(
          text = "• Gemini 4 Argon: المحرك الأقوى مع دعم وضع التفكير العالي (High Thinking) وحل أعقد المسائل.",
          style = MaterialTheme.typography.bodySmall
        )
        Text(
          text = "• Gemini 3.1 Flash Lite: استجابات فائقة السرعة وفورية.",
          style = MaterialTheme.typography.bodySmall
        )
        Text(
          text = "• Imagen 3: توليد صور احترافية بدقة 1K و 2K و 4K ونسب أبعاد متعددة.",
          style = MaterialTheme.typography.bodySmall
        )
      }
    }
  }
}
