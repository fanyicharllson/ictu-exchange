package com.fanyiadrien.ictu_ex.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanyiadrien.ictu_ex.R
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = viewModel()
) {
    // ── Entrance animation triggers ──────────────────────────────────────────
    var imageVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        imageVisible = true          // image fades/scales in immediately
        delay(300)
        contentVisible = true        // text & button slide up after 300ms
    }

    // Animated scale for the hero image (1f → full size)
    val imageScale by animateFloatAsState(
        targetValue = if (imageVisible) 1f else 0.75f,
        animationSpec = tween(durationMillis = 600),
        label = "imageScale"
    )

    // Animated alpha for the hero image
    val imageAlpha by animateFloatAsState(
        targetValue = if (imageVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "imageAlpha"
    )

    // ── Layout ───────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralWhite)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── TOP: Hero image area ─────────────────────────────────────
                OnboardingHeroImage(
                    scale = imageScale,
                    alpha = imageAlpha,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // ── BOTTOM CONTENT ───────────────────────────────────────────
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        animationSpec = tween(500),
                        initialOffsetY = { it / 3 }
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title
                        Text(
                            text = "Time Journey\nWith Nike Shoes",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = Purple10,
                                textAlign = TextAlign.Center
                            )
                        )

                        // Subtitle
                        Text(
                            text = "Every smart Nike Air best shoes will highlight beauty anywhere",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = NeutralGrey,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Page indicator dots
                        PageIndicator(currentPage = viewModel.currentPage)

                        Spacer(modifier = Modifier.height(8.dp))

                        // CTA Button
                        GetStartedButton(
                            onClick = {
                                // Navigate to CheckStatus and clear back stack
                                navController.navigate(Screen.CheckStatus.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingHeroImage(
    scale: Float,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Soft gradient backdrop — mimics the off-white/light grey card in the design
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(NeutralLight, NeutralWhite)
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        )

        // Shoe image with scale + fade entrance
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // ← replace with your asset
            contentDescription = "Nike Air sneaker",
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .aspectRatio(1f)
                .scale(scale)
                .alpha(alpha)
        )
    }
}

@Composable
fun PageIndicator(
    currentPage: Int,
    totalPages: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (isActive) 24.dp else 8.dp)
                    .background(
                        color = if (isActive) Purple40 else PurpleGrey80,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
fun GetStartedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple40,
            contentColor = NeutralWhite
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = "Get Started",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = NeutralWhite
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    IctuExTheme {
        OnboardingScreen(navController = rememberNavController())
    }
}