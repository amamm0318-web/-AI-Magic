package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = PrimaryIndigo,
  onPrimary = Color.White,
  primaryContainer = PrimaryContainer,
  onPrimaryContainer = Color.White,
  secondary = SecondaryCyan,
  onSecondary = Color.Black,
  secondaryContainer = SecondaryContainer,
  onSecondaryContainer = Color.White,
  tertiary = TertiaryPink,
  onTertiary = Color.White,
  background = BackgroundDark,
  onBackground = TextPrimary,
  surface = SurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceCardDark,
  onSurfaceVariant = TextSecondary,
  outline = SurfaceCardBorder,
  error = ErrorRed,
  onError = Color.White
)

private val LightColorScheme = DarkColorScheme // Modern dark-first experience for AI Studio

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = AppTypography,
    content = content
  )
}
