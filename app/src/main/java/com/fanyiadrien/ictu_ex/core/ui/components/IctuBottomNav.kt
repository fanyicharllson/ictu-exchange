package com.fanyiadrien.ictu_ex.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.feature.notifications.NotificationViewModel

/**
 * Floating pill bottom nav.
 *
 * Seller layout: Home | Search | [+] | 🔔(badge) | Profile
 * Buyer  layout: Home | Search | Settings | Cart | Profile
 */
@Composable
fun IctuBottomNav(
    navController: NavController,
    isSeller: Boolean,
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val currentRoute = navController
        .currentBackStackEntryAsState().value
        ?.destination?.route

    val unreadCount by notificationViewModel.unreadCount.collectAsState(initial = 0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation    = 16.dp,
                    shape        = RoundedCornerShape(32.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    spotColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier              = Modifier.fillMaxWidth()
            ) {
                // ── Home ──────────────────────────────────────────────────
                NavItem(
                    label    = "Home",
                    selected = currentRoute == Screen.Home.route,
                    icon     = Icons.Rounded.Home,
                    onClick  = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )

                // ── Search ────────────────────────────────────────────────
                NavItem(
                    label    = "Search",
                    selected = currentRoute == Screen.Search.route,
                    icon     = Icons.Rounded.Search,
                    onClick  = { navController.navigate(Screen.Search.route) }
                )

                // ── Centre Button ─────────────────────────────────────────
                if (isSeller) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { navController.navigate(Screen.PostItem.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.Add,
                            contentDescription = "Post Item",
                            tint               = MaterialTheme.colorScheme.onPrimary,
                            modifier           = Modifier.size(28.dp)
                        )
                    }
                } else {
                    NavItem(
                        label    = "Settings",
                        selected = currentRoute == Screen.Settings.route,
                        icon     = Icons.Rounded.Settings,
                        onClick  = { navController.navigate(Screen.Settings.route) }
                    )
                }

                // ── Notifications (seller) / Cart (buyer) ─────────────────
                if (isSeller) {
                    NotificationNavItem(
                        selected    = currentRoute == Screen.Notifications.route,
                        unreadCount = unreadCount,
                        onClick     = { navController.navigate(Screen.Notifications.route) }
                    )
                } else {
                    NavItem(
                        label    = "Cart",
                        selected = currentRoute == Screen.Cart.route,
                        icon     = Icons.Rounded.ShoppingCart,
                        onClick  = { navController.navigate(Screen.Cart.route) }
                    )
                }

                // ── Profile ───────────────────────────────────────────────
                NavItem(
                    label    = "Profile",
                    selected = currentRoute == Screen.Profile.route,
                    icon     = Icons.Rounded.Person,
                    onClick  = { navController.navigate(Screen.Profile.route) }
                )
            }
        }
    }
}

// ── Notification bell with unread badge ──────────────────────────────────────
@Composable
private fun NotificationNavItem(
    selected: Boolean,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box {
            Icon(
                imageVector        = Icons.Rounded.Notifications,
                contentDescription = "Notifications",
                tint               = if (selected) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(22.dp)
            )
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text      = if (unreadCount > 9) "9+" else unreadCount.toString(),
                        color     = MaterialTheme.colorScheme.onError,
                        fontSize  = 8.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 8.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text  = "Alerts",
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NavItem(
    label: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = if (selected) MaterialTheme.colorScheme.primary
                                 else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
