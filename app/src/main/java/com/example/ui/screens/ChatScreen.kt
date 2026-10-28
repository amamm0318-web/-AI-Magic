package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModelOption
import com.example.data.model.AiRole
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.ThinkingAmber
import com.example.ui.theme.ThinkingAmberContainer
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
  viewModel: ChatViewModel,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val messages by viewModel.messages.collectAsState()
  val selectedModel by viewModel.selectedModel.collectAsState()
  val selectedRole by viewModel.selectedRole.collectAsState()
  val isHighThinkingEnabled by viewModel.isHighThinkingEnabled.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val attachedImageBase64 by viewModel.attachedImageBase64.collectAsState()
  val isTtsPlaying by viewModel.ttsManager.isPlaying.collectAsState()
  val currentUtteranceId by viewModel.ttsManager.currentUtteranceId.collectAsState()
  val isAutoSpeakActive by viewModel.isAutoSpeakActive.collectAsState()

  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  // Photo picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      val b64 = ImageUtils.uriToBase64(context, uri)
      viewModel.attachImage(b64)
    }
  }

  // Scroll to bottom when new messages arrive
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Model Selector & High Thinking Toggle Bar
    Surface(
      color = SurfaceCardDark,
      shadowElevation = 4.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
        // Row 1: Model Choice & Clear History
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            items(AiModelOption.entries) { model ->
              val isSelected = model == selectedModel
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.selectModel(model) },
                label = {
                  Text(
                    text = model.displayName,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                leadingIcon = {
                  if (model.supportsThinking) {
                    Icon(
                      imageVector = Icons.Default.Psychology,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                      tint = if (isSelected) ThinkingAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  } else {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PrimaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("model_chip_${model.id}")
              )
            }
          }

          IconButton(
            onClick = { viewModel.clearChat() },
            modifier = Modifier.testTag("clear_chat_button")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = "مسح المحادثة",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 2: Roles & Thinking Mode Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // High Thinking Chip
          FilterChip(
            selected = isHighThinkingEnabled,
            onClick = { viewModel.toggleHighThinking() },
            label = {
              Text(
                text = "تفكير عالي",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = if (isHighThinkingEnabled) ThinkingAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = ThinkingAmberContainer,
              selectedLabelColor = ThinkingAmber
            ),
            modifier = Modifier.testTag("high_thinking_chip")
          )

          // Auto-Speak Toggle Chip
          FilterChip(
            selected = isAutoSpeakActive,
            onClick = { viewModel.toggleAutoSpeak() },
            label = { Text("قراءة تلقائية (TTS)", fontSize = 11.sp) },
            leadingIcon = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isAutoSpeakActive) SecondaryCyan else MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SecondaryCyan.copy(alpha = 0.2f),
              selectedLabelColor = SecondaryCyan
            ),
            modifier = Modifier.testTag("auto_speak_chip")
          )

          // Roles horizontal row
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
          ) {
            items(AiRole.presets) { role ->
              val isSelected = role.id == selectedRole.id
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.selectRole(role) },
                label = { Text("${role.iconEmoji} ${role.titleAr}", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("role_chip_${role.id}")
              )
            }
          }
        }
      }
    }

    // Error alert banner if any
    errorMessage?.let { error ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.errorContainer)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = error,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
          )
          IconButton(onClick = { viewModel.clearError() }) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "إغلاق الخطأ",
              tint = MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }
      }
    }

    // Chat Message Thread
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      contentPadding = PaddingValues(vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        MessageBubble(
          message = msg,
          isPlayingThis = isTtsPlaying && currentUtteranceId == msg.id,
          onSpeak = { viewModel.speakText(msg.text, msg.id) },
          onStopSpeak = { viewModel.stopSpeaking() },
          onCopy = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("AI Studio", msg.text))
            Toast.makeText(context, "تم نسخ النص إلى الحافظة", Toast.LENGTH_SHORT).show()
          }
        )
      }

      if (isLoading) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCardDark)
                .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = if (isHighThinkingEnabled) ThinkingAmber else PrimaryIndigo
              )
              Text(
                text = if (isHighThinkingEnabled) "جاري التفكير العميق وتحليل الاستجابة..." else "جاري كتابة الرد...",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isHighThinkingEnabled) ThinkingAmber else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    // Quick suggestions if only initial message is present
    if (messages.size <= 1) {
      val suggestions = listOf(
        "💡 اشرح لي الذكاء الاصطناعي ببساطة",
        "💻 اكتب كود تطبيق أندرويد متكامل",
        "✍️ صيغ خطة تسويقية لمشروع ناشئ",
        "🧠 حلل مسألة علمية معقدة بالخطوات"
      )
      LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(suggestions) { prompt ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(SurfaceCardDark)
              .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
              .clickable { viewModel.sendMessage(prompt) }
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Text(text = prompt, style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }

    // Attached Image Preview above input
    attachedImageBase64?.let { b64 ->
      val bitmap: Bitmap? = remember(b64) { ImageUtils.base64ToBitmap(b64) }
      bitmap?.let { bmp ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(SurfaceCardDark)
              .padding(8.dp)
          ) {
            Image(
              bitmap = bmp.asImageBitmap(),
              contentDescription = "صورة مرفقة",
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
              contentScale = ContentScale.Crop
            )
            Text(
              text = "صورة مرفقة للاستفسار والمحادثة",
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.attachImage(null) }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "إلغاء المرفق")
            }
          }
        }
      }
    }

    // Bottom Input Bar
    Surface(
      color = SurfaceCardDark,
      shadowElevation = 8.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Image attachment button
        IconButton(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          modifier = Modifier.testTag("attach_image_button")
        ) {
          Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "إرفاق صورة للمحادثة",
            tint = SecondaryCyan
          )
        }

        // Text input field
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = { Text("اكتب رسالتك لـ Gemini...") },
          modifier = Modifier
            .weight(1f)
            .testTag("chat_input_field"),
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryIndigo,
            unfocusedBorderColor = SurfaceCardBorder,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background
          ),
          maxLines = 4
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Send Button
        IconButton(
          onClick = {
            if (inputText.isNotBlank() || attachedImageBase64 != null) {
              viewModel.sendMessage(inputText)
              inputText = ""
            }
          },
          enabled = !isLoading && (inputText.isNotBlank() || attachedImageBase64 != null),
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
              if (!isLoading && (inputText.isNotBlank() || attachedImageBase64 != null))
                PrimaryIndigo
              else
                MaterialTheme.colorScheme.surfaceVariant
            )
            .testTag("send_message_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "إرسال الرسالة",
            tint = if (!isLoading && (inputText.isNotBlank() || attachedImageBase64 != null))
              MaterialTheme.colorScheme.onPrimary
            else
              MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun MessageBubble(
  message: ChatMessage,
  isPlayingThis: Boolean,
  onSpeak: () -> Unit,
  onStopSpeak: () -> Unit,
  onCopy: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isUser = message.sender == MessageSender.USER

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    // Model / Thinking Badge if AI
    if (!isUser && message.modelName != null) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(bottom = 2.dp, start = 8.dp)
      ) {
        if (message.isThinking) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = ThinkingAmber,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "تفكير عالي • ${message.modelName}",
            style = MaterialTheme.typography.labelSmall,
            color = ThinkingAmber,
            fontWeight = FontWeight.Bold
          )
        } else {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = SecondaryCyan,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = message.modelName,
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryCyan
          )
        }
      }
    }

    // Message Content Card
    Box(
      modifier = Modifier
        .clip(
          RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
          )
        )
        .background(if (isUser) PrimaryIndigo else SurfaceCardDark)
        .border(
          width = 1.dp,
          color = if (isUser) PrimaryContainer else SurfaceCardBorder,
          shape = RoundedCornerShape(16.dp)
        )
        .padding(14.dp)
    ) {
      Column {
        // If image attached in user message
        if (!message.imageBase64.isNullOrBlank()) {
          val bmp = remember(message.imageBase64) { ImageUtils.base64ToBitmap(message.imageBase64) }
          bmp?.let {
            Image(
              bitmap = it.asImageBitmap(),
              contentDescription = "صورة مرفقة",
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp)),
              contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
          }
        }

        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyLarge,
          color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
          lineHeight = 22.sp
        )

        // Action Buttons for AI message (TTS, Copy)
        if (!isUser) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // TTS Read-aloud button with clear label
            androidx.compose.material3.OutlinedButton(
              onClick = {
                if (isPlayingThis) onStopSpeak() else onSpeak()
              },
              colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                containerColor = if (isPlayingThis) SecondaryCyan.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent
              ),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isPlayingThis) SecondaryCyan else SurfaceCardBorder
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Icon(
                imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = if (isPlayingThis) SecondaryCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isPlayingThis) "إيقاف الصوت" else "قراءة صوتية (TTS)",
                fontSize = 11.sp,
                color = if (isPlayingThis) SecondaryCyan else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // Copy button
            IconButton(
              onClick = onCopy,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "نسخ النص",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}
