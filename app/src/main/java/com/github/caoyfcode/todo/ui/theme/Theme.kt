package com.github.caoyfcode.todo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

private val defaultDarkColorScheme = darkColorScheme()
private val defaultLightColorScheme = lightColorScheme()

private val DarkColorScheme = darkColorScheme(
    primary = defaultDarkColorScheme.background,
    onPrimary = defaultDarkColorScheme.onBackground,
    secondary = defaultDarkColorScheme.background,
    onSecondary = defaultDarkColorScheme.onBackground,
    tertiary = DarkGroupSelectedColor,
)

private val LightColorScheme = lightColorScheme(
    primary = defaultLightColorScheme.background,
    onPrimary = defaultLightColorScheme.onBackground,
    secondary = defaultLightColorScheme.background,
    onSecondary = defaultLightColorScheme.onBackground,
    tertiary = LightGroupSelectedColor,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TodoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    // 修改状态栏颜色
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // 设置状态栏为背景色
            // 因为状态栏颜色为背景色, 则状态栏前景应该在黑暗模式下亮, 非黑暗模式下暗
            // isAppearanceLightStatusBars 为 true 时前景将为黑色, false 时前景为默认(白色), 故为 !darkTheme
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}