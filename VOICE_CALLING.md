# In-App Voice Calling - Implementation Complete

## Overview

Free in-app voice calling using WebRTC with Supabase Realtime for signaling. No phone numbers exposed, E2E encrypted, only works during active trips.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      Voice Call Architecture                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐                          │
│  │ Customer App │◄──►│  Driver App  │                         │
│  └──────┬───────┘    └──────┬───────┘                          │
│         │                   │                                   │
│         │    WebRTC Peer Connection (P2P)                      │
│         │                   │                                   │
│         └────────┬──────────┘                                   │
│                  │                                               │
│         ┌───────▼────────┐                                      │
│         │ Supabase        │                                      │
│         │ Realtime        │  ← Signaling Channel                │
│         │ (WebSocket)     │                                      │
│         └─────────────────┘                                      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Components

### 1. CallSignal (`CallSignal.kt`)
Signal message types and data models for WebRTC negotiation.

**Signal Types:**
- `CALL_REQUEST` - Initial call request
- `OFFER` - WebRTC SDP offer
- `ANSWER` - WebRTC SDP answer
- `ICE_CANDIDATE` - ICE candidate exchange
- `CALL_ENDED` - Call termination
- `MUTE` - Mute state sync
- `SPEAKER` - Speaker toggle sync

### 2. WebRtcSignalingService (`WebRtcSignalingService.kt`)
Handles signaling via Supabase Realtime channels.

**Features:**
- Connects to Supabase Realtime
- Subscribes to booking-specific channels
- Sends/receives WebRTC signals
- Broadcasts to connected parties

### 3. WebRtcManager (`WebRtcManager.kt`)
Core WebRTC peer connection management.

**Features:**
- Creates peer-to-peer connection
- Manages local/remote audio tracks
- Handles ICE negotiation
- Audio controls (mute, speaker)
- Connection quality monitoring

### 4. VoiceCallManager (`VoiceCallManager.kt`)
High-level call interface with trip integration.

**Features:**
- Call initiation during active trips only
- Auto-end when trip completes/cancels
- State management (incoming, active, ended)
- Coordinates WebRTC and signaling

### 5. CallUI (`CallUI.kt`)
Jetpack Compose UI components.

**Screens:**
- `IncomingCallScreen` - Ringing call display
- `ActiveCallScreen` - In-call controls
- `CallEndedScreen` - Post-call summary
- `VoiceCallButton` - FAB for initiating calls

---

## How It Works

### 1. Call Initiation

```
Customer wants to call Driver
        │
        ▼
┌─────────────────────────────────────┐
│ VoiceCallManager.initiateCall()     │
│                                     │
│ 1. Verify active trip exists         │
│ 2. Generate unique callId            │
│ 3. WebRtcManager.startCall()        │
│    ├── Create audio track           │
│    ├── Create peer connection       │
│    └── Create SDP offer             │
│ 4. Send CALL_REQUEST via Realtime   │
│ 5. Wait for CALL_ACCEPTED           │
│ 6. Exchange ICE candidates          │
│ 7. Call connected!                 │
└─────────────────────────────────────┘
```

### 2. WebRTC Negotiation

```
Caller creates OFFER
        │
        ├──► Send to Supabase Realtime
        │
Callee ◄─┘ Receives OFFER
        │
        ├──► Create ANSWER
        │
        ├──► Send to Supabase Realtime
        │
Both ────► Exchange ICE candidates
        │
        ├──► Peer connection established
        │
        └──► Audio streams connected
```

### 3. Call During Trip

```
Trip Active (Customer ←→ Driver)
        │
        ▼
┌─────────────────────────────────────┐
│ VoiceCallManager.onTripStarted()    │
│  - Subscribe to call signals         │
│  - Enable call button               │
└─────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────┐
│ User taps call button               │
│  - Check hasActiveTrip = true       │
│  - Initiate call via WebRTC         │
└─────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────┐
│ Call connected (P2P via WebRTC)     │
│  - Audio streams active              │
│  - Duration timer running            │
└─────────────────────────────────────┘
```

### 4. Trip Ends - Auto Call End

```
Trip Completed/Cancelled
        │
        ▼
┌─────────────────────────────────────┐
│ VoiceCallManager.onTripEnded()      │
│                                     │
│ 1. If call active → endCall()      │
│ 2. Send CALL_ENDED signal           │
│ 3. Cleanup WebRTC resources          │
│ 4. Unsubscribe from signals         │
│ 5. Set hasActiveTrip = false         │
└─────────────────────────────────────┘
```

---

## Security Features

### ✅ Implemented

1. **No Phone Numbers**
   - Calls use internal user IDs only
   - No phone number exposure
   - Anonymous P2P connection

