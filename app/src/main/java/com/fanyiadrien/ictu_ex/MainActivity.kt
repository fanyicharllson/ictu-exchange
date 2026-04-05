package com.fanyiadrien.ictu_ex

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.fanyiadrien.ictu_ex.core.biometric.BiometricHelper
import com.fanyiadrien.ictu_ex.core.navigation.NavGraph
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.core.sensors.LightSensorManager
import com.fanyiadrien.ictu_ex.ui.theme.IctuExTheme
import com.fanyiadrien.ictu_ex.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var lightSensorManager: LightSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Explicitly call the companion function
        installSplashScreen()
        
        super.onCreate(savedInstanceState)

        setContent {
            val sensorIsDark by lightSensorManager.isDark.collectAsState()
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.AUTO) }

            val isDark = when (themeMode) {
                ThemeMode.AUTO -> sensorIsDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            var startDestination by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var biometricError by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                delay(1000) 
                
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    if (BiometricHelper.isAvailable(this@MainActivity)) {
                        BiometricHelper.authenticate(
                            activity = this@MainActivity,
                            onSuccess = {
                                startDestination = Screen.Home.route
                                isLoading = false
                            },
                            onFailure = { /* Handled by Biometric prompt */ },
                            onError = { error ->
                                biometricError = error
                            }
                        )
                    } else {
                        startDestination = Screen.Home.route
                        isLoading = false
                    }
                } else {
                    startDestination = Screen.Onboarding.route
                    isLoading = false
                }
            }

            IctuExTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoading) {
                        AppSplashScreen(errorMessage = biometricError) {
                            biometricError = null
                            recreate() 
                        }
                    } else {
                        val navController = rememberNavController()
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination ?: Screen.Onboarding.route,
                            auth = auth,
                            themeMode = themeMode,
                            onThemeModeChange = { themeMode = it }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AppSplashScreen(errorMessage: String? = null, onRetry: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_exchange_logo),
                    contentDescription = "ICTU-Exchange Logo",
                    modifier = Modifier.size(120.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                if (errorMessage != null) {
                    Text(
                        text = "Identity Verification Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRetry,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("Retry Verification")
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ICTU-Exchange",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Secure Student Marketplace",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Footer branding
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Made by",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Charllson & Adrien",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensorManager.register()
    }

    override fun onPause() {
        super.onPause()
        lightSensorManager.unregister()
    }
}