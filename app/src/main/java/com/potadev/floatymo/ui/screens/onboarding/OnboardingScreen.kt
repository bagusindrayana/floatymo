package com.potadev.floatymo.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.potadev.floatymo.ui.theme.CyanPrimary
import com.potadev.floatymo.ui.theme.CyanSubtle
import com.potadev.floatymo.ui.theme.DarkBg
import com.potadev.floatymo.ui.theme.DarkCard
import com.potadev.floatymo.ui.theme.TextMuted
import com.potadev.floatymo.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val gradientColors: List<Color>
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Selamat Datang di FloatyMo",
        description = "Tampilkan animasi GIF yang menarik sebagai overlay mengambang di atas aplikasi lain. Jadikan layar Anda lebih hidup dan dinamis!",
        icon = Icons.Default.Favorite,
        gradientColors = listOf(Color(0xFF00BCD4), Color(0xFF2196F3))
    ),
    OnboardingPage(
        title = "Pilih GIF Favorit Anda",
        description = "Cari GIF dari Giphy, pilih dari koleksi bundled, atau import dari galeri perangkat Anda. Ribuan pilihan menunggu untuk dieksplorasi!",
        icon = Icons.Default.Search,
        gradientColors = listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
    ),
    OnboardingPage(
        title = "Kustomisasi Sesuka Hati",
        description = "Atur ukuran, transparansi, dan posisi overlay dengan mudah. Drag untuk memindahkan, sesuaikan untuk tampilan yang sempurna!",
        icon = Icons.Default.Settings,
        gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFF5722))
    )
)

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val viewModel: OnboardingViewModel = remember {
        OnboardingViewModel.Factory(
            com.potadev.floatymo.AppContainer.provideSettingsRepository()
        ).create(OnboardingViewModel::class.java)
    }

    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onOnboardingComplete()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.nextPage()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Skip button
        IconButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Skip",
                tint = TextSecondary
            )
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo/Brand
            PremiumLogo()

            Spacer(modifier = Modifier.height(32.dp))

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = onboardingPages[page],
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Page indicators
            PageIndicators(
                currentPage = pagerState.currentPage,
                totalPages = onboardingPages.size,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CyanPrimary
                        ),
                        modifier = Modifier
                            .width(120.dp)
                            .height(48.dp)
                    ) {
                        Text("Kembali", fontWeight = FontWeight.Medium)
                    }
                } else {
                    Spacer(modifier = Modifier.width(120.dp))
                }

                // Next/Get Started button
                Button(
                    onClick = {
                        if (pagerState.currentPage < onboardingPages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.completeOnboarding()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = DarkBg
                    ),
                    modifier = Modifier
                        .width(160.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        if (pagerState.currentPage < onboardingPages.size - 1) "Lanjut" else "Mulai",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        if (pagerState.currentPage < onboardingPages.size - 1) Icons.Default.ArrowForward else Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PremiumLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(CyanPrimary, Color(0xFF2196F3))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = "FloatyMo Logo",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with gradient background
        Box(
            modifier = Modifier
                .size(180.dp)
                .graphicsLayer { translationY = offsetY }
                .clip(RoundedCornerShape(40.dp))
                .background(
                    Brush.linearGradient(
                        colors = page.gradientColors
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                page.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun PageIndicators(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            val width = if (isSelected) 32.dp else 8.dp
            val alpha = if (isSelected) 1f else 0.4f

            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        (if (isSelected) CyanPrimary else TextSecondary).copy(alpha = alpha)
                    )
            )

            if (index < totalPages - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    colors: ButtonColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(24.dp)
    ) {
        content()
    }
}

// Type alias for ButtonColors
typealias ButtonColors = androidx.compose.material3.ButtonColors
