package com.kareem.gitmatch.feature.discover.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.kareem.gitmatch.core.model.FeedCard
import com.kareem.gitmatch.core.model.FeedItemType
import com.kareem.gitmatch.core.theme.ZincBorder
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeableCard(
    card: FeedCard,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val swipeThreshold = 300f
    val rotationDegrees by remember {
        derivedStateOf { (offsetX.value / 30f).coerceIn(-15f, 15f) }
    }

    // Determine swipe hint direction and progress
    val swipeHintDirection by remember {
        derivedStateOf {
            when {
                offsetX.value > 50f -> SwipeHintDirection.RIGHT
                offsetX.value < -50f -> SwipeHintDirection.LEFT
                offsetY.value < -50f -> SwipeHintDirection.UP
                else -> null
            }
        }
    }
    val swipeProgress by remember {
        derivedStateOf {
            val maxOffset = swipeThreshold
            when {
                abs(offsetX.value) > abs(offsetY.value) -> abs(offsetX.value) / maxOffset
                offsetY.value < 0 -> abs(offsetY.value) / maxOffset
                else -> 0f
            }
        }
    }

    // NestedScrollConnection: when inner scrollable content reaches the bottom
    // and the user keeps dragging up, the overflow feeds into offsetY to trigger swipe-up.
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // available.y < 0 means upward scroll that the child couldn't consume
                if (available.y < 0 && source == NestedScrollSource.Drag) {
                    scope.launch { offsetY.snapTo(offsetY.value + available.y) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offsetY.value < -swipeThreshold) {
                    // Threshold crossed — fly off and trigger swipe up
                    scope.launch {
                        offsetY.animateTo(-1500f, spring())
                        onSwipeUp()
                    }
                    return available
                } else if (offsetY.value < -10f) {
                    // Below threshold — snap back
                    scope.launch {
                        offsetY.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 300f))
                    }
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        offsetX.value.roundToInt(),
                        offsetY.value.roundToInt()
                    )
                }
                .graphicsLayer {
                    rotationZ = rotationDegrees
                }
                .pointerInput(card.id) {
                    detectDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value > swipeThreshold -> {
                                        // Fly off right
                                        offsetX.animateTo(1500f, spring())
                                        onSwipeRight()
                                    }
                                    offsetX.value < -swipeThreshold -> {
                                        // Fly off left
                                        offsetX.animateTo(-1500f, spring())
                                        onSwipeLeft()
                                    }
                                    offsetY.value < -swipeThreshold -> {
                                        // Fly off up
                                        offsetY.animateTo(-1500f, spring())
                                        onSwipeUp()
                                    }
                                    else -> {
                                        // Snap back to center
                                        launch { offsetX.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 300f)) }
                                        launch { offsetY.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 300f)) }
                                    }
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                offsetY.snapTo(offsetY.value + dragAmount.y)
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, ZincBorder),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
            ) {
                // Card content based on type
                when (card.type) {
                    FeedItemType.REPO -> RepoCardContent(card = card)
                    FeedItemType.NEWS -> NewsCardContent(card = card)
                }

                // Swipe hint overlay
                swipeHintDirection?.let { direction ->
                    SwipeHintOverlay(
                        swipeProgress = swipeProgress,
                        swipeDirection = direction
                    )
                }
            }
        }
    }
}
