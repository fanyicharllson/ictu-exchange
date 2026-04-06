package com.fanyiadrien.ictu_ex.feature.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.data.model.ChatThread
import com.fanyiadrien.ictu_ex.data.model.User
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Messages",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                // Search bar
                OutlinedTextField(
                    value         = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder   = { Text("Search people…") },
                    leadingIcon   = {
                        Icon(
                            Icons.Rounded.Search, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon  = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Rounded.Close, null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    shape  = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor  = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor    = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── People section ────────────────────────────────────────────
            if (uiState.filteredUsers.isNotEmpty()) {
                item {
                    SectionLabel(
                        text     = if (uiState.searchQuery.isBlank()) "People" else "Results",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                items(uiState.filteredUsers, key = { "user_${it.uid}" }) { user ->
                    UserRow(
                        user    = user,
                        onClick = {
                            navController.navigate(
                                Screen.Messages.createRoute(sellerId = user.uid)
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // ── Recent chats section ──────────────────────────────────────
            if (uiState.threads.isNotEmpty() && uiState.searchQuery.isBlank()) {
                item {
                    SectionLabel(
                        text     = "Recent",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                items(uiState.threads, key = { "thread_${it.id}" }) { thread ->
                    ThreadRow(
                        thread     = thread,
                        currentUid = currentUid,
                        onClick    = { navController.navigate(Screen.Chat.createRoute(thread.id)) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // ── Empty state ───────────────────────────────────────────────
            if (uiState.filteredUsers.isEmpty() && uiState.threads.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.ChatBubbleOutline, null,
                                modifier = Modifier.size(64.dp),
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No users found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text.uppercase(),
        style    = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color    = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = modifier
    )
}

// ── User row (People section) ─────────────────────────────────────────────────
@Composable
private fun UserRow(user: User, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color   = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            AvatarCircle(
                name      = user.displayName,
                size      = 52,
                bgColor   = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = user.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text  = buildString {
                        append(if (user.userType == "SELLER") "Seller" else "Buyer")
                        if (user.studentId.isNotBlank()) append(" · ${user.studentId}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Icon(
                Icons.Rounded.ChevronRight, null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Thread row (Recent chats) ─────────────────────────────────────────────────
@Composable
private fun ThreadRow(
    thread: ChatThread,
    currentUid: String?,
    onClick: () -> Unit
) {
    val otherName  = thread.participantNames.entries
        .firstOrNull { it.key != currentUid }?.value ?: "Chat"
    val unread     = currentUid?.let { thread.unreadCountByUser[it] ?: 0L } ?: 0L
    val timeString = formatThreadTime(thread.lastMessageAt)

    Surface(
        onClick = onClick,
        color   = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AvatarCircle(
                name      = otherName,
                size      = 52,
                bgColor   = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text  = otherName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (unread > 0) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text  = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (unread > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text  = thread.lastMessage.ifBlank { "No messages yet" },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unread > 0) MaterialTheme.colorScheme.onBackground
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (unread > 0) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (unread > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = if (unread > 99) "99+" else unread.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Shared avatar composable ──────────────────────────────────────────────────
@Composable
fun AvatarCircle(
    name: String,
    size: Int,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = name.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = (size / 2.5).sp
        )
    }
}

private fun formatThreadTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000L              -> "now"
        diff < 3_600_000L           -> "${diff / 60_000}m"
        diff < 86_400_000L          -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        diff < 7 * 86_400_000L      -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        else                        -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(timestamp))
    }
}
