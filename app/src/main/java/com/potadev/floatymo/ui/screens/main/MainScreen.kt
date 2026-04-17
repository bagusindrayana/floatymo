package com.potadev.floatymo.ui.screens.main

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.potadev.floatymo.AppContainer
import com.potadev.floatymo.MainActivity
import com.potadev.floatymo.service.FloatingOverlayService
import com.potadev.floatymo.ui.components.GlassCard
import com.potadev.floatymo.ui.components.PulsingDot
import com.potadev.floatymo.ui.theme.CyanPrimary
import com.potadev.floatymo.ui.theme.CyanSubtle
import com.potadev.floatymo.ui.theme.DarkBg
import com.potadev.floatymo.ui.theme.DarkCard
import com.potadev.floatymo.ui.theme.StatusActive
import com.potadev.floatymo.ui.theme.TextMuted
import com.potadev.floatymo.ui.theme.TextSecondary

@Composable
fun MainScreen(
    onNavigateToGallery: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: MainViewModel = remember {
        MainViewModel.Factory(
            AppContainer.provideGifRepository(),
            AppContainer.provideSettingsRepository()
        ).create(MainViewModel::class.java)
    }

    val uiState by viewModel.uiState.collectAsState()
    val mainActivity = context as? MainActivity

    val isOverlayRunning = remember {
        FloatingOverlayService.isRunning()
    }

    DisposableEffect(Unit) {
        viewModel.updateOverlayStatus(isOverlayRunning)
        onDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FloatyMo",
                style = MaterialTheme.typography.headlineLarge,
                color = CyanPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onNavigateToGallery) {
                    Icon(Icons.Default.Add, "Gallery", tint = TextSecondary)
                }
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Default.Search, "Search", tint = TextSecondary)
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, "Settings", tint = TextSecondary)
                }
            }
        }

        Text(
            text = "Floating Character Animation",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Status card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.isOverlayRunning) {
                        PulsingDot(size = 8.dp)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(TextMuted, CircleShape)
                        )
                    }
                    Text(
                        text = if (uiState.isOverlayRunning) "Active" else "Inactive",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.isOverlayRunning) StatusActive else TextSecondary
                    )
                }

                Switch(
                    checked = uiState.isOverlayRunning,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            mainActivity?.startOverlayService()
                        } else {
                            mainActivity?.stopOverlayService()
                        }
                        viewModel.updateOverlayStatus(enabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanPrimary,
                        checkedTrackColor = CyanSubtle,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = TextMuted.copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current animation card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Animation",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.activeGif != null) {
                    GifPreview(
                        gifPath = uiState.activeGif!!.filePath,
                        opacity = uiState.overlayOpacity,
                        modifier = Modifier.size(100.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No GIF\nselected",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GifPreview(
    gifPath: String,
    opacity: Float,
    modifier: Modifier = Modifier
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

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(Uri.parse(gifPath))
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = "GIF Preview",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .alpha(opacity)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkCard, RoundedCornerShape(10.dp))
    )
}
