package com.fanyiadrien.ictu_ex.feature.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanyiadrien.ictu_ex.R
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.ui.theme.IctuExTheme

private const val TAG = "ICTU_CheckStatus"

@Composable
fun CheckStatusScreen(navController: NavController) {
    Log.d(TAG, "CheckStatusScreen composed")

    // ── Fix: explicit background ────────────────────────────────────────────
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "ICTU-Ex Logo",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "What brings you to ICTU-Ex?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose your role to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Seller Card ─────────────────────────────────────────────────
            RoleCard(
                title = "I'm a Seller",
                description = "Sell textbooks, electronics, and campus essentials to fellow students",
                benefits = listOf(
                    "Earn money from unused items",
                    "Connect with verified ICTU students"
                ),
                iconRes = R.drawable.ic_launcher_foreground,
                iconTint = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    Log.d(TAG, "Seller card clicked → navigating to ${Screen.SignUp.createRoute("SELLER")}")
                    try {
                        navController.navigate(Screen.SignUp.createRoute("SELLER"))
                        Log.d(TAG, "navigate(SELLER) called successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "CRASH navigating to SignUp(SELLER)", e)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Buyer Card ──────────────────────────────────────────────────
            RoleCard(
                title = "I'm a Buyer",
                description = "Find affordable textbooks and essentials from trusted student sellers",
                benefits = listOf(
                    "Save money on study materials",
                    "Secure transactions with QR verification"
                ),
                iconRes = R.drawable.ic_launcher_foreground,
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = {
                    Log.d(TAG, "Buyer card clicked → navigating to ${Screen.SignUp.createRoute("BUYER")}")
                    try {
                        navController.navigate(Screen.SignUp.createRoute("BUYER"))
                        Log.d(TAG, "navigate(BUYER) called successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "CRASH navigating to SignUp(BUYER)", e)
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {
                    Log.d(TAG, "Sign In link clicked → navigating to ${Screen.SignIn.route}")
                    try {
                        navController.navigate(Screen.SignIn.route)
                        Log.d(TAG, "navigate(SignIn) called successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "CRASH navigating to SignIn", e)
                    }
                }) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ── Reusable Role Card ──────────────────────────────────────────────────────
@Composable
private fun RoleCard(
    title: String,
    description: String,
    benefits: List<String>,
    iconRes: Int,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = iconTint
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckStatusScreenPreview() {
    IctuExTheme {
        CheckStatusScreen(navController = rememberNavController())
    }
}