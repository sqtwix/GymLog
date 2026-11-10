package com.example.gymlogapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gymlogapp.R

@Composable
fun GymLogLogo(modifier: Modifier = Modifier, size: Dp = 220.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.mipmap.logo),
            contentDescription = "логотип",
            modifier = Modifier.requiredSize(
                width = size * (2752f / 1360f),
                height = size * (1536f / 1360f)
            ),
            contentScale = ContentScale.Fit
        )
    }
}
