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

object IncomingCallEvents {
    private val _events = MutableSharedFlow<IncomingCallPush>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun publish(push: IncomingCallPush) {
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
