package ir.studiocube.themoments.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val LightColorScheme = lightColorScheme()
val DarkColorScheme = darkColorScheme()

@Composable
fun TheMomentsTheme(
    dynamicTheme: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = if (darkTheme) {
        if (dynamicTheme) dynamicLightColorScheme(context) else LightColorScheme
    } else {
        if (dynamicTheme) dynamicDarkColorScheme(context) else DarkColorScheme
    }

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = darkTheme,
        isNavigationBarContrastEnforced = false,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        typography = Typography,
        content = content,
    )
}