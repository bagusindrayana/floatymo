package com.potadev.floatymo.ui.screens.position

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.potadev.floatymo.AppContainer
import com.potadev.floatymo.domain.model.ActiveOverlay
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.domain.repository.GifRepository
import com.potadev.floatymo.domain.repository.SettingsRepository
import com.potadev.floatymo.ui.theme.CyanPrimary
import com.potadev.floatymo.ui.theme.CyanSubtle
import com.potadev.floatymo.ui.theme.DarkBg
import com.potadev.floatymo.ui.theme.DarkCard
import com.potadev.floatymo.ui.theme.DarkSurface
import com.potadev.floatymo.ui.theme.StatusError
import com.potadev.floatymo.ui.theme.TextMuted
import com.potadev.floatymo.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class PositionUiState(
    val overlays: List<ActiveOverlay> = emptyList(),
    val gifs: Map<Long, SavedGif> = emptyMap(),
    val showSidebar: Boolean = false,
    val showMenu: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val selectedOverlayId: String? = null
)

class PositionViewModel(
    private val gifRepository: GifRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _overlays = MutableStateFlow<List<ActiveOverlay>>(emptyList())
    private val _gifs = MutableStateFlow<Map<Long, SavedGif>>(emptyMap())
    private val _showSidebar = MutableStateFlow(false)
    private val _showMenu = MutableStateFlow(false)
    private val _showSettingsSheet = MutableStateFlow(false)
    private val _selectedOverlayId = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(PositionUiState())

    val uiState: StateFlow<PositionUiState> = _uiState

    private fun updateUiState() {
        _uiState.value = PositionUiState(
            overlays = _overlays.value,
            gifs = _gifs.value,
            showSidebar = _showSidebar.value,
            showMenu = _showMenu.value,
            showSettingsSheet = _showSettingsSheet.value,
            selectedOverlayId = _selectedOverlayId.value
        )
    }

    fun setEditing(editing: Boolean) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            settingsRepository.updateSettings(current.copy(isEditingPosition = editing))
        }
    }

    fun loadGifs() {
        viewModelScope.launch {
            val gifs = gifRepository.getAllGifs().first()
            _gifs.value = gifs.associateBy { it.id }
            updateUiState()
        }
    }

    fun toggleSidebar() {
        _showSidebar.value = !_showSidebar.value
        updateUiState()
    }

    fun toggleMenu() {
        _showMenu.value = !_showMenu.value
        updateUiState()
    }

    fun hideMenu() {
        _showMenu.value = false
        updateUiState()
    }

    fun showSettingsSheet(overlayId: String) {
        _selectedOverlayId.value = overlayId
        _showSettingsSheet.value = true
        _showSidebar.value = false
        updateUiState()
    }

    fun hideSettingsSheet() {
        _showSettingsSheet.value = false
        _selectedOverlayId.value = null
        updateUiState()
    }

    fun loadFromRepository() {
        viewModelScope.launch {
            setEditing(true)

            val overlays = gifRepository.getActiveOverlays().first()
            val gifs = gifRepository.getAllGifs().first()
            _gifs.value = gifs.associateBy { it.id }
            
            if (overlays.isNotEmpty()) {
                _overlays.value = overlays
            }
            updateUiState()
        }
    }

    fun onExitScreen() {
        setEditing(false)
    }

    fun initializeOverlays(selectedGifIds: Set<Long>, allGifs: List<SavedGif>) {
        _gifs.value = allGifs.associateBy { it.id }
        
        if (_overlays.value.isEmpty() && selectedGifIds.isNotEmpty()) {
            val newOverlays = selectedGifIds.mapIndexed { index, gifId ->
                ActiveOverlay(gifId = gifId, x = 50 + (index * 160), y = 200)
            }
            _overlays.value = newOverlays
        }
        updateUiState()
    }

    fun addOverlay(gifId: Long) {
        val current = _overlays.value.toMutableList()
        val existing = current.find { it.gifId == gifId }
        if (existing == null && current.size < 5) {
            current.add(ActiveOverlay(gifId = gifId, x = 50 + (current.size * 160), y = 200))
            _overlays.value = current
        }
        updateUiState()
    }

    fun removeOverlay(overlayId: String) {
        val current = _overlays.value.toMutableList()
        current.removeAll { it.id == overlayId }
        _overlays.value = current
        updateUiState()
    }

    fun updateOverlayPosition(overlayId: String, x: Int, y: Int) {
        val current = _overlays.value.toMutableList()
        val index = current.indexOfFirst { it.id == overlayId }
        if (index >= 0) {
            current[index] = current[index].copy(x = x, y = y)
            _overlays.value = current
            updateUiState()
        }
    }

    fun updateOverlay(overlayId: String, size: Float? = null, opacity: Float? = null) {
        val current = _overlays.value.toMutableList()
        val index = current.indexOfFirst { it.id == overlayId }
        if (index >= 0) {
            val overlay = current[index]
            current[index] = overlay.copy(
                size = size ?: overlay.size,
                opacity = opacity ?: overlay.opacity
            )
            _overlays.value = current
            updateUiState()
        }
    }

    fun saveAll() {
        viewModelScope.launch {
            gifRepository.clearAllActiveOverlays()
            _overlays.value.forEach { overlay ->
                gifRepository.addActiveOverlay(overlay)
            }
        }
    }

    fun resetAll() {
        _overlays.value = _overlays.value.map { overlay ->
            overlay.copy(x = -1, y = -1, size = 1.0f, opacity = 1.0f)
        }
        updateUiState()
    }

    fun getGifForOverlay(gifId: Long): SavedGif? {
        return _gifs.value[gifId]
    }

    class Factory(
        private val gifRepository: GifRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PositionViewModel(gifRepository, settingsRepository) as T
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PositionScreen(
    selectedGifIds: Set<Long>,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {
    val viewModel: PositionViewModel = remember {
        PositionViewModel.Factory(
            AppContainer.provideGifRepository(),
            AppContainer.provideSettingsRepository()
        ).create(PositionViewModel::class.java)
    }

    val uiState by viewModel.uiState.collectAsState()
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val density = LocalDensity.current

    var containerScreenX by remember { mutableStateOf(0f) }
    var containerScreenY by remember { mutableStateOf(0f) }
    var containerWidthPx by remember { mutableStateOf(0f) }
    var containerHeightPx by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        viewModel.loadFromRepository()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onExitScreen()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.onExitScreen()
                    onNavigateBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }

                Text(
                    text = "Posisi Overlay",
                    style = MaterialTheme.typography.titleLarge,
                    color = CyanPrimary
                )

                Box {
                    IconButton(onClick = { viewModel.toggleMenu() }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = uiState.showMenu,
                        onDismissRequest = { viewModel.hideMenu() }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Simpan") },
                            onClick = {
                                viewModel.saveAll()
                                viewModel.onExitScreen()
                                viewModel.hideMenu()
                                onSave()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reset") },
                            onClick = {
                                viewModel.resetAll()
                                viewModel.hideMenu()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Kembali") },
                            onClick = {
                                viewModel.onExitScreen()
                                viewModel.hideMenu()
                                onNavigateBack()
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            }
                        )
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkSurface.copy(alpha = 0.5f))
                    .onGloballyPositioned { coordinates ->
                        val posInRoot = coordinates.positionInRoot()
                        containerScreenX = posInRoot.x
                        containerScreenY = posInRoot.y
                        containerWidthPx = coordinates.size.width.toFloat()
                        containerHeightPx = coordinates.size.height.toFloat()
                    }
            ) {
                // Use BoxWithConstraints dimensions directly (available immediately)
                val bwcWidthPx = with(density) { maxWidth.toPx() }
                val bwcHeightPx = with(density) { maxHeight.toPx() }
                val effectiveWidth = if (containerWidthPx > 0f) containerWidthPx else bwcWidthPx
                val effectiveHeight = if (containerHeightPx > 0f) containerHeightPx else bwcHeightPx

                if (uiState.overlays.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum ada overlay",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pilih GIF dari tombol kiri bawah",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    uiState.overlays.forEachIndexed { index, overlay ->
                        val gif = viewModel.getGifForOverlay(overlay.gifId)
                        if (gif != null) {
                            key(overlay.id, overlay.size, overlay.opacity) {
                                DraggableOverlayItem(
                                    overlay = overlay,
                                    gif = gif,
                                    overlayIndex = index,
                                    containerWidthPx = effectiveWidth,
                                    containerHeightPx = effectiveHeight,
                                    containerScreenX = containerScreenX,
                                    containerScreenY = containerScreenY,
                                    onPositionChange = { x, y ->
                                        viewModel.updateOverlayPosition(overlay.id, x, y)
                                    },
                                    onLongPress = {
                                        viewModel.showSettingsSheet(overlay.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.toggleSidebar() },
                containerColor = CyanPrimary,
                contentColor = DarkBg,
                shape = CircleShape
            ) {
                Icon(Icons.Default.List, contentDescription = "List")
            }
        }

        AnimatedVisibility(
            visible = uiState.showSidebar,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            SidebarContent(
                gifs = uiState.gifs,
                overlays = uiState.overlays,
                onSelect = { gifId -> viewModel.addOverlay(gifId) },
                onRemove = { overlayId -> viewModel.removeOverlay(overlayId) },
                onSettings = { overlayId -> viewModel.showSettingsSheet(overlayId) },
                onClose = { viewModel.toggleSidebar() }
            )
        }
    }

    if (uiState.showSettingsSheet && uiState.selectedOverlayId != null) {
        val selectedOverlay = uiState.overlays.find { it.id == uiState.selectedOverlayId }
        val gif = selectedOverlay?.let { viewModel.getGifForOverlay(it.gifId) }

        if (selectedOverlay != null && gif != null) {
            val overlayId: String = selectedOverlay.id
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideSettingsSheet() },
                sheetState = settingsSheetState,
                containerColor = DarkCard,
                scrimColor = DarkBg.copy(alpha = 0.5f)
            ) {
                key(overlayId, selectedOverlay.size, selectedOverlay.opacity) {
                    OverlaySettingsContent(
                        overlay = selectedOverlay,
                        gif = gif,
                        onSizeChange = { size -> viewModel.updateOverlay(overlayId, size = size) },
                        onOpacityChange = { opacity -> viewModel.updateOverlay(overlayId, opacity = opacity) },
                        onRemove = {
                            viewModel.removeOverlay(overlayId)
                            viewModel.hideSettingsSheet()
                        },
                        onDismiss = { viewModel.hideSettingsSheet() }
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableOverlayItem(
    overlay: ActiveOverlay,
    gif: SavedGif,
    overlayIndex: Int = 0,
    containerWidthPx: Float,
    containerHeightPx: Float,
    containerScreenX: Float,
    containerScreenY: Float,
    onPositionChange: (Int, Int) -> Unit,
    onLongPress: () -> Unit
) {
    val density = LocalDensity.current.density
    val overlaySizeDp = (150 * overlay.size).dp
    val overlaySizePx = 150f * overlay.size * density

    // Stagger default positions so overlays don't stack
    val staggerOffset = overlayIndex * (60f * density)

    fun calculateX(): Float {
        return if (overlay.x != -1) {
            overlay.x.toFloat() - containerScreenX
        } else {
            ((containerWidthPx - overlaySizePx) / 2f) + staggerOffset
        }
    }

    fun calculateY(): Float {
        return if (overlay.y != -1) {
            overlay.y.toFloat() - containerScreenY
        } else {
            ((containerHeightPx - overlaySizePx) / 2f) + staggerOffset
        }
    }

    var offsetX by remember(overlay.id) { mutableStateOf(calculateX()) }
    var offsetY by remember(overlay.id) { mutableStateOf(calculateY()) }

    // Re-sync position when container dimensions or screen position change
    LaunchedEffect(containerWidthPx, containerHeightPx, containerScreenX, containerScreenY, overlay.size, overlay.opacity) {
        offsetX = calculateX()
        offsetY = calculateY()
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(overlaySizeDp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCard.copy(alpha = 0.3f))
            .pointerInput(overlay.id) {
                detectDragGestures(
                    onDragEnd = {
                        val serviceX = (offsetX + containerScreenX).roundToInt()
                        val serviceY = (offsetY + containerScreenY).roundToInt()
                        onPositionChange(serviceX, serviceY)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
            .combinedClickable(
                onClick = { },
                onLongClick = { onLongPress() }
            )
    ) {
        GifPreviewContent(
            gifPath = gif.filePath,
            opacity = overlay.opacity,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun GifPreviewContent(
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

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageData)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = "GIF Preview",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .alpha(opacity)
            .clip(RoundedCornerShape(8.dp))
            .border(
                2.dp,
                CyanPrimary.copy(alpha = 0.4f),
                RoundedCornerShape(8.dp)
            )
    )
}

@Composable
fun SidebarContent(
    gifs: Map<Long, SavedGif>,
    overlays: List<ActiveOverlay>,
    onSelect: (Long) -> Unit,
    onRemove: (String) -> Unit,
    onSettings: (String) -> Unit,
    onClose: () -> Unit
) {
    val selectedGifs = overlays.mapNotNull { overlay ->
        gifs[overlay.gifId]
    }

    Card(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pilih Overlay",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyanPrimary
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedGifs.isEmpty()) {
                val allGifs = gifs.values.toList()
                if (allGifs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pilih GIF dari halaman utama",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allGifs) { gif ->
                            SidebarItem(
                                gif = gif,
                                isActive = false,
                                onAdd = { onSelect(gif.id) },
                                onRemove = { },
                                onSettings = { }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedGifs) { gif ->
                        val overlay = overlays.find { it.gifId == gif.id }
                        if (overlay != null) {
                            SidebarItem(
                                gif = gif,
                                isActive = true,
                                onAdd = { },
                                onRemove = { onRemove(overlay.id) },
                                onSettings = { onSettings(overlay.id) }
                            )
                        }
                    }

                    val availableGifs = gifs.values.filter { savedGif ->
                        overlays.none { it.gifId == savedGif.id }
                    }
                    if (availableGifs.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tambah",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(availableGifs) { gif ->
                            SidebarItem(
                                gif = gif,
                                isActive = false,
                                onAdd = { onSelect(gif.id) },
                                onRemove = { },
                                onSettings = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarItem(
    gif: SavedGif,
    isActive: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (isActive) onSettings() else onAdd() }
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) CyanSubtle else DarkCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GifThumbnail(
                gifPath = gif.filePath,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = gif.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (isActive) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = StatusError,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Add",
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GifThumbnail(
    gifPath: String,
    opacity: Float = 1f,
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

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageData)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = "GIF thumbnail",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .alpha(opacity)
            .clip(RoundedCornerShape(6.dp))
            .background(DarkCard, RoundedCornerShape(6.dp))
    )
}

@Composable
fun OverlaySettingsContent(
    overlay: ActiveOverlay,
    gif: SavedGif,
    onSizeChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    var sizeValue by remember(overlay.id, overlay.size) { mutableFloatStateOf(overlay.size) }
    var opacityValue by remember(overlay.id, overlay.opacity) { mutableFloatStateOf(overlay.opacity) }

    LaunchedEffect(overlay.size, overlay.opacity) {
        sizeValue = overlay.size
        opacityValue = overlay.opacity
    }

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
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${(sizeValue * 100).toInt()}% · α ${(opacityValue * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Size",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "${(sizeValue * 100).toInt()}%",
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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Opacity",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "${(opacityValue * 100).toInt()}%",
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

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRemove,
            colors = ButtonDefaults.buttonColors(
                containerColor = StatusError
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Hapus Overlay", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
