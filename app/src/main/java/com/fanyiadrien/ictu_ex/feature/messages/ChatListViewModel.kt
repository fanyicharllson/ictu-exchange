package com.fanyiadrien.ictu_ex.feature.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.model.ChatThread
import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.data.repository.MessagesRepository
import com.fanyiadrien.ictu_ex.data.repository.UserRepository
import com.fanyiadrien.ictu_ex.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private var currentUserLoaded = false
    private var usersLoaded = false

    init {
        loadCurrentUser()
        loadAllUsers()
        observeThreads()
    }

    private fun updateLoadingState() {
        _uiState.update { it.copy(isLoading = !(currentUserLoaded && usersLoaded)) }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = userRepository.getCurrentUser()) {
                is AppResult.Success -> _uiState.update { it.copy(currentUser = result.data) }
                is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                else -> Unit
            }
            currentUserLoaded = true
            updateLoadingState()
        }
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            val users = messagesRepository.fetchAllUsers()
            _uiState.update { it.copy(allUsers = users) }
            usersLoaded = true
            updateLoadingState()
        }
    }

    private fun observeThreads() {
        viewModelScope.launch {
            messagesRepository.observeThreads().collect { threads ->
                _uiState.update { it.copy(threads = threads) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}

@Suppress("unused")
data class ChatListUiState(
    val currentUser: User? = null,
    val allUsers: List<User> = emptyList(),
    val threads: List<ChatThread> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isBuyer: Boolean get() = currentUser?.userType == "BUYER"
    val counterpartLabel: String get() = "People"
    val introLabel: String
        get() = "Discover buyers and sellers, then continue your conversations"

    val quickAccessTitle: String
        get() = if (isBuyer) "Quick Access Sellers" else "Quick Access Buyers"

    val quickAccessUsers: List<User>
        get() = filteredUsers.filter { user ->
            when (currentUser?.userType) {
                "BUYER" -> user.userType == "SELLER"
                "SELLER" -> user.userType == "BUYER"
                else -> true
            }
        }

    val filteredUsers: List<User>
        get() = allUsers
            .filter {
                searchQuery.isBlank() ||
                    it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.studentId.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<User> { user ->
                    threads.firstOrNull { thread -> thread.otherParticipantId(currentUser?.uid) == user.uid }
                        ?.lastMessageAt ?: 0L
                }.thenBy { it.displayName }
            )

    val filteredThreads: List<ChatThread>
        get() = threads.filter { thread ->
            if (searchQuery.isBlank()) return@filter true

            val currentUid = currentUser?.uid
            val otherName = thread.participantNames.entries.firstOrNull { it.key != currentUid }?.value.orEmpty()
            otherName.contains(searchQuery, ignoreCase = true) ||
                thread.listingTitle.orEmpty().contains(searchQuery, ignoreCase = true) ||
                thread.lastMessage.contains(searchQuery, ignoreCase = true)
        }

    val hasResults: Boolean get() = filteredUsers.isNotEmpty() || filteredThreads.isNotEmpty()
}

internal fun ChatThread.otherParticipantId(currentUid: String?): String? {
    if (currentUid.isNullOrBlank()) return participantIds.firstOrNull()
    return participantIds.firstOrNull { it != currentUid }
}