2. **End-to-End Encryption**
   - WebRTC SRTP encryption
   - Audio encrypted in transit
   - ICE servers for NAT traversal only

3. **Trip-Limited Access**
   - Calls only during active trips
   - No calls before/after trip
   - Auto-disabled on trip end

4. **No Admin Involvement**
   - Direct P2P connection
   - Admin not in call path
   - Signaling only via Realtime

---

## ICE Servers (STUN/TURN)

Used for NAT traversal and connectivity:

```
stun:stun.l.google.com:19302
stun:stun1.l.google.com:19302
stun:stun2.l.google.com:19302
stun:openrelay.metered.ca:80
turn:openrelay.metered.ca:80
turn:openrelay.metered.ca:443
```

These are free, public servers from Google and Open Relay Project.

---

## Integration Points

### Driver App - Start Trip
```kotlin
val voiceCallManager = VoiceCallManager.getInstance(context)

// When driver starts a trip
voiceCallManager.onTripStarted(
    bookingId = "BK123",
    userId = driverId,
    userRole = CallRole.CALLER,
    userName = "John Driver"
)
```

### Driver App - End Trip
```kotlin
// When trip completes
voiceCallManager.onTripEnded(
    bookingId = "BK123",
    reason = CallEndReason.TRIP_ENDED
)
```

### Customer App - Make Call
```kotlin
// User taps call button
if (voiceCallManager.isVoiceCallAvailable()) {
    voiceCallManager.initiateCall(
        remoteUserId = driverId,
        remoteUserName = "John Driver"
    )
}
```

### Handle Incoming Calls
```kotlin
// Observe incoming calls
voiceCallManager.incomingCall.collect { incoming ->
    incoming?.let {
        showIncomingCallScreen(it)
    }
}
```

---

## UI Flow

### Customer App

```
Home Screen
    │
    ├──► Tap Call Button (during trip)
    │
    ▼
Calling Screen
    │
    ├──► Call Connected ──► Active Call Screen
    │                            │
    │                            ├──► Mute/Unmute
    │                            ├──► Speaker Toggle
    │                            └──► End Call ──► Call Ended
    │
    └──► Call Rejected/Missed ──► Call Ended
```

### Driver App

```
Incoming Call Screen (while on trip)
    │
    ├──► Accept ──► Active Call Screen
    │
    └──► Reject ──► Back to Dashboard
```

---

## Files Created

```
common/src/main/java/com/vttcabs/common/voice/
├── CallSignal.kt           # Signal types and models
├── WebRtcSignalingService.kt # Supabase Realtime signaling
├── WebRtcManager.kt       # WebRTC peer connection
├── VoiceCallManager.kt     # High-level call interface
└── CallUI.kt             # Compose UI components
```

---

## Dependencies Required

Add to `build.gradle` (app level):

```groovy
dependencies {
    // WebRTC
    implementation 'io.getstream:stream-webrtc-android:1.0.2'
    
    // Supabase Realtime (if not already included)
    implementation 'io.supabase:supabase-kt:2.0.0'
    
    // Socket.IO for WebSocket (Supabase uses this)
    implementation 'io.socket:socket.io-client:2.1.0'
}
```

---

## Configuration

### WebRTC Audio Constraints

```kotlin
val audioConstraints = MediaConstraints().apply {
    mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
    mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
    mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
    mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
}
```

### ICE Servers

```kotlin
val iceServers = listOf(
    PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
    PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
    PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
)
```

---

## Testing Checklist

- [ ] Customer can call Driver during active trip
- [ ] Driver can call Customer during active trip
- [ ] Call button disabled when no active trip
- [ ] Call ends automatically when trip completes
- [ ] Call ends automatically when trip cancelled
- [ ] Incoming call shows for the other party
- [ ] Mute works correctly
- [ ] Speaker toggle works
- [ ] Call duration shown correctly
- [ ] No phone numbers visible in UI

---

## Next Steps

1. **Add WebRTC dependency** to Gradle files
2. **Integrate with Driver Dashboard** - Add call button
3. **Integrate with Customer Trip Screen** - Add call button
4. **Handle incoming calls** - Show incoming call overlay
5. **Test E2E** - Customer ↔ Driver voice call

---

## Cost Analysis

| Component | Cost |
|-----------|------|
| WebRTC | FREE (open source) |
| Supabase Realtime | FREE (included in free tier) |
| STUN/TURN servers | FREE (public servers) |
| **Total** | **$0/month** |

---

## Troubleshooting

### "Call not connecting"
- Check both users are on active trips
- Verify internet connectivity
- Check Supabase Realtime is working

### "Audio not working"
- Check microphone permissions
- Check speaker is enabled
- Verify audio track is created

### "Signaling failed"
- Check Supabase project is active
- Verify Realtime is enabled
- Check network connectivity
