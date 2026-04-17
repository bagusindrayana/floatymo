package com.potadev.floatymo.ui.screens.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.potadev.floatymo.AppContainer
import com.potadev.floatymo.domain.model.ActiveOverlay
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.ui.components.AnimatedGifThumbnail
import com.potadev.floatymo.ui.components.FadeInItem
import com.potadev.floatymo.ui.components.GlassCard
import com.potadev.floatymo.ui.components.PulsingDot
import com.potadev.floatymo.ui.theme.CyanPrimary
import com.potadev.floatymo.ui.theme.CyanSubtle
import com.potadev.floatymo.ui.theme.DarkBg
import com.potadev.floatymo.ui.theme.DarkCard
import com.potadev.floatymo.ui.theme.StatusActive
import com.potadev.floatymo.ui.theme.StatusError
import com.potadev.floatymo.ui.theme.TextMuted
import com.potadev.floatymo.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayManagementScreen(
    onNavigateToGallery: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPosition: () -> Unit,
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

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ─── Header ───
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "FloatyMo",
                    style = MaterialTheme.typography.headlineLarge,
                    color = CyanPrimary
                )
                Text(
                    text = "${uiState.overlays.size} overlay aktif",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // ─── Service Toggle ───
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
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
                        if (isServiceRunning) {
                            PulsingDot(size = 8.dp)
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(TextMuted, CircleShape)
                            )
                        }
                        Text(
                            text = if (isServiceRunning) "Overlay Aktif" else "Overlay Nonaktif",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isServiceRunning) StatusActive else TextSecondary
                        )
                    }

                    Switch(
                        checked = isServiceRunning,
                        onCheckedChange = { onToggleService(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanPrimary,
                            checkedTrackColor = CyanSubtle,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = TextMuted.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── Overlay List ───
            if (uiState.overlays.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada overlay",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Tap + untuk menambah overlay",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.overlays) { index, overlay ->
                        FadeInItem(index = index) {
                            val gif = viewModel.getGifForOverlay(overlay.gifId)
                            OverlayCard(
                                overlay = overlay,
                                gif = gif,
                                onClick = { viewModel.showSettingsSheet(overlay.id) }
                            )
                        }
                    }
                }
            }

            // ─── Bottom Navigation ───
            NavigationBar(
                containerColor = DarkCard,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToGallery,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Gallery") },
                    label = { Text("Bundled", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = CyanSubtle
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSearch,
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = CyanSubtle
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToPosition,
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Position") },
                    label = { Text("Posisi", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = CyanSubtle
                    )
                )
            }
        }

        // ─── FAB ───
        if (uiState.canAddMore) {
            FloatingActionButton(
                onClick = { viewModel.showGifPicker() },
                containerColor = CyanPrimary,
                contentColor = DarkBg,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 88.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add overlay")
            }
        }

        // ─── GIF Picker Sheet ───
        if (uiState.showGifPicker) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideGifPicker() },
                sheetState = gifPickerSheetState,
                containerColor = DarkCard,
                scrimColor = DarkBg.copy(alpha = 0.5f)
            ) {
                GifPickerContent(
                    gifs = uiState.gifs,
                    onGifSelected = { gifId -> viewModel.addOverlay(gifId) },
                    onDismiss = { viewModel.hideGifPicker() }
                )
            }
        }

        // ─── Overlay Settings Sheet ───
        if (uiState.showSettingsSheet && uiState.selectedOverlayId != null) {
            val selectedOverlay = uiState.overlays.find { it.id == uiState.selectedOverlayId }
            val gif = selectedOverlay?.let { viewModel.getGifForOverlay(it.gifId) }

            if (selectedOverlay != null && gif != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.hideSettingsSheet() },
                    sheetState = settingsSheetState,
                    containerColor = DarkCard,
                    scrimColor = DarkBg.copy(alpha = 0.5f)
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
}

// ─── Overlay Card ───
@Composable
fun OverlayCard(
    overlay: ActiveOverlay,
    gif: SavedGif?,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            gif?.let {
                AnimatedGifThumbnail(
                    gifPath = it.filePath,
                    opacity = overlay.opacity,
                    modifier = Modifier.size(50.dp),
                    cornerRadius = 8.dp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gif?.name ?: "Unknown",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${(overlay.size * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanPrimary
                    )
                    Text(
                        text = "α ${(overlay.opacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = "Atur ›",
                style = MaterialTheme.typography.labelSmall,
                color = CyanPrimary
            )
        }
    }
}

// ─── GIF Picker ───
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
                text = "Pilih GIF",
                style = MaterialTheme.typography.titleLarge,
                color = CyanPrimary
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (gifs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada GIF.\nTambah dari Bundled atau Search.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(gifs) { gif ->
                    GlassCard(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onGifSelected(gif.id) }
                            ),
                        cornerRadius = 10.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedGifThumbnail(
                                gifPath = gif.filePath,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                cornerRadius = 8.dp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─── Overlay Settings ───
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
                text = "Pengaturan Overlay",
                style = MaterialTheme.typography.titleLarge,
                color = CyanPrimary
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // GIF Preview + info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedGifThumbnail(
                gifPath = gif.filePath,
                opacity = opacityValue,
                modifier = Modifier.size(64.dp),
                cornerRadius = 10.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = gif.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${(sizeValue * 100).toInt()}% · α ${(opacityValue * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Size slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Size", style = MaterialTheme.typography.titleSmall)
            Text(
                "${(sizeValue * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = CyanPrimary
            )
        }

        Slider(
            value = sizeValue,
            onValueChange = {
                sizeValue = it
                onSizeChange(it)
            },
            valueRange = 0.5f..2f,
            steps = 5,
            colors = SliderDefaults.colors(
                thumbColor = CyanPrimary,
                activeTrackColor = CyanPrimary,
                inactiveTrackColor = DarkCard
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Opacity slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Opacity", style = MaterialTheme.typography.titleSmall)
            Text(
                "${(opacityValue * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = CyanPrimary
            )
        }

        Slider(
            value = opacityValue,
            onValueChange = {
                opacityValue = it
                onOpacityChange(it)
            },
            valueRange = 0.3f..1f,
            steps = 6,
            colors = SliderDefaults.colors(
                thumbColor = CyanPrimary,
                activeTrackColor = CyanPrimary,
                inactiveTrackColor = DarkCard
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onResetPosition,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Posisi", style = MaterialTheme.typography.labelLarge)
            }

            Button(
                onClick = onRemove,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusError)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hapus", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
