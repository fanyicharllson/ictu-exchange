package com.fanyiadrien.ictu_ex.feature.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.model.ChatThread
import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.data.repository.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadAllUsers()
        observeThreads()
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            val users = messagesRepository.fetchAllUsers()
            _uiState.update { it.copy(allUsers = users) }
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

data class ChatListUiState(
    val allUsers: List<User> = emptyList(),
    val threads: List<ChatThread> = emptyList(),
    val searchQuery: String = ""
) {
    val filteredUsers: List<User>
        get() = if (searchQuery.isBlank()) allUsers
                else allUsers.filter {
                    it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.studentId.contains(searchQuery, ignoreCase = true)
                }
}
