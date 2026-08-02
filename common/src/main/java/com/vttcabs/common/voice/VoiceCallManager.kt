package com.vttcabs.common.voice

import android.content.Context
import android.util.Log
import com.vttcabs.common.offline.OfflineSafetyService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

/**
 * Voice Call Manager
 * High-level interface for voice calling during trips
 * 
 * Features:
 * - Voice calls only during active trips
 * - Auto-end when trip completes/cancels
 * - No phone numbers exposed
 * - E2E encrypted via WebRTC
 * - Uses Supabase Realtime for signaling
 */
class VoiceCallManager private constructor(private val context: Context) {
    
    private val applicationContext = context.applicationContext
    private val webRtcManager = WebRtcManager.getInstance(context)
    private val signalingService = WebRtcSignalingService.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // State flows
    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()
    
    private val _callInfo = MutableStateFlow<ActiveCallInfo?>(null)
    val callInfo: StateFlow<ActiveCallInfo?> = _callInfo.asStateFlow()
    
    private val _incomingCall = MutableStateFlow<IncomingCallState?>(null)
    val incomingCall: StateFlow<IncomingCallState?> = _incomingCall.asStateFlow()
    
    private val _hasActiveTrip = MutableStateFlow(false)
    val hasActiveTrip: StateFlow<Boolean> = _hasActiveTrip.asStateFlow()
    
    private val _callError = MutableSharedFlow<String>()
    val callError: SharedFlow<String> = _callError.asSharedFlow()
    
    // Active trip booking ID
    private var activeBookingId: String? = null
    private var currentUserId: String? = null
    private var currentUserRole: CallSignal.CallRole? = null
    private var currentUserName: String? = null
    
    data class ActiveCallInfo(
        val callId: String,
        val bookingId: String,
        val remoteUserId: String,
        val remoteUserName: String,
        val remoteUserRole: CallSignal.CallRole,
        val callState: CallSignal.CallState,
        val isMuted: Boolean,
        val isSpeakerOn: Boolean,
        val durationSeconds: Long,
        val startedAt: Long
    )
    
    data class IncomingCallState(
        val callId: String,
        val callerId: String,
        val callerName: String,
        val callerRole: CallSignal.CallRole,
        val bookingId: String,
        val ringingSince: Long = System.currentTimeMillis()
    )
    
    init {
        setupObservers()
    }
    
    /**
     * Setup state observers
     */
    private fun setupObservers() {
        // Observe call state from WebRTC
        scope.launch {
            webRtcManager.callState.collect { state ->
                updateCallInfo(state)
                
                when (state) {
                    CallSignal.CallState.CONNECTED -> {
                        _isCallActive.value = true
                    }
                    CallSignal.CallState.ENDED, CallSignal.CallState.IDLE -> {
                        _isCallActive.value = false
                        _callInfo.value = null
                    }
                    else -> {}
                }
            }
        }
        
        // Observe incoming calls
        scope.launch {
            signalingService.incomingCall.collect { callInfo ->
                // Only show incoming call if we have an active trip
                if (_hasActiveTrip.value && callInfo.bookingId == activeBookingId) {
                    _incomingCall.value = IncomingCallState(
                        callId = callInfo.callId,
                        callerId = callInfo.callerId,
                        callerName = callInfo.callerName,
                        callerRole = callInfo.callerRole,
                        bookingId = callInfo.bookingId
                    )
                }
            }
        }
    }
    
    /**
     * Update call info from WebRTC state
     */
    private fun updateCallInfo(state: CallSignal.CallState) {
        _callInfo.value?.let { current ->
            _callInfo.value = current.copy(callState = state)
        }
    }
    
    // ==================== TRIP LIFECYCLE ====================
    
    /**
     * Called when user starts an active trip
     * Enables voice calling for this trip
     */
    fun onTripStarted(bookingId: String, userId: String, userRole: CallSignal.CallRole, userName: String) {
        activeBookingId = bookingId
        currentUserId = userId
        currentUserRole = userRole
        currentUserName = userName
        _hasActiveTrip.value = true
        
        // Connect to signaling
        signalingService.connect()
        
        // Subscribe to calls for this booking
        signalingService.subscribeToBookingCalls(bookingId, userId)
        
        Log.d(TAG, "Voice calling enabled for trip: $bookingId")
    }
    
    /**
     * Called when trip ends (completed or cancelled)
     * Automatically ends any active call
     */
    suspend fun onTripEnded(bookingId: String, reason: CallSignal.CallEndReason = CallSignal.CallEndReason.TRIP_ENDED) {
        if (activeBookingId != bookingId) return
        
        Log.d(TAG, "Trip ended: $bookingId, ending call...")
        
        // End call if active
        if (_isCallActive.value) {
            endCall(reason)
        }
        
        // Disable voice calling
        _hasActiveTrip.value = false
        activeBookingId = null
        
        // Unsubscribe from signals
        signalingService.unsubscribeFromBookingCalls(bookingId)
        
        Log.d(TAG, "Voice calling disabled for trip: $bookingId")
    }
    
    // ==================== CALL OPERATIONS ====================
    
