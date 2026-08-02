package com.vttcabs.common.voice

import com.google.gson.Gson

/**
 * Call signaling data models
 * Used for WebRTC signaling via Supabase Realtime
 */
object CallSignal {
    
    private val gson = Gson()
    
    /**
     * Types of call signals
     */
    enum class SignalType {
        OFFER,          // WebRTC offer from caller
        ANSWER,         // WebRTC answer from callee
        ICE_CANDIDATE,  // ICE candidate exchange
        CALL_REQUEST,   // Initial call request
        CALL_ACCEPTED,  // Call accepted
        CALL_REJECTED,  // Call rejected
        CALL_ENDED,     // Call ended
        CALL_TIMEOUT,   // No answer timeout
        MUTE,           // Mute/unmute notification
        SPEAKER         // Speaker toggle notification
    }
    
    /**
     * Call state
     */
    enum class CallState {
        IDLE,           // No call
        CALLING,        // Outgoing call initiated
        RINGING,        // Incoming call ringing
        CONNECTING,     // Connecting (ICE negotiation)
        CONNECTED,      // Call connected
        RECONNECTING,   // Connection lost, reconnecting
        ENDED           // Call ended
    }
    
    /**
     * Call role in the conversation
     */
    enum class CallRole {
        CALLER,     // Initiated the call
        CALLEE      // Received the call
    }
    
    /**
     * Base signaling message
     */
    data class SignalMessage(
        val type: SignalType,
        val callId: String,
        val callerId: String,
        val callerRole: CallRole,
        val calleeId: String,
        val bookingId: String,
        val timestamp: Long = System.currentTimeMillis(),
        val payload: String? = null
    ) {
        fun toJson(): String = gson.toJson(this)
        
        companion object {
            fun fromJson(json: String): SignalMessage? {
                return try {
                    gson.fromJson(json, SignalMessage::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
    
    /**
     * WebRTC Offer message
     */
    data class OfferPayload(
        val sdp: String
    ) {
        fun toJson(): String = gson.toJson(this)
        
        companion object {
            fun fromJson(json: String?): OfferPayload? {
                return json?.let {
                    try {
                        gson.fromJson(it, OfferPayload::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }
    
    /**
     * WebRTC Answer message
     */
    data class AnswerPayload(
        val sdp: String
    ) {
        fun toJson(): String = gson.toJson(this)
        
        companion object {
            fun fromJson(json: String?): AnswerPayload? {
                return json?.let {
                    try {
                        gson.fromJson(it, AnswerPayload::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }
    
    /**
     * ICE Candidate payload
     */
    data class IceCandidatePayload(
        val candidate: String,
        val sdpMid: String?,
        val sdpMLineIndex: Int?
    ) {
        fun toJson(): String = gson.toJson(this)
        
        companion object {
            fun fromJson(json: String?): IceCandidatePayload? {
                return json?.let {
                    try {
                        gson.fromJson(it, IceCandidatePayload::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }
    
    /**
     * Call request payload
     */
    data class CallRequestPayload(
        val callerName: String,
        val callerRole: CallRole,
        val bookingId: String,
        val avatarUrl: String? = null
    ) {
        fun toJson(): String = gson.toJson(this)
        
        companion object {
            fun fromJson(json: String?): CallRequestPayload? {
                return json?.let {
                    try {
                        gson.fromJson(it, CallRequestPayload::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }
    
    /**
     * Call end reason
     */
    enum class CallEndReason {
        NORMAL,             // Normal end
        REJECTED,           // Call rejected
        MISSED,             // Missed call (timeout)
        TRIP_ENDED,         // Trip completed
        TRIP_CANCELLED,     // Trip cancelled
        NETWORK_ERROR,      // Network error
        ERROR               // Generic error
    }
    
    /**
     * Helper to create signal messages
     */
    object Builder {
        fun callRequest(
            callId: String,
            callerId: String,
            callerRole: CallRole,
            calleeId: String,
            bookingId: String,
            callerName: String,
            avatarUrl: String? = null
        ): SignalMessage {
            val payload = CallRequestPayload(callerName, callerRole, bookingId, avatarUrl)
            return SignalMessage(
                type = SignalType.CALL_REQUEST,
                callId = callId,
                callerId = callerId,
                callerRole = callerRole,
                calleeId = calleeId,
                bookingId = bookingId,
                payload = payload.toJson()
            )
        }
        
        fun offer(callId: String, callerId: String, callerRole: CallRole, calleeId: String, bookingId: String, sdp: String): SignalMessage {
            val payload = OfferPayload(sdp)
            return SignalMessage(
                type = SignalType.OFFER,
                callId = callId,
                callerId = callerId,
                callerRole = callerRole,
                calleeId = calleeId,
                bookingId = bookingId,
                payload = payload.toJson()
            )
        }
        
        fun answer(callId: String, callerId: String, callerRole: CallRole, calleeId: String, bookingId: String, sdp: String): SignalMessage {
            val payload = AnswerPayload(sdp)
            return SignalMessage(
                type = SignalType.ANSWER,
                callId = callId,
                callerId = callerId,
                callerRole = callerRole,
                calleeId = calleeId,
                bookingId = bookingId,
                payload = payload.toJson()
            )
        }
        
        fun iceCandidate(
            callId: String,
            callerId: String,
            callerRole: CallRole,
            calleeId: String,
            bookingId: String,
            candidate: String,
            sdpMid: String?,
            sdpMLineIndex: Int?
        ): SignalMessage {
            val payload = IceCandidatePayload(candidate, sdpMid, sdpMLineIndex)
            return SignalMessage(
                type = SignalType.ICE_CANDIDATE,
                callId = callId,
                callerId = callerId,
                callerRole = callerRole,
                calleeId = calleeId,
                bookingId = bookingId,
                payload = payload.toJson()
            )
        }
        
        fun callEnded(
            callId: String,
            callerId: String,
            callerRole: CallRole,
            calleeId: String,
            bookingId: String,
            reason: CallEndReason
        ): SignalMessage {
            return SignalMessage(
                type = SignalType.CALL_ENDED,
                callId = callId,
                callerId = callerId,
                callerRole = callerRole,
                calleeId = calleeId,
                bookingId = bookingId,
                payload = reason.name
            )
        }
        
        fun muted(callId: String, callerId: String, callerRole: CallRole, calleeId: String, bookingId: String, muted: Boolean): SignalMessage {
            return SignalMessage(
                type = SignalType.MUTE,
                callId = callId,
                callerId = callerId,
                callerRole = callerRole,
                calleeId = calleeId,
                bookingId = bookingId,
                payload = muted.toString()
            )
        }
    }
}
