package com.muse.app.ui.theme

import androidx.compose.ui.graphics.Color

// Signature Accents (Acoustic Brass on Pitch Black)
val IlvaneBrass = Color(0xFFF59E0B)       // Primary Accent (Acoustic Brass 3.5mm Gold)
val IlvaneBrassDark = Color(0xFFD97706)   // Darker brass for light mode contrast
val IlvaneBrassGlow = Color(0x40F59E0B)   // 25% opacity for ambient glow

// Backward compatibility alias during ILVANE rebranding
val MuseAmber = IlvaneBrass
val MuseAmberDark = IlvaneBrassDark
val MuseAmberGlow = IlvaneBrassGlow

// Dark Surfaces (AMOLED Pitch Black & High Contrast Studio)
val MuseDarkBackground = Color(0xFF0B0B0E)   // OLED Pitch Black
val MuseDarkSurfaceLow = Color(0xFF141419)   // Card & item container
val MuseDarkSurfaceMid = Color(0xFF1E1E26)   // Elevated cards
val MuseDarkSurfaceHigh = Color(0xFF262630)  // Mini player background
val MuseDarkTextPrimary = Color(0xFFFFFFFF)  // Titles & headings
val MuseDarkTextSecondary = Color(0xFFA1A1AA)// Artists & sub-headers
val MuseDarkTextMuted = Color(0xFF71717A)    // Timestamps & borders

// True Light Surfaces (Crisp, Clean, Premium Off-White)
val MuseLightBackground = Color(0xFFF8F9FA)  // Clean soft porcelain
val MuseLightSurfaceLow = Color(0xFFFFFFFF)  // Pure White Card
val MuseLightSurfaceMid = Color(0xFFF1F3F5)  // Light gray elevated
val MuseLightSurfaceHigh = Color(0xFFE9ECEF) // Border / control backgrounds
val MuseLightTextPrimary = Color(0xFF1A1A1A) // Deep Charcoal
val MuseLightTextSecondary = Color(0xFF6C757D)// Medium Gray
val MuseLightTextMuted = Color(0xFF9E9E9E)   // Light Gray

// Compatibility Aliases
val MuseTextPrimary = MuseDarkTextPrimary
val MuseTextSecondary = MuseDarkTextSecondary
val MuseTextMuted = MuseDarkTextMuted
val MuseBackground = MuseDarkBackground
val MuseSurfaceLow = MuseDarkSurfaceLow
val MuseSurfaceHigh = MuseDarkSurfaceHigh
