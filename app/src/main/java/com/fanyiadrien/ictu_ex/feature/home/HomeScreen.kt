package com.fanyiadrien.ictu_ex.feature.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fanyiadrien.ictu_ex.R
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.core.ui.components.IctuBottomNav
import com.fanyiadrien.ictu_ex.data.model.Listing
import com.fanyiadrien.ictu_ex.data.model.ListingCategory
import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.ui.theme.ThemeMode
import java.text.NumberFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    
    LaunchedEffect(Unit) {
        viewModel.fetchListings()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            IctuBottomNav(navController = navController, isSeller = uiState.isSeller)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.MyActivity.route) },
                icon = { Icon(Icons.Rounded.Dashboard, null) },
                text = { Text(if (uiState.isSeller) "Seller Panel" else "My Activity") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for bottom nav
        ) {
            // ── 1. Premium Top Bar with Profile ───────────────────────────
            item {
                HomeTopBar(
                    user              = uiState.currentUser,
                    isSeller          = uiState.isSeller,
                    cartItemCount     = uiState.cartItemCount,
                    unreadNotifCount  = uiState.unreadNotifCount,
                    onCartClick       = { navController.navigate(Screen.Cart.route) },
                    onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                    onProfileClick    = { navController.navigate(Screen.Profile.route) }
                )
            }

            // ── 2. "Top Models" / Hero Title ──────────────────────────────
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "Top Models",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Explore top luxury marketplace models",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── 3. Search & Filter Row ────────────────────────────────────
            item {
                SearchAndFilterRow(
                    query = uiState.searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged
                )
            }

            // ── 4. Categories (Top Brands style) ──────────────────────────
            item {
                CategorySection(
                    selected = uiState.selectedCategory,
                    onSelected = viewModel::onCategorySelected
                )
            }

            // ── 5. Main Feed ("For You" style) ────────────────────────────
            item { 
                SectionHeader(
                    title = if (uiState.searchQuery.isNotEmpty()) "Search Results" else "For You"
                ) 
            }

            if (uiState.isLoading) {
                item { LoadingState() }
            } else if (uiState.filteredListings.isEmpty()) {
                item { 
                    EmptyFeedState(
                        isSeller = uiState.isSeller,
                        isSearch = uiState.searchQuery.isNotEmpty() || uiState.selectedCategory != ListingCategory.ALL
                    ) 
                }
            } else {
                val chunked = uiState.filteredListings.chunked(2)
                items(chunked) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { listing ->
                            ProductCard(
                                listing = listing,
                                modifier = Modifier.weight(1f),
                                isSaved = uiState.wishlistedIds.contains(listing.id),
                                onSave = { viewModel.toggleWishlist(listing.id) },
                                onClick = { navController.navigate(Screen.ItemDetail.createRoute(listing.id)) }
                            )
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    user: User?,
    isSeller: Boolean,
    cartItemCount: Int,
    unreadNotifCount: Int,
    onCartClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Left: avatar + greeting ───────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onProfileClick() }
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (user?.profileImageUrl?.isNotEmpty() == true) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Rounded.Person, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text  = "Hey, ${user?.displayName ?: "Student"}! 👋",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("ICTU Campus", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // ── Right: notification bell + cart (buyers only) ─────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Notification bell — visible to everyone
            BadgedBox(
                badge = {
                    if (unreadNotifCount > 0) {
                        Badge { Text(if (unreadNotifCount > 9) "9+" else unreadNotifCount.toString()) }
                    }
                }
            ) {
                IconButton(
                    onClick  = onNotificationClick,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Rounded.Notifications, null, tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Cart icon — buyers only
            if (!isSeller) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge { Text(if (cartItemCount > 9) "9+" else cartItemCount.toString()) }
                        }
                    }
                ) {
                    IconButton(
                        onClick  = onCartClick,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Rounded.ShoppingBag, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchAndFilterRow(query: String, onQueryChanged: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Search items...") },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true
        )
        
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.onBackground)
                .clickable { /* TODO: Advanced Filters */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Tune, null, tint = MaterialTheme.colorScheme.background)
        }
    }
}

@Composable
private fun CategorySection(selected: ListingCategory, onSelected: (ListingCategory) -> Unit) {
    Column {
        SectionHeader(title = "Top Brands")
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ListingCategory.values().forEach { category ->
                val isSelected = selected == category
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSelected(category) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when(category) {
                                ListingCategory.ALL -> Icons.Rounded.Apps
                                ListingCategory.ELECTRONICS -> Icons.Rounded.Laptop
                                ListingCategory.TEXTBOOKS -> Icons.Rounded.Book
                                ListingCategory.HOSTEL_GEAR -> Icons.Rounded.KingBed
                                ListingCategory.UNIFORMS -> Icons.Rounded.Checkroom
                            },
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = category.displayName, 
                        style = MaterialTheme.typography.labelMedium, 
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    listing: Listing,
    isSaved: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
            ) {
                AsyncImage(
                    model = listing.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(R.drawable.ic_launcher_foreground)
                )
                
                IconButton(
                    onClick = onSave,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(Modifier.padding(vertical = 12.dp, horizontal = 4.dp)) {
                Text(
                    text = listing.title, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listing.category, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatPrice(listing.price),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, Modifier.size(14.dp), tint = Color(0xFFFFB300))
                        Text(
                            text = " 4.9", 
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.clickable { }
        ) {
            Text("See all", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Rounded.ChevronRight, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}

@Composable
private fun EmptyFeedState(isSeller: Boolean, isSearch: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSearch) Icons.Rounded.SearchOff else Icons.Rounded.Inventory2, 
            contentDescription = null, 
            modifier = Modifier.size(80.dp), 
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isSearch) "No items found" else "No listings yet", 
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = if (isSearch) "Try adjusting your filters or search query" 
                   else if (isSeller) "Post your first item to start selling!" 
                   else "Check back later for new items!", 
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatPrice(price: Double): String {
    return "XAF " + String.format(Locale.US, "%,.0f", price)
}
