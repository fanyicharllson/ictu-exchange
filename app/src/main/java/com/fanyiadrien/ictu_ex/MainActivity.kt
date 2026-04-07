package com.fanyiadrien.ictu_ex

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.fanyiadrien.ictu_ex.core.biometric.BiometricHelper
import com.fanyiadrien.ictu_ex.core.navigation.NavGraph
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.core.sensors.LightSensorManager
import com.fanyiadrien.ictu_ex.core.sensors.ShakeSensorManager
import com.fanyiadrien.ictu_ex.ui.theme.IctuExTheme
import com.fanyiadrien.ictu_ex.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var lightSensorManager: LightSensorManager

    @Inject
    lateinit var shakeSensorManager: ShakeSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val sensorIsDark by lightSensorManager.isDark.collectAsState()
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.AUTO) }
            var showShakeReport by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                shakeSensorManager.onShake.collect {
                    showShakeReport = true
                }
            }

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
                            onFailure = { /* Handled by prompt */ },
                            onError = { error -> biometricError = error }
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
                        Box {
                            NavGraph(
                                navController = navController,
                                startDestination = startDestination ?: Screen.Onboarding.route,
                                auth = auth,
                                themeMode = themeMode,
                                onThemeModeChange = { themeMode = it }
                            )

                            if (showShakeReport) {
                                ShakeReportModal(
                                    onDismiss = { showShakeReport = false },
                                    onSubmit = { message ->
                                        scope.launch {
                                            submitReport(message)
                                            showShakeReport = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun submitReport(message: String) {
        val uid = auth.currentUser?.uid ?: "anonymous"
        val report = mapOf(
            "uid" to uid,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )
        
        firestore.collection("reports").add(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Report Received! 🛡️ We'll look into it.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send report. Please check connection.", Toast.LENGTH_SHORT).show()
            }
    }

    @Composable
    private fun ShakeReportModal(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
        var reportText by remember { mutableStateOf("") }
        
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Something wrong?",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "You've detected a shake! Report suspicious activity or bugs here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    OutlinedTextField(
                        value = reportText,
                        onValueChange = { reportText = it },
                        placeholder = { Text("What happened?") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { onSubmit(reportText) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = reportText.isNotBlank()
                        ) {
                            Text("Submit")
                        }
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
                    Text("Identity Verification Required", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) { Text("Retry Verification") }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ICTU-Exchange", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Secure Student Marketplace", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 32.dp), contentAlignment = Alignment.BottomCenter) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Made by", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Text("Charllson & Adrien", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensorManager.register()
        shakeSensorManager.register()
    }

    override fun onPause() {
        super.onPause()
        lightSensorManager.unregister()
        shakeSensorManager.unregister()
    }
}