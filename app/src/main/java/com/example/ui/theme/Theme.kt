package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SpecialistDarkColorScheme = darkColorScheme(
  primary = PrimaryBlue,
  onPrimary = Slate100,
  primaryContainer = BlueTintBg,
  onPrimaryContainer = PrimaryBlueLight,
  secondary = PrimaryBlueLight,
  onSecondary = SpecialistBg,
  secondaryContainer = SpecialistSurfaceVariant,
  onSecondaryContainer = Slate100,
  tertiary = StatusAmber,
  onTertiary = SpecialistBg,
  tertiaryContainer = StatusAmberDim,
  onTertiaryContainer = StatusAmberLight,
  background = SpecialistBg,
  onBackground = Slate100,
  surface = SpecialistHeaderBg,
  onSurface = Slate100,
  surfaceVariant = SpecialistCardBg,
  onSurfaceVariant = Slate400,
  outline = BorderMedium,
  outlineVariant = BorderProminent,
  error = StatusRed,
  onError = Slate100,
  errorContainer = StatusRedDim,
  onErrorContainer = StatusRedLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false, // Preserve dedicated specialist dark aesthetic
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = SpecialistDarkColorScheme,
    typography = Typography,
    content = content
  )
}

