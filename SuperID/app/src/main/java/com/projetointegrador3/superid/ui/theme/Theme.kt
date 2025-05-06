package com.projetointegrador3.superid.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4AF37),       // dourado
    background = Color(0xFF121212),    // fundo escuro
    surface = Color(0xFF1F1F1F),       // cor de cartões etc.
    onPrimary = Color.Black,
    onSurface = Color.White            // texto em cards
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD4AF37),       // dourado
    background = Color.White,          // fundo claro
    surface = Color(0xFFF5F5F5),       // cor dos cards no claro
    onPrimary = Color.White,
    onSurface = Color.Black            // texto em cards
)

@Composable
fun SuperIDTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}

