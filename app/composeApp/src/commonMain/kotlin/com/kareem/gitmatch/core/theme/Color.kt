package com.kareem.gitmatch.core.theme

import androidx.compose.ui.graphics.Color

// ── Zinc Neutrals ──────────────────────────────────────────
val ZincBackground   = Color(0xFF09090B)   // Deep zinc background
val ZincSurface      = Color(0xFF18181B)   // Card / surface
val ZincBorder       = Color(0xFF27272A)   // Structural border grey
val ZincMuted        = Color(0xFF3F3F46)   // Muted elements
val ZincSubtle       = Color(0xFF52525B)   // Subtle text / icons
val ZincText         = Color(0xFFA1A1AA)   // Secondary text
val ZincTextPrimary  = Color(0xFFFAFAFA)   // Primary text

// ── Light-mode zinc tones ──────────────────────────────────
val ZincLightBg      = Color(0xFFF4F4F5)   // zinc-100
val ZincLightSurface = Color(0xFFFFFFFF)
val ZincLightBorder  = Color(0xFFE4E4E7)   // zinc-200
val ZincLightText    = Color(0xFF18181B)
val ZincLightTextSec = Color(0xFF71717A)   // zinc-500

// ── Accent Colors ──────────────────────────────────────────
val Emerald          = Color(0xFF10B981)   // Primary accent
val Rose             = Color(0xFFEF4444)   // Destructive / nope
val Indigo           = Color(0xFF6366F1)   // Tertiary / "more info"
val Amber            = Color(0xFFF59E0B)   // Stars / secondary

// ── Backward-compatible aliases (old names → new values) ──
val SwipeRightGreen      = Emerald
val SwipeLeftRed         = Rose
val SwipeUpBlue          = Indigo
val StarAmber            = Amber
val GoodFirstIssuePurple = Indigo
val SubtitleGray         = ZincText
