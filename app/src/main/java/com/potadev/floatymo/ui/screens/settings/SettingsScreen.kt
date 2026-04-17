package com.potadev.floatymo.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.potadev.floatymo.AppContainer
import com.potadev.floatymo.domain.model.OverlaySize
import com.potadev.floatymo.ui.components.GlassCard
import com.potadev.floatymo.ui.theme.CyanPrimary
import com.potadev.floatymo.ui.theme.CyanSubtle
import com.potadev.floatymo.ui.theme.DarkBg
import com.potadev.floatymo.ui.theme.DarkCard
import com.potadev.floatymo.ui.theme.TextMuted
import com.potadev.floatymo.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPosition: () -> Unit
) {
    val viewModel: SettingsViewModel = remember {
        SettingsViewModel.Factory(
            AppContainer.provideSettingsRepository()
        ).create(SettingsViewModel::class.java)
    }

    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    var opacityValue by remember(settings.overlayOpacity) {
        mutableFloatStateOf(settings.overlayOpacity)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
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
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                color = CyanPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Size section
            Text(
                text = "OVERLAY SIZE",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OverlaySize.entries.forEach { size ->
                        val isSelected = settings.overlaySize == size.scale
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanSubtle else DarkCard.copy(alpha = 0.5f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { viewModel.updateSize(size) }
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = size.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) CyanPrimary else TextSecondary
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(CyanPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = DarkBg,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Opacity section
            Text(
                text = "OPACITY",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transparency",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "${(opacityValue * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = opacityValue,
                        onValueChange = {
                            opacityValue = it
                            viewModel.updateOpacityRealTime(it)
                        },
                        valueRange = 0.3f..1f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary,
                            inactiveTrackColor = DarkCard
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("30%", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("100%", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToPosition,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanSubtle,
                    contentColor = CyanPrimary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Overlay Position", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About section
            Text(
                text = "ABOUT",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "FloatyMo",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyanPrimary
                    )
                    Text(
                        text = "Version 1.1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Floating character animation overlay for Android",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
