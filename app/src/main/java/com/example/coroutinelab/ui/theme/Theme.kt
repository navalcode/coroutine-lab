package com.example.coroutinelab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.coroutinelab.R

val Ink = Color(0xFF202421)
val Muted = Color(0xFF5E625C)
val Paper = Color(0xFFFAF8F3)
val Accent = Color(0xFF9B432F)
val Soft = Color(0xFFF2E3DB)
val Line = Color(0xFFDDD8CD)
val Outline = Color(0xFF85877E)

val ReadingFont = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
)
private fun text(size: Int, height: Int, weight: FontWeight = FontWeight.Normal,
                 family: FontFamily = ReadingFont) = TextStyle(
    fontFamily = family, fontWeight = weight, fontSize = size.sp, lineHeight = height.sp,
)

@Composable
fun CoroutineLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme(
            primary = Color(0xFFEDB7A4), onPrimary = Color(0xFF35170F),
            primaryContainer = Color(0xFF4B3026), onPrimaryContainer = Color(0xFFFFD8CC),
            secondary = Color(0xFFEDB7A4), onSecondary = Color(0xFF35170F),
            secondaryContainer = Color(0xFF4B3026), onSecondaryContainer = Color(0xFFFFD8CC),
            tertiary = Color(0xFFEDB7A4), onTertiary = Color(0xFF35170F),
            background = Color(0xFF191D1A), onBackground = Color(0xFFF1EEE7),
            surface = Color(0xFF191D1A), onSurface = Color(0xFFF1EEE7), onSurfaceVariant = Color(0xFFC7C9C2),
            surfaceVariant = Color(0xFF4B3026), outline = Color(0xFF96998F), outlineVariant = Color(0xFF43483F),
            error = Color(0xFFEDB7A4), onError = Color(0xFF35170F), errorContainer = Color(0xFF4B3026), onErrorContainer = Color(0xFFF1EEE7),
        ) else lightColorScheme(
            primary = Accent, onPrimary = Color.White, primaryContainer = Soft, onPrimaryContainer = Accent,
            secondary = Accent, onSecondary = Color.White, secondaryContainer = Soft, onSecondaryContainer = Accent,
            tertiary = Ink, onTertiary = Color.White, background = Paper, onBackground = Ink,
            surface = Paper, onSurface = Ink, surfaceVariant = Soft, onSurfaceVariant = Muted,
            outline = Outline, outlineVariant = Line, error = Accent, onError = Color.White,
            errorContainer = Soft, onErrorContainer = Ink,
        ),
        typography = Typography(
            displayLarge = text(44, 50, FontWeight.Medium, ReadingFont),
            displayMedium = text(36, 42, FontWeight.Medium, ReadingFont),
            displaySmall = text(32, 38, FontWeight.Medium, ReadingFont),
            headlineLarge = text(32, 38, FontWeight.Medium, ReadingFont),
            headlineMedium = text(28, 34, FontWeight.Medium, ReadingFont),
            headlineSmall = text(26, 32, FontWeight.Medium, ReadingFont),
            titleLarge = text(22, 28, FontWeight.Medium, ReadingFont),
            titleMedium = text(17, 24, FontWeight.SemiBold),
            titleSmall = text(15, 22, FontWeight.SemiBold),
            bodyLarge = text(16, 25),
            bodyMedium = text(14, 21),
            bodySmall = text(12, 18),
            labelLarge = text(14, 20, FontWeight.SemiBold),
            labelMedium = text(12, 18, FontWeight.Medium),
            labelSmall = text(12, 18, FontWeight.SemiBold),
        ),
        shapes = Shapes(extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(12.dp), large = RoundedCornerShape(16.dp), extraLarge = RoundedCornerShape(16.dp)),
        content = content,
    )
}
