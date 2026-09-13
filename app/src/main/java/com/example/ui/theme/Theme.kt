package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.viewmodel.AppThemeStyle

// Cyber Void
private val CyberVoidDark = darkColorScheme(
    primary = Color(0xFF00FFCC), // Cyan
    secondary = Color(0xFFFF00FF), // Magenta
    tertiary = Color(0xFFB000FF),
    background = Color(0xFF050510),
    surface = Color(0xFF111122),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)
private val CyberVoidLight = lightColorScheme(
    primary = Color(0xFF00B099),
    secondary = Color(0xFFCC00CC),
    tertiary = Color(0xFF8800CC),
    background = Color(0xFFE8F0F2),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF050510),
    onSurface = Color(0xFF050510)
)

// Alpine Frost
private val AlpineFrostDark = darkColorScheme(
    primary = Color(0xFF4A90E2),
    secondary = Color(0xFF50E3C2),
    tertiary = Color(0xFF9013FE),
    background = Color(0xFF0B141A),
    surface = Color(0xFF13222B),
    onPrimary = Color.White,
    onBackground = Color(0xFFE0F7FA),
    onSurface = Color(0xFFE0F7FA)
)
private val AlpineFrostLight = lightColorScheme(
    primary = Color(0xFF1E88E5),
    secondary = Color(0xFF26A69A),
    tertiary = Color(0xFF5E35B1),
    background = Color(0xFFF0F8FF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF0B141A),
    onSurface = Color(0xFF0B141A)
)

// Sage Garden
private val SageGardenDark = darkColorScheme(
    primary = Color(0xFFA5C596),
    secondary = Color(0xFFDEBA85),
    tertiary = Color(0xFF9E7E62),
    background = Color(0xFF1B231B),
    surface = Color(0xFF263226),
    onPrimary = Color.Black,
    onBackground = Color(0xFFEBEBEB),
    onSurface = Color(0xFFEBEBEB)
)
private val SageGardenLight = lightColorScheme(
    primary = Color(0xFF558B2F),
    secondary = Color(0xFFD4A373),
    tertiary = Color(0xFF8D6E63),
    background = Color(0xFFF9F7F1),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF2E3B32),
    onSurface = Color(0xFF2E3B32)
)

// Hyper Berry
private val HyperBerryDark = darkColorScheme(
    primary = Color(0xFFFF007F),
    secondary = Color(0xFF8A2BE2),
    tertiary = Color(0xFFFF8C00),
    background = Color(0xFF1A0011),
    surface = Color(0xFF2D001E),
    onPrimary = Color.White,
    onBackground = Color(0xFFFFF0F5),
    onSurface = Color(0xFFFFF0F5)
)
private val HyperBerryLight = lightColorScheme(
    primary = Color(0xFFD81B60),
    secondary = Color(0xFF5E35B1),
    tertiary = Color(0xFFE65100),
    background = Color(0xFFFFF5F8),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF3E0028),
    onSurface = Color(0xFF3E0028)
)

// Default Theme
private val DarkColorScheme = darkColorScheme(
    primary = IndigoDarkPrimary,
    secondary = EmeraldSuccess,
    tertiary = IndigoSecondary,
    background = SlateDarkBackground,
    surface = SlateDarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = SlateDarkText,
    onSurface = SlateDarkText
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    secondary = EmeraldSuccess,
    tertiary = IndigoSecondary,
    background = SlateBackground,
    surface = SlateSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = SlateText,
    onSurface = SlateText
)

@Composable
fun MyApplicationTheme(
    appThemeStyle: AppThemeStyle = AppThemeStyle.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (appThemeStyle) {
        AppThemeStyle.CYBER_VOID -> if (darkTheme) CyberVoidDark else CyberVoidLight
        AppThemeStyle.ALPINE_FROST -> if (darkTheme) AlpineFrostDark else AlpineFrostLight
        AppThemeStyle.SAGE_GARDEN -> if (darkTheme) SageGardenDark else SageGardenLight
        AppThemeStyle.HYPER_BERRY -> if (darkTheme) HyperBerryDark else HyperBerryLight
        AppThemeStyle.DEFAULT -> if (darkTheme) DarkColorScheme else LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
