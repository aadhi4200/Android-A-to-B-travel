package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Hardware / Specialist Tool Design System Palette
val SpecialistBg = Color(0xFF0A0B10)             // Main screen deep background
val SpecialistHeaderBg = Color(0xFF0D0F16)       // Header and Bottom Nav bar
val SpecialistCanvasBg = Color(0xFF11131A)       // Canvas / Viewport background
val SpecialistCardBg = Color(0xFF161922)         // Elevated card and floating panel background
val SpecialistSurfaceVariant = Color(0xFF1E2230) // Active item / secondary container

// High-tech borders
val BorderSubtle = Color(0x0DFFFFFF)             // 5% white border
val BorderMedium = Color(0x1AFFFFFF)             // 10% white border
val BorderProminent = Color(0x33FFFFFF)          // 20% white border
val BorderBlue = Color(0x333B82F6)               // 20% blue border
val BorderAmber = Color(0x4DF59E0B)              // 30% amber border
val BorderRed = Color(0x4DEF4444)                // 30% red border
val BorderGreen = Color(0x3322C55E)              // 20% green border

// Specialist Primary & Accents (Precision Tool Blue)
val PrimaryBlue = Color(0xFF3B82F6)              // Blue 500
val PrimaryBlueHover = Color(0xFF2563EB)         // Blue 600
val PrimaryBlueLight = Color(0xFF60A5FA)         // Blue 400
val BlueGlow = Color(0x663B82F6)                 // 40% Blue glow
val BlueTintBg = Color(0x1A3B82F6)               // 10% Blue background
val BlueTintBorder = Color(0x333B82F6)           // 20% Blue border

// Status Indicators
val StatusGreen = Color(0xFF22C55E)              // Connected / Online
val StatusGreenDim = Color(0x1A22C55E)           // 10% green background
val StatusGreenLight = Color(0xFF4ADE80)         // Green 400 text
val StatusAmber = Color(0xFFF59E0B)              // Obstacle / Warning
val StatusAmberDim = Color(0x1AF59E0B)           // 10% amber background
val StatusAmberLight = Color(0xFFFCD34D)         // Amber 300 text
val StatusRed = Color(0xFFEF4444)                // Abort / Error
val StatusRedDim = Color(0x1AEF4444)             // 10% red background
val StatusRedLight = Color(0xFFF87171)           // Red 400 text

// Typography Neutrals
val Slate100 = Color(0xFFF1F5F9)                 // Primary high-contrast text
val Slate200 = Color(0xFFE2E8F0)                 // High readable secondary text
val Slate300 = Color(0xFFCBD5E1)                 // Secondary readable text
val Slate400 = Color(0xFF94A3B8)                 // Labels and subtitles
val Slate500 = Color(0xFF64748B)                 // Micro-metrics and muted labels
val Slate600 = Color(0xFF475569)                 // Inactive icons / dividers
val Slate700 = Color(0xFF334155)                 // Grid dots / subtle button background
val Slate800 = Color(0xFF1E293B)                 // Dark buttons / surface

// Backward Compatibility Aliases for components
val StatusBlueDim = Color(0x1A3B82F6)
val AerospaceBg = SpecialistBg
val AerospaceSurface = SpecialistHeaderBg
val AerospaceSurfaceVariant = SpecialistSurfaceVariant
val AerospaceCardBg = SpecialistCardBg
val AerospaceBorder = BorderMedium
val AerospaceBorderBright = BorderProminent
val CyanNeon = PrimaryBlueLight
val CyanDim = PrimaryBlue
val CyanGlow = BlueGlow
val BlueAccent = PrimaryBlue
val StatusSuccess = StatusGreen
val StatusSuccessDim = StatusGreenDim
val StatusWarning = StatusAmber
val StatusWarningDim = StatusAmberDim
val StatusError = StatusRed
val StatusErrorDim = StatusRedDim
val StatusInfo = PrimaryBlueLight
val TextPrimary = Slate100
val TextSecondary = Slate400
val TextMuted = Slate500
val TextCyan = PrimaryBlueLight
val TextAmber = StatusAmberLight
val GridLine = Color(0x143B82F6)
val GridCrosshair = Color(0x283B82F6)
val RadarGreen = StatusGreen

