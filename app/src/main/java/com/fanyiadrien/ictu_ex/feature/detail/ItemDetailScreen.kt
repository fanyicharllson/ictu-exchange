package com.fanyiadrien.ictu_ex.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fanyiadrien.ictu_ex.R

@Composable
fun ItemDetailScreen(
    navController: NavController,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    Box(modifier = Modifier.fillMaxSize()) {

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage,
                    onBack  = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.listing != null -> {
                DetailContent(
                    uiState     = uiState,
                    onBack      = { navController.popBackStack() },
                    onAddToCart = viewModel::addToCart
                )
            }
        }
    }
}

// ── Main content ──────────────────────────────────────────────────────────────
@Composable
private fun DetailContent(
    uiState: ItemDetailUiState,
    onBack: () -> Unit,
    onAddToCart: () -> Unit
) {
    val listing = uiState.listing!!
    var isSaved by remember { mutableStateOf(false) }
    var showContactSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // ── Hero image with back + save buttons overlaid ──────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            AsyncImage(
                model              = listing.imageUrl,
                contentDescription = listing.title,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop,
                error              = painterResource(R.drawable.ic_launcher_foreground),
                placeholder        = painterResource(R.drawable.ic_launcher_foreground)
            )

            // Gradient overlay — bottom fade so text below reads cleanly
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    )
            )

            // Back button
            IconButton(
                onClick  = onBack,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            ) {
                Icon(
                    Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Save / wishlist button
            IconButton(
                onClick  = { isSaved = !isSaved },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Rounded.Favorite
                    else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) Color(0xFFE53935)
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            // Category badge bottom-left on image
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text     = listing.category,
                    style    = MaterialTheme.typography.labelMedium,
                    color    = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // ── Main detail card ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Title + price row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Text(
                    text       = listing.title,
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground,
                    modifier   = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = "XAF ${listing.price.toInt()}",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text  = "negotiable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Rating row ────────────────────────────────────────────────
            RatingRow()

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // ── Seller card ───────────────────────────────────────────────
            uiState.seller?.let { seller ->
                SellerCard(seller = seller)
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            // ── Description ───────────────────────────────────────────────
            if (listing.description.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text       = "About this item",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text  = listing.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            // ── Item details chips ────────────────────────────────────────
            ItemDetailChips(listing = listing)

            Spacer(modifier = Modifier.height(8.dp))

            // ── Action buttons ────────────────────────────────────────────
            ActionButtons(
                inCart       = uiState.inCart,
                onAddToCart  = onAddToCart,
                onChatSeller = { showContactSheet = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Contact bottom sheet ──────────────────────────────────────────────────
    if (showContactSheet) {
        ContactSellerSheet(
            seller    = uiState.seller,
            onDismiss = { showContactSheet = false }
        )
    }
}

// ── Rating row ────────────────────────────────────────────────────────────────
@Composable
private fun RatingRow() {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 5 stars — static for now, can be dynamic later
        repeat(5) { index ->
            Icon(
                imageVector = if (index < 4) Icons.Rounded.Star else Icons.AutoMirrored.Rounded.StarHalf,
                contentDescription = null,
                tint     = Color(0xFFFFC107),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text  = "4.0",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = "(12 reviews)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Seller card ───────────────────────────────────────────────────────────────
@Composable
private fun SellerCard(seller: com.fanyiadrien.ictu_ex.data.model.User) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circle with initials
            Box(
                modifier          = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment  = Alignment.Center
            ) {
                Text(
                    text       = seller.displayName.take(1).uppercase(),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column {
                Text(
                    text       = seller.displayName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text  = "ICTU Student · ${seller.studentId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Verified badge
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text     = "Verified",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

// ── Item detail chips ─────────────────────────────────────────────────────────
@Composable
private fun ItemDetailChips(listing: com.fanyiadrien.ictu_ex.data.model.Listing) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailChip(icon = Icons.Rounded.Category,    label = listing.category)
        DetailChip(icon = Icons.Rounded.CheckCircle, label = if (listing.available) "Available" else "Sold")
        DetailChip(icon = Icons.Rounded.LocationOn,  label = "On Campus")
    }
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(14.dp)
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Action buttons ────────────────────────────────────────────────────────────
@Composable
private fun ActionButtons(
    inCart: Boolean,
    onAddToCart: () -> Unit,
    onChatSeller: () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick  = onChatSeller,
            modifier = Modifier.weight(1f).height(54.dp),
            shape = MaterialTheme.shapes.large,
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chat Seller", style = MaterialTheme.typography.titleSmall)
        }

        Button(
            onClick  = onAddToCart,
            enabled  = !inCart,
            modifier = Modifier.weight(1f).height(54.dp),
            shape    = MaterialTheme.shapes.large,
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (inCart) MaterialTheme.colorScheme.secondaryContainer
                                 else MaterialTheme.colorScheme.primary,
                contentColor   = if (inCart) MaterialTheme.colorScheme.onSecondaryContainer
                                 else MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContentColor   = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                imageVector = if (inCart) Icons.Rounded.ShoppingCart else Icons.Rounded.AddShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text  = if (inCart) "In Cart" else "Add to Cart",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

// ── Contact seller bottom sheet ───────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactSellerSheet(
    seller: com.fanyiadrien.ictu_ex.data.model.User?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement   = Arrangement.spacedBy(16.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Text(
                text       = "Contact Seller",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider()

            // Seller info inside sheet
            seller?.let {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = it.displayName.take(1).uppercase(),
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Text(it.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(it.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // TODO: Chat feature
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color    = MaterialTheme.colorScheme.primaryContainer,
                shape    = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text  = "In-app chat coming soon. For now, meet on campus for exchange.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Button(
                onClick  = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Got it", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Error state ───────────────────────────────────────────────────────────────
@Composable
private fun ErrorState(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint     = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onBack) {
            Text("Go Back")
        }
    }
}