    /**
     * Initiate a voice call
     * Can only be called during an active trip
     */
    suspend fun initiateCall(remoteUserId: String, remoteUserName: String): Result<Unit> {
        if (!_hasActiveTrip.value) {
            return Result.failure(Exception("No active trip. Voice calls are only available during trips."))
        }
        
        val bookingId = activeBookingId ?: return Result.failure(Exception("No active booking"))
        val userId = currentUserId ?: return Result.failure(Exception("Not logged in"))
        val userRole = currentUserRole ?: return Result.failure(Exception("Unknown user role"))
        val userName = currentUserName ?: return Result.failure(Exception("Unknown user name"))
        
        if (_isCallActive.value) {
            return Result.failure(Exception("Call already in progress"))
        }
        
        try {
            val callId = generateCallId()
            
            // Create call info
            _callInfo.value = ActiveCallInfo(
                callId = callId,
                bookingId = bookingId,
                remoteUserId = remoteUserId,
                remoteUserName = remoteUserName,
                remoteUserRole = if (userRole == CallSignal.CallRole.CALLER) CallSignal.CallRole.CALLEE else CallSignal.CallRole.CALLER,
                callState = CallSignal.CallState.CALLING,
                isMuted = false,
                isSpeakerOn = true,
                durationSeconds = 0,
                startedAt = System.currentTimeMillis()
            )
            
            // Clear any incoming call state
            _incomingCall.value = null
            
            // Start the call via WebRTC
            webRtcManager.startCall(
                callId = callId,
                bookingId = bookingId,
                callerId = userId,
                callerRole = userRole,
                calleeId = remoteUserId,
                callerName = userName
            )
            
            Log.d(TAG, "Initiated call to: $remoteUserName")
            return Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate call", e)
            _callError.emit("Failed to start call: ${e.message}")
            return Result.failure(e)
        }
    }
    
    /**
     * Answer an incoming call
     */
    suspend fun answerCall(): Result<Unit> {
        val incoming = _incomingCall.value ?: return Result.failure(Exception("No incoming call"))
        
        if (!_hasActiveTrip.value || incoming.bookingId != activeBookingId) {
            return Result.failure(Exception("Cannot answer: trip not active"))
        }
        
        try {
            val userId = currentUserId ?: return Result.failure(Exception("Not logged in"))
            val userRole = currentUserRole ?: return Result.failure(Exception("Unknown user role"))
            val userName = currentUserName ?: return Result.failure(Exception("Unknown user name"))
            
            // Create call info
            _callInfo.value = ActiveCallInfo(
                callId = incoming.callId,
                bookingId = incoming.bookingId,
                remoteUserId = incoming.callerId,
                remoteUserName = incoming.callerName,
                remoteUserRole = incoming.callerRole,
                callState = CallSignal.CallState.CONNECTING,
                isMuted = false,
                isSpeakerOn = true,
                durationSeconds = 0,
                startedAt = System.currentTimeMillis()
            )
            
            // Clear incoming call state
            _incomingCall.value = null
            
            // Answer via WebRTC
            webRtcManager.answerCall(
                callId = incoming.callId,
                bookingId = incoming.bookingId,
                userId = userId,
                userRole = userRole,
                callerId = incoming.callerId,
                callerName = userName
            )
            
            Log.d(TAG, "Answered call from: ${incoming.callerName}")
            return Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to answer call", e)
            _callError.emit("Failed to answer call: ${e.message}")
            return Result.failure(e)
        }
    }
    
    /**
     * Reject an incoming call
     */
    suspend fun rejectCall() {
        val incoming = _incomingCall.value ?: return
        
        try {
            webRtcManager.rejectCall()
            _incomingCall.value = null
            Log.d(TAG, "Rejected call from: ${incoming.callerName}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reject call", e)
        }
    }
    
    /**
     * End the current call
     */
    suspend fun endCall(reason: CallSignal.CallEndReason = CallSignal.CallEndReason.NORMAL) {
        try {
            webRtcManager.endCall(reason)
            _callInfo.value = null
            Log.d(TAG, "Call ended: $reason")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to end call", e)
        }
    }
    
    /**
     * Toggle mute
     */
    fun toggleMute() {
        webRtcManager.toggleMute()
        
        _callInfo.value?.let { info ->
            _callInfo.value = info.copy(isMuted = !info.isMuted)
        }
    }
    
    /**
     * Toggle speaker
     */
    fun toggleSpeaker() {
        webRtcManager.toggleSpeaker()
        
        _callInfo.value?.let { info ->
            _callInfo.value = info.copy(isSpeakerOn = !info.isSpeakerOn)
        }
    }
    
    // ==================== UTILITY ====================
    
    /**
     * Check if voice call is available
     */
    fun isVoiceCallAvailable(): Boolean {
        return _hasActiveTrip.value && !_isCallActive.value
    }
    
    /**
     * Get call state description
     */
    fun getCallStateDescription(): String {
        val state = webRtcManager.callState.value
        return when (state) {
            CallSignal.CallState.IDLE -> "Not in call"
            CallSignal.CallState.CALLING -> "Calling..."
            CallSignal.CallState.RINGING -> "Ringing..."
            CallSignal.CallState.CONNECTING -> "Connecting..."
            CallSignal.CallState.CONNECTED -> "Connected"
            CallSignal.CallState.RECONNECTING -> "Reconnecting..."
            CallSignal.CallState.ENDED -> "Call ended"
        }
    }
    
    /**
     * Format call duration
     */
    fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / 1000 / 60) % 60
        val hours = durationMs / 1000 / 60 / 60
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    /**
     * Generate unique call ID
     */
    private fun generateCallId(): String {
        return "call_${UUID.randomUUID().toString().take(8)}"
    }
    
    /**
     * Release resources
     */
    fun release() {
        scope.cancel()
        webRtcManager.release()
        signalingService.disconnect()
        instance = null
    }
    
    companion object {
        private const val TAG = "VoiceCallManager"
        
        @Volatile
        private var instance: VoiceCallManager? = null
        
        fun getInstance(context: Context): VoiceCallManager {
            return instance ?: synchronized(this) {
                instance ?: VoiceCallManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
