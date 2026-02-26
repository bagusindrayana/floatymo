package com.potadev.floatymo.ui.screens.overlay

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.potadev.floatymo.AppContainer
import com.potadev.floatymo.domain.model.ActiveOverlay
import com.potadev.floatymo.domain.model.SavedGif

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayManagementScreen(
    onNavigateToGallery: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onToggleService: (Boolean) -> Unit,
    isServiceRunning: Boolean
) {
    val viewModel: OverlayViewModel = remember {
        OverlayViewModel.Factory(
            AppContainer.provideGifRepository()
        ).create(OverlayViewModel::class.java)
    }

    val uiState by viewModel.uiState.collectAsState()
    val gifPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "FloatyMo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Manage Overlays (${uiState.overlays.size}/5)",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.overlays.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No overlays active",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first overlay",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.overlays) { overlay ->
                        val gif = viewModel.getGifForOverlay(overlay.gifId)
                        OverlayCard(
                            overlay = overlay,
                            gif = gif,
                            onClick = { viewModel.showSettingsSheet(overlay.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onNavigateToGallery,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Bundled")
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onNavigateToSearch,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Search")
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Settings")
                }
            }
        }

        if (uiState.showGifPicker) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideGifPicker() },
                sheetState = gifPickerSheetState
            ) {
                GifPickerContent(
                    gifs = uiState.gifs,
                    onGifSelected = { gifId -> viewModel.addOverlay(gifId) },
                    onDismiss = { viewModel.hideGifPicker() }
                )
            }
        }

        if (uiState.showSettingsSheet && uiState.selectedOverlayId != null) {
            val selectedOverlay = uiState.overlays.find { it.id == uiState.selectedOverlayId }
            val gif = selectedOverlay?.let { viewModel.getGifForOverlay(it.gifId) }

            if (selectedOverlay != null && gif != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.hideSettingsSheet() },
                    sheetState = settingsSheetState
                ) {
                    OverlaySettingsContent(
                        overlay = selectedOverlay,
                        gif = gif,
                        onSizeChange = { size -> viewModel.updateOverlay(selectedOverlay.id, size = size) },
                        onOpacityChange = { opacity -> viewModel.updateOverlay(selectedOverlay.id, opacity = opacity) },
                        onResetPosition = { viewModel.resetOverlayPosition(selectedOverlay.id) },
                        onRemove = { viewModel.removeOverlay(selectedOverlay.id) },
                        onDismiss = { viewModel.hideSettingsSheet() }
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            if (uiState.canAddMore) {
                FloatingActionButton(
                    onClick = { viewModel.showGifPicker() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add overlay")
                }
            }

            FloatingActionButton(
                onClick = { onToggleService(!isServiceRunning) },
                containerColor = if (isServiceRunning)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    if (isServiceRunning) Icons.Default.Close else Icons.Default.Check,
                    contentDescription = if (isServiceRunning) "Stop service" else "Start service"
                )
            }
        }
    }
}

@Composable
fun OverlayCard(
    overlay: ActiveOverlay,
    gif: SavedGif?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            gif?.let {
                GifThumbnail(
                    gifPath = it.filePath,
                    opacity = overlay.opacity,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gif?.name ?: "Unknown",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Size: ${(overlay.size * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Opacity: ${(overlay.opacity * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GifThumbnail(
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
        contentDescription = "GIF thumbnail",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .alpha(opacity)
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(8.dp)
            )
    )
}

@Composable
fun GifPickerContent(
    gifs: List<SavedGif>,
    onGifSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select GIF",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (gifs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No GIFs available.\nAdd some from Bundled or Search.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(gifs) { gif ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onGifSelected(gif.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            GifThumbnail(
                                gifPath = gif.filePath,
                                opacity = 1f,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun OverlaySettingsContent(
    overlay: ActiveOverlay,
    gif: SavedGif,
    onSizeChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onResetPosition: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    var sizeValue by remember(overlay.size) { mutableFloatStateOf(overlay.size) }
    var opacityValue by remember(overlay.opacity) { mutableFloatStateOf(overlay.opacity) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Overlay Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GifThumbnail(
                gifPath = gif.filePath,
                opacity = opacityValue,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = gif.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Size: ${(sizeValue * 100).toInt()}% | Opacity: ${(opacityValue * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Size",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Slider(
            value = sizeValue,
            onValueChange = {
                sizeValue = it
                onSizeChange(it)
            },
            valueRange = 0.5f..2f,
            steps = 5
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "50%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "200%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Opacity",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Slider(
            value = opacityValue,
            onValueChange = {
                opacityValue = it
                onOpacityChange(it)
            },
            valueRange = 0.3f..1f,
            steps = 6
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "30%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "100%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onResetPosition,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Position")
            }

            androidx.compose.material3.Button(
                onClick = onRemove,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Remove")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
