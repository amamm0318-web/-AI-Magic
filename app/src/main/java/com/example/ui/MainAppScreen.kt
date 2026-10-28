package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ApiKeyBanner
import com.example.ui.components.TtsAudioPlayerBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ImageStudioScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VisionScreen
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.ThinkingAmber
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ImageStudioViewModel
import com.example.ui.viewmodel.VisionViewModel

sealed class AppTab(
  val index: Int,
  val titleAr: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  data object Chat : AppTab(0, "محادثة", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline)
  data object ImageStudio : AppTab(1, "توليد الصور", Icons.Filled.Image, Icons.Outlined.Image)
  data object Vision : AppTab(2, "تحليل الرؤية", Icons.Filled.Visibility, Icons.Outlined.Visibility)
  data object Settings : AppTab(3, "الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings)

  companion object {
    val items = listOf(Chat, ImageStudio, Vision, Settings)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
  chatViewModel: ChatViewModel = viewModel(),
  imageStudioViewModel: ImageStudioViewModel = viewModel(),
  visionViewModel: VisionViewModel = viewModel()
) {
  var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
  val currentTab = AppTab.items.getOrElse(selectedTabIndex) { AppTab.Chat }

  val hasValidApiKey = chatViewModel.prefs.hasValidApiKey
  val isHighThinkingActive by chatViewModel.isHighThinkingEnabled.collectAsState()
  val selectedModel by chatViewModel.selectedModel.collectAsState()

  // Enforce Arabic RTL layout for cohesive presentation
  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing),
      topBar = {
        Surface(
          color = SurfaceCardDark,
          shadowElevation = 3.dp
        ) {
          TopAppBar(
            title = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = SecondaryCyan,
                    modifier = Modifier.size(20.dp)
                  )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                  Text(
                    text = "استوديو الذكاء الاصطناعي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 1.dp)
                  ) {
                    if (isHighThinkingActive) {
                      Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = ThinkingAmber,
                        modifier = Modifier.size(12.dp)
                      )
                      Spacer(modifier = Modifier.width(3.dp))
                      Text(
                        text = "وضع التفكير العالي (High Thinking)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThinkingAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                      )
                    } else {
                      Text(
                        text = selectedModel.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryCyan,
                        fontSize = 10.sp
                      )
                    }
                  }
                }
              }
            },
            actions = {
              IconButton(
                onClick = { selectedTabIndex = AppTab.Settings.index },
                modifier = Modifier.testTag("top_settings_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Settings,
                  contentDescription = "الإعدادات",
                  tint = if (selectedTabIndex == AppTab.Settings.index) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = SurfaceCardDark,
              titleContentColor = MaterialTheme.colorScheme.onSurface
            )
          )
        }
      },
      bottomBar = {
        NavigationBar(
          containerColor = SurfaceCardDark,
          modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar")
        ) {
          AppTab.items.forEach { tab ->
            val isSelected = tab.index == selectedTabIndex
            NavigationBarItem(
              selected = isSelected,
              onClick = { selectedTabIndex = tab.index },
              icon = {
                Icon(
                  imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = tab.titleAr
                )
              },
              label = {
                Text(
                  text = tab.titleAr,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                indicatorColor = PrimaryIndigo,
                selectedTextColor = PrimaryIndigo,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.testTag("nav_item_${tab.index}")
            )
          }
        }
      }
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        // Api Key Warning Banner if key is missing
        ApiKeyBanner(
          hasValidKey = hasValidApiKey,
          onOpenSettings = { selectedTabIndex = AppTab.Settings.index }
        )

        // Floating Active TTS Audio Player Bar
        TtsAudioPlayerBar(ttsManager = chatViewModel.ttsManager)

        // Screen Content with smooth fade animation
        AnimatedContent(
          targetState = currentTab,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          modifier = Modifier.weight(1f),
          label = "screen_transition"
        ) { tab ->
          when (tab) {
            is AppTab.Chat -> ChatScreen(
              viewModel = chatViewModel,
              onOpenSettings = { selectedTabIndex = AppTab.Settings.index }
            )
            is AppTab.ImageStudio -> ImageStudioScreen(
              viewModel = imageStudioViewModel,
              onOpenSettings = { selectedTabIndex = AppTab.Settings.index }
            )
            is AppTab.Vision -> VisionScreen(
              viewModel = visionViewModel,
              onOpenSettings = { selectedTabIndex = AppTab.Settings.index }
            )
            is AppTab.Settings -> SettingsScreen(
              prefs = chatViewModel.prefs,
              ttsManager = chatViewModel.ttsManager
            )
          }
        }
      }
    }
  }
}
