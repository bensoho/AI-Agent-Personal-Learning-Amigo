package com.agentlearning.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val base = Typography()

// Roughly 10-15% smaller than the Material defaults.
val AppTypography = base.copy(
    headlineMedium = base.headlineMedium.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontSize = 14.sp, lineHeight = 21.sp),
    bodyMedium = base.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = base.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
    labelLarge = base.labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp),
    labelMedium = base.labelMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),
    labelSmall = base.labelSmall.copy(fontSize = 10.sp, lineHeight = 14.sp),
)
