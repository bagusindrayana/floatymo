package com.potadev.floatymo.ui.screens.position

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.potadev.floatymo.AppContainer
import kotlin.math.roundToInt

@Composable
fun PositionScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: PositionViewModel = remember {
        PositionViewModel.Factory(
            AppContainer.provideSettingsRepository(),
            AppContainer.provideGifRepository()
        ).create(PositionViewModel::class.java)
    }

    val uiState by viewModel.settings.collectAsState()

    // Start position - will be synced from settings via LaunchedEffect
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Sync position from settings when entering edit mode
    LaunchedEffect(uiState.settings.overlayX, uiState.settings.overlayY) {
        if (uiState.settings.overlayX >= 0) {
            offsetX = uiState.settings.overlayX.toFloat()
        }
        if (uiState.settings.overlayY >= 0) {
            offsetY = uiState.settings.overlayY.toFloat()
        }
    }

    // Enter edit mode on start
    DisposableEffect(Unit) {
        viewModel.enterEditMode()
        if (uiState.settings.overlayX >= 0) {
            offsetX = uiState.settings.overlayX.toFloat()
        }
        if (uiState.settings.overlayY >= 0) {
            offsetY = uiState.settings.overlayY.toFloat()
        }
        onDispose {
            viewModel.exitEditMode()
        }
    }

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

    // FULLSCREEN - no TopAppBar, no Scaffold padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Back button - floating at top-left with status bar padding
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Full screen draggable area - TRUE FULLSCREEN from (0,0)
        if (uiState.gifPath != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(uiState.gifPath))
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .size((150 * uiState.size).dp)
                        .pointerInput(Unit) {
                            var isDragging = false
                            detectDragGestures(
                                onDragStart = { isDragging = true },
                                onDragEnd = { isDragging = false },
                                onDragCancel = { isDragging = false }
                            ) { change, dragAmount ->
                                if (isDragging) {
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                }
                            }
                        }
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pilih GIF terlebih dahulu\ndi Gallery",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // UI Controls at bottom - also fullscreen with status bar padding
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Geser GIF di atas untuk mengatur posisi",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        offsetX = 0f
                        offsetY = 0f
                        viewModel.resetPosition()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }

                Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                Button(
                    onClick = {
                        viewModel.savePosition(offsetX.toInt(), offsetY.toInt())
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Simpan")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Posisi: X=${offsetX.toInt()}, Y=${offsetY.toInt()}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
