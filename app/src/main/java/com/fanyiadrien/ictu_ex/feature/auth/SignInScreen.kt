package com.fanyiadrien.ictu_ex.feature.auth

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fanyiadrien.ictu_ex.R
import com.fanyiadrien.ictu_ex.core.navigation.Screen

private const val TAG = "ICTU_SignIn"

@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    Log.d(TAG, "SignInScreen composed")
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()

    // ── Theme tokens ──────────────────────────────────────────────────────────
    // Light: white card, purple accents, black text, black borders
    // Dark : dark-surface card, soft purple accents, white text, white borders
    val cardBg          = if (isDark) Color(0xFF2B2930) else Color.White
    val cardContent     = if (isDark) Color(0xFFEADDFF) else Color(0xFF1C1B1F)
    val fieldText       = if (isDark) Color(0xFFEADDFF) else Color(0xFF1C1B1F)
    val fieldBorder     = if (isDark) Color(0xFFBB86FC) else Color.Black
    val fieldBorderDim  = if (isDark) Color(0xFF625B71) else Color(0xFF79747E)
    val fieldLabel      = if (isDark) Color(0xFFCCC2DC) else Color(0xFF49454F)
    val fieldContainer  = if (isDark) Color(0xFF1C1B1F) else Color(0xFFF6F0FF)
    val fieldIcon       = if (isDark) Color(0xFFBB86FC) else Color(0xFF6200EE)
    val errorColor      = if (isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)
    val dividerColor    = if (isDark) Color(0xFF49454F) else Color(0xFFCAC4D0)
    val subtitleColor   = if (isDark) Color(0xFFCCC2DC) else Color(0xFF49454F)
    val bottomTextColor = MaterialTheme.colorScheme.onBackground

    // Google button: white bg + black border in light; dark surface + purple border in dark
    val googleBg        = if (isDark) Color(0xFF2B2930) else Color.White
    val googleBorder    = if (isDark) Color(0xFFBB86FC) else Color.Black
    val googleText      = if (isDark) Color(0xFFEADDFF) else Color(0xFF1C1B1F)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Curved header gradient ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.42f)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDark)
                                listOf(Color(0xFF4A00A0), Color(0xFF2B2930))
                            else
                                listOf(Color(0xFF6200EE), Color(0xFF9C4DCC))
                        ),
                        shape = RoundedCornerShape(bottomStart = 56.dp, bottomEnd = 56.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(56.dp))

                // ── Logo badge ─────────────────────────────────────────────
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.user),
                            contentDescription = "Logo",
                            modifier = Modifier.size(56.dp),
                            tint = Color(0xFF6200EE)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Auth card ──────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 8.dp else 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome Back",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = cardContent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sign in to your ICTU-Ex account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = subtitleColor
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor        = fieldText,
                            unfocusedTextColor      = fieldText,
                            focusedBorderColor      = fieldBorder,
                            unfocusedBorderColor    = fieldBorderDim,
                            focusedLabelColor       = fieldBorder,
                            unfocusedLabelColor     = fieldLabel,
                            cursorColor             = fieldBorder,
                            focusedContainerColor   = fieldContainer,
                            unfocusedContainerColor = fieldContainer,
                            errorBorderColor        = errorColor,
                            errorLabelColor         = errorColor,
                            errorLeadingIconColor   = errorColor
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; viewModel.clearError() },
                            label = { Text("ICTU Email") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = fieldIcon) },
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            singleLine = true,
                            isError = uiState.errorMessage != null
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; viewModel.clearError() },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = fieldIcon) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility
                                                      else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = fieldIcon
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            singleLine = true
                        )

                        uiState.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = error,
                                color = errorColor,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.signIn(email, password) {
                                    navController.navigate(Screen.Home.route) { popUpTo(0) }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor   = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                disabledContentColor   = Color.White.copy(alpha = 0.6f)
                            ),
                            enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading
                        ) {
                            if (uiState.isLoading)
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            else
                                Text(
                                    "Sign In",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Divider ────────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = dividerColor)
                    Text(
                        text = "  or  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = dividerColor)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Google button ──────────────────────────────────────────
                OutlinedButton(
                    onClick = { /* TODO: Google Sign-In */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = googleBg,
                        contentColor   = googleText
                    ),
                    border = BorderStroke(1.5.dp, googleBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.google_logo),
                            contentDescription = "Google",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = googleText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Bottom link ────────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Don't have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = bottomTextColor.copy(alpha = 0.7f)
                    )
                    TextButton(onClick = { navController.navigate(Screen.CheckStatus.route) }) {
                        Text(
                            "Sign Up",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
