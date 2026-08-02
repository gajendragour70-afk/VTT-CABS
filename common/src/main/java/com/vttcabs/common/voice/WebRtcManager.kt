package com.vttcabs.common.voice

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.webrtc.*
import java.util.concurrent.TimeUnit

/**
 * WebRTC Voice Call Manager
 * Handles peer-to-peer voice calls using WebRTC
 */
class WebRtcManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // WebRTC components
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var audioSource: AudioSource? = null
    
    // Signaling
    private val signalingService = WebRtcSignalingService.getInstance()
    
    // State
    private val _callState = MutableStateFlow(CallSignal.CallState.IDLE)
    val callState: StateFlow<CallSignal.CallState> = _callState.asStateFlow()
    
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()
    
    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()
    
    private val _connectionQuality = MutableStateFlow(ConnectionQuality.UNKNOWN)
    val connectionQuality: StateFlow<ConnectionQuality> = _connectionQuality.asStateFlow()
    
    private val _callDuration = MutableStateFlow(0L)
    val callDuration: StateFlow<Long> = _callDuration.asStateFlow()
    
    // Call info
    private var currentCallId: String? = null
    private var currentBookingId: String? = null
    private var currentUserId: String? = null
    private var currentUserRole: CallSignal.CallRole? = null
    private var remoteUserId: String? = null
    private var callStartTime: Long = 0
    
    // Audio manager for speaker control
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    // ICE server configuration
    private val iceServers = listOf(
        // Google's public STUN servers
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
        // Open Relay Project TURN servers (free)
        PeerConnection.IceServer.builder("stun:openrelay.metered.ca:80").createIceServer(),
        PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80").createIceServer(),
        PeerConnection.IceServer.builder("turn:openrelay.metered.ca:443").createIceServer()
    )
    
    enum class ConnectionQuality {
        EXCELLENT, GOOD, FAIR, POOR, UNKNOWN
    }
    
    init {
        initializeWebRtc()
        setupSignalingListeners()
        startDurationTimer()
    }
    
    /**
     * Initialize WebRTC
     */
    private fun initializeWebRtc() {
        try {
            // Initialize PeerConnectionFactory
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)
            
            // Create factory with video disabled (voice only)
            val encoderFactory = DefaultVideoEncoderFactory(null, true, true)
            val decoderFactory = DefaultVideoDecoderFactory(null)
            
            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .setOptions(PeerConnectionFactory.Options())
                .createPeerConnectionFactory()
            
            Log.d(TAG, "WebRTC initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WebRTC", e)
        }
    }
    
    /**
     * Setup signaling listeners
     */
    private fun setupSignalingListeners() {
        // Listen for incoming calls
        scope.launch {
            signalingService.incomingCall.collect { callInfo ->
                if (_callState.value == CallSignal.CallState.IDLE) {
                    currentCallId = callInfo.callId
                    currentBookingId = callInfo.bookingId
                    remoteUserId = callInfo.callerId
                    
                    _callState.value = CallSignal.CallState.RINGING
                    // Note: UI should show incoming call screen
                }
            }
        }
        
        // Listen for call signals
        scope.launch {
            signalingService.callSignals.collect { signal ->
                handleIncomingSignal(signal)
            }
        }
    }
    
    /**
     * Handle incoming signal
     */
    private suspend fun handleIncomingSignal(signal: CallSignal.SignalMessage) {
        when (signal.type) {
            CallSignal.SignalType.OFFER -> {
                if (_callState.value == CallSignal.CallState.RINGING || _callState.value == CallSignal.CallState.CALLING) {
                    val offerPayload = CallSignal.OfferPayload.fromJson(signal.payload)
                    offerPayload?.let {
                        handleOffer(it.sdp)
                    }
                }
            }
            
            CallSignal.SignalType.ANSWER -> {
                if (_callState.value == CallSignal.CallState.CONNECTING) {
                    val answerPayload = CallSignal.AnswerPayload.fromJson(signal.payload)
                    answerPayload?.let {
                        handleAnswer(it.sdp)
                    }
                }
            }
            
            CallSignal.SignalType.ICE_CANDIDATE -> {
                val icePayload = CallSignal.IceCandidatePayload.fromJson(signal.payload)
                icePayload?.let {
                    handleRemoteIceCandidate(it)
                }
            }
            
            CallSignal.SignalType.CALL_ENDED -> {
                val reason = try {
                    CallSignal.CallEndReason.valueOf(signal.payload ?: "NORMAL")
                } catch (e: Exception) {
                    CallSignal.CallEndReason.NORMAL
                }
                endCallInternal(reason)
            }
            
            CallSignal.SignalType.MUTE -> {
                val muted = signal.payload?.toBoolean() ?: false
                // Notify UI about remote mute state
                Log.d(TAG, "Remote user muted: $muted")
            }
            
            else -> {}
        }
    }
    
    /**
     * Start a call (as caller)
     */
    suspend fun startCall(
        callId: String,
        bookingId: String,
        callerId: String,
        callerRole: CallSignal.CallRole,
        calleeId: String,
        callerName: String
    ) {
        if (_callState.value != CallSignal.CallState.IDLE) {
            Log.w(TAG, "Cannot start call: not idle")
            return
        }
        
        currentCallId = callId
        currentBookingId = bookingId
        currentUserId = callerId
        currentUserRole = callerRole
        remoteUserId = calleeId
        
        // Subscribe to booking signals
        signalingService.subscribeToBookingCalls(bookingId, callerId)
        
        // Create local audio track
        createLocalAudioTrack()
        
        // Create peer connection
        createPeerConnection()
        
        _callState.value = CallSignal.CallState.CALLING
        
        // Send call request
        signalingService.initiateCall(
            callId = callId,
            callerId = callerId,
            callerRole = callerRole,
            calleeId = calleeId,
            bookingId = bookingId,
            callerName = callerName
        )
        
        // Create and send offer
        createOffer()
    }
    
    /**
     * Answer an incoming call
     */
    suspend fun answerCall(
        callId: String,
        bookingId: String,
        userId: String,
        userRole: CallSignal.CallRole,
        callerId: String,
        callerName: String
    ) {
        if (_callState.value != CallSignal.CallState.RINGING) {
            Log.w(TAG, "Cannot answer: not ringing")
            return
        }
        
        currentCallId = callId
        currentBookingId = bookingId
        currentUserId = userId
        currentUserRole = userRole
        remoteUserId = callerId
        
        // Create local audio track
        createLocalAudioTrack()
        
        // Create peer connection
        createPeerConnection()
        
        _callState.value = CallSignal.CallState.CONNECTING
        
        // Create and send answer
        createAnswer()
    }
    
    /**
     * Reject an incoming call
     */
    suspend fun rejectCall() {
        currentCallId?.let { callId ->
            currentUserId?.let { userId ->
                currentUserRole?.let { role ->
                    remoteUserId?.let { calleeId ->
                        currentBookingId?.let { bookingId ->
                            signalingService.endCall(
                                callId = callId,
                                callerId = userId,
                                callerRole = role,
                                calleeId = calleeId,
                                bookingId = bookingId,
                                reason = CallSignal.CallEndReason.REJECTED
                            )
                        }
                    }
                }
            }
        }
        
        cleanup()
    }
    
    /**
     * End the current call
     */
    suspend fun endCall(reason: CallSignal.CallEndReason = CallSignal.CallEndReason.NORMAL) {
        currentCallId?.let { callId ->
            currentUserId?.let { userId ->
                currentUserRole?.let { role ->
                    remoteUserId?.let { calleeId ->
                        currentBookingId?.let { bookingId ->
                            signalingService.endCall(
                                callId = callId,
                                callerId = userId,
                                callerRole = role,
                                calleeId = calleeId,
                                bookingId = bookingId,
                                reason = reason
                            )
                        }
                    }
                }
            }
        }
        
        endCallInternal(reason)
    }
    
    /**
     * End call internally (after signal sent)
     */
    private fun endCallInternal(reason: CallSignal.CallEndReason) {
        cleanup()
        _callState.value = CallSignal.CallState.ENDED
        
        // Reset to idle after a short delay
        scope.launch {
            delay(500)
            _callState.value = CallSignal.CallState.IDLE
        }
    }
    
    /**
     * Toggle mute
     */
    fun toggleMute() {
        val newMuteState = !_isMuted.value
        _isMuted.value = newMuteState
        
        localAudioTrack?.setEnabled(!newMuteState)
        
        // Notify remote
        scope.launch {
            currentCallId?.let { callId ->
                currentUserId?.let { userId ->
                    currentUserRole?.let { role ->
                        remoteUserId?.let { calleeId ->
                            currentBookingId?.let { bookingId ->
                                signalingService.sendMuteState(
                                    callId = callId,
                                    callerId = userId,
                                    callerRole = role,
                                    calleeId = calleeId,
                                    bookingId = bookingId,
                                    muted = newMuteState
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Toggle speaker
     */
    fun toggleSpeaker() {
        val newSpeakerState = !_isSpeakerOn.value
        _isSpeakerOn.value = newSpeakerState
        
        audioManager.isSpeakerphoneOn = newSpeakerState
    }
    
    /**
     * Create local audio track
     */
    private fun createLocalAudioTrack() {
        try {
            // Create audio source
            val audioConstraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
            }
            
            audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
            
            // Create audio track
            localAudioTrack = peerConnectionFactory?.createAudioTrack("audio_track", audioSource)
            localAudioTrack?.setEnabled(true)
            
            Log.d(TAG, "Local audio track created")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create audio track", e)
        }
    }
    
    /**
     * Create peer connection
     */
    private fun createPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
        
        peerConnection = peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            createPeerConnectionObserver()
        )
        
        // Add local audio track
        localAudioTrack?.let { track ->
            peerConnection?.addTrack(track, listOf("stream"))
        }
        
        Log.d(TAG, "Peer connection created")
    }
    
    /**
     * Create peer connection observer
     */
    private fun createPeerConnectionObserver(): PeerConnection.Observer {
        return object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                Log.d(TAG, "Signaling state: $state")
            }
            
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "ICE connection state: $state")
                
                when (state) {
                    PeerConnection.IceConnectionState.CONNECTED -> {
                        _callState.value = CallSignal.CallState.CONNECTED
                        callStartTime = System.currentTimeMillis()
                        setSpeakerphoneOn(true)
                    }
                    PeerConnection.IceConnectionState.DISCONNECTED -> {
                        _callState.value = CallSignal.CallState.RECONNECTING
                    }
                    PeerConnection.IceConnectionState.FAILED, PeerConnection.IceConnectionState.CLOSED -> {
                        scope.launch {
                            endCallInternal(CallSignal.CallEndReason.NETWORK_ERROR)
                        }
                    }
                    else -> {}
                }
            }
            
            override fun onIceConnectionReceivingChange(receiving: Boolean) {
                Log.d(TAG, "ICE connection receiving: $receiving")
            }
            
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                Log.d(TAG, "ICE gathering state: $state")
            }
            
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    scope.launch {
                        signalingService.sendIceCandidate(
                            callId = currentCallId ?: return@launch,
                            callerId = currentUserId ?: return@launch,
                            callerRole = currentUserRole ?: return@launch,
                            calleeId = remoteUserId ?: return@launch,
                            bookingId = currentBookingId ?: return@launch,
                            candidate = it.sdp,
                            sdpMid = it.sdpMid,
                            sdpMLineIndex = it.sdpMLineIndex
                        )
                    }
                }
            }
            
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
                // Handle removed ICE candidates
            }
            
            override fun onAddStream(stream: MediaStream?) {
                // Handle incoming audio stream
                stream?.audioTracks?.forEach { track ->
                    track.setEnabled(true)
                }
            }
            
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {
                Log.d(TAG, "Renegotiation needed")
            }
            
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                receiver?.track?.let { track ->
                    if (track is AudioTrack) {
                        track.setEnabled(true)
                    }
                }
            }
        }
    }
    
    /**
     * Create WebRTC offer
     */
    private suspend fun createOffer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("VoiceActivityDetection", "true"))
        }
        
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            scope.launch {
                                signalingService.sendOffer(
                                    callId = currentCallId ?: return@launch,
                                    callerId = currentUserId ?: return@launch,
                                    callerRole = currentUserRole ?: return@launch,
                                    calleeId = remoteUserId ?: return@launch,
                                    bookingId = currentBookingId ?: return@launch,
                                    sdp = it.sdp
                                )
                            }
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, it)
                }
            }
            
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Failed to create offer: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }
    
    /**
     * Create WebRTC answer
     */
    private suspend fun createAnswer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("VoiceActivityDetection", "true"))
        }
        
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            scope.launch {
                                signalingService.sendAnswer(
                                    callId = currentCallId ?: return@launch,
                                    callerId = currentUserId ?: return@launch,
                                    callerRole = currentUserRole ?: return@launch,
                                    calleeId = remoteUserId ?: return@launch,
                                    bookingId = currentBookingId ?: return@launch,
                                    sdp = it.sdp
                                )
                            }
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, it)
                }
            }
            
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Failed to create answer: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }
    
    /**
     * Handle incoming offer
     */
    private fun handleOffer(sdp: String) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                Log.d(TAG, "Remote description set from offer")
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Failed to set remote description: $error")
            }
        }, SessionDescription(SessionDescription.Type.OFFER, sdp))
    }
    
    /**
     * Handle incoming answer
     */
    private fun handleAnswer(sdp: String) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                Log.d(TAG, "Remote description set from answer")
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Failed to set remote description: $error")
            }
        }, SessionDescription(SessionDescription.Type.ANSWER, sdp))
    }
    
    /**
     * Handle remote ICE candidate
     */
    private fun handleRemoteIceCandidate(payload: CallSignal.IceCandidatePayload) {
        try {
            val candidate = IceCandidate(
                payload.sdpMid,
                payload.sdpMLineIndex ?: 0,
                payload.candidate
            )
            peerConnection?.addIceCandidate(candidate)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add ICE candidate", e)
        }
    }
    
    /**
     * Start duration timer
     */
    private fun startDurationTimer() {
        scope.launch {
            while (true) {
                delay(1000)
                if (_callState.value == CallSignal.CallState.CONNECTED && callStartTime > 0) {
                    _callDuration.value = System.currentTimeMillis() - callStartTime
                }
            }
        }
    }
    
    /**
     * Cleanup resources
     */
    private fun cleanup() {
        try {
            peerConnection?.close()
            peerConnection = null
            
            localAudioTrack?.setEnabled(false)
            localAudioTrack?.dispose()
            localAudioTrack = null
            
            audioSource?.dispose()
            audioSource = null
            
            // Unsubscribe from signals
            currentBookingId?.let { signalingService.unsubscribeFromBookingCalls(it) }
            
            // Reset state
            currentCallId = null
            currentBookingId = null
            currentUserId = null
            currentUserRole = null
            remoteUserId = null
            callStartTime = 0
            _callDuration.value = 0
            _isMuted.value = false
            
            // Reset audio
            setSpeakerphoneOn(false)
            
            Log.d(TAG, "WebRTC cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    /**
     * Set speakerphone on/off
     */
    private fun setSpeakerphoneOn(on: Boolean) {
        try {
            audioManager.isSpeakerphoneOn = on
            _isSpeakerOn.value = on
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set speakerphone", e)
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        cleanup()
        scope.cancel()
        instance = null
    }
    
    companion object {
        private const val TAG = "WebRtcManager"
        
        @Volatile
        private var instance: WebRtcManager? = null
        
        fun getInstance(context: Context): WebRtcManager {
            return instance ?: synchronized(this) {
                instance ?: WebRtcManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
