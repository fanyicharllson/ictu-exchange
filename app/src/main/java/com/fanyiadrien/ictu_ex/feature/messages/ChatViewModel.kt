package com.fanyiadrien.ictu_ex.feature.messages

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.data.model.ChatMessage
import com.fanyiadrien.ictu_ex.data.model.ChatThread
import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.data.remote.CloudinaryService
import com.fanyiadrien.ictu_ex.data.repository.MessagesRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val cloudinaryService: CloudinaryService,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    private var lastResolvedOtherUid: String? = null

    init {
        loadCurrentUser()

        // Route 1: opened from ChatListScreen — threadId is known
        val threadId = savedStateHandle.get<String>("threadId")

        // Route 2: opened from ItemDetailScreen — sellerId + optional listingId
        val sellerId  = savedStateHandle.get<String>(Screen.Messages.sellerIdArg)
        val listingId = savedStateHandle.get<String>(Screen.Messages.listingIdArg)

        when {
            !threadId.isNullOrBlank() -> openThread(threadId)
            !sellerId.isNullOrBlank() -> createAndOpenThread(sellerId, listingId)
            else -> _uiState.update {
                it.copy(isLoading = false, errorMessage = "No chat target provided.")
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = userRepository.getCurrentUser()) {
                is AppResult.Success -> _uiState.update { it.copy(currentUser = result.data) }
                else -> Unit
            }
        }
    }

    // ── Open an existing thread ───────────────────────────────────────────────

    private fun openThread(threadId: String) {
        _uiState.update { it.copy(threadId = threadId, isLoading = false) }
        observeMessages(threadId)
        observeThread(threadId)
        viewModelScope.launch { messagesRepository.markThreadRead(threadId) }
    }

    // ── Create (or reuse) a thread then open it ───────────────────────────────

    private fun createAndOpenThread(sellerId: String, listingId: String?) {
        if (sellerId == auth.currentUser?.uid) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "You cannot start a chat with yourself."
                )
            }
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = messagesRepository.ensureThread(sellerId, listingId)) {
                is AppResult.Success -> openThread(result.data)
                is AppResult.Error   -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    // ── Real-time message stream from Firestore ───────────────────────────────

    private fun observeMessages(threadId: String) {
        viewModelScope.launch {
            messagesRepository.observeMessages(threadId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    // ── Real-time thread metadata (title bar name + listing title) ────────────

    private fun observeThread(threadId: String) {
        viewModelScope.launch {
            messagesRepository.observeThread(threadId).collect { thread ->
                if (thread != null) {
                    _uiState.update { it.copy(thread = thread) }
                    resolveOtherUserType(thread)
                }
            }
        }
    }

    private fun resolveOtherUserType(thread: ChatThread) {
        val currentUid = auth.currentUser?.uid ?: return
        val otherUid = thread.participantIds.firstOrNull { it != currentUid } ?: return
        if (lastResolvedOtherUid == otherUid) return
        lastResolvedOtherUid = otherUid

        viewModelScope.launch {
            when (val result = userRepository.getUserById(otherUid)) {
                is AppResult.Success -> _uiState.update { it.copy(otherUserType = result.data.userType) }
                else -> _uiState.update { it.copy(otherUserType = null) }
            }
        }
    }

    // ── Send a message to Firestore ───────────────────────────────────────────

    fun sendImageMessage(context: Context, uri: Uri) {
        val threadId = _uiState.value.threadId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingImage = true) }
            val uploadResult = cloudinaryService.uploadImage(context, uri, folder = "chat_images")
            when (uploadResult) {
                is AppResult.Success -> {
                    messagesRepository.sendImageMessage(threadId, uploadResult.data)
                    _uiState.update { it.copy(isSendingImage = false) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isSendingImage = false, errorMessage = uploadResult.message) }
                }
                else -> _uiState.update { it.copy(isSendingImage = false) }
            }
        }
    }

    fun sendMessage() {
        val threadId = _uiState.value.threadId ?: return
        val text = _uiState.value.composerText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            when (val result = messagesRepository.sendMessage(threadId, text)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(composerText = "", errorMessage = null) }
                    messagesRepository.markThreadRead(threadId)
                }
                is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                else -> Unit
            }
        }
    }

    fun onComposerTextChange(text: String) {
        _uiState.update { it.copy(composerText = text) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class ChatUiState(
    val threadId: String? = null,
    val thread: ChatThread? = null,
    val currentUser: User? = null,
    val otherUserType: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val composerText: String = "",
    val isLoading: Boolean = true,
    val isSendingImage: Boolean = false,
    val errorMessage: String? = null
)
