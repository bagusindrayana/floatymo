package com.potadev.floatymo.ui.screens.home

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.potadev.floatymo.AppContainer
import com.potadev.floatymo.domain.model.GifSource
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.ui.components.AnimatedGifThumbnail
import com.potadev.floatymo.ui.components.FadeInItem
import com.potadev.floatymo.ui.components.GlassCard
import com.potadev.floatymo.ui.components.PulsingDot
import com.potadev.floatymo.ui.theme.CyanPrimary
import com.potadev.floatymo.ui.theme.CyanSubtle
import com.potadev.floatymo.ui.theme.StatusActive
import com.potadev.floatymo.ui.theme.TextMuted
import com.potadev.floatymo.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onNavigateToGallery: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onToggleService: (Boolean) -> Unit,
    isServiceRunning: Boolean
) {
    val viewModel: HomeViewModel = remember {
        HomeViewModel.Factory(
            AppContainer.provideGifRepository()
        ).create(HomeViewModel::class.java)
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(isServiceRunning) {
        viewModel.setServiceRunning(isServiceRunning)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FloatyMo",
                    style = MaterialTheme.typography.headlineLarge,
                    color = CyanPrimary
                )
                Text(
                    text = "Pilih GIF untuk overlay",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

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

        Spacer(modifier = Modifier.height(12.dp))

        // GIF List
        if (uiState.gifs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada GIF",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Tambah dari Bundled atau Search",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(uiState.gifs) { index, gif ->
                    FadeInItem(index = index) {
                        GifListItem(
                            gif = gif,
                            isSelected = uiState.selectedGifIds.contains(gif.id),
                            onToggle = { viewModel.toggleGifSelection(gif.id) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Service Toggle
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.isServiceRunning) {
                        PulsingDot(size = 8.dp)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(TextMuted, CircleShape)
                        )
                    }
                    Column {
                        Text(
                            text = if (uiState.isServiceRunning) "Overlay Aktif" else "Overlay Nonaktif",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (uiState.isServiceRunning) StatusActive else TextSecondary
                        )
                        Text(
                            text = "${uiState.selectedGifIds.size}/5 overlay",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Switch(
                    checked = uiState.isServiceRunning,
                    onCheckedChange = { enabled ->
                        onToggleService(enabled)
                        viewModel.setServiceRunning(enabled)
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
    }
}

@Composable
fun GifListItem(
    gif: SavedGif,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) CyanSubtle else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "bgColor"
    )
    val selectScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selectScale"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedGifThumbnail(
                gifPath = gif.filePath,
                modifier = Modifier.size(44.dp),
                cornerRadius = 8.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gif.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (gif.source) {
                        GifSource.BUNDLE -> "Bundled"
                        GifSource.GIPHY -> "GIPHY"
                        GifSource.USER -> "Imported"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Animated check circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) CyanPrimary else TextMuted.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else TextMuted,
                    modifier = Modifier
                        .size(14.dp)
                        .scale(selectScale)
                )
            }
        }
    }
}
