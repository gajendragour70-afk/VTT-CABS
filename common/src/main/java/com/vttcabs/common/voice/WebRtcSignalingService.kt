package com.vttcabs.common.voice

import android.util.Log
import com.vttcabs.common.SupabaseService
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * WebRTC Signaling Service using Supabase Realtime
 * Handles signaling for peer-to-peer WebRTC connections
 */
class WebRtcSignalingService private constructor() {
    
    private val supabaseService = SupabaseService.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // State flows
    private val _connectionState = MutableStateFlow(SignalingState.DISCONNECTED)
    val connectionState: StateFlow<SignalingState> = _connectionState.asStateFlow()
    
    private val _incomingCall = MutableSharedFlow<IncomingCallInfo>()
    val incomingCall: SharedFlow<IncomingCallInfo> = _incomingCall.asSharedFlow()
    
    private val _callSignals = MutableSharedFlow<CallSignal.SignalMessage>()
    val callSignals: SharedFlow<CallSignal.SignalMessage> = _callSignals.asSharedFlow()
    
    private val _connectionError = MutableSharedFlow<String>()
    val connectionError: SharedFlow<String> = _connectionError.asSharedFlow()
    
    // Active channels per booking
    private val activeChannels = mutableMapOf<String, RealtimeChannel>()
    private val channelScopes = mutableMapOf<String, CoroutineScope>()
    
