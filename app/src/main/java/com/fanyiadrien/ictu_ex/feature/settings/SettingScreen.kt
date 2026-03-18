package com.fanyiadrien.ictu_ex.feature.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.core.ui.components.IctuBottomNav
import com.fanyiadrien.ictu_ex.ui.theme.*

@Composable
fun SettingScreen(
    navController: NavController,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    viewModel: SettingViewModel = hiltViewModel()
) {
    val state   = viewModel.uiState
    val context = LocalContext.current
    val isDark  = isSystemInDarkTheme()

    // Sync external themeMode into ViewModel on first composition
    LaunchedEffect(themeMode) { viewModel.setThemeMode(themeMode) }

    // Compute cache size once on entry
    LaunchedEffect(Unit) { viewModel.computeCacheSize(context) }

    // Delete-account confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSwitchRoleDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        containerColor   = MaterialTheme.colorScheme.background,
        snackbarHost     = { SnackbarHost(snackbarHostState) },
        topBar           = { SettingsTopBar(onBack = { navController.popBackStack() }) },
        bottomBar        = { IctuBottomNav(navController = navController, isSeller = state.currentUserType == "SELLER") }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── 0. Role switcher ─────────────────────────────────────────
            if (state.currentUserType.isNotBlank()) {
                RoleSwitcherCard(
                    currentType = state.currentUserType,
                    isLoading   = state.isSwitchingRole,
                    isDark      = isDark,
                    onSwitch    = { showSwitchRoleDialog = true }
                )
            }

            // ── 1. Appearance ─────────────────────────────────────────────
            SettingsSection(title = "Appearance") {
                ThemeModeSelector(
                    current  = state.themeMode,
                    isDark   = isDark,
                    onChange = {
                        viewModel.setThemeMode(it)
                        onThemeModeChange(it)   // propagate up to MainActivity
                    }
                )
            }

            // ── 2. Sensors & Hardware ─────────────────────────────────────
            SettingsSection(title = "Sensors & Hardware") {
                ToggleRow(
                    icon     = Icons.Rounded.Vibration,
                    title    = "Shake to Report",
                    subtitle = "Shake device to flag a suspicious listing",
                    checked  = state.shakeToReportEnabled,
                    isDark   = isDark,
                    onChange = viewModel::toggleShakeToReport
                )
                SettingsDivider()
                ToggleRow(
                    icon     = Icons.Rounded.LightMode,
                    title    = "Light Sensor (Auto Dark Mode)",
                    subtitle = "Switches theme based on ambient light",
                    checked  = state.lightSensorEnabled,
                    isDark   = isDark,
                    onChange = viewModel::toggleLightSensor
                )
                if (state.lightSensorEnabled) {
                    SliderRow(
                        label    = "Dark mode threshold: ${state.lightSensorThreshold.toInt()} lux",
                        value    = state.lightSensorThreshold,
                        range    = 5f..50f,
                        isDark   = isDark,
                        onChange = viewModel::setLightSensorThreshold
                    )
                }
            }

            // ── 3. Security ───────────────────────────────────────────────
            SettingsSection(title = "Security") {
                ToggleRow(
                    icon     = Icons.Rounded.Fingerprint,
                    title    = "Biometric Lock",
                    subtitle = "Require fingerprint on app launch",
                    checked  = state.biometricLockEnabled,
                    isDark   = isDark,
                    onChange = viewModel::toggleBiometricLock
                )
            }

            // ── 4. Notifications ──────────────────────────────────────────
            SettingsSection(title = "Notifications") {
                ToggleRow(
                    icon     = Icons.Rounded.NotificationsActive,
                    title    = "New Listing Alerts",
                    subtitle = "Get notified when new items are posted",
                    checked  = state.newListingAlertsEnabled,
                    isDark   = isDark,
                    onChange = viewModel::toggleNewListingAlerts
                )
                SettingsDivider()
                ToggleRow(
                    icon     = Icons.Rounded.Chat,
                    title    = "Chat Notifications",
                    subtitle = "Messages from buyers and sellers",
                    checked  = state.chatNotificationsEnabled,
                    isDark   = isDark,
                    onChange = viewModel::toggleChatNotifications
                )
            }

            // ── 5. Storage ────────────────────────────────────────────────
            SettingsSection(title = "Storage") {
                ActionRow(
                    icon     = Icons.Rounded.CleaningServices,
                    title    = "Clear Cache",
                    subtitle = "Currently using ${state.cacheSize}",
                    isDark   = isDark,
                    isLoading = state.isClearingCache,
                    onClick  = { viewModel.clearCache(context) }
                )
            }

            // ── 6. Account ────────────────────────────────────────────────
            SettingsSection(title = "Account") {
                ActionRow(
                    icon      = Icons.Rounded.DeleteForever,
                    title     = "Delete Account",
                    subtitle  = "Permanently remove your account and data",
                    isDark    = isDark,
                    isLoading = state.isDeletingAccount,
                    tint      = MaterialTheme.colorScheme.error,
                    onClick   = { showDeleteDialog = true }
                )
            }

            // ── 7. App info ───────────────────────────────────────────────
            AppInfoFooter(version = state.appVersion)

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Delete account confirmation ────────────────────────────────────────
    if (showDeleteDialog) {
        DeleteAccountDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteAccount {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // ── Switch role confirmation ───────────────────────────────────────────
    if (showSwitchRoleDialog) {
        val targetType = if (state.currentUserType == "SELLER") "BUYER" else "SELLER"
        SwitchRoleDialog(
            currentType = state.currentUserType,
            targetType  = targetType,
            onConfirm   = {
                showSwitchRoleDialog = false
                viewModel.switchUserType()
            },
            onDismiss   = { showSwitchRoleDialog = false }
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ── Section wrapper ───────────────────────────────────────────────────────────
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            text     = title.uppercase(),
            style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

// ── Theme mode selector (3-way chip row) ──────────────────────────────────────
@Composable
private fun ThemeModeSelector(
    current: ThemeMode,
    isDark: Boolean,
    onChange: (ThemeMode) -> Unit
) {
    val iconBg = if (isDark) Purple30.copy(alpha = 0.4f) else Purple90

    Row(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment   = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Palette,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Theme",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Auto follows the light sensor",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Chip row
    Row(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp, end = 16.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeMode.entries.forEach { mode ->
            val selected = current == mode
            val label    = when (mode) {
                ThemeMode.AUTO  -> "Auto"
                ThemeMode.LIGHT -> "Light"
                ThemeMode.DARK  -> "Dark"
            }
            val icon = when (mode) {
                ThemeMode.AUTO  -> Icons.Rounded.BrightnessAuto
                ThemeMode.LIGHT -> Icons.Rounded.LightMode
                ThemeMode.DARK  -> Icons.Rounded.DarkMode
            }
            FilterChip(
                selected = selected,
                onClick  = { onChange(mode) },
                label    = { Text(label, style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
        }
    }
}

// ── Toggle row ────────────────────────────────────────────────────────────────
@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    isDark: Boolean,
    onChange: (Boolean) -> Unit
) {
    val iconBg = if (isDark) Purple30.copy(alpha = 0.4f) else Purple90

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked         = checked,
            onCheckedChange = onChange,
            colors          = SwitchDefaults.colors(
                checkedThumbColor       = Color.White,
                checkedTrackColor       = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor     = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor     = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// ── Slider row (lux threshold) ────────────────────────────────────────────────
@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    isDark: Boolean,
    onChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value         = value,
            onValueChange = onChange,
            valueRange    = range,
            colors        = SliderDefaults.colors(
                thumbColor       = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

// ── Action row (cache clear, delete account) ──────────────────────────────────
@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDark: Boolean,
    isLoading: Boolean,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val resolvedTint = if (tint == Color.Unspecified) MaterialTheme.colorScheme.primary else tint
    val iconBg = if (tint == Color.Unspecified) {
        if (isDark) Purple30.copy(alpha = 0.4f) else Purple90
    } else {
        resolvedTint.copy(alpha = 0.12f)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "actionRowScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .then(
                if (!isLoading) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication        = ripple(color = resolvedTint)
                ) { onClick() } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    color       = resolvedTint,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    icon,
                    contentDescription = null,
                    tint     = resolvedTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = if (tint == Color.Unspecified) MaterialTheme.colorScheme.onSurface else resolvedTint
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ── Thin divider between rows ─────────────────────────────────────────────────
@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 20.dp),
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.8.dp
    )
}

// ── App info footer ───────────────────────────────────────────────────────────
@Composable
private fun AppInfoFooter(version: String) {
    val gradient = Brush.horizontalGradient(listOf(Purple40, Purple80))
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.School,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            "ICTU-Ex",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Version $version · The ICT University, Cameroon",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Role switcher card ────────────────────────────────────────────────────────
@Composable
private fun RoleSwitcherCard(
    currentType: String,
    isLoading: Boolean,
    isDark: Boolean,
    onSwitch: () -> Unit
) {
    val isSeller       = currentType == "SELLER"
    val targetLabel    = if (isSeller) "Switch to Buyer" else "Switch to Seller"
    val currentLabel   = if (isSeller) "Seller" else "Buyer"
    val targetSubLabel = if (isSeller) "Browse & buy items" else "Post & sell items"

    // Animated background gradient that flips direction based on role
    val gradientColors = if (isSeller)
        listOf(Purple40, Purple80)
    else
        listOf(Teal40, Purple40)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "roleSwitcherScale"
    )

    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header label
            Text(
                text  = "ACCOUNT ROLE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            // Current role pill + arrow + target role pill
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active role badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(gradientColors))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSeller) Icons.Rounded.Storefront
                                          else Icons.Rounded.ShoppingCart,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text  = currentLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }

                Icon(
                    Icons.Rounded.SwapHoriz,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                // Target role (greyed out)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDark) Purple30.copy(alpha = 0.3f) else Purple90
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSeller) Icons.Rounded.ShoppingCart
                                          else Icons.Rounded.Storefront,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text  = if (isSeller) "Buyer" else "Seller",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text  = targetSubLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            // Switch button
            Button(
                onClick  = onSwitch,
                enabled  = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .scale(scale),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    contentColor           = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor   = Color.White.copy(alpha = 0.6f)
                ),
                interactionSource = interactionSource
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Rounded.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        targetLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

// ── Switch role dialog ────────────────────────────────────────────────────────
@Composable
private fun SwitchRoleDialog(
    currentType: String,
    targetType: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Rounded.SwapHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Switch to $targetType?", fontWeight = FontWeight.Bold) },
        text  = {
            Text(
                "You are currently a $currentType. Switching to $targetType will " +
                if (targetType == "SELLER")
                    "allow you to post and manage listings on campus."
                else
                    "hide your listings and switch you to browsing mode.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Switch", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ── Delete account dialog ─────────────────────────────────────────────────────
@Composable
private fun DeleteAccountDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
        text  = {
            Text(
                "This will permanently delete your account, all your listings, and your data. " +
                "This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Delete", color = Color.White) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
