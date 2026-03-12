package com.kareem.gitmatch.feature.discover.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kareem.gitmatch.core.theme.SwipeLeftRed
import com.kareem.gitmatch.core.theme.SwipeRightGreen
import com.kareem.gitmatch.core.theme.SwipeUpBlue

@Composable
fun SwipeHintOverlay(
    swipeProgress: Float,
    swipeDirection: SwipeHintDirection,
    modifier: Modifier = Modifier
) {
    val (text, color, alignment) = when (swipeDirection) {
        SwipeHintDirection.RIGHT -> Triple("LIKE", SwipeRightGreen, Alignment.CenterStart)
        SwipeHintDirection.LEFT -> Triple("NOPE", SwipeLeftRed, Alignment.CenterEnd)
        SwipeHintDirection.UP -> Triple("MORE", SwipeUpBlue, Alignment.BottomCenter)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(swipeProgress.coerceIn(0f, 1f)),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(24.dp)
        )
    }
}

enum class SwipeHintDirection {
    RIGHT, LEFT, UP
}
