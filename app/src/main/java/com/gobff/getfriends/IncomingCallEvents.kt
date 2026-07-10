package com.gobff.getfriends

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class IncomingCallPush(
    val roomId: String,
    val requestedRole: String,
    val callerName: String,
    val callerAvatarUrl: String?,
    val notificationId: Int
)

data class CallEndedPush(
    val callId: String,
    val roomId: String,
    val roomType: String,
    val status: String,
    val endedByUserId: String,
    val endedByName: String,
    val endedByAvatarUrl: String?,
    val createdByUserId: String,
    val invitedUserId: String,
    val endedAt: String,
    val expiresAt: String,
    val connected: Boolean?
) {
    fun matchesRoom(roomId: String?): Boolean {
        if (roomId.isNullOrBlank()) return false
        return roomId == this.roomId || roomId == callId
    }

    fun wasDeclinedByCallee(currentUserId: String): Boolean {
        return connected == false &&
            currentUserId.isNotBlank() &&
            currentUserId == createdByUserId &&
            endedByUserId == invitedUserId
    }

    fun displayMessage(currentUserId: String): String {
        val name = endedByName.ifBlank { "The other person" }

        return when {
            wasDeclinedByCallee(currentUserId) -> "$name declined the call"
            connected == false -> "Call ended"
            else -> "$name ended the call"
        }
    }
}

object IncomingCallEvents {
    private val _events = MutableSharedFlow<IncomingCallPush>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun publish(push: IncomingCallPush) {
        _events.tryEmit(push)
    }
}

object CallEndedEvents {
    private val _events = MutableSharedFlow<CallEndedPush>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun publish(push: CallEndedPush) {
        _events.tryEmit(push)
    }
}

object AppForegroundState {
    @Volatile
    var isForeground: Boolean = false
        private set

    fun markForeground() {
        isForeground = true
    }

    fun markBackground() {
        isForeground = false
    }
}
