package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.tts.TextToSpeechManager
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardDark

@Composable
fun TtsAudioPlayerBar(
  ttsManager: TextToSpeechManager,
  modifier: Modifier = Modifier
) {
  val isPlaying by ttsManager.isPlaying.collectAsState()
  val speakingText by ttsManager.currentlySpeakingText.collectAsState()

  AnimatedVisibility(
    visible = isPlaying,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically(),
    modifier = modifier
  ) {
    Surface(
      color = SurfaceCardDark,
      shadowElevation = 8.dp,
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryCyan.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .testTag("tts_audio_player_bar")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(PrimaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.VolumeUp,
              contentDescription = null,
              tint = SecondaryCyan,
              modifier = Modifier.size(20.dp)
            )
          }

          Column {
            Text(
              text = "جاري قراءة رد Gemini بصوت مسموع...",
              style = MaterialTheme.typography.labelMedium,
              color = SecondaryCyan,
              fontWeight = FontWeight.Bold
            )
            speakingText?.let { txt ->
              Text(
                text = txt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                fontSize = 11.sp
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Speed toggle chip
          val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
          val currentSpeed = ttsManager.speechRate
          FilterChip(
            selected = false,
            onClick = {
              val nextIdx = (speeds.indexOfFirst { it == currentSpeed } + 1) % speeds.size
              ttsManager.speechRate = speeds[if (nextIdx >= 0) nextIdx else 1]
            },
            label = { Text("${currentSpeed}x", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier.testTag("tts_speed_quick_chip")
          )

          // Stop button
          IconButton(
            onClick = { ttsManager.stop() },
            modifier = Modifier
              .size(36.dp)
              .testTag("tts_stop_button")
          ) {
            Icon(
              imageVector = Icons.Default.Stop,
              contentDescription = "إيقاف القراءة",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }
    }
  }
}
