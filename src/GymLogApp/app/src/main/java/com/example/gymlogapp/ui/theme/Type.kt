package com.example.gymlogapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import com.example.gymlogapp.R

// Change this family in one place to switch the app typography later.
private val GymLogFontFamily = FontFamily(
    Font(R.font.comfortaa_variable)
)

private fun TextStyle.withGymLogFont() = copy(fontFamily = GymLogFontFamily)

// Apply the family to the full Material type scale so buttons, fields, tabs,
// dialogs, and screen text all use Comfortaa. Change the family above to revert.
private val DefaultTypography = Typography()
val Typography = Typography(
    displayLarge = DefaultTypography.displayLarge.withGymLogFont(),
    displayMedium = DefaultTypography.displayMedium.withGymLogFont(),
    displaySmall = DefaultTypography.displaySmall.withGymLogFont(),
    headlineLarge = DefaultTypography.headlineLarge.withGymLogFont(),
    headlineMedium = DefaultTypography.headlineMedium.withGymLogFont(),
    headlineSmall = DefaultTypography.headlineSmall.withGymLogFont(),
    titleLarge = DefaultTypography.titleLarge.withGymLogFont(),
    titleMedium = DefaultTypography.titleMedium.withGymLogFont(),
    titleSmall = DefaultTypography.titleSmall.withGymLogFont(),
    bodyLarge = DefaultTypography.bodyLarge.withGymLogFont(),
    bodyMedium = DefaultTypography.bodyMedium.withGymLogFont(),
    bodySmall = DefaultTypography.bodySmall.withGymLogFont(),
    labelLarge = DefaultTypography.labelLarge.withGymLogFont(),
    labelMedium = DefaultTypography.labelMedium.withGymLogFont(),
    labelSmall = DefaultTypography.labelSmall.withGymLogFont()
)
