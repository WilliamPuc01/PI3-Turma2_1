package com.projetointegrador3.superid

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.projetointegrador3.superid.R

@Composable
fun BackgroundImage() {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundImage = if (isDarkTheme) {
        painterResource(R.drawable.fundoescurocerto)
    } else {
        painterResource(R.drawable.fundoclarocerto)
    }

    Image(
        painter = backgroundImage,
        contentDescription = "Fundo do app",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
    )
}