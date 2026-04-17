package com.potadev.floatymo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.potadev.floatymo.service.FloatingOverlayService
import com.potadev.floatymo.ui.navigation.AppNavigation
import com.potadev.floatymo.ui.theme.FloatyMoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var isServiceRunning by mutableStateOf(false)
    private var startDestination by mutableStateOf("overlay_management")

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Permission result handled
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()
        isServiceRunning = FloatingOverlayService.isRunning()

        // Check onboarding status
        lifecycleScope.launch {
            val settingsRepository = AppContainer.provideSettingsRepository()
            val isOnboardingCompleted = settingsRepository.isOnboardingCompleted()
            startDestination = if (isOnboardingCompleted) "overlay_management" else "onboarding"
        }

        setContent {
            FloatyMoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        onToggleService = { enabled ->
                            if (enabled) {
                                startOverlayService()
                            } else {
                                stopOverlayService()
                            }
                            isServiceRunning = enabled
                        },
                        isServiceRunning = isServiceRunning,
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isServiceRunning = FloatingOverlayService.isRunning()
    }

    fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermissionLauncher.launch(intent)
    }

    fun startOverlayService() {
        if (!checkOverlayPermission()) {
            requestOverlayPermission()
            return
        }

        val intent = Intent(this, FloatingOverlayService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    fun stopOverlayService() {
        val intent = Intent(this, FloatingOverlayService::class.java)
        intent.action = FloatingOverlayService.ACTION_STOP
        startService(intent)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
