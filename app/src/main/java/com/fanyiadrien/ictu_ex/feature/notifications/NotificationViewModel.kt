package com.fanyiadrien.ictu_ex.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanyiadrien.ictu_ex.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val notifications = notificationRepository.getNotifications()

    val unreadCount = notificationRepository.getUnreadCount()

    fun markAsRead(notifId: String) {
        viewModelScope.launch { notificationRepository.markAsRead(notifId) }
    }

    fun deleteNotification(notifId: String) {
        viewModelScope.launch { notificationRepository.deleteNotification(notifId) }
    }
}
