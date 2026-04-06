package com.fanyiadrien.ictu_ex.feature.auth

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private const val TAG = "ICTU_SignUp"

@Composable
fun SignUpScreen(
    navController: NavController,
    userType: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    Log.d(TAG, "SignUpScreen composed")
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()

    // ── Theme tokens ──────────────────────────────────────────────────────────
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
    val badgeBg         = if (isDark) Color(0xFF4A00A0) else Color(0xFFE9DDFF)
    val badgeText       = if (isDark) Color(0xFFEADDFF) else Color(0xFF4A00A0)
    val bottomTextColor = MaterialTheme.colorScheme.onBackground

    val googleBg     = if (isDark) Color(0xFF2B2930) else Color.White
    val googleBorder = if (isDark) Color(0xFFBB86FC) else Color.Black
    val googleText   = if (isDark) Color(0xFFEADDFF) else Color(0xFF1C1B1F)

    var email by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val userTypeDisplay  = if (userType == "SELLER") "Seller" else "Buyer"

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Curved header gradient ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
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

            // ── Back Button ─────────────────────────────────────────────
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(44.dp))

                // ── Logo badge ─────────────────────────────────────────────
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.user),
                            contentDescription = "Logo",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF6200EE)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                            text = "Create Account",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = cardContent
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Role badge
                        Surface(
                            color = badgeBg,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Registering as $userTypeDisplay",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = badgeText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

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
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = fieldIcon) },
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = studentId,
                            onValueChange = { studentId = it },
                            label = { Text("Student ID") },
                            leadingIcon = { Icon(Icons.Default.Badge, null, tint = fieldIcon) },
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            singleLine = true
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
                                .padding(vertical = 5.dp),
                            singleLine = true,
                            isError = uiState.errorMessage != null
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = fieldIcon) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
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
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = fieldIcon) },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility
                                                     else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = fieldIcon
                                    )
                                }
                            },
                            isError = passwordMismatch,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            singleLine = true
                        )

                        if (passwordMismatch) {
                            Text(
                                text = "Passwords do not match",
                                color = errorColor,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = errorColor,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.signUp(
                                    email = email,
                                    password = password,
                                    displayName = displayName,
                                    studentId = studentId,
                                    userType = userType,
                                    onSuccess = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFFBB86FC) else Color(0xFF6200EE)
                            ),
                            enabled = email.isNotEmpty() && password.isNotEmpty() &&
                                      displayName.isNotEmpty() && studentId.isNotEmpty() &&
                                      !passwordMismatch && !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Sign Up",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Footer ──────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = bottomTextColor
                    )
                    TextButton(onClick = { navController.navigate(Screen.SignIn.route) }) {
                        Text(
                            text = "Login",
                            color = if (isDark) Color(0xFFBB86FC) else Color(0xFF6200EE),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
