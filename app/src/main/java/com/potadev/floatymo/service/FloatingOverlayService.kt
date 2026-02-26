package com.potadev.floatymo.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
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
import com.potadev.floatymo.domain.repository.GifRepository
import com.potadev.floatymo.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FloatingOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var gifRepository: GifRepository
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var windowManager: WindowManager
    private lateinit var lifecycleRegistry: LifecycleRegistry
    private lateinit var savedStateRegistryController: SavedStateRegistryController

    private var floatingView: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentGifPath: String? = null
    private var currentSize: Float = 1.0f
    private var currentOpacity: Float = 1.0f
    private var currentX: Int = -1
    private var currentY: Int = -1
    private var currentIsEditing: Boolean = false

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
        
        // Initialize repositories
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
                gifRepository.getActiveGif(),
                settingsRepository.getSettings()
            ) { gif, settings ->
                Triple(gif, settings, Unit)
            }.collect { (gif, settings) ->
                val gifPath = gif?.filePath
                val size = settings.overlaySize
                val opacity = settings.overlayOpacity
                val x = settings.overlayX
                val y = settings.overlayY
                val isEditing = settings.isEditingPosition

                // Handle visibility based on editing mode
                if (isEditing) {
                    // Hide overlay when editing position
                    if (floatingView != null) {
                        removeOverlay()
                    }
                } else {
                    // Show overlay when not editing - ALWAYS try to show if there's a GIF
                    // Force recreate by passing different flag
                    if (gifPath != null) {
                        currentGifPath = gifPath
                        currentSize = size
                        currentOpacity = opacity
                        currentX = x
                        currentY = y
                        currentIsEditing = isEditing

                        if (floatingView != null) {
                            // Update existing overlay with click-through enabled
                            updateOverlay(gifPath, size, opacity, x, y, clickThrough = true)
                        } else {
                            // Create new overlay with click-through enabled
                            createOverlay(gifPath, size, opacity, x, y, clickThrough = true)
                        }
                    } else {
                        // No GIF selected - hide overlay
                        if (floatingView != null) {
                            removeOverlay()
                        }
                    }
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
        removeOverlay()
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

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FloatyMo Active")
            .setContentText("Floating animation is running")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_close, "Stop", stopIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun createOverlay(gifPath: String, size: Float, opacity: Float, x: Int = -1, y: Int = -1, clickThrough: Boolean = false) {
        if (floatingView != null) return

        val screenWidth = windowManager.defaultDisplay.width
        val screenHeight = windowManager.defaultDisplay.height

        val baseSize = 150.dp
        val sizeInPx = (baseSize.value * size * resources.displayMetrics.density).roundToInt()

        // Use saved position or default to center
        val finalX = if (x >= 0) x else screenWidth / 2 - sizeInPx / 2
        val finalY = if (y >= 0) y else screenHeight / 2 - sizeInPx / 2

        // Build flags - clickThrough determines if touches pass through to apps below
        val flags = if (clickThrough) {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE  // Allow clicks to pass through
        } else {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH  // Catch touches
        }

        overlayParams = WindowManager.LayoutParams(
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

        floatingView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingOverlayService)
            setViewTreeSavedStateRegistryOwner(this@FloatingOverlayService)

            setContent {
                FloatingGifContent(
                    gifPath = gifPath,
                    opacity = opacity,
                    size = size
                )
            }
        }

        setupDragListener(floatingView!!)

        try {
            windowManager.addView(floatingView, overlayParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOverlay(gifPath: String?, size: Float, opacity: Float, x: Int = -1, y: Int = -1, clickThrough: Boolean = false) {
        if (floatingView == null || overlayParams == null) {
            if (gifPath != null) {
                createOverlay(gifPath, size, opacity, x, y, clickThrough)
            }
            return
        }

        if (gifPath == null) {
            removeOverlay()
            return
        }

        val oldSize = currentSize
        val oldOpacity = currentOpacity
        val oldX = currentX
        val oldY = currentY

        currentGifPath = gifPath
        currentSize = size
        currentOpacity = opacity
        currentX = x
        currentY = y
        currentIsEditing = clickThrough

        val screenWidth = windowManager.defaultDisplay.width
        val screenHeight = windowManager.defaultDisplay.height
        val sizeInPx = (150.dp.value * size * resources.displayMetrics.density).roundToInt()

        val savedX = if (x >= 0) x else overlayParams?.x ?: (screenWidth / 2 - sizeInPx / 2)
        val savedY = if (y >= 0) y else overlayParams?.y ?: (screenHeight / 2 - sizeInPx / 2)

        val sizeChanged = oldSize != size
        val positionChanged = x != oldX || y != oldY

        if (!sizeChanged && !positionChanged) {
            removeOverlay()
            createOverlay(gifPath, size, opacity, savedX, savedY, clickThrough)
        } else {
            val flags = if (clickThrough) {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            } else {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            }

            overlayParams?.let { params ->
                params.width = sizeInPx
                params.height = sizeInPx
                params.x = savedX
                params.y = savedY
                params.flags = flags
                windowManager.updateViewLayout(floatingView, params)
            }

            (floatingView as ComposeView).setContent {
                key(gifPath, size, opacity) {
                    FloatingGifContent(
                        gifPath = gifPath,
                        opacity = opacity,
                        size = size
                    )
                }
            }
            floatingView?.requestLayout()
        }
    }

    private fun removeOverlay() {
        floatingView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            floatingView = null
        }
        overlayParams = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDragListener(view: View) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { _, event ->
            overlayParams?.let { params ->
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
                        windowManager.updateViewLayout(view, params)
                        true
                    }
                    else -> false
                }
            } ?: false
        }
    }
}

@Composable
fun FloatingGifContent(
    gifPath: String,
    opacity: Float,
    size: Float
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

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
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
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
