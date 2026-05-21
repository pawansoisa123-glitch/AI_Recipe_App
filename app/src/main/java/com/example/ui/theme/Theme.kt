package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFAECEB1),
    onPrimary = Color(0xFF1E3523),
    primaryContainer = NaturalSage,
    onPrimaryContainer = Color(0xFFD4EAD6),
    secondary = Color(0xFFC7C7B7),
    onSecondary = Color(0xFF2E3228),
    secondaryContainer = Color(0xFF3B4133),
    onSecondaryContainer = Color(0xFFE4E2D3),
    tertiary = Color(0xFFE5C39C),
    onTertiary = Color(0xFF432B13),
    tertiaryContainer = Color(0xFF5E432A),
    onTertiaryContainer = Color(0xFFFBE4CD),
    background = Color(0xFF1A1A15),
    onBackground = Color(0xFFE6E5DE),
    surface = Color(0xFF161612),
    onSurface = Color(0xFFE6E5DE),
    surfaceVariant = Color(0xFF2A2B23),
    onSurfaceVariant = Color(0xFFC7C7B7),
    outline = Color(0xFF5E6254)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NaturalSage,
    onPrimary = OnNaturalSage,
    primaryContainer = PaleSage,
    onPrimaryContainer = OnPaleSage,
    secondary = NaturalGrey,
    onSecondary = NaturalIvory,
    secondaryContainer = NaturalTaupe,
    onSecondaryContainer = NaturalGrey,
    tertiary = WarmClay,
    onTertiary = Color.White,
    tertiaryContainer = WarmClayContainer,
    onTertiaryContainer = NaturalCharcoal,
    background = NaturalIvory,
    onBackground = NaturalCharcoal,
    surface = LightIvory,
    onSurface = NaturalCharcoal,
    surfaceVariant = NaturalNavBg,
    onSurfaceVariant = NaturalGrey,
    outline = NaturalTaupe,
    outlineVariant = PaleSage
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamicColor by default to prioritize our precise Natural Tones theme branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
