package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.data.model.VisionAnalysisMode
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.ThinkingAmber
import com.example.ui.theme.ThinkingAmberContainer
import com.example.ui.viewmodel.VisionViewModel
import com.example.util.ImageUtils

@Composable
fun VisionScreen(
  viewModel: VisionViewModel,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val selectedImageBase64 by viewModel.selectedImageBase64.collectAsState()
  val selectedMode by viewModel.selectedMode.collectAsState()
  val customPrompt by viewModel.customPrompt.collectAsState()
  val useHighThinking by viewModel.useHighThinking.collectAsState()
  val analysisResult by viewModel.analysisResult.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val isTtsPlaying by viewModel.ttsManager.isPlaying.collectAsState()

  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      val b64 = ImageUtils.uriToBase64(context, uri)
      viewModel.setImage(b64)
    }
  }

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
            imageVector = Icons.Default.Visibility,
            contentDescription = null,
            tint = SecondaryCyan
          )
        }
        Column {
          Text(
            text = "تحليل الصور والرؤية البصرية (Vision)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "افحص المستندات، استخرج النصوص (OCR)، وتعرف على محتويات المشاهد بأحدث نماذج Gemini",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Error Alert
    errorMessage?.let { error ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.errorContainer)
          .padding(12.dp)
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
              contentDescription = "إغلاق",
              tint = MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }
      }
    }

    // Image Upload / Preview Area
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .clickable {
          photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
          )
        }
        .testTag("pick_vision_image_card")
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        if (selectedImageBase64 != null) {
          val bmp = remember(selectedImageBase64) { ImageUtils.base64ToBitmap(selectedImageBase64!!) }
          bmp?.let {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "الصورة المختارة",
                modifier = Modifier
                  .fillMaxWidth()
                  .height(220.dp)
                  .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "اضغط لتغيير الصورة",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryCyan
              )
            }
          }
        } else {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 24.dp)
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PrimaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = null,
                tint = SecondaryCyan,
                modifier = Modifier.size(28.dp)
              )
            }
            Text(
              text = "اضغط لاختيار صورة للتحليل أو استخراج النصوص",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "يدعم صور المستندات، الطبيعة، الأشياء، والأكواد",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Mode Selector
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "نوع التحليل المطلوب:",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
      )

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        VisionAnalysisMode.entries.forEach { mode ->
          val isSelected = mode == selectedMode
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) PrimaryContainer else SurfaceCardDark)
              .border(1.dp, if (isSelected) PrimaryIndigo else SurfaceCardBorder, RoundedCornerShape(12.dp))
              .clickable { viewModel.setMode(mode) }
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = mode.titleAr,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // Custom Question Input if Mode is CUSTOM
    AnimatedVisibility(visible = selectedMode == VisionAnalysisMode.CUSTOM) {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "سؤالك المخصص حول الصورة:",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
          value = customPrompt,
          onValueChange = { viewModel.setCustomPrompt(it) },
          placeholder = { Text("مثال: كم عدد الأشخاص في الصورة وماذا يفعلون؟") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("vision_custom_prompt_input"),
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryIndigo,
            unfocusedBorderColor = SurfaceCardBorder,
            focusedContainerColor = SurfaceCardDark,
            unfocusedContainerColor = SurfaceCardDark
          )
        )
      }
    }

    // High Thinking Toggle
    Card(
      colors = CardDefaults.cardColors(
        containerColor = if (useHighThinking) ThinkingAmberContainer.copy(alpha = 0.5f) else SurfaceCardDark
      ),
      border = CardDefaults.outlinedCardBorder().copy(
        brush = androidx.compose.ui.graphics.SolidColor(
          if (useHighThinking) ThinkingAmber else SurfaceCardBorder
        )
      ),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { viewModel.toggleHighThinking() }
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = null,
          tint = if (useHighThinking) ThinkingAmber else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "تفعيل التفكير العالي (High Thinking)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (useHighThinking) ThinkingAmber else MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "استخدام نموذج Gemini 4 Argon مع أقصى مستويات التحليل المنطقي",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        FilterChip(
          selected = useHighThinking,
          onClick = { viewModel.toggleHighThinking() },
          label = { Text(if (useHighThinking) "مفعل" else "معطل", fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ThinkingAmber,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
          )
        )
      }
    }

    // Analyze Action Button
    Button(
      onClick = { viewModel.analyzeImage() },
      enabled = !isLoading && selectedImageBase64 != null,
      colors = ButtonDefaults.buttonColors(
        containerColor = PrimaryIndigo,
        disabledContainerColor = SurfaceCardDark
      ),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("analyze_image_button")
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = MaterialTheme.colorScheme.onPrimary,
          strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(if (useHighThinking) "جاري التفكير البصري المعمق..." else "جاري تحليل الصورة...")
      } else {
        Icon(imageVector = Icons.Default.DocumentScanner, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("بدء التحليل البصري", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    }

    // Analysis Result Display Card
    analysisResult?.let { resultText ->
      Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = SecondaryCyan,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "نتائج التحليل:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              // TTS Audio read-aloud button
              IconButton(
                onClick = {
                  if (isTtsPlaying) viewModel.stopSpeaking() else viewModel.speakResult()
                }
              ) {
                Icon(
                  imageVector = if (isTtsPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                  contentDescription = "استماع صوتي",
                  tint = if (isTtsPlaying) SecondaryCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              // Copy button
              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  clipboard.setPrimaryClip(ClipData.newPlainText("Vision Result", resultText))
                  Toast.makeText(context, "تم نسخ التحليل إلى الحافظة", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "نسخ التحليل",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Text(
            text = resultText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
          )
        }
      }
    }
  }
}
