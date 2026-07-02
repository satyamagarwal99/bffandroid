package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.AppAttestation
import com.gobff.getfriends.data.model.CallRoomUiState
import com.gobff.getfriends.data.model.CreateRoomBody
import com.gobff.getfriends.data.model.JoinRoomBody
import com.gobff.getfriends.data.model.RoomRole
import com.gobff.getfriends.data.model.RoomType
import com.gobff.getfriends.data.model.RtcTokenBody
import com.gobff.getfriends.data.model.RtcTokenResponse
import com.gobff.getfriends.utils.Constant
import com.gobff.getfriends.utils.OtpDeviceProvider
import com.gobff.getfriends.utils.TokenUtils
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.launch

class CallViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()
    private val otpDeviceProvider = OtpDeviceProvider(application.applicationContext)
    private var rtcEngine: RtcEngine? = null
    private val leftRoomIds = mutableSetOf<String>()

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            updateRtcState {
                it.copy(
                    isJoiningRtc = false,
                    isRtcJoined = true,
                    errorMessage = null
                )
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            updateRtcState {
                it.copy(remoteAudioUserIds = (it.remoteAudioUserIds + uid).distinct())
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            updateRtcState {
                it.copy(remoteAudioUserIds = it.remoteAudioUserIds.filterNot { userId -> userId == uid })
            }
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            updateRtcState {
                it.copy(
                    isJoiningRtc = false,
                    isRtcJoined = false,
                    remoteAudioUserIds = emptyList()
                )
            }
        }

        override fun onError(err: Int) {
            updateRtcState {
                it.copy(
                    isJoiningRtc = false,
                    errorMessage = "Agora RTC error: $err"
                )
            }
        }
    }

    var uiState by mutableStateOf(CallRoomUiState())
        private set

    fun createAudioRoom(
        title: String,
        maxParticipants: Int = DEFAULT_MAX_PARTICIPANTS
    ) {
        createRoom(
            type = RoomType.GroupAudioRoom,
            title = title,
            maxParticipants = maxParticipants
        )
    }

    fun createRandomOneToOneAudioCall(
        title: String
    ) {
        createRoom(
            type = RoomType.OneToOneAudioCall,
            title = title,
            maxParticipants = null
        )
    }

    fun createVideoRoom(
        title: String,
        maxParticipants: Int = DEFAULT_MAX_PARTICIPANTS
    ) {
        createRoom(
            type = RoomType.GroupVideoRoom,
            title = title,
            maxParticipants = maxParticipants
        )
    }

    fun createOneToOneVideoCall(
        title: String,
        invitedUserId: String? = null
    ) {
        createRoom(
            type = RoomType.OneToOneVideoCall,
            title = title,
            maxParticipants = null,
            invitedUserId = invitedUserId
        )
    }

    fun joinAudioRoom(
        roomId: String,
        requestedRole: String = RoomRole.Listener
    ) {
        joinRoom(roomId = roomId, requestedRole = requestedRole)
    }

    fun getAudioRtcToken(
        roomId: String,
        requestedRole: String = RoomRole.Speaker
    ) {
        getRtcToken(roomId = roomId, requestedRole = requestedRole)
    }

    fun setMuted(isMuted: Boolean) {
        rtcEngine?.muteLocalAudioStream(isMuted)
        uiState = uiState.copy(isMuted = isMuted)
    }

    fun setSpeakerEnabled(isEnabled: Boolean) {
        rtcEngine?.setEnableSpeakerphone(isEnabled)
        uiState = uiState.copy(isSpeakerEnabled = isEnabled)
    }

    fun leaveCall() {
        rtcEngine?.leaveChannel()
        leaveCurrentBackendRoom()
        uiState = uiState.copy(
            isJoiningRtc = false,
            isRtcJoined = false,
            remoteAudioUserIds = emptyList()
        )
    }

    fun closeCurrentRoom() {
        val token = TokenUtils.getToken()
        val roomId = uiState.room?.id
        if (token.isBlank() || roomId.isNullOrBlank()) return

        viewModelScope.launch {
            runCatching { mainRepository.closeRoom(token, roomId) }
                .onSuccess { response ->
                    response.body()?.let { room ->
                        uiState = uiState.copy(room = room)
                    }
                }
        }
    }

    fun requestVideoUpgrade() {
        val token = TokenUtils.getToken()
        val roomId = uiState.room?.id
        if (token.isBlank() || roomId.isNullOrBlank()) return

        viewModelScope.launch {
            runCatching { mainRepository.requestVideoUpgrade(token, roomId) }
                .onFailure { error ->
                    uiState = uiState.copy(
                        errorMessage = error.message ?: "Unable to request video upgrade"
                    )
                }
        }
    }

    private fun createRoom(
        type: String,
        title: String,
        maxParticipants: Int?,
        invitedUserId: String? = null
    ) {
        val token = TokenUtils.getToken()
        if (token.isBlank()) {
            uiState = uiState.copy(
                isCreatingRoom = false,
                room = null,
                errorMessage = "Login token missing"
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isCreatingRoom = true,
                errorMessage = null
            )

            val body = CreateRoomBody(
                type = type,
                title = title,
                maxParticipants = maxParticipants,
                invitedUserId = invitedUserId
            )

            runCatching { mainRepository.createRoom(token, body) }
                .onSuccess { response ->
                    val responseBody = response.body()
                    uiState = if (response.isSuccessful && responseBody != null) {
                        responseBody.id?.let { roomId ->
                            getAudioRtcToken(roomId = roomId)
                        }
                        uiState.copy(
                            isCreatingRoom = false,
                            room = responseBody,
                            errorMessage = null
                        )
                    } else {
                        uiState.copy(
                            isCreatingRoom = false,
                            room = null,
                            errorMessage = "Unable to create room"
                        )
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isCreatingRoom = false,
                        room = null,
                        errorMessage = error.message ?: "Unable to create room"
                    )
            }
        }
    }

    private fun leaveCurrentBackendRoom() {
        val token = TokenUtils.getToken()
        val roomId = uiState.room?.id
        if (token.isBlank() || roomId.isNullOrBlank() || !leftRoomIds.add(roomId)) return

        viewModelScope.launch {
            runCatching { mainRepository.leaveRoom(token, roomId) }
                .onSuccess { response ->
                    response.body()?.let { room ->
                        uiState = uiState.copy(room = room)
                    }
                }
        }
    }

    private fun joinRoom(
        roomId: String,
        requestedRole: String
    ) {
        val token = TokenUtils.getToken()
        if (token.isBlank()) {
            uiState = uiState.copy(
                isJoiningRoom = false,
                errorMessage = "Login token missing"
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isJoiningRoom = true,
                errorMessage = null
            )

            val body = JoinRoomBody(requestedRole = requestedRole)

            runCatching { mainRepository.joinRoom(token, roomId, body) }
                .onSuccess { response ->
                    val responseBody = response.body()
                    uiState = if (response.isSuccessful && responseBody != null) {
                        responseBody.id?.let { roomId ->
                            getAudioRtcToken(roomId = roomId)
                        }
                        uiState.copy(
                            isJoiningRoom = false,
                            room = responseBody,
                            errorMessage = null
                        )
                    } else {
                        uiState.copy(
                            isJoiningRoom = false,
                            errorMessage = "Unable to join room"
                        )
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isJoiningRoom = false,
                        errorMessage = error.message ?: "Unable to join room"
                    )
                }
        }
    }

    private fun getRtcToken(
        roomId: String,
        requestedRole: String
    ) {
        val token = TokenUtils.getToken()
        if (token.isBlank()) {
            uiState = uiState.copy(
                isFetchingRtcToken = false,
                errorMessage = "Login token missing"
            )
            return
        }

        val deviceId = otpDeviceProvider.installationId()

        viewModelScope.launch {
            uiState = uiState.copy(
                isFetchingRtcToken = true,
                errorMessage = null
            )

            val body = RtcTokenBody(requestedRole = requestedRole)

            runCatching {
                mainRepository.getRtcToken(
                    bearerToken = token,
                    roomId = roomId,
                    appPlatform = Constant.DEVICE_PLATFORM,
                    deviceId = deviceId,
                    appAttestation = AppAttestation.DevAndroid,
                    body = body
                )
            }.onSuccess { response ->
                val responseBody = response.body()
                uiState = if (response.isSuccessful && responseBody != null) {
                    uiState.copy(
                        isFetchingRtcToken = false,
                        rtcToken = responseBody,
                        errorMessage = null
                    ).also { joinAudioChannel(responseBody) }
                } else {
                    uiState.copy(
                        isFetchingRtcToken = false,
                        errorMessage = "Unable to fetch RTC token"
                    )
                }
            }.onFailure { error ->
                uiState = uiState.copy(
                    isFetchingRtcToken = false,
                    errorMessage = error.message ?: "Unable to fetch RTC token"
                )
            }
        }
    }

    private fun joinAudioChannel(rtcToken: RtcTokenResponse) {
        val appId = rtcToken.appId
        val channelName = rtcToken.channelName
        val token = rtcToken.token
        val uid = rtcToken.uid?.toInt() ?: 0

        if (appId.isNullOrBlank() || channelName.isNullOrBlank() || token.isNullOrBlank()) {
            uiState = uiState.copy(
                isJoiningRtc = false,
                errorMessage = "RTC token response is missing Agora credentials"
            )
            return
        }

        runCatching {
            val engine = getOrCreateRtcEngine(appId)
            engine.enableAudio()
            engine.disableVideo()
            engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            engine.setDefaultAudioRoutetoSpeakerphone(uiState.isSpeakerEnabled)
            engine.setEnableSpeakerphone(uiState.isSpeakerEnabled)
            engine.muteLocalAudioStream(uiState.isMuted)

            val options = ChannelMediaOptions().apply {
                channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
                clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                publishMicrophoneTrack = true
                autoSubscribeAudio = true
                publishCameraTrack = false
                autoSubscribeVideo = false
            }

            uiState = uiState.copy(
                isJoiningRtc = true,
                errorMessage = null
            )
            engine.joinChannel(token, channelName, uid, options)
        }.onFailure { error ->
            uiState = uiState.copy(
                isJoiningRtc = false,
                errorMessage = error.message ?: "Unable to join Agora audio channel"
            )
        }
    }

    private fun getOrCreateRtcEngine(appId: String): RtcEngine {
        rtcEngine?.let { return it }

        val config = RtcEngineConfig().apply {
            mContext = getApplication<Application>().applicationContext
            mAppId = appId
            mEventHandler = rtcEventHandler
        }
        return RtcEngine.create(config).also { rtcEngine = it }
    }

    private fun updateRtcState(reducer: (CallRoomUiState) -> CallRoomUiState) {
        viewModelScope.launch {
            uiState = reducer(uiState)
        }
    }

    override fun onCleared() {
        leaveCall()
        rtcEngine?.let {
            RtcEngine.destroy()
            rtcEngine = null
        }
        super.onCleared()
    }

    private companion object {
        const val DEFAULT_MAX_PARTICIPANTS = 50
    }
}
