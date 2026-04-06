package com.fanyiadrien.ictu_ex.feature.messages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fanyiadrien.ictu_ex.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state   by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid }

    // Scroll to bottom on new messages
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    // Gallery picker for photo messages
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendImageMessage(context, it) }
    }

    val otherName = state.thread?.participantNames?.entries
        ?.firstOrNull { it.key != currentUid }?.value ?: "Chat"

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
                    }

                    // Avatar
                    AvatarCircle(
                        name      = otherName,
                        size      = 40,
                        bgColor   = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = otherName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (!state.thread?.listingTitle.isNullOrBlank()) {
                            Text(
                                text  = "Re: ${state.thread!!.listingTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        snackbarHost    = { SnackbarHost(snackbarHostState) },
        containerColor  = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            state.errorMessage != null && state.threadId == null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // ── Messages ──────────────────────────────────────────
                    LazyColumn(
                        state          = listState,
                        modifier       = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            MessageBubble(
                                message    = message,
                                currentUid = currentUid
                            )
                        }
                    }

                    // ── Image uploading indicator ─────────────────────────
                    if (state.isSendingImage) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color    = MaterialTheme.colorScheme.primary
                        )
                    }

                    // ── Composer ──────────────────────────────────────────
                    Surface(
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Attach photo button
                            IconButton(
                                onClick  = { galleryLauncher.launch("image/*") },
                                enabled  = !state.isSendingImage,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    Icons.Rounded.AttachFile,
                                    contentDescription = "Attach photo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Text input
                            OutlinedTextField(
                                value         = state.composerText,
                                onValueChange = viewModel::onComposerTextChange,
                                modifier      = Modifier.weight(1f),
                                placeholder   = { Text("Message…") },
                                shape         = RoundedCornerShape(24.dp),
                                maxLines      = 4,
                                colors        = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor    = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedBorderColor      = MaterialTheme.colorScheme.primary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            )

                            // Send button
                            val canSend = state.composerText.isNotBlank() && !state.isSendingImage
                            IconButton(
                                onClick  = viewModel::sendMessage,
                                enabled  = canSend,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (canSend) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "Send",
                                    tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Message bubble ────────────────────────────────────────────────────────────
@Composable
private fun MessageBubble(message: ChatMessage, currentUid: String?) {
    val isMine = message.senderId == currentUid
    val timeStr = remember(message.createdAt) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.createdAt))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!message.imageUrl.isNullOrBlank()) {
                // ── Image bubble ──────────────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(
                        topStart    = 18.dp, topEnd = 18.dp,
                        bottomStart = if (isMine) 18.dp else 4.dp,
                        bottomEnd   = if (isMine) 4.dp  else 18.dp
                    ),
                    color = if (isMine) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    AsyncImage(
                        model              = message.imageUrl,
                        contentDescription = "Photo",
                        modifier           = Modifier
                            .widthIn(min = 120.dp, max = 260.dp)
                            .heightIn(min = 80.dp, max = 260.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart    = 18.dp, topEnd = 18.dp,
                                    bottomStart = if (isMine) 18.dp else 4.dp,
                                    bottomEnd   = if (isMine) 4.dp  else 18.dp
                                )
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            } else if (message.text.isNotBlank()) {
                // ── Text bubble ───────────────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(
                        topStart    = 18.dp, topEnd = 18.dp,
                        bottomStart = if (isMine) 18.dp else 4.dp,
                        bottomEnd   = if (isMine) 4.dp  else 18.dp
                    ),
                    color = if (isMine) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text     = message.text,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = if (isMine) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // Timestamp
            Text(
                text     = timeStr,
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
