package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.AspectRatioOption
import com.example.data.model.GeneratedImageItem
import com.example.data.model.ImageResolutionOption
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.ThinkingAmber
import com.example.ui.viewmodel.ImageStudioViewModel
import com.example.util.ImageUtils
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageStudioScreen(
  viewModel: ImageStudioViewModel,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val prompt by viewModel.prompt.collectAsState()
  val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()
  val selectedResolution by viewModel.selectedResolution.collectAsState()
  val editImageBase64 by viewModel.editImageBase64.collectAsState()
  val currentImage by viewModel.currentGeneratedImage.collectAsState()
  val gallery by viewModel.gallery.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val isEnhancingPrompt by viewModel.isEnhancingPrompt.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()

  var fullScreenImage by remember { mutableStateOf<GeneratedImageItem?>(null) }

  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      val b64 = ImageUtils.uriToBase64(context, uri)
      viewModel.setEditImage(b64)
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
    // Header Title Card
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
            .size(46.dp)
            .clip(CircleShape)
            .background(PrimaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = SecondaryCyan,
            modifier = Modifier.size(24.dp)
          )
        }
        Column {
          Text(
            text = "توليد الصور بنموذج Gemini (مع Coil)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "أدخل وصفك النصي لتوليد صور مذهلة وعرضها فورياً باستخدام مكتبة Coil",
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

    // Prompt Input & AI Enhancement
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (editImageBase64 != null) "تعليمات تعديل الصورة المرفقة:" else "وصف الصورة المطلوب توليدها:",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold
        )

        // Enhance Prompt with Gemini Button
        if (prompt.isNotBlank()) {
          OutlinedButton(
            onClick = { viewModel.enhancePromptWithGemini() },
            enabled = !isEnhancingPrompt && !isLoading,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("enhance_prompt_button")
          ) {
            if (isEnhancingPrompt) {
              CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = ThinkingAmber
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("جاري التحسين...", fontSize = 11.sp, color = ThinkingAmber)
            } else {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = ThinkingAmber,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("تحسين الوصف بـ Gemini", fontSize = 11.sp, color = ThinkingAmber)
            }
          }
        }
      }

      OutlinedTextField(
        value = prompt,
        onValueChange = { viewModel.setPrompt(it) },
        placeholder = {
          Text(
            if (editImageBase64 != null)
              "مثال: اجعل المشهد ليلياً وأضف كواكب متوهجة في السماء..."
            else
              "مثال: مشهد سينمائي لمدينة عربية مستقبلية عائمة بين الغيوم الذهبية وإضاءة نيون فاخرة..."
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("image_prompt_input"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = PrimaryIndigo,
          unfocusedBorderColor = SurfaceCardBorder,
          focusedContainerColor = SurfaceCardDark,
          unfocusedContainerColor = SurfaceCardDark
        ),
        minLines = 3,
        maxLines = 5
      )

      // Quick Inspiration Prompts
      val inspirationPrompts = listOf(
        "لوحة زيتية لفارس عربي في صحراء نيون مستقبلية",
        "واحة خيالية مع بحيرة مضيئة وأشجار كريستالية",
        "رائد فضاء عربي يقرأ كتاباً على سطح المريخ",
        "تصميم ثلاثي الأبعاد لشخصية ذكاء اصطناعي ودودة ولطيفة"
      )
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
      ) {
        items(inspirationPrompts) { p ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(SurfaceCardDark)
              .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
              .clickable { viewModel.setPrompt(p) }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(text = p, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
          }
        }
      }
    }

    // Aspect Ratio Affordance (1:1, 16:9, 9:16, 4:3, 3:4, 2:3, 3:2, 21:9)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "نسبة العرض إلى الارتفاع (Aspect Ratio):",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
      )

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
      ) {
        items(AspectRatioOption.entries) { ratio ->
          val isSelected = ratio == selectedAspectRatio
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.setAspectRatio(ratio) },
            label = { Text(ratio.label, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = PrimaryIndigo,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("ratio_chip_${ratio.name}")
          )
        }
      }
    }

    // Image Resolution Affordance (1K, 2K, 4K)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "دقة وحجم الصورة (Resolution):",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ImageResolutionOption.entries.forEach { res ->
          val isSelected = res == selectedResolution
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.setResolution(res) },
            label = { Text(res.label, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = PrimaryIndigo,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("res_chip_${res.name}")
          )
        }
      }
    }

    // Image Editing / Image-to-Image reference section
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "تعديل صورة موجودة (اختياري):",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
          )
          if (editImageBase64 != null) {
            IconButton(onClick = { viewModel.setEditImage(null) }, modifier = Modifier.size(32.dp)) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "إلغاء الصورة")
            }
          }
        }

        if (editImageBase64 != null) {
          val bmp = remember(editImageBase64) { ImageUtils.base64ToBitmap(editImageBase64!!) }
          bmp?.let {
            Image(
              bitmap = it.asImageBitmap(),
              contentDescription = "صورة مرجعية للتعديل",
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp)),
              contentScale = ContentScale.Crop
            )
          }
        } else {
          Button(
            onClick = {
              photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("pick_edit_image_button")
          ) {
            Icon(imageVector = Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("اختيار صورة لتعديلها بأمر نصي")
          }
        }
      }
    }

    // Generate Button
    Button(
      onClick = { viewModel.generateImage() },
      enabled = !isLoading && prompt.isNotBlank(),
      colors = ButtonDefaults.buttonColors(
        containerColor = PrimaryIndigo,
        disabledContainerColor = SurfaceCardDark
      ),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("generate_image_button")
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = MaterialTheme.colorScheme.onPrimary,
          strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("جاري توليد وعرض الصورة عبر Coil...")
      } else {
        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (editImageBase64 != null) "تعديل الصورة الآن" else "توليد وعرض الصورة الآن",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }
    }

    // Generated Image Preview Card (Rendered immediately with Coil)
    currentImage?.let { item ->
      Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("generated_image_card")
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(SecondaryCyan)
              )
              Text(
                text = "الصورة الناتجة (عبر Coil):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
            }

            IconButton(onClick = { fullScreenImage = item }) {
              Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "عرض بحجم كامل",
                tint = SecondaryCyan
              )
            }
          }

          // Coil SubcomposeAsyncImage with crossfade and loading state
          val imageModel = item.filePath ?: ImageUtils.base64ToByteArray(item.base64Data)

          SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
              .data(imageModel)
              .crossfade(true)
              .build(),
            contentDescription = item.prompt,
            loading = {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .aspectRatio(item.aspectRatio.ratioFloat)
                  .background(SurfaceCardDark),
                contentAlignment = Alignment.Center
              ) {
                CircularProgressIndicator(
                  color = SecondaryCyan,
                  strokeWidth = 2.dp
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(item.aspectRatio.ratioFloat)
              .clip(RoundedCornerShape(12.dp))
              .clickable { fullScreenImage = item },
            contentScale = ContentScale.Fit
          )

          Text(
            text = item.prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Copy prompt button
            OutlinedButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Image Prompt", item.prompt))
                Toast.makeText(context, "تم نسخ الوصف إلى الحافظة", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("نسخ الوصف", fontSize = 12.sp)
            }

            // Share & Save button
            Button(
              onClick = {
                shareImage(context, item)
              },
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("مشاركة وحفظ")
            }
          }
        }
      }
    }

    // Gallery History (Loaded immediately with Coil AsyncImage)
    if (gallery.isNotEmpty()) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(imageVector = Icons.Default.Collections, contentDescription = null, tint = SecondaryCyan)
          Text(
            text = "معرض الصور المولدة (${gallery.size}):",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          items(gallery, key = { it.id }) { item ->
            val modelData = item.filePath ?: ImageUtils.base64ToByteArray(item.base64Data)
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
              border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder)),
              modifier = Modifier
                .width(140.dp)
                .clickable { fullScreenImage = item }
            ) {
              Column {
                AsyncImage(
                  model = ImageRequest.Builder(context)
                    .data(modelData)
                    .crossfade(true)
                    .build(),
                  contentDescription = item.prompt,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                  contentScale = ContentScale.Crop
                )
                Text(
                  text = item.prompt,
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 1,
                  modifier = Modifier.padding(6.dp),
                  fontSize = 10.sp
                )
              }
            }
          }
        }
      }
    }
  }

  // Full Screen Preview Dialog
  fullScreenImage?.let { item ->
    Dialog(onDismissRequest = { fullScreenImage = null }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCardDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "عرض الصورة بالكامل",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { fullScreenImage = null }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
            }
          }

          val modelData = item.filePath ?: ImageUtils.base64ToByteArray(item.base64Data)
          SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
              .data(modelData)
              .crossfade(true)
              .build(),
            contentDescription = item.prompt,
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(item.aspectRatio.ratioFloat)
              .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
          )

          Text(
            text = item.prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )

          Button(
            onClick = { shareImage(context, item) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("مشاركة أو حفظ في الجهاز")
          }
        }
      }
    }
  }
}

private fun shareImage(context: Context, item: GeneratedImageItem) {
  try {
    val file = if (item.filePath != null) {
      File(item.filePath)
    } else {
      val bmp = ImageUtils.base64ToBitmap(item.base64Data) ?: return
      val cachePath = File(context.cacheDir, "images")
      cachePath.mkdirs()
      val newFile = File(cachePath, "generated_${System.currentTimeMillis()}.jpg")
      val fos = FileOutputStream(newFile)
      bmp.compress(Bitmap.CompressFormat.JPEG, 95, fos)
      fos.close()
      newFile
    }

    val contentUri: Uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "image/jpeg"
      putExtra(Intent.EXTRA_STREAM, contentUri)
      putExtra(Intent.EXTRA_TEXT, "تم التوليد بنموذج Gemini عبر استوديو الذكاء الاصطناعي: ${item.prompt}")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة الصورة"))
  } catch (e: Exception) {
    Toast.makeText(context, "تم حفظ الصورة في الذاكرة", Toast.LENGTH_SHORT).show()
  }
}
