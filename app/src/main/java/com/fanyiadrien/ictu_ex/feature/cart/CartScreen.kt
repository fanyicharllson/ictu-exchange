package com.fanyiadrien.ictu_ex.feature.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fanyiadrien.ictu_ex.ui.theme.*

@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    // Navigate back to Home after successful checkout
    LaunchedEffect(state.checkoutOrderId) {
        if (state.checkoutOrderId != null) {
            navController.navigate(com.fanyiadrien.ictu_ex.core.navigation.Screen.Home.route) {
                popUpTo(com.fanyiadrien.ictu_ex.core.navigation.Screen.Home.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar         = {
            CartTopBar(
                itemCount = state.itemCount,
                onClose   = { navController.popBackStack() },
                onShare   = { /* TODO: share cart */ }
            )
        },
        bottomBar = {
            CartCheckoutBar(
                total       = state.total,
                itemCount   = state.items.size,
                isLoading   = state.isCheckingOut,
                onClick     = { viewModel.checkout() }
            )
        }
    ) { padding ->

        if (state.items.isEmpty()) {
            EmptyCartState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier       = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                // ── Cart items ────────────────────────────────────────────
                itemsIndexed(
                    items = state.items,
                    key   = { _, item -> item.id }
                ) { index, item ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 6 }
                    ) {
                        CartItemCard(
                            item        = item,
                            onIncrement = { viewModel.increment(item.id) },
                            onDecrement = { viewModel.decrement(item.id) },
                            onRemove    = { viewModel.removeItem(item.id) }
                        )
                    }

                    if (index < state.items.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(horizontal = 20.dp),
                            thickness = 0.8.dp,
                            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // ── Promo code ────────────────────────────────────────────
                item {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 20.dp),
                        thickness = 0.8.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    PromoCodeSection(
                        code         = state.promoCode,
                        applied      = state.promoApplied,
                        onCodeChange = viewModel::onPromoCodeChange,
                        onApply      = viewModel::applyPromoCode
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 20.dp),
                        thickness = 0.8.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                // ── Price breakdown ───────────────────────────────────────
                item {
                    PriceBreakdown(
                        subtotal        = state.subtotal,
                        discount        = state.discount,
                        total           = state.total,
                        discountPercent = state.discountPercent
                    )
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartTopBar(
    itemCount: Int,
    onClose: () -> Unit,
    onShare: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text      = "$itemCount ITEMS",
                style     = MaterialTheme.typography.titleMedium.copy(
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color     = MaterialTheme.colorScheme.onBackground,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close cart",
                    tint               = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = {
            IconButton(onClick = onShare) {
                Icon(
                    Icons.Rounded.IosShare,
                    contentDescription = "Share cart",
                    tint               = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ── Promo code section ────────────────────────────────────────────────────────
@Composable
private fun PromoCodeSection(
    code: String,
    applied: Boolean,
    onCodeChange: (String) -> Unit,
    onApply: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.LocalOffer,
            contentDescription = null,
            tint               = if (applied) MaterialTheme.colorScheme.tertiary
                                 else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        OutlinedTextField(
            value         = code,
            onValueChange = onCodeChange,
            placeholder   = {
                Text(
                    "Promo code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine    = true,
            modifier      = Modifier.weight(1f),
            shape         = RoundedCornerShape(10.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            textStyle     = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction      = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onApply()
            }),
            trailingIcon  = if (applied) ({
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = "Promo applied",
                    tint               = MaterialTheme.colorScheme.tertiary,
                    modifier           = Modifier.size(18.dp)
                )
            }) else null
        )

        Spacer(Modifier.width(10.dp))

        TextButton(
            onClick  = {
                focusManager.clearFocus()
                onApply()
            },
            modifier = Modifier.height(48.dp),
            colors   = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                "Apply",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

// ── Price breakdown ───────────────────────────────────────────────────────────
@Composable
private fun PriceBreakdown(
    subtotal: Double,
    discount: Double,
    total: Double,
    discountPercent: Int
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Subtotal row
        PriceRow(
            label = "Subtotal",
            value = formatXaf(subtotal),
            bold  = false
        )

        // Discount row — only shown when a promo is active
        if (discountPercent > 0) {
            PriceRow(
                label      = "Discount ($discountPercent%)",
                value      = "− ${formatXaf(discount)}",
                bold       = false,
                valueColor = MaterialTheme.colorScheme.tertiary
            )
        }

        HorizontalDivider(
            thickness = 0.8.dp,
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Total row
        PriceRow(
            label = "Total",
            value = formatXaf(total),
            bold  = true
        )
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    bold: Boolean,
    valueColor: Color = Color.Unspecified
) {
    val resolvedColor = if (valueColor == Color.Unspecified)
        MaterialTheme.colorScheme.onBackground else valueColor

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = if (bold)
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            else
                MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text  = value,
            style = if (bold)
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 18.sp
                )
            else
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (bold) MaterialTheme.colorScheme.primary else resolvedColor
        )
    }
}

// ── Checkout sticky bottom bar ────────────────────────────────────────────────
@Composable
private fun CartCheckoutBar(
    total: Double,
    itemCount: Int,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(listOf(Purple30, Purple40))

    Surface(
        shadowElevation = 12.dp,
        color           = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Price summary
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "$itemCount item${if (itemCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = formatXaf(total),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Checkout button
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(gradient)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = ripple(color = Color.White.copy(alpha = 0.3f)),
                        enabled           = !isLoading,
                        onClick           = onClick
                    )
                    .padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text  = "Checkout",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
                            ),
                            color = Color.White
                        )
                        Icon(
                            Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────
@Composable
private fun EmptyCartState(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.ShoppingCartCheckout,
            contentDescription = null,
            modifier           = Modifier.size(80.dp),
            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "Your cart is empty",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Browse listings and add items to get started.",
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}
