package com.fanyiadrien.ictu_ex.feature.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fanyiadrien.ictu_ex.ui.theme.*

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state   = viewModel.uiState
    val context = LocalContext.current
    val isDark  = isSystemInDarkTheme()

    // Sync displayName into rememberSaveable so it survives rotation
    var displayName by rememberSaveable(state.displayName) { mutableStateOf(state.displayName) }

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.onImagePicked(it) } }

    // Show toast on success / error, then clear
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
            navController.popBackStack()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearFeedback()
        }
    }

    val headerGradient = Brush.verticalGradient(
        colors = if (isDark) listOf(Purple20, Purple30) else listOf(Purple30, Purple40)
    )
    val buttonGradient = Brush.horizontalGradient(
        colors = if (isDark) listOf(Purple30, Purple40) else listOf(Purple40, Purple30)
    )

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Gradient header ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(headerGradient)
            ) {
                // Back button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint               = Color.White,
                        modifier           = Modifier.size(18.dp)
                    )
                }

                // Title
                Text(
                    text     = "Edit Profile",
                    style    = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    ),
                    modifier = Modifier
                        .statusBarsPadding()
                        .align(Alignment.TopCenter)
                        .padding(top = 18.dp)
                )

                // Avatar (overlaps header bottom)
                AvatarPicker(
                    avatarUrl       = state.pendingImageUri?.toString() ?: state.avatarUrl,
                    displayName     = displayName,
                    isLoading       = state.isLoading,
                    modifier        = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 52.dp),
                    onPickImage     = { imagePicker.launch("image/*") }
                )
            }

            Spacer(modifier = Modifier.height(64.dp))   // space for avatar overlap

            // ── Form card ─────────────────────────────────────────────────
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                ProfileForm(
                    displayName     = displayName,
                    email           = state.email,
                    onNameChange    = { displayName = it; viewModel.onDisplayNameChange(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Update button ─────────────────────────────────────────────
            UpdateButton(
                isSaving  = state.isSaving,
                gradient  = buttonGradient,
                onClick   = { viewModel.updateProfile(context) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Avatar with camera badge ──────────────────────────────────────────────────
@Composable
private fun AvatarPicker(
    avatarUrl: String,
    displayName: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onPickImage: () -> Unit
) {
    // Subtle scale animation when avatar changes
    val scale by animateFloatAsState(
        targetValue    = 1f,
        animationSpec  = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label          = "avatarScale"
    )

    Box(
        modifier           = modifier.size(104.dp),
        contentAlignment   = Alignment.BottomEnd
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .clip(CircleShape)
                .border(3.dp, Color.White, CircleShape)
                .background(Brush.linearGradient(listOf(Purple30, Purple80)))
                .clickable(onClick = onPickImage),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(28.dp),
                    color       = Color.White,
                    strokeWidth = 2.dp
                )
            } else if (avatarUrl.isNotBlank()) {
                AsyncImage(
                    model            = avatarUrl,
                    contentDescription = "Avatar",
                    modifier         = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale     = ContentScale.Crop
                )
            } else {
                Text(
                    text  = displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                )
            }
        }

        // Camera badge
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, Color.White, CircleShape)
                .clickable(onClick = onPickImage),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Rounded.CameraAlt,
                contentDescription = "Change photo",
                tint               = Color.White,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

// ── Form fields ───────────────────────────────────────────────────────────────
@Composable
private fun ProfileForm(
    displayName: String,
    email: String,
    onNameChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val fieldShape   = RoundedCornerShape(14.dp)
    val borderColor  = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val fieldColors  = OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = borderColor,
        focusedLabelColor    = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(
        modifier            = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Display name
        OutlinedTextField(
            value         = displayName,
            onValueChange = onNameChange,
            label         = { Text("Display Name") },
            leadingIcon   = {
                Icon(Icons.Rounded.Person, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
            },
            singleLine    = true,
            shape         = fieldShape,
            colors        = fieldColors,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction      = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier      = Modifier.fillMaxWidth()
        )

        // Email (read-only — Firebase Auth email cannot be changed here)
        OutlinedTextField(
            value         = email,
            onValueChange = {},
            label         = { Text("Email") },
            leadingIcon   = {
                Icon(Icons.Rounded.Email, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            readOnly      = true,
            singleLine    = true,
            shape         = fieldShape,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = borderColor,
                unfocusedBorderColor = borderColor,
                disabledBorderColor  = borderColor,
                focusedLabelColor    = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier      = Modifier.fillMaxWidth()
        )
    }
}

// ── Update button ─────────────────────────────────────────────────────────────
@Composable
private fun UpdateButton(
    isSaving: Boolean,
    gradient: Brush,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "btnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(54.dp)
            .scale(scale)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(28.dp),
                ambientColor = Purple40.copy(alpha = 0.3f),
                spotColor    = Purple40.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(28.dp))
            .background(if (isSaving) gradient.let { gradient } else gradient)
            .clickable(
                interactionSource = interactionSource,
                indication        = ripple(color = Color.White.copy(alpha = 0.3f)),
                enabled           = !isSaving,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier    = Modifier.size(24.dp),
                color       = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text  = "Update",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
            )
        }
    }
}