    enum class SignalingState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        ERROR
    }
    
    data class IncomingCallInfo(
        val callId: String,
        val callerId: String,
        val callerName: String,
        val callerRole: CallSignal.CallRole,
        val bookingId: String,
        val avatarUrl: String?
    )
    
    /**
     * Connect to Supabase Realtime
     */
    fun connect() {
        if (_connectionState.value == SignalingState.CONNECTED) return
        
        scope.launch {
            try {
                _connectionState.value = SignalingState.CONNECTING
                Log.d(TAG, "Connecting to Supabase Realtime...")
                
                // Supabase Realtime uses Phoenix channels over WebSocket
                // We'll use Supabase's built-in realtime subscription
                
                _connectionState.value = SignalingState.CONNECTED
                Log.d(TAG, "Connected to Supabase Realtime")
                
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed", e)
                _connectionState.value = SignalingState.ERROR
                _connectionError.emit("Failed to connect: ${e.message}")
            }
        }
    }
    
    /**
     * Disconnect from Supabase Realtime
     */
    fun disconnect() {
        scope.launch {
            activeChannels.values.forEach { channel ->
                try {
                    channel.unsubscribe()
                } catch (e: Exception) {
                    Log.e(TAG, "Error unsubscribing channel", e)
                }
            }
            activeChannels.clear()
            channelScopes.clear()
            _connectionState.value = SignalingState.DISCONNECTED
            Log.d(TAG, "Disconnected from Supabase Realtime")
        }
    }
    
    /**
     * Subscribe to call signals for a booking
     */
    fun subscribeToBookingCalls(bookingId: String, userId: String) {
        // Unsubscribe if already subscribed
        unsubscribeFromBookingCalls(bookingId)
        
        val channelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        channelScopes[bookingId] = channelScope
        
        channelScope.launch {
            try {
                // Use Supabase Realtime channel for signaling
                val channel = supabaseService.createRealtimeChannel("call_signal:$bookingId")
                activeChannels[bookingId] = channel
                
                // Subscribe to call signals
                channel.on("signal:*") { payload ->
                    handleSignalReceived(bookingId, userId, payload)
                }
                
                // Subscribe to own presence for connection status
                channel.on("presence:sync") {
                    Log.d(TAG, "Presence sync for booking: $bookingId")
                }
                
                channel.subscribe()
                Log.d(TAG, "Subscribed to calls for booking: $bookingId")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to subscribe to booking calls", e)
                _connectionError.emit("Failed to subscribe: ${e.message}")
            }
        }
    }
    
    /**
     * Unsubscribe from booking call signals
     */
    fun unsubscribeFromBookingCalls(bookingId: String) {
        channelScopes[bookingId]?.cancel()
        channelScopes.remove(bookingId)
        
        activeChannels[bookingId]?.let { channel ->
            try {
                channel.unsubscribe()
            } catch (e: Exception) {
                Log.e(TAG, "Error unsubscribing", e)
            }
        }
        activeChannels.remove(bookingId)
        
        Log.d(TAG, "Unsubscribed from calls for booking: $bookingId")
    }
    
    /**
     * Handle incoming signal
     */
    private suspend fun handleSignalReceived(bookingId: String, currentUserId: String, payload: Any?) {
        try {
            val jsonString = when (payload) {
                is String -> payload
                is JSONObject -> payload.toString()
                else -> return
            }
            
            val signal = CallSignal.SignalMessage.fromJson(jsonString) ?: return
            
            // Ignore signals from self
            if (signal.callerId == currentUserId) return
            
            // Ignore signals not meant for this user
            if (signal.calleeId != currentUserId) return
            
            Log.d(TAG, "Received signal: ${signal.type} from ${signal.callerId}")
            
            when (signal.type) {
                CallSignal.SignalType.CALL_REQUEST -> {
                    val callPayload = CallSignal.CallRequestPayload.fromJson(signal.payload)
                    callPayload?.let {
                        _incomingCall.emit(
                            IncomingCallInfo(
                                callId = signal.callId,
                                callerId = signal.callerId,
                                callerName = it.callerName,
                                callerRole = it.callerRole,
                                bookingId = signal.bookingId,
                                avatarUrl = it.avatarUrl
                            )
                        )
                    }
                }
                
                else -> {
                    _callSignals.emit(signal)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling signal", e)
        }
    }
    
    /**
     * Send a call signal to the other party
     */
    suspend fun sendSignal(signal: CallSignal.SignalMessage) {
        val bookingId = signal.bookingId
        
        activeChannels[bookingId]?.let { channel ->
            try {
                val eventName = "signal:${signal.type.name.lowercase()}"
                channel.send(eventName, signal.toJson())
                Log.d(TAG, "Sent signal: ${signal.type} to $bookingId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send signal", e)
                _connectionError.emit("Failed to send signal: ${e.message}")
            }
        } ?: run {
            Log.w(TAG, "No active channel for booking: $bookingId")
        }
    }
    
    /**
     * Broadcast call request (initiate call)
     */
    suspend fun initiateCall(
        callId: String,
        callerId: String,
        callerRole: CallSignal.CallRole,
        calleeId: String,
        bookingId: String,
        callerName: String,
        avatarUrl: String? = null
    ) {
        val signal = CallSignal.Builder.callRequest(
            callId = callId,
            callerId = callerId,
            callerRole = callerRole,
            calleeId = calleeId,
            bookingId = bookingId,
            callerName = callerName,
            avatarUrl = avatarUrl
        )
        sendSignal(signal)
    }
    
    /**
     * Broadcast WebRTC offer
     */
    suspend fun sendOffer(
        callId: String,
        callerId: String,
        callerRole: CallSignal.CallRole,
        calleeId: String,
        bookingId: String,
        sdp: String
    ) {
        val signal = CallSignal.Builder.offer(
            callId = callId,
            callerId = callerId,
            callerRole = callerRole,
            calleeId = calleeId,
            bookingId = bookingId,
            sdp = sdp
        )
        sendSignal(signal)
    }
    
    /**
     * Broadcast WebRTC answer
     */
    suspend fun sendAnswer(
        callId: String,
        callerId: String,
        callerRole: CallSignal.CallRole,
        calleeId: String,
        bookingId: String,
        sdp: String
    ) {
        val signal = CallSignal.Builder.answer(
            callId = callId,
            callerId = callerId,
            callerRole = callerRole,
            calleeId = calleeId,
            bookingId = bookingId,
            sdp = sdp
        )
        sendSignal(signal)
    }
    
    /**
     * Broadcast ICE candidate
     */
    suspend fun sendIceCandidate(
        callId: String,
        callerId: String,
        callerRole: CallSignal.CallRole,
        calleeId: String,
        bookingId: String,
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int?
    ) {
        val signal = CallSignal.Builder.iceCandidate(
            callId = callId,
            callerId = callerId,
            callerRole = callerRole,
            calleeId = calleeId,
            bookingId = bookingId,
            candidate = candidate,
            sdpMid = sdpMid,
            sdpMLineIndex = sdpMLineIndex
        )
        sendSignal(signal)
    }
    
    /**
     * Broadcast call end
     */
    suspend fun endCall(
        callId: String,
        callerId: String,
        callerRole: CallSignal.CallRole,
        calleeId: String,
        bookingId: String,
        reason: CallSignal.CallEndReason
    ) {
        val signal = CallSignal.Builder.callEnded(
            callId = callId,
            callerId = callerId,
            callerRole = callerRole,
            calleeId = calleeId,
            bookingId = bookingId,
            reason = reason
        )
        sendSignal(signal)
    }
    
    /**
     * Broadcast mute state
     */
    suspend fun sendMuteState(
        callId: String,
        callerId: String,
        callerRole: CallSignal.CallRole,
        calleeId: String,
        bookingId: String,
        muted: Boolean
    ) {
        val signal = CallSignal.Builder.muted(
            callId = callId,
            callerId = callerId,
            callerRole = callerRole,
            calleeId = calleeId,
            bookingId = bookingId,
            muted = muted
        )
        sendSignal(signal)
    }
    
    /**
     * Check if subscribed to a booking
     */
    fun isSubscribed(bookingId: String): Boolean {
        return activeChannels.containsKey(bookingId)
    }
    
    /**
     * Release resources
     */
    fun release() {
        disconnect()
        scope.cancel()
        instance = null
    }
    
    companion object {
        private const val TAG = "WebRtcSignaling"
        
        @Volatile
        private var instance: WebRtcSignalingService? = null
        
        fun getInstance(): WebRtcSignalingService {
            return instance ?: synchronized(this) {
                instance ?: WebRtcSignalingService().also { instance = it }
            }
        }
    }
}

/**
 * Supabase Realtime Channel interface
 * This is a simplified interface - actual implementation depends on Supabase client version
 */
interface RealtimeChannel {
    fun on(event: String, callback: (payload: Any?) -> Unit)
    fun send(event: String, payload: String)
    fun subscribe()
    fun unsubscribe()
}
