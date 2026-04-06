package com.fanyiadrien.ictu_ex.data.repository

import com.fanyiadrien.ictu_ex.data.model.ChatMessage
import com.fanyiadrien.ictu_ex.data.model.ChatThread
import com.fanyiadrien.ictu_ex.data.model.User
import com.fanyiadrien.ictu_ex.utils.AppError
import com.fanyiadrien.ictu_ex.utils.AppResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagesRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val chatsCollection = firestore.collection("chats")

    /** Returns all users in the app excluding the current user. */
    suspend fun fetchAllUsers(): List<User> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            firestore.collection("users")
                .get().await()
                .documents
                .mapNotNull { it.toObject(User::class.java)?.copy(uid = it.id) }
                .filter { it.uid != uid }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun observeThreads(): Flow<List<ChatThread>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = chatsCollection
            .whereArrayContains("participantIds", uid)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val threads = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatThread::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(threads)
            }

        awaitClose { registration.remove() }
    }

    fun observeThread(threadId: String): Flow<ChatThread?> = callbackFlow {
        if (threadId.isBlank()) { trySend(null); close(); return@callbackFlow }

        val registration = chatsCollection.document(threadId)
            .addSnapshotListener { snapshot, _ ->
                val thread = snapshot?.toObject(ChatThread::class.java)?.copy(id = snapshot.id)
                trySend(thread)
            }

        awaitClose { registration.remove() }
    }

    fun observeMessages(threadId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (threadId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = chatsCollection
            .document(threadId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(messages)
            }

        awaitClose { registration.remove() }
    }

    suspend fun ensureThread(otherUserId: String, listingId: String?): AppResult<String> {
        val uid = auth.currentUser?.uid ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)
        if (otherUserId.isBlank() || otherUserId == uid) {
            return AppResult.Error("Invalid chat recipient.")
        }

        val threadId = listOf(uid, otherUserId).sorted().joinToString("_")
        val threadRef = chatsCollection.document(threadId)

        return try {
            val me = firestore.collection("users").document(uid).get().await()
            val other = firestore.collection("users").document(otherUserId).get().await()
            val now = System.currentTimeMillis()

            val payload = mutableMapOf<String, Any>(
                "participantIds" to listOf(uid, otherUserId),
                "participantNames" to mapOf(
                    uid to (me.getString("displayName") ?: "You"),
                    otherUserId to (other.getString("displayName") ?: "Student")
                ),
                "lastMessageAt" to now,
                "updatedAt" to now,
                "unreadCountByUser" to mapOf(uid to 0L, otherUserId to 0L)
            )
            if (!listingId.isNullOrBlank()) payload["listingId"] = listingId
            val listingTitle = if (!listingId.isNullOrBlank()) {
                firestore.collection("listings").document(listingId).get().await().getString("title")
            } else null
            if (!listingTitle.isNullOrBlank()) payload["listingTitle"] = listingTitle

            threadRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
            AppResult.Success(threadId)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    suspend fun sendMessage(threadId: String, text: String): AppResult<Unit> {
        val uid = auth.currentUser?.uid ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)
        if (threadId.isBlank() || text.isBlank()) return AppResult.Error("Message cannot be empty.")

        return try {
            val threadRef = chatsCollection.document(threadId)
            val threadDoc = threadRef.get().await()
            val participantIds = threadDoc.get("participantIds") as? List<*> ?: emptyList<Any>()
            val receiverId = participantIds.firstOrNull { it is String && it != uid } as? String
            val now = System.currentTimeMillis()

            val messageRef = threadRef.collection("messages").document()
            messageRef.set(
                mapOf(
                    "senderId" to uid,
                    "text" to text.trim(),
                    "createdAt" to now
                )
            ).await()

            val updates = mutableMapOf<String, Any>(
                "lastMessage" to text.trim(),
                "lastMessageAt" to now,
                "updatedAt" to now,
                "unreadCountByUser.$uid" to 0L
            )
            if (!receiverId.isNullOrBlank()) {
                updates["unreadCountByUser.$receiverId"] = FieldValue.increment(1)
            }
            threadRef.update(updates).await()

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    /** Writes a message document that carries an image URL instead of text. */
    suspend fun sendImageMessage(threadId: String, imageUrl: String): AppResult<Unit> {
        val uid = auth.currentUser?.uid ?: return AppResult.Error(AppError.UNKNOWN_AUTH_ERROR)
        if (threadId.isBlank()) return AppResult.Error("Invalid thread.")

        return try {
            val threadRef = chatsCollection.document(threadId)
            val threadDoc = threadRef.get().await()
            val participantIds = threadDoc.get("participantIds") as? List<*> ?: emptyList<Any>()
            val receiverId = participantIds.firstOrNull { it is String && it != uid } as? String
            val now = System.currentTimeMillis()

            threadRef.collection("messages").document().set(
                mapOf(
                    "senderId"  to uid,
                    "text"      to "",
                    "imageUrl"  to imageUrl,
                    "createdAt" to now
                )
            ).await()

            val updates = mutableMapOf<String, Any>(
                "lastMessage"  to "📷 Photo",
                "lastMessageAt" to now,
                "updatedAt"    to now,
                "unreadCountByUser.$uid" to 0L
            )
            if (!receiverId.isNullOrBlank()) {
                updates["unreadCountByUser.$receiverId"] = FieldValue.increment(1)
            }
            threadRef.update(updates).await()

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.SAVE_FAILED, e)
        }
    }

    suspend fun markThreadRead(threadId: String) {
        val uid = auth.currentUser?.uid ?: return
        chatsCollection.document(threadId)
            .update("unreadCountByUser.$uid", 0L)
            .await()
    }
}

