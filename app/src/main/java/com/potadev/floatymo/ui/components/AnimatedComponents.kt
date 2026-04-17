package com.potadev.floatymo.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.potadev.floatymo.ui.theme.DarkCard
import com.potadev.floatymo.ui.theme.GlassBorder
import com.potadev.floatymo.ui.theme.GlassSurface
import com.potadev.floatymo.ui.theme.StatusActive
import kotlinx.coroutines.delay

/**
 * Glassmorphism-style Card
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(GlassBorder, GlassBorder.copy(alpha = 0.05f)),
                    start = Offset.Zero,
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface
        )
    ) {
        content()
    }
}

/**
 * Animated GIF Thumbnail with fade-in + scale
 */
@Composable
fun AnimatedGifThumbnail(
    gifPath: String,
    modifier: Modifier = Modifier,
    opacity: Float = 1f,
    cornerRadius: Dp = 10.dp
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    // Handle both raw paths and URI strings
    val imageData = remember(gifPath) {
        if (gifPath.startsWith("content://") || gifPath.startsWith("file://")) {
            Uri.parse(gifPath)
        } else if (gifPath.startsWith("/")) {
            Uri.parse("file://$gifPath")
        } else {
            Uri.parse(gifPath)
        }
    }

    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(gifPath) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    LaunchedEffect(gifPath) {
        alpha.animateTo(1f, tween(400))
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageData)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = "GIF thumbnail",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .scale(scale.value)
            .alpha(alpha.value * opacity)
            .clip(RoundedCornerShape(cornerRadius))
            .background(DarkCard, RoundedCornerShape(cornerRadius))
    )
}

/**
 * Pulsing dot for active status indicator
 */
@Composable
fun PulsingDot(
    modifier: Modifier = Modifier,
    color: Color = StatusActive,
    size: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale)
            .alpha(pulseAlpha)
            .background(color, CircleShape)
    )
}

/**
 * Fade-in item animation wrapper for stagger effect
 */
@Composable
fun FadeInItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(20f) }

    LaunchedEffect(index) {
        delay(index * 50L)
        alpha.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(index) {
        delay(index * 50L)
        offsetY.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha.value
                translationY = offsetY.value
            }
    ) {
        content()
    }
}

/**
 * Shimmer loading effect
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            DarkCard.copy(alpha = 0.3f),
            DarkCard.copy(alpha = 0.6f),
            DarkCard.copy(alpha = 0.3f)
        ),
        start = Offset(shimmerOffset * 300f, 0f),
        end = Offset(shimmerOffset * 300f + 300f, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush)
    )
}

/**
 * Animated scale-in content
 */
@Composable
fun ScaleInContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scaleIn"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(250),
        label = "fadeIn"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
    }
}
