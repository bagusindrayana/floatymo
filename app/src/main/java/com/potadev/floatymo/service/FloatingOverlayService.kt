package com.potadev.floatymo.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import com.potadev.floatymo.MainActivity
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.potadev.floatymo.AppContainer
import com.potadev.floatymo.R
import com.potadev.floatymo.domain.model.ActiveOverlay
import com.potadev.floatymo.domain.model.SavedGif
import com.potadev.floatymo.domain.repository.GifRepository
import com.potadev.floatymo.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class OverlayInstance(
    val id: String,
    val view: View,
    val params: WindowManager.LayoutParams,
    val gifPath: String
)

class FloatingOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var gifRepository: GifRepository
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var windowManager: WindowManager
    private lateinit var lifecycleRegistry: LifecycleRegistry
    private lateinit var savedStateRegistryController: SavedStateRegistryController

    private val overlays = mutableMapOf<String, OverlayInstance>()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val CHANNEL_ID = "floatymo_overlay_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "com.potadev.floatymo.ACTION_STOP"

        private var isRunning = false

        fun isRunning(): Boolean = isRunning
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        
        gifRepository = AppContainer.provideGifRepository()
        settingsRepository = AppContainer.provideSettingsRepository()
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        lifecycleRegistry = LifecycleRegistry(this)
        savedStateRegistryController = SavedStateRegistryController.create(this)
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            combine(
                gifRepository.getActiveOverlays(),
                gifRepository.getAllGifs(),
                settingsRepository.getSettings()
            ) { overlaysList, gifs, settings ->
                Triple(overlaysList, gifs, settings)
            }.collect { (overlaysList, gifs, settings) ->
                val isEditing = settings.isEditingPosition

                if (isEditing) {
                    hideAllOverlays()
                } else {
                    syncOverlays(overlaysList, gifs)
                }
            }
        }
    }

    private fun syncOverlays(activeOverlays: List<ActiveOverlay>, gifs: List<SavedGif>) {
        val currentIds = overlays.keys.toSet()
        val newIds = activeOverlays.map { it.id }.toSet()

        val toRemove = currentIds - newIds
        toRemove.forEach { removeOverlay(it) }

        activeOverlays.forEach { overlay ->
            val gif = gifs.find { it.id == overlay.gifId }
            if (gif != null) {
                if (overlays.containsKey(overlay.id)) {
                    updateOverlay(overlay, gif.filePath)
                } else {
                    createOverlay(overlay.id, gif.filePath, overlay.size, overlay.opacity, overlay.x, overlay.y)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()
        removeAllOverlays()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FloatyMo Overlay",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Floating animation overlay service"
            setShowBadge(false)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, FloatingOverlayService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val overlayCount = overlays.size
        val contentText = if (overlayCount == 0) {
            "No overlays active"
        } else {
            "$overlayCount overlay${if (overlayCount > 1) "s" else ""} active"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FloatyMo Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_close, "Stop", stopIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun createOverlay(id: String, gifPath: String, size: Float, opacity: Float, x: Int = -1, y: Int = -1) {
        if (overlays.containsKey(id)) return

        val screenWidth = windowManager.defaultDisplay.width
        val screenHeight = windowManager.defaultDisplay.height

        val baseSize = 150.dp
        val sizeInPx = (baseSize.value * size * resources.displayMetrics.density).roundToInt()

        val finalX = if (x >= 0) x else screenWidth / 2 - sizeInPx / 2
        val finalY = if (y >= 0) y else screenHeight / 2 - sizeInPx / 2

        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        val params = WindowManager.LayoutParams(
            sizeInPx,
            sizeInPx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = finalX
            this.y = finalY
        }

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingOverlayService)
            setViewTreeSavedStateRegistryOwner(this@FloatingOverlayService)

            setContent {
                key(id, size, opacity) {
                    FloatingGifContent(
                        gifPath = gifPath,
                        opacity = opacity,
                        size = size
                    )
                }
            }
        }

        setupDragListener(id, view, params)

        try {
            windowManager.addView(view, params)
            overlays[id] = OverlayInstance(id, view, params, gifPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOverlay(overlay: ActiveOverlay, gifPath: String) {
        val instance = overlays[overlay.id] ?: return

        val screenWidth = windowManager.defaultDisplay.width
        val screenHeight = windowManager.defaultDisplay.height
        val sizeInPx = (150.dp.value * overlay.size * resources.displayMetrics.density).roundToInt()

        val savedX = if (overlay.x >= 0) overlay.x else instance.params.x
        val savedY = if (overlay.y >= 0) overlay.y else instance.params.y

        instance.params.width = sizeInPx
        instance.params.height = sizeInPx
        instance.params.x = savedX
        instance.params.y = savedY

        try {
            windowManager.updateViewLayout(instance.view, instance.params)

            (instance.view as ComposeView).setContent {
                key(overlay.id, overlay.size, overlay.opacity) {
                    FloatingGifContent(
                        gifPath = gifPath,
                        opacity = overlay.opacity,
                        size = overlay.size
                    )
                }
            }
            instance.view.requestLayout()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlay(id: String) {
        val instance = overlays[id] ?: return
        try {
            windowManager.removeView(instance.view)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        overlays.remove(id)
    }

    private fun removeAllOverlays() {
        overlays.keys.toList().forEach { removeOverlay(it) }
    }

    private fun hideAllOverlays() {
        overlays.keys.toList().forEach { removeOverlay(it) }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDragListener(id: String, view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).roundToInt()
                    params.y = initialY + (event.rawY - initialTouchY).roundToInt()
                    try {
                        windowManager.updateViewLayout(view, params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    serviceScope.launch(Dispatchers.IO) {
                        val overlay = gifRepository.getActiveOverlayById(id)
                        overlay?.let {
                            gifRepository.updateActiveOverlay(it.copy(x = params.x, y = params.y))
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun updateOverlayPosition(id: String, x: Int, y: Int) {
        val instance = overlays[id] ?: return
        instance.params.x = x
        instance.params.y = y
        try {
            windowManager.updateViewLayout(instance.view, instance.params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun FloatingGifContent(
    gifPath: String,
    opacity: Float,
    size: Float
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val baseSize = 150
    val actualSize = (baseSize * size).dp

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(Uri.parse(gifPath))
                .crossfade(true)
                .build(),
            contentDescription = "Floating animation",
            imageLoader = imageLoader,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(actualSize)
                .alpha(opacity)
                .background(Color.Transparent)
        )
    }
}
