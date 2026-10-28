package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ThinkingAmber
import com.example.ui.theme.ThinkingAmberContainer

@Composable
fun ApiKeyBanner(
  hasValidKey: Boolean,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(visible = !hasValidKey, modifier = modifier) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(ThinkingAmberContainer.copy(alpha = 0.8f))
        .clickable { onOpenSettings() }
        .padding(12.dp)
        .testTag("api_key_banner")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "تحذير المفتاح",
            tint = ThinkingAmber
          )
          Text(
            text = "مفتاح Gemini API غير معين. اضغط هنا لإدخاله للبدء بالاستخدام.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
          )
        }
        Icon(
          imageVector = Icons.Default.Key,
          contentDescription = "إعداد المفتاح",
          tint = ThinkingAmber,
          modifier = Modifier.padding(start = 8.dp)
        )
      }
    }
  }
}
