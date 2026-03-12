package com.fanyiadrien.ictu_ex.feature.auth

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
import com.fanyiadrien.ictu_ex.ui.theme.*

/**
 * Check Status Screen for ICTU-Ex
 *
 * Allows users to choose between Buyer and Seller roles before registration or login.
 * Professional design with clear role descriptions and visual hierarchy.
 */
@Composable
fun CheckStatusScreen(navController: NavController) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // App Logo/Icon
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // TODO: Replace with actual logo
                contentDescription = "ICTU-Ex Logo",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "What brings you to ICTU-Ex?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Choose your role to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Seller Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = {
                    navController.navigate(Screen.SignUp.createRoute("SELLER"))
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Seller Icon
                    Icon(
                        painter = painterResource(id = R.drawable.price_tag), // TODO: Add seller icon
                        contentDescription = "Seller",
                        modifier = Modifier.size(48.dp),
                        tint = Teal40
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seller Title
                    Text(
                        text = "I'm a Seller",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Seller Description
                    Text(
                        text = "Sell textbooks, electronics, and campus essentials to fellow students",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seller Benefits
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.check), // TODO: Add check icon
                            contentDescription = "Check",
                            modifier = Modifier.size(20.dp),
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Earn money from unused items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.check), // TODO: Add check icon
                            contentDescription = "Check",
                            modifier = Modifier.size(20.dp),
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connect with verified ICTU students",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buyer Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = {
                    navController.navigate(Screen.SignUp.createRoute("BUYER"))
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Buyer Icon
                    Icon(
                        painter = painterResource(id = R.drawable.checklist), // TODO: Add buyer icon
                        contentDescription = "Buyer",
                        modifier = Modifier.size(48.dp),
                        tint = Purple40
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buyer Title
                    Text(
                        text = "I'm a Buyer",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buyer Description
                    Text(
                        text = "Find affordable textbooks and essentials from trusted student sellers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buyer Benefits
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.check), // TODO: Add check icon
                            contentDescription = "Check",
                            modifier = Modifier.size(20.dp),
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save money on study materials",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.check), // TODO: Add check icon
                            contentDescription = "Check",
                            modifier = Modifier.size(20.dp),
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Secure transactions with QR verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Already have account link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        navController.navigate(Screen.SignIn.route)
                    }
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Purple40
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
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
