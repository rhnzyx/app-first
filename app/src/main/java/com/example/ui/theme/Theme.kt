package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = FieldlyPrimary,
    onPrimary = Color.White,
    primaryContainer = FieldlyAccent,
    onPrimaryContainer = Color.White,
    secondary = FieldlyAccent,
    onSecondary = Color.White,
    tertiary = FieldlyWarning,
    background = FieldlyBgLight,
    onBackground = FieldlyTextPrimaryLight,
    surface = FieldlySurfaceLight,
    onSurface = FieldlyTextPrimaryLight,
    surfaceVariant = FieldlySurfaceVariantLight,
    onSurfaceVariant = FieldlyTextSecondaryLight,
    error = FieldlyError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = FieldlyAccent,
    onPrimary = Color.White,
    primaryContainer = FieldlyPrimary,
    onPrimaryContainer = Color.White,
    secondary = FieldlyAccent,
    onSecondary = Color.White,
    tertiary = FieldlyWarning,
    background = FieldlyBgDark,
    onBackground = FieldlyTextPrimaryDark,
    surface = FieldlySurfaceDark,
    onSurface = FieldlyTextPrimaryDark,
    surfaceVariant = FieldlySurfaceVariantDark,
    onSurfaceVariant = FieldlyTextSecondaryDark,
    error = FieldlyError,
    onError = Color.White
)

@Composable
fun FieldlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
