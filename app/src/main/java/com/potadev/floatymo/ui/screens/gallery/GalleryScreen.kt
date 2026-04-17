package com.potadev.floatymo.ui.screens.gallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.potadev.floatymo.AppContainer
import com.potadev.floatymo.domain.model.GifSource
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.ui.components.GlassCard
import com.potadev.floatymo.ui.theme.CyanPrimary
import com.potadev.floatymo.ui.theme.CyanSubtle
import com.potadev.floatymo.ui.theme.DarkBg
import com.potadev.floatymo.ui.theme.DarkCard
import com.potadev.floatymo.ui.theme.StatusError
import com.potadev.floatymo.ui.theme.TextMuted
import com.potadev.floatymo.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GalleryScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: GalleryViewModel = remember {
        GalleryViewModel.Factory(
            AppContainer.provideGifRepository(),
            AppContainer.provideSettingsRepository(),
            context
        ).create(GalleryViewModel::class.java)
    }

    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is GalleryUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importGifFromGallery(it) }
    }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { pickerLauncher.launch("image/*") },
                    containerColor = CyanPrimary,
                    contentColor = DarkBg,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Import GIF")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Custom header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextSecondary)
                }
                Text(
                    text = "Gallery",
                    style = MaterialTheme.typography.titleLarge,
                    color = CyanPrimary
                )
            }

            // Custom tab row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabPill(
                    text = "Bundled (${uiState.bundleGifs.size})",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabPill(
                    text = "My GIFs (${uiState.savedGifs.size})",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> BundledGifGrid(
                    gifs = uiState.bundleGifs,
                    onSelect = { viewModel.selectBundleGif(it) }
                )
                1 -> SavedGifList(
                    gifs = uiState.savedGifs,
                    activeGifId = uiState.activeGifId,
                    onSelect = { viewModel.selectSavedGif(it) },
                    onDelete = { viewModel.deleteSavedGif(it) }
                )
            }
        }
    }
}

@Composable
fun TabPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) CyanSubtle else DarkCard,
        animationSpec = tween(250),
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) CyanPrimary else TextSecondary,
        animationSpec = tween(250),
        label = "tabText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
fun BundledGifGrid(
    gifs: List<BundleGif>,
    onSelect: (BundleGif) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(gifs) { gif ->
            BundledGifItem(
                gif = gif,
                onClick = { onSelect(gif) }
            )
        }
    }
}

@Composable
fun BundledGifItem(
    gif: BundleGif,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        cornerRadius = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val resourceId = remember(gif.resourceName) {
                context.resources.getIdentifier(
                    gif.resourceName,
                    "drawable",
                    context.packageName
                )
            }

            if (resourceId != 0) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(resourceId)
                        .crossfade(true)
                        .build(),
                    contentDescription = gif.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(DarkCard, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gif.name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = gif.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SavedGifList(
    gifs: List<SavedGif>,
    activeGifId: Long?,
    onSelect: (SavedGif) -> Unit,
    onDelete: (SavedGif) -> Unit
) {
    if (gifs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No saved GIFs",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Import GIFs from gallery or download from GIPHY",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gifs) { gif ->
                SavedGifItem(
                    gif = gif,
                    isActive = gif.id == activeGifId,
                    onSelect = { onSelect(gif) },
                    onDelete = { onDelete(gif) }
                )
            }
        }
    }
}

@Composable
fun SavedGifItem(
    gif: SavedGif,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
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

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            ),
        cornerRadius = 12.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(gif.filePath))
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = gif.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            )

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(22.dp)
                    .background(StatusError, CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(12.dp)
                )
            }

            // Source label
            Text(
                text = when (gif.source) {
                    GifSource.BUNDLE -> "Bundle"
                    GifSource.GIPHY -> "GIPHY"
                    GifSource.USER -> "Imported"
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .background(DarkBg.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            // Active indicator
            if (isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(10.dp)
                        .background(CyanPrimary, CircleShape)
                )
            }
        }
    }
}
